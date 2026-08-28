package it.vigilfuoco.tlcfield.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject


object PersonnelRepository {

    @Volatile
    private var items: List<Personnel> = emptyList()

    fun getAll(): List<Personnel> =
        items
            .filter { it.active }
            .sortedBy { it.fullName }

    fun updateFromServer(personnel: List<Personnel>) {
        items = personnel
    }
}


object VehicleRepository {

    @Volatile
    private var items: List<Vehicle> = emptyList()

    fun getAll(): List<Vehicle> =
        items
            .filter { it.active }
            .sortedWith(
                compareBy<Vehicle>(
                    { it.description },
                    { it.plate }
                )
            )

    fun updateFromServer(vehicles: List<Vehicle>) {
        items = vehicles
    }
}


object PersonnelVehicleCacheRepository {

    private const val PREFS_NAME =
        "tlc_field_reference_cache"

    private const val KEY_PERSONNEL =
        "personnel_json"

    private const val KEY_VEHICLES =
        "vehicles_json"


    fun savePersonnel(
        context: Context,
        personnel: List<Personnel>
    ) {

        val array = JSONArray()

        personnel.forEach { person ->

            array.put(
                JSONObject().apply {
                    put("id", person.id)
                    put(
                        "qualification",
                        person.qualification
                    )
                    put(
                        "fullName",
                        person.fullName
                    )
                    put(
                        "active",
                        person.active
                    )
                }
            )
        }

        prefs(context)
            .edit()
            .putString(
                KEY_PERSONNEL,
                array.toString()
            )
            .apply()
    }


    fun loadPersonnel(
        context: Context
    ): List<Personnel> {

        val raw =
            prefs(context)
                .getString(
                    KEY_PERSONNEL,
                    null
                )
                ?: return emptyList()

        return runCatching {

            val array = JSONArray(raw)

            buildList {

                for (
                    i in 0 until array.length()
                ) {

                    val item =
                        array.getJSONObject(i)

                    add(
                        Personnel(
                            id = item.getInt("id"),

                            qualification =
                                item.optString(
                                    "qualification",
                                    ""
                                ),

                            fullName =
                                item.optString(
                                    "fullName",
                                    ""
                                ),

                            active =
                                item.optBoolean(
                                    "active",
                                    true
                                )
                        )
                    )
                }
            }

        }.getOrDefault(
            emptyList()
        )
    }


    fun saveVehicles(
        context: Context,
        vehicles: List<Vehicle>
    ) {

        val array = JSONArray()

        vehicles.forEach { vehicle ->

            array.put(
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

                    put(
                        "active",
                        vehicle.active
                    )
                }
            )
        }

        prefs(context)
            .edit()
            .putString(
                KEY_VEHICLES,
                array.toString()
            )
            .apply()
    }


    fun loadVehicles(
        context: Context
    ): List<Vehicle> {

        val raw =
            prefs(context)
                .getString(
                    KEY_VEHICLES,
                    null
                )
                ?: return emptyList()

        return runCatching {

            val array = JSONArray(raw)

            buildList {

                for (
                    i in 0 until array.length()
                ) {

                    val item =
                        array.getJSONObject(i)

                    add(
                        Vehicle(
                            id = item.getInt("id"),

                            description =
                                item.optString(
                                    "description",
                                    ""
                                ),

                            plate =
                                item.optString(
                                    "plate",
                                    ""
                                ),

                            active =
                                item.optBoolean(
                                    "active",
                                    true
                                )
                        )
                    )
                }
            }

        }.getOrDefault(
            emptyList()
        )
    }


    fun restore(
        context: Context
    ) {

        val personnel =
            loadPersonnel(context)

        if (personnel.isNotEmpty()) {
            PersonnelRepository
                .updateFromServer(personnel)
        }

        val vehicles =
            loadVehicles(context)

        if (vehicles.isNotEmpty()) {
            VehicleRepository
                .updateFromServer(vehicles)
        }
    }


    private fun prefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
}
