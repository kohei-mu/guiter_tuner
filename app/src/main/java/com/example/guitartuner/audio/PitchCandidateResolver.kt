package com.example.guitartuner.audio

import kotlin.math.abs

/** Applies guitar-specific octave correction without coupling it to the YIN algorithm. */
class PitchCandidateResolver {
    fun resolve(rawFrequency: Double, targetFrequency: Double): Double {
        val range = PitchDetectionRange.forTarget(targetFrequency)
        if (rawFrequency in range.minimumFrequency..range.maximumFrequency) return rawFrequency

        return (2..MAX_HARMONIC)
            .map { rawFrequency / it }
            .filter { it in range.minimumFrequency..range.maximumFrequency }
            .minByOrNull { abs(it - targetFrequency) }
            ?: rawFrequency
    }

    private companion object {
        const val MAX_HARMONIC = 3
    }
}

data class PitchDetectionRange(
    val minimumFrequency: Double,
    val maximumFrequency: Double,
) {
    companion object {
        const val ABSOLUTE_MINIMUM_HZ = 60.0
        const val ABSOLUTE_MAXIMUM_HZ = 400.0
        private const val MINIMUM_TARGET_RATIO = 0.75
        private const val MAXIMUM_TARGET_RATIO = 1.35

        fun forTarget(targetFrequency: Double) = PitchDetectionRange(
            minimumFrequency = (targetFrequency * MINIMUM_TARGET_RATIO)
                .coerceAtLeast(ABSOLUTE_MINIMUM_HZ),
            maximumFrequency = (targetFrequency * MAXIMUM_TARGET_RATIO)
                .coerceAtMost(ABSOLUTE_MAXIMUM_HZ),
        )
    }
}
