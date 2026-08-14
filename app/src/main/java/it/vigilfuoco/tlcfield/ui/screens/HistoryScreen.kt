package it.vigilfuoco.tlcfield.ui.screens

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
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import it.vigilfuoco.tlcfield.data.InterventionRepository
import it.vigilfuoco.tlcfield.data.PdfReportGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val interventions = InterventionRepository.getAll(context)
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY)

    Scaffold(topBar = {
        TopAppBar(title = { Text("Storico interventi") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Indietro") }
        })
    }) { padding ->
        if (interventions.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                Text("Nessun intervento salvato.", style = MaterialTheme.typography.titleMedium)
                Text("Gli interventi registrati sul dispositivo compariranno in questa sezione.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(interventions, key = { it.id }) { intervention ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(intervention.siteName, style = MaterialTheme.typography.titleMedium)
                            Text(formatter.format(Date(intervention.timestamp)))
                            Text("Tipo: ${intervention.type}")
                            Text("Esito: ${intervention.result}")
                            if (intervention.reportedProblem.isNotBlank()) Text("Segnalazione: ${intervention.reportedProblem}")
                            intervention.measurements.filter { it.measuredRssi != null }.forEach { m ->
                                val delta = m.deltaDb?.let { " (Δ ${if (it > 0) "+" else ""}$it dB)" } ?: ""
                                Text("${m.linkName}: ${m.measuredRssi} dBm$delta", style = MaterialTheme.typography.bodySmall)
                            }
                            if (intervention.photos.isNotEmpty()) Text("Fotografie: ${intervention.photos.size}", style = MaterialTheme.typography.bodySmall)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Button(onClick = {
                                    val pdf = PdfReportGenerator.generate(context, intervention)
                                    PdfReportGenerator.share(context, pdf)
                                }) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                    Text("RAPPORTO PDF", modifier = Modifier.padding(start = 6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
