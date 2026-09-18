package com.example.guitartuner.audio

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class YinPitchDetectorTest {
    private val detector = YinPitchDetector()

    @Test fun `detects all standard tuning frequencies`() {
        listOf(82.41, 110.0, 146.83, 196.0, 246.94, 329.63).forEach { expected ->
            val actual = detector.detect(sineWave(expected), SAMPLE_RATE)
            assertNotNull("No pitch detected for $expected Hz", actual)
            assertEquals(expected, actual!!, 1.0)
        }
    }

    private fun sineWave(frequency: Double): ShortArray = ShortArray(4096) { index ->
        (sin(2.0 * PI * frequency * index / SAMPLE_RATE) * 12_000).toInt().toShort()
    }

    companion object { const val SAMPLE_RATE = 44_100 }
}
