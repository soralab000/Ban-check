package com.mrsora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mrsora.app.data.OutcomeKind
import com.mrsora.app.network.BanCheckApi
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.CheckUi
import com.mrsora.app.ui.components.*
import com.mrsora.app.ui.theme.LocalPalette

fun outcomeColor(k: OutcomeKind): Color = when (k) {
    OutcomeKind.BANNED -> Color(0xFFB5485A)
    OutcomeKind.NOT_BANNED -> Color(0xFF5E9E7E)
    OutcomeKind.UNKNOWN -> Color(0xFFC2A15B)
    else -> Color(0xFF8A8A96)
}

@Composable
fun BanCheckScreen(vm: AppViewModel) {
    var number by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val copy = rememberCopy()
    val state = vm.checkState
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader("BAN CHECK", "Vérifier. Observer. Comprendre.")
        SoraField(number, { number = it; error = null }, "Numéro", keyboard = KeyboardType.Phone)
        if (error != null) Body(error!!, dim = true)
        SoraButton("Vérifier", {
            error = BanCheckApi.validate(number)
            if (error == null) vm.runCheck(number)
        }, Modifier.fillMaxWidth(), Icons.Outlined.Search, filled = true, enabled = state !is CheckUi.Loading)
        when (state) {
            CheckUi.Idle -> {}
            CheckUi.Loading -> SoraLoader("Vérification en cours…")
            is CheckUi.Done -> {
                val r = state.result
                val msg = vm.message(r.kind)
                SoraCard {
                    Box(Modifier.fillMaxWidth().height(2.dp).background(outcomeColor(r.kind)))
                    Spacer(Modifier.height(12.dp))
                    H1(msg.title, size = 22)
                    Spacer(Modifier.height(6.dp))
                    Body(msg.message)
                    if (msg.sub.isNotBlank()) { Spacer(Modifier.height(4.dp)); Body(msg.sub, dim = true) }
                    // Vérité du moteur, indépendante des textes personnalisés
                    Spacer(Modifier.height(12.dp))
                    Mono("Numéro : ${state.number}")
                    Mono("Résultat API : ${r.kind.name}", color = outcomeColor(r.kind))
                    if (r.reason != null) Mono(r.reason.label)
                    r.details.forEach { (k, v) -> Mono("$k : $v") }
                    Spacer(Modifier.height(12.dp))
                    Divider()
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SoraButton("Copier", {
                            copy(buildString {
                                append("${state.number} — ${r.kind.name}\n"); r.details.forEach { (k, v) -> append("$k : $v\n") }
                            })
                        }, icon = Icons.Outlined.ContentCopy)
                        if (r.kind == OutcomeKind.ERROR || r.kind == OutcomeKind.OFFLINE)
                            SoraButton("Réessayer", { vm.runCheck(state.number) }, icon = Icons.Outlined.Refresh)
                    }
                }
            }
        }
    }
}
