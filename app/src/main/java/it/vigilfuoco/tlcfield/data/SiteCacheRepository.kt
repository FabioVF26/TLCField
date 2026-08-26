package it.vigilfuoco.tlcfield.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object SiteCacheRepository {

    private const val PREFS_NAME = "tlc_field_site_cache"
    private const val KEY_SITES = "sites_json"

    fun save(
        context: Context,
        sites: List<Site>
    ) {
        val array = JSONArray()

        sites.forEach { site ->
            array.put(siteToJson(site))
        }

        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_SITES,
                array.toString()
            )
            .apply()
    }

    fun load(
        context: Context
    ): List<Site> {

        val raw = context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .getString(
                KEY_SITES,
                null
            )
            ?: return emptyList()

        return runCatching {

            val array = JSONArray(raw)

            buildList {

                for (i in 0 until array.length()) {
                    add(
                        siteFromJson(
                            array.getJSONObject(i)
                        )
                    )
                }
            }

        }.getOrElse {
            emptyList()
        }
    }

    fun clear(
        context: Context
    ) {
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(KEY_SITES)
            .apply()
    }

    private fun siteToJson(
        site: Site
    ): JSONObject {

        return JSONObject().apply {

            put("id", site.id)
            put("name", site.name)

            putNullable(
                "code",
                site.code
            )

            putNullable(
                "network",
                site.network
            )

            putNullable(
                "owner",
                site.owner
            )

            putNullable(
                "latitude",
                site.latitude
            )

            putNullable(
                "longitude",
                site.longitude
            )

            putNullable(
                "altitudeM",
                site.altitudeM
            )

            putNullable(
                "rackLocation",
                site.rackLocation
            )

            putNullable(
                "phone",
                site.phone
            )

            putNullable(
                "email",
                site.email
            )

            putNullable(
                "accessNotes",
                site.accessNotes
            )

            putNullable(
                "technicalNotes",
                site.technicalNotes
            )

            put(
                "navigationVerified",
                site.navigationVerified
            )

            put(
                "kairosEndpoints",
                JSONArray().apply {

                    site.kairosEndpoints.forEach { endpoint ->

                        put(
                            JSONObject().apply {

                                put(
                                    "label",
                                    endpoint.label
                                )

                                put(
                                    "ipAddress",
                                    endpoint.ipAddress
                                )
                            }
                        )
                    }
                }
            )

            put(
                "links",
                JSONArray().apply {

                    site.links.forEach { link ->

                        put(
                            JSONObject().apply {

                                put(
                                    "name",
                                    link.name
                                )

                                put(
                                    "type",
                                    link.type
                                )

                                putNullable(
                                    "rssiDbm",
                                    link.rssiDbm
                                )

                                putNullable(
                                    "txMhz",
                                    link.txMhz
                                )

                                putNullable(
                                    "rxMhz",
                                    link.rxMhz
                                )
                            }
                        )
                    }
                }
            )
        }
    }

    private fun siteFromJson(
        json: JSONObject
    ): Site {

        val links = buildList {

            val array =
                json.optJSONArray("links")
                    ?: JSONArray()

            for (i in 0 until array.length()) {

                val item =
                    array.getJSONObject(i)

                add(
                    RadioLink(
                        name = item.optString(
                            "name",
                            ""
                        ),
                        type = item.optString(
                            "type",
                            ""
                        ),
                        rssiDbm =
                            nullableInt(
                                item,
                                "rssiDbm"
                            ),
                        txMhz =
                            nullableDouble(
                                item,
                                "txMhz"
                            ),
                        rxMhz =
                            nullableDouble(
                                item,
                                "rxMhz"
                            )
                    )
                )
            }
        }

        val kairosEndpoints = buildList {

            val array =
                json.optJSONArray(
                    "kairosEndpoints"
                )
                    ?: JSONArray()

            for (i in 0 until array.length()) {

                val item =
                    array.getJSONObject(i)

                add(
                    KairosEndpoint(
                        label = item.optString(
                            "label",
                            ""
                        ),
                        ipAddress =
                            item.optString(
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
                json.optBoolean(
                    "navigationVerified",
                    true
                ),
            kairosEndpoints =
                kairosEndpoints,
            links = links
        )
    }

    private fun JSONObject.putNullable(
        key: String,
        value: Any?
    ) {
        if (value == null) {
            put(
                key,
                JSONObject.NULL
            )
        } else {
            put(
                key,
                value
            )
        }
    }

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
