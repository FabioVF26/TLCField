package it.vigilfuoco.tlcfield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.vigilfuoco.tlcfield.ui.screens.*
import it.vigilfuoco.tlcfield.ui.theme.TLCFieldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TLCFieldTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onSites = { navController.navigate("sites") },
                            onNewIntervention = { navController.navigate("new_intervention") },
                            onHistory = { navController.navigate("history") },
                            onDiagnosis = { navController.navigate("kairos_diagnosis") }
                        )
                    }
                    composable("sites") { SitesScreen(onBack = { navController.popBackStack() }, onSiteSelected = { navController.navigate("site/$it") }) }
                    composable("site/{siteId}") { entry -> SiteDetailScreen(entry.arguments?.getString("siteId").orEmpty(), onBack = { navController.popBackStack() }) }
                    composable("new_intervention") {
                        NewInterventionScreen(
                            onBack = { navController.popBackStack() },
                            onSaved = { navController.navigate("history") { popUpTo("new_intervention") { inclusive = true } } }
                        )
                    }
                    composable("history") { HistoryScreen(onBack = { navController.popBackStack() }) }
                    composable("kairos_diagnosis") {
                        KairosDiagnosisScreen(onBack = { navController.popBackStack() }, onAlarms = { navController.navigate("kairos_alarms") })
                    }
                    composable("kairos_alarms") {
                        KairosAlarmScreen(
                            onBack = { navController.popBackStack() },
                            onAlarmSelected = { navController.navigate("kairos_alarm/$it") }
                        )
                    }
                    composable("kairos_alarm/{alarmNumber}") { entry ->
                        KairosAlarmDetailScreen(
                            alarmNumber = entry.arguments?.getString("alarmNumber")?.toIntOrNull() ?: -1,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
