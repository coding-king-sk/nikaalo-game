package com.codingkingsk.nikaalo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
	totalLevels: Int,
	solvedLevels: Int,
	totalStars: Int,
	onPlay: () -> Unit,
	onLevels: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(Background)
			.padding(28.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Text(vehicleEmoji("auto"), fontSize = 72.sp)
		Spacer(Modifier.height(8.dp))
		Text("NIKAALO", color = Accent, fontSize = 40.sp, fontWeight = FontWeight.Black)
		Text("Jam se nikaalo", color = TextMuted, fontSize = 16.sp)

		Spacer(Modifier.height(36.dp))
		Text("Solved: $solvedLevels / $totalLevels", color = TextPrimary, fontSize = 16.sp)
		Text("Stars: $totalStars / ${totalLevels * 3}", color = Success, fontSize = 16.sp)

		Spacer(Modifier.height(36.dp))
		Button(onClick = onPlay, modifier = Modifier.fillMaxWidth()) {
			Text("PLAY", fontSize = 18.sp, fontWeight = FontWeight.Bold)
		}
		Spacer(Modifier.height(12.dp))
		OutlinedButton(onClick = onLevels, modifier = Modifier.fillMaxWidth()) {
			Text("Levels")
		}

		Spacer(Modifier.height(28.dp))
		Text(
			"Auto ko right side ke exit tak pahuchao. Gaadiyan sirf apni direction me slide hoti hain.",
			color = TextMuted,
			fontSize = 13.sp,
		)
	}
}
