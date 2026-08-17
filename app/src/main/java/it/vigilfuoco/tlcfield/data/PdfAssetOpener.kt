package it.vigilfuoco.tlcfield.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object PdfAssetOpener {
    fun open(context: Context, document: TechnicalDocument) {
        try {
            val dir = File(context.cacheDir, "technical_docs").apply { mkdirs() }
            val fileName = document.assetPath.substringAfterLast('/')
            val outFile = File(dir, fileName)

            if (!outFile.exists() || outFile.length() == 0L) {
                context.assets.open(document.assetPath).use { input ->
                    outFile.outputStream().use { output -> input.copyTo(output) }
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Nessuna applicazione PDF disponibile sul dispositivo.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Impossibile aprire il documento: ${e.localizedMessage ?: "errore"}", Toast.LENGTH_LONG).show()
        }
    }
}
