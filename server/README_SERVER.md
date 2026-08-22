# TLC Field Server 1.2

Backend iniziale per centralizzare gli interventi TLC Field su PostgreSQL.

## Avvio rapido con Docker

1. Modificare `docker-compose.yml` cambiando **password PostgreSQL** e `TLCFIELD_API_TOKEN`.
2. Dalla cartella `server` eseguire:
   `docker compose up -d --build`
3. Verificare dal browser o con curl:
   `http://IP_SERVER:8000/docs`
4. Nell'app Android aprire **SERVER / SINCRONIZZAZIONE** e impostare:
   - URL: `http://IP_SERVER:8000` per test LAN oppure URL HTTPS in produzione;
   - Token API: lo stesso valore configurato in `TLCFIELD_API_TOKEN`.
5. Premere **PROVA CONNESSIONE** e poi **SINCRONIZZA ORA**.

## Comportamento

- Il telefono conserva sempre l'archivio locale.
- La sincronizzazione invia tutti gli interventi locali con upsert per UUID.
- Scarica poi l'archivio centrale e lo unisce a quello locale senza duplicati.
- In questa prima versione fotografie e PDF restano sul dispositivo: il database centrale contiene i dati strutturati dell'intervento. Lo storage server di foto/PDF è previsto nello step successivo.

## Sicurezza

L'uso HTTP è previsto solo per test su LAN/VPN. In esercizio pubblicare l'API dietro HTTPS (reverse proxy Caddy/Nginx/Traefik) e sostituire token/password di esempio.
