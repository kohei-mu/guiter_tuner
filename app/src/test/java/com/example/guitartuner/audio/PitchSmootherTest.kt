package com.example.guitartuner.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class PitchSmootherTest {
    @Test fun `octave outlier does not disturb stable pitch`() {
        val smoother = PitchSmoother()
        repeat(5) { smoother.add(110.0) }

        assertEquals(110.0, smoother.add(220.0), 0.0001)
        assertEquals(110.0, smoother.add(110.0), 0.0001)
    }

    @Test fun `small variations continue to update smoothed pitch`() {
        val smoother = PitchSmoother()
        smoother.add(110.0)

        assertEquals(110.15, smoother.add(111.0), 0.0001)
    }

    @Test fun `reset clears median and EMA history`() {
        val smoother = PitchSmoother()
        repeat(5) { smoother.add(100.0) }
        smoother.reset()
        assertEquals(200.0, smoother.add(200.0), 0.0001)
    }
}
