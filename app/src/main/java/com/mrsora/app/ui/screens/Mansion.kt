package com.mrsora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrsora.app.data.Brand
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.components.*
import com.mrsora.app.ui.theme.Fonts
import com.mrsora.app.ui.theme.LocalPalette

@Composable
fun MansionScreen(vm: AppViewModel, go: (String) -> Unit) {
    val p = LocalPalette.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hero : image entière, titre fondu dans le bas de l'image
        Box(Modifier.fillMaxWidth()) {
            FullImage(vm.settings.mansionImage, "IMAGE DU MANOIR", Modifier.fillMaxWidth(), RoundedCornerShape(20.dp))
            Box(Modifier.matchParentSize().clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to p.bg.copy(alpha = 0.96f))))
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                SoraLabel(Brand.SIGNATURE)
                Spacer(Modifier.height(4.dp))
                Text(Brand.NAME, color = p.text, fontFamily = Fonts.Title, fontSize = 36.sp, letterSpacing = 9.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).height(0.8.dp).background(p.accent.copy(alpha = 0.4f)))
            Text("  ⚜︎  ", color = p.accent, fontSize = 16.sp)
            Box(Modifier.weight(1f).height(0.8.dp).background(p.accent.copy(alpha = 0.4f)))
        }
        Quote("« ${Brand.QUOTE} »", center = true, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
        Action("Ban Check", "Vérifier. Observer. Comprendre.", Icons.Outlined.Shield) { go("bancheck") }
        Action("Les Pensées", "Discipline, silence, maîtrise.", Icons.Outlined.FormatQuote) { go("thoughts") }
        Action("La Galerie", "La vitrine du Manoir.", Icons.Outlined.PhotoLibrary) { go("gallery") }
        Action("Mes Liens", "Les espaces officiels.", Icons.Outlined.Link) { go("links") }
        Action("Historique", "Vérifications passées.", Icons.Outlined.History) { go("history") }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Action(title: String, sub: String, icon: ImageVector, onClick: () -> Unit) {
    val p = LocalPalette.current
    val tick = rememberTick()
    SoraCard(onClick = { tick(); onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(CircleShape).background(p.accent.copy(alpha = 0.10f)).border(0.8.dp, p.accent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = p.accent, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title.uppercase(), color = p.text, fontFamily = Fonts.Title, fontSize = 15.sp, letterSpacing = 3.sp)
                Spacer(Modifier.height(2.dp)); Body(sub, dim = true)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = p.dim)
        }
    }
}
