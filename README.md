# MR SORA — projet Android (Kotlin + Jetpack Compose)

## Compiler
- **GitHub Actions** (recommandé depuis un téléphone) : pousse le dossier sur GitHub, onglet Actions > "Build APK" > télécharge l'artefact `MrSora-debug-apk`.
- **Local** : Java 17 + Android SDK + Gradle 8.7 → `gradle assembleDebug` (APK dans `app/build/outputs/apk/debug/`).
- Tests : `gradle testDebugUnitTest`.

## Tes fichiers
Image du Manoir, vidéo, musique, images des pensées/galerie : tout se choisit dans l'app (Paramètres) et se copie dans le stockage privé. Aucun faux contenu : des placeholders s'affichent tant que rien n'est fourni.

## Ban Check — à vérifier
L'API n'a pas pu être appelée pendant la génération. Le parseur (`network/BanCheckApi.kt`) garde toutes les données brutes et classe en UNKNOWN au moindre doute. Teste un vrai numéro, regarde la réponse dans « Résultat API », puis ajuste uniquement `ResponseParser.classify`.
Attention : les hébergeurs gratuits type *.kesug.com renvoient souvent une page HTML anti-bot aux clients non-navigateur ; l'app affiche alors « Réponse impossible à interpréter ».

## Admin
Au premier accès (Paramètres > Administration) tu crées identifiant + mot de passe : seul un hash PBKDF2 salé est stocké. Sessions de 10 min, blocage 60 s après 5 échecs. Les permissions (admin uniquement / autorisé / verrouillé) sont appliquées dans `AppViewModel`, pas seulement dans l'UI.
Limite : tout est local à l'appareil. Pour contrôler les droits d'autres utilisateurs sur LEURS téléphones, il faudra un serveur.

## Vidéo par défaut
Le workflow GitHub télécharge automatiquement https://files.catbox.moe/2nij2j.mp4 dans `app/src/main/assets/defaults/fond.mp4` avant de compiler.
En local/Termux : `sh fetch_video.sh` puis `gradle assembleDebug`. Sans ce fichier, le fond reste simplement sombre.

## Vidéo et musique par défaut (poids de l'APK)
Dépose tes fichiers directement dans le repo :
- `app/src/main/assets/defaults/fond.mp4` (vidéo d'arrière-plan)
- `app/src/main/assets/defaults/musique.mp3` (musique, lancée à l'entrée du Manoir)
La taille de l'APK = taille de ces fichiers + ~15 Mo d'app. Limite GitHub : 100 Mo par fichier.
