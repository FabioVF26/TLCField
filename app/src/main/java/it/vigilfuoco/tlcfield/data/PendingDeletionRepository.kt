package it.vigilfuoco.tlcfield.data

import android.content.Context
import org.json.JSONArray

object PendingDeletionRepository {
    private const val PREFS = "tlc_field_pending_deletions"
    private const val KEY = "intervention_ids"

    fun getAll(context: Context): Set<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildSet {
                for (i in 0 until array.length()) {
                    val id = array.optString(i)
                    if (id.isNotBlank()) add(id)
                }
            }
        }.getOrDefault(emptySet())
    }

    fun mark(context: Context, interventionId: String) {
        val ids = getAll(context).toMutableSet()
        ids += interventionId
        persist(context, ids)
    }

    fun remove(context: Context, interventionId: String) {
        val ids = getAll(context).toMutableSet()
        ids -= interventionId
        persist(context, ids)
    }

    private fun persist(context: Context, ids: Set<String>) {
        val array = JSONArray()
        ids.forEach { array.put(it) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }
}
