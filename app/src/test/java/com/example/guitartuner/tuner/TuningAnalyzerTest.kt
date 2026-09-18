package com.example.guitartuner.tuner

import org.junit.Assert.assertEquals
import org.junit.Test

class TuningAnalyzerTest {
    private val aString = GuitarTuning.standard.first { it.number == 5 }

    @Test fun `equal frequencies are zero cents`() {
        assertEquals(0.0, TuningAnalyzer.cents(440.0, 440.0), 0.0001)
    }

    @Test fun `one octave is 1200 cents`() {
        assertEquals(1200.0, TuningAnalyzer.cents(880.0, 440.0), 0.0001)
    }

    @Test fun `selected A string at 110 Hz is in tune`() {
        assertEquals(TuningStatus.IN_TUNE, TuningAnalyzer.analyze(aString, 110.0).status)
    }

    @Test fun `frequency below selected A string is low`() {
        assertEquals(TuningStatus.LOW, TuningAnalyzer.analyze(aString, 109.0).status)
    }

    @Test fun `frequency above selected A string is high`() {
        assertEquals(TuningStatus.HIGH, TuningAnalyzer.analyze(aString, 111.0).status)
    }

    @Test fun `analysis never changes selected string`() {
        val result = TuningAnalyzer.analyze(aString, 82.41)
        assertEquals(aString, result.selectedString)
        assertEquals(5, result.selectedString?.number)
        assertEquals(true, result.isOutOfRange)
    }
}
