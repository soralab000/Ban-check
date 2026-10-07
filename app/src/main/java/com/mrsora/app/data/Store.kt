package com.mrsora.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Sérialisation JSON tolérante (org.json, fourni par Android). */
object Codec {
    private fun JSONObject.str(k: String): String? = if (has(k) && !isNull(k)) getString(k) else null
    private fun <T> parse(s: String, f: (JSONObject) -> T): List<T> = runCatching {
        val a = JSONArray(s); (0 until a.length()).map { f(a.getJSONObject(it)) }
    }.getOrDefault(emptyList())

    fun thoughtsToJson(l: List<Thought>): String = JSONArray().also { a ->
        l.forEach { a.put(JSONObject().put("id", it.id).put("t", it.title).put("x", it.text).put("c", it.category).put("i", it.imagePath ?: JSONObject.NULL)) }
    }.toString()
    fun thoughtsFromJson(s: String) = parse(s) { Thought(it.getString("id"), it.getString("t"), it.getString("x"), it.getString("c"), it.str("i")) }

    fun galleryToJson(l: List<GalleryItem>): String = JSONArray().also { a ->
        l.forEach { a.put(JSONObject().put("id", it.id).put("t", it.title).put("d", it.description).put("i", it.imagePath ?: JSONObject.NULL)) }
    }.toString()
    fun galleryFromJson(s: String) = parse(s) { GalleryItem(it.getString("id"), it.getString("t"), it.getString("d"), it.str("i")) }

    fun linksToJson(l: List<LinkItem>): String = JSONArray().also { a ->
        l.forEach { a.put(JSONObject().put("id", it.id).put("n", it.name).put("d", it.description).put("u", it.url).put("b", it.button).put("i", it.imagePath ?: JSONObject.NULL)) }
    }.toString()
    fun linksFromJson(s: String) = parse(s) { LinkItem(it.getString("id"), it.getString("n"), it.getString("d"), it.getString("u"), it.getString("b"), it.str("i")) }

    fun historyToJson(l: List<HistoryEntry>): String = JSONArray().also { a ->
        l.forEach { e ->
            val det = JSONArray().also { d -> e.details.forEach { (k, v) -> d.put(JSONArray().put(k).put(v)) } }
            a.put(JSONObject().put("id", e.id).put("n", e.number).put("o", e.outcome.name).put("ts", e.timestamp).put("d", det))
        }
    }.toString()
    fun historyFromJson(s: String) = parse(s) { o ->
        val d = o.getJSONArray("d")
        HistoryEntry(o.getString("id"), o.getString("n"), OutcomeKind.valueOf(o.getString("o")), o.getLong("ts"),
            (0 until d.length()).map { d.getJSONArray(it).let { p -> p.getString(0) to p.getString(1) } })
    }

    fun messagesToJson(m: Map<OutcomeKind, MessageSet>): String = JSONObject().also { o ->
        m.forEach { (k, v) -> o.put(k.name, JSONObject().put("t", v.title).put("m", v.message).put("s", v.sub)) }
    }.toString()
    fun messagesFromJson(s: String): Map<OutcomeKind, MessageSet> = runCatching {
        val o = JSONObject(s)
        OutcomeKind.values().mapNotNull { k ->
            o.optJSONObject(k.name)?.let { k to MessageSet(it.optString("t"), it.optString("m"), it.optString("s")) }
        }.toMap()
    }.getOrDefault(emptyMap())

    fun policyToJson(m: Map<Perm, Level>): String = JSONObject().also { o -> m.forEach { (k, v) -> o.put(k.name, v.name) } }.toString()
    fun policyFromJson(s: String): Map<Perm, Level> = runCatching {
        val o = JSONObject(s)
        Perm.values().mapNotNull { p -> o.optString(p.name).takeIf { it.isNotEmpty() }?.let { p to Level.valueOf(it) } }.toMap()
    }.getOrDefault(emptyMap())
}

/** Stockage local persistant (SharedPreferences privées à l'app). */
class Store(ctx: Context) {
    private val p = ctx.getSharedPreferences("mrsora_data", Context.MODE_PRIVATE)

    fun loadSettings() = AppSettings(
        theme = p.getString("theme", "OBSIDIAN") ?: "OBSIDIAN",
        animations = p.getBoolean("animations", true),
        haptics = p.getBoolean("haptics", true),
        saveHistory = p.getBoolean("saveHistory", true),
        textScale = p.getFloat("textScale", 1f),
        mansionImage = p.getString("mansionImage", DefaultContent.MANSION),
        videoPath = p.getString("videoPath", DefaultContent.VIDEO),
        videoEnabled = p.getBoolean("videoEnabled", true),
        audioPath = p.getString("audioPath", DefaultContent.AUDIO),
        volume = p.getFloat("volume", 0.7f),
        muted = p.getBoolean("muted", false),
        loopAudio = p.getBoolean("loopAudio", true),
        audioInBackground = p.getBoolean("audioBg", false),
        autoplayMusic = p.getBoolean("autoplay", true)
    )

    fun saveSettings(s: AppSettings) {
        p.edit().putString("theme", s.theme).putBoolean("animations", s.animations).putBoolean("haptics", s.haptics)
            .putBoolean("saveHistory", s.saveHistory).putFloat("textScale", s.textScale)
            .putString("mansionImage", s.mansionImage).putString("videoPath", s.videoPath).putBoolean("videoEnabled", s.videoEnabled)
            .putString("audioPath", s.audioPath).putFloat("volume", s.volume).putBoolean("muted", s.muted)
            .putBoolean("loopAudio", s.loopAudio).putBoolean("audioBg", s.audioInBackground).putBoolean("autoplay", s.autoplayMusic).apply()
    }

    fun loadThoughts() = p.getString("thoughts", null)?.let(Codec::thoughtsFromJson) ?: DefaultContent.thoughts
    fun saveThoughts(l: List<Thought>) = p.edit().putString("thoughts", Codec.thoughtsToJson(l)).apply()
    fun loadGallery() = p.getString("gallery", null)?.let(Codec::galleryFromJson) ?: emptyList()
    fun saveGallery(l: List<GalleryItem>) = p.edit().putString("gallery", Codec.galleryToJson(l)).apply()
    fun loadLinks() = p.getString("links", null)?.let(Codec::linksFromJson) ?: DefaultContent.links
    fun saveLinks(l: List<LinkItem>) = p.edit().putString("links", Codec.linksToJson(l)).apply()
    fun loadHistory() = p.getString("history", null)?.let(Codec::historyFromJson) ?: emptyList()
    fun saveHistory(l: List<HistoryEntry>) = p.edit().putString("history", Codec.historyToJson(l)).apply()
    fun loadMessages() = DefaultContent.messages + (p.getString("messages", null)?.let(Codec::messagesFromJson) ?: emptyMap())
    fun saveMessages(m: Map<OutcomeKind, MessageSet>) = p.edit().putString("messages", Codec.messagesToJson(m)).apply()
    fun loadPolicy() = p.getString("policy", null)?.let(Codec::policyFromJson) ?: emptyMap()
    fun savePolicy(m: Map<Perm, Level>) = p.edit().putString("policy", Codec.policyToJson(m)).apply()

    /** Efface tout sauf la politique de permissions (sinon un utilisateur contournerait les verrous). */
    fun clearAllExceptPolicy() {
        val pol = p.getString("policy", null)
        p.edit().clear().also { if (pol != null) it.putString("policy", pol) }.apply()
    }
}
