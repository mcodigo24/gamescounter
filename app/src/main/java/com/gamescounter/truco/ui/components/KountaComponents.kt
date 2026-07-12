@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamescounter.truco.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamescounter.truco.ui.theme.ClayOnAccent
import com.gamescounter.truco.ui.theme.KountaBorder
import com.gamescounter.truco.ui.theme.KountaPrimary
import com.gamescounter.truco.ui.theme.KountaSurface

@Composable
fun KountaCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = KountaSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val clayModifier = modifier.fillMaxWidth().claymorphic(shape = KountaShape)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = clayModifier,
            shape = KountaShape,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content,
        )
    } else {
        Card(
            modifier = clayModifier,
            shape = KountaShape,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content,
        )
    }
}

@Composable
fun KountaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().claymorphic(shape = KountaShape, elevation = 6.dp),
        shape = KountaShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = KountaPrimary,
            contentColor = ClayOnAccent,
            disabledContainerColor = KountaPrimary.copy(alpha = 0.4f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun KountaSection(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.claymorphic(shape = KountaShape),
        shape = KountaShape,
        color = KountaSurface,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, KountaBorder),
    ) {
        content()
    }
}

@Composable
fun KountaHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(top = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
