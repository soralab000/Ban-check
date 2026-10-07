package com.mrsora.app.media

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/** Vidéo plein écran, muette, en boucle, recadrée (center-crop) sans débordement. Suit le cycle de vie. */
@Composable
fun VideoBackground(path: String, modifier: Modifier = Modifier) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val holder = remember(path) { VideoHolder(path) }
    DisposableEffect(holder) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_START -> holder.resume()
                Lifecycle.Event.ON_STOP -> holder.pause()
                else -> {}
            }
        }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs); holder.release() }
    }
    AndroidView(factory = { ctx -> TextureView(ctx).also { holder.attach(it) } }, modifier = modifier.fillMaxSize())
}

private class VideoHolder(val path: String) : TextureView.SurfaceTextureListener {
    private var mp: MediaPlayer? = null
    private var surface: Surface? = null
    private var tv: TextureView? = null
    private var prepared = false
    private var wantPlay = false
    private var vw = 0
    private var vh = 0

    fun attach(view: TextureView) {
        tv = view
        view.surfaceTextureListener = this
        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> applyCrop() }
    }

    fun resume() { wantPlay = true; if (prepared) mp?.start() }
    fun pause() { wantPlay = false; if (prepared) runCatching { mp?.pause() } }

    override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
        val p = MediaPlayer()
        mp = p
        try {
            surface = Surface(st)
            if (path.startsWith("asset:")) {
                tv?.context?.assets?.openFd(path.removePrefix("asset:"))?.use { p.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            } else p.setDataSource(path)
            p.setSurface(surface)
            p.isLooping = true
            p.setVolume(0f, 0f)
            p.setOnVideoSizeChangedListener { _, w2, h2 -> vw = w2; vh = h2; applyCrop() }
            p.setOnPreparedListener { prepared = true; if (wantPlay) it.start() }
            p.setOnErrorListener { _, _, _ -> true }
            p.prepareAsync()
        } catch (e: Exception) { releasePlayer() }
    }

    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) = applyCrop()
    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean { releasePlayer(); return true }
    override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}

    private fun applyCrop() {
        val v = tv ?: return
        if (vw == 0 || vh == 0 || v.width == 0 || v.height == 0) return
        val s = maxOf(v.width.toFloat() / vw, v.height.toFloat() / vh)
        val m = Matrix()
        m.setScale(vw * s / v.width, vh * s / v.height, v.width / 2f, v.height / 2f)
        v.setTransform(m)
    }

    private fun releasePlayer() {
        runCatching { mp?.release() }
        runCatching { surface?.release() }
        mp = null; surface = null; prepared = false
    }

    fun release() { wantPlay = false; tv?.surfaceTextureListener = null; releasePlayer() }
}
