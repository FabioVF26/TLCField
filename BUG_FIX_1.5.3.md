# TLC FIELD 1.5.3 - BUG offline fallback

Modifiche:
- funzione BUG utilizzabile anche senza server configurato e senza sincronizzazione precedente;
- elenco personale locale di fallback (15 nominativi);
- priorità dati: memoria -> cache -> server -> fallback locale;
- gli ID fallback sono negativi per evitare collisioni con quelli del backend;
- il report conserva comunque qualifica e nominativo completi;
- il BUG viene sempre salvato localmente e resta in attesa di sincronizzazione se il server non è disponibile;
- versione aggiornata a 1.5.3 (versionCode 18).
