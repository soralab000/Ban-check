package com.mrsora.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.mrsora.app.data.Brand
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.components.*
import com.mrsora.app.ui.theme.Fonts
import com.mrsora.app.ui.theme.LocalAnimations
import com.mrsora.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val p = LocalPalette.current
    val anim = LocalAnimations.current
    var shown by remember { mutableStateOf(false) }
    val a by animateFloatAsState(if (shown) 1f else 0f, tween(if (anim) 900 else 0), label = "splash")
    LaunchedEffect(Unit) { shown = true; delay(if (anim) 1900L else 600L); onDone() }
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(Modifier.alpha(a), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(Brand.NAME, color = p.text, fontFamily = Fonts.Title, fontSize = 38.sp, letterSpacing = 8.sp)
            Spacer(Modifier.height(10.dp))
            Text(Brand.SIGNATURE, color = p.dim, fontFamily = Fonts.Title, fontStyle = FontStyle.Italic, fontSize = 15.sp)
        }
    }
}

@Composable
fun IntroScreen(vm: AppViewModel, onEnter: () -> Unit) {
    val p = LocalPalette.current
    val anim = LocalAnimations.current
    var shown by remember { mutableStateOf(false) }
    val a by animateFloatAsState(if (shown) 1f else 0f, tween(if (anim) 2200 else 0), label = "fade")
    val zoom by animateFloatAsState(if (shown) 1.0f else 0.96f, tween(if (anim) 2600 else 0, easing = LinearEasing), label = "zoom")
    LaunchedEffect(Unit) { shown = true }
    val hasVideo = vm.settings.videoPath != null && vm.settings.videoEnabled
    Box(Modifier.fillMaxSize()) {
        if (!hasVideo) Box(Modifier.fillMaxSize().graphicsLayer { alpha = a * 0.35f }) {
            ImageBox(vm.settings.mansionImage, "", Modifier.fillMaxSize(), RoundedCornerShape(0.dp))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, p.bg.copy(alpha = 0.5f), p.bg.copy(alpha = 0.92f)))))
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            ImageBox(vm.settings.mansionImage, "IMAGE DU MANOIR",
                Modifier.weight(1f, fill = false).fillMaxWidth().aspectRatio(1f).graphicsLayer { scaleX = zoom; scaleY = zoom; alpha = a },
                RoundedCornerShape(18.dp), androidx.compose.ui.layout.ContentScale.Fit)
            Column(Modifier.alpha(a), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(22.dp))
                Text("BIENVENUE DANS LE", color = p.accent, fontFamily = Fonts.Ui, fontSize = 11.sp, letterSpacing = 4.sp)
                Spacer(Modifier.height(8.dp))
                Text("MANOIR", color = p.text, fontFamily = Fonts.Title, fontSize = 40.sp, letterSpacing = 8.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text("DE SORA", color = p.accent, fontFamily = Fonts.Title, fontSize = 40.sp, letterSpacing = 8.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.width(120.dp).height(1.5.dp).background(p.accent))
                Spacer(Modifier.height(12.dp))
                Quote("« Entrez. Ici, le silence parle. »", center = true)
                Spacer(Modifier.height(22.dp))
                SoraButton("Entrer dans le Manoir", onEnter, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
