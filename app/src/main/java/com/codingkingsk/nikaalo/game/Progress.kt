package com.codingkingsk.nikaalo.game

import android.content.Context
import android.content.SharedPreferences

/**
 * Local progress: best move count per level, derived stars and unlocks.
 *
 * Star thresholds are deliberately strict so that 3 stars means "optimal":
 *   best == minMoves        -> 3
 *   best <= minMoves + 2    -> 2
 *   otherwise               -> 1
 */
class Progress(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nikaalo_progress", Context.MODE_PRIVATE)

    fun bestMoves(levelId: Int): Int? =
        prefs.getInt("best_$levelId", 0).takeIf { it > 0 }

    fun record(levelId: Int, moves: Int) {
        if (moves <= 0) return
        val current = bestMoves(levelId)
        if (current == null || moves < current) {
            prefs.edit().putInt("best_$levelId", moves).apply()
        }
    }

    fun isSolved(levelId: Int): Boolean = bestMoves(levelId) != null

    fun stars(levelId: Int, minMoves: Int): Int {
        val best = bestMoves(levelId) ?: return 0
        return when {
            minMoves <= 0 -> 3
            best <= minMoves -> 3
            best <= minMoves + 2 -> 2
            else -> 1
        }
    }

    fun highestUnlockedIndex(levels: List<LevelSpec>): Int {
        if (levels.isEmpty()) return 0
        var index = 0
        while (index < levels.size - 1 && isSolved(levels[index].id)) index++
        return index
    }

    fun solvedCount(levels: List<LevelSpec>): Int = levels.count { isSolved(it.id) }

    fun totalStars(levels: List<LevelSpec>): Int =
        levels.sumOf { stars(it.id, it.minMoves) }
}
