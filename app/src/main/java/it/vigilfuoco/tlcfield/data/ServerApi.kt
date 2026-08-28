package it.vigilfuoco.tlcfield.data

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ServerApi {

    data class Result(
        val ok: Boolean,
        val message: String,
        val body: String = ""
    )

    private fun connection(
        settings: ServerSettingsRepository.Settings,
        path: String,
        method: String
    ): HttpURLConnection {

        require(settings.baseUrl.isNotBlank()) {
            "URL server non configurato"
        }

        return (
            URL(
                settings.baseUrl.trimEnd('/') + path
            ).openConnection() as HttpURLConnection
        ).apply {

            requestMethod = method

            connectTimeout = 8000
            readTimeout = 12000

            setRequestProperty(
                "Accept",
                "application/json"
            )

            setRequestProperty(
                "Content-Type",
                "application/json; charset=utf-8"
            )

            if (settings.apiToken.isNotBlank()) {
                setRequestProperty(
                    "Authorization",
                    "Bearer ${settings.apiToken}"
                )
            }
        }
    }


    // ========================================================
    // HEALTH
    // ========================================================

    fun health(
        settings: ServerSettingsRepository.Settings
    ): Result = runCatching {

        val c = connection(
            settings,
            "/api/v1/health",
            "GET"
        )

        val code = c.responseCode

        val body = (
            if (code in 200..299) {
                c.inputStream
            } else {
                c.errorStream
            }
        )?.bufferedReader()?.use {
            it.readText()
        }.orEmpty()

        Result(
            ok = code in 200..299,
            message = if (code in 200..299) {
                "Server raggiungibile"
            } else {
                "HTTP $code"
            },
            body = body
        )

    }.getOrElse {

        Result(
            false,
            it.message ?: "Errore di connessione"
        )
    }


    // ========================================================
    // INTERVENTI - UPLOAD
    // ========================================================

    fun uploadIntervention(
        settings: ServerSettingsRepository.Settings,
        intervention: Intervention
    ): Result = runCatching {

        val c = connection(
            settings,
            "/api/v1/interventions",
            "POST"
        )

        c.doOutput = true

        c.outputStream
            .bufferedWriter(Charsets.UTF_8)
            .use {
                it.write(
                    InterventionRepository
                        .toJson(intervention)
                        .toString()
                )
            }

        val code = c.responseCode

        val body = (
            if (code in 200..299) {
                c.inputStream
            } else {
                c.errorStream
            }
        )?.bufferedReader()?.use {
            it.readText()
        }.orEmpty()

        Result(
            ok = code in 200..299,
            message = if (code in 200..299) {
                "OK"
            } else {
                "HTTP $code"
            },
            body = body
        )

    }.getOrElse {

        Result(
            false,
            it.message ?: "Errore di connessione"
        )
    }


    // ========================================================
    // INTERVENTI - DOWNLOAD
    // ========================================================

    fun downloadInterventions(
        settings: ServerSettingsRepository.Settings
    ): Pair<Result, List<Intervention>> = runCatching {

        val c = connection(
            settings,
            "/api/v1/interventions",
            "GET"
        )

        val code = c.responseCode

        val body = (
            if (code in 200..299) {
                c.inputStream
            } else {
                c.errorStream
            }
        )?.bufferedReader()?.use {
            it.readText()
        }.orEmpty()

        if (code !in 200..299) {
            return@runCatching Result(
                false,
                "HTTP $code",
                body
            ) to emptyList()
        }

        val array = JSONArray(body)

        val items = buildList {

            for (i in 0 until array.length()) {

                add(
                    InterventionRepository.fromJson(
                        array.getJSONObject(i)
                    )
                )
            }
        }

        Result(
            true,
            "${items.size} interventi ricevuti",
            body
        ) to items

    }.getOrElse {

        Result(
            false,
            it.message ?: "Errore di connessione"
        ) to emptyList()
    }


    // ========================================================
    // SITI TLC - DOWNLOAD
    // ========================================================

    fun downloadSites(
        settings: ServerSettingsRepository.Settings
    ): Pair<Result, List<Site>> = runCatching {

        val c = connection(
            settings,
            "/api/v1/sites",
            "GET"
        )

        val code = c.responseCode

        val body = (
            if (code in 200..299) {
                c.inputStream
            } else {
                c.errorStream
            }
        )?.bufferedReader()?.use {
            it.readText()
        }.orEmpty()

        if (code !in 200..299) {

            return@runCatching Result(
                false,
                "HTTP $code",
                body
            ) to emptyList()
        }

        val array = JSONArray(body)

        val sites = buildList {

            for (i in 0 until array.length()) {

                add(
                    siteFromJson(
                        array.getJSONObject(i)
                    )
                )
            }
        }

        Result(
            true,
            "${sites.size} siti ricevuti",
            body
        ) to sites

    }.getOrElse {

        Result(
            false,
            it.message
                ?: "Errore durante il download dei siti"
        ) to emptyList()
    }

// ========================================================
// PERSONALE - DOWNLOAD
// ========================================================

fun downloadPersonnel(
    settings: ServerSettingsRepository.Settings
): Pair<Result, List<Personnel>> = runCatching {

    val c = connection(
        settings,
        "/api/v1/personnel",
        "GET"
    )

    val code = c.responseCode

    val body = (
        if (code in 200..299) {
            c.inputStream
        } else {
            c.errorStream
        }
    )?.bufferedReader()?.use {
        it.readText()
    }.orEmpty()

    if (code !in 200..299) {
        return@runCatching Result(
            false,
            "HTTP $code",
            body
        ) to emptyList()
    }

    val array = JSONArray(body)

    val personnel = buildList {

        for (i in 0 until array.length()) {

            val item = array.getJSONObject(i)

            add(
                Personnel(
                    id = item.getInt("id"),
                    qualification = item.optString(
                        "qualification",
                        ""
                    ),
                    fullName = item.optString(
                        "fullName",
                        ""
                    ),
                    active = item.optBoolean(
                        "active",
                        true
                    )
                )
            )
        }
    }

    Result(
        true,
        "${personnel.size} operatori ricevuti",
        body
    ) to personnel

}.getOrElse {

    Result(
        false,
        it.message
            ?: "Errore durante il download del personale"
    ) to emptyList()
}


// ========================================================
// AUTOMEZZI - DOWNLOAD
// ========================================================

fun downloadVehicles(
    settings: ServerSettingsRepository.Settings
): Pair<Result, List<Vehicle>> = runCatching {

    val c = connection(
        settings,
        "/api/v1/vehicles",
        "GET"
    )

    val code = c.responseCode

    val body = (
        if (code in 200..299) {
            c.inputStream
        } else {
            c.errorStream
        }
    )?.bufferedReader()?.use {
        it.readText()
    }.orEmpty()

    if (code !in 200..299) {
        return@runCatching Result(
            false,
            "HTTP $code",
            body
        ) to emptyList()
    }

    val array = JSONArray(body)

    val vehicles = buildList {

        for (i in 0 until array.length()) {

            val item = array.getJSONObject(i)

            add(
                Vehicle(
                    id = item.getInt("id"),
                    description = item.optString(
                        "description",
                        ""
                    ),
                    plate = item.optString(
                        "plate",
                        ""
                    ),
                    active = item.optBoolean(
                        "active",
                        true
                    )
                )
            )
        }
    }

    Result(
        true,
        "${vehicles.size} automezzi ricevuti",
        body
    ) to vehicles

}.getOrElse {

    Result(
        false,
        it.message
            ?: "Errore durante il download degli automezzi"
    ) to emptyList()
}
 
    // ========================================================
    // CONVERSIONE JSON -> SITE
    // ========================================================

    private fun siteFromJson(
        json: JSONObject
    ): Site {

        val links = mutableListOf<RadioLink>()

        val linksArray = json.optJSONArray("links")

        if (linksArray != null) {

            for (i in 0 until linksArray.length()) {

                val item = linksArray.getJSONObject(i)

                links.add(
                    RadioLink(
                        name = item.optString(
                            "name",
                            ""
                        ),

                        type = item.optString(
                            "type",
                            ""
                        ),

                        rssiDbm = nullableInt(
                            item,
                            "rssiDbm"
                        ),

                        txMhz = nullableDouble(
                            item,
                            "txMhz"
                        ),

                        rxMhz = nullableDouble(
                            item,
                            "rxMhz"
                        )
                    )
                )
            }
        }


        val kairosEndpoints =
            mutableListOf<KairosEndpoint>()

        val kairosArray =
            json.optJSONArray("kairosEndpoints")

        if (kairosArray != null) {

            for (i in 0 until kairosArray.length()) {

                val item =
                    kairosArray.getJSONObject(i)

                kairosEndpoints.add(
                    KairosEndpoint(
                        label = item.optString(
                            "label",
                            ""
                        ),

                        ipAddress = item.optString(
                            "ipAddress",
                            ""
                        )
                    )
                )
            }
        }


        return Site(

            id = json.getString("id"),

            name = json.getString("name"),

            code = nullableString(
                json,
                "code"
            ),

            network = nullableString(
                json,
                "network"
            ),

            owner = nullableString(
                json,
                "owner"
            ),

            latitude = nullableDouble(
                json,
                "latitude"
            ),

            longitude = nullableDouble(
                json,
                "longitude"
            ),

            altitudeM = nullableInt(
                json,
                "altitudeM"
            ),

            rackLocation = nullableString(
                json,
                "rackLocation"
            ),

            phone = nullableString(
                json,
                "phone"
            ),

            email = nullableString(
                json,
                "email"
            ),

            accessNotes = nullableString(
                json,
                "accessNotes"
            ),

            technicalNotes = nullableString(
                json,
                "technicalNotes"
            ),

            navigationVerified =
                if (
                    json.has("navigationVerified") &&
                    !json.isNull("navigationVerified")
                ) {
                    json.getBoolean(
                        "navigationVerified"
                    )
                } else {
                    true
                },

            kairosEndpoints = kairosEndpoints,

            links = links
        )
    }


    // ========================================================
    // FUNZIONI JSON NULL-SAFE
    // ========================================================

    private fun nullableString(
        json: JSONObject,
        key: String
    ): String? {

        if (
            !json.has(key) ||
            json.isNull(key)
        ) {
            return null
        }

        return json.getString(key)
    }


    private fun nullableDouble(
        json: JSONObject,
        key: String
    ): Double? {

        if (
            !json.has(key) ||
            json.isNull(key)
        ) {
            return null
        }

        return json.getDouble(key)
    }


    private fun nullableInt(
        json: JSONObject,
        key: String
    ): Int? {

        if (
            !json.has(key) ||
            json.isNull(key)
        ) {
            return null
        }

        return json.getInt(key)
    }
}
