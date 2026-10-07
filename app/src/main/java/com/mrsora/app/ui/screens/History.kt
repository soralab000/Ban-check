package com.mrsora.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(vm: AppViewModel) {
    val copy = rememberCopy()
    var confirmAll by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionHeader("HISTORIQUE", "Ce qui a été observé.") }
        item { SoraCard { SettingSwitch("Sauvegarder l'historique", vm.settings.saveHistory, vm::setSaveHistory) } }
        if (vm.history.isEmpty()) item { EmptyState("Rien n'a encore été consigné.", "Les vérifications apparaîtront ici.") }
        items(vm.history, key = { it.id }) { e ->
            SoraCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        H2(e.number); Mono(fmt.format(Date(e.timestamp)))
                        Mono(e.outcome.name, color = outcomeColor(e.outcome))
                    }
                    IconAction(Icons.Outlined.ContentCopy, "Copier", {
                        copy(buildString { append("${e.number} — ${e.outcome.name} — ${fmt.format(Date(e.timestamp))}\n"); e.details.forEach { (k, v) -> append("$k : $v\n") } })
                    })
                    IconAction(Icons.Outlined.Delete, "Supprimer", { vm.deleteHistory(e.id) })
                }
                if (e.details.isNotEmpty()) { Spacer(Modifier.height(6.dp)); e.details.take(12).forEach { (k, v) -> Mono("$k : $v") } }
            }
        }
        if (vm.history.isNotEmpty()) item { SoraButton("Tout supprimer", { confirmAll = true }, icon = Icons.Outlined.Delete) }
    }
    if (confirmAll) ConfirmDialog("Supprimer tout l'historique ?", "Cette action est définitive.", { vm.clearHistory() }, { confirmAll = false })
}
