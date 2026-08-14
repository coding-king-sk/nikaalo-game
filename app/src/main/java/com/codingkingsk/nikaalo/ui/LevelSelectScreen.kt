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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    chapterLabel: (Int) -> String,
    onPick: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val chapters = remember(levels) {
        levels.withIndex().groupBy { entry -> entry.value.chapter }.toSortedMap()
    }

    Column(modifier = Modifier.fillMaxSize().background(Ink)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PillButton(text = "\u2039 Back", onClick = onBack)
            Spacer(Modifier.padding(horizontal = 6.dp))
            Text(
                text = "Levels",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp),
        ) {
            chapters.forEach { (chapter, entries) ->
                item(key = "header-$chapter") {
                    val earned = entries.sumOf { starsFor(it.value) }
                    Column(modifier = Modifier.padding(top = 14.dp, bottom = 10.dp)) {
                        Text(
                            text = "Chapter $chapter \u00b7 ${chapterLabel(chapter)}",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${entries.size} levels \u00b7 \u2605 $earned/${entries.size * 3} \u00b7 target ${entries.first().value.minMoves}-${entries.last().value.minMoves} moves",
                            color = TextMuted,
                            fontSize = 11.sp,
                        )
                    }
                }

                entries.chunked(5).forEachIndexed { rowIndex, rowEntries ->
                    item(key = "row-$chapter-$rowIndex") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowEntries.forEach { entry ->
                                LevelTile(
                                    number = entry.index + 1,
                                    stars = starsFor(entry.value),
                                    locked = entry.index > unlockedIndex,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onPick(entry.index) },
                                )
                            }
                            repeat(5 - rowEntries.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelTile(
    number: Int,
    stars: Int,
    locked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .background(if (locked) InkSoft else Card, RoundedCornerShape(16.dp))
            .clickable(enabled = !locked, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (locked) "\uD83D\uDD12" else "$number",
            color = if (locked) TextMuted else TextPrimary,
            fontSize = if (locked) 15.sp else 17.sp,
            fontWeight = FontWeight.Bold,
        )
        if (!locked) {
            Box(modifier = Modifier.height(14.dp)) {
                Text(
                    text = "\u2605".repeat(stars),
                    color = Accent,
                    fontSize = 10.sp,
                )
            }
        }
    }
}
