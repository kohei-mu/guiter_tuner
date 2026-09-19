package com.example.guitartuner.audio

import kotlin.math.abs
import kotlin.math.ln

class PitchSmoother(
    private val windowSize: Int = MEDIAN_WINDOW_SIZE,
    private val alpha: Double = EMA_ALPHA,
) {
    private val values = ArrayDeque<Double>()
    private var ema: Double? = null

    fun add(frequency: Double): Double {
        val current = ema
        if (current != null && abs(cents(frequency, current)) > MAX_SMOOTHER_JUMP_CENTS) {
            return current
        }
        values.addLast(frequency)
        while (values.size > windowSize) values.removeFirst()
        val sorted = values.sorted()
        val median = if (sorted.size % 2 == 1) {
            sorted[sorted.size / 2]
        } else {
            (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
        }
        return ((ema?.let { alpha * median + (1.0 - alpha) * it }) ?: median)
            .also { ema = it }
    }

    fun reset() {
        values.clear()
        ema = null
    }

    companion object {
        const val MEDIAN_WINDOW_SIZE = 5
        const val EMA_ALPHA = 0.3
        const val MAX_SMOOTHER_JUMP_CENTS = 100.0

        private fun cents(frequency: Double, reference: Double): Double =
            1200.0 * ln(frequency / reference) / ln(2.0)
    }
}
