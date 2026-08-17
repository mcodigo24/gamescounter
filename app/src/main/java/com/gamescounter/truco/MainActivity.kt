package com.gamescounter.truco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gamescounter.truco.chinchon.ChinChonViewModel
import com.gamescounter.truco.comodin.ComodinViewModel
import com.gamescounter.truco.diezmil.DiezMilViewModel
import com.gamescounter.truco.generala.GeneralaViewModel
import com.gamescounter.truco.ui.ChinChonScreen
import com.gamescounter.truco.ui.ComodinScreen
import com.gamescounter.truco.ui.DiezMilScreen
import com.gamescounter.truco.ui.GameEntry
import com.gamescounter.truco.ui.GeneralaScreen
import com.gamescounter.truco.ui.HomeScreen
import com.gamescounter.truco.ui.TrucoScreen
import com.gamescounter.truco.ui.components.KountaBottomNavBar
import com.gamescounter.truco.ui.components.isLandscape
import com.gamescounter.truco.ui.theme.GamesCounterTheme
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.ui.theme.KountaTheme

class MainActivity : ComponentActivity() {

    private val trucoViewModel: TrucoViewModel by viewModels()
    private val generalaViewModel: GeneralaViewModel by viewModels()
    private val chinChonViewModel: ChinChonViewModel by viewModels()
    private val diezMilViewModel: DiezMilViewModel by viewModels()
    private val comodinViewModel: ComodinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // App is dark-only regardless of system theme: force light (bright) status/nav bar icons.
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = false

        setContent {
            var currentScreen by rememberSaveable { mutableStateOf("home") }
            KountaTheme {
                BackHandler(enabled = currentScreen != "home") {
                    currentScreen = "home"
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = KountaBackground,
                    // Insets are handled per-screen (top by each GameTopBar, bottom by
                    // KountaBottomNavBar itself) — zero here to avoid reserving them twice.
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (!isLandscape()) {
                            KountaBottomNavBar(
                                currentScreen = currentScreen,
                                onSelect = { currentScreen = it },
                            )
                        }
                    },
                ) { padding ->
                    when (currentScreen) {
                        "home" -> HomeScreen(
                            onSelectGame = { selected ->
                                currentScreen = selected.name
                            },
                            modifier = Modifier.fillMaxSize().padding(padding),
                        )

                        GameEntry.Truco.name -> TrucoScreen(
                            viewModel = trucoViewModel,
                            modifier = Modifier.fillMaxSize().padding(padding),
                            onBack = { currentScreen = "home" },
                        )

                        GameEntry.Generala.name -> GeneralaScreen(
                            viewModel = generalaViewModel,
                            modifier = Modifier.fillMaxSize().padding(padding),
                            onBack = { currentScreen = "home" },
                        )

                        GameEntry.ChinChon.name -> ChinChonScreen(
                            viewModel = chinChonViewModel,
                            onBack = { currentScreen = "home" },
                            modifier = Modifier.fillMaxSize().padding(padding),
                        )

                        GameEntry.DiezMil.name -> DiezMilScreen(
                            viewModel = diezMilViewModel,
                            onBack = { currentScreen = "home" },
                            modifier = Modifier.fillMaxSize().padding(padding),
                        )

                        GameEntry.Comodin.name -> ComodinScreen(
                            viewModel = comodinViewModel,
                            onBack = { currentScreen = "home" },
                            modifier = Modifier.fillMaxSize().padding(padding),
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        trucoViewModel.flushSave()
        generalaViewModel.flushSave()
        chinChonViewModel.flushSave()
        diezMilViewModel.flushSave()
        comodinViewModel.flushSave()
    }
}
