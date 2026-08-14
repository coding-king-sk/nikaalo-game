package com.codingkingsk.nikaalo.game

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class LevelRepository(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val levels: List<LevelSpec> = run {
        val raw = context.assets.open("levels.json")
            .bufferedReader()
            .use { reader -> reader.readText() }
        json.decodeFromString<List<LevelSpec>>(raw)
    }

    fun byIndex(index: Int): LevelSpec = levels[index.coerceIn(0, levels.size - 1)]
}
