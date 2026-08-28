package it.vigilfuoco.tlcfield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.vigilfuoco.tlcfield.data.PersonnelVehicleCacheRepository
import it.vigilfuoco.tlcfield.data.SiteCacheRepository
import it.vigilfuoco.tlcfield.data.SiteRepository
import it.vigilfuoco.tlcfield.ui.screens.*
import it.vigilfuoco.tlcfield.ui.theme.TLCFieldTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // =====================================================
        // RIPRISTINO CACHE SITI
        // =====================================================

        val cachedSites = SiteCacheRepository.load(this)

        if (cachedSites.isNotEmpty()) {
            SiteRepository.updateFromServer(cachedSites)
        }

        // =====================================================
        // RIPRISTINO CACHE PERSONALE E AUTOMEZZI
        // =====================================================

        PersonnelVehicleCacheRepository.restore(this)

        // =====================================================
        // INTERFACCIA APP
        // =====================================================

        setContent {

            TLCFieldTheme {

                val navController =
                    rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    composable("home") {

                        HomeScreen(
                            onSites = {
                                navController.navigate("sites")
                            },
                            onMap = {
                                navController.navigate("sites_map")
                            },
                            onNewIntervention = {
                                navController.navigate(
                                    "new_intervention"
                                )
                            },
                            onHistory = {
                                navController.navigate("history")
                            },
                            onDiagnosis = {
                                navController.navigate(
                                    "kairos_diagnosis"
                                )
                            },
                            onDocumentation = {
                                navController.navigate(
                                    "documentation"
                                )
                            },
                            onRfTools = {
                                navController.navigate(
                                    "rf_tools"
                                )
                            },
                            onServer = {
                                navController.navigate(
                                    "server_sync"
                                )
                            }
                        )
                    }

                    composable("sites") {

                        SitesScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onSiteSelected = {
                                navController.navigate(
                                    "site/$it"
                                )
                            }
                        )
                    }

                    composable("sites_map") {

                        SitesMapScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onSiteSelected = {
                                navController.navigate(
                                    "site/$it"
                                )
                            }
                        )
                    }

                    composable("site/{siteId}") { entry ->

                        SiteDetailScreen(
                            siteId =
                                entry.arguments
                                    ?.getString("siteId")
                                    .orEmpty(),

                            onBack = {
                                navController.popBackStack()
                            },

                            onNewIntervention = {
                                navController.navigate(
                                    "new_intervention/$it"
                                )
                            }
                        )
                    }

                    composable("new_intervention") {

                        NewInterventionScreen(
                            onBack = {
                                navController.popBackStack()
                            },

                            onSaved = {

                                navController.navigate(
                                    "history"
                                ) {

                                    popUpTo(
                                        "new_intervention"
                                    ) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }

                    composable(
                        "new_intervention/{siteId}"
                    ) { entry ->

                        NewInterventionScreen(
                            initialSiteId =
                                entry.arguments
                                    ?.getString("siteId"),

                            onBack = {
                                navController.popBackStack()
                            },

                            onSaved = {
                                navController.navigate(
                                    "history"
                                )
                            }
                        )
                    }

                    composable("history") {

                        HistoryScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("documentation") {

                        DocumentationScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("rf_tools") {

                        RfToolsScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("server_sync") {

                        ServerSyncScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("kairos_diagnosis") {

                        KairosDiagnosisScreen(
                            onBack = {
                                navController.popBackStack()
                            },

                            onAlarms = {
                                navController.navigate(
                                    "kairos_alarms"
                                )
                            }
                        )
                    }

                    composable("kairos_alarms") {

                        KairosAlarmScreen(
                            onBack = {
                                navController.popBackStack()
                            },

                            onAlarmSelected = {
                                navController.navigate(
                                    "kairos_alarm/$it"
                                )
                            }
                        )
                    }

                    composable(
                        "kairos_alarm/{alarmNumber}"
                    ) { entry ->

                        KairosAlarmDetailScreen(
                            alarmNumber =
                                entry.arguments
                                    ?.getString(
                                        "alarmNumber"
                                    )
                                    ?.toIntOrNull()
                                    ?: -1,

                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
