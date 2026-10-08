package it.vigilfuoco.tlcfield.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object BugReportRepository {

    private const val PREFS = "tlc_field_bug_reports"
    private const val KEY_REPORTS = "reports"

    fun getAll(context: Context): List<BugReport> =
        load(context).sortedByDescending { it.timestamp }

    fun getPending(context: Context): List<BugReport> =
        load(context).filter { !it.synced }

    fun save(context: Context, report: BugReport) {
        val items = load(context).toMutableList()
        val index = items.indexOfFirst { it.id == report.id }

        if (index >= 0) {
            items[index] = report
        } else {
            items.add(report)
        }

        persist(context, items)
    }

    fun markSynced(context: Context, reportId: String) {
        val items = load(context).map { report ->
            if (report.id == reportId) {
                report.copy(synced = true)
            } else {
                report
            }
        }
        persist(context, items)
    }

    private fun load(context: Context): List<BugReport> {
        val raw = context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_REPORTS, null)
            ?: return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(
                        BugReport(
                            id = item.getString("id"),
                            personnelId = item.optInt("personnelId", -1),
                            qualification = item.optString("qualification", ""),
                            fullName = item.optString("fullName", ""),
                            notes = item.optString("notes", ""),
                            timestamp = item.optLong("timestamp", 0L),
                            appVersion = item.optString("appVersion", ""),
                            synced = item.optBoolean("synced", false)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun persist(context: Context, reports: List<BugReport>) {
        val array = JSONArray()

        reports.forEach { report ->
            array.put(
                JSONObject().apply {
                    put("id", report.id)
                    put("personnelId", report.personnelId)
                    put("qualification", report.qualification)
                    put("fullName", report.fullName)
                    put("notes", report.notes)
                    put("timestamp", report.timestamp)
                    put("appVersion", report.appVersion)
                    put("synced", report.synced)
                }
            )
        }

        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_REPORTS, array.toString())
            .apply()
    }
}
