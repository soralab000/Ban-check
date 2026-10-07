package com.mrsora.app.admin

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Authentification admin locale. Aucun identifiant n'existe dans le code ni dans l'APK :
 * le propriétaire les crée au premier lancement ; seul un hash PBKDF2 salé est stocké (jamais le mot de passe).
 * Méthodes bloquantes (PBKDF2) : à appeler hors du thread principal.
 */
class AdminAuth(ctx: Context) {
    sealed interface Result {
        object Ok : Result
        object Bad : Result
        object NotConfigured : Result
        data class Locked(val seconds: Int) : Result
    }

    private val prefs = ctx.getSharedPreferences("mrsora_admin", Context.MODE_PRIVATE)
    @Volatile private var sessionUntil = 0L
    private var failures = 0
    private var lockedUntil = 0L

    fun isConfigured() = prefs.getString("hash", null) != null
    fun sessionActive() = System.currentTimeMillis() < sessionUntil
    fun touch() { if (sessionActive()) sessionUntil = System.currentTimeMillis() + SESSION_MS }
    fun logout() { sessionUntil = 0L }

    fun setup(user: String, pass: String): Boolean {
        if (isConfigured() || user.isBlank() || pass.length < 8) return false
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString("salt", b64(salt)).putString("hash", b64(derive(user, pass, salt))).apply()
        sessionUntil = System.currentTimeMillis() + SESSION_MS
        return true
    }

    fun login(user: String, pass: String): Result {
        val hash = prefs.getString("hash", null) ?: return Result.NotConfigured
        val salt = prefs.getString("salt", null) ?: return Result.NotConfigured
        val now = System.currentTimeMillis()
        if (now < lockedUntil) return Result.Locked(((lockedUntil - now) / 1000).toInt() + 1)
        val ok = MessageDigest.isEqual(Base64.decode(hash, Base64.NO_WRAP), derive(user, pass, Base64.decode(salt, Base64.NO_WRAP)))
        if (ok) { failures = 0; sessionUntil = now + SESSION_MS; return Result.Ok }
        if (++failures >= 5) { failures = 0; lockedUntil = now + 60_000 }
        return Result.Bad
    }

    private fun derive(user: String, pass: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec((user.trim().lowercase() + "\u0000" + pass).toCharArray(), salt, 120_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded } finally { spec.clearPassword() }
    }

    private fun b64(b: ByteArray) = Base64.encodeToString(b, Base64.NO_WRAP)

    companion object { const val SESSION_MS = 10 * 60 * 1000L }
}
