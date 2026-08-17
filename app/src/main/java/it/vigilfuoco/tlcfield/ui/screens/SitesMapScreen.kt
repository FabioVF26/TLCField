package it.vigilfuoco.tlcfield.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import it.vigilfuoco.tlcfield.data.Site
import it.vigilfuoco.tlcfield.data.SiteRepository
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SitesMapScreen(
    onBack: () -> Unit,
    onSiteSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val mappableSites = SiteRepository.sites.filter {
        it.latitude != null && it.longitude != null && it.navigationVerified
    }
    var mapError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mappa siti") },
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
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Ponti radio TLC", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Toccare un indicatore per aprire la scheda del sito. La cartografia richiede connessione dati; la navigazione stradale viene avviata dalla scheda sito tramite Google Maps.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            mapError?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        "Errore caricamento mappa: $error",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxWidth().weight(1f),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val uri = request?.url ?: return false
                                if (uri.scheme == "tlcfield" && uri.host == "site") {
                                    val id = uri.pathSegments.firstOrNull()
                                    if (!id.isNullOrBlank()) onSiteSelected(id)
                                    return true
                                }
                                if (uri.scheme == "geo" || uri.host?.contains("google") == true) {
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                                    return true
                                }
                                return false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    mapError = error?.description?.toString() ?: "errore sconosciuto"
                                }
                            }
                        }

                        val htmlFile = File(ctx.cacheDir, "sites_map.html")
                        runCatching {
                            htmlFile.writeText(buildMapHtml(mappableSites))
                            loadUrl("file://${htmlFile.absolutePath}")
                        }.onFailure { e ->
                            mapError = e.message ?: "impossibile preparare la mappa"
                        }
                    }
                }
            )
        }
    }
}

private fun buildMapHtml(sites: List<Site>): String {
    val markers = sites.joinToString("\n") { site ->
        val lat = site.latitude!!
        val lon = site.longitude!!
        val title = jsEscape(site.name)
        val subtitle = jsEscape(
            listOfNotNull(site.code?.let { "Codice $it" }, site.network, site.altitudeM?.let { "Quota ${it} m" })
                .joinToString(" · ")
        )
        """
        L.marker([$lat, $lon]).addTo(map)
          .bindPopup('<b>$title</b><br>$subtitle<br><a href="tlcfield://site/${site.id}">Apri scheda sito</a>');
        bounds.push([$lat, $lon]);
        """.trimIndent()
    }

    return """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
          <link rel="stylesheet" href="file:///android_asset/leaflet/leaflet.css" />
          <script src="file:///android_asset/leaflet/leaflet.js"></script>
          <style>
            html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; }
            body { font-family: sans-serif; }
            .leaflet-popup-content { font-size: 14px; line-height: 1.35; }
          </style>
        </head>
        <body>
          <div id="map"></div>
          <script>
            L.Icon.Default.prototype.options.imagePath = 'file:///android_asset/leaflet/images/';
            const map = L.map('map', { zoomControl: true });
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
              maxZoom: 19,
              attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);
            const bounds = [];
            $markers
            if (bounds.length > 0) {
              map.fitBounds(bounds, {padding: [30, 30], maxZoom: 10});
            } else {
              map.setView([41.9, 12.5], 7);
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}

private fun jsEscape(value: String): String = value
    .replace("\\", "\\\\")
    .replace("'", "\\'")
    .replace("\n", " ")
    .replace("\r", " ")
