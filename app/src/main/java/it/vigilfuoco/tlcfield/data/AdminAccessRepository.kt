package it.vigilfuoco.tlcfield.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

object AdminAccessRepository {
    private const val PREFS = "tlc_field_admin"
    private const val KEY_PIN_HASH = "admin_pin_hash"
    private const val KEY_AUDIT = "deletion_audit"

    data class Result(
        val ok: Boolean,
        val message: String
    )

    fun isConfigured(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PIN_HASH, null)
            .isNullOrBlank()
            .not()

    fun verifyPin(context: Context, pin: String): Boolean {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PIN_HASH, null)
            ?: return false
        return stored == hash(pin)
    }

    fun configure(
        context: Context,
        currentPin: String,
        newPin: String,
        confirmPin: String
    ): Result {
        if (newPin.length < 4 || newPin.any { !it.isDigit() }) {
            return Result(false, "Il PIN amministratore deve contenere almeno 4 cifre")
        }
        if (newPin != confirmPin) {
            return Result(false, "I nuovi PIN non coincidono")
        }
        if (isConfigured(context) && !verifyPin(context, currentPin)) {
            return Result(false, "PIN amministratore attuale non corretto")
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PIN_HASH, hash(newPin))
            .apply()

        return Result(true, "PIN amministratore configurato")
    }

    fun recordDeletion(
        context: Context,
        intervention: Intervention
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = runCatching {
            JSONArray(prefs.getString(KEY_AUDIT, "[]") ?: "[]")
        }.getOrDefault(JSONArray())

        existing.put(
            JSONObject().apply {
                put("interventionId", intervention.id)
                put("siteId", intervention.siteId)
                put("siteName", intervention.siteName)
                put("interventionTimestamp", intervention.timestamp)
                put("deletedAt", System.currentTimeMillis())
                put("role", "admin")
            }
        )

        prefs.edit().putString(KEY_AUDIT, existing.toString()).apply()
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
