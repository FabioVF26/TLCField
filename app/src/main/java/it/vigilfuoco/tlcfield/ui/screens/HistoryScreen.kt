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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import it.vigilfuoco.tlcfield.data.AdminAccessRepository
import it.vigilfuoco.tlcfield.data.Intervention
import it.vigilfuoco.tlcfield.data.InterventionRepository
import it.vigilfuoco.tlcfield.data.PdfReportGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class SiteHistoryFolder(
    val siteId: String,
    val siteName: String,
    val interventions: List<Intervention>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val context = LocalContext.current
    var refreshVersion by remember { mutableIntStateOf(0) }
    val interventions = remember(refreshVersion) {
        InterventionRepository.getAll(context)
    }
    val formatter = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY)
    }
    var selectedSiteId by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<Intervention?>(null) }
    var adminPin by remember { mutableStateOf("") }
    var deleteError by remember { mutableStateOf("") }

    val folders = interventions
        .groupBy { it.siteId }
        .map { (siteId, siteInterventions) ->
            SiteHistoryFolder(
                siteId = siteId,
                siteName = siteInterventions.firstOrNull()?.siteName ?: siteId,
                interventions = siteInterventions.sortedByDescending { it.timestamp }
            )
        }
        .sortedBy { it.siteName.lowercase(Locale.ITALY) }

    val selectedFolder = folders.firstOrNull { it.siteId == selectedSiteId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedFolder == null) {
                            "Storico interventi — Siti"
                        } else {
                            selectedFolder.siteName
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedFolder != null) {
                                selectedSiteId = null
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        when {
            interventions.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Nessun intervento salvato.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Quando verrà registrato il primo intervento, lo storico creerà automaticamente la cartella del relativo sito."
                    )
                }
            }

            selectedFolder == null -> {
                SiteFolderList(
                    folders = folders,
                    formatter = formatter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onFolderSelected = { selectedSiteId = it }
                )
            }

            else -> {
                SiteInterventionList(
                    folder = selectedFolder,
                    formatter = formatter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onGeneratePdf = { intervention ->
                        val pdf = PdfReportGenerator.generate(context, intervention)
                        PdfReportGenerator.share(context, pdf)
                    },
                    onEdit = { intervention ->
                        onEdit(intervention.id)
                    },
                    onDelete = { intervention ->
                        pendingDelete = intervention
                        adminPin = ""
                        deleteError = ""
                    }
                )
            }
        }
    }

    pendingDelete?.let { intervention ->
        val adminConfigured = AdminAccessRepository.isConfigured(context)

        AlertDialog(
            onDismissRequest = {
                pendingDelete = null
                adminPin = ""
                deleteError = ""
            },
            title = {
                Text("Eliminazione riservata all'amministratore")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Rapporto: ${intervention.siteName} — ${formatter.format(Date(intervention.timestamp))}"
                    )

                    if (!adminConfigured) {
                        Text(
                            "Il PIN amministratore non è ancora configurato. Configurarlo nella sezione Server / Sincronizzazione prima di eliminare rapporti.",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        OutlinedTextField(
                            value = adminPin,
                            onValueChange = { adminPin = it.filter(Char::isDigit) },
                            label = { Text("PIN amministratore") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (deleteError.isNotBlank()) {
                        Text(
                            deleteError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        "La cancellazione locale viene registrata e sarà propagata al server alla successiva sincronizzazione.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = adminConfigured && adminPin.isNotBlank(),
                    onClick = {
                        if (!AdminAccessRepository.verifyPin(context, adminPin)) {
                            deleteError = "PIN amministratore non corretto"
                            return@Button
                        }

                        if (InterventionRepository.delete(context, intervention.id)) {
                            pendingDelete = null
                            adminPin = ""
                            deleteError = ""
                            refreshVersion++
                            if (InterventionRepository.getAll(context).none { it.siteId == selectedSiteId }) {
                                selectedSiteId = null
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("ELIMINA", modifier = Modifier.padding(start = 6.dp))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        adminPin = ""
                        deleteError = ""
                    }
                ) {
                    Text("ANNULLA")
                }
            }
        )
    }
}

@Composable
private fun SiteFolderList(
    folders: List<SiteHistoryFolder>,
    formatter: SimpleDateFormat,
    modifier: Modifier = Modifier,
    onFolderSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Rapporti organizzati per sito",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
            Text(
                "Aprire una cartella per visualizzare esclusivamente gli interventi effettuati su quel ponte radio.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        items(folders, key = { it.siteId }) { folder ->
            val latest = folder.interventions.maxByOrNull { it.timestamp }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFolderSelected(folder.siteId) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            folder.siteName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (folder.interventions.size == 1) {
                                "1 rapporto di intervento"
                            } else {
                                "${folder.interventions.size} rapporti di intervento"
                            }
                        )
                        latest?.let {
                            Text(
                                "Ultimo: ${formatter.format(Date(it.timestamp))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Icon(Icons.Default.FolderOpen, contentDescription = "Apri cartella")
                }
            }
        }

        item { Column(modifier = Modifier.padding(bottom = 12.dp)) {} }
    }
}

@Composable
private fun SiteInterventionList(
    folder: SiteHistoryFolder,
    formatter: SimpleDateFormat,
    modifier: Modifier = Modifier,
    onGeneratePdf: (Intervention) -> Unit,
    onEdit: (Intervention) -> Unit,
    onDelete: (Intervention) -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Column {
                    Text("Cartella sito", style = MaterialTheme.typography.labelLarge)
                    Text(
                        if (folder.interventions.size == 1) {
                            "1 rapporto archiviato"
                        } else {
                            "${folder.interventions.size} rapporti archiviati"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        items(folder.interventions, key = { it.id }) { intervention ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        formatter.format(Date(intervention.timestamp)),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Tipo: ${intervention.type}")
                    Text("Esito: ${intervention.result}")

                    if (intervention.lastModified > intervention.timestamp) {
                        Text(
                            "Ultima modifica: ${formatter.format(Date(intervention.lastModified))}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (intervention.personnel.isNotEmpty()) {
                        Text(
                            "Personale: " + intervention.personnel.joinToString(", ") { person ->
                                listOf(person.qualification, person.fullName)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" ")
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (intervention.vehicles.isNotEmpty()) {
                        Text(
                            "Mezzi: " + intervention.vehicles.joinToString(", ") { vehicle ->
                                "${vehicle.description} (${vehicle.plate})"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (intervention.kairosAlarmNumbers.isNotEmpty()) {
                        Text(
                            "Diagnosi KAIROS: ${intervention.kairosAlarmNumbers.size} allarmi, ${intervention.kairosCompletedChecks.size} verifiche registrate"
                        )
                    }

                    if (intervention.reportedProblem.isNotBlank()) {
                        Text("Segnalazione: ${intervention.reportedProblem}")
                    }

                    intervention.measurements
                        .filter { it.measuredRssi != null }
                        .forEach { measurement ->
                            val delta = measurement.deltaDb?.let {
                                " (Δ ${if (it > 0) "+" else ""}$it dB)"
                            } ?: ""
                            Text(
                                "${measurement.linkName}: ${measurement.measuredRssi} dBm$delta",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                    if (intervention.photos.isNotEmpty()) {
                        Text(
                            "Fotografie: ${intervention.photos.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        OutlinedButton(onClick = { onEdit(intervention) }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Text("MODIFICA", modifier = Modifier.padding(start = 6.dp))
                        }

                        Button(onClick = { onGeneratePdf(intervention) }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Text("PDF", modifier = Modifier.padding(start = 6.dp))
                        }

                        OutlinedButton(onClick = { onDelete(intervention) }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Text("ELIMINA", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }
        }

        item { Column(modifier = Modifier.padding(bottom = 12.dp)) {} }
    }
}
