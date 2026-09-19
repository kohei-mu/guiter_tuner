package com.example.guitartuner.tuner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitartuner.audio.AudioRecorder
import com.example.guitartuner.audio.PitchCandidateResolver
import com.example.guitartuner.audio.PitchDetectionRange
import com.example.guitartuner.audio.PitchSmoother
import com.example.guitartuner.audio.SignalActivityDetector
import com.example.guitartuner.audio.YinPitchDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TunerViewModel : ViewModel() {
    private val recorder = AudioRecorder()
    private val pitchDetector = YinPitchDetector()
    private val smoother = PitchSmoother()
    private val candidateResolver = PitchCandidateResolver()
    private val signalActivityDetector = SignalActivityDetector()
    private val _state = MutableStateFlow(TunerState())
    val state: StateFlow<TunerState> = _state.asStateFlow()
    private var recordingJob: Job? = null
    private var consecutivePitchFailures = 0

    fun selectString(string: GuitarString) {
        smoother.reset()
        signalActivityDetector.reset()
        consecutivePitchFailures = 0
        _state.value = TunerState(
            selectedString = string,
            status = TuningStatus.WAITING,
            hasMicrophonePermission = _state.value.hasMicrophonePermission,
            permissionDenied = _state.value.permissionDenied,
        )
    }

    fun updatePermission(granted: Boolean, denied: Boolean? = null) {
        _state.value = _state.value.copy(
            hasMicrophonePermission = granted,
            permissionDenied = denied ?: if (granted) false else _state.value.permissionDenied,
        )
        if (!granted) stopListening()
    }

    fun startListening() {
        if (!_state.value.hasMicrophonePermission || recordingJob?.isActive == true) return
        recordingJob = viewModelScope.launch {
            recorder.record(::processSamples)
        }
    }

    fun stopListening() {
        recorder.stop()
        recordingJob?.cancel()
        recordingJob = null
        signalActivityDetector.reset()
        consecutivePitchFailures = 0
        val selected = _state.value.selectedString
        _state.value = _state.value.copy(
            detectedFrequencyHz = null,
            cents = null,
            status = if (selected == null) TuningStatus.NO_STRING_SELECTED else TuningStatus.WAITING,
        )
    }

    private fun processSamples(samples: ShortArray) {
        val selected = _state.value.selectedString ?: return
        if (!signalActivityDetector.isActive(samples)) {
            consecutivePitchFailures = 0
            _state.value = _state.value.copy(
                detectedFrequencyHz = null,
                cents = null,
                status = TuningStatus.WAITING,
                isOutOfRange = false,
            )
            return
        }
        val range = PitchDetectionRange.forTarget(selected.frequencyHz)
        val detected = pitchDetector.detect(
            samples,
            AudioRecorder.SAMPLE_RATE,
            range.minimumFrequency,
            range.maximumFrequency,
        )
        if (detected == null) {
            consecutivePitchFailures++
            if (consecutivePitchFailures > MAX_TRANSIENT_PITCH_FAILURES || _state.value.cents == null) {
                _state.value = _state.value.copy(status = TuningStatus.DETECTING)
            }
            return
        }
        consecutivePitchFailures = 0
        val resolved = candidateResolver.resolve(detected, selected.frequencyHz)
        val analyzed = TuningAnalyzer.analyze(selected, smoother.add(resolved))
        _state.value = analyzed.copy(
            meterCents = if (analyzed.isOutOfRange) _state.value.meterCents else analyzed.meterCents,
            hasMicrophonePermission = _state.value.hasMicrophonePermission,
            permissionDenied = _state.value.permissionDenied,
        )
    }

    override fun onCleared() {
        recorder.stop()
        super.onCleared()
    }

    private companion object {
        const val MAX_TRANSIENT_PITCH_FAILURES = 2
    }
}
