package com.nothing.tune

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

object EqualizerPlugin {

    private const val CHANNEL = "com.nothing.tune/equalizer"

    // Audio session 0 = global effect (works with just_audio)
    private var eq: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudness: LoudnessEnhancer? = null

    private const val SESSION_ID = 0

    fun register(flutterEngine: FlutterEngine) {
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            try {
                when (call.method) {
                    "init" -> {
                        initEffects()
                        val eqRef = eq
                        if (eqRef != null) {
                            val bandCount = eqRef.numberOfBands.toInt()
                            val freqs = (0 until bandCount).map {
                                eqRef.getCenterFreq(it.toShort()) / 1000 // mHz → Hz
                            }
                            result.success(mapOf("bandCount" to bandCount, "bandFreqs" to freqs))
                        } else {
                            result.success(null)
                        }
                    }

                    "setEnabled" -> {
                        val enabled = call.argument<Boolean>("enabled") ?: false
                        eq?.enabled = enabled
                        bassBoost?.enabled = enabled
                        virtualizer?.enabled = enabled
                        loudness?.enabled = enabled
                        result.success(null)
                    }

                    "setBand" -> {
                        val band = call.argument<Int>("band")?.toShort() ?: 0
                        val level = call.argument<Int>("level") ?: 0
                        eq?.setBandLevel(band, level.toShort())
                        result.success(null)
                    }

                    "setBassBoost" -> {
                        val strength = call.argument<Int>("strength") ?: 0
                        bassBoost?.setStrength(strength.toShort())
                        result.success(null)
                    }

                    "setVirtualizer" -> {
                        val strength = call.argument<Int>("strength") ?: 0
                        virtualizer?.setStrength(strength.toShort())
                        result.success(null)
                    }

                    "setLoudness" -> {
                        val enabled = call.argument<Boolean>("enabled") ?: false
                        if (enabled) {
                            loudness?.setTargetGain(500)
                        }
                        loudness?.enabled = enabled
                        result.success(null)
                    }

                    else -> result.notImplemented()
                }
            } catch (e: Exception) {
                result.error("EQ_ERROR", e.message, null)
            }
        }
    }

    private fun initEffects() {
        try {
            if (eq == null) {
                eq = Equalizer(0, SESSION_ID).apply { enabled = false }
            }
            if (bassBoost == null) {
                bassBoost = BassBoost(0, SESSION_ID).apply { enabled = false }
            }
            if (virtualizer == null) {
                virtualizer = Virtualizer(0, SESSION_ID).apply { enabled = false }
            }
            if (loudness == null) {
                loudness = LoudnessEnhancer(SESSION_ID).apply { enabled = false }
            }
        } catch (_: Exception) {}
    }
}
