package it.vigilfuoco.tlcfield.data

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 42f

    fun generate(context: Context, intervention: Intervention): File {
        val site = SiteRepository.sites.firstOrNull { it.id == intervention.siteId }
        val pdf = PdfDocument()
        val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 8f }
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var pageNumber = 0
        var page: PdfDocument.Page? = null
        var y = MARGIN

        fun newPage() {
            page?.let { pdf.finishPage(it) }
            pageNumber += 1
            page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create())
            y = MARGIN
            page!!.canvas.drawText("TLC FIELD — Rapporto tecnico di intervento", MARGIN, y, title)
            y += 24f
            page!!.canvas.drawText("Centro TLC Nazionale VVF", MARGIN, y, normal)
            y += 24f
        }

        fun ensureSpace(needed: Float) {
            if (page == null || y + needed > PAGE_H - MARGIN) newPage()
        }

        fun line(text: String, paint: Paint = normal, indent: Float = 0f) {
            val maxChars = if (paint.textSize <= 8f) 92 else 76
            val words = text.split(" ")
            var current = ""
            for (word in words) {
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (candidate.length > maxChars && current.isNotEmpty()) {
                    ensureSpace(16f)
                    page!!.canvas.drawText(current, MARGIN + indent, y, paint)
                    y += 14f
                    current = word
                } else current = candidate
            }
            if (current.isNotEmpty()) {
                ensureSpace(16f)
                page!!.canvas.drawText(current, MARGIN + indent, y, paint)
                y += 14f
            }
        }

        fun section(name: String) {
            ensureSpace(28f)
            y += 6f
            page!!.canvas.drawText(name, MARGIN, y, heading)
            y += 18f
        }

        newPage()
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY)
        line("Sito: ${intervention.siteName}${site?.code?.let { " — $it" } ?: ""}")
        line("Data e ora: ${df.format(Date(intervention.timestamp))}")
        line("Tipologia: ${intervention.type}")
        site?.network?.let { line("Rete: $it") }
        if (site?.latitude != null && site.longitude != null) line("Coordinate: ${site.latitude}, ${site.longitude}")
        site?.altitudeM?.let { line("Quota: $it m") }

        section("Segnalazione e stato iniziale")
        line("Motivo / problema: ${intervention.reportedProblem.ifBlank { "Non indicato" }}")
        line("Stato all'arrivo: ${intervention.initialState}")

        section("Verifiche tecniche")
        line("Alimentazione regolare: ${yesNo(intervention.powerOk)}")
        line("Apparato radio acceso: ${yesNo(intervention.radioOn)}")
        line("Allarmi presenti: ${yesNo(intervention.alarms)}")
        line("Collegamento dati / IP operativo: ${yesNo(intervention.ipLinkOk)}")

        section("Misure RSSI")
        if (intervention.measurements.none { it.referenceRssi != null || it.measuredRssi != null }) {
            line("Nessuna misura registrata.")
        } else {
            intervention.measurements.forEach { m ->
                val ref = m.referenceRssi?.let { "$it dBm" } ?: "—"
                val measured = m.measuredRssi?.let { "$it dBm" } ?: "—"
                val delta = m.deltaDb?.let { "${if (it > 0) "+" else ""}$it dB" } ?: "—"
                line("${m.linkName}: riferimento $ref | misurato $measured | Δ $delta", small)
            }
        }

        if (intervention.kairosSnapshot != null || intervention.kairosAlarmNumbers.isNotEmpty()) {
            section("Diagnosi KAIROS")
            intervention.kairosSnapshot?.let { k ->
                k.supplyVoltageV?.let { line("Input Supply Voltage: $it V") }
                k.txTemperatureC?.let { line("TX Temperature: $it °C") }
                k.forwardPowerW?.let { line("Forward Power: $it W") }
                k.reflectedPowerW?.let { line("Reflected Power: $it W") }
                k.rssiMainDbm?.let { line("RSSI Main: $it dBm") }
                k.rssiDiversityDbm?.let { line("RSSI Diversity: $it dBm") }
                if (k.synchronizationSource.isNotBlank()) line("Sincronizzazione: ${k.synchronizationSource}")
            }
            if (intervention.kairosAlarmNumbers.isEmpty()) {
                line("Nessun allarme KAIROS registrato.")
            } else {
                intervention.kairosAlarmNumbers.forEach { number ->
                    val alarm = KairosRepository.alarm(number)
                    val guide = KairosRepository.guide(number)
                    if (alarm != null) {
                        line("Allarme ${alarm.number} — ${alarm.label} [${alarm.severity.label}]", heading)
                        line("Area: ${alarm.diagnosticArea}", small)
                        guide.checks.forEachIndexed { index, check ->
                            val key = "$number|$index"
                            val done = if (key in intervention.kairosCompletedChecks) "ESEGUITA" else "NON REGISTRATA"
                            line("[$done] $check", small, 8f)
                        }
                    }
                }
            }
            if (intervention.kairosDiagnosticNotes.isNotBlank()) line("Note diagnosi: ${intervention.kairosDiagnosticNotes}")
        }

        section("Operazioni e conclusioni")
        line(intervention.notes.ifBlank { "Nessuna nota inserita." })
        line("Esito: ${intervention.result}", heading)

        if (intervention.photos.isNotEmpty()) {
            section("Documentazione fotografica")
            intervention.photos.forEachIndexed { index, photo ->
                val file = File(photo.path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        val availableW = PAGE_W - (MARGIN * 2)
                        val maxH = 260f
                        val ratio = minOf(availableW / bitmap.width, maxH / bitmap.height)
                        val drawW = bitmap.width * ratio
                        val drawH = bitmap.height * ratio
                        ensureSpace(drawH + 34f)
                        line("Foto ${index + 1} — ${photo.category}", heading)
                        val left = MARGIN + (availableW - drawW) / 2f
                        page!!.canvas.drawBitmap(bitmap, null, android.graphics.RectF(left, y, left + drawW, y + drawH), null)
                        y += drawH + 14f
                        bitmap.recycle()
                    }
                }
            }
        }

        page?.let { pdf.finishPage(it) }
        val reportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "reports").apply { mkdirs() }
        val safeSite = intervention.siteName.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.ITALY).format(Date(intervention.timestamp))
        val output = File(reportDir, "TLCField_${safeSite}_$stamp.pdf")
        FileOutputStream(output).use { pdf.writeTo(it) }
        pdf.close()
        return output
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Condividi rapporto TLC Field"))
    }

    private fun yesNo(value: Boolean) = if (value) "Sì" else "No"
}
