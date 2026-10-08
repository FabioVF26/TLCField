# TLC FIELD 1.5 — Segnalazione BUG sperimentale

## Nuova funzione
- Nuovo pulsante **SEGNALA BUG (TEST)** nella home.
- L'operatore viene selezionato dall'elenco del personale già sincronizzato con il server.
- Campo note libero per descrivere anomalia, schermata e comportamento atteso.
- Registrazione automatica di data/ora e versione dell'app.
- Salvataggio locale immediato anche senza connessione.
- Invio immediato al server quando disponibile.
- Le segnalazioni non inviate vengono ritentate con **SINCRONIZZA ORA**.

## Backend
Sono stati aggiunti:
- `POST /api/v1/bugs`
- `GET /api/v1/bugs`
- tabella PostgreSQL `bug_reports` creata automaticamente da SQLAlchemy all'avvio.

## Versione
- `versionCode = 15`
- `versionName = 1.5.0`


## Correzione 1.5.1
- Abilitata la generazione di `BuildConfig` in `app/build.gradle.kts`.
- Corretto il build GitHub Actions che falliva in `BugReportScreen.kt` sui riferimenti `BuildConfig.VERSION_NAME`.
- Versione aggiornata a 1.5.1 (versionCode 16).
