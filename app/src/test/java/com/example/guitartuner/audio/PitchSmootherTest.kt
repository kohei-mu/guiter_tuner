package com.example.guitartuner.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class PitchSmootherTest {
    @Test fun `reset clears median and EMA history`() {
        val smoother = PitchSmoother()
        repeat(5) { smoother.add(100.0) }
        smoother.reset()
        assertEquals(200.0, smoother.add(200.0), 0.0001)
    }
}
