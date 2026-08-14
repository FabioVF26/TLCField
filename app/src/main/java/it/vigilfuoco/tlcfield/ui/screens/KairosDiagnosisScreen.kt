package it.vigilfuoco.tlcfield.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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

private enum class CheckState { UNKNOWN, OK, FAULT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KairosDiagnosisScreen(onBack: () -> Unit, onAlarms: () -> Unit) {
    var supply by remember { mutableStateOf(CheckState.UNKNOWN) }
    var ethernet by remember { mutableStateOf(CheckState.UNKNOWN) }
    var txPower by remember { mutableStateOf(CheckState.UNKNOWN) }
    var swr by remember { mutableStateOf(CheckState.UNKNOWN) }
    var rx by remember { mutableStateOf(CheckState.UNKNOWN) }
    var sync by remember { mutableStateOf(CheckState.UNKNOWN) }
    var networkRegistration by remember { mutableStateOf(CheckState.UNKNOWN) }

    val result = when {
        supply == CheckState.FAULT -> "Area prioritaria: alimentazione. Verificare gli allarmi Logic Supply Status, Supply Undervoltage e Supply Overvoltage."
        ethernet == CheckState.FAULT -> "Area prioritaria: rete IP/Ethernet. Verificare Ethernet Link Status e la raggiungibilità IP dell'apparato."
        txPower == CheckState.FAULT -> "Area prioritaria: trasmettitore RF. Verificare No TX Power, TX Power too low/high e TX Power Reduction."
        swr == CheckState.FAULT -> "Area prioritaria: sistema radiante. Verificare TX SWR Warning/Alarm e quindi antenna, feeder e connessioni RF."
        rx == CheckState.FAULT -> "Area prioritaria: ricevitore. Verificare RX IFs Status, RXs Status e RF Channel Noise."
        sync == CheckState.FAULT -> "Area prioritaria: sincronizzazione. Verificare GNSS Status, Synchronization Source e Synchronization Status."
        networkRegistration == CheckState.FAULT -> "Area prioritaria: rete DMR/IP. Verificare Registration to Master, Loss of Slave e gli eventuali allarmi SIP."
        listOf(supply, ethernet, txPower, swr, rx, sync, networkRegistration).all { it == CheckState.OK } -> "I controlli principali selezionati risultano regolari. Consultare comunque l'elenco allarmi e lo stato dettagliato del KAIROS."
        else -> "Completare i controlli nell'ordine proposto. La procedura è un supporto diagnostico e non sostituisce il manuale del costruttore."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnosi KAIROS") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Indietro") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Percorso guidato basato sui sottosistemi monitorati dal KAIROS: alimentazione, Ethernet, TX, SWR, RX, sincronizzazione e rete.",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item { CheckCard("1. Alimentazione", "Stato alimentazione e tensioni", supply) { supply = it } }
            item { CheckCard("2. Ethernet / IP", "Link Ethernet e raggiungibilità IP", ethernet) { ethernet = it } }
            item { CheckCard("3. Potenza TX", "Presenza e regolarità della potenza RF", txPower) { txPower = it } }
            item { CheckCard("4. SWR / sistema radiante", "Allarmi di ROS e potenza riflessa", swr) { swr = it } }
            item { CheckCard("5. Ricevitore", "Stato RX, IF e rumore RF", rx) { rx = it } }
            item { CheckCard("6. Sincronizzazione", "GNSS, sorgente e stato sync", sync) { sync = it } }
            item { CheckCard("7. Registrazione rete", "Master/Slave e servizi IP/SIP", networkRegistration) { networkRegistration = it } }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Esito orientativo", style = MaterialTheme.typography.titleMedium)
                        Text(result, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
            item {
                Button(onClick = onAlarms, modifier = Modifier.fillMaxWidth()) { Text("CONSULTA ALLARMI KAIROS") }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Avvertenza restart", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Text("Non eseguire shutdown remoto come tentativo diagnostico: la documentazione indica che, dopo lo shutdown, l'apparato può richiedere la riaccensione fisica sul posto.")
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckCard(title: String, subtitle: String, state: CheckState, onChange: (CheckState) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onChange(CheckState.OK) }, enabled = state != CheckState.OK, modifier = Modifier.weight(1f)) { Text("OK") }
                Button(onClick = { onChange(CheckState.FAULT) }, enabled = state != CheckState.FAULT, modifier = Modifier.weight(1f)) { Text("ANOMALIA") }
            }
        }
    }
}
