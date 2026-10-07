package com.mrsora.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ImageBox(vm.settings.mansionImage, "IMAGE DU MANOIR", Modifier.fillMaxWidth().height(210.dp))
        Column(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(Brand.NAME, color = p.text, fontFamily = Fonts.Title, fontSize = 40.sp, letterSpacing = 8.sp)
            Spacer(Modifier.height(4.dp))
            Quote(Brand.SIGNATURE, center = true)
            Spacer(Modifier.height(14.dp))
            Body("« ${Brand.QUOTE} »", dim = true, center = true)
        }
        Action("BAN CHECK", "Vérifier. Observer. Comprendre.", Icons.Outlined.Shield) { go("bancheck") }
        Action("LES PENSÉES", "Discipline, silence, maîtrise.", Icons.Outlined.FormatQuote) { go("thoughts") }
        Action("LA GALERIE", "La vitrine du Manoir.", Icons.Outlined.PhotoLibrary) { go("gallery") }
        Action("MES LIENS", "Les espaces officiels.", Icons.Outlined.Link) { go("links") }
        Action("HISTORIQUE", "Vérifications passées.", Icons.Outlined.History) { go("history") }
    }
}

@Composable
private fun Action(title: String, sub: String, icon: ImageVector, onClick: () -> Unit) {
    val tick = rememberTick()
    SoraCard(onClick = { tick(); onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = LocalPalette.current.accent, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(16.dp))
            Column { H2(title); Body(sub, dim = true) }
        }
    }
}
