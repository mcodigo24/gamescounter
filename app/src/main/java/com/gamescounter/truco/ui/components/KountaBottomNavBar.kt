package com.gamescounter.truco.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamescounter.truco.ui.GameEntry
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.ui.theme.KountaNavBackground
import com.gamescounter.truco.ui.theme.KountaPrimary
import com.gamescounter.truco.ui.theme.KountaTextMuted

private const val HOME_ROUTE = "home"

@Composable
fun KountaBottomNavBar(
    currentScreen: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = KountaNavBackground,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = currentScreen == HOME_ROUTE,
            onClick = { onSelect(HOME_ROUTE) },
            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = kountaNavItemColors(),
        )
        GameEntry.entries.forEach { game ->
            NavigationBarItem(
                selected = currentScreen == game.name,
                onClick = { onSelect(game.name) },
                icon = { Icon(imageVector = game.icon, contentDescription = game.title) },
                label = { Text(game.title) },
                colors = kountaNavItemColors(),
            )
        }
    }
}

@Composable
private fun kountaNavItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = KountaBackground,
    selectedTextColor = KountaPrimary,
    indicatorColor = KountaPrimary,
    unselectedIconColor = KountaTextMuted,
    unselectedTextColor = KountaTextMuted,
)
