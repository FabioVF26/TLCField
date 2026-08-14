package it.vigilfuoco.tlcfield.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vigilfuoco.tlcfield.data.KairosRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KairosAlarmDetailScreen(alarmNumber: Int, onBack: () -> Unit) {
    val alarm = KairosRepository.alarm(alarmNumber)
    val guide = KairosRepository.guide(alarmNumber)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(alarm?.let { "#${it.number} ${it.label}" } ?: "Allarme KAIROS") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Indietro") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                alarm?.let {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${it.severity.label} • ${it.diagnosticArea}", style = MaterialTheme.typography.titleMedium)
                            Text("Livello ${it.severity.level}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Significato dell'evento", style = MaterialTheme.typography.titleMedium)
                        Text(guide.meaning, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
            if (guide.values.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("Valori e soglie documentate", style = MaterialTheme.typography.titleMedium)
                            guide.values.forEach { Text("• $it") }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Verifiche guidate", style = MaterialTheme.typography.titleMedium)
                        guide.checks.forEachIndexed { index, check -> Text("${index + 1}. $check") }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Fonte tecnica", style = MaterialTheme.typography.titleMedium)
                        Text(guide.sourceNote, modifier = Modifier.padding(top = 6.dp))
                        Text(
                            "Le verifiche sono un supporto operativo ricavato dai manuali allegati. Non sostituiscono la configurazione specifica del sito né autorizzano modifiche automatiche ai parametri.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Precauzione", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Text("Prima di restart, reboot, Vtune o modifiche di configurazione, acquisire lo stato e verificare la procedura nel manuale applicabile. Lo shutdown remoto non è proposto come azione diagnostica.")
                    }
                }
            }
        }
    }
}
