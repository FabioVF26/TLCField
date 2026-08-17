# TLC Field Android 0.9

App Android per supporto tecnico agli interventi sui ponti radio VVF.

## Novità 0.7
- Diagnosi KAIROS integrata direttamente nel Nuovo intervento.
- Selezione di uno o più allarmi KAIROS realmente riscontrati.
- Checklist diagnostica specifica per ciascun allarme.
- Registrazione delle verifiche effettivamente eseguite dal tecnico.
- Acquisizione dei principali parametri KAIROS: tensione di alimentazione, temperatura TX, potenza diretta/riflessa, RSSI Main/Diversity e sorgente di sincronizzazione.
- Salvataggio offline di allarmi, verifiche e misure insieme all'intervento.
- Rapporto PDF aggiornato con una sezione Diagnosi KAIROS.
- Storico interventi con riepilogo delle verifiche KAIROS registrate.
- Compatibilità con gli interventi già salvati nelle versioni precedenti.

## Funzioni già presenti
- Home con logo TLC/VVF.
- Archivio siti e ponti radio.
- Navigazione verso il sito con Google Maps.
- Accesso Web Interface KAIROS per gli IP censiti.
- Misure RSSI con confronto rispetto ai valori di riferimento del sito.
- Fotografie associate all'intervento.
- Storico locale offline.
- Generazione e condivisione del rapporto PDF.
- Catalogo e guida diagnostica degli allarmi KAIROS.

## Nota operativa
La diagnostica non modifica automaticamente la configurazione dell'apparato. Le procedure e le soglie KAIROS derivano dalla documentazione tecnica fornita e le verifiche vengono registrate solo quando il tecnico le marca come eseguite.

## Novità 0.9
La Home include ora la Mappa Siti. I marker aprono la relativa scheda tecnica e da ciascun sito è possibile avviare un nuovo intervento con il sito già selezionato. La cartografia usa OpenStreetMap/Leaflet e richiede connessione dati; la navigazione verso il sito continua ad aprirsi con Google Maps tramite Intent Android.


## Novità 0.9
- Sezione DOCUMENTAZIONE attiva dalla Home.
- 6 manuali KAIROS consultabili offline.
- 9 schede impianto dei siti consultabili offline.
- Apertura diretta della scheda PDF dalla pagina del sito.
- I PDF sono inclusi negli assets e copiati in cache solo al momento dell'apertura.
