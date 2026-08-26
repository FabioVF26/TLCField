package it.vigilfuoco.tlcfield.data

import android.content.Context

object SyncRepository {

    data class SyncSummary(
        val ok: Boolean,
        val uploaded: Int,
        val downloaded: Int,
        val sitesDownloaded: Int,
        val message: String
    )

    fun sync(context: Context): SyncSummary {

        val settings = ServerSettingsRepository.load(context)

        if (settings.baseUrl.isBlank()) {
            return SyncSummary(
                ok = false,
                uploaded = 0,
                downloaded = 0,
                sitesDownloaded = 0,
                message = "Configurare prima l'indirizzo del server"
            )
        }

        val health = ServerApi.health(settings)

        if (!health.ok) {
            return SyncSummary(
                ok = false,
                uploaded = 0,
                downloaded = 0,
                sitesDownloaded = 0,
                message = health.message
            )
        }

        var uploaded = 0
        var failed = 0

        InterventionRepository
            .getAll(context)
            .forEach { intervention ->

                if (
                    ServerApi
                        .uploadIntervention(
                            settings,
                            intervention
                        )
                        .ok
                ) {
                    uploaded++
                } else {
                    failed++
                }
            }

        val (
            downloadResult,
            remoteInterventions
        ) = ServerApi.downloadInterventions(settings)

        if (downloadResult.ok) {
            InterventionRepository.merge(
                context,
                remoteInterventions
            )
        }

        val (
            sitesResult,
            remoteSites
        ) = ServerApi.downloadSites(settings)

        if (
    sitesResult.ok &&
    remoteSites.isNotEmpty()
) {
    SiteCacheRepository.save(
        context,
        remoteSites
    )

    SiteRepository.updateFromServer(
        remoteSites
    )
}
        val interventionsOk =
            downloadResult.ok

        val sitesOk =
            sitesResult.ok

        val ok =
            failed == 0 &&
            interventionsOk &&
            sitesOk

        val message = when {

            ok ->
                "Sincronizzazione completata"

            !sitesOk && interventionsOk ->
                "Interventi sincronizzati, ma aggiornamento siti non riuscito"

            sitesOk && !interventionsOk ->
                "Siti aggiornati, ma sincronizzazione interventi non riuscita"

            else ->
                "Sincronizzazione parziale: $failed invii non riusciti"
        }

        return SyncSummary(
            ok = ok,
            uploaded = uploaded,
            downloaded = if (downloadResult.ok) {
                remoteInterventions.size
            } else {
                0
            },
            sitesDownloaded = if (sitesResult.ok) {
                remoteSites.size
            } else {
                0
            },
            message = message
        )
    }
}
