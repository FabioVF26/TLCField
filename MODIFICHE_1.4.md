# TLC Field 1.4

## Modifiche principali

- Modifica di un rapporto già salvato direttamente dallo Storico.
- La modifica mantiene lo stesso ID e la data/orario originari dell’intervento; viene registrata l’ultima modifica.
- Salvataggio locale trasformato in upsert per evitare duplicati dopo una modifica.
- Cancellazione dei rapporti protetta da PIN amministratore.
- Configurazione/cambio PIN amministratore nella schermata Server / Sincronizzazione.
- Audit locale minimo delle cancellazioni con ID rapporto, sito, data dell’intervento e data di cancellazione.
- Coda locale delle cancellazioni per supportare l’uso offline e la successiva propagazione al server.
- Aggiunta chiamata DELETE /api/v1/interventions/{id} nel client e nel backend incluso nel sorgente.
- Gestione di lastModified per risolvere correttamente le modifiche durante il merge tra locale e server.
- Correzione automatica dell’orientamento EXIF delle fotografie sia nell’anteprima dell’app sia nel PDF.
- Aggiunta dipendenza androidx.exifinterface.
- PDF: indicazione dell’ultima modifica quando il rapporto è stato aggiornato.
- Versione applicativa 1.4.0 (versionCode 14).

## Nota backend

Il server già installato sul Toughbook contiene personalizzazioni successive rispetto al server di esempio incluso nel repository. Non sostituire integralmente `server/app/main.py` sul server operativo: integrare soltanto l’endpoint DELETE riportato in `SERVER_UPDATE_1.4.md`.
