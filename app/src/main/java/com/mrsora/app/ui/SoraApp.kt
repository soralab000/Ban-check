package com.mrsora.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.shape.RoundedCornerShape
import com.mrsora.app.ui.components.ImageBox
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mrsora.app.media.VideoBackground
import com.mrsora.app.ui.components.H2
import com.mrsora.app.ui.screens.*
import com.mrsora.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    MANSION("mansion", "Manoir", Icons.Outlined.Home),
    BANCHECK("bancheck", "Ban Check", Icons.Outlined.Shield),
    THOUGHTS("thoughts", "Pensées", Icons.Outlined.FormatQuote),
    GALLERY("gallery", "Galerie", Icons.Outlined.PhotoLibrary),
    LINKS("links", "Mes Liens", Icons.Outlined.Link),
    HISTORY("history", "Historique", Icons.Outlined.History),
    THEMES("themes", "Thèmes", Icons.Outlined.Palette),
    SETTINGS("settings", "Paramètres", Icons.Outlined.Settings),
    ABOUT("about", "À propos", Icons.Outlined.Info)
}

@Composable
fun SoraApp(vm: AppViewModel) {
    val s = vm.settings
    val pal = paletteFor(s.theme)
    SoraTheme(pal, s.textScale, s.haptics, s.animations) {
        var phase by rememberSaveable { mutableIntStateOf(0) } // 0 splash, 1 intro, 2 manoir
        LaunchedEffect(Unit) { while (true) { delay(15_000); vm.refreshSession() } } // expiration de session admin
        Box(Modifier.fillMaxSize().background(pal.bg)) {
            val video = s.videoPath
            if (video != null && s.videoEnabled && phase >= 1) {
                VideoBackground(video)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(pal.bg.copy(alpha = 0.55f), pal.bg.copy(alpha = 0.88f)))))
            }
            if (!(video != null && s.videoEnabled) && phase >= 1) {
                // Atmosphère : image du Manoir très atténuée derrière toute l'interface
                ImageBox(s.mansionImage, "", Modifier.fillMaxSize().alpha(0.16f), RoundedCornerShape(0.dp))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(pal.bg.copy(alpha = 0.35f), pal.bg.copy(alpha = 0.85f)))))
            }
            // Halo d'accent en haut de l'écran
            Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(pal.accent.copy(alpha = 0.16f), Color.Transparent), center = Offset(300f, 0f), radius = 1100f)))
            Crossfade(phase, animationSpec = tween(if (s.animations) 800 else 0), label = "phase") { ph ->
                when (ph) {
                    0 -> SplashScreen { phase = 1 }
                    1 -> IntroScreen(vm) { vm.onEnterManor(); phase = 2 }
                    else -> MainShell(vm)
                }
            }
        }
    }
}

@Composable
private fun MainShell(vm: AppViewModel) {
    val p = LocalPalette.current
    val nav = rememberNavController()
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: Dest.MANSION.route
    val dur = if (LocalAnimations.current) 450 else 0
    val go: (String) -> Unit = { r -> nav.navigate(r) { launchSingleTop = true; popUpTo(Dest.MANSION.route) } }
    BackHandler(drawer.isOpen) { scope.launch { drawer.close() } }

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = p.bg) {
                Spacer(Modifier.height(24.dp))
                Dest.values().forEach { d ->
                    NavigationDrawerItem(
                        label = { Text(d.label) }, icon = { Icon(d.icon, null) }, selected = route == d.route,
                        onClick = { scope.launch { drawer.close() }; go(d.route) },
                        modifier = Modifier.padding(horizontal = 12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = p.card, unselectedContainerColor = Color.Transparent,
                            selectedIconColor = p.accent, unselectedIconColor = p.dim, selectedTextColor = p.text, unselectedTextColor = p.dim)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton({ scope.launch { drawer.open() } }) { Icon(Icons.Outlined.Menu, "Menu", tint = p.text) }
                    val title = Dest.values().firstOrNull { it.route == route }?.label ?: when (route) { "messages" -> "Messages du Check"; "admin" -> "Administration"; else -> "" }
                    Text(title.uppercase(), color = p.text, fontFamily = Fonts.Title, fontSize = 15.sp, letterSpacing = 4.sp, modifier = Modifier.weight(1f))
                    Text("⚜︎", color = p.accent, fontSize = 18.sp, modifier = Modifier.padding(end = 16.dp))
                }
            },
            bottomBar = {
                val item = NavigationBarItemDefaults.colors(
                    selectedIconColor = p.accent, selectedTextColor = p.accent, indicatorColor = p.card,
                    unselectedIconColor = p.dim, unselectedTextColor = p.dim)
                NavigationBar(containerColor = p.bg.copy(alpha = 0.82f), tonalElevation = 0.dp) {
                    listOf(Dest.MANSION, Dest.BANCHECK, Dest.THOUGHTS, Dest.GALLERY, Dest.LINKS).forEach { d ->
                        NavigationBarItem(selected = route == d.route, onClick = { go(d.route) }, colors = item,
                            icon = { Icon(d.icon, d.label) }, label = { Text(d.label, fontSize = 10.sp, maxLines = 1) })
                    }
                    NavigationBarItem(selected = false, onClick = { scope.launch { drawer.open() } }, colors = item,
                        icon = { Icon(Icons.Outlined.Menu, "Plus") }, label = { Text("Plus", fontSize = 10.sp, maxLines = 1) })
                }
            }
        ) { pad ->
            NavHost(nav, Dest.MANSION.route, Modifier.padding(pad),
                enterTransition = { fadeIn(tween(dur)) }, exitTransition = { fadeOut(tween(dur)) },
                popEnterTransition = { fadeIn(tween(dur)) }, popExitTransition = { fadeOut(tween(dur)) }) {
                composable(Dest.MANSION.route) { MansionScreen(vm, go) }
                composable(Dest.BANCHECK.route) { BanCheckScreen(vm) }
                composable(Dest.THOUGHTS.route) { ThoughtsScreen(vm) }
                composable(Dest.GALLERY.route) { GalleryScreen(vm) }
                composable(Dest.LINKS.route) { LinksScreen(vm) }
                composable(Dest.HISTORY.route) { HistoryScreen(vm) }
                composable(Dest.THEMES.route) { ThemesScreen(vm) }
                composable(Dest.SETTINGS.route) { SettingsScreen(vm, go) }
                composable(Dest.ABOUT.route) { AboutScreen() }
                composable("messages") { MessagesScreen(vm) }
                composable("admin") { AdminScreen(vm, go) }
            }
        }
    }
}
