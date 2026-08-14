package com.codingkingsk.nikaalo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codingkingsk.nikaalo.game.LevelSpec

@Composable
fun LevelSelectScreen(
	levels: List<LevelSpec>,
	unlockedIndex: Int,
	starsFor: (LevelSpec) -> Int,
	onPick: (Int) -> Unit,
	onBack: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(Background)
			.padding(16.dp),
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			TextButton(onClick = onBack) { Text("Back") }
			Spacer(Modifier.fillMaxWidth(0.18f))
			Text("Levels", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
		}
		Spacer(Modifier.height(12.dp))

		LazyVerticalGrid(
			columns = GridCells.Fixed(4),
			horizontalArrangement = Arrangement.spacedBy(10.dp),
			verticalArrangement = Arrangement.spacedBy(10.dp),
		) {
			itemsIndexed(levels) { index, level ->
				val locked = index > unlockedIndex
				val stars = starsFor(level)
				Box(
					modifier = Modifier
						.aspectRatio(1f)
						.background(
							if (locked) CellBg else BoardBg,
							RoundedCornerShape(12.dp),
						)
						.clickable(enabled = !locked) { onPick(index) },
					contentAlignment = Alignment.Center,
				) {
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Text(
							if (locked) "\uD83D\uDD12" else "${index + 1}",
							color = if (locked) TextMuted else TextPrimary,
							fontSize = 18.sp,
							fontWeight = FontWeight.Bold,
						)
						if (!locked) {
							Text(
								"\u2605".repeat(stars).ifEmpty { "\u00B7" },
								color = Accent,
								fontSize = 11.sp,
							)
						}
					}
				}
			}
		}
	}
}
