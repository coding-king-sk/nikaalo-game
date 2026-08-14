package com.codingkingsk.nikaalo.game

import android.content.Context

/**
 * Loads levels from the compact `assets/levels.txt` format.
 *
 *   grid:minMoves:chapter:piece,piece,piece
 *
 * Each piece is exactly five characters - [type][row][col][len][dir] - which
 * keeps 120 levels under 10 KB and parses without any JSON overhead.
 */
class LevelRepository(context: Context) {

    val levels: List<LevelSpec> = context.assets.open("levels.txt")
        .bufferedReader()
        .useLines { lines ->
            lines.map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .mapIndexedNotNull { index, line -> parseLevel(index + 1, line) }
                .toList()
        }

    fun byIndex(index: Int): LevelSpec = levels[index.coerceIn(0, levels.size - 1)]

    private fun parseLevel(id: Int, line: String): LevelSpec? {
        val parts = line.split(':')
        if (parts.size < 4) return null
        val grid = parts[0].toIntOrNull() ?: return null
        val minMoves = parts[1].toIntOrNull() ?: 0
        val chapter = parts[2].toIntOrNull() ?: 1
        val vehicles = parts[3].split(',').mapNotNull { parseVehicle(it) }
        if (vehicles.isEmpty()) return null
        return LevelSpec(
            id = id,
            grid = grid,
            minMoves = minMoves,
            chapter = chapter,
            vehicles = vehicles,
        )
    }

    private fun parseVehicle(token: String): VehicleSpec? {
        if (token.length < 5) return null
        val type = when (token[0]) {
            'a' -> "auto"
            'k' -> "bike"
            'c' -> "car"
            't' -> "thela"
            'b' -> "bus"
            'T' -> "truck"
            'w' -> "cow"
            else -> return null
        }
        val row = token[1] - '0'
        val col = token[2] - '0'
        val len = token[3] - '0'
        if (row < 0 || col < 0 || len < 1) return null
        return VehicleSpec(
            type = type,
            r = row,
            c = col,
            len = len,
            dir = if (token[4] == 'v') "v" else "h",
        )
    }
}
