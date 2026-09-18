package com.example.guitartuner.tuner

enum class TuningStatus { NO_STRING_SELECTED, WAITING, DETECTING, LOW, IN_TUNE, HIGH }

data class TunerState(
    val selectedString: GuitarString? = null,
    val detectedFrequencyHz: Double? = null,
    val cents: Double? = null,
    val meterCents: Float = 0f,
    val status: TuningStatus = TuningStatus.NO_STRING_SELECTED,
    val isOutOfRange: Boolean = false,
    val hasMicrophonePermission: Boolean = false,
    val permissionDenied: Boolean = false,
)
