package com.codingkingsk.nikaalo.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun WinOverlay(
    moves: Int,
    target: Int,
    stars: Int,
    isLast: Boolean,
    onNext: () -> Unit,
    onRetry: () -> Unit,
) {
    var revealed by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        repeat(3) {
            delay(240)
            revealed += 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.74f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .background(Card, RoundedCornerShape(26.dp))
                .padding(horizontal = 22.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "NIKAL GAYI!",
                color = Accent,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = if (stars == 3) "Perfect \u2014 optimal solution!" else "$moves moves \u00b7 best possible $target",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (index in 0 until 3) {
                    val appeared = index < revealed
                    val scale by animateFloatAsState(
                        targetValue = if (appeared) 1f else 0.3f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                        label = "star$index",
                    )
                    Text(
                        text = "\u2605",
                        fontSize = 46.sp,
                        color = if (index < stars) Accent else Color.White.copy(alpha = 0.13f),
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = if (appeared) 1f else 0f
                        },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PillButton("Retry", Modifier.weight(1f), onClick = onRetry)
                PillButton(
                    text = if (isLast) "Levels" else "Next Level \u203A",
                    modifier = Modifier.weight(1.4f),
                    primary = true,
                    onClick = onNext,
                )
            }
        }
    }
}
