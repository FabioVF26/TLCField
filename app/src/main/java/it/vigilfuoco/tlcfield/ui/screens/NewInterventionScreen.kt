package it.vigilfuoco.tlcfield.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.vigilfuoco.tlcfield.data.Intervention
import it.vigilfuoco.tlcfield.data.InterventionPhoto
import it.vigilfuoco.tlcfield.data.InterventionRepository
import it.vigilfuoco.tlcfield.data.KairosRepository
import it.vigilfuoco.tlcfield.data.KairosSnapshot
import it.vigilfuoco.tlcfield.data.PdfReportGenerator
import it.vigilfuoco.tlcfield.data.PhotoStorage
import it.vigilfuoco.tlcfield.data.RssiMeasurement
import it.vigilfuoco.tlcfield.data.Site
import it.vigilfuoco.tlcfield.data.SiteRepository
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInterventionScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    initialSiteId: String? = null
) {
    val context = LocalContext.current
    val sites = SiteRepository.sites
    var selectedSite by remember(initialSiteId) {
        mutableStateOf(initialSiteId?.let { SiteRepository.getSite(it) })
    }
    var siteMenuOpen by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("Guasto") }
    var typeMenuOpen by remember { mutableStateOf(false) }
    var reportedProblem by remember { mutableStateOf("") }
    var initialState by remember { mutableStateOf("Fuori servizio") }
    var initialMenuOpen by remember { mutableStateOf(false) }
    var powerOk by remember { mutableStateOf(true) }
    var radioOn by remember { mutableStateOf(true) }
    var alarms by remember { mutableStateOf(false) }
    var ipLinkOk by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Impianto ripristinato") }
    var resultMenuOpen by remember { mutableStateOf(false) }
    val measuredRssi = remember { mutableStateMapOf<String, String>() }
    val photos = remember { mutableStateListOf<InterventionPhoto>() }
    var photoCategory by remember { mutableStateOf("Anomalia") }
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var pendingPhoto by remember { mutableStateOf<Pair<File, android.net.Uri>?>(null) }
    var showSaved by remember { mutableStateOf(false) }
    var savedIntervention by remember { mutableStateOf<Intervention?>(null) }

    val selectedKairosAlarms = remember { mutableStateListOf<Int>() }
    val completedKairosChecks = remember { mutableStateMapOf<String, Boolean>() }
    var kairosAlarmMenuOpen by remember { mutableStateOf(false) }
    var pendingKairosAlarm by remember { mutableStateOf(KairosRepository.alarms.first().number) }
    var kairosDiagnosticNotes by remember { mutableStateOf("") }
    var kairosSupplyVoltage by remember { mutableStateOf("") }
    var kairosTxTemperature by remember { mutableStateOf("") }
    var kairosForwardPower by remember { mutableStateOf("") }
    var kairosReflectedPower by remember { mutableStateOf("") }
    var kairosRssiMain by remember { mutableStateOf("") }
    var kairosRssiDiversity by remember { mutableStateOf("") }
    var kairosSyncSource by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val pending = pendingPhoto
        if (success && pending != null) {
            photos += InterventionPhoto(pending.first.absolutePath, photoCategory)
        } else pending?.first?.delete()
        pendingPhoto = null
    }

    fun buildIntervention(): Intervention? {
        val site = selectedSite ?: return null
        val measurements = site.links.map { link ->
            val key = link.name + "|" + link.type
            RssiMeasurement(link.name, link.rssiDbm, measuredRssi[key]?.toIntOrNull())
        }
        return Intervention(
            id = InterventionRepository.newId(),
            siteId = site.id,
            siteName = site.name,
            timestamp = System.currentTimeMillis(),
            type = type,
            reportedProblem = reportedProblem.trim(),
            initialState = initialState,
            powerOk = powerOk,
            radioOn = radioOn,
            alarms = alarms,
            ipLinkOk = ipLinkOk,
            notes = notes.trim(),
            result = result,
            measurements = measurements,
            photos = photos.toList(),
            kairosAlarmNumbers = selectedKairosAlarms.toList(),
            kairosCompletedChecks = completedKairosChecks.filterValues { it }.keys.toList(),
            kairosDiagnosticNotes = kairosDiagnosticNotes.trim(),
            kairosSnapshot = if (site.kairosEndpoints.isNotEmpty() || selectedKairosAlarms.isNotEmpty()) {
                KairosSnapshot(
                    supplyVoltageV = kairosSupplyVoltage.replace(',', '.').toDoubleOrNull(),
                    txTemperatureC = kairosTxTemperature.replace(',', '.').toDoubleOrNull(),
                    forwardPowerW = kairosForwardPower.replace(',', '.').toDoubleOrNull(),
                    reflectedPowerW = kairosReflectedPower.replace(',', '.').toDoubleOrNull(),
                    rssiMainDbm = kairosRssiMain.toIntOrNull(),
                    rssiDiversityDbm = kairosRssiDiversity.toIntOrNull(),
                    synchronizationSource = kairosSyncSource.trim()
                )
            } else null
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuovo intervento") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Indietro") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SectionTitle("Identificazione") }
            item {
                ExposedDropdownMenuBox(expanded = siteMenuOpen, onExpandedChange = { siteMenuOpen = !siteMenuOpen }) {
                    OutlinedTextField(
                        value = selectedSite?.name ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Sito / ponte radio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(siteMenuOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = siteMenuOpen, onDismissRequest = { siteMenuOpen = false }) {
                        sites.forEach { site ->
                            DropdownMenuItem(
                                text = { Text(site.name + (site.code?.let { " — $it" } ?: "")) },
                                onClick = { selectedSite = site; measuredRssi.clear(); selectedKairosAlarms.clear(); completedKairosChecks.clear(); kairosDiagnosticNotes = ""; siteMenuOpen = false }
                            )
                        }
                    }
                }
            }
            item {
                SimpleDropdown("Tipologia intervento", type,
                    listOf("Guasto", "Manutenzione programmata", "Verifica funzionale", "Installazione", "Sostituzione apparato", "Collaudo", "Sopralluogo", "Altro"),
                    typeMenuOpen, { typeMenuOpen = it }, { type = it; typeMenuOpen = false })
            }
            item {
                OutlinedTextField(reportedProblem, { reportedProblem = it }, label = { Text("Problema / motivo dell'intervento") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
            item {
                SimpleDropdown("Stato impianto all'arrivo", initialState,
                    listOf("Operativo", "Parzialmente operativo", "Fuori servizio", "Stato non noto"),
                    initialMenuOpen, { initialMenuOpen = it }, { initialState = it; initialMenuOpen = false })
            }

            selectedSite?.let { site ->
                item { SectionTitle("Valori nominali e misure RSSI") }
                item { Text("Inserire il valore rilevato sul posto. Lo scostamento viene calcolato rispetto all'RSSI di riferimento presente nella scheda del sito.", style = MaterialTheme.typography.bodySmall) }
                items(site.links, key = { it.name + it.type }) { link ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(link.name, style = MaterialTheme.typography.titleMedium)
                            Text(link.type)
                            Text("RSSI riferimento: ${link.rssiDbm?.let { "$it dBm" } ?: "non disponibile"}")
                            if (link.txMhz != null || link.rxMhz != null) Text("TX: ${link.txMhz ?: "—"} MHz   RX: ${link.rxMhz ?: "—"} MHz")
                            val key = link.name + "|" + link.type
                            OutlinedTextField(
                                value = measuredRssi[key].orEmpty(),
                                onValueChange = { measuredRssi[key] = it.filter { c -> c == '-' || c.isDigit() }.take(4) },
                                label = { Text("RSSI misurato (dBm)") }, modifier = Modifier.fillMaxWidth()
                            )
                            val measured = measuredRssi[key]?.toIntOrNull()
                            val delta = if (measured != null && link.rssiDbm != null) measured - link.rssiDbm else null
                            if (delta != null) Text(
                                "Scostamento: ${if (delta > 0) "+" else ""}$delta dB",
                                fontWeight = FontWeight.Bold,
                                color = when { delta <= -10 -> MaterialTheme.colorScheme.error; delta <= -6 -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.primary }
                            )
                        }
                    }
                }
            }

            item { SectionTitle("Verifiche tecniche") }
            item { CheckRow("Alimentazione regolare", powerOk) { powerOk = it } }
            item { CheckRow("Apparato radio acceso", radioOn) { radioOn = it } }
            item { CheckRow("Allarmi presenti", alarms) { alarms = it } }
            item { CheckRow("Collegamento dati / IP operativo", ipLinkOk) { ipLinkOk = it } }

            selectedSite?.takeIf { it.kairosEndpoints.isNotEmpty() }?.let { site ->
                item { SectionTitle("Diagnosi KAIROS") }
                item {
                    Text(
                        "Registrare i parametri letti dall'apparato e gli allarmi effettivamente presenti. Le verifiche selezionate saranno riportate nel rapporto PDF.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Parametri KAIROS", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(kairosSupplyVoltage, { kairosSupplyVoltage = it }, label = { Text("Input Supply Voltage (V)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(kairosTxTemperature, { kairosTxTemperature = it }, label = { Text("TX Temperature (°C)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(kairosForwardPower, { kairosForwardPower = it }, label = { Text("Forward Power (W)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(kairosReflectedPower, { kairosReflectedPower = it }, label = { Text("Reflected Power (W)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(kairosRssiMain, { kairosRssiMain = it.filter { c -> c == '-' || c.isDigit() }.take(4) }, label = { Text("RSSI Main (dBm)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(kairosRssiDiversity, { kairosRssiDiversity = it.filter { c -> c == '-' || c.isDigit() }.take(4) }, label = { Text("RSSI Diversity (dBm)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(kairosSyncSource, { kairosSyncSource = it }, label = { Text("Sorgente di sincronizzazione / Lock") }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                item {
                    ExposedDropdownMenuBox(expanded = kairosAlarmMenuOpen, onExpandedChange = { kairosAlarmMenuOpen = !kairosAlarmMenuOpen }) {
                        val current = KairosRepository.alarm(pendingKairosAlarm)
                        OutlinedTextField(
                            value = current?.let { "${it.number} — ${it.label}" } ?: "",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Allarme KAIROS") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(kairosAlarmMenuOpen) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = kairosAlarmMenuOpen, onDismissRequest = { kairosAlarmMenuOpen = false }) {
                            KairosRepository.alarms.forEach { alarm ->
                                DropdownMenuItem(
                                    text = { Text("${alarm.number} — ${alarm.label} [${alarm.severity.label}]") },
                                    onClick = { pendingKairosAlarm = alarm.number; kairosAlarmMenuOpen = false }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { if (pendingKairosAlarm !in selectedKairosAlarms) selectedKairosAlarms += pendingKairosAlarm },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("AGGIUNGI ALLARME ALL'INTERVENTO") }
                }
                items(selectedKairosAlarms, key = { it }) { alarmNumber ->
                    val alarm = KairosRepository.alarm(alarmNumber)
                    val guide = KairosRepository.guide(alarmNumber)
                    if (alarm != null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${alarm.number} — ${alarm.label}", style = MaterialTheme.typography.titleMedium)
                                        Text("${alarm.severity.label} · ${alarm.diagnosticArea}", fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(onClick = {
                                        selectedKairosAlarms.remove(alarmNumber)
                                        guide.checks.indices.forEach { completedKairosChecks.remove("$alarmNumber|$it") }
                                    }) { Icon(Icons.Default.Delete, contentDescription = "Rimuovi allarme") }
                                }
                                Text(guide.meaning, style = MaterialTheme.typography.bodySmall)
                                if (guide.values.isNotEmpty()) {
                                    guide.values.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) }
                                }
                                Text("Verifiche eseguite", fontWeight = FontWeight.Bold)
                                guide.checks.forEachIndexed { index, check ->
                                    val key = "$alarmNumber|$index"
                                    CheckRow(check, completedKairosChecks[key] == true) { completedKairosChecks[key] = it }
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        kairosDiagnosticNotes,
                        { kairosDiagnosticNotes = it },
                        label = { Text("Note diagnosi KAIROS") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item { SectionTitle("Documentazione fotografica") }
            item {
                SimpleDropdown("Categoria foto", photoCategory,
                    listOf("Apparato", "Antenna", "Feeder", "Connettore", "Alimentazione", "Batteria", "Quadro", "Anomalia", "Riparazione effettuata", "Altro"),
                    categoryMenuOpen, { categoryMenuOpen = it }, { photoCategory = it; categoryMenuOpen = false })
            }
            item {
                OutlinedButton(
                    onClick = {
                        val pair = PhotoStorage.createPhoto(context)
                        pendingPhoto = pair
                        cameraLauncher.launch(pair.second)
                    }, modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Text("SCATTA FOTO", modifier = Modifier.padding(start = 8.dp))
                }
            }
            items(photos, key = { it.path }) { photo ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val bitmap = remember(photo.path) { BitmapFactory.decodeFile(photo.path)?.asImageBitmap() }
                        bitmap?.let {
                            Image(it, contentDescription = photo.category, modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(photo.category, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                            IconButton(onClick = { PhotoStorage.delete(photo.path); photos.remove(photo) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina foto")
                            }
                        }
                    }
                }
            }

            item { SectionTitle("Chiusura intervento") }
            item { OutlinedTextField(notes, { notes = it }, label = { Text("Anomalia individuata / operazioni effettuate / note") }, minLines = 5, modifier = Modifier.fillMaxWidth()) }
            item {
                SimpleDropdown("Esito", result,
                    listOf("Impianto ripristinato", "Impianto operativo con riserva", "Impianto non ripristinato"),
                    resultMenuOpen, { resultMenuOpen = it }, { result = it; resultMenuOpen = false })
            }
            item {
                Button(enabled = selectedSite != null, onClick = {
                    val intervention = buildIntervention() ?: return@Button
                    InterventionRepository.save(context, intervention)
                    savedIntervention = intervention
                    showSaved = true
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Text("SALVA INTERVENTO", modifier = Modifier.padding(start = 10.dp))
                }
            }
            item { Text("Le fotografie vengono archiviate localmente e inserite nel rapporto PDF.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 20.dp)) }
        }
    }

    if (showSaved) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Intervento salvato") },
            text = { Text("L'intervento è nello storico. È possibile generare subito il rapporto PDF completo di misure e fotografie.") },
            confirmButton = {
                Button(onClick = {
                    savedIntervention?.let { PdfReportGenerator.share(context, PdfReportGenerator.generate(context, it)) }
                }) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Text("GENERA PDF", modifier = Modifier.padding(start = 6.dp))
                }
            },
            dismissButton = { OutlinedButton(onClick = { showSaved = false; onSaved() }) { Text("APRI STORICO") } }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, modifier = Modifier.weight(1f).padding(top = 12.dp))
            Checkbox(checked = checked, onCheckedChange = onChecked)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdown(label: String, value: String, values: List<String>, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, onSelected: (String) -> Unit) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(value = value, onValueChange = {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            values.forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { onSelected(item) }) }
        }
    }
}
