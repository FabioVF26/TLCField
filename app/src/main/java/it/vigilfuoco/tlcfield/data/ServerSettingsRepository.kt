package it.vigilfuoco.tlcfield.data

import android.content.Context

object ServerSettingsRepository {
    private const val PREFS = "tlc_field_server"
    private const val KEY_URL = "base_url"
    private const val KEY_TOKEN = "api_token"

    data class Settings(val baseUrl: String, val apiToken: String)

    fun load(context: Context): Settings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Settings(
            baseUrl = prefs.getString(KEY_URL, "") ?: "",
            apiToken = prefs.getString(KEY_TOKEN, "") ?: ""
        )
    }

    fun save(context: Context, baseUrl: String, apiToken: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_URL, baseUrl.trim().trimEnd('/'))
            .putString(KEY_TOKEN, apiToken.trim())
            .apply()
    }
}
