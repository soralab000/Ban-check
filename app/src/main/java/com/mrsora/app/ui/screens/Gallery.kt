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
import com.mrsora.app.data.GalleryItem
import com.mrsora.app.data.Perm
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.components.*

@Composable
fun GalleryScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    var editing by remember { mutableStateOf<GalleryItem?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<GalleryItem?>(null) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionHeader("LA GALERIE DU MANOIR", "Une vitrine silencieuse.") }
        if (vm.gallery.isEmpty()) item { EmptyState("Le Manoir est encore silencieux.", "Ajoutez votre première image.") }
        items(vm.gallery, key = { it.id }) { g ->
            SoraCard {
                ImageBox(g.imagePath, "VOTRE IMAGE", Modifier.fillMaxWidth().height(200.dp))
                Spacer(Modifier.height(10.dp)); H2(g.title)
                if (g.description.isNotBlank()) { Spacer(Modifier.height(4.dp)); Body(g.description, dim = true) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconAction(Icons.Outlined.Edit, "Modifier", { editing = g }, vm.canEdit(Perm.GALLERY_EDIT), vm.lockLabel(Perm.GALLERY_EDIT))
                    IconAction(Icons.Outlined.Delete, "Supprimer", { deleting = g }, vm.canEdit(Perm.GALLERY_DELETE), vm.lockLabel(Perm.GALLERY_DELETE))
                    if (vm.isAdmin) {
                        IconAction(Icons.Outlined.ArrowUpward, "Monter", { vm.moveGallery(g.id, -1) })
                        IconAction(Icons.Outlined.ArrowDownward, "Descendre", { vm.moveGallery(g.id, 1) })
                    }
                }
            }
        }
        item { GuardedButton(vm, Perm.GALLERY_ADD, "Ajouter une image", Icons.Outlined.Add) { adding = true } }
    }
    if (adding) GalleryEditor(vm, null) { adding = false }
    editing?.let { GalleryEditor(vm, it) { editing = null } }
    deleting?.let { d -> ConfirmDialog("Supprimer cet élément ?", d.title, { if (!vm.deleteGalleryItem(d.id)) toast(ctx, DENIED) }, { deleting = null }) }
}

@Composable
private fun GalleryEditor(vm: AppViewModel, initial: GalleryItem?, onClose: () -> Unit) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var uri by remember { mutableStateOf<android.net.Uri?>(null) }
    val pick = rememberImagePicker { uri = it }
    EditorDialog(if (initial == null) "Nouvel élément" else "Modifier l'élément", onClose, {
        if (vm.saveGalleryItem(initial?.id, title, desc, uri)) onClose() else toast(ctx, DENIED)
    }, title.isNotBlank() && (initial != null || uri != null)) {
        SoraField(title, { title = it }, "Titre")
        SoraField(desc, { desc = it }, "Description (facultative)", singleLine = false)
        Body(if (uri != null) "Image sélectionnée" else if (initial != null) "Image actuelle conservée" else "Une image est requise", dim = true)
        SoraButton(if (initial != null || uri != null) "Remplacer l'image" else "Choisir une image", pick, icon = Icons.Outlined.Image)
    }
}
