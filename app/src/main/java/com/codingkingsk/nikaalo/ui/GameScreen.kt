package com.codingkingsk.nikaalo.ui

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codingkingsk.nikaalo.game.Board
import com.codingkingsk.nikaalo.game.Dir
import com.codingkingsk.nikaalo.game.LevelSpec
import com.codingkingsk.nikaalo.game.Solver
import com.codingkingsk.nikaalo.game.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun GameScreen(
	level: LevelSpec,
	levelNumber: Int,
	totalLevels: Int,
	bestMoves: Int,
	onSolved: (Int) -> Unit,
	onNext: () -> Unit,
	onBack: () -> Unit,
) {
	var board by remember(level.id) { mutableStateOf(Board.from(level)) }
	var moves by remember(level.id) { mutableStateOf(0) }
	var history by remember(level.id) { mutableStateOf(emptyList<Board>()) }
	var hint by remember(level.id) { mutableStateOf<Solver.Move?>(null) }
	var thinking by remember(level.id) { mutableStateOf(false) }
	var showWin by remember(level.id) { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

	fun reset() {
		board = Board.from(level)
		moves = 0
		history = emptyList()
		hint = null
		showWin = false
	}

	fun commit(vehicleId: Int, delta: Int) {
		history = history + board
		board = board.move(vehicleId, delta)
		moves += 1
		hint = null
		if (board.solved) {
			showWin = true
			onSolved(moves)
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(Background)
			.padding(16.dp),
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			TextButton(onClick = onBack) { Text("Back") }
			Spacer(Modifier.fillMaxWidth(0.12f))
			Text(
				"Level $levelNumber / $totalLevels",
				color = TextPrimary,
				fontSize = 18.sp,
				fontWeight = FontWeight.Bold,
			)
		}

		Row(modifier = Modifier.padding(vertical = 6.dp)) {
			Text("Moves: $moves", color = TextPrimary, fontSize = 14.sp)
			Spacer(Modifier.width(14.dp))
			Text("Target: ${level.minMoves}", color = TextMuted, fontSize = 14.sp)
			if (bestMoves > 0) {
				Spacer(Modifier.width(14.dp))
				Text("Best: $bestMoves", color = Success, fontSize = 14.sp)
			}
		}

		Spacer(Modifier.height(10.dp))
		BoardView(board = board, hint = hint, onMove = ::commit)
		Spacer(Modifier.height(12.dp))

		val activeHint = hint
		if (activeHint != null) {
			val vehicle = board.vehicles.first { it.id == activeHint.vehicleId }
			Text(
				"\uD83D\uDCA1 ${vehicleEmoji(vehicle.type)} ko ${abs(activeHint.delta)} " +
					directionWord(vehicle.dir, activeHint.delta) + " karo",
				color = Accent,
				fontSize = 14.sp,
			)
			Spacer(Modifier.height(8.dp))
		}

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			OutlinedButton(
				onClick = {
					history.lastOrNull()?.let {
						board = it
						history = history.dropLast(1)
						moves = (moves - 1).coerceAtLeast(0)
						hint = null
					}
				},
				enabled = history.isNotEmpty(),
				modifier = Modifier.weight(1f),
			) { Text("Undo") }

			OutlinedButton(
				onClick = { reset() },
				modifier = Modifier.weight(1f),
			) { Text("Restart") }

			Button(
				onClick = {
					val snapshot = board
					thinking = true
					scope.launch {
						val next = withContext(Dispatchers.Default) {
							Solver.solve(snapshot)?.firstOrNull()
						}
						hint = next
						thinking = false
					}
				},
				enabled = !thinking,
				modifier = Modifier.weight(1f),
			) { Text(if (thinking) "..." else "Hint") }
		}
	}

	if (showWin) {
		val stars = when {
			level.minMoves <= 0 -> 1
			moves <= level.minMoves -> 3
			moves <= level.minMoves + 3 -> 2
			else -> 1
		}
		AlertDialog(
			onDismissRequest = { showWin = false },
			title = { Text("Nikal gayi! " + vehicleEmoji("auto")) },
			text = {
				Text(
					"\u2605".repeat(stars) + "\u2606".repeat(3 - stars) +
					"\n\n$moves moves me solve kiya. Optimal: ${level.minMoves}."
				)
			},
			confirmButton = {
				TextButton(onClick = {
					showWin = false
					onNext()
				}) { Text("Next Level") }
			},
			dismissButton = {
				TextButton(onClick = { reset() }) { Text("Retry") }
			},
		)
	}
}

private fun directionWord(dir: Dir, delta: Int): String = when {
	dir == Dir.H && delta < 0 -> "left"
	dir == Dir.H -> "right"
	delta < 0 -> "upar"
	else -> "neeche"
}

@Composable
private fun BoardView(
	board: Board,
	hint: Solver.Move?,
	onMove: (Int, Int) -> Unit,
) {
	BoxWithConstraints(
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(1f)
			.background(BoardBg, RoundedCornerShape(16.dp))
			.padding(6.dp),
	) {
		val size = board.size
		val cell: Dp = maxWidth / size
		val cellPx = with(LocalDensity.current) { cell.toPx() }

		for (r in 0 until size) {
			for (c in 0 until size) {
				Box(
					modifier = Modifier
						.offset(x = cell * c, y = cell * r)
						.size(cell)
						.padding(2.dp)
						.background(CellBg, RoundedCornerShape(6.dp)),
				)
			}
		}

		val player = board.player
		Box(
			modifier = Modifier
				.offset(x = cell * (size - 1), y = cell * player.r)
				.size(cell)
				.padding(2.dp)
				.background(Success.copy(alpha = 0.22f), RoundedCornerShape(6.dp)),
			contentAlignment = Alignment.Center,
		) {
			Text("\u203A", color = Success, fontSize = 20.sp, fontWeight = FontWeight.Bold)
		}

		board.vehicles.forEach { vehicle ->
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

@Composable
private fun VehiclePiece(
	vehicle: Vehicle,
	cell: Dp,
	cellPx: Float,
	range: IntRange,
	highlighted: Boolean,
	onCommit: (Int) -> Unit,
) {
	var drag by remember(vehicle.id, vehicle.r, vehicle.c) { mutableStateOf(0f) }
	val horizontal = vehicle.dir == Dir.H
	val width = if (horizontal) cell * vehicle.len else cell
	val height = if (horizontal) cell else cell * vehicle.len

	Box(
		modifier = Modifier
			.offset(x = cell * vehicle.c, y = cell * vehicle.r)
			.offset {
				if (horizontal) IntOffset(drag.roundToInt(), 0)
				else IntOffset(0, drag.roundToInt())
			}
			.size(width = width, height = height)
			.padding(3.dp)
			.background(vehicleColor(vehicle.type), RoundedCornerShape(10.dp))
			.then(
				if (highlighted) Modifier.border(3.dp, Accent, RoundedCornerShape(10.dp))
				else Modifier
			)
			.pointerInput(vehicle.id, vehicle.r, vehicle.c, range.first, range.last) {
				if (!vehicle.movable) return@pointerInput
				detectDragGestures(
					onDrag = { _, amount ->
						val raw = drag + if (horizontal) amount.x else amount.y
						drag = raw.coerceIn(range.first * cellPx, range.last * cellPx)
					},
					onDragEnd = {
						val delta = (drag / cellPx).roundToInt()
							.coerceIn(range.first, range.last)
						drag = 0f
						if (delta != 0) onCommit(delta)
					},
					onDragCancel = { drag = 0f },
				)
			},
		contentAlignment = Alignment.Center,
	) {
		Text(vehicleEmoji(vehicle.type), fontSize = 18.sp)
	}
}
