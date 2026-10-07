package com.mrsora.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density

data class SoraPalette(
    val id: String, val desc: String,
    val bg: Color, val card: Color, val accent: Color, val text: Color, val dim: Color, val border: Color
)

val Palettes = listOf(
    SoraPalette("OBSIDIAN", "Noir profond, élégant.", Color(0xFF0A0A0C), Color(0xFF141418), Color(0xFFC9C9D4), Color(0xFFECECF1), Color(0xFF8A8A96), Color(0xFF26262E)),
    SoraPalette("ROYAL", "Ambiance noble et luxueuse.", Color(0xFF0C0A18), Color(0xFF17132B), Color(0xFFC9A86A), Color(0xFFF0EBFA), Color(0xFF9189AE), Color(0xFF2D2548)),
    SoraPalette("CRIMSON", "Rouge sombre et mystérieux.", Color(0xFF0C0607), Color(0xFF1A0C0F), Color(0xFFB83A4B), Color(0xFFF2E8E9), Color(0xFF9A7C80), Color(0xFF3A1A20)),
    SoraPalette("GOLD", "Noir et détails dorés.", Color(0xFF090806), Color(0xFF14110C), Color(0xFFD4AF37), Color(0xFFF3EBD8), Color(0xFF9A907A), Color(0xFF332B1A)),
    SoraPalette("MIDNIGHT", "Bleu nuit extrêmement discret.", Color(0xFF060912), Color(0xFF0D1424), Color(0xFF6F8FD8), Color(0xFFE4EAF7), Color(0xFF7C88A3), Color(0xFF1C2744)),
    SoraPalette("SHADOW", "Minimaliste, sombre et mystérieux.", Color(0xFF050505), Color(0xFF0E0E0E), Color(0xFF9A9A9A), Color(0xFFDADADA), Color(0xFF6E6E6E), Color(0xFF1E1E1E))
)

fun paletteFor(id: String) = Palettes.firstOrNull { it.id == id } ?: Palettes.first()

val LocalPalette = staticCompositionLocalOf { Palettes.first() }
val LocalHaptics = staticCompositionLocalOf { true }
val LocalAnimations = staticCompositionLocalOf { true }

/** Titres : serif ; interface : sans-serif ; informations techniques : monospace. */
object Fonts {
    val Title = FontFamily.Serif
    val Ui = FontFamily.SansSerif
    val Mono = FontFamily.Monospace
}

@Composable
fun SoraTheme(p: SoraPalette, textScale: Float, haptics: Boolean, animations: Boolean, content: @Composable () -> Unit) {
    val d = LocalDensity.current
    val scheme = darkColorScheme(
        primary = p.accent, onPrimary = p.bg, secondary = p.accent, background = p.bg, onBackground = p.text,
        surface = p.card, onSurface = p.text, surfaceVariant = p.card, onSurfaceVariant = p.dim, outline = p.border
    )
    CompositionLocalProvider(
        LocalPalette provides p, LocalHaptics provides haptics, LocalAnimations provides animations,
        LocalDensity provides Density(d.density, d.fontScale * textScale)
    ) { MaterialTheme(colorScheme = scheme, content = content) }
}
