package com.nothing.tune.player

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.sqrt

class RealtimePcmAudioProcessor : BaseAudioProcessor() {

    private val barCount = 26
    private val _frequencyBands = MutableStateFlow(FloatArray(barCount) { 0.12f })
    val frequencyBands: StateFlow<FloatArray> = _frequencyBands.asStateFlow()

    private val smoothedBands = FloatArray(barCount) { 0.12f }
    private var lastLowPassSample = 0f

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return inputAudioFormat // Passthrough format
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        // Duplicate buffer to analyze raw PCM without consuming position for playback
        val duplicate = inputBuffer.duplicate().order(ByteOrder.LITTLE_ENDIAN)
        analyzePcmBuffer(duplicate)

        // Pass audio data untouched to output buffer for audio playback
        val outputBuffer = replaceOutputBuffer(remaining)
        outputBuffer.put(inputBuffer)
        outputBuffer.flip()
    }

    private fun analyzePcmBuffer(buffer: ByteBuffer) {
        val totalBytes = buffer.remaining()
        val totalShorts = totalBytes / 2
        if (totalShorts < 8) return

        var sumSquare = 0.0
        var bassEnergy = 0.0
        var midEnergy = 0.0
        var trebleEnergy = 0.0

        val shortBuf = buffer.asShortBuffer()
        val count = shortBuf.remaining().coerceAtMost(512)

        for (i in 0 until count) {
            val sample = shortBuf.get().toFloat() / 32768.0f // Normalized -1.0 .. 1.0
            sumSquare += (sample * sample)

            // Low-pass filter for real bass / kick drum detection (cutoff ~250Hz)
            val alpha = 0.08f
            val lowPass = lastLowPassSample + alpha * (sample - lastLowPassSample)
            lastLowPassSample = lowPass

            val highPass = sample - lowPass // Treble / cymbals
            val bandPass = abs(sample) - abs(lowPass) // Midrange / vocals

            bassEnergy += abs(lowPass)
            midEnergy += abs(bandPass)
            trebleEnergy += abs(highPass)
        }

        val rms = sqrt(sumSquare / count).toFloat().coerceIn(0f, 1f)
        val bassNorm = (bassEnergy / count).toFloat().coerceIn(0f, 1f) * 2.8f
        val midNorm = (midEnergy / count).toFloat().coerceIn(0f, 1f) * 2.2f
        val trebleNorm = (trebleEnergy / count).toFloat().coerceIn(0f, 1f) * 3.2f

        val newBands = FloatArray(barCount)
        for (i in 0 until barCount) {
            val frac = i.toFloat() / barCount.toFloat()
            val target = when {
                frac < 0.28f -> {
                    // Bass & sub-bass columns (left)
                    val factor = 1.0f - (frac / 0.28f) * 0.3f
                    (bassNorm * factor * 0.9f + rms * 0.3f)
                }
                frac < 0.68f -> {
                    // Mid-range & vocal columns (center)
                    val factor = 1.0f - abs(frac - 0.48f) * 1.5f
                    (midNorm * factor * 0.85f + rms * 0.25f)
                }
                else -> {
                    // High-frequency & cymbals columns (right)
                    val factor = ((frac - 0.68f) / 0.32f) * 0.8f + 0.2f
                    (trebleNorm * factor * 0.85f + rms * 0.2f)
                }
            }.coerceIn(0.12f, 1.0f)

            // Fast attack, organic decay
            smoothedBands[i] = if (target > smoothedBands[i]) {
                smoothedBands[i] * 0.25f + target * 0.75f
            } else {
                smoothedBands[i] * 0.78f + target * 0.22f
            }
            newBands[i] = smoothedBands[i]
        }

        _frequencyBands.value = newBands
    }

    override fun onReset() {
        super.onReset()
        resetBands()
    }

    fun resetBands() {
        for (i in 0 until barCount) {
            smoothedBands[i] = 0.12f
        }
        _frequencyBands.value = FloatArray(barCount) { 0.12f }
    }
}
