package com.codingkingsk.nikaalo.game

import android.content.Context

/** Tiny SharedPreferences-backed progress store: best moves, stars, unlocks. */
class Progress(context: Context) {

	private val prefs = context.getSharedPreferences("nikaalo_progress", Context.MODE_PRIVATE)

	fun bestMoves(levelId: Int): Int = prefs.getInt(KEY_BEST + levelId, 0)

	fun isSolved(levelId: Int): Boolean = bestMoves(levelId) > 0

	fun record(levelId: Int, moves: Int) {
		val best = bestMoves(levelId)
		if (best == 0 || moves < best) {
			prefs.edit().putInt(KEY_BEST + levelId, moves).apply()
		}
	}

	fun stars(levelId: Int, minMoves: Int): Int {
		val best = bestMoves(levelId)
		if (best == 0) return 0
		return when {
			minMoves <= 0 -> 1
			best <= minMoves -> 3
			best <= minMoves + 3 -> 2
			else -> 1
		}
	}

	/** First level that has not been solved yet. */
	fun highestUnlockedIndex(levels: List<LevelSpec>): Int {
		var index = 0
		while (index < levels.lastIndex && isSolved(levels[index].id)) index++
		return index
	}

	fun solvedCount(levels: List<LevelSpec>): Int = levels.count { isSolved(it.id) }

	fun totalStars(levels: List<LevelSpec>): Int = levels.sumOf { stars(it.id, it.minMoves) }

	private companion object {
		const val KEY_BEST = "best_"
	}
}
