package com.gamescounter.truco.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay

/**
 * Mantiene la pantalla encendida durante [timeoutMillis] tras cada cambio de [resetKey].
 * Al montarse arranca el temporizador; cada nuevo valor de [resetKey] lo reinicia.
 * Pasado el tiempo sin cambios, libera el flag y la pantalla se apaga según el sistema.
 */
@Composable
fun KeepScreenAwake(resetKey: Any?, timeoutMillis: Long = 40_000L) {
    val view = LocalView.current
    DisposableEffect(Unit) {
        onDispose { view.keepScreenOn = false }
    }
    LaunchedEffect(resetKey) {
        view.keepScreenOn = true
        delay(timeoutMillis)
        view.keepScreenOn = false
    }
}
