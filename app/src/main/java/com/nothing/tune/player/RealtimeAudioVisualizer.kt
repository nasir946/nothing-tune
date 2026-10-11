package com.nothing.tune.player

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.hypot
import kotlin.math.sin

class RealtimeAudioVisualizer {

    private var visualizer: Visualizer? = null
    private var currentSessionId: Int = 0

    private val barCount = 26
    private val _frequencyBands = MutableStateFlow(FloatArray(barCount) { 0.15f })
    val frequencyBands: StateFlow<FloatArray> = _frequencyBands.asStateFlow()

    private val smoothedValues = FloatArray(barCount) { 0.15f }

    fun attachSession(sessionId: Int) {
        if (sessionId <= 0 || sessionId == currentSessionId) return
        release()
        currentSessionId = sessionId

        try {
            val viz = Visualizer(sessionId).apply {
                captureSize = 256.coerceIn(Visualizer.getCaptureSizeRange()[0], Visualizer.getCaptureSizeRange()[1])
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            // Waveform PCM data capture (amplitude peaks)
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (fft == null || fft.isEmpty()) return
                            processFftData(fft)
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    false,
                    true
                )
                enabled = true
            }
            visualizer = viz
        } catch (_: Throwable) {
            // Permission or hardware unsupported, will use dynamic rhythm synthesizer
        }
    }

    private fun processFftData(fft: ByteArray) {
        val n = (fft.size / 2).coerceAtLeast(1)
        val step = (n.toFloat() / barCount.toFloat()).coerceAtLeast(1f)
        val newValues = FloatArray(barCount)

        for (i in 0 until barCount) {
            val startIdx = (i * step).toInt().coerceIn(0, n - 1)
            val endIdx = ((i + 1) * step).toInt().coerceIn(startIdx + 1, n)

            var sumMag = 0f
            var count = 0
            for (k in startIdx until endIdx) {
                val real = fft[2 * k].toFloat()
                val imag = fft[2 * k + 1].toFloat()
                sumMag += hypot(real, imag)
                count++
            }

            val avgMag = if (count > 0) (sumMag / count) / 100f else 0f
            // Bass bands (lower i) naturally have more energy, scale higher frequencies
            val boost = 1.0f + (i.toFloat() / barCount.toFloat()) * 1.6f
            val target = (avgMag * boost).coerceIn(0.12f, 1.0f)

            // Smooth attack & decay for organic motion
            smoothedValues[i] = if (target > smoothedValues[i]) {
                smoothedValues[i] * 0.4f + target * 0.6f
            } else {
                smoothedValues[i] * 0.82f + target * 0.18f
            }
            newValues[i] = smoothedValues[i]
        }

        _frequencyBands.value = newValues
    }

    fun updateSimulatedFrame(isPlaying: Boolean, timeMs: Long) {
        if (visualizer != null) return // Using real hardware FFT

        if (!isPlaying) {
            _frequencyBands.value = FloatArray(barCount) { 0.12f }
            return
        }

        val t = timeMs / 1000.0
        val arr = FloatArray(barCount)

        // Beat rhythm pulses on low frequencies (kick drum)
        val beatPulse = (sin(t * 8.5) * 0.5 + 0.5).toFloat()
        val snarePulse = (sin(t * 4.25) * 0.5 + 0.5).toFloat()

        for (i in 0 until barCount) {
            val fraction = i.toFloat() / barCount.toFloat()
            val valAtFreq = when {
                fraction < 0.25f -> 0.3f + 0.65f * beatPulse * (sin(t * 12.0 + i) * 0.3f + 0.7f).toFloat() // Bass / Low pitch
                fraction < 0.65f -> 0.25f + 0.55f * snarePulse * (sin(t * 16.0 + i * 2) * 0.4f + 0.6f).toFloat() // Mid pitch
                else -> 0.2f + 0.6f * (sin(t * 22.0 + i * 3) * 0.5f + 0.5f).toFloat() // High pitch / Treble
            }
            arr[i] = valAtFreq.coerceIn(0.15f, 0.98f)
        }

        _frequencyBands.value = arr
    }

    fun release() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
            visualizer = null
            currentSessionId = 0
        } catch (_: Throwable) {}
    }
}
