package it.vigilfuoco.tlcfield.data

import android.content.Context

object SyncRepository {
    data class SyncSummary(
        val ok: Boolean,
        val uploaded: Int,
        val downloaded: Int,
        val message: String
    )

    fun sync(context: Context): SyncSummary {
        val settings = ServerSettingsRepository.load(context)
        if (settings.baseUrl.isBlank()) return SyncSummary(false, 0, 0, "Configurare prima l'indirizzo del server")

        val health = ServerApi.health(settings)
        if (!health.ok) return SyncSummary(false, 0, 0, health.message)

        var uploaded = 0
        var failed = 0
        InterventionRepository.getAll(context).forEach { intervention ->
            if (ServerApi.uploadIntervention(settings, intervention).ok) uploaded++ else failed++
        }

        val (downloadResult, remote) = ServerApi.downloadInterventions(settings)
        if (downloadResult.ok) InterventionRepository.merge(context, remote)

        val ok = failed == 0 && downloadResult.ok
        val msg = if (ok) "Sincronizzazione completata" else "Sincronizzazione parziale: $failed invii non riusciti"
        return SyncSummary(ok, uploaded, if (downloadResult.ok) remote.size else 0, msg)
    }
}
