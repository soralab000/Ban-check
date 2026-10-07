package com.mrsora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.SoraApp

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SoraApp(vm) }
    }

    // Cycle de vie : la musique se met en pause en arrière-plan (sauf réglage contraire) et reprend au retour.
    override fun onStop() { super.onStop(); vm.music.onStop(vm.settings.audioInBackground) }
    override fun onStart() { super.onStart(); vm.music.onStart() }
}
