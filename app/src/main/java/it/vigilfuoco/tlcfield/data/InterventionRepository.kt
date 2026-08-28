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

    fun save(
        context: Context,
        intervention: Intervention
    ) {
        val items =
            getAll(context)
                .toMutableList()

        items.add(
            0,
            intervention
        )

        persist(
            context,
            items
        )
    }

    fun getAll(
        context: Context
    ): List<Intervention> {

        val raw =
            context
                .getSharedPreferences(
                    PREFS,
                    Context.MODE_PRIVATE
                )
                .getString(
                    KEY,
                    "[]"
                )
                ?: "[]"

        return runCatching {

            val array =
                JSONArray(raw)

            buildList {

                for (
                    i in 0 until array.length()
                ) {

                    add(
                        fromJson(
                            array.getJSONObject(i)
                        )
                    )
                }
            }

        }.getOrDefault(
            emptyList()
        )
    }

    fun newId(): String =
        UUID.randomUUID()
            .toString()

    fun replaceAll(
        context: Context,
        items: List<Intervention>
    ) {

        persist(
            context,
            items.sortedByDescending {
                it.timestamp
            }
        )
    }

    fun merge(
        context: Context,
        remote: List<Intervention>
    ) {

        val merged =
            LinkedHashMap<
                String,
                Intervention
            >()

        (
            getAll(context) +
                remote
        )
            .sortedByDescending {
                it.timestamp
            }
            .forEach { item ->

                if (
                    !merged.containsKey(
                        item.id
                    )
                ) {
                    merged[item.id] =
                        item
                }
            }

        persist(
            context,
            merged.values.toList()
        )
    }

    private fun persist(
        context: Context,
        items: List<Intervention>
    ) {

        val array =
            JSONArray()

        items.forEach {
            array.put(
                toJson(it)
            )
        }

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY,
                array.toString()
            )
            .apply()
    }

    // =========================================================
    // INTERVENTION -> JSON
    // =========================================================

    fun toJson(
        i: Intervention
    ) = JSONObject().apply {

        put(
            "id",
            i.id
        )

        put(
            "siteId",
            i.siteId
        )

        put(
            "siteName",
            i.siteName
        )

        put(
            "timestamp",
            i.timestamp
        )

        put(
            "type",
            i.type
        )

        put(
            "reportedProblem",
            i.reportedProblem
        )

        put(
            "initialState",
            i.initialState
        )

        put(
            "powerOk",
            i.powerOk
        )

        put(
            "radioOn",
            i.radioOn
        )

        put(
            "alarms",
            i.alarms
        )

        put(
            "ipLinkOk",
            i.ipLinkOk
        )

        put(
            "notes",
            i.notes
        )

        put(
            "result",
            i.result
        )

        // =====================================================
        // MISURE RSSI
        // =====================================================

        val measurements =
            JSONArray()

        i.measurements.forEach { m ->

            measurements.put(

                JSONObject().apply {

                    put(
                        "linkName",
                        m.linkName
                    )

                    if (
                        m.referenceRssi != null
                    ) {
                        put(
                            "referenceRssi",
                            m.referenceRssi
                        )
                    }

                    if (
                        m.measuredRssi != null
                    ) {
                        put(
                            "measuredRssi",
                            m.measuredRssi
                        )
                    }
                }
            )
        }

        put(
            "measurements",
            measurements
        )

        // =====================================================
        // FOTO
        // =====================================================

        val photos =
            JSONArray()

        i.photos.forEach { p ->

            photos.put(

                JSONObject().apply {

                    put(
                        "path",
                        p.path
                    )

                    put(
                        "category",
                        p.category
                    )
                }
            )
        }

        put(
            "photos",
            photos
        )

        // =====================================================
        // PERSONALE INTERVENUTO
        // =====================================================

        val personnel =
            JSONArray()

        i.personnel.forEach { person ->

            personnel.put(

                JSONObject().apply {

                    put(
                        "id",
                        person.id
                    )

                    put(
                        "qualification",
                        person.qualification
                    )

                    put(
                        "fullName",
                        person.fullName
                    )
                }
            )
        }

        put(
            "personnel",
            personnel
        )

        // =====================================================
        // AUTOMEZZI UTILIZZATI
        // =====================================================

        val vehicles =
            JSONArray()

        i.vehicles.forEach { vehicle ->

            vehicles.put(

                JSONObject().apply {

                    put(
                        "id",
                        vehicle.id
                    )

                    put(
                        "description",
                        vehicle.description
                    )

                    put(
                        "plate",
                        vehicle.plate
                    )
                }
            )
        }

        put(
            "vehicles",
            vehicles
        )

        // =====================================================
        // KAIROS
        // =====================================================

        val kairosAlarms =
            JSONArray()

        i.kairosAlarmNumbers
            .forEach {
                kairosAlarms.put(it)
            }

        put(
            "kairosAlarmNumbers",
            kairosAlarms
        )

        val kairosChecks =
            JSONArray()

        i.kairosCompletedChecks
            .forEach {
                kairosChecks.put(it)
            }

        put(
            "kairosCompletedChecks",
            kairosChecks
        )

        put(
            "kairosDiagnosticNotes",
            i.kairosDiagnosticNotes
        )

        i.kairosSnapshot
            ?.let { k ->

                put(
                    "kairosSnapshot",

                    JSONObject().apply {

                        k.supplyVoltageV
                            ?.let {
                                put(
                                    "supplyVoltageV",
                                    it
                                )
                            }

                        k.txTemperatureC
                            ?.let {
                                put(
                                    "txTemperatureC",
                                    it
                                )
                            }

                        k.forwardPowerW
                            ?.let {
                                put(
                                    "forwardPowerW",
                                    it
                                )
                            }

                        k.reflectedPowerW
                            ?.let {
                                put(
                                    "reflectedPowerW",
                                    it
                                )
                            }

                        k.rssiMainDbm
                            ?.let {
                                put(
                                    "rssiMainDbm",
                                    it
                                )
                            }

                        k.rssiDiversityDbm
                            ?.let {
                                put(
                                    "rssiDiversityDbm",
                                    it
                                )
                            }

                        put(
                            "synchronizationSource",
                            k.synchronizationSource
                        )
                    }
                )
            }
    }

    // =========================================================
    // JSON -> INTERVENTION
    // =========================================================

    fun fromJson(
        o: JSONObject
    ): Intervention {

        // =====================================================
        // MISURE RSSI
        // =====================================================

        val measurements =
            mutableListOf<
                RssiMeasurement
            >()

        val measurementArray =
            o.optJSONArray(
                "measurements"
            )
                ?: JSONArray()

        for (
            i in 0 until measurementArray.length()
        ) {

            val m =
                measurementArray
                    .getJSONObject(i)

            measurements +=
                RssiMeasurement(

                    linkName =
                        m.optString(
                            "linkName"
                        ),

                    referenceRssi =
                        if (
                            m.has(
                                "referenceRssi"
                            ) &&
                            !m.isNull(
                                "referenceRssi"
                            )
                        ) {
                            m.optInt(
                                "referenceRssi"
                            )
                        } else {
                            null
                        },

                    measuredRssi =
                        if (
                            m.has(
                                "measuredRssi"
                            ) &&
                            !m.isNull(
                                "measuredRssi"
                            )
                        ) {
                            m.optInt(
                                "measuredRssi"
                            )
                        } else {
                            null
                        }
                )
        }

        // =====================================================
        // FOTO
        // =====================================================

        val photos =
            mutableListOf<
                InterventionPhoto
            >()

        val photoArray =
            o.optJSONArray(
                "photos"
            )
                ?: JSONArray()

        for (
            i in 0 until photoArray.length()
        ) {

            val p =
                photoArray
                    .getJSONObject(i)

            photos +=
                InterventionPhoto(

                    path =
                        p.optString(
                            "path"
                        ),

                    category =
                        p.optString(
                            "category",
                            "Altro"
                        )
                )
        }

        // =====================================================
        // PERSONALE
        // =====================================================

        val personnel =
            mutableListOf<
                InterventionPersonnel
            >()

        val personnelArray =
            o.optJSONArray(
                "personnel"
            )
                ?: JSONArray()

        for (
            i in 0 until personnelArray.length()
        ) {

            val person =
                personnelArray
                    .getJSONObject(i)

            personnel +=
                InterventionPersonnel(

                    id =
                        person.optInt(
                            "id"
                        ),

                    qualification =
                        person.optString(
                            "qualification"
                        ),

                    fullName =
                        person.optString(
                            "fullName"
                        )
                )
        }

        // =====================================================
        // AUTOMEZZI
        // =====================================================

        val vehicles =
            mutableListOf<
                InterventionVehicle
            >()

        val vehicleArray =
            o.optJSONArray(
                "vehicles"
            )
                ?: JSONArray()

        for (
            i in 0 until vehicleArray.length()
        ) {

            val vehicle =
                vehicleArray
                    .getJSONObject(i)

            vehicles +=
                InterventionVehicle(

                    id =
                        vehicle.optInt(
                            "id"
                        ),

                    description =
                        vehicle.optString(
                            "description"
                        ),

                    plate =
                        vehicle.optString(
                            "plate"
                        )
                )
        }

        // =====================================================
        // KAIROS
        // =====================================================

        val kairosAlarmNumbers =
            mutableListOf<Int>()

        val alarmArray =
            o.optJSONArray(
                "kairosAlarmNumbers"
            )
                ?: JSONArray()

        for (
            i in 0 until alarmArray.length()
        ) {

            kairosAlarmNumbers +=
                alarmArray.optInt(i)
        }

        val kairosCompletedChecks =
            mutableListOf<String>()

        val checkArray =
            o.optJSONArray(
                "kairosCompletedChecks"
            )
                ?: JSONArray()

        for (
            i in 0 until checkArray.length()
        ) {

            kairosCompletedChecks +=
                checkArray.optString(i)
        }

        val snapshotObject =
            o.optJSONObject(
                "kairosSnapshot"
            )

        val kairosSnapshot =
            snapshotObject
                ?.let { k ->

                    KairosSnapshot(

                        supplyVoltageV =
                            if (
                                k.has(
                                    "supplyVoltageV"
                                ) &&
                                !k.isNull(
                                    "supplyVoltageV"
                                )
                            ) {
                                k.optDouble(
                                    "supplyVoltageV"
                                )
                            } else {
                                null
                            },

                        txTemperatureC =
                            if (
                                k.has(
                                    "txTemperatureC"
                                ) &&
                                !k.isNull(
                                    "txTemperatureC"
                                )
                            ) {
                                k.optDouble(
                                    "txTemperatureC"
                                )
                            } else {
                                null
                            },

                        forwardPowerW =
                            if (
                                k.has(
                                    "forwardPowerW"
                                ) &&
                                !k.isNull(
                                    "forwardPowerW"
                                )
                            ) {
                                k.optDouble(
                                    "forwardPowerW"
                                )
                            } else {
                                null
                            },

                        reflectedPowerW =
                            if (
                                k.has(
                                    "reflectedPowerW"
                                ) &&
                                !k.isNull(
                                    "reflectedPowerW"
                                )
                            ) {
                                k.optDouble(
                                    "reflectedPowerW"
                                )
                            } else {
                                null
                            },

                        rssiMainDbm =
                            if (
                                k.has(
                                    "rssiMainDbm"
                                ) &&
                                !k.isNull(
                                    "rssiMainDbm"
                                )
                            ) {
                                k.optInt(
                                    "rssiMainDbm"
                                )
                            } else {
                                null
                            },

                        rssiDiversityDbm =
                            if (
                                k.has(
                                    "rssiDiversityDbm"
                                ) &&
                                !k.isNull(
                                    "rssiDiversityDbm"
                                )
                            ) {
                                k.optInt(
                                    "rssiDiversityDbm"
                                )
                            } else {
                                null
                            },

                        synchronizationSource =
                            k.optString(
                                "synchronizationSource"
                            )
                    )
                }

        // =====================================================
        // COSTRUZIONE INTERVENTO
        // =====================================================

        return Intervention(

            id =
                o.optString(
                    "id"
                ),

            siteId =
                o.optString(
                    "siteId"
                ),

            siteName =
                o.optString(
                    "siteName"
                ),

            timestamp =
                o.optLong(
                    "timestamp"
                ),

            type =
                o.optString(
                    "type"
                ),

            reportedProblem =
                o.optString(
                    "reportedProblem"
                ),

            initialState =
                o.optString(
                    "initialState"
                ),

            powerOk =
                o.optBoolean(
                    "powerOk"
                ),

            radioOn =
                o.optBoolean(
                    "radioOn"
                ),

            alarms =
                o.optBoolean(
                    "alarms"
                ),

            ipLinkOk =
                o.optBoolean(
                    "ipLinkOk"
                ),

            notes =
                o.optString(
                    "notes"
                ),

            result =
                o.optString(
                    "result"
                ),

            measurements =
                measurements,

            photos =
                photos,

            personnel =
                personnel,

            vehicles =
                vehicles,

            kairosAlarmNumbers =
                kairosAlarmNumbers,

            kairosCompletedChecks =
                kairosCompletedChecks,

            kairosDiagnosticNotes =
                o.optString(
                    "kairosDiagnosticNotes"
                ),

            kairosSnapshot =
                kairosSnapshot
        )
    }
}
