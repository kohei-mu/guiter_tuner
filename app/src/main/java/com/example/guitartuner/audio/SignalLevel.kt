package com.example.guitartuner.audio

import kotlin.math.sqrt

/** Fast silence gate used before the more expensive pitch detector. */
object SignalLevel {
    // Keep this low: YIN, rather than input volume, determines whether a sound has a pitch.
    const val MIN_RMS = 100.0

    fun isAudible(samples: ShortArray, minimumRms: Double = MIN_RMS): Boolean {
        if (samples.isEmpty()) return false
        val meanSquare = samples.sumOf { it.toDouble() * it.toDouble() } / samples.size
        return sqrt(meanSquare) >= minimumRms
    }
}
