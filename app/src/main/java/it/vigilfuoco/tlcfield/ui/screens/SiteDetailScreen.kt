package it.vigilfuoco.tlcfield.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import it.vigilfuoco.tlcfield.data.RadioLink
import it.vigilfuoco.tlcfield.data.SiteRepository
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDetailScreen(siteId: String, onBack: () -> Unit) {
    val site = SiteRepository.getSite(siteId)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(site?.name ?: "Sito") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Indietro") } }
            )
        }
    ) { padding ->
        if (site == null) {
            Text("Sito non trovato", modifier = Modifier.padding(padding).padding(20.dp))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(site.name, style = MaterialTheme.typography.headlineSmall)
                site.code?.let { Text("Codice TLC: $it") }
                site.network?.let { Text(it) }
                site.owner?.let { Text("Sito: $it") }
                site.altitudeM?.let { Text("Quota: $it m") }
                if (site.latitude != null && site.longitude != null) Text("Coordinate: ${site.latitude}, ${site.longitude}")
            }

            item {
                if (site.navigationVerified) {
                    Button(
                        onClick = {
                            val lat = site.latitude; val lon = site.longitude
                            if (lat != null && lon != null) openNavigation(context, lat, lon, site.name)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Navigation, null)
                        Text("NAVIGA AL SITO", modifier = Modifier.padding(start = 10.dp))
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Navigazione non abilitata", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                            Text("Le coordinate della scheda devono essere verificate prima di essere usate operativamente.")
                        }
                    }
                }
            }

            site.rackLocation?.let { item { InfoCard("Posizione apparati", it) } }
            site.accessNotes?.let { item { InfoCard("Accesso al sito", it) } }
            site.technicalNotes?.let { item { InfoCard("Note tecniche", it) } }

            if (site.kairosEndpoints.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("KAIROS — Web Interface", style = MaterialTheme.typography.titleMedium)
                            Text("Disponibile quando lo smartphone è collegato alla rete IP che raggiunge l'apparato. L'app non memorizza credenziali.")
                            site.kairosEndpoints.forEach { endpoint ->
                                OutlinedButton(
                                    onClick = { openKairosWeb(context, endpoint.ipAddress) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Language, null)
                                    Text("${endpoint.label} — ${endpoint.ipAddress}", modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (site.phone != null || site.email != null) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        site.phone?.let { phone ->
                            OutlinedButton(onClick = { dial(context, phone) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Call, null); Text("Chiama", modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                        site.email?.let { email ->
                            OutlinedButton(onClick = { email(context, email) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Email, null); Text("E-mail", modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                    }
                }
            }

            if (site.links.isNotEmpty()) {
                item { Text("Collegamenti radio", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp)) }
                items(site.links) { LinkCard(it) }
            }
            item { androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 16.dp)) }
        }
    }
}

@Composable
private fun InfoCard(title: String, text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(text, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun LinkCard(link: RadioLink) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(link.name, style = MaterialTheme.typography.titleMedium)
            Text(link.type, style = MaterialTheme.typography.bodyMedium)
            link.rssiDbm?.let { Text("RSSI riferimento: $it dBm") }
            if (link.txMhz != null || link.rxMhz != null) {
                val tx = link.txMhz?.let { String.format(Locale.US, "%.4f", it) } ?: "—"
                val rx = link.rxMhz?.let { String.format(Locale.US, "%.4f", it) } ?: "—"
                Text("TX: $tx MHz   RX: $rx MHz")
            }
        }
    }
}

private fun openNavigation(context: Context, latitude: Double, longitude: Double, label: String) {
    val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")).apply { setPackage("com.google.android.apps.maps") }
    try { context.startActivity(mapsIntent) }
    catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$latitude,$longitude(${Uri.encode(label)})")))
    }
}

private fun openKairosWeb(context: Context, ipAddress: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://$ipAddress")))
}

private fun dial(context: Context, phone: String) = context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}")))
private fun email(context: Context, email: String) = context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${Uri.encode(email)}")))
