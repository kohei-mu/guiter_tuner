package com.example.guitartuner.audio

import kotlin.math.sqrt

/**
 * Distinguishes actual silence from a recently played, decaying note.
 *
 * A short release period prevents adjacent audio buffers near the noise floor from making the
 * UI alternate between "waiting" and "detecting" while a string is still ringing.
 */
class SignalActivityDetector(
    private val minimumRms: Double = MIN_RMS,
    private val releaseBuffers: Int = RELEASE_BUFFERS,
) {
    private var quietBuffersRemaining = 0

    @Synchronized
    fun isActive(samples: ShortArray): Boolean {
        if (rms(samples) >= minimumRms) {
            quietBuffersRemaining = releaseBuffers
            return true
        }

        if (quietBuffersRemaining > 0) {
            quietBuffersRemaining--
            return true
        }
        return false
    }

    @Synchronized
    fun reset() {
        quietBuffersRemaining = 0
    }

    private fun rms(samples: ShortArray): Double {
        if (samples.isEmpty()) return 0.0
        val meanSquare = samples.sumOf { it.toDouble() * it.toDouble() } / samples.size
        return sqrt(meanSquare)
    }

    companion object {
        // The YIN confidence check rejects unpitched noise; this gate should reject only silence.
        const val MIN_RMS = 10.0
        const val RELEASE_BUFFERS = 8
    }
}
