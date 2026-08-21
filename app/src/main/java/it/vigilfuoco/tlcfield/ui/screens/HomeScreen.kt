package it.vigilfuoco.tlcfield.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.vigilfuoco.tlcfield.R

@Composable
fun HomeScreen(
    onSites: () -> Unit,
    onMap: () -> Unit,
    onNewIntervention: () -> Unit,
    onHistory: () -> Unit,
    onDiagnosis: () -> Unit,
    onDocumentation: () -> Unit,
    onRfTools: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.tlc_logo),
                contentDescription = "Logo TLC Vigili del Fuoco",
                modifier = Modifier.height(120.dp),
                contentScale = ContentScale.Fit
            )
            Text("TLC FIELD", style = MaterialTheme.typography.headlineLarge)
            Text("Supporto tecnico ponti radio VVF", fontSize = 16.sp)

            HomeButton("NUOVO INTERVENTO", Icons.Default.Build, true, onNewIntervention)
            HomeButton("SITI / PONTI RADIO", Icons.Default.Radio, true, onSites)
            HomeButton("MAPPA SITI", Icons.Default.Map, true, onMap)
            HomeButton("DIAGNOSI KAIROS", Icons.Default.SettingsInputAntenna, true, onDiagnosis)
            HomeButton("DOCUMENTAZIONE", Icons.Default.Description, true, onDocumentation)
            HomeButton("STRUMENTI RF", Icons.Default.Calculate, true, onRfTools)
            HomeButton("STORICO INTERVENTI", Icons.Default.History, true, onHistory)

            Text(
                text = "Versione 1.1 — storico interventi organizzato in cartelle per sito, strumenti RF, biblioteca tecnica offline, mappa e diagnostica KAIROS.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun HomeButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(15.dp)) {
        Icon(icon, contentDescription = null)
        Text(label, modifier = Modifier.padding(start = 12.dp))
    }
}
