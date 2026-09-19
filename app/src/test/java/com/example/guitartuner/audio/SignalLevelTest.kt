package com.example.guitartuner.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class SignalLevelTest {
    @Test fun `silence does not enter pitch detection`() {
        assertFalse(SignalLevel.isAudible(ShortArray(4096)))
    }

    @Test fun `quiet guitar-like signal enters pitch detection`() {
        val samples = ShortArray(4096) { index ->
            (sin(2.0 * PI * 110.0 * index / 44_100) * 200.0).toInt().toShort()
        }

        assertTrue(SignalLevel.isAudible(samples))
    }
}
