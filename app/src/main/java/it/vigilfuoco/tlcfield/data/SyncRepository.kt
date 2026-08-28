package it.vigilfuoco.tlcfield.data

import android.content.Context

object SyncRepository {

    data class SyncSummary(
        val ok: Boolean,
        val uploaded: Int,
        val downloaded: Int,
        val sitesDownloaded: Int,
        val personnelDownloaded: Int = 0,
        val vehiclesDownloaded: Int = 0,
        val message: String
    )

    fun sync(context: Context): SyncSummary {

        val settings =
            ServerSettingsRepository.load(context)

        if (settings.baseUrl.isBlank()) {
            return SyncSummary(
                ok = false,
                uploaded = 0,
                downloaded = 0,
                sitesDownloaded = 0,
                personnelDownloaded = 0,
                vehiclesDownloaded = 0,
                message =
                    "Configurare prima l'indirizzo del server"
            )
        }

        val health =
            ServerApi.health(settings)

        if (!health.ok) {
            return SyncSummary(
                ok = false,
                uploaded = 0,
                downloaded = 0,
                sitesDownloaded = 0,
                personnelDownloaded = 0,
                vehiclesDownloaded = 0,
                message = health.message
            )
        }

        // =====================================================
        // UPLOAD INTERVENTI LOCALI
        // =====================================================

        var uploaded = 0
        var failed = 0
        var deletionFailed = 0

        // =====================================================
        // ELIMINAZIONI PENDENTI (ADMIN)
        // =====================================================

        PendingDeletionRepository
            .getAll(context)
            .forEach { interventionId ->

                val deleteResult =
                    ServerApi.deleteIntervention(
                        settings,
                        interventionId
                    )

                if (deleteResult.ok) {
                    PendingDeletionRepository.remove(
                        context,
                        interventionId
                    )
                } else {
                    deletionFailed++
                }
            }

        InterventionRepository
            .getAll(context)
            .forEach { intervention ->

                val result =
                    ServerApi.uploadIntervention(
                        settings,
                        intervention
                    )

                if (result.ok) {
                    uploaded++
                } else {
                    failed++
                }
            }


        // =====================================================
        // DOWNLOAD INTERVENTI
        // =====================================================

        val (
            downloadResult,
            remoteInterventions
        ) = ServerApi.downloadInterventions(
            settings
        )

        if (downloadResult.ok) {
            InterventionRepository.merge(
                context,
                remoteInterventions
            )
        }


        // =====================================================
        // DOWNLOAD SITI
        // =====================================================

        val (
            sitesResult,
            remoteSites
        ) = ServerApi.downloadSites(
            settings
        )

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


        // =====================================================
        // DOWNLOAD PERSONALE
        // =====================================================

        val (
            personnelResult,
            remotePersonnel
        ) = ServerApi.downloadPersonnel(
            settings
        )

        if (
            personnelResult.ok &&
            remotePersonnel.isNotEmpty()
        ) {

            PersonnelVehicleCacheRepository
                .savePersonnel(
                    context,
                    remotePersonnel
                )

            PersonnelRepository
                .updateFromServer(
                    remotePersonnel
                )
        }


        // =====================================================
        // DOWNLOAD AUTOMEZZI
        // =====================================================

        val (
            vehiclesResult,
            remoteVehicles
        ) = ServerApi.downloadVehicles(
            settings
        )

        if (
            vehiclesResult.ok &&
            remoteVehicles.isNotEmpty()
        ) {

            PersonnelVehicleCacheRepository
                .saveVehicles(
                    context,
                    remoteVehicles
                )

            VehicleRepository
                .updateFromServer(
                    remoteVehicles
                )
        }


        // =====================================================
        // ESITO SINCRONIZZAZIONE
        // =====================================================

        val interventionsOk =
            downloadResult.ok

        val sitesOk =
            sitesResult.ok

        val personnelOk =
            personnelResult.ok

        val vehiclesOk =
            vehiclesResult.ok

        val ok =
            failed == 0 &&
            deletionFailed == 0 &&
            interventionsOk &&
            sitesOk &&
            personnelOk &&
            vehiclesOk


        val message = when {

            ok ->
                "Sincronizzazione completata"

            !personnelOk || !vehiclesOk ->
                "Sincronizzazione parziale: anagrafiche personale o automezzi non aggiornate"

            !sitesOk ->
                "Sincronizzazione parziale: aggiornamento siti non riuscito"

            !interventionsOk ->
                "Sincronizzazione parziale: interventi non aggiornati"

            deletionFailed > 0 ->
                "Sincronizzazione parziale: $deletionFailed eliminazioni admin da sincronizzare"

            failed > 0 ->
                "Sincronizzazione parziale: $failed invii non riusciti"

            else ->
                "Sincronizzazione parziale"
        }


        return SyncSummary(
            ok = ok,

            uploaded = uploaded,

            downloaded =
                if (downloadResult.ok) {
                    remoteInterventions.size
                } else {
                    0
                },

            sitesDownloaded =
                if (sitesResult.ok) {
                    remoteSites.size
                } else {
                    0
                },

            personnelDownloaded =
                if (personnelResult.ok) {
                    remotePersonnel.size
                } else {
                    0
                },

            vehiclesDownloaded =
                if (vehiclesResult.ok) {
                    remoteVehicles.size
                } else {
                    0
                },

            message = message
        )
    }
}
