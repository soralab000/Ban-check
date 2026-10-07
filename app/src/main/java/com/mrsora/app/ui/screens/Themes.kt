package com.mrsora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mrsora.app.data.Perm
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.components.*
import com.mrsora.app.ui.theme.Palettes

@Composable
fun ThemesScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionHeader("THÈMES", "Une seule ambiance à la fois.") }
        items(Palettes, key = { it.id }) { t ->
            SoraCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(t.bg, t.card, t.accent, t.text).forEach { c -> Swatch(c, t.border) }
                }
                Spacer(Modifier.height(10.dp)); H2(t.id); Body(t.desc, dim = true); Spacer(Modifier.height(10.dp))
                if (vm.canEdit(Perm.THEME_CHANGE)) {
                    SoraButton(if (vm.settings.theme == t.id) "Thème actuel" else "Appliquer", { if (!vm.setTheme(t.id)) toast(ctx, DENIED) },
                        enabled = vm.settings.theme != t.id)
                } else LockedHint(vm.lockLabel(Perm.THEME_CHANGE))
            }
        }
    }
}

@Composable
private fun Swatch(c: Color, border: Color) =
    Box(Modifier.size(34.dp).clip(CircleShape).background(c).border(0.8.dp, border, CircleShape))
