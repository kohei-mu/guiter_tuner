package com.example.guitartuner.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class PitchCandidateResolverTest {
    private val resolver = PitchCandidateResolver()

    @Test fun `fundamental remains unchanged`() {
        assertEquals(110.0, resolver.resolve(110.0, 110.0), 0.0001)
    }

    @Test fun `second harmonic resolves to fundamental`() {
        assertEquals(110.0, resolver.resolve(220.0, 110.0), 0.0001)
    }

    @Test fun `third harmonic resolves to fundamental`() {
        assertEquals(110.0, resolver.resolve(330.0, 110.0), 0.0001)
    }

    @Test fun `plausible non-harmonic is not forced to target`() {
        assertEquals(146.0, resolver.resolve(146.0, 110.0), 0.0001)
    }

    @Test fun `target range is ratio based and absolutely bounded`() {
        assertEquals(PitchDetectionRange(82.5, 148.5), PitchDetectionRange.forTarget(110.0))
        assertEquals(60.0, PitchDetectionRange.forTarget(70.0).minimumFrequency, 0.0001)
        assertEquals(400.0, PitchDetectionRange.forTarget(329.63).maximumFrequency, 0.0001)
    }
}
