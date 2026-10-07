#!/bin/sh
# Télécharge la vidéo d'arrière-plan par défaut (à lancer une fois avant de compiler en local / Termux).
curl -fL --retry 3 -o app/src/main/assets/defaults/fond.mp4 https://files.catbox.moe/2nij2j.mp4
