package it.vigilfuoco.tlcfield.data

data class RadioLink(
    val name: String,
    val type: String,
    val rssiDbm: Int? = null,
    val txMhz: Double? = null,
    val rxMhz: Double? = null
)

data class KairosEndpoint(
    val label: String,
    val ipAddress: String
)

data class Site(
    val id: String,
    val name: String,
    val code: String? = null,
    val network: String? = null,
    val owner: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitudeM: Int? = null,
    val rackLocation: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val accessNotes: String? = null,
    val technicalNotes: String? = null,
    val navigationVerified: Boolean = true,
    val kairosEndpoints: List<KairosEndpoint> = emptyList(),
    val links: List<RadioLink> = emptyList()
)

data class RssiMeasurement(
    val linkName: String,
    val referenceRssi: Int?,
    val measuredRssi: Int?
) {
    val deltaDb: Int? get() = if (referenceRssi != null && measuredRssi != null) measuredRssi - referenceRssi else null
}

data class InterventionPhoto(
    val path: String,
    val category: String
)

data class Intervention(
    val id: String,
    val siteId: String,
    val siteName: String,
    val timestamp: Long,
    val type: String,
    val reportedProblem: String,
    val initialState: String,
    val powerOk: Boolean,
    val radioOn: Boolean,
    val alarms: Boolean,
    val ipLinkOk: Boolean,
    val notes: String,
    val result: String,
    val measurements: List<RssiMeasurement>,
    val photos: List<InterventionPhoto> = emptyList()
)

enum class AlarmSeverity(val level: Int, val label: String) {
    NOTICE(0, "NOTICE"),
    WARNING(1, "WARNING"),
    MINOR(2, "MINOR"),
    MAJOR(3, "MAJOR"),
    CRITICAL(4, "CRITICAL")
}

data class KairosAlarm(
    val number: Int,
    val label: String,
    val severity: AlarmSeverity,
    val diagnosticArea: String
)
