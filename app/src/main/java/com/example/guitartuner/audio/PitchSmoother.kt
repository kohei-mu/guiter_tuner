package com.example.guitartuner.audio

class PitchSmoother(
    private val windowSize: Int = MEDIAN_WINDOW_SIZE,
    private val alpha: Double = EMA_ALPHA,
) {
    private val values = ArrayDeque<Double>()
    private var ema: Double? = null

    fun add(frequency: Double): Double {
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
    }
}
