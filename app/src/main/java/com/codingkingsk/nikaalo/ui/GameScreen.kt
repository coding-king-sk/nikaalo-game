package com.codingkingsk.nikaalo.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.codingkingsk.nikaalo.game.Board
import com.codingkingsk.nikaalo.game.Dir
import com.codingkingsk.nikaalo.game.LevelSpec
import com.codingkingsk.nikaalo.game.Solver
import com.codingkingsk.nikaalo.game.Vehicle
import com.codingkingsk.nikaalo.game.vehicleLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

fun starsFor(moves: Int, minMoves: Int): Int = when {
    minMoves <= 0 -> 3
    moves <= minMoves -> 3
    moves <= minMoves + 2 -> 2
    else -> 1
}

@Composable
fun GameScreen(
    level: LevelSpec,
    levelNumber: Int,
    totalLevels: Int,
    chapterName: String,
    bestMoves: Int?,
    onSolved: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    var board by remember(level.id) { mutableStateOf(Board.from(level)) }
    var moves by remember(level.id) { mutableIntStateOf(0) }
    val history = remember(level.id) { mutableStateListOf<Board>() }
    var hint by remember(level.id) { mutableStateOf<Solver.Move?>(null) }
    var hintText by remember(level.id) { mutableStateOf<String?>(null) }
    var thinking by remember(level.id) { mutableStateOf(false) }
    var showWin by remember(level.id) { mutableStateOf(false) }
    var finalMoves by remember(level.id) { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    fun restart() {
        board = Board.from(level)
        history.clear()
        moves = 0
        hint = null
        hintText = null
    }

    Box(modifier = Modifier.fillMaxSize().background(Ink)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                PillButton(text = "\u2039 Back", onClick = onBack)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Level $levelNumber / $totalLevels",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(text = chapterName, color = TextMuted, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip("Moves", "$moves", Accent, Modifier.weight(1f))
                StatChip("Target", "${level.minMoves}", TextPrimary, Modifier.weight(1f))
                StatChip("Best", bestMoves?.toString() ?: "\u2014", Success, Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            BoardView(board = board, hint = hint) { vehicleId, delta ->
                history.add(board)
                board = board.move(vehicleId, delta)
                moves += 1
                hint = null
                hintText = null
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (board.solved) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    finalMoves = moves
                    onSolved(moves)
                    showWin = true
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = hintText
                    ?: if (thinking) "Soch raha hoon\u2026" else "Auto ko right side ke green gate tak nikaalo",
                color = if (hintText != null) Accent else TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PillButton("Undo", Modifier.weight(1f), enabled = history.isNotEmpty()) {
                    if (history.isNotEmpty()) {
                        board = history.removeAt(history.size - 1)
                        moves = (moves - 1).coerceAtLeast(0)
                        hint = null
                        hintText = null
                    }
                }
                PillButton("Restart", Modifier.weight(1f)) { restart() }
                PillButton("\uD83D\uDCA1 Hint", Modifier.weight(1f), primary = true, enabled = !thinking) {
                    thinking = true
                    val snapshot = board
                    scope.launch {
                        val solution = withContext(Dispatchers.Default) { Solver.solve(snapshot) }
                        thinking = false
                        val next = solution?.firstOrNull()
                        if (next == null) {
                            hint = null
                            hintText = "Yahan se solution nahi mila \u2014 Undo ya Restart karo"
                        } else {
                            hint = next
                            val vehicle = snapshot.vehicles.first { it.id == next.vehicleId }
                            val direction = if (vehicle.dir == Dir.H) {
                                if (next.delta > 0) "right" else "left"
                            } else {
                                if (next.delta > 0) "neeche" else "upar"
                            }
                            hintText = "\uD83D\uDCA1 ${vehicleLabel(vehicle.type)} ko ${abs(next.delta)} cell $direction"
                        }
                    }
                }
            }
        }

        if (showWin) {
            WinOverlay(
                moves = finalMoves,
                target = level.minMoves,
                stars = starsFor(finalMoves, level.minMoves),
                isLast = levelNumber >= totalLevels,
                onNext = {
                    showWin = false
                    onNext()
                },
                onRetry = {
                    showWin = false
                    restart()
                },
            )
        }
    }
}

@Composable
private fun BoardView(
    board: Board,
    hint: Solver.Move?,
    onMove: (Int, Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Wall, RoundedCornerShape(24.dp))
            .padding(8.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Asphalt),
        ) {
            val gridSize = board.size
            val cell: Dp = maxWidth / gridSize
            val cellPx = with(LocalDensity.current) { cell.toPx() }
            val exitRow = board.player.r

            Canvas(modifier = Modifier.fillMaxSize()) {
                val boardWidth = this.size.width
                val boardHeight = this.size.height
                val stroke = boardWidth * 0.006f
                val dash = cellPx * 0.20f

                for (i in 1 until gridSize) {
                    val p = cellPx * i
                    var y = dash * 0.5f
                    while (y < boardHeight) {
                        drawRect(
                            color = Lane.copy(alpha = 0.09f),
                            topLeft = Offset(p - stroke / 2f, y),
                            size = Size(stroke, dash),
                        )
                        y += dash * 2f
                    }
                    var x = dash * 0.5f
                    while (x < boardWidth) {
                        drawRect(
                            color = Lane.copy(alpha = 0.09f),
                            topLeft = Offset(x, p - stroke / 2f),
                            size = Size(dash, stroke),
                        )
                        x += dash * 2f
                    }
                }

                drawRect(
                    color = Success.copy(alpha = 0.13f),
                    topLeft = Offset(0f, cellPx * exitRow),
                    size = Size(boardWidth, cellPx),
                )
                drawRect(
                    color = Success,
                    topLeft = Offset(boardWidth - cellPx * 0.09f, cellPx * exitRow + cellPx * 0.10f),
                    size = Size(cellPx * 0.09f, cellPx * 0.80f),
                )
            }

            for (vehicle in board.vehicles) {
                key(vehicle.id) {
                    VehiclePiece(
                        vehicle = vehicle,
                        cell = cell,
                        cellPx = cellPx,
                        range = board.freeRange(vehicle),
                        highlighted = hint?.vehicleId == vehicle.id,
                        onCommit = { delta -> onMove(vehicle.id, delta) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VehiclePiece(
    vehicle: Vehicle,
    cell: Dp,
    cellPx: Float,
    range: IntRange,
    highlighted: Boolean,
    onCommit: (Int) -> Unit,
) {
    val horizontal = vehicle.dir == Dir.H
    val pieceWidth = if (horizontal) cell * vehicle.len else cell
    val pieceHeight = if (horizontal) cell else cell * vehicle.len

    var drag by remember(vehicle.id, vehicle.r, vehicle.c) { mutableFloatStateOf(0f) }
    var dragging by remember(vehicle.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val lift by animateFloatAsState(
        targetValue = if (dragging) 1.07f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "lift",
    )
    val glow by animateFloatAsState(
        targetValue = if (highlighted) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "glow",
    )

    Box(
        modifier = Modifier
            .zIndex(if (dragging) 2f else 1f)
            .offset(x = cell * vehicle.c, y = cell * vehicle.r)
            .offset {
                IntOffset(
                    x = if (horizontal) drag.roundToInt() else 0,
                    y = if (horizontal) 0 else drag.roundToInt(),
                )
            }
            .size(width = pieceWidth, height = pieceHeight)
            .padding(cell * 0.05f)
            .graphicsLayer {
                scaleX = lift
                scaleY = lift
            }
            .pointerInput(vehicle.id, vehicle.r, vehicle.c, range) {
                if (!vehicle.movable) return@pointerInput
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDrag = { _, amount ->
                        val raw = drag + if (horizontal) amount.x else amount.y
                        drag = raw.coerceIn(range.first * cellPx, range.last * cellPx)
                    },
                    onDragEnd = {
                        dragging = false
                        val delta = (drag / cellPx).roundToInt().coerceIn(range.first, range.last)
                        val from = drag
                        val to = delta * cellPx
                        scope.launch {
                            animate(
                                initialValue = from,
                                targetValue = to,
                                animationSpec = spring(
                                    dampingRatio = 0.72f,
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                            ) { value, _ -> drag = value }
                            if (delta != 0) onCommit(delta) else drag = 0f
                        }
                    },
                    onDragCancel = {
                        dragging = false
                        val from = drag
                        scope.launch {
                            animate(initialValue = from, targetValue = 0f) { value, _ -> drag = value }
                        }
                    },
                )
            },
    ) {
        VehicleArt(
            type = vehicle.type,
            horizontal = horizontal,
            modifier = Modifier.fillMaxSize(),
        )
        if (glow > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = Accent.copy(alpha = 0.30f + 0.60f * glow),
                        shape = RoundedCornerShape(12.dp),
                    ),
            )
        }
    }
}
