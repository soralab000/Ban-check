package com.mrsora.app.data

import java.util.UUID

object Brand {
    const val NAME = "MR SORA"
    const val SIGNATURE = "Un des Héritiers du M"
    const val UNIVERSE = "𓆩⚜︎ 𝐒𝐎𝐑𝐀 × 𝐑𝐀𝐌𝐒𝐄𝐒 𝐋𝐀𝐁𝐒 • 𝐃𝐄𝐕 • 𝐋𝐔𝐗𝐔𝐑𝐘 ⚜︎𓆪"
    const val QUOTE = "Je préfère construire dans l'ombre et laisser le résultat parler."
    const val PERSONAL = "Seul le travail me permet de m'éloigner de la douleur."
}

data class Thought(val id: String, val title: String, val text: String, val category: String, val imagePath: String? = null)
data class GalleryItem(val id: String, val title: String, val description: String, val imagePath: String?)
data class LinkItem(val id: String, val name: String, val description: String, val url: String, val button: String, val imagePath: String? = null)
data class HistoryEntry(val id: String, val number: String, val outcome: OutcomeKind, val timestamp: Long, val details: List<Pair<String, String>>)
data class MessageSet(val title: String, val message: String, val sub: String = "")

/** Résultat RÉEL du moteur. La personnalisation ne change jamais cette valeur, seulement le texte affiché. */
enum class OutcomeKind { BANNED, NOT_BANNED, UNKNOWN, ERROR, OFFLINE }
enum class MediaKind { MANSION_IMAGE, VIDEO, AUDIO }

enum class Level(val label: String) {
    USER_ALLOWED("Utilisateur autorisé"),
    ADMIN_ONLY("Admin uniquement"),
    LOCKED("Verrouillé")
}

/** Toutes les permissions éditables. Ajouter une entrée ici suffit : l'écran admin la liste automatiquement. */
enum class Perm(val group: String, val label: String) {
    MANSION_IMAGE("Manoir", "Modifier l'image du Manoir"),
    MANSION_VIDEO("Manoir", "Modifier la vidéo"),
    MANSION_MUSIC("Manoir", "Modifier la musique"),
    THOUGHT_TEXT("Pensées", "Modifier le texte"),
    THOUGHT_ADD("Pensées", "Ajouter une pensée"),
    THOUGHT_DELETE("Pensées", "Supprimer une pensée"),
    THOUGHT_IMAGE("Pensées", "Modifier les images"),
    GALLERY_ADD("Galerie", "Ajouter une image"),
    GALLERY_EDIT("Galerie", "Modifier une image"),
    GALLERY_DELETE("Galerie", "Supprimer une image"),
    MSG_BANNED("Ban Check", "Modifier le message « Banni »"),
    MSG_NOT_BANNED("Ban Check", "Modifier le message « Non banni »"),
    MSG_UNKNOWN("Ban Check", "Modifier le message « UNKNOWN »"),
    MSG_ERROR("Ban Check", "Modifier le message d'erreur"),
    LINK_TELEGRAM("Liens", "Modifier Telegram"),
    LINK_WA1("Liens", "Modifier WhatsApp 01"),
    LINK_WA2("Liens", "Modifier WhatsApp 02"),
    LINK_SITE("Liens", "Modifier le site"),
    LINK_CONTACT("Liens", "Modifier le contact"),
    THEME_CHANGE("Apparence", "Changer de thème"),
    VISUAL_SETTINGS("Apparence", "Modifier les paramètres visuels")
}

fun permForLink(id: String): Perm? = when (id) {
    "telegram" -> Perm.LINK_TELEGRAM
    "wa1" -> Perm.LINK_WA1
    "wa2" -> Perm.LINK_WA2
    "site" -> Perm.LINK_SITE
    "contact" -> Perm.LINK_CONTACT
    else -> null // liens ajoutés par l'admin : modifiables par l'admin uniquement
}

fun permForMessage(k: OutcomeKind): Perm = when (k) {
    OutcomeKind.BANNED -> Perm.MSG_BANNED
    OutcomeKind.NOT_BANNED -> Perm.MSG_NOT_BANNED
    OutcomeKind.UNKNOWN -> Perm.MSG_UNKNOWN
    OutcomeKind.ERROR, OutcomeKind.OFFLINE -> Perm.MSG_ERROR
}

data class AppSettings(
    val theme: String = "OBSIDIAN",
    val animations: Boolean = true,
    val haptics: Boolean = true,
    val saveHistory: Boolean = true,
    val textScale: Float = 1f,
    val mansionImage: String? = DefaultContent.MANSION,
    val videoPath: String? = DefaultContent.VIDEO,
    val videoEnabled: Boolean = true,
    val audioPath: String? = DefaultContent.AUDIO,
    val volume: Float = 0.7f,
    val muted: Boolean = false,
    val loopAudio: Boolean = true,
    val audioInBackground: Boolean = false,
    val autoplayMusic: Boolean = true
)

fun newId(): String = UUID.randomUUID().toString()

object DefaultContent {
    const val AUDIO = "asset:defaults/musique.mp3"
    const val VIDEO = "asset:defaults/fond.mp4"
    const val MANSION = "asset:defaults/manoir.jpg"
    private fun img(n: String) = "asset:defaults/$n.jpg"
    val thoughts = listOf(
        Thought("t1", "Discipline", "La discipline n'a pas de public. Elle se mesure aux heures données à l'ouvrage quand personne ne viendra les compter.", "Discipline", img("discipline")),
        Thought("t2", "Silence", "Celui qui progresse n'a pas besoin de l'annoncer. Le silence n'est pas une absence : c'est de l'énergie que l'on refuse de disperser.", "Silence", img("silence")),
        Thought("t3", "Force", "La force n'est pas le bruit que l'on impose aux autres. C'est le calme que l'on conserve lorsque tout s'effondre autour de soi.", "Force", img("force")),
        Thought("t4", "Échec", "Un échec est une information. Il ne dit pas ce que l'on vaut, seulement ce qu'il reste à corriger.", "Échec", img("echec")),
        Thought("t5", "Solitude", "Apprendre à avancer sans dépendre du regard des autres, c'est découvrir que la direction se trouvait déjà en soi.", "Solitude", img("solitude")),
        Thought("t6", "Élégance", "La sophistication n'est pas l'extravagance. L'élégance retire ce qui est inutile jusqu'à ce que seul l'essentiel demeure.", "Élégance", img("elegance")),
        Thought("t7", "Travail", "Que le résultat parle avant l'ego. Ce qui est bien construit n'a jamais besoin d'être défendu.", "Travail", img("travail")),
        Thought("t8", "Ombre", "Travailler dans l'ombre, c'est construire sans chercher l'attention. La lumière finit toujours par trouver ce qui est solide.", "Ombre", img("ombre"))
    )
    val links = listOf(
        LinkItem("telegram", "IZANA DEV HOUSE", "Canal Telegram officiel", "https://t.me/IZANADEVHOUSE", "Rejoindre le canal"),
        LinkItem("wa1", "—͟͟͞͞𝐈𝐙𝐀𝐍𝐀 ͟͟͞͞𝐃𝐄𝐕 ͟͟͞͞𝐇𝐎𝐔𝐒𝐄 🥷", "Chaîne WhatsApp 01", "https://whatsapp.com/channel/0029VbBwNeSBA1f6aRR0HE2G", "Suivre la chaîne"),
        LinkItem("wa2", Brand.UNIVERSE, "Chaîne WhatsApp 02", "https://whatsapp.com/channel/0029VbDIzIRCnA7wOIaJea0z", "Suivre la chaîne"),
        LinkItem("site", "BAN CHECK — WEB", "Version web du Ban Check", "https://ban-check-lord-sora-x-izana.netlify.app/#check", "Ouvrir le site"),
        LinkItem("contact", "@johnDking", "Contact / créateur", "", "")
    )
    val messages: Map<OutcomeKind, MessageSet> = mapOf(
        OutcomeKind.BANNED to MessageSet("Compte restreint", "Ce numéro est signalé comme banni.", "Résultat transmis par le service."),
        OutcomeKind.NOT_BANNED to MessageSet("Aucune restriction", "Ce numéro n'est pas signalé comme banni.", "Résultat transmis par le service."),
        OutcomeKind.UNKNOWN to MessageSet("Résultat inconclusif", "Le service n'a pas permis de conclure.", "Aucune conclusion ne doit être tirée."),
        OutcomeKind.ERROR to MessageSet("Connexion interrompue", "Impossible d'obtenir une réponse du service pour le moment."),
        OutcomeKind.OFFLINE to MessageSet("Hors ligne", "Aucune connexion détectée. Vérifiez votre réseau puis réessayez.")
    )
}
