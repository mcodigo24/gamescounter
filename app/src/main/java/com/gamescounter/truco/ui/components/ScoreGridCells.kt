package com.gamescounter.truco.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gamescounter.truco.ui.theme.KountaBorder
import com.gamescounter.truco.ui.theme.KountaBorderFocus
import com.gamescounter.truco.ui.theme.KountaPrimary
import com.gamescounter.truco.ui.theme.KountaSecondary
import com.gamescounter.truco.ui.theme.KountaSurface

private val signedInputPattern = Regex("^-?\\d*$")

@Composable
fun GridLabel(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        shape = KountaShapeSmall,
        color = containerColor,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, KountaBorder),
        modifier = modifier.width(72.dp),
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = contentColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun GridTotalCell(
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        shape = KountaShapeSmall,
        color = containerColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, KountaBorder),
        modifier = modifier.width(72.dp),
    ) {
        Text(
            text = value,
            textAlign = TextAlign.Center,
            color = contentColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun SignedScoreField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = KountaSurface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    isError: Boolean = false,
) {
    // Local buffer so a lone "-" (which parses to no numeric value) isn't
    // immediately erased from the field before the user types the digits.
    var isFocused by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(value) }
    LaunchedEffect(value, isFocused) {
        if (!isFocused) text = value
    }
    val isNegative = text.startsWith("-")

    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            if (input.isEmpty() || input.matches(signedInputPattern)) {
                text = input
                onValueChange(input)
            }
        },
        singleLine = true,
        isError = isError,
        modifier = modifier
            .width(88.dp)
            .onFocusChanged { isFocused = it.isFocused },
        shape = KountaShapeSmall,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            textAlign = TextAlign.Center,
            color = contentColor,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
        leadingIcon = {
            TextButton(
                onClick = {
                    val toggled = when {
                        text.isEmpty() -> "-"
                        isNegative -> text.removePrefix("-")
                        else -> "-$text"
                    }
                    text = toggled
                    onValueChange(toggled)
                },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.width(28.dp),
            ) {
                Text(
                    text = if (isNegative) "-" else "+",
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
            }
        },
        colors = kountaFieldColors(containerColor, contentColor),
    )
}

@Composable
fun UnsignedScoreField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = KountaSurface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.isEmpty() || input.all { it.isDigit() }) {
                onValueChange(input)
            }
        },
        singleLine = true,
        isError = isError,
        modifier = modifier.width(72.dp),
        shape = KountaShapeSmall,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            textAlign = TextAlign.Center,
            color = contentColor,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
        colors = kountaFieldColors(containerColor, contentColor),
    )
}

@Composable
private fun kountaFieldColors(
    containerColor: Color,
    contentColor: Color,
) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = containerColor,
    unfocusedContainerColor = containerColor,
    disabledContainerColor = containerColor.copy(alpha = 0.6f),
    focusedTextColor = contentColor,
    unfocusedTextColor = contentColor,
    focusedBorderColor = KountaSecondary,
    unfocusedBorderColor = KountaBorder,
    cursorColor = KountaPrimary,
)

fun formatSignedInput(value: Int?): String = when (value) {
    null -> ""
    else -> value.toString()
}

fun formatUnsignedInput(value: Int?): String = when (value) {
    null -> ""
    else -> value.toString()
}
