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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import it.vigilfuoco.tlcfield.data.AdminAccessRepository
import it.vigilfuoco.tlcfield.data.ServerApi
import it.vigilfuoco.tlcfield.data.ServerSettingsRepository
import it.vigilfuoco.tlcfield.data.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSyncScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val initial = remember { ServerSettingsRepository.load(context) }
    var url by remember { mutableStateOf(initial.baseUrl) }
    var token by remember { mutableStateOf(initial.apiToken) }
    var status by remember { mutableStateOf("Non connesso") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var adminConfigured by remember {
        mutableStateOf(AdminAccessRepository.isConfigured(context))
    }
    var currentAdminPin by remember { mutableStateOf("") }
    var newAdminPin by remember { mutableStateOf("") }
    var confirmAdminPin by remember { mutableStateOf("") }
    var adminStatus by remember { mutableStateOf("") }

    fun save() = ServerSettingsRepository.save(context, url, token)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server e sincronizzazione") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Database centrale", style = MaterialTheme.typography.titleLarge)
            Text(
                "L'app continua a salvare sul telefono. Quando il server è raggiungibile, gli interventi vengono sincronizzati con il database centrale."
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL server") },
                placeholder = { Text("https://tlc.example.it") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Token API") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Button(
                onClick = {
                    save()
                    status = "Configurazione salvata"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SALVA CONFIGURAZIONE")
            }

            OutlinedButton(
                enabled = !busy,
                onClick = {
                    save()
                    busy = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            ServerApi.health(ServerSettingsRepository.load(context))
                        }
                        status = result.message
                        busy = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("PROVA CONNESSIONE")
            }

            Button(
                enabled = !busy,
                onClick = {
                    save()
                    busy = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            SyncRepository.sync(context)
                        }
                        status = "${result.message} — inviati ${result.uploaded}, ricevuti ${result.downloaded}"
                        busy = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (busy) "SINCRONIZZAZIONE..." else "SINCRONIZZA ORA")
            }

            HorizontalDivider()
            Text("Stato: $status", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Per prove su rete locale è possibile usare un indirizzo HTTP. In esercizio è raccomandato HTTPS con certificato valido e accesso protetto.",
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()

            Text(
                "Amministrazione rapporti",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                if (adminConfigured) {
                    "PIN amministratore configurato. Il PIN è richiesto per eliminare un rapporto dallo storico."
                } else {
                    "Configurare un PIN amministratore. Gli operatori possono modificare i rapporti, mentre la cancellazione è protetta dal PIN."
                },
                style = MaterialTheme.typography.bodyMedium
            )

            if (adminConfigured) {
                OutlinedTextField(
                    value = currentAdminPin,
                    onValueChange = { currentAdminPin = it.filter(Char::isDigit) },
                    label = { Text("PIN amministratore attuale") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = newAdminPin,
                onValueChange = { newAdminPin = it.filter(Char::isDigit) },
                label = {
                    Text(if (adminConfigured) "Nuovo PIN amministratore" else "PIN amministratore")
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            OutlinedTextField(
                value = confirmAdminPin,
                onValueChange = { confirmAdminPin = it.filter(Char::isDigit) },
                label = { Text("Conferma nuovo PIN") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            OutlinedButton(
                onClick = {
                    val result = AdminAccessRepository.configure(
                        context = context,
                        currentPin = currentAdminPin,
                        newPin = newAdminPin,
                        confirmPin = confirmAdminPin
                    )
                    adminStatus = result.message
                    if (result.ok) {
                        adminConfigured = true
                        currentAdminPin = ""
                        newAdminPin = ""
                        confirmAdminPin = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (adminConfigured) "CAMBIA PIN ADMIN" else "CONFIGURA PIN ADMIN")
            }

            if (adminStatus.isNotBlank()) {
                Text(
                    adminStatus,
                    color = if (adminStatus.contains("configurato", ignoreCase = true)) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
