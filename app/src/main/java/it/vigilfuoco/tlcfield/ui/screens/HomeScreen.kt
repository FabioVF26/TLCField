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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    onRfTools: () -> Unit,
    onServer: () -> Unit
) {
    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.logo_tlc
                ),
                contentDescription = "Logo TLC Vigili del Fuoco",
                modifier = Modifier.height(120.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "TLC FIELD",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Supporto tecnico ponti radio VVF",
                fontSize = 16.sp
            )

            HomeButton(
                label = "NUOVO INTERVENTO",
                icon = Icons.Default.Build,
                enabled = true,
                onClick = onNewIntervention
            )

            HomeButton(
                label = "SITI / PONTI RADIO",
                icon = Icons.Default.Radio,
                enabled = true,
                onClick = onSites
            )

            HomeButton(
                label = "MAPPA SITI",
                icon = Icons.Default.Map,
                enabled = true,
                onClick = onMap
            )

            HomeButton(
                label = "DIAGNOSI KAIROS",
                icon = Icons.Default.SettingsInputAntenna,
                enabled = true,
                onClick = onDiagnosis
            )

            HomeButton(
                label = "DOCUMENTAZIONE",
                icon = Icons.Default.Description,
                enabled = true,
                onClick = onDocumentation
            )

            HomeButton(
                label = "STRUMENTI RF",
                icon = Icons.Default.Calculate,
                enabled = true,
                onClick = onRfTools
            )

            HomeButton(
                label = "STORICO INTERVENTI",
                icon = Icons.Default.History,
                enabled = true,
                onClick = onHistory
            )

            HomeButton(
                label = "SERVER / SINCRONIZZAZIONE",
                icon = Icons.Default.CloudSync,
                enabled = true,
                onClick = onServer
            )

            Text(
                text = "Versione 1.4 — modifica rapporti, cancellazione protetta da amministratore, foto orientate automaticamente, sincronizzazione server e diagnostica KAIROS.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun HomeButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(15.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Text(
            text = label,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
