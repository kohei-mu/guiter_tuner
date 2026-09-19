package com.example.guitartuner.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class SignalActivityDetectorTest {
    @Test fun `silence does not enter pitch detection`() {
        assertFalse(SignalActivityDetector().isActive(ShortArray(4096)))
    }

    @Test fun `very quiet guitar-like signal enters pitch detection`() {
        val samples = ShortArray(4096) { index ->
            (sin(2.0 * PI * 110.0 * index / 44_100) * 20.0).toInt().toShort()
        }

        assertTrue(SignalActivityDetector().isActive(samples))
    }

    @Test fun `brief quiet buffers do not make activity flicker`() {
        val detector = SignalActivityDetector(minimumRms = 10.0, releaseBuffers = 2)
        val audible = ShortArray(32) { 20 }
        val silence = ShortArray(32)

        assertTrue(detector.isActive(audible))
        assertTrue(detector.isActive(silence))
        assertTrue(detector.isActive(silence))
        assertFalse(detector.isActive(silence))
    }

    @Test fun `reset clears release period`() {
        val detector = SignalActivityDetector(minimumRms = 10.0, releaseBuffers = 8)
        detector.isActive(ShortArray(32) { 20 })

        detector.reset()

        assertFalse(detector.isActive(ShortArray(32)))
    }
}
