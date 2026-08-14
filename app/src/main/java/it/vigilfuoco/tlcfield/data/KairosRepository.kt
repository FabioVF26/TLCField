package it.vigilfuoco.tlcfield.data

data class KairosAlarmGuide(
    val meaning: String,
    val checks: List<String>,
    val values: List<String> = emptyList(),
    val sourceNote: String
)

object KairosRepository {
    val alarms = listOf(
        KairosAlarm(3, "Logic Supply Status", AlarmSeverity.CRITICAL, "Alimentazione"),
        KairosAlarm(4, "Supply Undervoltage", AlarmSeverity.MAJOR, "Alimentazione"),
        KairosAlarm(5, "Supply Overvoltage", AlarmSeverity.MAJOR, "Alimentazione"),
        KairosAlarm(6, "Ethernet Link Status", AlarmSeverity.MAJOR, "Rete IP"),
        KairosAlarm(7, "PLD Data Correctness", AlarmSeverity.CRITICAL, "Hardware/PLD"),
        KairosAlarm(8, "DSP Loading/Starting", AlarmSeverity.CRITICAL, "DSP"),
        KairosAlarm(9, "GNSS Status", AlarmSeverity.MAJOR, "Sincronizzazione/GNSS"),
        KairosAlarm(10, "Vocoders Status", AlarmSeverity.MAJOR, "Audio/Vocoder"),
        KairosAlarm(11, "BS Temperature", AlarmSeverity.MAJOR, "Temperatura stazione"),
        KairosAlarm(12, "TX Temperature", AlarmSeverity.MAJOR, "Trasmettitore"),
        KairosAlarm(13, "No TX Power", AlarmSeverity.CRITICAL, "Trasmettitore RF"),
        KairosAlarm(14, "TX Power too low", AlarmSeverity.MAJOR, "Trasmettitore RF"),
        KairosAlarm(15, "TX Power too high", AlarmSeverity.MAJOR, "Trasmettitore RF"),
        KairosAlarm(16, "TX SWR Warning", AlarmSeverity.MAJOR, "Sistema radiante"),
        KairosAlarm(17, "TX SWR Alarm", AlarmSeverity.CRITICAL, "Sistema radiante"),
        KairosAlarm(18, "TX Power Reduction", AlarmSeverity.CRITICAL, "Trasmettitore RF"),
        KairosAlarm(19, "Synchronization Source", AlarmSeverity.WARNING, "Sincronizzazione"),
        KairosAlarm(20, "Synchronization Status", AlarmSeverity.WARNING, "Sincronizzazione"),
        KairosAlarm(21, "Board Vtunes Status", AlarmSeverity.CRITICAL, "Sintonia RF"),
        KairosAlarm(22, "TRX Vtunes Status", AlarmSeverity.CRITICAL, "Sintonia RF"),
        KairosAlarm(23, "Board Clocks Status", AlarmSeverity.CRITICAL, "Clock"),
        KairosAlarm(24, "TRX Clocks Status", AlarmSeverity.CRITICAL, "Clock"),
        KairosAlarm(25, "Board PLLs Lock Status", AlarmSeverity.CRITICAL, "PLL"),
        KairosAlarm(26, "TRX PLLs Lock Status", AlarmSeverity.CRITICAL, "PLL"),
        KairosAlarm(27, "PLD Status", AlarmSeverity.CRITICAL, "Hardware/PLD"),
        KairosAlarm(28, "PLD<->DSP Chan Status", AlarmSeverity.CRITICAL, "PLD/DSP"),
        KairosAlarm(29, "RX IFs Status", AlarmSeverity.CRITICAL, "Ricevitore"),
        KairosAlarm(30, "RXs Status", AlarmSeverity.CRITICAL, "Ricevitore"),
        KairosAlarm(31, "RF Channel Noise", AlarmSeverity.MINOR, "Ricevitore/RF"),
        KairosAlarm(32, "Registration to Master", AlarmSeverity.MAJOR, "Rete DMR"),
        KairosAlarm(35, "Loss of Slave", AlarmSeverity.MAJOR, "Rete DMR"),
        KairosAlarm(36, "Master Role", AlarmSeverity.WARNING, "Rete DMR"),
        KairosAlarm(37, "Connection to BK MST", AlarmSeverity.WARNING, "Rete DMR"),
        KairosAlarm(41, "TRX Layer Status", AlarmSeverity.WARNING, "Software TRX"),
        KairosAlarm(42, "BS Layer Status", AlarmSeverity.WARNING, "Software BS"),
        KairosAlarm(43, "SIP name Resolution", AlarmSeverity.MAJOR, "SIP/IP"),
        KairosAlarm(48, "SIP TRUNK Server", AlarmSeverity.MAJOR, "SIP/IP")
    )

    fun alarm(number: Int): KairosAlarm? = alarms.firstOrNull { it.number == number }

    fun guide(number: Int): KairosAlarmGuide = guides[number] ?: KairosAlarmGuide(
        meaning = "La documentazione identifica questo evento come anomalia del sottosistema indicato.",
        checks = listOf(
            "Aprire KAIROS Manager / Web Interface e verificare lo stato dettagliato del sottosistema.",
            "Registrare l'allarme e i parametri visualizzati prima di modificare configurazioni o riavviare servizi.",
            "Per anomalie persistenti fare riferimento al manuale del costruttore e alla configurazione specifica del sito."
        ),
        sourceNote = "Guida generica: il manuale descrive l'evento ma non fornisce una procedura di troubleshooting univoca."
    )

    private val guides = mapOf(
        3 to KairosAlarmGuide(
            "Il regolatore interno che alimenta i dispositivi logici rileva un guasto.",
            listOf("Controllare la tensione di ingresso DC in Analog Measures.", "Verificare se sono contemporaneamente presenti Supply Undervoltage o Supply Overvoltage."),
            sourceNote = "Alarm Events Subsystem: Logic Supply Status; KAIROS Manager: Analog Measures."
        ),
        4 to KairosAlarmGuide(
            "L'allarme si attiva quando la tensione principale o quella logica scendono sotto le soglie previste.",
            listOf("Controllare Input Supply Voltage in Analog Measures.", "Verificare alimentatore, cablaggio DC e cadute di tensione sotto carico."),
            listOf("RAISE: alimentazione principale < 10,8 V oppure logica < 2,8 V", "CLEAR: principale > 11,1 V e logica > 2,9 V"),
            "Alarm Events Subsystem, evento 4."
        ),
        5 to KairosAlarmGuide(
            "L'allarme si attiva quando la tensione principale o quella logica superano le soglie previste.",
            listOf("Controllare Input Supply Voltage in Analog Measures.", "Verificare la regolazione e la stabilità dell'alimentatore prima di proseguire."),
            listOf("RAISE: alimentazione principale > 15,6 V oppure logica > 3,5 V", "CLEAR: principale < 15,3 V e logica < 3,4 V"),
            "Alarm Events Subsystem, evento 5."
        ),
        6 to KairosAlarmGuide(
            "Il collegamento Ethernet risulta assente o interrotto.",
            listOf("Verificare fisicamente link Ethernet, cavo e apparato di rete collegato.", "Controllare indirizzo IP e raggiungibilità dell'apparato.", "Se l'indirizzo IP non è noto, utilizzare la procedura IP Discover solo secondo il manuale e con accesso fisico all'apparato."),
            sourceNote = "Alarm Events Subsystem, evento 6; KAIROS Manager, IP Discover."
        ),
        9 to KairosAlarmGuide(
            "È rilevato un guasto del dispositivo GNSS; l'evento è significativo solo se la relativa opzione è installata.",
            listOf("Aprire Primary Synchronization Status.", "Controllare presenza, validità e lock del PPS GPS e la sorgente di sincronizzazione corrente."),
            sourceNote = "Alarm Events Subsystem, evento 9; KAIROS Manager, Primary Synchronization Status."
        ),
        11 to KairosAlarmGuide(
            "La temperatura interna della stazione base, in prossimità del VCTCXO, è eccessiva.",
            listOf("Controllare la temperatura riportata dall'apparato.", "Verificare ventilazione, condizioni ambientali e ostruzioni prima di richiedere trasmissioni prolungate."),
            listOf("RAISE > 70 °C", "CLEAR < 65 °C", "L'evento può provocare riduzione progressiva della potenza."),
            "Alarm Events Subsystem, evento 11."
        ),
        12 to KairosAlarmGuide(
            "La temperatura dello stadio finale TX è eccessiva.",
            listOf("Controllare TX Temperature in Analog Measures.", "Controllare anche Forward Power, Reflected Power e gli allarmi SWR."),
            listOf("RAISE > 85 °C", "CLEAR < 80 °C", "L'evento provoca riduzione progressiva della potenza finché la temperatura rientra."),
            "Alarm Events Subsystem, evento 12; KAIROS Manager, Analog Measures."
        ),
        13 to KairosAlarmGuide(
            "È richiesta una trasmissione ma non viene rilevata potenza RF in uscita.",
            listOf("Verificare che TX sia abilitato e che il PTT digitale/analogico risulti attivo quando previsto.", "Controllare Forward Power in Analog Measures durante una trasmissione.", "Controllare TRX Status e gli eventuali allarmi TX/PLL correlati."),
            sourceNote = "Alarm Events Subsystem, evento 13; KAIROS Manager, TRX Status e Analog Measures."
        ),
        14 to KairosAlarmGuide(
            "Durante la trasmissione la potenza RF rilevata è molto inferiore a quella desiderata.",
            listOf("Confrontare Forward Power con la potenza TX impostata.", "Controllare TX Temperature, TX Input Current e presenza di TX Power Reduction.", "Verificare gli allarmi SWR prima di intervenire sulla configurazione della potenza."),
            listOf("Regola indicativa del manuale: >50% del valore desiderato sotto 10 W", ">75% del valore desiderato per valori superiori"),
            "Alarm Events Subsystem, evento 14; KAIROS Manager, Analog Measures."
        ),
        15 to KairosAlarmGuide(
            "Durante la trasmissione la potenza RF rilevata è molto superiore a quella desiderata.",
            listOf("Confrontare Forward Power con la potenza TX impostata.", "Verificare configurazione del canale e presenza/configurazione di eventuale amplificatore esterno."),
            listOf("Regola indicativa del manuale: <150% del valore desiderato sotto 10 W", "<120% del valore desiderato per valori superiori"),
            "Alarm Events Subsystem, evento 15."
        ),
        16 to KairosAlarmGuide(
            "Durante la trasmissione viene misurato un SWR superiore alla soglia di warning.",
            listOf("Controllare Forward Power e Reflected Power in Analog Measures.", "Se la potenza riflessa è vicina alla diretta, controllare cablaggio TX e antenna come indicato dal manuale.", "Verificare connettori, feeder e sistema radiante con strumentazione idonea prima di proseguire."),
            listOf("SWR Warning: > 2,5", "La rilevazione avviene con TX impegnato e potenza > 1 W"),
            "Alarm Events Subsystem, evento 16; KAIROS Manager, Analog Measures."
        ),
        17 to KairosAlarmGuide(
            "Durante la trasmissione viene misurato un SWR superiore alla soglia di allarme critico.",
            listOf("Controllare immediatamente Forward Power e Reflected Power.", "Controllare cablaggio TX, connettori, feeder e antenna.", "Tenere conto che l'apparato applica automaticamente una riduzione fissa della potenza a 1 W."),
            listOf("SWR Alarm: > 4,0", "La rilevazione avviene con TX impegnato e potenza > 1 W", "Riduzione automatica a 1 W"),
            "Alarm Events Subsystem, evento 17."
        ),
        18 to KairosAlarmGuide(
            "È attiva la riduzione automatica della potenza TX.",
            listOf("Ricercare l'allarme che ha determinato la riduzione: SWR o temperatura sono tra gli eventi documentati che possono causarla.", "Controllare TX Temperature, Forward Power e Reflected Power prima di ulteriori prove."),
            sourceNote = "Alarm Events Subsystem, evento 18 e specifiche degli eventi termici/SWR."
        ),
        19 to KairosAlarmGuide(
            "KAIROS è agganciato a una sorgente di sincronizzazione alternativa rispetto alla primaria configurata.",
            listOf("Aprire Primary Synchronization Status.", "Confrontare Current Synchronization Source con la sorgente primaria prevista.", "Verificare i campi Present, Valid e Lock delle sorgenti configurate."),
            sourceNote = "Alarm Events Subsystem, evento 19; KAIROS Manager, Primary Synchronization Status."
        ),
        20 to KairosAlarmGuide(
            "KAIROS non risulta agganciato ad alcuna delle sorgenti di sincronizzazione configurate.",
            listOf("Aprire Primary Synchronization Status.", "Verificare Present, Valid e Lock per GPS/PPS, PTP, External PPS, External Network o Internal Reference secondo la configurazione del sito.", "Se Internal Reference è in lock al posto della sorgente primaria, il manuale indica che la prima scelta presenta un problema."),
            sourceNote = "Alarm Events Subsystem, evento 20; KAIROS Manager, Primary Synchronization Status."
        ),
        29 to KairosAlarmGuide(
            "Il DSP segnala un guasto sui moduli IF del ricevitore Main o Diversity.",
            listOf("Aprire TRX Status e controllare M.RX Fail e D.RX Fail.", "Controllare RSSI Main/Diversity e i parametri DMR Status.", "Non eseguire Vtune test come azione ordinaria: il manuale lo descrive come test tecnico specifico con condizioni precise."),
            sourceNote = "Alarm Events Subsystem, evento 29; KAIROS Manager, TRX Status/DMR Status."
        ),
        30 to KairosAlarmGuide(
            "Il DSP segnala un guasto del ricevitore Main o Diversity.",
            listOf("Aprire TRX Status e verificare M.RX Fail / D.RX Fail.", "Verificare che Main RX e, se presente, Diversity RX siano abilitati nella configurazione TRX.", "Controllare RSSI Main e Diversity."),
            sourceNote = "Alarm Events Subsystem, evento 30; KAIROS Manager, TRX Status, TRX Configuration e DMR Status."
        ),
        31 to KairosAlarmGuide(
            "L'evento RF Channel Noise è elencato nella documentazione, ma nelle specifiche consultate è indicato come non ancora implementato.",
            listOf("Non interpretare questo evento come misura autonoma di rumore senza verificare la versione software e la documentazione applicabile."),
            sourceNote = "Alarm Events Subsystem, evento 31: Not yet implemented."
        ),
        32 to KairosAlarmGuide(
            "Una stazione Slave/Submaster/Link-Down perde la connessione logica LAN con la propria Master.",
            listOf("Verificare prima Ethernet Link Status e raggiungibilità IP.", "Controllare configurazione e stato della rete Master/Slave.", "Verificare che la stazione si registri nuovamente al Master dopo il ripristino della connettività."),
            sourceNote = "Alarm Events Subsystem, evento 32."
        ),
        35 to KairosAlarmGuide(
            "Una Slave già registrata non invia più i keepalive e viene considerata scomparsa dalla rete.",
            listOf("Controllare connettività LAN/RF-Link verso la Slave interessata.", "Verificare l'ultimo stato e il timestamp nel sistema di monitoraggio prima di modificare parametri."),
            sourceNote = "Alarm Events Subsystem, evento 35; concetto di polling/stato da NetControl/KaSysco."
        ),
        41 to KairosAlarmGuide(
            "Lo stato del TRX Layer è cambiato; l'evento può includere il motivo di abilitazione/disabilitazione.",
            listOf("Leggere il testo/additional data dell'evento.", "Aprire TRX Status e verificare DSP Ready, TRX Active e gli stati RX/TX."),
            sourceNote = "Alarm Events Subsystem, evento 41; KAIROS Manager, TRX Status."
        ),
        42 to KairosAlarmGuide(
            "Lo stato del Base Station Layer è cambiato; l'evento può includere il motivo della disabilitazione.",
            listOf("Leggere il motivo riportato dall'evento.", "Controllare ruolo e configurazione della Base Station senza modificarli finché non è stata identificata la causa."),
            sourceNote = "Alarm Events Subsystem, evento 42."
        )
    )
}
