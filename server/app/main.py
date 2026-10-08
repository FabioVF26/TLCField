import json
import os
from datetime import datetime, timezone
from typing import Any

from fastapi import Depends, FastAPI, Header, HTTPException
from pydantic import BaseModel, ConfigDict
from sqlalchemy import DateTime, String, Text, create_engine
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column, sessionmaker

DATABASE_URL = os.getenv("DATABASE_URL", "postgresql+psycopg://tlcfield:change_me_now@localhost:5432/tlcfield")
API_TOKEN = os.getenv("TLCFIELD_API_TOKEN", "change_this_token")

engine = create_engine(DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)

class Base(DeclarativeBase):
    pass

class InterventionRow(Base):
    __tablename__ = "interventions"
    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    site_id: Mapped[str] = mapped_column(String(128), index=True)
    site_name: Mapped[str] = mapped_column(String(255), index=True)
    timestamp_ms: Mapped[str] = mapped_column(String(32), index=True)
    payload_json: Mapped[str] = mapped_column(Text)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))


class BugReportRow(Base):
    __tablename__ = "bug_reports"
    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    personnel_id: Mapped[str] = mapped_column(String(32), index=True)
    full_name: Mapped[str] = mapped_column(String(255), index=True)
    timestamp_ms: Mapped[str] = mapped_column(String(32), index=True)
    payload_json: Mapped[str] = mapped_column(Text)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))


Base.metadata.create_all(engine)
app = FastAPI(title="TLC Field API", version="1.5.0")

class InterventionPayload(BaseModel):
    model_config = ConfigDict(extra="allow")
    id: str
    siteId: str
    siteName: str
    timestamp: int


class BugReportPayload(BaseModel):
    model_config = ConfigDict(extra="allow")
    id: str
    personnelId: int
    qualification: str = ""
    fullName: str
    notes: str
    timestamp: int
    appVersion: str = ""


def auth(authorization: str | None = Header(default=None)):
    if API_TOKEN and API_TOKEN != "disabled":
        expected = f"Bearer {API_TOKEN}"
        if authorization != expected:
            raise HTTPException(status_code=401, detail="Unauthorized")


def db_session():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/api/v1/health")
def health(_: None = Depends(auth)):
    return {"ok": True, "service": "tlc-field-api", "version": "1.5.0"}

@app.post("/api/v1/interventions")
def upsert_intervention(payload: InterventionPayload, _: None = Depends(auth), db: Session = Depends(db_session)):
    data: dict[str, Any] = payload.model_dump(mode="json")
    # Preserve every extra field received from the Android app.
    row = db.get(InterventionRow, payload.id)
    if row is None:
        row = InterventionRow(
            id=payload.id,
            site_id=payload.siteId,
            site_name=payload.siteName,
            timestamp_ms=str(payload.timestamp),
            payload_json=json.dumps(data, ensure_ascii=False),
        )
        db.add(row)
    else:
        row.site_id = payload.siteId
        row.site_name = payload.siteName
        row.timestamp_ms = str(payload.timestamp)
        row.payload_json = json.dumps(data, ensure_ascii=False)
        row.updated_at = datetime.now(timezone.utc)
    db.commit()
    return {"ok": True, "id": payload.id}

@app.get("/api/v1/interventions")
def list_interventions(_: None = Depends(auth), db: Session = Depends(db_session)):
    rows = db.query(InterventionRow).order_by(InterventionRow.timestamp_ms.desc()).all()
    return [json.loads(r.payload_json) for r in rows]


@app.delete("/api/v1/interventions/{intervention_id}")
def delete_intervention(
    intervention_id: str,
    _: None = Depends(auth),
    db: Session = Depends(db_session),
):
    row = db.get(InterventionRow, intervention_id)
    if row is None:
        return {"ok": True, "id": intervention_id, "deleted": False}

    db.delete(row)
    db.commit()
    return {"ok": True, "id": intervention_id, "deleted": True}


@app.post("/api/v1/bugs")
def upsert_bug_report(payload: BugReportPayload, _: None = Depends(auth), db: Session = Depends(db_session)):
    data: dict[str, Any] = payload.model_dump(mode="json")
    row = db.get(BugReportRow, payload.id)
    if row is None:
        row = BugReportRow(
            id=payload.id,
            personnel_id=str(payload.personnelId),
            full_name=payload.fullName,
            timestamp_ms=str(payload.timestamp),
            payload_json=json.dumps(data, ensure_ascii=False),
        )
        db.add(row)
    else:
        row.personnel_id = str(payload.personnelId)
        row.full_name = payload.fullName
        row.timestamp_ms = str(payload.timestamp)
        row.payload_json = json.dumps(data, ensure_ascii=False)
    db.commit()
    return {"ok": True, "id": payload.id}


@app.get("/api/v1/bugs")
def list_bug_reports(_: None = Depends(auth), db: Session = Depends(db_session)):
    rows = db.query(BugReportRow).order_by(BugReportRow.timestamp_ms.desc()).all()
    return [json.loads(r.payload_json) for r in rows]
