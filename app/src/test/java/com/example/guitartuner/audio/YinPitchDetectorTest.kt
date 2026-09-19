package com.example.guitartuner.audio

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class YinPitchDetectorTest {
    private val detector = YinPitchDetector()

    @Test fun `detects all standard tuning frequencies`() {
        listOf(82.41, 110.0, 146.83, 196.0, 246.94, 329.63).forEach { expected ->
            val range = PitchDetectionRange.forTarget(expected)
            val actual = detector.detect(
                sineWave(expected), SAMPLE_RATE, range.minimumFrequency, range.maximumFrequency,
            )
            assertNotNull("No pitch detected for $expected Hz", actual)
            assertEquals(expected, actual!!, 1.0)
        }
    }

    @Test fun `frequency outside selected string range is rejected`() {
        val range = PitchDetectionRange.forTarget(110.0)
        val actual = detector.detect(
            sineWave(155.0), SAMPLE_RATE, range.minimumFrequency, range.maximumFrequency,
        )

        assertEquals(null, actual)
    }

    @Test fun `detects fundamental when second harmonic is stronger`() {
        val range = PitchDetectionRange.forTarget(110.0)
        val raw = detector.detect(
            harmonicWave(noiseAmplitude = 0.0), SAMPLE_RATE,
            range.minimumFrequency, range.maximumFrequency,
        )

        assertNotNull(raw)
        assertEquals(110.0, PitchCandidateResolver().resolve(raw!!, 110.0), 1.0)
    }

    @Test fun `detects noisy guitar-like harmonic signal deterministically`() {
        val range = PitchDetectionRange.forTarget(110.0)
        val raw = detector.detect(
            harmonicWave(noiseAmplitude = 300.0), SAMPLE_RATE,
            range.minimumFrequency, range.maximumFrequency,
        )

        assertNotNull(raw)
        assertEquals(110.0, PitchCandidateResolver().resolve(raw!!, 110.0), 1.0)
    }

    private fun sineWave(frequency: Double): ShortArray = ShortArray(4096) { index ->
        (sin(2.0 * PI * frequency * index / SAMPLE_RATE) * 12_000).toInt().toShort()
    }

    private fun harmonicWave(noiseAmplitude: Double): ShortArray {
        val random = Random(0)
        return ShortArray(4096) { index ->
            val t = index.toDouble() / SAMPLE_RATE
            val signal =
                sin(2.0 * PI * 110.0 * t) * 0.5 +
                    sin(2.0 * PI * 220.0 * t) * 1.0 +
                    sin(2.0 * PI * 330.0 * t) * 0.4
            val noise = if (noiseAmplitude == 0.0) 0.0 else {
                random.nextDouble(-noiseAmplitude, noiseAmplitude)
            }
            (signal * 8_000 + noise)
                .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                .toInt().toShort()
        }
    }

    companion object { const val SAMPLE_RATE = 44_100 }
}
