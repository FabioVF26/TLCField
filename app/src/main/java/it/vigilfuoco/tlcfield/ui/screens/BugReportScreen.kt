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
import androidx.compose.runtime.LaunchedEffect
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
import it.vigilfuoco.tlcfield.data.PersonnelVehicleCacheRepository
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

    var personnel by remember {
        mutableStateOf(PersonnelRepository.getAll())
    }
    var loadingPersonnel by remember { mutableStateOf(false) }
    var personnelStatus by remember { mutableStateOf("") }

    var selectedPerson by remember { mutableStateOf<Personnel?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    fun refreshPersonnel() {
        if (loadingPersonnel) return

        loadingPersonnel = true
        personnelStatus = "Caricamento elenco personale..."

        scope.launch {
            // 1) Memoria applicativa
            var loaded = PersonnelRepository.getAll()

            // 2) Cache persistente locale
            if (loaded.isEmpty()) {
                val cached = withContext(Dispatchers.IO) {
                    PersonnelVehicleCacheRepository.loadPersonnel(context)
                }
                if (cached.isNotEmpty()) {
                    PersonnelRepository.updateFromServer(cached)
                    loaded = PersonnelRepository.getAll()
                }
            }

            // 3) Server, se necessario e configurato
            if (loaded.isEmpty()) {
                val settings = ServerSettingsRepository.load(context)

                if (settings.baseUrl.isNotBlank()) {
                    val (result, remotePersonnel) = withContext(Dispatchers.IO) {
                        ServerApi.downloadPersonnel(settings)
                    }

                    if (result.ok && remotePersonnel.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            PersonnelVehicleCacheRepository.savePersonnel(
                                context,
                                remotePersonnel
                            )
                        }
                        PersonnelRepository.updateFromServer(remotePersonnel)
                        loaded = PersonnelRepository.getAll()
                        personnelStatus = ""
                    } else {
                        personnelStatus = if (!result.ok) {
                            "Impossibile scaricare il personale dal server: ${result.message}."
                        } else {
                            "Il server ha restituito un elenco personale vuoto."
                        }
                    }
                } else {
                    // 4) Fallback locale: la funzione BUG deve poter essere
                    // usata anche durante prove completamente offline.
                    val fallback = PersonnelRepository.fallbackPersonnel()
                    PersonnelRepository.updateFromServer(fallback)
                    loaded = PersonnelRepository.getAll()
                    personnelStatus = "Modalità offline: elenco personale locale. Le segnalazioni saranno inviate alla prossima sincronizzazione."
                }
            } else {
                personnelStatus = ""
            }

            // Se il server è configurato ma non raggiungibile / restituisce
            // un elenco vuoto, consenti comunque la segnalazione con il
            // fallback locale.
            if (loaded.isEmpty()) {
                val fallback = PersonnelRepository.fallbackPersonnel()
                PersonnelRepository.updateFromServer(fallback)
                loaded = PersonnelRepository.getAll()
                personnelStatus = "Modalità offline: elenco personale locale. Le segnalazioni saranno inviate alla prossima sincronizzazione."
            }

            personnel = loaded

            // Se il nominativo selezionato non è più nell'elenco, azzeralo.
            if (selectedPerson != null && personnel.none { it.id == selectedPerson?.id }) {
                selectedPerson = null
            }

            loadingPersonnel = false
        }
    }

    LaunchedEffect(Unit) {
        refreshPersonnel()
    }

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
                    when {
                        loadingPersonnel -> "Caricamento elenco personale..."
                        personnelStatus.isNotBlank() -> personnelStatus
                        else -> "Elenco personale non disponibile."
                    },
                    color = if (loadingPersonnel) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )

                Button(
                    onClick = { refreshPersonnel() },
                    enabled = !loadingPersonnel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (loadingPersonnel) "CARICAMENTO..." else "RICARICA ELENCO PERSONALE")
                }
            } else {
                if (personnelStatus.isNotBlank()) {
                    Text(
                        personnelStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

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

                    // Il salvataggio locale avviene sempre prima del tentativo di invio.
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

            if (selectedPerson == null && notes.isNotBlank() && !loadingPersonnel) {
                Text(
                    "Per salvare la segnalazione è necessario selezionare l'operatore.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
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
