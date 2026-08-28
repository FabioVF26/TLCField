# Aggiornamento backend richiesto per TLC Field 1.4

Sul server operativo aggiungere **solo** questo endpoint al `backend/app/main.py` già funzionante, senza sostituire il resto del file:

```python
@app.delete("/api/v1/interventions/{intervention_id}")
def delete_intervention(
    intervention_id: str,
    _: None = Depends(auth),
    db: Session = Depends(db_session),
):
    row = db.get(InterventionRow, intervention_id)

    if row is None:
        return {
            "ok": True,
            "id": intervention_id,
            "deleted": False,
        }

    db.delete(row)
    db.commit()

    return {
        "ok": True,
        "id": intervention_id,
        "deleted": True,
    }
```

Dopo la modifica:

```bash
python3 -m py_compile backend/app/main.py
docker compose up -d --build backend
docker compose ps
```

La versione Android 1.4 mantiene comunque una coda locale delle eliminazioni: se il server non è raggiungibile, il rapporto resta cancellato sul telefono e la richiesta di eliminazione viene ritentata alla sincronizzazione successiva.
