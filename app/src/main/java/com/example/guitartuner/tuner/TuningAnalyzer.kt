package com.example.guitartuner.tuner

import kotlin.math.ln

object TuningAnalyzer {
    const val IN_TUNE_CENTS = 5.0
    const val MAX_RELEVANT_CENTS = 300.0

    fun cents(detectedFrequency: Double, targetFrequency: Double): Double =
        1200.0 * (ln(detectedFrequency / targetFrequency) / ln(2.0))

    fun analyze(selectedString: GuitarString, detectedFrequency: Double): TunerState {
        val cents = cents(detectedFrequency, selectedString.frequencyHz)
        if (kotlin.math.abs(cents) > MAX_RELEVANT_CENTS) {
            return TunerState(
                selectedString = selectedString,
                detectedFrequencyHz = detectedFrequency,
                status = TuningStatus.DETECTING,
                isOutOfRange = true,
            )
        }
        val status = when {
            cents < -IN_TUNE_CENTS -> TuningStatus.LOW
            cents > IN_TUNE_CENTS -> TuningStatus.HIGH
            else -> TuningStatus.IN_TUNE
        }
        return TunerState(
            selectedString = selectedString,
            detectedFrequencyHz = detectedFrequency,
            cents = cents,
            meterCents = cents.coerceIn(-50.0, 50.0).toFloat(),
            status = status,
        )
    }
}
