package com.gamescounter.truco.data

const val MAX_PLAYER_NAME_LENGTH = 3

/** Mayúsculas, solo A-Z y 0-9, máximo 3 caracteres. Puede devolver "". */
fun normalizePlayerName(raw: String): String =
    raw.uppercase()
        .filter { it in 'A'..'Z' || it in '0'..'9' }
        .take(MAX_PLAYER_NAME_LENGTH)
