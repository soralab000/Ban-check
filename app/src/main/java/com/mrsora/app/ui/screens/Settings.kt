package com.mrsora.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mrsora.app.data.*
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.components.*
import com.mrsora.app.ui.theme.LocalPalette

@Composable
fun SettingsScreen(vm: AppViewModel, go: (String) -> Unit) {
    val ctx = LocalContext.current
    val p = LocalPalette.current
    var confirmReset by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }
    var confirmHistory by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader("PARAMÈTRES")
        SoraCard { Mono("PERSONNALISATION"); Spacer(Modifier.height(8.dp)); MediaSection(vm, includeVideo = false, includeAudio = false)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SoraButton("Pensées", { go("thoughts") }); SoraButton("Galerie", { go("gallery") }); SoraButton("Thèmes", { go("themes") })
            } }
        SoraCard { Mono("ARRIÈRE-PLAN"); Spacer(Modifier.height(8.dp)); MediaSection(vm, includeImage = false, includeAudio = false) }
        SoraCard { Mono("AUDIO"); Spacer(Modifier.height(8.dp)); MediaSection(vm, includeImage = false, includeVideo = false) }
        SoraCard {
            Mono("BAN CHECK")
            SoraButton("Messages du Check", { go("messages") }, Modifier.fillMaxWidth(), Icons.Outlined.Message)
            SettingSwitch("Sauvegarder l'historique", vm.settings.saveHistory, vm::setSaveHistory)
        }
        SoraCard {
            Mono("INTERFACE")
            val canVis = vm.canEdit(Perm.VISUAL_SETTINGS)
            SettingSwitch("Animations", vm.settings.animations, { vm.setAnimations(it) }, canVis, if (canVis) null else vm.lockLabel(Perm.VISUAL_SETTINGS))
            SettingSwitch("Retour haptique", vm.settings.haptics, { vm.setHaptics(it) }, canVis)
            Body("Taille du texte", dim = true)
            Slider(vm.settings.textScale, { vm.setTextScale(it) }, valueRange = 0.85f..1.3f, enabled = canVis,
                colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.border))
        }
        SoraCard { Mono("LIENS"); Spacer(Modifier.height(6.dp)); Body("Telegram, WhatsApp 01 et 02, site et contact se modifient depuis Mes Liens.", dim = true)
            Spacer(Modifier.height(8.dp)); SoraButton("Ouvrir Mes Liens", { go("links") }, icon = Icons.Outlined.Link) }
        SoraCard {
            Mono("DONNÉES")
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SoraButton("Effacer l'historique", { confirmHistory = true }, Modifier.fillMaxWidth(), Icons.Outlined.Delete)
                SoraButton("Restaurer les paramètres", { confirmRestore = true }, Modifier.fillMaxWidth(), Icons.Outlined.Restore)
                if (vm.canResetApp) SoraButton("Réinitialiser l'application", { confirmReset = true }, Modifier.fillMaxWidth(), Icons.Outlined.RestartAlt)
                else LockedHint("Réinitialisation — réservée à l'administrateur")
            }
        }
        SoraCard { Mono("ADMINISTRATION"); Spacer(Modifier.height(8.dp)); SoraButton("Administration", { go("admin") }, Modifier.fillMaxWidth(), Icons.Outlined.Lock) }
        SoraCard { Mono("À PROPOS"); Spacer(Modifier.height(8.dp)); SoraButton("À propos", { go("about") }, Modifier.fillMaxWidth(), Icons.Outlined.Info) }
    }
    if (confirmHistory) ConfirmDialog("Effacer l'historique ?", "Cette action est définitive.", { vm.clearHistory() }, { confirmHistory = false })
    if (confirmRestore) ConfirmDialog("Restaurer les paramètres ?", "Thème, animations, haptique et audio reviennent aux valeurs par défaut.",
        { if (!vm.restoreUserSettings()) toast(ctx, DENIED) }, { confirmRestore = false })
    if (confirmReset) ConfirmDialog("Réinitialiser l'application ?", "Pensées, galerie, liens, messages, médias, historique et réglages seront effacés.",
        { if (!vm.resetApp()) toast(ctx, DENIED) }, { confirmReset = false })
}

/** Section médias réutilisée dans Paramètres et dans l'espace Admin. */
@Composable
fun MediaSection(vm: AppViewModel, includeImage: Boolean = true, includeVideo: Boolean = true, includeAudio: Boolean = true) {
    val ctx = LocalContext.current
    val p = LocalPalette.current
    val pickImg = rememberImagePicker { if (!vm.importMedia(MediaKind.MANSION_IMAGE, it)) toast(ctx, DENIED) }
    val pickVid = rememberVideoPicker { if (!vm.importMedia(MediaKind.VIDEO, it)) toast(ctx, DENIED) }
    val pickAud = rememberAudioPicker { if (!vm.importMedia(MediaKind.AUDIO, it)) toast(ctx, DENIED) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (includeImage) {
            H2("Image du Manoir")
            ImageBox(vm.settings.mansionImage, "IMAGE DU MANOIR", Modifier.fillMaxWidth().height(130.dp), scale = androidx.compose.ui.layout.ContentScale.Fit)
            MediaButtons(vm, Perm.MANSION_IMAGE, vm.settings.mansionImage.let { it != null && it != DefaultContent.MANSION }, pickImg) { vm.removeMedia(MediaKind.MANSION_IMAGE) }
        }
        if (includeVideo) {
            H2("Vidéo d'arrière-plan")
            Body(if (vm.settings.videoPath.let { it != null && it != DefaultContent.VIDEO }) "Vidéo personnalisée active" else "Vidéo par défaut", dim = true)
            MediaButtons(vm, Perm.MANSION_VIDEO, vm.settings.videoPath.let { it != null && it != DefaultContent.VIDEO }, pickVid) { vm.removeMedia(MediaKind.VIDEO) }
            SettingSwitch("Afficher la vidéo", vm.settings.videoEnabled, vm::setVideoEnabled)
        }
        if (includeAudio) {
            H2("Musique")
            Body(if (vm.settings.audioPath.let { it != null && it != DefaultContent.AUDIO }) "Fichier audio personnalisé" else if (vm.music.available) "Musique par défaut" else "VOTRE MUSIQUE", dim = true)
            MediaButtons(vm, Perm.MANSION_MUSIC, vm.settings.audioPath.let { it != null && it != DefaultContent.AUDIO }, pickAud) { vm.removeMedia(MediaKind.AUDIO) }
            if (vm.music.available) {
                SoraButton(if (vm.music.isPlaying) "Pause" else "Lecture", { vm.music.toggle() }, icon = if (vm.music.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow)
                Body("Volume", dim = true)
                Slider(vm.settings.volume, vm::setVolume, colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.border))
                SettingSwitch("Muet", vm.settings.muted, vm::setMuted)
                SettingSwitch("Lecture en boucle", vm.settings.loopAudio, vm::setLoop)
                SettingSwitch("Lancer la musique à l'entrée du Manoir", vm.settings.autoplayMusic, vm::setAutoplay)
                SettingSwitch("Continuer en arrière-plan", vm.settings.audioInBackground, vm::setAudioBackground)
            }
        }
    }
}

@Composable
private fun MediaButtons(vm: AppViewModel, perm: Perm, has: Boolean, pick: () -> Unit, remove: () -> Unit) {
    if (!vm.canEdit(perm)) { LockedHint(vm.lockLabel(perm)); return }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SoraButton(if (has) "Remplacer" else "Choisir", pick, icon = Icons.Outlined.Image)
        if (has) SoraButton("Retirer (défaut)", remove, icon = Icons.Outlined.Restore)
    }
}
