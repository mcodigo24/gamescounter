package com.gamescounter.truco.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gamescounter.truco.ui.theme.KountaBorder
import com.gamescounter.truco.ui.theme.KountaBorderFocus
import com.gamescounter.truco.ui.theme.KountaPrimary
import com.gamescounter.truco.ui.theme.KountaSecondary
import com.gamescounter.truco.ui.theme.KountaSurface

@Composable
fun GridLabel(
    text: String,
    modifier: Modifier = Modifier.width(72.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        shape = KountaShapeSmall,
        color = containerColor,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, KountaBorder),
        modifier = modifier.claymorphic(shape = KountaShapeSmall, elevation = 4.dp),
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
    modifier: Modifier = Modifier.width(72.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        shape = KountaShapeSmall,
        color = containerColor,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, KountaBorder),
        modifier = modifier.claymorphic(shape = KountaShapeSmall, elevation = 6.dp),
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
fun PlayerInitialField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    containerColor: Color = KountaSurface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.SemiBold,
    ),
) {
    // Tapping an existing initial clears it immediately so typing replaces it
    // outright, instead of requiring the user to delete the old letter first.
    var isFocused by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(value) }
    LaunchedEffect(value, isFocused) {
        if (!isFocused) text = value
    }
    // Built on BasicTextField (instead of OutlinedTextField) so the cell matches the
    // exact shape/padding of the other grid cells (GridLabel, score/total cells) and
    // lines up with them at the same row height.
    Surface(
        shape = KountaShapeSmall,
        color = containerColor,
        border = BorderStroke(1.dp, if (isFocused) KountaBorderFocus else KountaBorder),
        modifier = modifier.claymorphic(shape = KountaShapeSmall, elevation = 5.dp),
    ) {
        BasicTextField(
            value = if (readOnly) value else text,
            onValueChange = { input ->
                text = input
                onValueChange(input)
            },
            singleLine = true,
            readOnly = readOnly,
            // Respect an explicit color on the caller's textStyle (e.g. lost/won
            // highlighting); only fall back to contentColor when none was set.
            textStyle = textStyle.copy(
                color = if (textStyle.color.isSpecified) textStyle.color else contentColor,
            ),
            cursorBrush = SolidColor(KountaPrimary),
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { focusState ->
                    if (!readOnly && focusState.isFocused && !isFocused) {
                        text = ""
                    }
                    isFocused = focusState.isFocused
                },
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    innerTextField()
                }
            },
        )
    }
}

@Composable
fun SignedScoreField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.width(88.dp),
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
    val digits = text.removePrefix("-")

    fun commit(newDigits: String, negative: Boolean) {
        val combined = if (negative) "-$newDigits" else newDigits
        text = combined
        onValueChange(combined)
    }

    OutlinedTextField(
        // Only the digits are shown; the sign is conveyed by the leading +/- toggle,
        // so a negative value doesn't render as a confusing "- -10".
        value = digits,
        onValueChange = { input ->
            if (input.isEmpty() || input.all { it.isDigit() }) {
                commit(input, isNegative)
            }
        },
        singleLine = true,
        isError = isError,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused },
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
                onClick = { commit(digits, !isNegative) },
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
    modifier: Modifier = Modifier.width(72.dp),
    containerColor: Color = KountaSurface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    isError: Boolean = false,
    onFocusLost: (() -> Unit)? = null,
) {
    var wasFocused by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.isEmpty() || input.all { it.isDigit() }) {
                onValueChange(input)
            }
        },
        singleLine = true,
        isError = isError,
        modifier = modifier.onFocusChanged { focusState ->
            if (wasFocused && !focusState.isFocused) {
                onFocusLost?.invoke()
            }
            wasFocused = focusState.isFocused
        },
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
