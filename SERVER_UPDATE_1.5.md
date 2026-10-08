# Aggiornamento server TLC FIELD 1.5 — Segnalazioni BUG

## Importante
Il file `server/app/main.py` contenuto in questo archivio è quello presente nel sorgente fornito.
Se il server attualmente in esercizio contiene endpoint aggiuntivi (ad esempio siti, personale o automezzi), **non sostituire alla cieca il file operativo**: integrare soltanto le parti relative ai BUG nel `main.py` effettivamente in uso.

## Componenti da aggiungere al backend operativo
1. Modello SQLAlchemy `BugReportRow` con tabella `bug_reports`.
2. Modello Pydantic `BugReportPayload`.
3. Endpoint autenticati:
   - `POST /api/v1/bugs`
   - `GET /api/v1/bugs`
4. Eseguire `Base.metadata.create_all(engine)` dopo la dichiarazione di `BugReportRow`, così la tabella viene creata automaticamente se assente.

## Dati registrati
Ogni segnalazione contiene:
- ID univoco;
- ID operatore;
- qualifica;
- nominativo;
- testo della segnalazione;
- data/ora (timestamp);
- versione dell'app.

## Consultazione
Con token API valido:

```bash
curl -H "Authorization: Bearer TOKEN" http://SERVER:8000/api/v1/bugs
```

Oppure da PostgreSQL:

```sql
SELECT id, full_name, timestamp_ms, payload_json
FROM bug_reports
ORDER BY timestamp_ms DESC;
```

## Aggiornamento container
Dopo aver integrato il codice nel backend operativo:

```bash
cd ~/tlc-field
docker compose up -d --build
```

Poi verificare:

```bash
curl -H "Authorization: Bearer TOKEN" http://10.10.10.10:8000/api/v1/bugs
```
