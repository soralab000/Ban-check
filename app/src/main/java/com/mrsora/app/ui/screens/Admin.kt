package com.mrsora.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.mrsora.app.admin.AdminAuth
import com.mrsora.app.data.Perm
import com.mrsora.app.ui.AppViewModel
import com.mrsora.app.ui.components.*
import com.mrsora.app.ui.theme.LocalPalette

@Composable
fun AdminScreen(vm: AppViewModel, go: (String) -> Unit) {
    when {
        !vm.auth.isConfigured() -> AdminSetup(vm)
        !vm.isAdmin -> AdminLogin(vm)
        else -> AdminDashboard(vm, go)
    }
}

@Composable
private fun AdminSetup(vm: AppViewModel) {
    val ctx = LocalContext.current
    var user by remember { mutableStateOf("") }; var pass by remember { mutableStateOf("") }; var pass2 by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("ADMINISTRATION DU MANOIR", "Première configuration")
        Body("Créez les identifiants du propriétaire. Seul un hash salé est conservé sur l'appareil : le mot de passe n'est jamais stocké ni affiché.", dim = true)
        SoraField(user, { user = it }, "Identifiant")
        SoraField(pass, { pass = it }, "Mot de passe (8 caractères min.)", password = true)
        SoraField(pass2, { pass2 = it }, "Confirmer le mot de passe", password = true)
        SoraButton("Créer l'accès admin", {
            if (pass != pass2) toast(ctx, "Les mots de passe diffèrent.")
            else vm.adminSetup(user, pass) { ok -> if (!ok) toast(ctx, "Identifiant ou mot de passe invalide.") }
        }, Modifier.fillMaxWidth(), filled = true, enabled = user.isNotBlank() && pass.length >= 8)
    }
}

@Composable
private fun AdminLogin(vm: AppViewModel) {
    val ctx = LocalContext.current
    var user by rememberSaveable { mutableStateOf("") }; var pass by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("ADMINISTRATION DU MANOIR", "Accès protégé")
        SoraField(user, { user = it }, "Identifiant")
        SoraField(pass, { pass = it }, "Mot de passe", password = true)
        SoraButton("Se connecter", {
            vm.adminLogin(user, pass) { r ->
                pass = ""
                when (r) {
                    AdminAuth.Result.Ok -> {}
                    is AdminAuth.Result.Locked -> toast(ctx, "Trop d'essais. Réessayez dans ${r.seconds} s.")
                    else -> toast(ctx, "Identifiants incorrects.")
                }
            }
        }, Modifier.fillMaxWidth(), Icons.Outlined.Lock, filled = true, enabled = user.isNotBlank() && pass.isNotEmpty())
    }
}

@Composable
private fun AdminDashboard(vm: AppViewModel, go: (String) -> Unit) {
    val ctx = LocalContext.current
    val p = LocalPalette.current
    var confirm by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader("ADMIN DASHBOARD", "Mode administrateur — session de 10 minutes")
        SoraButton("Se déconnecter", { vm.adminLogout() }, Modifier.fillMaxWidth(), Icons.Outlined.Logout)

        SoraCard {
            Mono("PERMISSIONS UTILISATEUR")
            Body("Touchez un niveau pour le changer : Utilisateur autorisé → Admin uniquement → Verrouillé.", dim = true)
            Perm.values().groupBy { it.group }.forEach { (g, list) ->
                Spacer(Modifier.height(10.dp)); H2(g)
                list.forEach { perm ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Body(perm.label) }
                        TextButton({ vm.cycleLevel(perm) }) { Text(vm.levelOf(perm).label, color = p.accent) }
                    }
                }
            }
        }
        SoraCard {
            Mono("CONTENU")
            Body("En session admin, vous ajoutez, modifiez, supprimez et réordonnez directement dans les pages concernées.", dim = true)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SoraButton("Pensées", { go("thoughts") }); SoraButton("Galerie", { go("gallery") }); SoraButton("Liens", { go("links") }) }
            Spacer(Modifier.height(8.dp)); SoraButton("Messages du Check", { go("messages") }, icon = Icons.Outlined.Message)
        }
        SoraCard { Mono("MÉDIAS"); Spacer(Modifier.height(8.dp)); MediaSection(vm) }
        SoraCard {
            Mono("RESTAURATION")
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SoraButton("Restaurer les paramètres par défaut", { confirm = "Restaurer les paramètres ?" to { vm.restoreDefaultSettingsAdmin() } }, Modifier.fillMaxWidth())
                SoraButton("Restaurer les messages par défaut", { confirm = "Restaurer les messages ?" to { vm.restoreAllMessages() } }, Modifier.fillMaxWidth())
                SoraButton("Restaurer les liens par défaut", { confirm = "Restaurer les liens ?" to { vm.restoreLinks() } }, Modifier.fillMaxWidth())
                SoraButton("Restaurer le contenu initial", { confirm = "Restaurer pensées et vider la galerie ?" to { vm.restoreThoughts(); vm.clearGallery() } }, Modifier.fillMaxWidth())
            }
        }
    }
    confirm?.let { (t, a) -> ConfirmDialog(t, "Cette opération est destructive.", { a(); toast(ctx, "Fait") }, { confirm = null }) }
}
