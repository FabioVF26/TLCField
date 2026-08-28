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
    val deltaDb: Int?
        get() =
            if (
                referenceRssi != null &&
                measuredRssi != null
            ) {
                measuredRssi - referenceRssi
            } else {
                null
            }
}

data class InterventionPhoto(
    val path: String,
    val category: String
)

data class KairosSnapshot(
    val supplyVoltageV: Double? = null,
    val txTemperatureC: Double? = null,
    val forwardPowerW: Double? = null,
    val reflectedPowerW: Double? = null,
    val rssiMainDbm: Int? = null,
    val rssiDiversityDbm: Int? = null,
    val synchronizationSource: String = ""
)

// =========================================================
// PERSONALE ASSOCIATO ALL'INTERVENTO
// =========================================================

data class InterventionPersonnel(
    val id: Int,
    val qualification: String,
    val fullName: String
)

// =========================================================
// AUTOMEZZI ASSOCIATI ALL'INTERVENTO
// =========================================================

data class InterventionVehicle(
    val id: Int,
    val description: String,
    val plate: String
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
    val photos: List<InterventionPhoto> = emptyList(),

    // Personale intervenuto
    val personnel: List<InterventionPersonnel> = emptyList(),

    // Automezzi utilizzati
    val vehicles: List<InterventionVehicle> = emptyList(),

    // Dati KAIROS
    val kairosAlarmNumbers: List<Int> = emptyList(),
    val kairosCompletedChecks: List<String> = emptyList(),
    val kairosDiagnosticNotes: String = "",
    val kairosSnapshot: KairosSnapshot? = null
)

enum class AlarmSeverity(
    val level: Int,
    val label: String
) {
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
