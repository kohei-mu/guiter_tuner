package com.example.guitartuner.tuner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitartuner.audio.AudioRecorder
import com.example.guitartuner.audio.PitchSmoother
import com.example.guitartuner.audio.SignalLevel
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
    private val _state = MutableStateFlow(TunerState())
    val state: StateFlow<TunerState> = _state.asStateFlow()
    private var recordingJob: Job? = null

    fun selectString(string: GuitarString) {
        smoother.reset()
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
        val selected = _state.value.selectedString
        _state.value = _state.value.copy(
            detectedFrequencyHz = null,
            cents = null,
            status = if (selected == null) TuningStatus.NO_STRING_SELECTED else TuningStatus.WAITING,
        )
    }

    private fun processSamples(samples: ShortArray) {
        val selected = _state.value.selectedString ?: return
        if (!SignalLevel.isAudible(samples)) {
            _state.value = _state.value.copy(
                detectedFrequencyHz = null,
                cents = null,
                status = TuningStatus.WAITING,
                isOutOfRange = false,
            )
            return
        }
        val detected = pitchDetector.detect(samples, AudioRecorder.SAMPLE_RATE)
        if (detected == null) {
            _state.value = _state.value.copy(status = TuningStatus.DETECTING)
            return
        }
        val analyzed = TuningAnalyzer.analyze(selected, smoother.add(detected))
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
}
