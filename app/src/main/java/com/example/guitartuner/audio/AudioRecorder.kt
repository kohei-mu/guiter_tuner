package com.example.guitartuner.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.math.max

class AudioRecorder {
    private val recordingMutex = Mutex()
    @Volatile private var isRecording = false
    @Volatile private var activeRecorder: AudioRecord? = null

    /** Captures transient buffers only. Samples are never persisted. */
    @SuppressLint("MissingPermission")
    suspend fun record(onSamples: (ShortArray) -> Unit) = withContext(Dispatchers.IO) {
        // A stopped AudioRecord can take a moment to unblock read(). Serialize sessions so
        // cleanup from the previous lifecycle cannot turn off a newly-created recorder.
        recordingMutex.withLock {
            recordSession(onSamples)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun recordSession(onSamples: (ShortArray) -> Unit) {
        val minimumBytes = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minimumBytes <= 0) return
        val bufferBytes = max(BUFFER_SAMPLES * Short.SIZE_BYTES, minimumBytes)
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferBytes,
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            return
        }

        activeRecorder = recorder
        isRecording = true
        try {
            recorder.startRecording()
            val buffer = ShortArray(BUFFER_SAMPLES)
            while (isRecording && coroutineContext.isActive) {
                val count = recorder.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
                if (count > 0) onSamples(buffer.copyOf(count))
            }
        } finally {
            isRecording = false
            activeRecorder = null
            if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) recorder.stop()
            recorder.release()
        }
    }

    fun stop() {
        isRecording = false
        runCatching { activeRecorder?.stop() }
    }

    companion object {
        const val SAMPLE_RATE = 44_100
        const val BUFFER_SAMPLES = 4_096
    }
}
