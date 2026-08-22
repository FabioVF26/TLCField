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

Base.metadata.create_all(engine)
app = FastAPI(title="TLC Field API", version="1.2.0")

class InterventionPayload(BaseModel):
    model_config = ConfigDict(extra="allow")
    id: str
    siteId: str
    siteName: str
    timestamp: int


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
    return {"ok": True, "service": "tlc-field-api", "version": "1.2.0"}

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
