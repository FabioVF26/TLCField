package it.vigilfuoco.tlcfield.ui.screens

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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import it.vigilfuoco.tlcfield.data.DocumentCategory
import it.vigilfuoco.tlcfield.data.DocumentationRepository
import it.vigilfuoco.tlcfield.data.PdfAssetOpener
import it.vigilfuoco.tlcfield.data.TechnicalDocument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<DocumentCategory?>(null) }
    val docs = DocumentationRepository.documents.filter { selectedCategory == null || it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documentazione tecnica") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Indietro") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Biblioteca offline", style = MaterialTheme.typography.titleMedium)
                        Text("I manuali KAIROS e le schede dei siti sono inclusi nell'app e possono essere consultati anche in assenza di rete dati.")
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = selectedCategory == null, onClick = { selectedCategory = null }, label = { Text("Tutti") })
                    FilterChip(selected = selectedCategory == DocumentCategory.KAIROS, onClick = { selectedCategory = DocumentCategory.KAIROS }, label = { Text("KAIROS") })
                    FilterChip(selected = selectedCategory == DocumentCategory.SITE, onClick = { selectedCategory = DocumentCategory.SITE }, label = { Text("Siti") })
                }
            }
            items(docs, key = { it.id }) { doc ->
                DocumentCard(doc = doc, onOpen = { PdfAssetOpener.open(context, doc) })
            }
            item { androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 16.dp)) }
        }
    }
}

@Composable
private fun DocumentCard(doc: TechnicalDocument, onOpen: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(if (doc.category == DocumentCategory.KAIROS) Icons.Default.Radio else Icons.Default.Description, contentDescription = null)
                Column(modifier = Modifier.weight(1f)) {
                    Text(doc.title, style = MaterialTheme.typography.titleMedium)
                    Text(doc.subtitle, style = MaterialTheme.typography.bodyMedium)
                }
            }
            OutlinedButton(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Description, contentDescription = null)
                Text("APRI PDF", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
