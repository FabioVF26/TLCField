package it.vigilfuoco.tlcfield.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import it.vigilfuoco.tlcfield.BuildConfig
import it.vigilfuoco.tlcfield.data.BugReport
import it.vigilfuoco.tlcfield.data.BugReportRepository
import it.vigilfuoco.tlcfield.data.Personnel
import it.vigilfuoco.tlcfield.data.PersonnelRepository
import it.vigilfuoco.tlcfield.data.ServerApi
import it.vigilfuoco.tlcfield.data.ServerSettingsRepository
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val personnel = remember { PersonnelRepository.getAll() }

    var selectedPerson by remember { mutableStateOf<Personnel?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Segnala BUG") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Funzione sperimentale per raccogliere anomalie e problemi riscontrati durante l'uso dell'app.",
                style = MaterialTheme.typography.bodyMedium
            )

            if (personnel.isEmpty()) {
                Text(
                    "Elenco personale non disponibile. Eseguire prima una sincronizzazione con il server.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedPerson?.let {
                            listOf(it.qualification, it.fullName)
                                .filter { value -> value.isNotBlank() }
                                .joinToString(" - ")
                        }.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Operatore") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        personnel.forEach { person ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        listOf(person.qualification, person.fullName)
                                            .filter { it.isNotBlank() }
                                            .joinToString(" - ")
                                    )
                                },
                                onClick = {
                                    selectedPerson = person
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Descrizione del bug") },
                placeholder = {
                    Text("Descrivere cosa è successo, in quale schermata e cosa ci si aspettava.")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6
            )

            Button(
                enabled = !busy && selectedPerson != null && notes.isNotBlank(),
                onClick = {
                    val person = selectedPerson ?: return@Button
                    val report = BugReport(
                        id = UUID.randomUUID().toString(),
                        personnelId = person.id,
                        qualification = person.qualification,
                        fullName = person.fullName,
                        notes = notes.trim(),
                        timestamp = System.currentTimeMillis(),
                        appVersion = BuildConfig.VERSION_NAME,
                        synced = false
                    )

                    BugReportRepository.save(context, report)
                    busy = true
                    status = "Segnalazione salvata sul dispositivo. Invio al server..."

                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            val settings = ServerSettingsRepository.load(context)
                            if (settings.baseUrl.isBlank()) {
                                ServerApi.Result(false, "Server non configurato")
                            } else {
                                ServerApi.uploadBugReport(settings, report)
                            }
                        }

                        if (result.ok) {
                            BugReportRepository.markSynced(context, report.id)
                            status = "BUG salvato e inviato al server."
                        } else {
                            status = "BUG salvato sul dispositivo. Sarà ritentato alla prossima sincronizzazione."
                        }

                        notes = ""
                        busy = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (busy) "SALVATAGGIO..." else "SALVA SEGNALAZIONE")
            }

            if (status.isNotBlank()) {
                Text(
                    status,
                    color = if (status.startsWith("BUG salvato e inviato")) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Text(
                "Vengono registrati automaticamente data/ora e versione dell'app (${BuildConfig.VERSION_NAME}).",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
