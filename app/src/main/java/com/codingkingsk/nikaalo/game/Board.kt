package com.codingkingsk.nikaalo.game

enum class Dir { H, V }

data class Vehicle(
	val id: Int,
	val type: String,
	val r: Int,
	val c: Int,
	val len: Int,
	val dir: Dir,
) {
	val isPlayer: Boolean get() = type == TYPE_AUTO

	/** Cows are permanent obstacles. */
	val movable: Boolean get() = type != TYPE_COW

	fun cells(): List<IntArray> = (0 until len).map { i ->
		if (dir == Dir.H) intArrayOf(r, c + i) else intArrayOf(r + i, c)
	}

	companion object {
		const val TYPE_AUTO = "auto"
		const val TYPE_COW = "cow"
	}
}

/**
 * Immutable board state. Every move returns a brand new [Board], which keeps the
 * Compose state model simple and makes BFS search trivial.
 */
class Board(val size: Int, val vehicles: List<Vehicle>) {

	private val grid = Array(size) { IntArray(size) { EMPTY } }

	init {
		for (vehicle in vehicles) {
			for (cell in vehicle.cells()) {
				val r = cell[0]
				val c = cell[1]
				require(r in 0 until size && c in 0 until size) {
					"Vehicle ${vehicle.id} (${vehicle.type}) is out of bounds at $r,$c"
				}
				require(grid[r][c] == EMPTY) {
					"Overlapping vehicles at $r,$c"
				}
				grid[r][c] = vehicle.id
			}
		}
	}

	val player: Vehicle get() = vehicles.first { it.isPlayer }

	/** Solved once the auto's right edge reaches the exit on the right wall. */
	val solved: Boolean
		get() = player.let { it.dir == Dir.H && it.c + it.len >= size }

	fun idAt(r: Int, c: Int): Int =
		if (r in 0 until size && c in 0 until size) grid[r][c] else WALL

	/**
	 * How far a vehicle may slide. Negative values are left (horizontal) or up
	 * (vertical); positive values are right or down.
	 */
	fun freeRange(vehicle: Vehicle): IntRange {
		if (!vehicle.movable) return 0..0
		var back = 0
		var forward = 0
		if (vehicle.dir == Dir.H) {
			var c = vehicle.c - 1
			while (c >= 0 && grid[vehicle.r][c] == EMPTY) {
				back++
				c--
			}
			var cf = vehicle.c + vehicle.len
			while (cf < size && grid[vehicle.r][cf] == EMPTY) {
				forward++
				cf++
			}
		} else {
			var r = vehicle.r - 1
			while (r >= 0 && grid[r][vehicle.c] == EMPTY) {
				back++
				r--
			}
			var rf = vehicle.r + vehicle.len
			while (rf < size && grid[rf][vehicle.c] == EMPTY) {
				forward++
				rf++
			}
		}
		return -back..forward
	}

	fun move(id: Int, delta: Int): Board {
		val updated = vehicles.map { vehicle ->
			when {
				vehicle.id != id -> vehicle
				vehicle.dir == Dir.H -> vehicle.copy(c = vehicle.c + delta)
				else -> vehicle.copy(r = vehicle.r + delta)
			}
		}
		return Board(size, updated)
	}

	/** Canonical state key used for de-duplication during search. */
	fun key(): String = vehicles.joinToString(";") { "${it.id}:${it.r},${it.c}" }

	companion object {
		const val EMPTY = -1
		const val WALL = -2

		fun from(level: LevelSpec): Board {
			val vehicles = level.vehicles.mapIndexed { index, spec ->
				Vehicle(
					id = index,
					type = spec.type,
					r = spec.r,
					c = spec.c,
					len = spec.len,
					dir = if (spec.dir.equals("h", ignoreCase = true)) Dir.H else Dir.V,
				)
			}
			return Board(level.grid, vehicles)
		}
	}
}
