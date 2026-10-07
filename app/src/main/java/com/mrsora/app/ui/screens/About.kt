package com.mrsora.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrsora.app.data.Brand
import com.mrsora.app.network.BanCheckApi
import com.mrsora.app.ui.components.*

@Composable
fun AboutScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Spacer(Modifier.height(20.dp))
        H1(Brand.NAME, size = 38, center = true)
        Quote(Brand.SIGNATURE, center = true)
        H2(Brand.UNIVERSE)
        SoraCard {
            Body("Projet personnel de développement et de création.")
            Spacer(Modifier.height(10.dp))
            Mono("Version : 1.0.0"); Mono("API : ${BanCheckApi.HOST}")
        }
    }
}
