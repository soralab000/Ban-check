package com.mrsora.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mrsora.app.data.Perm
import com.mrsora.app.data.Thought
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.components.*
import com.mrsora.app.ui.theme.LocalPalette

private fun roman(n: Int): String {
    val v = listOf(1000 to "M", 900 to "CM", 500 to "D", 400 to "CD", 100 to "C", 90 to "XC", 50 to "L", 40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I")
    var x = n; val sb = StringBuilder()
    for ((k, s) in v) while (x >= k) { sb.append(s); x -= k }
    return sb.toString()
}

@Composable
fun ThoughtsScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val copy = rememberCopy()
    var editing by remember { mutableStateOf<Thought?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Thought?>(null) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionHeader("LES PENSÉES DE SORA", "Calme, froid, profond.") }
        item { SoraCard { Quote("« Seul le travail me permet de m'éloigner de la douleur. »") } }
        if (vm.thoughts.isEmpty()) item { EmptyState("Aucune pensée n'a encore été déposée ici.", "Ajoutez la première.") }
        itemsIndexed(vm.thoughts, key = { _, t -> t.id }) { i, t ->
            SoraCard {
                if (t.imagePath != null) { ImageBox(t.imagePath, "VOTRE IMAGE", Modifier.fillMaxWidth().height(170.dp)); Spacer(Modifier.height(12.dp)) }
                Mono("PENSÉE ${roman(i + 1)} · ${t.category.uppercase()}")
                Spacer(Modifier.height(6.dp)); H2(t.title); Spacer(Modifier.height(6.dp)); Quote(t.text)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconAction(Icons.Outlined.ContentCopy, "Copier", { copy("${t.title}\n${t.text}") })
                    val canEdit = vm.canEdit(Perm.THOUGHT_TEXT) || vm.canEdit(Perm.THOUGHT_IMAGE)
                    IconAction(Icons.Outlined.Edit, "Modifier", { editing = t }, canEdit, vm.lockLabel(Perm.THOUGHT_TEXT))
                    IconAction(Icons.Outlined.Delete, "Supprimer", { deleting = t }, vm.canEdit(Perm.THOUGHT_DELETE), vm.lockLabel(Perm.THOUGHT_DELETE))
                    if (vm.isAdmin) {
                        IconAction(Icons.Outlined.ArrowUpward, "Monter", { vm.moveThought(t.id, -1) })
                        IconAction(Icons.Outlined.ArrowDownward, "Descendre", { vm.moveThought(t.id, 1) })
                    }
                }
            }
        }
        item { GuardedButton(vm, Perm.THOUGHT_ADD, "Ajouter une pensée", Icons.Outlined.Add) { adding = true } }
        if (vm.isAdmin) item { var c by remember { mutableStateOf(false) }
            SoraButton("Restaurer le contenu initial", { c = true }, icon = Icons.Outlined.Restore)
            if (c) ConfirmDialog("Restaurer les pensées ?", "Vos pensées actuelles seront remplacées par le contenu initial.", { vm.restoreThoughts() }, { c = false }) }
    }
    if (adding) ThoughtEditor(vm, null) { adding = false }
    editing?.let { ThoughtEditor(vm, it) { editing = null } }
    deleting?.let { d -> ConfirmDialog("Supprimer cette pensée ?", d.title, { if (!vm.deleteThought(d.id)) toast(ctx, DENIED) }, { deleting = null }) }
}

@Composable
private fun ThoughtEditor(vm: AppViewModel, initial: Thought?, onClose: () -> Unit) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var text by remember { mutableStateOf(initial?.text ?: "") }
    var cat by remember { mutableStateOf(initial?.category ?: "") }
    var uri by remember { mutableStateOf<android.net.Uri?>(null) }
    var removeImg by remember { mutableStateOf(false) }
    val pick = rememberImagePicker { uri = it; removeImg = false }
    val textOk = initial == null || vm.canEdit(Perm.THOUGHT_TEXT)
    EditorDialog(if (initial == null) "Nouvelle pensée" else "Modifier la pensée", onClose, {
        if (vm.saveThought(initial?.id, title, text, cat, uri, removeImg)) onClose() else toast(ctx, DENIED)
    }, title.isNotBlank() && text.isNotBlank()) {
        SoraField(title, { title = it }, "Titre", enabled = textOk)
        SoraField(cat, { cat = it }, "Catégorie", enabled = textOk)
        SoraField(text, { text = it }, "Texte", singleLine = false, enabled = textOk)
        if (!textOk) LockedHint(vm.lockLabel(Perm.THOUGHT_TEXT))
        val shown = if (removeImg) null else (initial?.imagePath)
        Body(if (uri != null) "Nouvelle image sélectionnée" else if (shown != null) "Image actuelle conservée" else "Aucune image", dim = true)
        if (vm.canEdit(Perm.THOUGHT_IMAGE)) {
            SoraButton(if (shown != null || uri != null) "Remplacer l'image" else "Choisir une image", pick, icon = Icons.Outlined.Image)
            if (shown != null || uri != null) SoraButton("Retirer l'image", { uri = null; removeImg = true }, icon = Icons.Outlined.Delete)
        } else LockedHint(vm.lockLabel(Perm.THOUGHT_IMAGE))
    }
}
