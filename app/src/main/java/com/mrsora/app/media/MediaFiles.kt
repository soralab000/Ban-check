package com.mrsora.app.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

/** Copie les fichiers choisis (sélecteur système, sans permission) dans le stockage privé de l'app. */
object MediaFiles {
    fun copy(ctx: Context, uri: Uri, prefix: String): String? = try {
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(ctx.contentResolver.getType(uri)) ?: "bin"
        val dir = File(ctx.filesDir, "media").apply { mkdirs() }
        val f = File(dir, "${prefix}_${System.currentTimeMillis()}.$ext")
        ctx.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { o -> i.copyTo(o) } } ?: error("no stream")
        f.absolutePath
    } catch (e: Exception) { null }

    fun delete(path: String?) { if (path != null) runCatching { File(path).delete() } }
    fun deleteAll(ctx: Context) { runCatching { File(ctx.filesDir, "media").deleteRecursively() } }
}

/** Lecteur audio indépendant de la vidéo. Pause en arrière-plan sauf si l'utilisateur l'autorise. */
class MusicController {
    private var player: MediaPlayer? = null
    private var prepared = false
    private var wantPlay = false
    private var resumeOnForeground = false
    var isPlaying by mutableStateOf(false)
        private set
    var volume = 0.7f
    var muted = false
    var loop = true

    fun load(path: String?) {
        release()
        if (path == null) return
        val mp = MediaPlayer()
        try {
            mp.setDataSource(path)
            mp.isLooping = loop
            mp.setOnPreparedListener { prepared = true; applyVolume(); if (wantPlay) { it.start(); isPlaying = true } }
            mp.setOnCompletionListener { isPlaying = false; wantPlay = false }
            mp.setOnErrorListener { _, _, _ -> release(); true }
            player = mp
            mp.prepareAsync()
        } catch (e: Exception) { runCatching { mp.release() }; player = null }
    }

    fun play() { wantPlay = true; if (prepared) { player?.start(); isPlaying = true } }
    fun pause() { wantPlay = false; if (prepared && player?.isPlaying == true) player?.pause(); isPlaying = false }
    fun toggle() { if (isPlaying) pause() else play() }
    fun setVolume(v: Float) { volume = v; applyVolume() }
    fun setMuted(m: Boolean) { muted = m; applyVolume() }
    fun setLoop(l: Boolean) { loop = l; player?.isLooping = l }
    private fun applyVolume() { val x = if (muted) 0f else volume; player?.setVolume(x, x) }

    fun onStop(keepPlaying: Boolean) { if (!keepPlaying && isPlaying) { resumeOnForeground = true; pause() } }
    fun onStart() { if (resumeOnForeground) { resumeOnForeground = false; play() } }

    fun release() {
        runCatching { player?.release() }
        player = null; prepared = false; wantPlay = false; isPlaying = false
    }
}
