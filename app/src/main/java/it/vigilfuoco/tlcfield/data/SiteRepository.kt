package it.vigilfuoco.tlcfield.data

object SiteRepository {

    private val localSites = listOf(
        Site(
            id = "cavo-snam",
            name = "Monte Cavo SNAM",
            code = "402 / 802",
            network = "SRTDL Nord-Est 402 / Nord-Ovest 802",
            owner = "VVF",
            latitude = 41.7500652,
            longitude = 12.7083325,
            altitudeM = 920,
            rackLocation = "1° e 2° rack a sinistra",
            technicalNotes = "Scheda revisionata 2026. Nuovo impianto SRTDL 402 con ponte Kairos analogico-digitale.",
            kairosEndpoints = listOf(
                KairosEndpoint("Dorsale Amiata 402", "172.33.110.161"),
                KairosEndpoint("Link Monte Cavo 402", "172.33.110.162"),
                KairosEndpoint("Circolare CH 88", "172.33.110.163")
            ),
            links = listOf(
                RadioLink("M. Amiata", "Dorsale 402", -95, 444.2625, 434.6625),
                RadioLink("M. Cavo COTIE", "Link 402", -59, 444.7875, 434.9250),
                RadioLink("Canale 88", "Circolare 402", -65, 74.3375, 73.5375),
                RadioLink("M. Argentario", "Dorsale 802", -78, 444.6500, 434.6500),
                RadioLink("M. Cavo COTIE", "Link 802", -65, 444.7875, 434.9250),
                RadioLink("Canale 94", "Circolare 802", -60, 74.3750, 73.5750)
            )
        ),
        Site(
            id = "monte-cosce",
            name = "Monte Cosce - Configni",
            code = "303",
            network = "SRTDL Nord-Est 303",
            owner = "Mediaset / EI Towers",
            latitude = 42.41111,
            longitude = 12.63330,
            altitudeM = 1800,
            rackLocation = "Armadio n°2 centrale, sotto interruttore VVF2",
            phone = "0660517838",
            email = "sicurezza-eit@eitowers.it",
            accessNotes = "Per richiesta accesso usare l'indirizzo e-mail indicato nella scheda. Centrale Mediaset per distacco allarme: 0660517838/5.",
            links = listOf(
                RadioLink("Comero", "Dorsale", -82, 434.9000, 444.9000),
                RadioLink("Amiata", "Link", -76, 444.2625, 434.6625),
                RadioLink("Canale 98", "Circolare", -80, 74.4000, 73.6000)
            )
        ),
        Site(
            id = "monte-mario",
            name = "Monte Mario",
            code = "CRUN SAT CH100",
            network = "ID 10.10.4.2",
            owner = "Esercito Italiano",
            latitude = 41.92222,
            longitude = 12.45060,
            altitudeM = 139,
            links = listOf(
                RadioLink("Campocatino", "Ridiffusore", -91, 444.9250, 434.7625),
                RadioLink("Circolare", "Circolare", -71, 74.4125, 73.6125)
            )
        ),
        Site(
            id = "monte-midia",
            name = "Monte Midia",
            code = "517",
            network = "SRTDL Sud-Est 517",
            owner = "Telespazio",
            latitude = 42.205780,
            longitude = 13.17740,
            altitudeM = 1677,
            rackLocation = "Rack SRTDL n°1; rack CRUN n°1",
            phone = "0863-550409",
            accessNotes = "Numero aggiuntivo riportato in scheda: 3666680341.",
            links = listOf(
                RadioLink("Maiella", "Dorsale", -76, 444.4375, 434.9625),
                RadioLink("Monte Cavo", "Link", -77, 444.7875, 434.9250),
                RadioLink("Canale 92", "Circolare", -77, 74.3625, 73.5625)
            )
        ),
        Site(
            id = "terminillo",
            name = "Terminillo",
            code = "202",
            network = "SRTDL Nord-Est 202",
            owner = "VVF - sito Aeronautica Militare",
            latitude = 42.4603611,
            longitude = 12.9843056,
            altitudeM = 1875,
            rackLocation = "3° piano sottotetto: 1° CRUN, 2° CH68, 3° alimentatori, 4° CH20, 5° SRTDL + rinvio Lazio 1",
            links = listOf(
                RadioLink("San Michele", "Dorsale", -93, 434.8875, 444.7625),
                RadioLink("Monte Cavo", "Link", -85, 444.7875, 434.9250),
                RadioLink("Canale 86", "Circolare", -85, 74.3250, 73.5250)
            )
        ),
        Site(
            id = "argentario",
            name = "Monte Argentario",
            code = "101",
            network = "SRTDL Nord-Ovest 101",
            owner = "Aeronautica Militare",
            latitude = 42.38663,
            longitude = 11.16985,
            altitudeM = 635,
            rackLocation = "1° rack a destra SRTDL; alimentatore n°3 dall'alto nel rack di alimentazione",
            phone = "0564-445515",
            links = listOf(
                RadioLink("Montieri", "Dorsale", -78, 444.7625, 434.5875),
                RadioLink("San Michele", "Dorsale", -88, 444.7625, 434.5875),
                RadioLink("Massoncello", "Dorsale", -84, 444.7625, 434.5875),
                RadioLink("Limbara", "Link", -85, 434.5000, 444.3375),
                RadioLink("Monte Cavo", "Circolare", -90, 434.6500, 444.6500)
            )
        ),
        Site(
            id = "campocatino",
            name = "Campocatino",
            code = "702",
            network = "SRTDL Rete Sud 702",
            owner = "VVF",
            latitude = 41.8344,
            longitude = 13.33500,
            altitudeM = 1800,
            rackLocation = "2° rack da sinistra SRTDL",
            technicalNotes = "POD contatore 602139310; stazione SRTDL sotto trasformatore d'isolamento.",
            links = listOf(
                RadioLink("Cerreto", "Dorsale", -76, 434.8875, 444.0500),
                RadioLink("Monte Cavo", "Link", -82, 444.7875, 434.9250),
                RadioLink("Canale 96", "Circolare", -72, 74.3875, 73.5875)
            )
        ),
        Site(
            id = "cavo-alto",
            name = "Monte Cavo Alto",
            code = "Nodo 502 - Backup 102",
            network = "SRTDL Nodo TLC 502",
            owner = "VVF",
            latitude = 41.45000,
            longitude = 12.41290,
            altitudeM = 973,
            technicalNotes = "Coordinate riportate nella scheda originale; da verificare prima dell'impiego operativo per la navigazione.",
            navigationVerified = false,
            links = listOf(
                RadioLink("Monte Argentario", "Dorsale", null, 444.6500, 434.6500),
                RadioLink("Nodo", "Link", null, 434.9250, 444.7875),
                RadioLink("Canale 90", "Circolare", null, 74.3500, 73.5500)
            )
        ),
        Site(
            id = "cavo-cotie",
            name = "Monte Cavo COTIE",
            code = "102",
            network = "SRTDL Nodo 102",
            owner = "Esercito Italiano",
            latitude = 41.75639,
            longitude = 12.70810,
            altitudeM = 950,
            rackLocation = "Armadio n°1 da destra",
            phone = "0680995331",
            technicalNotes = "Nodo con collegamenti verso SNAM 402/802, Campocatino 702, Midia 517 e Terminillo 202.",
            links = listOf(
                RadioLink("Monte Argentario - Portale", "Dorsale", -82, 444.6500, 434.6500),
                RadioLink("Monte Argentario - Traliccio", "Dorsale", -88, null, null),
                RadioLink("Canale 90", "Circolare", null, 74.3500, 73.5500),
                RadioLink("Nodo", "Link", null, 434.9250, 444.7875),
                RadioLink("Cavo SNAM 802", "Nodo", -67),
                RadioLink("Cavo SNAM 402", "Nodo", -42),
                RadioLink("Campocatino 702", "Nodo", -84),
                RadioLink("Monte Midia 517", "Nodo", -79),
                RadioLink("Terminillo 202", "Nodo", -82)
            )
        )
    )

    fun getSite(id: String): Site? = sites.firstOrNull { it.id == id }
}
