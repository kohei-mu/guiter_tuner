package com.example.guitartuner.audio

import kotlin.math.abs

class YinPitchDetector(
    private val threshold: Double = DEFAULT_THRESHOLD,
) : PitchDetector {
    override fun detect(
        samples: ShortArray,
        sampleRate: Int,
        minimumFrequency: Double,
        maximumFrequency: Double,
    ): Double? {
        if (
            samples.size < 4 || sampleRate <= 0 ||
            minimumFrequency <= 0.0 || maximumFrequency <= minimumFrequency
        ) return null

        val minTau = (sampleRate / maximumFrequency).toInt().coerceAtLeast(2)
        val maxTau = (sampleRate / minimumFrequency).toInt()
            .coerceAtMost(samples.size / 2 - 1)
        if (minTau >= maxTau) return null

        val difference = DoubleArray(maxTau + 1)
        for (tau in 1..maxTau) {
            var sum = 0.0
            val limit = samples.size - tau
            for (index in 0 until limit) {
                val delta = samples[index].toDouble() - samples[index + tau].toDouble()
                sum += delta * delta
            }
            difference[tau] = sum
        }

        val normalized = DoubleArray(maxTau + 1)
        normalized[0] = 1.0
        var runningSum = 0.0
        for (tau in 1..maxTau) {
            runningSum += difference[tau]
            normalized[tau] = if (runningSum == 0.0) 1.0 else difference[tau] * tau / runningSum
        }

        var tauEstimate = -1
        var tau = minTau
        while (tau <= maxTau) {
            if (normalized[tau] < threshold) {
                while (tau + 1 <= maxTau && normalized[tau + 1] < normalized[tau]) tau++
                tauEstimate = tau
                break
            }
            tau++
        }
        if (tauEstimate < 0) return null

        val refinedTau = parabolicInterpolation(normalized, tauEstimate)
        val frequency = sampleRate / refinedTau
        return frequency.takeIf { it in minimumFrequency..maximumFrequency && it.isFinite() }
    }

    private fun parabolicInterpolation(values: DoubleArray, index: Int): Double {
        if (index <= 0 || index >= values.lastIndex) return index.toDouble()
        val left = values[index - 1]
        val center = values[index]
        val right = values[index + 1]
        val denominator = 2.0 * (2.0 * center - right - left)
        if (abs(denominator) < 1e-12) return index.toDouble()
        return index + (right - left) / denominator
    }

    companion object {
        const val DEFAULT_THRESHOLD = 0.15
    }
}
