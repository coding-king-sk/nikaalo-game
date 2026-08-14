package com.codingkingsk.nikaalo.game

import android.content.Context
import kotlinx.serialization.json.Json

class LevelRepository(context: Context) {

	private val json = Json { ignoreUnknownKeys = true }

	val levels: List<LevelSpec> = context.assets.open(ASSET_NAME).use { stream ->
		json.decodeFromString<List<LevelSpec>>(stream.bufferedReader().readText())
	}

	fun byIndex(index: Int): LevelSpec = levels[index.coerceIn(0, levels.lastIndex)]

	private companion object {
		const val ASSET_NAME = "levels.json"
	}
}
