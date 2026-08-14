package com.codingkingsk.nikaalo.game

val CHAPTER_NAMES = listOf(
    "Gali Mohalla",
    "Sabzi Mandi",
    "Bus Stand",
    "Highway Toll",
    "Gaay Chowk",
    "Rush Hour Mumbai",
)

fun chapterName(chapter: Int): String =
    CHAPTER_NAMES.getOrElse(chapter - 1) { "Chapter $chapter" }

fun vehicleLabel(type: String): String = when (type) {
    "auto" -> "Auto"
    "bike" -> "Bike"
    "car" -> "Car"
    "thela" -> "Thela"
    "bus" -> "Bus"
    "truck" -> "Truck"
    "cow" -> "Gaay"
    else -> "Gaadi"
}
