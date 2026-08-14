package it.vigilfuoco.tlcfield.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vigilfuoco.tlcfield.data.AlarmSeverity
import it.vigilfuoco.tlcfield.data.KairosRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KairosAlarmScreen(onBack: () -> Unit, onAlarmSelected: (Int) -> Unit) {
    var filter by remember { mutableStateOf<AlarmSeverity?>(null) }
    val alarms = KairosRepository.alarms.filter { filter == null || it.severity == filter }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Allarmi KAIROS") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Indietro") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Selezionare un allarme per aprire significato, soglie documentate e verifiche guidate. I livelli vanno da NOTICE (0) a CRITICAL (4).",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("Tutti") })
                    FilterChip(selected = filter == AlarmSeverity.MAJOR, onClick = { filter = AlarmSeverity.MAJOR }, label = { Text("Major") })
                    FilterChip(selected = filter == AlarmSeverity.CRITICAL, onClick = { filter = AlarmSeverity.CRITICAL }, label = { Text("Critical") })
                }
            }
            items(alarms) { alarm ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onAlarmSelected(alarm.number) }) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("#${alarm.number} ${alarm.label}", style = MaterialTheme.typography.titleMedium)
                        Text("${alarm.severity.label} • Area: ${alarm.diagnosticArea}")
                        Text("Toccare per guida diagnostica", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}
