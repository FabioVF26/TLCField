package it.vigilfuoco.tlcfield.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import it.vigilfuoco.tlcfield.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_W = 595
    private const val PAGE_H = 842

    private const val MARGIN_LEFT = 38f
    private const val MARGIN_RIGHT = 38f
    private const val MARGIN_TOP = 30f
    private const val MARGIN_BOTTOM = 42f

    private const val CONTENT_W =
        PAGE_W - MARGIN_LEFT - MARGIN_RIGHT

    private val institutionalRed =
        Color.rgb(170, 25, 35)

    private val darkText =
        Color.rgb(45, 45, 45)

    private val mediumGray =
        Color.rgb(105, 105, 105)

    private val lightGray =
        Color.rgb(240, 240, 240)

    private val borderGray =
        Color.rgb(205, 205, 205)

    fun generate(
        context: Context,
        intervention: Intervention
    ): File {

        val site =
            SiteRepository.sites.firstOrNull {
                it.id == intervention.siteId
            }

        val pdf = PdfDocument()

        // =====================================================
        // PAINT
        // =====================================================

        val normalPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 9.5f
                color = darkText
                typeface =
                    Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.NORMAL
                    )
            }

        val smallPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 8f
                color = mediumGray
            }

        val boldPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 9.5f
                color = darkText
                typeface =
                    Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                    )
            }

        val titlePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 17f
                color = darkText
                typeface =
                    Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                    )
                textAlign = Paint.Align.CENTER
            }

        val subtitlePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 9f
                color = mediumGray
                textAlign = Paint.Align.CENTER
            }

        val sectionPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 11f
                color = Color.WHITE
                typeface =
                    Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                    )
            }

        val linePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                strokeWidth = 0.8f
                color = borderGray
                style = Paint.Style.STROKE
            }

        val sectionBackgroundPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = institutionalRed
                style = Paint.Style.FILL
            }

        val boxBackgroundPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(
                    250,
                    250,
                    250
                )
                style = Paint.Style.FILL
            }

        // =====================================================
        // STATO PAGINE
        // =====================================================

        var pageNumber = 0
        var page: PdfDocument.Page? = null
        var y = MARGIN_TOP

        val logoVvf =
            runCatching {
                loadScaledResourceBitmap(
                    context,
                    R.drawable.logo_vvf,
                    420
                )
            }.getOrNull()

        val logoTlc =
            runCatching {
                loadScaledResourceBitmap(
                    context,
                    R.drawable.logo_tlc,
                    420
                )
            }.getOrNull()

        // =====================================================
        // FUNZIONI GRAFICHE
        // =====================================================

        fun drawBitmapFit(
            bitmap: Bitmap?,
            left: Float,
            top: Float,
            maxWidth: Float,
            maxHeight: Float
        ) {

            if (bitmap == null) return

            val ratio =
                minOf(
                    maxWidth / bitmap.width,
                    maxHeight / bitmap.height
                )

            val width =
                bitmap.width * ratio

            val height =
                bitmap.height * ratio

            val x =
                left +
                    (maxWidth - width) / 2f

            val yBitmap =
                top +
                    (maxHeight - height) / 2f

            page!!.canvas.drawBitmap(
                bitmap,
                null,
                RectF(
                    x,
                    yBitmap,
                    x + width,
                    yBitmap + height
                ),
                null
            )
        }

        fun drawFooter() {

            val canvas =
                page?.canvas ?: return

            val footerY =
                PAGE_H - 23f

            val footerLine =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = borderGray
                    strokeWidth = 0.7f
                }

            canvas.drawLine(
                MARGIN_LEFT,
                footerY - 10f,
                PAGE_W - MARGIN_RIGHT,
                footerY - 10f,
                footerLine
            )

            val footerPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 7.5f
                    color = mediumGray
                }

            canvas.drawText(
                "Centro TLC Nazionale — Corpo Nazionale dei Vigili del Fuoco",
                MARGIN_LEFT,
                footerY,
                footerPaint
            )

            footerPaint.textAlign =
                Paint.Align.RIGHT

            canvas.drawText(
                "TLC Field  •  Pagina $pageNumber",
                PAGE_W - MARGIN_RIGHT,
                footerY,
                footerPaint
            )
        }

        fun finishPage() {

            page?.let {

                drawFooter()

                pdf.finishPage(it)

                page = null
            }
        }

        fun newPage() {

            finishPage()

            pageNumber++

            page =
                pdf.startPage(
                    PdfDocument.PageInfo.Builder(
                        PAGE_W,
                        PAGE_H,
                        pageNumber
                    ).create()
                )

            val canvas =
                page!!.canvas

            // Logo VVF
            drawBitmapFit(
                logoVvf,
                MARGIN_LEFT,
                22f,
                65f,
                58f
            )

            // Logo TLC
            drawBitmapFit(
                logoTlc,
                PAGE_W -
                    MARGIN_RIGHT -
                    65f,
                22f,
                65f,
                58f
            )

            canvas.drawText(
                "RAPPORTO TECNICO DI INTERVENTO",
                PAGE_W / 2f,
                48f,
                titlePaint
            )

            canvas.drawText(
                "Centro TLC Nazionale",
                PAGE_W / 2f,
                64f,
                subtitlePaint
            )

            canvas.drawLine(
                MARGIN_LEFT,
                90f,
                PAGE_W - MARGIN_RIGHT,
                90f,
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {
                    color = institutionalRed
                    strokeWidth = 2.3f
                }
            )

            y = 108f
        }

        fun ensureSpace(
            needed: Float
        ) {

            if (
                page == null ||
                y + needed >
                PAGE_H - MARGIN_BOTTOM
            ) {
                newPage()
            }
        }

        fun wrapText(
            text: String,
            paint: Paint,
            maxWidth: Float
        ): List<String> {

            if (text.isBlank()) {
                return listOf("")
            }

            val result =
                mutableListOf<String>()

            val paragraphs =
                text.split("\n")

            paragraphs.forEach { paragraph ->

                val words =
                    paragraph.split(" ")

                var current = ""

                words.forEach { word ->

                    val candidate =
                        if (current.isBlank()) {
                            word
                        } else {
                            "$current $word"
                        }

                    if (
                        paint.measureText(candidate) >
                        maxWidth &&
                        current.isNotBlank()
                    ) {

                        result += current
                        current = word

                    } else {

                        current = candidate
                    }
                }

                if (current.isNotBlank()) {
                    result += current
                }

                if (paragraph.isBlank()) {
                    result += ""
                }
            }

            return result
        }

        fun textLine(
            text: String,
            paint: Paint = normalPaint,
            indent: Float = 0f
        ) {

            val maxWidth =
                CONTENT_W - indent

            val rows =
                wrapText(
                    text,
                    paint,
                    maxWidth
                )

            rows.forEach { row ->

                ensureSpace(15f)

                page!!.canvas.drawText(
                    row,
                    MARGIN_LEFT + indent,
                    y,
                    paint
                )

                y += 13f
            }
        }

        fun section(
            name: String
        ) {

            ensureSpace(38f)

            y += 7f

            val rect =
                RectF(
                    MARGIN_LEFT,
                    y,
                    PAGE_W - MARGIN_RIGHT,
                    y + 25f
                )

            page!!.canvas.drawRoundRect(
                rect,
                4f,
                4f,
                sectionBackgroundPaint
            )

            page!!.canvas.drawText(
                name.uppercase(),
                MARGIN_LEFT + 10f,
                y + 17f,
                sectionPaint
            )

            y += 34f
        }

        fun infoRow(
            label: String,
            value: String
        ) {

            ensureSpace(26f)

            val rowHeight = 24f

            val rect =
                RectF(
                    MARGIN_LEFT,
                    y,
                    PAGE_W - MARGIN_RIGHT,
                    y + rowHeight
                )

            page!!.canvas.drawRect(
                rect,
                boxBackgroundPaint
            )

            page!!.canvas.drawRect(
                rect,
                linePaint
            )

            val labelWidth =
                145f

            page!!.canvas.drawLine(
                MARGIN_LEFT + labelWidth,
                y,
                MARGIN_LEFT + labelWidth,
                y + rowHeight,
                linePaint
            )

            page!!.canvas.drawText(
                label,
                MARGIN_LEFT + 8f,
                y + 16f,
                boldPaint
            )

            page!!.canvas.drawText(
                value,
                MARGIN_LEFT +
                    labelWidth +
                    8f,
                y + 16f,
                normalPaint
            )

            y += rowHeight
        }

        fun paragraphBox(
            label: String,
            value: String
        ) {

            val display =
                value.ifBlank {
                    "Non indicato"
                }

            val availableWidth =
                CONTENT_W - 20f

            val lines =
                wrapText(
                    display,
                    normalPaint,
                    availableWidth
                )

            val height =
                32f +
                    (lines.size * 13f)

            ensureSpace(height + 8f)

            val rect =
                RectF(
                    MARGIN_LEFT,
                    y,
                    PAGE_W - MARGIN_RIGHT,
                    y + height
                )

            page!!.canvas.drawRoundRect(
                rect,
                4f,
                4f,
                boxBackgroundPaint
            )

            page!!.canvas.drawRoundRect(
                rect,
                4f,
                4f,
                linePaint
            )

            page!!.canvas.drawText(
                label,
                MARGIN_LEFT + 10f,
                y + 17f,
                boldPaint
            )

            var textY =
                y + 34f

            lines.forEach { line ->

                page!!.canvas.drawText(
                    line,
                    MARGIN_LEFT + 10f,
                    textY,
                    normalPaint
                )

                textY += 13f
            }

            y += height + 6f
        }

        fun statusRow(
            label: String,
            value: Boolean
        ) {

            val status =
                if (value) {
                    "SÌ"
                } else {
                    "NO"
                }

            infoRow(
                label,
                status
            )
        }

        // =====================================================
        // PRIMA PAGINA
        // =====================================================

        newPage()

        val dateFormat =
            SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.ITALY
            )

        // =====================================================
        // IDENTIFICAZIONE
        // =====================================================

        section(
            "Identificazione intervento"
        )

        infoRow(
            "Sito / ponte radio",
            intervention.siteName
        )

        site?.code?.let {
            infoRow(
                "Codice sito",
                it
            )
        }

        infoRow(
            "Data e ora",
            dateFormat.format(
                Date(
                    intervention.timestamp
                )
            )
        )

        infoRow(
            "Tipologia",
            intervention.type
        )

        site?.network?.let {
            infoRow(
                "Rete",
                it
            )
        }

        if (
            site?.latitude != null &&
            site.longitude != null
        ) {

            infoRow(
                "Coordinate",
                "${site.latitude}, ${site.longitude}"
            )
        }

        site?.altitudeM?.let {

            infoRow(
                "Quota",
                "$it m s.l.m."
            )
        }

        // =====================================================
        // PERSONALE E AUTOMEZZI
        // =====================================================

        section(
            "Personale intervenuto"
        )

        if (intervention.personnel.isEmpty()) {
            textLine("Nessun nominativo registrato.")
        } else {
            intervention.personnel.forEachIndexed { index, person ->
                val qualification = person.qualification.ifBlank { "—" }
                infoRow(
                    "Operatore ${index + 1}",
                    "$qualification — ${person.fullName}"
                )
            }
        }

        section(
            "Automezzi utilizzati"
        )

        if (intervention.vehicles.isEmpty()) {
            textLine("Nessun automezzo registrato.")
        } else {
            intervention.vehicles.forEachIndexed { index, vehicle ->
                infoRow(
                    "Mezzo ${index + 1}",
                    "${vehicle.description} — ${vehicle.plate}"
                )
            }
        }

        // =====================================================
        // SEGNALAZIONE
        // =====================================================

        section(
            "Segnalazione e stato iniziale"
        )

        paragraphBox(
            "Problema / motivo dell'intervento",
            intervention.reportedProblem
        )

        infoRow(
            "Stato impianto all'arrivo",
            intervention.initialState
        )

        // =====================================================
        // VERIFICHE
        // =====================================================

        section(
            "Verifiche tecniche"
        )

        statusRow(
            "Alimentazione regolare",
            intervention.powerOk
        )

        statusRow(
            "Apparato radio acceso",
            intervention.radioOn
        )

        statusRow(
            "Allarmi presenti",
            intervention.alarms
        )

        statusRow(
            "Collegamento dati / IP operativo",
            intervention.ipLinkOk
        )

        // =====================================================
        // RSSI
        // =====================================================

        section(
            "Misure radio — RSSI"
        )

        val usefulMeasurements =
            intervention.measurements
                .filter {
                    it.referenceRssi != null ||
                        it.measuredRssi != null
                }

        if (
            usefulMeasurements.isEmpty()
        ) {

            textLine(
                "Nessuna misura RSSI registrata."
            )

        } else {

            // Intestazione tabella

            ensureSpace(30f)

            val x1 = MARGIN_LEFT
            val x2 = x1 + 215f
            val x3 = x2 + 90f
            val x4 = x3 + 90f
            val x5 = PAGE_W - MARGIN_RIGHT

            val tableHeader =
                RectF(
                    x1,
                    y,
                    x5,
                    y + 24f
                )

            page!!.canvas.drawRect(
                tableHeader,
                Paint().apply {
                    color = lightGray
                    style = Paint.Style.FILL
                }
            )

            page!!.canvas.drawRect(
                tableHeader,
                linePaint
            )

            page!!.canvas.drawText(
                "COLLEGAMENTO",
                x1 + 6f,
                y + 16f,
                boldPaint
            )

            page!!.canvas.drawText(
                "RIF.",
                x2 + 6f,
                y + 16f,
                boldPaint
            )

            page!!.canvas.drawText(
                "MIS.",
                x3 + 6f,
                y + 16f,
                boldPaint
            )

            page!!.canvas.drawText(
                "Δ",
                x4 + 6f,
                y + 16f,
                boldPaint
            )

            y += 24f

            usefulMeasurements
                .forEach { measurement ->

                    ensureSpace(25f)

                    val top =
                        y

                    val bottom =
                        y + 24f

                    page!!.canvas.drawRect(
                        RectF(
                            x1,
                            top,
                            x5,
                            bottom
                        ),
                        linePaint
                    )

                    page!!.canvas.drawLine(
                        x2,
                        top,
                        x2,
                        bottom,
                        linePaint
                    )

                    page!!.canvas.drawLine(
                        x3,
                        top,
                        x3,
                        bottom,
                        linePaint
                    )

                    page!!.canvas.drawLine(
                        x4,
                        top,
                        x4,
                        bottom,
                        linePaint
                    )

                    val ref =
                        measurement
                            .referenceRssi
                            ?.let {
                                "$it dBm"
                            }
                            ?: "—"

                    val measured =
                        measurement
                            .measuredRssi
                            ?.let {
                                "$it dBm"
                            }
                            ?: "—"

                    val delta =
                        measurement
                            .deltaDb
                            ?.let {

                                "${if (it > 0) "+" else ""}$it dB"
                            }
                            ?: "—"

                    val linkName =
                        if (
                            measurement
                                .linkName
                                .length > 31
                        ) {
                            measurement
                                .linkName
                                .take(28) +
                                "..."
                        } else {
                            measurement.linkName
                        }

                    page!!.canvas.drawText(
                        linkName,
                        x1 + 6f,
                        y + 16f,
                        smallPaint
                    )

                    page!!.canvas.drawText(
                        ref,
                        x2 + 6f,
                        y + 16f,
                        smallPaint
                    )

                    page!!.canvas.drawText(
                        measured,
                        x3 + 6f,
                        y + 16f,
                        smallPaint
                    )

                    page!!.canvas.drawText(
                        delta,
                        x4 + 6f,
                        y + 16f,
                        smallPaint
                    )

                    y += 24f
                }
        }

        // =====================================================
        // KAIROS
        // =====================================================

        if (
            intervention.kairosSnapshot != null ||
            intervention
                .kairosAlarmNumbers
                .isNotEmpty()
        ) {

            section(
                "Diagnosi KAIROS"
            )

            intervention
                .kairosSnapshot
                ?.let { k ->

                    k.supplyVoltageV
                        ?.let {

                            infoRow(
                                "Input Supply Voltage",
                                "$it V"
                            )
                        }

                    k.txTemperatureC
                        ?.let {

                            infoRow(
                                "TX Temperature",
                                "$it °C"
                            )
                        }

                    k.forwardPowerW
                        ?.let {

                            infoRow(
                                "Forward Power",
                                "$it W"
                            )
                        }

                    k.reflectedPowerW
                        ?.let {

                            infoRow(
                                "Reflected Power",
                                "$it W"
                            )
                        }

                    k.rssiMainDbm
                        ?.let {

                            infoRow(
                                "RSSI Main",
                                "$it dBm"
                            )
                        }

                    k.rssiDiversityDbm
                        ?.let {

                            infoRow(
                                "RSSI Diversity",
                                "$it dBm"
                            )
                        }

                    if (
                        k.synchronizationSource
                            .isNotBlank()
                    ) {

                        infoRow(
                            "Sincronizzazione",
                            k.synchronizationSource
                        )
                    }
                }

            if (
                intervention
                    .kairosAlarmNumbers
                    .isEmpty()
            ) {

                textLine(
                    "Nessun allarme KAIROS registrato."
                )

            } else {

                intervention
                    .kairosAlarmNumbers
                    .forEach { number ->

                        val alarm =
                            KairosRepository
                                .alarm(number)

                        val guide =
                            KairosRepository
                                .guide(number)

                        if (alarm != null) {

                            ensureSpace(45f)

                            textLine(
                                "Allarme ${alarm.number} — ${alarm.label} [${alarm.severity.label}]",
                                boldPaint
                            )

                            textLine(
                                "Area diagnostica: ${alarm.diagnosticArea}",
                                smallPaint
                            )

                            guide.checks
                                .forEachIndexed {
                                    index,
                                    check ->

                                    val key =
                                        "$number|$index"

                                    val done =
                                        if (
                                            key in
                                            intervention
                                                .kairosCompletedChecks
                                        ) {
                                            "✓"
                                        } else {
                                            "—"
                                        }

                                    textLine(
                                        "$done  $check",
                                        smallPaint,
                                        8f
                                    )
                                }

                            y += 4f
                        }
                    }
            }

            if (
                intervention
                    .kairosDiagnosticNotes
                    .isNotBlank()
            ) {

                paragraphBox(
                    "Note diagnosi",
                    intervention
                        .kairosDiagnosticNotes
                )
            }
        }

        // =====================================================
        // OPERAZIONI
        // =====================================================

        section(
            "Attività eseguite e conclusioni"
        )

        paragraphBox(
            "Anomalia individuata / operazioni effettuate / note",
            intervention.notes
        )

        infoRow(
            "Esito dell'intervento",
            intervention.result
        )

        // =====================================================
        // DOCUMENTAZIONE FOTOGRAFICA
        // =====================================================

        if (
            intervention.photos
                .isNotEmpty()
        ) {

            section(
                "Documentazione fotografica"
            )

            intervention.photos
                .forEachIndexed {
                    index,
                    photo ->

                    val file =
                        File(photo.path)

                    if (file.exists()) {

                        val bitmap =
                            decodeSampledBitmap(
                                file.absolutePath,
                                1200
                            )

                        if (bitmap != null) {

                            val availableW =
                                CONTENT_W - 20f

                            val maxH =
                                255f

                            val ratio =
                                minOf(
                                    availableW /
                                        bitmap.width,
                                    maxH /
                                        bitmap.height
                                )

                            val drawW =
                                bitmap.width *
                                    ratio

                            val drawH =
                                bitmap.height *
                                    ratio

                            ensureSpace(
                                drawH + 55f
                            )

                            val photoTitle =
                                "Foto ${index + 1} — ${photo.category}"

                            textLine(
                                photoTitle,
                                boldPaint
                            )

                            val left =
                                MARGIN_LEFT +
                                    (
                                        CONTENT_W -
                                            drawW
                                        ) /
                                    2f

                            val rect =
                                RectF(
                                    left - 3f,
                                    y - 3f,
                                    left +
                                        drawW +
                                        3f,
                                    y +
                                        drawH +
                                        3f
                                )

                            page!!.canvas.drawRect(
                                rect,
                                linePaint
                            )

                            page!!.canvas.drawBitmap(
                                bitmap,
                                null,
                                RectF(
                                    left,
                                    y,
                                    left + drawW,
                                    y + drawH
                                ),
                                null
                            )

                            y +=
                                drawH +
                                    18f

                            bitmap.recycle()
                        }
                    }
                }
        }

        // =====================================================
        // CHIUSURA PDF
        // =====================================================

        finishPage()

        logoVvf?.recycle()
        logoTlc?.recycle()

        val reportDir =
            File(
                context.getExternalFilesDir(
                    Environment
                        .DIRECTORY_DOCUMENTS
                ),
                "reports"
            ).apply {
                mkdirs()
            }

        val safeSite =
            intervention.siteName.replace(
                Regex(
                    "[^A-Za-z0-9_-]"
                ),
                "_"
            )

        val stamp =
            SimpleDateFormat(
                "yyyyMMdd_HHmm",
                Locale.ITALY
            ).format(
                Date(
                    intervention.timestamp
                )
            )

        val output =
            File(
                reportDir,
                "TLCField_${safeSite}_$stamp.pdf"
            )

        val tempOutput =
            File(
                reportDir,
                ".TLCField_${safeSite}_$stamp.tmp"
            )

        try {
            FileOutputStream(tempOutput).use { stream ->
                pdf.writeTo(stream)
                stream.flush()
            }
        } finally {
            pdf.close()
        }

        if (output.exists()) {
            output.delete()
        }

        if (!tempOutput.renameTo(output)) {
            tempOutput.copyTo(output, overwrite = true)
            tempOutput.delete()
        }

        return output
    }

    private fun loadScaledResourceBitmap(
        context: Context,
        resourceId: Int,
        maxDimension: Int
    ): Bitmap {
        val original = BitmapFactory.decodeResource(
            context.resources,
            resourceId
        ) ?: error("Risorsa grafica non disponibile")

        return scaleBitmap(original, maxDimension)
    }

    private fun decodeSampledBitmap(
        path: String,
        maxDimension: Int
    ): Bitmap? {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, bounds)

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        var sampleSize = 1
        while (
            bounds.outWidth / sampleSize > maxDimension * 2 ||
            bounds.outHeight / sampleSize > maxDimension * 2
        ) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }

        val decoded = BitmapFactory.decodeFile(path, options) ?: return null
        return scaleBitmap(decoded, maxDimension)
    }

    private fun scaleBitmap(
        source: Bitmap,
        maxDimension: Int
    ): Bitmap {
        val largest = maxOf(source.width, source.height)
        if (largest <= maxDimension) {
            return source
        }

        val ratio = maxDimension.toFloat() / largest.toFloat()
        val targetWidth = maxOf(1, (source.width * ratio).toInt())
        val targetHeight = maxOf(1, (source.height * ratio).toInt())
        val scaled = Bitmap.createScaledBitmap(
            source,
            targetWidth,
            targetHeight,
            true
        )

        if (scaled !== source) {
            source.recycle()
        }

        return scaled
    }

    fun share(
        context: Context,
        file: File
    ) {

        val uri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

        val intent =
            Intent(
                Intent.ACTION_SEND
            ).apply {

                type =
                    "application/pdf"

                putExtra(
                    Intent.EXTRA_STREAM,
                    uri
                )

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

        context.startActivity(
            Intent.createChooser(
                intent,
                "Condividi rapporto TLC Field"
            )
        )
    }

    private fun yesNo(
        value: Boolean
    ) =
        if (value) {
            "Sì"
        } else {
            "No"
        }
}
