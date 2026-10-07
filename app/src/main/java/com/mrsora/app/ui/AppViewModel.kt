package com.mrsora.app.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mrsora.app.admin.AdminAuth
import com.mrsora.app.data.*
import com.mrsora.app.media.MediaFiles
import com.mrsora.app.media.MusicController
import com.mrsora.app.network.BanCheckApi
import com.mrsora.app.network.CheckResult
import com.mrsora.app.network.Reason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface CheckUi {
    object Idle : CheckUi
    object Loading : CheckUi
    data class Done(val number: String, val result: CheckResult) : CheckUi
}

/**
 * Point central de la logique métier. TOUTE écriture passe par [canEdit] : les permissions sont donc
 * appliquées ici, pas seulement en masquant des boutons.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx: Context = app.applicationContext
    private val store = Store(ctx)
    private val api = BanCheckApi()
    val auth = AdminAuth(ctx)
    val music = MusicController()

    var settings by mutableStateOf(store.loadSettings())
        private set
    var thoughts by mutableStateOf(store.loadThoughts())
        private set
    var gallery by mutableStateOf(store.loadGallery())
        private set
    var links by mutableStateOf(store.loadLinks())
        private set
    var messages by mutableStateOf(store.loadMessages())
        private set
    var history by mutableStateOf(store.loadHistory())
        private set
    var policy by mutableStateOf(store.loadPolicy())
        private set
    var checkState by mutableStateOf<CheckUi>(CheckUi.Idle)
        private set
    var tick by mutableStateOf(0L)
        private set

    init { syncMusic(); music.load(settings.audioPath) }

    // ---------- Permissions / session ----------
    fun refreshSession() { tick = System.currentTimeMillis() }
    val isAdmin: Boolean get() { if (tick < 0L) return false; return auth.sessionActive() }
    fun levelOf(p: Perm): Level = policy[p] ?: Level.USER_ALLOWED
    fun canEdit(p: Perm): Boolean { if (tick < 0L) return false; return auth.sessionActive() || levelOf(p) == Level.USER_ALLOWED }
    fun lockLabel(p: Perm) = if (levelOf(p) == Level.LOCKED) "Fonction verrouillée par l'administrateur" else "Réservée à l'administrateur"
    val canResetApp: Boolean get() = isAdmin || Perm.values().all { levelOf(it) == Level.USER_ALLOWED }
    private fun requireAdmin(): Boolean = auth.sessionActive().also { if (it) auth.touch() }

    fun adminSetup(user: String, pass: String, done: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = withContext(Dispatchers.Default) { auth.setup(user, pass) }; refreshSession(); done(ok)
    }
    fun adminLogin(user: String, pass: String, done: (AdminAuth.Result) -> Unit) = viewModelScope.launch {
        val r = withContext(Dispatchers.Default) { auth.login(user, pass) }; refreshSession(); done(r)
    }
    fun adminLogout() { auth.logout(); refreshSession() }
    fun setLevel(p: Perm, l: Level) {
        if (!requireAdmin()) return
        policy = policy + (p to l); store.savePolicy(policy)
    }
    fun cycleLevel(p: Perm) {
        val all = Level.values(); setLevel(p, all[(levelOf(p).ordinal + 1) % all.size])
    }

    // ---------- Réglages ----------
    private fun update(f: (AppSettings) -> AppSettings) { settings = f(settings); store.saveSettings(settings) }
    private fun syncMusic() { music.loop = settings.loopAudio; music.volume = settings.volume; music.muted = settings.muted }

    fun setTheme(id: String): Boolean { if (!canEdit(Perm.THEME_CHANGE)) return false; update { it.copy(theme = id) }; return true }
    fun setAnimations(v: Boolean): Boolean { if (!canEdit(Perm.VISUAL_SETTINGS)) return false; update { it.copy(animations = v) }; return true }
    fun setHaptics(v: Boolean): Boolean { if (!canEdit(Perm.VISUAL_SETTINGS)) return false; update { it.copy(haptics = v) }; return true }
    fun setTextScale(v: Float): Boolean { if (!canEdit(Perm.VISUAL_SETTINGS)) return false; update { it.copy(textScale = v) }; return true }
    fun setSaveHistory(v: Boolean) = update { it.copy(saveHistory = v) }
    fun setVideoEnabled(v: Boolean) = update { it.copy(videoEnabled = v) }
    fun setVolume(v: Float) { update { it.copy(volume = v) }; music.setVolume(v) }
    fun setMuted(v: Boolean) { update { it.copy(muted = v) }; music.setMuted(v) }
    fun setLoop(v: Boolean) { update { it.copy(loopAudio = v) }; music.setLoop(v) }
    fun setAudioBackground(v: Boolean) = update { it.copy(audioInBackground = v) }

    fun restoreUserSettings(): Boolean {
        if (!(canEdit(Perm.THEME_CHANGE) && canEdit(Perm.VISUAL_SETTINGS))) return false
        resetSettingsKeepingMedia(); return true
    }
    fun restoreDefaultSettingsAdmin(): Boolean { if (!requireAdmin()) return false; resetSettingsKeepingMedia(); return true }
    private fun resetSettingsKeepingMedia() {
        update { AppSettings(mansionImage = it.mansionImage, videoPath = it.videoPath, audioPath = it.audioPath) }; syncMusic()
    }

    // ---------- Médias ----------
    private fun permFor(k: MediaKind) = when (k) {
        MediaKind.MANSION_IMAGE -> Perm.MANSION_IMAGE; MediaKind.VIDEO -> Perm.MANSION_VIDEO; MediaKind.AUDIO -> Perm.MANSION_MUSIC
    }
    private fun pathOf(k: MediaKind) = when (k) {
        MediaKind.MANSION_IMAGE -> settings.mansionImage; MediaKind.VIDEO -> settings.videoPath; MediaKind.AUDIO -> settings.audioPath
    }
    private fun setPath(k: MediaKind, path: String?) = update {
        when (k) {
            MediaKind.MANSION_IMAGE -> it.copy(mansionImage = path)
            MediaKind.VIDEO -> it.copy(videoPath = path)
            MediaKind.AUDIO -> it.copy(audioPath = path)
        }
    }
    fun importMedia(k: MediaKind, uri: Uri): Boolean {
        if (!canEdit(permFor(k))) return false
        val path = MediaFiles.copy(ctx, uri, k.name.lowercase()) ?: return false
        val old = pathOf(k); setPath(k, path); MediaFiles.delete(old)
        if (k == MediaKind.AUDIO) music.load(path)
        return true
    }
    /** « Restaurer par défaut » = retirer le fichier perso : le placeholder élégant revient. */
    fun removeMedia(k: MediaKind): Boolean {
        if (!canEdit(permFor(k))) return false
        val old = pathOf(k); setPath(k, when (k) { MediaKind.MANSION_IMAGE -> DefaultContent.MANSION; MediaKind.VIDEO -> DefaultContent.VIDEO; else -> null }); MediaFiles.delete(old)
        if (k == MediaKind.AUDIO) music.load(null)
        return true
    }

    // ---------- Pensées ----------
    fun saveThought(id: String?, title: String, text: String, cat: String, image: Uri?, removeImage: Boolean): Boolean {
        val ex = thoughts.find { it.id == id }
        if (ex == null) { if (!canEdit(Perm.THOUGHT_ADD)) return false }
        else if ((ex.title != title || ex.text != text || ex.category != cat) && !canEdit(Perm.THOUGHT_TEXT)) return false
        if ((image != null || removeImage) && !canEdit(Perm.THOUGHT_IMAGE)) return false
        if (title.isBlank() || text.isBlank()) return false
        var img = ex?.imagePath
        if (removeImage) { MediaFiles.delete(img); img = null }
        if (image != null) { val n = MediaFiles.copy(ctx, image, "thought") ?: return false; MediaFiles.delete(img); img = n }
        val t = Thought(ex?.id ?: newId(), title.trim(), text.trim(), cat.trim().ifEmpty { "Pensée" }, img)
        thoughts = if (ex == null) thoughts + t else thoughts.map { if (it.id == t.id) t else it }
        store.saveThoughts(thoughts); return true
    }
    fun deleteThought(id: String): Boolean {
        if (!canEdit(Perm.THOUGHT_DELETE)) return false
        thoughts.find { it.id == id }?.let { MediaFiles.delete(it.imagePath) }
        thoughts = thoughts.filterNot { it.id == id }; store.saveThoughts(thoughts); return true
    }
    fun moveThought(id: String, dir: Int) { if (requireAdmin()) { thoughts = move(thoughts, thoughts.indexOfFirst { it.id == id }, dir); store.saveThoughts(thoughts) } }
    fun restoreThoughts(): Boolean {
        if (!requireAdmin()) return false
        thoughts.forEach { MediaFiles.delete(it.imagePath) }; thoughts = DefaultContent.thoughts; store.saveThoughts(thoughts); return true
    }

    // ---------- Galerie ----------
    fun saveGalleryItem(id: String?, title: String, desc: String, image: Uri?): Boolean {
        val ex = gallery.find { it.id == id }
        if (!canEdit(if (ex == null) Perm.GALLERY_ADD else Perm.GALLERY_EDIT)) return false
        if (title.isBlank() || (ex == null && image == null)) return false
        var img = ex?.imagePath
        if (image != null) { val n = MediaFiles.copy(ctx, image, "gallery") ?: return false; MediaFiles.delete(img); img = n }
        val g = GalleryItem(ex?.id ?: newId(), title.trim(), desc.trim(), img)
        gallery = if (ex == null) gallery + g else gallery.map { if (it.id == g.id) g else it }
        store.saveGallery(gallery); return true
    }
    fun deleteGalleryItem(id: String): Boolean {
        if (!canEdit(Perm.GALLERY_DELETE)) return false
        gallery.find { it.id == id }?.let { MediaFiles.delete(it.imagePath) }
        gallery = gallery.filterNot { it.id == id }; store.saveGallery(gallery); return true
    }
    fun moveGallery(id: String, dir: Int) { if (requireAdmin()) { gallery = move(gallery, gallery.indexOfFirst { it.id == id }, dir); store.saveGallery(gallery) } }
    fun clearGallery(): Boolean {
        if (!requireAdmin()) return false
        gallery.forEach { MediaFiles.delete(it.imagePath) }; gallery = emptyList(); store.saveGallery(gallery); return true
    }

    // ---------- Liens ----------
    private fun urlOk(u: String) = u.isBlank() || u.startsWith("https://") || u.startsWith("http://")
    fun saveLink(id: String, name: String, desc: String, url: String, image: Uri?): Boolean {
        val perm = permForLink(id)
        if (perm == null) { if (!requireAdmin()) return false } else if (!canEdit(perm)) return false
        if (name.isBlank() || !urlOk(url.trim())) return false
        val ex = links.find { it.id == id } ?: return false
        var img = ex.imagePath
        if (image != null) { val n = MediaFiles.copy(ctx, image, "link") ?: return false; MediaFiles.delete(img); img = n }
        val l = ex.copy(name = name.trim(), description = desc.trim(), url = url.trim(), imagePath = img)
        links = links.map { if (it.id == id) l else it }; store.saveLinks(links); return true
    }
    fun addLink(name: String, desc: String, url: String, button: String): Boolean {
        if (!requireAdmin() || name.isBlank() || !urlOk(url.trim())) return false
        links = links + LinkItem(newId(), name.trim(), desc.trim(), url.trim(), button.trim().ifEmpty { "Ouvrir" }); store.saveLinks(links); return true
    }
    fun deleteLink(id: String): Boolean {
        if (!requireAdmin()) return false
        links.find { it.id == id }?.let { MediaFiles.delete(it.imagePath) }
        links = links.filterNot { it.id == id }; store.saveLinks(links); return true
    }
    fun restoreLinks(): Boolean {
        if (!requireAdmin()) return false
        links.forEach { MediaFiles.delete(it.imagePath) }; links = DefaultContent.links; store.saveLinks(links); return true
    }

    // ---------- Messages du check (présentation uniquement) ----------
    fun message(k: OutcomeKind): MessageSet = messages[k] ?: DefaultContent.messages.getValue(k)
    fun saveMessage(k: OutcomeKind, m: MessageSet): Boolean {
        if (!canEdit(permForMessage(k)) || m.title.isBlank() || m.message.isBlank()) return false
        messages = messages + (k to m); store.saveMessages(messages); return true
    }
    fun restoreMessage(k: OutcomeKind): Boolean {
        if (!canEdit(permForMessage(k))) return false
        messages = messages + (k to DefaultContent.messages.getValue(k)); store.saveMessages(messages); return true
    }
    fun restoreAllMessages() { OutcomeKind.values().forEach { restoreMessage(it) } }

    // ---------- Ban Check ----------
    private fun isOnline(): Boolean {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    fun runCheck(raw: String) {
        if (checkState is CheckUi.Loading || BanCheckApi.validate(raw) != null) return
        val number = raw.trim()
        checkState = CheckUi.Loading
        viewModelScope.launch {
            val res = if (!isOnline()) CheckResult(OutcomeKind.OFFLINE, Reason.OFFLINE, emptyList()) else api.check(number)
            checkState = CheckUi.Done(number, res)
            if (settings.saveHistory) {
                val det = res.details + (if (res.reason != null) listOf("Statut technique" to res.reason.label) else emptyList())
                history = (listOf(HistoryEntry(newId(), number, res.kind, System.currentTimeMillis(), det)) + history).take(300)
                store.saveHistory(history)
            }
        }
    }
    fun resetCheck() { checkState = CheckUi.Idle }
    fun deleteHistory(id: String) { history = history.filterNot { it.id == id }; store.saveHistory(history) }
    fun clearHistory() { history = emptyList(); store.saveHistory(history) }

    // ---------- Données ----------
    fun resetApp(): Boolean {
        if (!canResetApp) return false
        music.load(null); MediaFiles.deleteAll(ctx); store.clearAllExceptPolicy()
        settings = store.loadSettings(); thoughts = store.loadThoughts(); gallery = store.loadGallery()
        links = store.loadLinks(); messages = store.loadMessages(); history = store.loadHistory(); checkState = CheckUi.Idle
        syncMusic(); return true
    }

    private fun <T> move(l: List<T>, from: Int, dir: Int): List<T> {
        val to = from + dir
        if (from !in l.indices || to !in l.indices) return l
        return l.toMutableList().also { val x = it.removeAt(from); it.add(to, x) }
    }

    override fun onCleared() { music.release() }
}
