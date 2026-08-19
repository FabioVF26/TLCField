package it.vigilfuoco.tlcfield.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
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
    var mapStatus by remember { mutableStateOf("in preparazione…") }

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

            Text(
                "Stato mappa: $mapStatus",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            mapError?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        "Errore mappa: $error",
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

                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                mapStatus = "pagina in caricamento…"
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                mapStatus = "pagina caricata, verifico Leaflet…"
                                view?.evaluateJavascript(
                                    "(function(){try{" +
                                        "if (typeof L === 'undefined') return 'ERRORE: libreria Leaflet non caricata';" +
                                        "if (typeof map === 'undefined') return 'ERRORE: mappa non inizializzata';" +
                                        "window.dispatchEvent(new Event('resize'));" +
                                        "map.invalidateSize();" +
                                        "var el = document.getElementById('map');" +
                                        "return 'OK: leaflet caricato, contenitore ' + el.offsetWidth + 'x' + el.offsetHeight;" +
                                        "}catch(e){return 'ERRORE JS: ' + e.message;}})()"
                                ) { result ->
                                    val clean = result?.trim('"') ?: "nessuna risposta"
                                    mapStatus = clean
                                    if (clean.startsWith("ERRORE")) mapError = clean

                                    view?.postDelayed({
                                        view.evaluateJavascript(
                                            "(function(){var el=document.getElementById('map'); return 'ricontrollo dopo 2s: ' + el.offsetWidth + 'x' + el.offsetHeight;})()"
                                        ) { result2 ->
                                            mapStatus = (result2?.trim('"') ?: "") + "  |  " + clean
                                        }
                                    }, 2000)
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                val failedUrl = request?.url?.lastPathSegment ?: "risorsa sconosciuta"
                                val desc = error?.description?.toString() ?: "errore sconosciuto"
                                mapError = "$failedUrl -> $desc"
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                                if (message?.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                                    mapError = "JS: ${message.message()} (riga ${message.lineNumber()})"
                                }
                                return true
                            }
                        }

                        runCatching {
                            // Copia i file della mappa (html + libreria leaflet + icone) in un'unica
                            // cartella reale, cosi' i percorsi relativi funzionano senza ambiguita'.
                            val mapDir = File(ctx.cacheDir, "map").apply { mkdirs() }
                            val imagesDir = File(mapDir, "images").apply { mkdirs() }

                            fun copyAsset(assetPath: String, dest: File) {
                                ctx.assets.open(assetPath).use { input ->
                                    dest.outputStream().use { output -> input.copyTo(output) }
                                }
                            }

                            copyAsset("leaflet/leaflet.css", File(mapDir, "leaflet.css"))
                            copyAsset("leaflet/leaflet.js", File(mapDir, "leaflet.js"))
                            for (img in listOf("marker-icon.png", "marker-icon-2x.png", "marker-shadow.png", "layers.png", "layers-2x.png")) {
                                copyAsset("leaflet/images/$img", File(imagesDir, img))
                            }

                            val htmlFile = File(mapDir, "sites_map.html")
                            htmlFile.writeText(buildMapHtml(mappableSites))
                            mapStatus = "file pronti, avvio caricamento…"
                            loadUrl("file://${htmlFile.absolutePath}")
                        }.onFailure { e ->
                            mapError = "preparazione fallita: ${e.message}"
                            mapStatus = "preparazione fallita"
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
          <link rel="stylesheet" href="leaflet.css" />
          <script src="leaflet.js"></script>
          <style>
            html, body { margin: 0; padding: 0; height: 100%; width: 100%; }
            #map { position: absolute; top: 0; left: 0; right: 0; bottom: 0; }
            body { font-family: sans-serif; }
            .leaflet-popup-content { font-size: 14px; line-height: 1.35; }
          </style>
        </head>
        <body>
          <div id="map"></div>
          <script>
            var map = L.map('map', { zoomControl: true });
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
              maxZoom: 19,
              attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);
            var bounds = [];
            $markers
            if (bounds.length > 0) {
              map.fitBounds(bounds, {padding: [30, 30], maxZoom: 10});
            } else {
              map.setView([41.9, 12.5], 7);
            }
            setTimeout(function(){ map.invalidateSize(); }, 300);

            // Il contenitore puo' avere altezza 0 nell'istante esatto in cui la mappa
            // si inizializza (la WebView non ha ancora ricevuto le dimensioni definitive
            // da Android). Osserviamo il contenitore e ridisegniamo la mappa ogni volta
            // che le sue dimensioni cambiano, cosi' non resta mai bloccata a 0.
            var mapEl = document.getElementById('map');
            function fixSize() { map.invalidateSize(); }
            if (window.ResizeObserver) {
              new ResizeObserver(fixSize).observe(mapEl);
            } else {
              var tries = 0;
              var iv = setInterval(function(){
                fixSize();
                tries++;
                if (tries > 20) clearInterval(iv);
              }, 250);
            }
            window.addEventListener('resize', fixSize);
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
