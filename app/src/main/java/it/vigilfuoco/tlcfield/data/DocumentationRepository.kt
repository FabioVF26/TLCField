package it.vigilfuoco.tlcfield.data

enum class DocumentCategory(val label: String) {
    KAIROS("Manuali KAIROS"),
    SITE("Schede siti")
}

data class TechnicalDocument(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: DocumentCategory,
    val assetPath: String,
    val siteId: String? = null
)

object DocumentationRepository {
    val documents = listOf(
        TechnicalDocument(
            id = "kairos-operativo",
            title = "Manuale operativo KAIROS",
            subtitle = "Versione 1.2 — Radio Activity",
            category = DocumentCategory.KAIROS,
            assetPath = "manuals/manuale_operativo_kairos_v1_2.pdf"
        ),
        TechnicalDocument(
            id = "kairos-manager",
            title = "KAIROS Manager — Function Manual",
            subtitle = "Revision 1.01 — configurazione, controlli e diagnostica",
            category = DocumentCategory.KAIROS,
            assetPath = "manuals/kairos_manager_function_manual_v101.pdf"
        ),
        TechnicalDocument(
            id = "kairos-web",
            title = "Guida interfaccia Web KAIROS",
            subtitle = "ITA57 v0.6 — settembre 2022",
            category = DocumentCategory.KAIROS,
            assetPath = "manuals/kairos_web_interface_0_6_ita.pdf"
        ),
        TechnicalDocument(
            id = "kairos-allarmi",
            title = "KAIROS Alarm Events Subsystem",
            subtitle = "AN-K001 — allarmi, eventi e SNMP",
            category = DocumentCategory.KAIROS,
            assetPath = "manuals/kairos_alarm_events_1v0.pdf"
        ),
        TechnicalDocument(
            id = "dmr-netcontrol",
            title = "DMR NetControl",
            subtitle = "Versione 1v0 — monitoraggio della rete",
            category = DocumentCategory.KAIROS,
            assetPath = "manuals/dmr_netcontrol_1v0.pdf"
        ),
        TechnicalDocument(
            id = "kasysco",
            title = "KaSysco — Manuale utente",
            subtitle = "Versione 1.0 — configurazione e controllo",
            category = DocumentCategory.KAIROS,
            assetPath = "manuals/kasysco_ita_1v0.pdf"
        ),
        TechnicalDocument("site-cavo-snam", "Monte Cavo SNAM", "Scheda impianto rev. 2026", DocumentCategory.SITE, "sites/cavo_snam_2026.pdf", "cavo-snam"),
        TechnicalDocument("site-monte-cosce", "Monte Cosce - Configni", "Scheda impianto rev. giugno 2026", DocumentCategory.SITE, "sites/monte_cosce_giugno_2026.pdf", "monte-cosce"),
        TechnicalDocument("site-monte-mario", "Monte Mario", "Scheda impianto", DocumentCategory.SITE, "sites/monte_mario.pdf", "monte-mario"),
        TechnicalDocument("site-monte-midia", "Monte Midia", "Scheda impianto rev. 2025", DocumentCategory.SITE, "sites/monte_midia_2025.pdf", "monte-midia"),
        TechnicalDocument("site-terminillo", "Terminillo", "Scheda impianto rev. 2025", DocumentCategory.SITE, "sites/terminillo_2025.pdf", "terminillo"),
        TechnicalDocument("site-argentario", "Monte Argentario", "Scheda impianto 2024", DocumentCategory.SITE, "sites/argentario_2024.pdf", "argentario"),
        TechnicalDocument("site-campocatino", "Campocatino", "Scheda impianto rev. gennaio 2026", DocumentCategory.SITE, "sites/campocatino_gennaio_2026.pdf", "campocatino"),
        TechnicalDocument("site-cavo-alto", "Monte Cavo Alto", "Scheda impianto — coordinate da verificare", DocumentCategory.SITE, "sites/cavo_alto.pdf", "cavo-alto"),
        TechnicalDocument("site-cavo-cotie", "Monte Cavo COTIE", "Scheda impianto rev. aprile 2026", DocumentCategory.SITE, "sites/cavo_cotie_aprile_2026.pdf", "cavo-cotie")
    )

    fun forSite(siteId: String): TechnicalDocument? = documents.firstOrNull { it.siteId == siteId }
}
