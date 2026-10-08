package it.vigilfuoco.tlcfield.data

data class BugReport(
    val id: String,
    val personnelId: Int,
    val qualification: String,
    val fullName: String,
    val notes: String,
    val timestamp: Long,
    val appVersion: String,
    val synced: Boolean = false
)
