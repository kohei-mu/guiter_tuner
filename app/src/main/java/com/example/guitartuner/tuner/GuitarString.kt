package com.example.guitartuner.tuner

data class GuitarString(
    val number: Int,
    val note: String,
    val frequencyHz: Double,
)

object GuitarTuning {
    // Standard tuning at A4 = 440 Hz, ordered as displayed on a guitar tuner.
    val standard = listOf(
        GuitarString(6, "E2", 82.41),
        GuitarString(5, "A2", 110.00),
        GuitarString(4, "D3", 146.83),
        GuitarString(3, "G3", 196.00),
        GuitarString(2, "B3", 246.94),
        GuitarString(1, "E4", 329.63),
    )
}
