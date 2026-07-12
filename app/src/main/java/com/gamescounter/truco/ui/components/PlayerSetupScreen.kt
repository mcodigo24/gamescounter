@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamescounter.truco.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.ui.theme.KountaPrimary
import com.gamescounter.truco.ui.theme.KountaSecondary
import com.gamescounter.truco.ui.theme.KountaSurface

@Composable
fun PlayerSetupScreen(
    gameTitle: String,
    minPlayers: Int,
    maxPlayers: Int,
    defaultPlayers: Int,
    onBack: () -> Unit,
    onStart: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var playerCount by remember { mutableIntStateOf(defaultPlayers) }
    val tonalColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = KountaSecondary.copy(alpha = 0.35f),
        contentColor = KountaPrimary,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KountaBackground,
        contentWindowInsets = kountaScreenInsets(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = gameTitle, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KountaBackground,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = "Cantidad de jugadores",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Elegi entre $minPlayers y $maxPlayers jugadores",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            KountaSection(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalIconButton(
                        onClick = { playerCount = (playerCount - 1).coerceAtLeast(minPlayers) },
                        enabled = playerCount > minPlayers,
                        colors = tonalColors,
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = playerCount.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    FilledTonalIconButton(
                        onClick = { playerCount = (playerCount + 1).coerceAtMost(maxPlayers) },
                        enabled = playerCount < maxPlayers,
                        colors = tonalColors,
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            KountaPrimaryButton(
                text = "Comenzar partida",
                onClick = { onStart(playerCount) },
            )
        }
    }
}
