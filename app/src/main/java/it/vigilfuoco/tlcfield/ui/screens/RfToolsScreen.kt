package it.vigilfuoco.tlcfield.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

private fun String.toRfDouble(): Double? = replace(',', '.').trim().toDoubleOrNull()
private fun fmt(v: Double, decimals: Int = 2): String = if (v.isFinite()) "% .${decimals}f".format(v).trim() else "∞"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfToolsScreen(onBack: () -> Unit) {
    val tools = listOf<@Composable () -> Unit>(
        { PowerConverterCard() },
        { SwrCard() },
        { CableLossCard() },
        { EirpCard() },
        { FsPLCard() },
        { LinkBudgetCard() },
        { FresnelCard() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Strumenti RF") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Calcoli di supporto alle verifiche sul campo. I risultati non sostituiscono la misura strumentale.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(tools) { tool -> tool() }
            item { Text("", modifier = Modifier.padding(bottom = 8.dp)) }
        }
    }
}

@Composable
private fun ToolCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            content()
        }
    }
}

@Composable
private fun NumField(value: String, label: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun Result(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun PowerConverterCard() {
    var watts by remember { mutableStateOf("") }
    var dbm by remember { mutableStateOf("") }
    ToolCard("Potenza W ↔ dBm", "Conversione della potenza RF.") {
        NumField(watts, "Watt [W]", { watts = it })
        val w = watts.toRfDouble()
        Result("dBm", if (w != null && w > 0) "${fmt(30.0 + 10.0 * log10(w))} dBm" else "—")
        NumField(dbm, "Potenza [dBm]", { dbm = it })
        val d = dbm.toRfDouble()
        Result("Watt", if (d != null) "${fmt(10.0.pow((d - 30.0) / 10.0), 4)} W" else "—")
    }
}

@Composable
private fun SwrCard() {
    var pf by remember { mutableStateOf("") }
    var pr by remember { mutableStateOf("") }
    ToolCard("ROS / SWR e Return Loss", "Calcolo da potenza diretta e riflessa misurate nello stesso punto.") {
        NumField(pf, "Potenza diretta Pf [W]", { pf = it })
        NumField(pr, "Potenza riflessa Pr [W]", { pr = it })
        val f = pf.toRfDouble()
        val r = pr.toRfDouble()
        if (f != null && r != null && f > 0 && r >= 0 && r < f) {
            val gamma = sqrt(r / f)
            val swr = if (gamma < 1.0) (1.0 + gamma) / (1.0 - gamma) else Double.POSITIVE_INFINITY
            val rl = if (r == 0.0) Double.POSITIVE_INFINITY else 10.0 * log10(f / r)
            Result("ROS / SWR", fmt(swr, 2))
            Result("Return Loss", if (rl.isFinite()) "${fmt(rl)} dB" else "∞ dB")
            Result("Potenza riflessa", "${fmt((r / f) * 100.0)} %")
        } else {
            Result("Risultato", if (f != null && r != null && r >= f) "Pr deve essere inferiore a Pf" else "—")
        }
    }
}

@Composable
private fun CableLossCard() {
    var attenuation by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    ToolCard("Perdita di linea", "Inserire l'attenuazione del cavo alla frequenza di lavoro, espressa in dB/100 m.") {
        NumField(attenuation, "Attenuazione [dB/100 m]", { attenuation = it })
        NumField(length, "Lunghezza cavo [m]", { length = it })
        val a = attenuation.toRfDouble()
        val l = length.toRfDouble()
        Result("Perdita stimata", if (a != null && l != null && a >= 0 && l >= 0) "${fmt(a * l / 100.0)} dB" else "—")
    }
}

@Composable
private fun EirpCard() {
    var txW by remember { mutableStateOf("") }
    var gain by remember { mutableStateOf("") }
    var loss by remember { mutableStateOf("") }
    ToolCard("EIRP", "Potenza isotropica equivalente: Ptx + guadagno antenna − perdite di linea.") {
        NumField(txW, "Potenza TX [W]", { txW = it })
        NumField(gain, "Guadagno antenna [dBi]", { gain = it })
        NumField(loss, "Perdite TX [dB]", { loss = it })
        val w = txW.toRfDouble(); val g = gain.toRfDouble(); val l = loss.toRfDouble()
        if (w != null && w > 0 && g != null && l != null) {
            val eirp = 30.0 + 10.0 * log10(w) + g - l
            Result("EIRP", "${fmt(eirp)} dBm")
            Result("EIRP", "${fmt(10.0.pow((eirp - 30.0) / 10.0), 3)} W")
        } else Result("EIRP", "—")
    }
}

@Composable
private fun FsPLCard() {
    var freq by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    ToolCard("Attenuazione di spazio libero (FSPL)", "Formula con frequenza in MHz e distanza in km.") {
        NumField(freq, "Frequenza [MHz]", { freq = it })
        NumField(distance, "Distanza [km]", { distance = it })
        val f = freq.toRfDouble(); val d = distance.toRfDouble()
        Result("FSPL", if (f != null && d != null && f > 0 && d > 0) "${fmt(32.44 + 20.0 * log10(f) + 20.0 * log10(d))} dB" else "—")
    }
}

@Composable
private fun LinkBudgetCard() {
    var txW by remember { mutableStateOf("") }
    var txGain by remember { mutableStateOf("") }
    var txLoss by remember { mutableStateOf("") }
    var freq by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var rxGain by remember { mutableStateOf("") }
    var rxLoss by remember { mutableStateOf("") }
    var otherLoss by remember { mutableStateOf("0") }
    ToolCard("Link budget", "Stima del livello ricevuto in spazio libero.") {
        NumField(txW, "Potenza TX [W]", { txW = it })
        NumField(txGain, "Guadagno antenna TX [dBi]", { txGain = it })
        NumField(txLoss, "Perdite linea TX [dB]", { txLoss = it })
        NumField(freq, "Frequenza [MHz]", { freq = it })
        NumField(distance, "Distanza [km]", { distance = it })
        NumField(rxGain, "Guadagno antenna RX [dBi]", { rxGain = it })
        NumField(rxLoss, "Perdite linea RX [dB]", { rxLoss = it })
        NumField(otherLoss, "Altre perdite [dB]", { otherLoss = it })
        val vals = listOf(txW, txGain, txLoss, freq, distance, rxGain, rxLoss, otherLoss).map { it.toRfDouble() }
        if (vals.all { it != null }) {
            val w=vals[0]!!; val gt=vals[1]!!; val lt=vals[2]!!; val f=vals[3]!!; val d=vals[4]!!; val gr=vals[5]!!; val lr=vals[6]!!; val lo=vals[7]!!
            if (w > 0 && f > 0 && d > 0) {
                val txDbm = 30.0 + 10.0 * log10(w)
                val fspl = 32.44 + 20.0 * log10(f) + 20.0 * log10(d)
                val rxDbm = txDbm + gt - lt - fspl + gr - lr - lo
                Result("Livello RX stimato", "${fmt(rxDbm)} dBm")
                Result("FSPL", "${fmt(fspl)} dB")
            } else Result("Livello RX stimato", "—")
        } else Result("Livello RX stimato", "—")
    }
}

@Composable
private fun FresnelCard() {
    var freq by remember { mutableStateOf("") }
    var d1 by remember { mutableStateOf("") }
    var d2 by remember { mutableStateOf("") }
    ToolCard("1ª zona di Fresnel", "Raggio della prima zona di Fresnel nel punto considerato. d1 e d2 sono le distanze dai due estremi.") {
        NumField(freq, "Frequenza [MHz]", { freq = it })
        NumField(d1, "d1 [km]", { d1 = it })
        NumField(d2, "d2 [km]", { d2 = it })
        val f = freq.toRfDouble(); val a = d1.toRfDouble(); val b = d2.toRfDouble()
        if (f != null && a != null && b != null && f > 0 && a > 0 && b > 0) {
            val r = 17.32 * sqrt((a * b) / (f / 1000.0 * (a + b)))
            Result("Raggio F1", "${fmt(r)} m")
            Result("60% F1", "${fmt(r * 0.60)} m")
        } else Result("Raggio F1", "—")
    }
}
