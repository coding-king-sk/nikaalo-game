package com.codingkingsk.nikaalo.game

/**
 * Breadth-first search over board states. Because BFS explores by depth, the
 * first solution found is always the shortest one, so it doubles as:
 *  - a hint engine (first move of the optimal path)
 *  - a difficulty rating tool (length of the optimal path)
 */
object Solver {

	data class Move(val vehicleId: Int, val delta: Int)

	fun solve(start: Board, maxStates: Int = 150_000): List<Move>? {
		if (start.solved) return emptyList()

		val startKey = start.key()
		val parents = HashMap<String, Pair<String, Move>>()
		val seen = HashSet<String>()
		seen.add(startKey)

		val queue = ArrayDeque<Board>()
		queue.add(start)
		var expanded = 0

		while (queue.isNotEmpty() && expanded < maxStates) {
			val board = queue.removeFirst()
			expanded++
			val fromKey = board.key()

			for (vehicle in board.vehicles) {
				if (!vehicle.movable) continue
				for (delta in board.freeRange(vehicle)) {
					if (delta == 0) continue
					val next = board.move(vehicle.id, delta)
					val nextKey = next.key()
					if (!seen.add(nextKey)) continue
					parents[nextKey] = fromKey to Move(vehicle.id, delta)
					if (next.solved) return reconstruct(parents, startKey, nextKey)
					queue.add(next)
				}
			}
		}
		return null
	}

	fun minMoves(start: Board): Int? = solve(start)?.size

	private fun reconstruct(
		parents: Map<String, Pair<String, Move>>,
		startKey: String,
		endKey: String,
	): List<Move> {
		val moves = ArrayList<Move>()
		var cursor = endKey
		while (cursor != startKey) {
			val entry = parents[cursor] ?: break
			moves.add(entry.second)
			cursor = entry.first
		}
		return moves.reversed()
	}
}
