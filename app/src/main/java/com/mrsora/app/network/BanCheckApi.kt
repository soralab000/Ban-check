package com.mrsora.app.network

import com.mrsora.app.data.OutcomeKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

enum class Reason(val label: String) {
    NETWORK("Échec de connexion au service."),
    TIMEOUT("Le service n'a pas répondu dans le délai imparti."),
    EMPTY("Le service a renvoyé une réponse vide."),
    UNREADABLE("Réponse impossible à interpréter."),
    HTTP("Le service a répondu par une erreur HTTP."),
    OFFLINE("Aucune connexion Internet.")
}

/** [details] = uniquement des données réellement renvoyées par l'API. Rien n'est inventé. */
data class CheckResult(val kind: OutcomeKind, val reason: Reason?, val details: List<Pair<String, String>>)

class BanCheckApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).callTimeout(20, TimeUnit.SECONDS).build()

    suspend fun check(number: String): CheckResult = withContext(Dispatchers.IO) {
        // Le numéro est envoyé tel que saisi (trim uniquement), comme demandé. Le format attendu par l'API
        // (avec/sans « + », indicatif) n'est pas documenté : à confirmer avec une vraie réponse.
        val url = HttpUrl.Builder().scheme("https").host(HOST).addPathSegment("bancheck.php")
            .addQueryParameter("numéro", number).build()
        val req = Request.Builder().url(url).header("Accept", "application/json, text/plain, */*").build()
        try {
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) CheckResult(OutcomeKind.ERROR, Reason.HTTP, listOf("HTTP" to resp.code.toString()))
                else ResponseParser.parse(body)
            }
        } catch (e: SocketTimeoutException) {
            CheckResult(OutcomeKind.ERROR, Reason.TIMEOUT, emptyList())
        } catch (e: InterruptedIOException) {
            CheckResult(OutcomeKind.ERROR, Reason.TIMEOUT, emptyList())
        } catch (e: IOException) {
            CheckResult(OutcomeKind.ERROR, Reason.NETWORK, emptyList())
        }
    }

    companion object {
        const val HOST = "banchek-by-awais.kesug.com"
        private val FORMAT = Regex("^\\+?[0-9 ().-]{5,20}$")
        /** Retourne un message d'erreur, ou null si l'entrée est acceptable. */
        fun validate(raw: String): String? {
            val n = raw.trim()
            if (n.isEmpty()) return "Saisissez un numéro."
            if (!FORMAT.matches(n) || n.count { it.isDigit() } < 5) return "Format de numéro invalide."
            return null
        }
    }
}

/**
 * Parseur tolérant. La structure exacte de l'API n'ayant pas pu être inspectée, TOUT est gardé comme détail brut
 * et la classification est volontairement prudente : au moindre doute ou conflit => UNKNOWN.
 * Une fois une vraie réponse observée, ajuster uniquement [classify].
 */
object ResponseParser {
    private val STATUS_KEYS = setOf("status", "result", "state", "message", "msg", "response", "verdict", "ban_status", "account_status")
    private val NEGATIVE = listOf("not banned", "non banni", "unbanned", "not_banned", "no ban", "not ban", "pas banni", "clean")

    fun parse(body: String): CheckResult {
        val t = body.trim().removePrefix("\uFEFF")
        if (t.isEmpty()) return CheckResult(OutcomeKind.ERROR, Reason.EMPTY, emptyList())
        if (t.startsWith("<")) return CheckResult(OutcomeKind.ERROR, Reason.UNREADABLE, emptyList()) // page HTML / défi anti-bot
        val details = mutableListOf<Pair<String, String>>()
        if (t.startsWith("{") || t.startsWith("[")) {
            val node: Any = try { if (t.startsWith("{")) JSONObject(t) else JSONArray(t) }
            catch (e: Exception) { return CheckResult(OutcomeKind.ERROR, Reason.UNREADABLE, emptyList()) }
            flatten("", node, details)
        } else {
            details += "message" to t.take(500)
        }
        return CheckResult(classify(details), null, details)
    }

    private fun flatten(prefix: String, node: Any?, out: MutableList<Pair<String, String>>) {
        if (out.size >= 60) return
        when (node) {
            is JSONObject -> node.keys().forEach { k -> flatten(if (prefix.isEmpty()) k else "$prefix.$k", node.opt(k), out) }
            is JSONArray -> for (i in 0 until node.length()) flatten("$prefix[$i]", node.opt(i), out)
            else -> out += (prefix.ifEmpty { "valeur" }) to node.toString()
        }
    }

    internal fun classify(details: List<Pair<String, String>>): OutcomeKind {
        val votes = mutableSetOf<OutcomeKind>()
        for ((k, v) in details) {
            val key = k.substringAfterLast('.').lowercase()
            val value = v.trim().lowercase()
            if ("ban" in key) {
                when (value) {
                    "true", "yes", "1", "oui" -> votes += OutcomeKind.BANNED
                    "false", "no", "0", "non" -> votes += OutcomeKind.NOT_BANNED
                    else -> textVote(value)?.let { votes += it }
                }
            } else if (key in STATUS_KEYS) {
                textVote(value)?.let { votes += it }
            }
        }
        return if (votes.size == 1) votes.first() else OutcomeKind.UNKNOWN
    }

    private fun textVote(v: String): OutcomeKind? {
        if (NEGATIVE.any { it in v }) return OutcomeKind.NOT_BANNED
        if ("banned" in v || "banni" in v || v == "ban") return OutcomeKind.BANNED
        return null
    }
}
