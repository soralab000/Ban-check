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
    val zoom by animateFloatAsState(if (shown) 1.07f else 1f, tween(if (anim) 16000 else 0, easing = LinearEasing), label = "zoom")
    LaunchedEffect(Unit) { shown = true }
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().graphicsLayer { scaleX = zoom; scaleY = zoom; alpha = a }) {
            ImageBox(vm.settings.mansionImage, "IMAGE DU MANOIR", Modifier.fillMaxSize(), RoundedCornerShape(0.dp))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, p.bg.copy(alpha = 0.55f), p.bg.copy(alpha = 0.96f)))))
        Column(Modifier.align(Alignment.BottomCenter).padding(28.dp).navigationBarsPadding().alpha(a), horizontalAlignment = Alignment.CenterHorizontally) {
            H1("Bienvenue dans le Manoir de Sora", center = true, size = 30)
            Spacer(Modifier.height(10.dp))
            Quote("« Entrez. Ici, le silence parle. »", center = true)
            Spacer(Modifier.height(28.dp))
            SoraButton("Entrer", onEnter, filled = true, modifier = Modifier.fillMaxWidth())
        }
    }
}
