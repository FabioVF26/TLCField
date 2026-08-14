package it.vigilfuoco.tlcfield.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Archivio locale leggero per il prototipo.
 * I dati restano sul telefono e sono disponibili anche senza rete.
 */
object InterventionRepository {
    private const val PREFS = "tlc_field_interventions"
    private const val KEY = "items"

    fun save(context: Context, intervention: Intervention) {
        val items = getAll(context).toMutableList()
        items.add(0, intervention)
        persist(context, items)
    }

    fun getAll(context: Context): List<Intervention> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    add(fromJson(array.getJSONObject(i)))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun newId(): String = UUID.randomUUID().toString()

    private fun persist(context: Context, items: List<Intervention>) {
        val array = JSONArray()
        items.forEach { array.put(toJson(it)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, array.toString()).apply()
    }

    private fun toJson(i: Intervention) = JSONObject().apply {
        put("id", i.id)
        put("siteId", i.siteId)
        put("siteName", i.siteName)
        put("timestamp", i.timestamp)
        put("type", i.type)
        put("reportedProblem", i.reportedProblem)
        put("initialState", i.initialState)
        put("powerOk", i.powerOk)
        put("radioOn", i.radioOn)
        put("alarms", i.alarms)
        put("ipLinkOk", i.ipLinkOk)
        put("notes", i.notes)
        put("result", i.result)
        val measurements = JSONArray()
        i.measurements.forEach { m ->
            measurements.put(JSONObject().apply {
                put("linkName", m.linkName)
                if (m.referenceRssi != null) put("referenceRssi", m.referenceRssi)
                if (m.measuredRssi != null) put("measuredRssi", m.measuredRssi)
            })
        }
        put("measurements", measurements)
        val photos = JSONArray()
        i.photos.forEach { p ->
            photos.put(JSONObject().apply {
                put("path", p.path)
                put("category", p.category)
            })
        }
        put("photos", photos)
    }

    private fun fromJson(o: JSONObject): Intervention {
        val ms = mutableListOf<RssiMeasurement>()
        val arr = o.optJSONArray("measurements") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val m = arr.getJSONObject(i)
            ms += RssiMeasurement(
                linkName = m.optString("linkName"),
                referenceRssi = if (m.has("referenceRssi")) m.optInt("referenceRssi") else null,
                measuredRssi = if (m.has("measuredRssi")) m.optInt("measuredRssi") else null
            )
        }
        val photos = mutableListOf<InterventionPhoto>()
        val photoArray = o.optJSONArray("photos") ?: JSONArray()
        for (i in 0 until photoArray.length()) {
            val p = photoArray.getJSONObject(i)
            photos += InterventionPhoto(
                path = p.optString("path"),
                category = p.optString("category", "Altro")
            )
        }
        return Intervention(
            id = o.optString("id"),
            siteId = o.optString("siteId"),
            siteName = o.optString("siteName"),
            timestamp = o.optLong("timestamp"),
            type = o.optString("type"),
            reportedProblem = o.optString("reportedProblem"),
            initialState = o.optString("initialState"),
            powerOk = o.optBoolean("powerOk"),
            radioOn = o.optBoolean("radioOn"),
            alarms = o.optBoolean("alarms"),
            ipLinkOk = o.optBoolean("ipLinkOk"),
            notes = o.optString("notes"),
            result = o.optString("result"),
            measurements = ms,
            photos = photos
        )
    }
}
