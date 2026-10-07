package com.mrsora.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mrsora.app.data.MessageSet
import com.mrsora.app.data.OutcomeKind
import com.mrsora.app.data.permForMessage
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.components.*

private fun label(k: OutcomeKind) = when (k) {
    OutcomeKind.BANNED -> "BANNI"; OutcomeKind.NOT_BANNED -> "NON BANNI"; OutcomeKind.UNKNOWN -> "UNKNOWN"
    OutcomeKind.ERROR -> "ERREUR"; OutcomeKind.OFFLINE -> "HORS LIGNE"
}

/** Ces textes ne changent que la PRÉSENTATION ; le résultat réel de l'API reste affiché à part. */
@Composable
fun MessagesScreen(vm: AppViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader("MESSAGES DU CHECK", "Seule la présentation change, jamais la vérité de l'API.")
        OutcomeKind.values().forEach { k -> MessageCard(vm, k) }
        SoraButton("Restaurer toutes les valeurs par défaut", { vm.restoreAllMessages() }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun MessageCard(vm: AppViewModel, k: OutcomeKind) {
    val ctx = LocalContext.current
    val cur = vm.message(k)
    val perm = permForMessage(k)
    val allowed = vm.canEdit(perm)
    var t by remember(cur) { mutableStateOf(cur.title) }
    var m by remember(cur) { mutableStateOf(cur.message) }
    var s by remember(cur) { mutableStateOf(cur.sub) }
    val hasSub = k != OutcomeKind.ERROR && k != OutcomeKind.OFFLINE
    SoraCard {
        Mono(label(k)); Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SoraField(t, { t = it }, "Titre", enabled = allowed)
            SoraField(m, { m = it }, "Message", singleLine = false, enabled = allowed)
            if (hasSub) SoraField(s, { s = it }, "Sous-message (facultatif)", singleLine = false, enabled = allowed)
            if (!allowed) LockedHint(vm.lockLabel(perm))
            else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SoraButton("Enregistrer", { if (vm.saveMessage(k, MessageSet(t.trim(), m.trim(), if (hasSub) s.trim() else ""))) toast(ctx, "Enregistré") else toast(ctx, DENIED) }, filled = true)
                SoraButton("Restaurer", { vm.restoreMessage(k) })
            }
        }
    }
}
