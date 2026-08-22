package it.vigilfuoco.tlcfield.data

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ServerApi {
    data class Result(val ok: Boolean, val message: String, val body: String = "")

    private fun connection(settings: ServerSettingsRepository.Settings, path: String, method: String): HttpURLConnection {
        require(settings.baseUrl.isNotBlank()) { "URL server non configurato" }
        return (URL(settings.baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8000
            readTimeout = 12000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            if (settings.apiToken.isNotBlank()) setRequestProperty("Authorization", "Bearer ${settings.apiToken}")
        }
    }

    fun health(settings: ServerSettingsRepository.Settings): Result = runCatching {
        val c = connection(settings, "/api/v1/health", "GET")
        val code = c.responseCode
        val body = (if (code in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
        Result(code in 200..299, if (code in 200..299) "Server raggiungibile" else "HTTP $code", body)
    }.getOrElse { Result(false, it.message ?: "Errore di connessione") }

    fun uploadIntervention(settings: ServerSettingsRepository.Settings, intervention: Intervention): Result = runCatching {
        val c = connection(settings, "/api/v1/interventions", "POST")
        c.doOutput = true
        c.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(InterventionRepository.toJson(intervention).toString()) }
        val code = c.responseCode
        val body = (if (code in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
        Result(code in 200..299, if (code in 200..299) "OK" else "HTTP $code", body)
    }.getOrElse { Result(false, it.message ?: "Errore di connessione") }

    fun downloadInterventions(settings: ServerSettingsRepository.Settings): Pair<Result, List<Intervention>> = runCatching {
        val c = connection(settings, "/api/v1/interventions", "GET")
        val code = c.responseCode
        val body = (if (code in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) return@runCatching Result(false, "HTTP $code", body) to emptyList()
        val array = JSONArray(body)
        val items = buildList {
            for (i in 0 until array.length()) add(InterventionRepository.fromJson(array.getJSONObject(i)))
        }
        Result(true, "${items.size} interventi ricevuti", body) to items
    }.getOrElse { Result(false, it.message ?: "Errore di connessione") to emptyList() }
}
