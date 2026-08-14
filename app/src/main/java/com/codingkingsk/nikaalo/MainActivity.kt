package com.codingkingsk.nikaalo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.codingkingsk.nikaalo.game.LevelRepository
import com.codingkingsk.nikaalo.game.Progress
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
				NikaaloApp(repository = repository, progress = progress)
			}
		}
	}
}

private sealed interface Screen {
	data object Home : Screen
	data object Levels : Screen
	data class Play(val index: Int) : Screen
}

@Composable
private fun NikaaloApp(repository: LevelRepository, progress: Progress) {
	val levels = repository.levels
	var screen by remember { mutableStateOf<Screen>(Screen.Home) }
	var revision by remember { mutableStateOf(0) }

	when (val current = screen) {
		Screen.Home -> {
			val solved = remember(revision) { progress.solvedCount(levels) }
			val stars = remember(revision) { progress.totalStars(levels) }
			HomeScreen(
				totalLevels = levels.size,
				solvedLevels = solved,
				totalStars = stars,
				onPlay = { screen = Screen.Play(progress.highestUnlockedIndex(levels)) },
				onLevels = { screen = Screen.Levels },
			)
		}

		Screen.Levels -> LevelSelectScreen(
			levels = levels,
			unlockedIndex = remember(revision) { progress.highestUnlockedIndex(levels) },
			starsFor = { level -> progress.stars(level.id, level.minMoves) },
			onPick = { index -> screen = Screen.Play(index) },
			onBack = { screen = Screen.Home },
		)

		is Screen.Play -> {
			val level = levels[current.index]
			GameScreen(
				level = level,
				levelNumber = current.index + 1,
				totalLevels = levels.size,
				bestMoves = remember(revision, level.id) { progress.bestMoves(level.id) },
				onSolved = { moves ->
					progress.record(level.id, moves)
					revision++
				},
				onNext = {
					screen = if (current.index < levels.lastIndex) {
						Screen.Play(current.index + 1)
					} else {
						Screen.Home
					}
				},
				onBack = { screen = Screen.Home },
			)
		}
	}
}
