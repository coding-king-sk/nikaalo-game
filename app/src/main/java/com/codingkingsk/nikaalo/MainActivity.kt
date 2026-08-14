package com.codingkingsk.nikaalo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.codingkingsk.nikaalo.game.LevelRepository
import com.codingkingsk.nikaalo.game.LevelSpec
import com.codingkingsk.nikaalo.game.Progress
import com.codingkingsk.nikaalo.game.chapterName
import com.codingkingsk.nikaalo.ui.GameScreen
import com.codingkingsk.nikaalo.ui.HomeScreen
import com.codingkingsk.nikaalo.ui.LevelSelectScreen
import com.codingkingsk.nikaalo.ui.NikaaloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = LevelRepository(applicationContext)
        val progress = Progress(applicationContext)
        setContent {
            NikaaloTheme {
                NikaaloApp(levels = repository.levels, progress = progress)
            }
        }
    }
}

sealed interface Route {
    data object Home : Route
    data object Levels : Route
    data class Game(val index: Int) : Route
}

@Composable
fun NikaaloApp(levels: List<LevelSpec>, progress: Progress) {
    var route by remember { mutableStateOf<Route>(Route.Home) }
    var refresh by remember { mutableIntStateOf(0) }

    when (val current = route) {
        Route.Home -> HomeScreen(
            totalLevels = levels.size,
            solvedLevels = remember(refresh) { progress.solvedCount(levels) },
            totalStars = remember(refresh) { progress.totalStars(levels) },
            onPlay = { route = Route.Game(progress.highestUnlockedIndex(levels)) },
            onLevels = { route = Route.Levels },
        )

        Route.Levels -> LevelSelectScreen(
            levels = levels,
            unlockedIndex = remember(refresh) { progress.highestUnlockedIndex(levels) },
            starsFor = { level -> progress.stars(level.id, level.minMoves) },
            chapterLabel = { chapter -> chapterName(chapter) },
            onPick = { index -> route = Route.Game(index) },
            onBack = { route = Route.Home },
        )

        is Route.Game -> {
            val index = current.index.coerceIn(0, levels.size - 1)
            val level = levels[index]
            GameScreen(
                level = level,
                levelNumber = index + 1,
                totalLevels = levels.size,
                chapterName = chapterName(level.chapter),
                bestMoves = remember(refresh, level.id) { progress.bestMoves(level.id) },
                onSolved = { moves ->
                    progress.record(level.id, moves)
                    refresh += 1
                },
                onNext = { route = Route.Game((index + 1).coerceAtMost(levels.size - 1)) },
                onBack = { route = Route.Levels },
            )
        }
    }
}
