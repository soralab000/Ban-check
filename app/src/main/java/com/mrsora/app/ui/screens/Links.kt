package com.mrsora.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mrsora.app.data.Brand
import com.mrsora.app.data.LinkItem
import com.mrsora.app.data.permForLink
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.components.*

@Composable
fun LinksScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val copy = rememberCopy()
    var editing by remember { mutableStateOf<LinkItem?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<LinkItem?>(null) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionHeader("MES LIENS", "COMMUNITY — les espaces officiels.") }
        item { SoraCard { Mono("IDENTITÉ"); Spacer(Modifier.height(6.dp)); H2(Brand.UNIVERSE); Spacer(Modifier.height(4.dp)); Body("${Brand.NAME} — ${Brand.SIGNATURE}", dim = true) } }
        items(vm.links, key = { it.id }) { l ->
            SoraCard {
                if (l.imagePath != null) { ImageBox(l.imagePath, "VOTRE IMAGE", Modifier.fillMaxWidth().height(120.dp)); Spacer(Modifier.height(10.dp)) }
                H2(l.name)
                if (l.description.isNotBlank()) Body(l.description, dim = true)
                if (l.url.isNotBlank()) { Spacer(Modifier.height(4.dp)); Mono(l.url) }
                Spacer(Modifier.height(10.dp))
                if (l.url.isNotBlank()) SoraButton(l.button.ifBlank { "Ouvrir" }, { openUrl(ctx, l.url) }, Modifier.fillMaxWidth(), Icons.Outlined.OpenInNew)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconAction(Icons.Outlined.ContentCopy, "Copier", { copy(l.url.ifBlank { l.name }) })
                    if (l.url.isNotBlank()) IconAction(Icons.Outlined.Share, "Partager le lien", { shareText(ctx, l.url) })
                    val perm = permForLink(l.id)
                    val allowed = if (perm == null) vm.isAdmin else vm.canEdit(perm)
                    IconAction(Icons.Outlined.Edit, "Modifier", { editing = l }, allowed, if (perm == null) "Réservée à l'administrateur" else vm.lockLabel(perm))
                    if (vm.isAdmin) IconAction(Icons.Outlined.Delete, "Supprimer", { deleting = l })
                }
            }
        }
        if (vm.isAdmin) item { SoraButton("Ajouter un lien", { adding = true }, icon = Icons.Outlined.Add) }
    }
    if (adding) AddLinkDialog(vm) { adding = false }
    editing?.let { LinkEditor(vm, it) { editing = null } }
    deleting?.let { d -> ConfirmDialog("Supprimer ce lien ?", d.name, { vm.deleteLink(d.id) }, { deleting = null }) }
}

@Composable
private fun LinkEditor(vm: AppViewModel, l: LinkItem, onClose: () -> Unit) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf(l.name) }
    var desc by remember { mutableStateOf(l.description) }
    var url by remember { mutableStateOf(l.url) }
    var uri by remember { mutableStateOf<android.net.Uri?>(null) }
    val pick = rememberImagePicker { uri = it }
    EditorDialog("Modifier le lien", onClose, { if (vm.saveLink(l.id, name, desc, url, uri)) onClose() else toast(ctx, "Lien invalide ou action non autorisée.") }, name.isNotBlank()) {
        SoraField(name, { name = it }, "Nom"); SoraField(desc, { desc = it }, "Description")
        SoraField(url, { url = it }, "URL (https://…)")
        SoraButton(if (uri != null || l.imagePath != null) "Remplacer l'image" else "Ajouter une image", pick, icon = Icons.Outlined.Image)
    }
}

@Composable
private fun AddLinkDialog(vm: AppViewModel, onClose: () -> Unit) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf("") }; var desc by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }; var btn by remember { mutableStateOf("Ouvrir") }
    EditorDialog("Nouveau lien", onClose, { if (vm.addLink(name, desc, url, btn)) onClose() else toast(ctx, "Lien invalide.") }, name.isNotBlank()) {
        SoraField(name, { name = it }, "Nom"); SoraField(desc, { desc = it }, "Description")
        SoraField(url, { url = it }, "URL (https://…)"); SoraField(btn, { btn = it }, "Texte du bouton")
    }
}
