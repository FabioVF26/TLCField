package it.vigilfuoco.tlcfield.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
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

    fun save() = ServerSettingsRepository.save(context, url, token)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server e sincronizzazione") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Database centrale", style = MaterialTheme.typography.titleLarge)
            Text("L'app continua a salvare sul telefono. Quando il server è raggiungibile, gli interventi vengono sincronizzati con il database centrale.")

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

            Button(onClick = { save(); status = "Configurazione salvata" }, modifier = Modifier.fillMaxWidth()) {
                Text("SALVA CONFIGURAZIONE")
            }

            OutlinedButton(
                enabled = !busy,
                onClick = {
                    save(); busy = true
                    scope.launch {
                        val r = withContext(Dispatchers.IO) { ServerApi.health(ServerSettingsRepository.load(context)) }
                        status = r.message
                        busy = false
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) { Text("PROVA CONNESSIONE") }

            Button(
                enabled = !busy,
                onClick = {
                    save(); busy = true
                    scope.launch {
                        val r = withContext(Dispatchers.IO) { SyncRepository.sync(context) }
                        status = "${r.message} — inviati ${r.uploaded}, ricevuti ${r.downloaded}"
                        busy = false
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) { Text(if (busy) "SINCRONIZZAZIONE..." else "SINCRONIZZA ORA") }

            HorizontalDivider()
            Text("Stato: $status", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Per prove su rete locale è possibile usare un indirizzo HTTP. In esercizio è raccomandato HTTPS con certificato valido e accesso protetto.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
