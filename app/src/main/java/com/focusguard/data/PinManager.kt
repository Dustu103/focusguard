package com.focusguard.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * AES-256 GCM encrypted PIN storage backed by Android Keystore.
 *
 * PIN hashing: PBKDF2WithHmacSHA256
 *   - 310,000 iterations  (OWASP 2023 recommendation, same as banking apps)
 *   - 256-bit random salt per PIN (stored encrypted)
 *   - App-level pepper mixed in before hashing
 *   - Constant-time comparison to prevent timing attacks
 *
 * Storage: EncryptedSharedPreferences
 *   - Keys:   AES-256 SIV
 *   - Values: AES-256 GCM
 *   - Master key lives in Android Keystore (hardware-backed on supported devices)
 */
class PinManager(private val context: Context) {

    companion object {
        private const val PBKDF2_ITERATIONS = 310_000   // OWASP 2023
        private const val SALT_BYTES        = 32         // 256-bit salt
        private const val OUTPUT_BITS       = 256        // 256-bit derived key
        // App-level pepper — adds a second secret even if DB is dumped
        private const val PEPPER = "FG\$v2_a9K#mZ!qR8_nP4xL_7Bw@"

        private const val KEY_PIN_HASH      = "pin_hash_v2"
        private const val KEY_PIN_SALT      = "pin_salt_v2"
        private const val KEY_REC_HASH      = "rec_hash_v2"
        private const val KEY_REC_SALT      = "rec_salt_v2"
        private const val KEY_SUPPORT_ID    = "support_id"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL   = "lockout_until"
    }

    // AES-256 GCM EncryptedSharedPreferences, master key in Android Keystore
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "focusguard_secure_v2",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun setPin(pin: String) {
        val salt = randomSalt()
        val hash = pbkdf2(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, salt.toHex())
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    fun getLockoutTimeRemaining(): Long {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val now = System.currentTimeMillis()
        return if (lockoutUntil > now) lockoutUntil - now else 0L
    }

    fun getLockoutMessage(): String? {
        val remaining = getLockoutTimeRemaining()
        if (remaining <= 0) return null
        val secs = remaining / 1000
        return when {
            secs > 3600 -> "Too many attempts. Locked for ${secs / 3600} hours."
            secs > 60 -> "Too many attempts. Locked for ${secs / 60} mins."
            else -> "Too many attempts. Locked for $secs seconds."
        }
    }

    fun verifyPin(input: String): Boolean {
        if (getLockoutTimeRemaining() > 0) return false

        val hash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val salt = prefs.getString(KEY_PIN_SALT, null)?.hexToBytes() ?: return false
        
        val isCorrect = constantTimeEquals(hash, pbkdf2(input, salt))
        
        if (isCorrect) {
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .apply()
        } else {
            val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts)
            
            val now = System.currentTimeMillis()
            when {
                attempts >= 10 -> editor.putLong(KEY_LOCKOUT_UNTIL, now + 24 * 60 * 60 * 1000L) // 24 hrs
                attempts >= 6  -> editor.putLong(KEY_LOCKOUT_UNTIL, now + 5 * 60 * 1000L) // 5 mins
                attempts >= 3  -> editor.putLong(KEY_LOCKOUT_UNTIL, now + 30 * 1000L) // 30 secs
            }
            editor.apply()
        }
        return isCorrect
    }

    fun isPinSet(): Boolean = prefs.getString(KEY_PIN_HASH, null) != null

    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .apply()
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        if (!verifyPin(oldPin)) return false
        setPin(newPin)
        return true
    }

    fun setRecoveryKey(key: String) {
        val salt = randomSalt()
        val hash = pbkdf2(key, salt)
        prefs.edit()
            .putString(KEY_REC_SALT, salt.toHex())
            .putString(KEY_REC_HASH, hash)
            .apply()
    }

    fun verifyRecoveryKey(input: String): Boolean {
        val hash = prefs.getString(KEY_REC_HASH, null) ?: return false
        val salt = prefs.getString(KEY_REC_SALT, null)?.hexToBytes() ?: return false
        return constantTimeEquals(hash, pbkdf2(input, salt))
    }

    // ── Support unlock (one-time code derived from support ID) ─────────────────
    fun getSupportId(): String {
        var id = prefs.getString(KEY_SUPPORT_ID, null)
        if (id == null) {
            id = (100_000..999_999).random().toString()
            prefs.edit().putString(KEY_SUPPORT_ID, id).apply()
        }
        return id
    }

    fun verifySupportUnlockCode(input: String): Boolean {
        val supportId = getSupportId()
        val digest    = MessageDigest.getInstance("SHA-256")
        val raw       = digest.digest("$supportId$PEPPER".toByteArray(Charsets.UTF_8))
        // Derive 6-digit code from digest bytes
        var code = ""
        for (b in raw) { code += (b.toInt() and 0xFF).toString(); if (code.length >= 6) break }
        val expected = code.substring(0, 6)
        return if (constantTimeEquals(input, expected)) {
            // One-time use: rotate the support ID immediately
            prefs.edit().putString(KEY_SUPPORT_ID, (100_000..999_999).random().toString()).apply()
            true
        } else false
    }

    // ── Crypto helpers ────────────────────────────────────────────────────────

    /**
     * PBKDF2WithHmacSHA256 — same algorithm banks use for protecting sensitive data.
     * The PEPPER is mixed into the password before derivation so even a stolen
     * encrypted prefs file cannot be brute-forced without the APK's pepper value.
     */
    private fun pbkdf2(input: String, salt: ByteArray): String {
        val pepperedInput = (input + PEPPER).toCharArray()
        val spec = PBEKeySpec(pepperedInput, salt, PBKDF2_ITERATIONS, OUTPUT_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
                .toHex()
        } finally {
            spec.clearPassword()          // wipe char[] from memory immediately
        }
    }

    /** Cryptographically secure random salt */
    private fun randomSalt(): ByteArray = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }

    /** Constant-time equality — prevents timing-based side-channel attacks */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        val aBytes = a.toByteArray(Charsets.UTF_8)
        val bBytes = b.toByteArray(Charsets.UTF_8)
        if (aBytes.size != bBytes.size) return false
        var diff = 0
        for (i in aBytes.indices) diff = diff or (aBytes[i].toInt() xor bBytes[i].toInt())
        return diff == 0
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    private fun String.hexToBytes(): ByteArray {
        check(length % 2 == 0)
        return ByteArray(length / 2) { i ->
            ((this[2 * i].digitToInt(16) shl 4) or this[2 * i + 1].digitToInt(16)).toByte()
        }
    }
}
