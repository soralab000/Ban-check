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
        H1(title, size = 24)
        if (subtitle != null) { Spacer(Modifier.height(4.dp)); Text(subtitle, color = LocalPalette.current.dim, fontFamily = Fonts.Title, fontStyle = FontStyle.Italic, fontSize = 15.sp) }
    }
}

// ---------- Cartes / états ----------
@Composable fun SoraCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(14.dp)
    val base = modifier.fillMaxWidth().shadow(4.dp, shape, ambientColor = Color.Black, spotColor = Color.Black).clip(shape)
        .background(p.card.copy(alpha = 0.88f)).border(0.6.dp, p.border, shape)
    Column((if (onClick != null) base.clickable(onClick = onClick) else base).padding(16.dp), content = content)
}

@Composable fun EmptyState(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("⚜︎", color = LocalPalette.current.accent, fontSize = 22.sp)
        Spacer(Modifier.height(10.dp)); Quote(title, center = true)
        Spacer(Modifier.height(4.dp)); Body(subtitle, dim = true, center = true)
    }
}

@Composable fun SoraLoader(text: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = LocalPalette.current.accent, strokeWidth = 1.5.dp, modifier = Modifier.size(36.dp))
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
        Text(text, fontFamily = Fonts.Ui, fontSize = 14.sp, letterSpacing = 0.5.sp)
    }
    if (filled) Button(onClick = click, enabled = enabled, shape = shape, modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.bg), content = inner)
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
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
        visualTransformation = if (password) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
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
@Composable fun ImageBox(path: String?, placeholder: String, modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(14.dp)) {
    val p = LocalPalette.current
    Box(modifier.clip(shape).background(p.card).border(0.6.dp, p.border, shape), contentAlignment = Alignment.Center) {
        if (path != null) AsyncImage(if (path.startsWith("asset:")) Uri.parse("file:///android_asset/" + path.removePrefix("asset:")) else File(path), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
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
