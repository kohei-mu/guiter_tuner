package com.example.guitartuner.audio

interface PitchDetector {
    /** Returns the fundamental frequency in Hz, or null when no reliable pitch exists. */
    fun detect(
        samples: ShortArray,
        sampleRate: Int,
        minimumFrequency: Double,
        maximumFrequency: Double,
    ): Double?
}
