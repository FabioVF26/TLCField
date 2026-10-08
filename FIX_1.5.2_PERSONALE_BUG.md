# TLC FIELD 1.5.2 - correzione schermata BUG

Correzioni:
- la schermata BUG non usa più una fotografia statica dell'elenco personale presa una sola volta;
- all'apertura cerca il personale in memoria, poi nella cache persistente, quindi sul server;
- se il server risponde, l'elenco viene salvato in cache e reso immediatamente selezionabile;
- aggiunto pulsante "RICARICA ELENCO PERSONALE" in caso di errore;
- mostrato il motivo per cui "SALVA SEGNALAZIONE" resta disabilitato quando manca l'operatore;
- il salvataggio della segnalazione resta offline-first: prima locale, poi tentativo di invio al server.

Versione: 1.5.2 (versionCode 17)
