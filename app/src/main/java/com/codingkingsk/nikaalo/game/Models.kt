package com.codingkingsk.nikaalo.game

/**
 * A single piece on the board as loaded from `assets/levels.txt`.
 *
 * @param type one of auto, bike, car, thela, bus, truck, cow
 * @param r    top row of the piece
 * @param c    left column of the piece
 * @param len  length in cells
 * @param dir  "h" for horizontal, "v" for vertical
 */
data class VehicleSpec(
    val type: String,
    val r: Int,
    val c: Int,
    val len: Int,
    val dir: String,
)

/**
 * @param minMoves the true optimal solution length, verified by BFS at
 *                 generation time. Used for the move target and star rating.
 */
data class LevelSpec(
    val id: Int,
    val grid: Int = 6,
    val minMoves: Int = 0,
    val chapter: Int = 1,
    val vehicles: List<VehicleSpec>,
)
