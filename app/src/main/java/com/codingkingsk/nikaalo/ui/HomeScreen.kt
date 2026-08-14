package com.codingkingsk.nikaalo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(InkSoft, Ink))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(modifier = Modifier.size(width = 132.dp, height = 66.dp)) {
                VehicleArt(type = "auto", horizontal = true, modifier = Modifier.fillMaxSize())
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "NIKAALO",
                color = TextPrimary,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
            )
            Text(
                text = "Jam se apni auto nikaalo",
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(34.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatChip("Solved", "$solvedLevels/$totalLevels", Accent, Modifier.weight(1f))
                StatChip("Stars", "$totalStars/${totalLevels * 3}", Success, Modifier.weight(1f))
            }

            Spacer(Modifier.height(30.dp))

            PillButton(
                text = if (solvedLevels == 0) "PLAY" else "CONTINUE",
                modifier = Modifier.fillMaxWidth(0.72f),
                primary = true,
                onClick = onPlay,
            )

            Spacer(Modifier.height(12.dp))

            PillButton(
                text = "All Levels",
                modifier = Modifier.fillMaxWidth(0.72f),
                onClick = onLevels,
            )
        }
    }
}
