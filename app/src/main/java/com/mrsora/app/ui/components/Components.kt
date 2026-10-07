package com.mrsora.app.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import com.mrsora.app.ui.theme.LocalAnimations
import com.mrsora.app.data.Perm
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.theme.Fonts
import com.mrsora.app.ui.theme.LocalHaptics
import com.mrsora.app.ui.theme.LocalPalette
import java.io.File

// ---------- Texte ----------
@Composable fun H1(text: String, modifier: Modifier = Modifier, size: Int = 28, center: Boolean = false) =
    Text(text, modifier, color = LocalPalette.current.text, fontFamily = Fonts.Title, fontSize = size.sp, letterSpacing = 1.sp,
        textAlign = if (center) TextAlign.Center else null)

@Composable fun H2(text: String, modifier: Modifier = Modifier) =
    Text(text, modifier, color = LocalPalette.current.text, fontFamily = Fonts.Title, fontSize = 18.sp, fontWeight = FontWeight.Medium)

@Composable fun Body(text: String, modifier: Modifier = Modifier, dim: Boolean = false, center: Boolean = false) =
    Text(text, modifier, color = if (dim) LocalPalette.current.dim else LocalPalette.current.text, fontFamily = Fonts.Ui, fontSize = 14.sp,
        lineHeight = 21.sp, textAlign = if (center) TextAlign.Center else null)

@Composable fun Quote(text: String, modifier: Modifier = Modifier, center: Boolean = false) =
    Text(text, modifier, color = LocalPalette.current.text, fontFamily = Fonts.Title, fontStyle = FontStyle.Italic, fontSize = 17.sp,
        lineHeight = 26.sp, textAlign = if (center) TextAlign.Center else null)

@Composable fun Mono(text: String, modifier: Modifier = Modifier, color: Color = LocalPalette.current.dim) =
    Text(text, modifier, color = color, fontFamily = Fonts.Mono, fontSize = 12.sp, lineHeight = 17.sp)

@Composable fun SectionHeader(title: String, subtitle: String? = null) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(title.uppercase(), color = LocalPalette.current.text, fontFamily = Fonts.Title, fontSize = 22.sp, letterSpacing = 4.sp)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.width(44.dp).height(1.5.dp).background(LocalPalette.current.accent))
        if (subtitle != null) { Spacer(Modifier.height(4.dp)); Text(subtitle, color = LocalPalette.current.dim, fontFamily = Fonts.Title, fontStyle = FontStyle.Italic, fontSize = 15.sp) }
    }
}

// ---------- Cartes / états ----------
@Composable fun SoraCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    val anim = LocalAnimations.current
    val shape = RoundedCornerShape(18.dp)
    var shown by remember { mutableStateOf(false) }
    val a by animateFloatAsState(if (shown) 1f else 0f, tween(if (anim) 600 else 0), label = "card")
    LaunchedEffect(Unit) { shown = true }
    val base = modifier.fillMaxWidth()
        .graphicsLayer { alpha = a; translationY = (1f - a) * 28f }
        .shadow(10.dp, shape, ambientColor = Color.Black, spotColor = p.accent.copy(alpha = 0.35f))
        .clip(shape)
        .background(Brush.verticalGradient(listOf(p.card.copy(alpha = 0.78f), p.card.copy(alpha = 0.52f))))
        .border(0.8.dp, Brush.verticalGradient(listOf(p.accent.copy(alpha = 0.45f), p.accent.copy(alpha = 0.08f))), shape)
    Column((if (onClick != null) base.clickable(onClick = onClick) else base).padding(18.dp), content = content)
}

@Composable fun EmptyState(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("⚜︎", color = LocalPalette.current.accent, fontSize = 22.sp)
        Spacer(Modifier.height(10.dp)); Quote(title, center = true)
        Spacer(Modifier.height(4.dp)); Body(subtitle, dim = true, center = true)
    }
}

@Composable fun SoraLoader(text: String) {
    val p = LocalPalette.current
    val t = rememberInfiniteTransition(label = "loader")
    val k by t.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(1800, easing = LinearEasing)), label = "k")
    Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(84.dp)) {
            val maxR = size.minDimension / 2f
            listOf(0f, 0.5f).forEach { off ->
                val q = (k + off) % 1f
                drawCircle(p.accent.copy(alpha = (1f - q) * 0.8f), radius = maxR * (0.25f + 0.75f * q), style = Stroke(width = 1.5.dp.toPx()))
            }
            drawCircle(p.accent, radius = 4.dp.toPx())
        }
        Spacer(Modifier.height(14.dp)); Quote(text, center = true)
    }
}

@Composable fun LockedHint(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(Icons.Outlined.Lock, null, tint = LocalPalette.current.dim, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp)); Text(text, color = LocalPalette.current.dim, fontFamily = Fonts.Ui, fontSize = 12.sp)
    }
}

@Composable fun Divider() = Box(Modifier.fillMaxWidth().height(0.6.dp).background(LocalPalette.current.border))

// ---------- Retour haptique ----------
@Composable fun rememberTick(): () -> Unit {
    val on = LocalHaptics.current
    val h = LocalHapticFeedback.current
    return remember(on, h) { { if (on) h.performHapticFeedback(HapticFeedbackType.LongPress) } }
}

// ---------- Boutons ----------
@Composable fun SoraButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, filled: Boolean = false, enabled: Boolean = true) {
    val p = LocalPalette.current
    val tick = rememberTick()
    val shape = RoundedCornerShape(10.dp)
    val click = { tick(); onClick() }
    val inner: @Composable RowScope.() -> Unit = {
        if (icon != null) { Icon(icon, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text.uppercase(), fontFamily = Fonts.Ui, fontSize = 13.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Medium)
    }
    if (filled) Button(onClick = click, enabled = enabled, shape = shape,
        modifier = modifier.heightIn(min = 52.dp)
            .shadow(if (enabled) 14.dp else 0.dp, shape, ambientColor = p.accent, spotColor = p.accent)
            .background(Brush.horizontalGradient(listOf(p.accent, p.accent.copy(alpha = 0.70f))), shape),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = p.bg,
            disabledContainerColor = Color.Transparent, disabledContentColor = p.bg.copy(alpha = 0.5f)),
        content = inner)
    else OutlinedButton(onClick = click, enabled = enabled, shape = shape, modifier = modifier.heightIn(min = 48.dp),
        border = BorderStroke(0.8.dp, p.accent.copy(alpha = if (enabled) 0.8f else 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = p.accent), content = inner)
}

/** Bouton soumis à une permission : sinon, état verrouillé discret avec cadenas. */
@Composable fun GuardedButton(vm: AppViewModel, perm: Perm, text: String, icon: ImageVector? = null, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (vm.canEdit(perm)) SoraButton(text, onClick, modifier, icon) else LockedHint("$text — ${vm.lockLabel(perm)}")
}

@Composable fun IconAction(icon: ImageVector, desc: String, onClick: () -> Unit, allowed: Boolean = true, lockMsg: String = "") {
    val ctx = LocalContext.current
    val tick = rememberTick()
    IconButton(onClick = { if (allowed) { tick(); onClick() } else toast(ctx, lockMsg) }) {
        Icon(if (allowed) icon else Icons.Outlined.Lock, desc, tint = LocalPalette.current.dim, modifier = Modifier.size(20.dp))
    }
}

// ---------- Champs / dialogues ----------
@Composable fun SoraField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, singleLine: Boolean = true,
                          enabled: Boolean = true, keyboard: KeyboardType = KeyboardType.Text, password: Boolean = false) {
    val p = LocalPalette.current
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = singleLine, enabled = enabled,
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Fonts.Ui, fontSize = 17.sp, letterSpacing = 0.6.sp, color = p.text),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
        visualTransformation = if (password) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = p.card.copy(alpha = 0.45f), unfocusedContainerColor = p.card.copy(alpha = 0.30f),
            focusedBorderColor = p.accent, unfocusedBorderColor = p.border, focusedLabelColor = p.accent, unfocusedLabelColor = p.dim,
            focusedTextColor = p.text, unfocusedTextColor = p.text, cursorColor = p.accent, disabledTextColor = p.dim, disabledBorderColor = p.border)
    )
}

@Composable fun EditorDialog(title: String, onDismiss: () -> Unit, onConfirm: () -> Unit, confirmEnabled: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = p.card, titleContentColor = p.text, textContentColor = p.text,
        title = { H2(title) },
        text = { Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp), content = content) },
        confirmButton = { TextButton(onClick = onConfirm, enabled = confirmEnabled) { Text("Enregistrer", color = if (confirmEnabled) p.accent else p.dim) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler", color = p.dim) } }
    )
}

@Composable fun ConfirmDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val p = LocalPalette.current
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = p.card, titleContentColor = p.text, textContentColor = p.dim,
        title = { H2(title) }, text = { Text(text, fontFamily = Fonts.Ui) },
        confirmButton = { TextButton(onClick = { onConfirm(); onDismiss() }) { Text("Confirmer", color = p.accent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler", color = p.dim) } }
    )
}

@Composable fun SettingSwitch(title: String, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true, hint: String? = null) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Body(title); if (hint != null) LockedHint(hint) }
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = p.accent, checkedThumbColor = p.bg, uncheckedTrackColor = p.card, uncheckedThumbColor = p.dim, uncheckedBorderColor = p.border))
    }
}

// ---------- Images ----------
@Composable fun ImageBox(path: String?, placeholder: String, modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(14.dp), scale: ContentScale = ContentScale.Crop) {
    val p = LocalPalette.current
    Box(modifier.clip(shape).background(p.card).border(0.6.dp, p.border, shape), contentAlignment = Alignment.Center) {
        if (path != null) AsyncImage(imgModel(path), null, Modifier.matchParentSize(), contentScale = scale)
        else Text(placeholder, color = p.dim, fontFamily = Fonts.Mono, fontSize = 12.sp, letterSpacing = 2.sp)
    }
}

@Composable fun rememberImagePicker(onPicked: (Uri) -> Unit): () -> Unit {
    val l = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { it?.let(onPicked) }
    return { l.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}
@Composable fun rememberVideoPicker(onPicked: (Uri) -> Unit): () -> Unit {
    val l = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { it?.let(onPicked) }
    return { l.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }
}
@Composable fun rememberAudioPicker(onPicked: (Uri) -> Unit): () -> Unit {
    val l = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(onPicked) }
    return { l.launch(arrayOf("audio/*")) }
}

// ---------- Actions Android : copier / partager / ouvrir ----------
fun toast(ctx: Context, msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
const val DENIED = "Action non autorisée."

@Composable fun rememberCopy(): (String) -> Unit {
    val cb = LocalClipboardManager.current
    val ctx = LocalContext.current
    return { s -> cb.setText(AnnotatedString(s)); toast(ctx, "Copié") }
}
fun shareText(ctx: Context, text: String) {
    val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
    ctx.startActivity(Intent.createChooser(i, "Partager le lien").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
fun openUrl(ctx: Context, url: String) {
    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (e: ActivityNotFoundException) { toast(ctx, "Aucune application pour ouvrir ce lien.") }
}

internal fun imgModel(path: String): Any =
    if (path.startsWith("asset:")) Uri.parse("file:///android_asset/" + path.removePrefix("asset:")) else File(path)

/** Affiche l'image ENTIÈRE (aucun recadrage) : la hauteur suit le ratio réel de l'image. */
@Composable fun FullImage(path: String?, placeholder: String, modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(14.dp)) {
    val p = LocalPalette.current
    if (path == null) { ImageBox(null, placeholder, modifier.aspectRatio(1.6f), shape); return }
    Box(modifier.clip(shape).border(0.6.dp, p.border, shape).background(p.card)) {
        AsyncImage(imgModel(path), null, Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
    }
}

/** Petit libellé MAJUSCULES espacé (style des sites Sora). */
@Composable fun SoraLabel(text: String, modifier: Modifier = Modifier) =
    Text(text.uppercase(), modifier, color = LocalPalette.current.dim, fontFamily = Fonts.Ui, fontSize = 11.sp, letterSpacing = 2.5.sp)

/** Ligne « LIBELLÉ ........ valeur » pour les détails de résultat. */
@Composable fun KeyValueRow(k: String, v: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
        SoraLabel(k, Modifier.padding(top = 3.dp).widthIn(max = 120.dp))
        Spacer(Modifier.width(12.dp))
        Text(v, Modifier.weight(1f), color = LocalPalette.current.text, fontFamily = Fonts.Ui, fontSize = 14.sp, textAlign = TextAlign.End)
    }
}
