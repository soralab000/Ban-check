package com.mrsora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrsora.app.data.OutcomeKind
import com.mrsora.app.network.BanCheckApi
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.CheckUi
import com.mrsora.app.ui.components.*
import com.mrsora.app.ui.theme.Fonts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun outcomeColor(k: OutcomeKind): Color = when (k) {
    OutcomeKind.BANNED -> Color(0xFFFF5470)
    OutcomeKind.NOT_BANNED -> Color(0xFF45E0A8)
    OutcomeKind.UNKNOWN -> Color(0xFFE6B450)
    else -> Color(0xFF8A8A96)
}

/** Libellé du résultat RÉEL du moteur (jamais modifié par les messages personnalisés). */
fun kindLabel(k: OutcomeKind) = when (k) {
    OutcomeKind.BANNED -> "BANNI"
    OutcomeKind.NOT_BANNED -> "NON BANNI"
    OutcomeKind.UNKNOWN -> "UNKNOWN / INCONCLUSIF"
    OutcomeKind.ERROR -> "ERREUR"
    OutcomeKind.OFFLINE -> "HORS LIGNE"
}

@Composable
fun BanCheckScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    var number by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val copy = rememberCopy()
    val state = vm.checkState
    val timeFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.FRANCE) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader("BAN CHECK", "Vérifier. Observer. Comprendre.")
        SoraCard {
            SoraLabel("Numéro de téléphone")
            Spacer(Modifier.height(10.dp))
            SoraField(number, { number = it; error = null }, "Numéro", keyboard = KeyboardType.Phone)
            if (error != null) { Spacer(Modifier.height(6.dp)); Body(error!!, dim = true) }
            Spacer(Modifier.height(16.dp))
            SoraButton("Vérifier", {
                error = BanCheckApi.validate(number)
                if (error == null) vm.runCheck(number)
            }, Modifier.fillMaxWidth(), Icons.Outlined.Search, filled = true, enabled = state !is CheckUi.Loading)
            Spacer(Modifier.height(10.dp))
            Body("Le résultat provient directement du service.", dim = true, center = true)
        }
        when (state) {
            CheckUi.Idle -> {}
            CheckUi.Loading -> SoraLoader("Vérification en cours…")
            is CheckUi.Done -> {
                val r = state.result
                val msg = vm.message(r.kind)
                val color = outcomeColor(r.kind)
                val summary = buildString {
                    append("${state.number} — ${kindLabel(r.kind)}\n")
                    r.details.forEach { (k, v) -> append("$k : $v\n") }
                }
                SoraCard {
                    SoraLabel("Résultat de vérification")
                    Spacer(Modifier.height(8.dp))
                    H1(state.number, size = 24)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(10.dp))
                        Text(msg.title.uppercase(), color = color, fontFamily = Fonts.Ui, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Body(msg.message, dim = true)
                    if (msg.sub.isNotBlank()) Body(msg.sub, dim = true)
                    Spacer(Modifier.height(12.dp)); Divider(); Spacer(Modifier.height(4.dp))
                    KeyValueRow("Statut API", kindLabel(r.kind))
                    KeyValueRow("Heure", timeFmt.format(Date(state.at)))
                    if (r.reason != null) KeyValueRow("Détail", r.reason.label)
                    r.details.take(12).forEach { (k, v) -> KeyValueRow(k, v) }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        SoraButton("Copier", { copy(summary) }, Modifier.weight(1f), Icons.Outlined.ContentCopy)
                        SoraButton("Partager", { shareText(ctx, summary) }, Modifier.weight(1f), Icons.Outlined.Share)
                    }
                    Spacer(Modifier.height(10.dp))
                    if (r.kind == OutcomeKind.ERROR || r.kind == OutcomeKind.OFFLINE)
                        SoraButton("Réessayer", { vm.runCheck(state.number) }, Modifier.fillMaxWidth(), Icons.Outlined.Refresh)
                    else SoraButton("Nouveau", { vm.resetCheck(); number = "" }, Modifier.fillMaxWidth(), Icons.Outlined.Refresh)
                }
            }
        }
    }
}
