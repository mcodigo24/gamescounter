@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamescounter.truco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamescounter.truco.ui.components.KountaCard
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.ui.theme.KountaText

enum class GameEntry(val title: String, val subtitle: String) {
    Truco("Truco", "Tanteador 0 a 30"),
    Generala("Generala", "Planilla editable por jugador"),
    ChinChon("Chin Chon", "Acumulador con valores negativos"),
    DiezMil("10Mil", "Llegada exacta a 10000 puntos"),
}

@Composable
fun HomeScreen(
    onSelectGame: (GameEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = GameEntry.entries

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KountaBackground,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "Kounta",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Text(
                    text = "Elegi el juego para empezar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            items(entries) { game ->
                KountaCard(onClick = { onSelectGame(game) }) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = game.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = KountaText,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
