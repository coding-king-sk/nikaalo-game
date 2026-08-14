package com.codingkingsk.nikaalo.game

import kotlinx.serialization.Serializable

/** Raw vehicle definition as stored in assets/levels.json. */
@Serializable
data class VehicleSpec(
	val type: String,
	val r: Int,
	val c: Int,
	val len: Int,
	val dir: String,
)

/** Raw level definition as stored in assets/levels.json. */
@Serializable
data class LevelSpec(
	val id: Int,
	val grid: Int = 6,
	val minMoves: Int = 0,
	val chapter: Int = 1,
	val vehicles: List<VehicleSpec>,
)
