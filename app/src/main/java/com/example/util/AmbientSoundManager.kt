package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.model.AmbientSoundType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

object AmbientSoundManager {
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE_SAMPLES = 4096

    @Volatile
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var isPlaying = false

    private var synthesisThread: Thread? = null

    private val _currentSound = MutableStateFlow(AmbientSoundType.NONE)
    val currentSound: StateFlow<AmbientSoundType> = _currentSound.asStateFlow()

    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isAudioActive = MutableStateFlow(false)
    val isAudioActive: StateFlow<Boolean> = _isAudioActive.asStateFlow()

    private val _autoPlayWithTimer = MutableStateFlow(true)
    val autoPlayWithTimer: StateFlow<Boolean> = _autoPlayWithTimer.asStateFlow()

    fun setAutoPlayWithTimer(enabled: Boolean) {
        _autoPlayWithTimer.value = enabled
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            audioTrack?.setVolume(clamped)
        } catch (e: Exception) {
            // Track might be uninitialized
        }
    }

    fun selectSound(sound: AmbientSoundType) {
        _currentSound.value = sound
        if (sound == AmbientSoundType.NONE) {
            stop()
        } else {
            start()
        }
    }

    fun togglePlayPause() {
        if (_isAudioActive.value) {
            pause()
        } else {
            if (_currentSound.value == AmbientSoundType.NONE) {
                _currentSound.value = AmbientSoundType.BROWN_NOISE
            }
            start()
        }
    }

    @Synchronized
    fun start() {
        if (_currentSound.value == AmbientSoundType.NONE) {
            _currentSound.value = AmbientSoundType.BROWN_NOISE
        }

        if (isPlaying) return
        stopInternal()

        isPlaying = true
        _isAudioActive.value = true

        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = (minBufferSize * 2).coerceAtLeast(BUFFER_SIZE_SAMPLES * 4)

        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            track.setVolume(_volume.value)
            track.play()
            audioTrack = track

            synthesisThread = Thread({
                synthesizeAudioLoop(track)
            }, "AmbientFocusSynthThread").apply {
                priority = Thread.NORM_PRIORITY
                start()
            }
        } catch (e: Exception) {
            isPlaying = false
            _isAudioActive.value = false
            stopInternal()
        }
    }

    @Synchronized
    fun pause() {
        isPlaying = false
        _isAudioActive.value = false
        stopInternal()
    }

    @Synchronized
    fun stop() {
        isPlaying = false
        _isAudioActive.value = false
        stopInternal()
    }

    private fun stopInternal() {
        val oldThread = synthesisThread
        synthesisThread = null
        oldThread?.interrupt()

        val track = audioTrack
        audioTrack = null
        if (track != null) {
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    private fun synthesizeAudioLoop(track: AudioTrack) {
        val pcmBuffer = ShortArray(BUFFER_SIZE_SAMPLES * 2) // Stereo: L, R interleaved
        val random = Random()

        var lastBrownL = 0.0
        var lastBrownR = 0.0

        var b0L = 0.0; var b1L = 0.0; var b2L = 0.0; var b3L = 0.0; var b4L = 0.0; var b5L = 0.0; var b6L = 0.0
        var b0R = 0.0; var b1R = 0.0; var b2R = 0.0; var b3R = 0.0; var b4R = 0.0; var b5R = 0.0; var b6R = 0.0

        var phaseL = 0.0
        var phaseR = 0.0
        var lfoPhase = 0.0
        var sampleCounter: Long = 0
        var crackleDecay = 0.0

        try {
            while (isPlaying && !Thread.currentThread().isInterrupted) {
                val sound = _currentSound.value
                if (sound == AmbientSoundType.NONE) {
                    break
                }

                for (i in 0 until BUFFER_SIZE_SAMPLES) {
                    sampleCounter++
                    var leftSample = 0.0
                    var rightSample = 0.0

                    when (sound) {
                        AmbientSoundType.WHITE_NOISE -> {
                            leftSample = (random.nextDouble() * 2.0 - 1.0) * 0.28
                            rightSample = (random.nextDouble() * 2.0 - 1.0) * 0.28
                        }

                        AmbientSoundType.BROWN_NOISE -> {
                            val whiteL = random.nextDouble() * 2.0 - 1.0
                            val whiteR = random.nextDouble() * 2.0 - 1.0
                            lastBrownL = (lastBrownL + (0.025 * whiteL)) / 1.025
                            lastBrownR = (lastBrownR + (0.025 * whiteR)) / 1.025
                            leftSample = lastBrownL * 1.8
                            rightSample = lastBrownR * 1.8
                        }

                        AmbientSoundType.PINK_RAIN -> {
                            val whiteL = random.nextDouble() * 2.0 - 1.0
                            val whiteR = random.nextDouble() * 2.0 - 1.0

                            b0L = 0.99886 * b0L + whiteL * 0.0555179
                            b1L = 0.99332 * b1L + whiteL * 0.0750759
                            b2L = 0.96900 * b2L + whiteL * 0.1538520
                            b3L = 0.86650 * b3L + whiteL * 0.3104856
                            b4L = 0.55000 * b4L + whiteL * 0.5329522
                            b5L = -0.7616 * b5L - whiteL * 0.0168980
                            val pinkL = b0L + b1L + b2L + b3L + b4L + b5L + b6L + whiteL * 0.5362
                            b6L = whiteL * 0.115926

                            b0R = 0.99886 * b0R + whiteR * 0.0555179
                            b1R = 0.99332 * b1R + whiteR * 0.0750759
                            b2R = 0.96900 * b2R + whiteR * 0.1538520
                            b3R = 0.86650 * b3R + whiteR * 0.3104856
                            b4R = 0.55000 * b4R + whiteR * 0.5329522
                            b5R = -0.7616 * b5R - whiteR * 0.0168980
                            val pinkR = b0R + b1R + b2R + b3R + b4R + b5R + b6R + whiteR * 0.5362
                            b6R = whiteR * 0.115926

                            val drop = if (random.nextDouble() < 0.0008) (random.nextDouble() * 0.25) else 0.0
                            leftSample = (pinkL * 0.12) + drop
                            rightSample = (pinkR * 0.12) + drop
                        }

                        AmbientSoundType.OCEAN_WAVES -> {
                            lfoPhase += (2.0 * PI * 0.08) / SAMPLE_RATE
                            if (lfoPhase > 2.0 * PI) lfoPhase -= 2.0 * PI
                            val lfo = (0.35 + 0.65 * (0.5 * (1.0 + sin(lfoPhase))))

                            val whiteL = random.nextDouble() * 2.0 - 1.0
                            val whiteR = random.nextDouble() * 2.0 - 1.0
                            lastBrownL = (lastBrownL + (0.02 * whiteL)) / 1.02
                            lastBrownR = (lastBrownR + (0.02 * whiteR)) / 1.02

                            leftSample = lastBrownL * 1.5 * lfo
                            rightSample = lastBrownR * 1.5 * lfo
                        }

                        AmbientSoundType.BINAURAL_ALPHA -> {
                            val freqL = 200.0
                            val freqR = 210.0
                            phaseL += (2.0 * PI * freqL) / SAMPLE_RATE
                            phaseR += (2.0 * PI * freqR) / SAMPLE_RATE
                            if (phaseL > 2.0 * PI) phaseL -= 2.0 * PI
                            if (phaseR > 2.0 * PI) phaseR -= 2.0 * PI

                            val sineL = sin(phaseL) * 0.35
                            val sineR = sin(phaseR) * 0.35

                            val white = random.nextDouble() * 2.0 - 1.0
                            lastBrownL = (lastBrownL + (0.01 * white)) / 1.01

                            leftSample = sineL + (lastBrownL * 0.15)
                            rightSample = sineR + (lastBrownL * 0.15)
                        }

                        AmbientSoundType.BINAURAL_BETA -> {
                            val freqL = 230.0
                            val freqR = 248.0
                            phaseL += (2.0 * PI * freqL) / SAMPLE_RATE
                            phaseR += (2.0 * PI * freqR) / SAMPLE_RATE
                            if (phaseL > 2.0 * PI) phaseL -= 2.0 * PI
                            if (phaseR > 2.0 * PI) phaseR -= 2.0 * PI

                            val sineL = sin(phaseL) * 0.35
                            val sineR = sin(phaseR) * 0.35

                            val white = random.nextDouble() * 2.0 - 1.0
                            lastBrownL = (lastBrownL + (0.01 * white)) / 1.01

                            leftSample = sineL + (lastBrownL * 0.15)
                            rightSample = sineR + (lastBrownL * 0.15)
                        }

                        AmbientSoundType.COZY_CAMPFIRE -> {
                            val whiteL = random.nextDouble() * 2.0 - 1.0
                            val whiteR = random.nextDouble() * 2.0 - 1.0
                            lastBrownL = (lastBrownL + (0.03 * whiteL)) / 1.03
                            lastBrownR = (lastBrownR + (0.03 * whiteR)) / 1.03

                            if (random.nextDouble() < 0.00025) {
                                crackleDecay = 0.85
                            }
                            var crackleSample = 0.0
                            if (crackleDecay > 0.01) {
                                crackleSample = (random.nextDouble() * 2.0 - 1.0) * crackleDecay
                                crackleDecay *= 0.985
                            }

                            leftSample = (lastBrownL * 0.8) + (crackleSample * 0.6)
                            rightSample = (lastBrownR * 0.8) + (crackleSample * 0.6)
                        }

                        AmbientSoundType.CLOCK_TICKING -> {
                            val tickPos = (sampleCounter % SAMPLE_RATE)
                            var tickSample = 0.0
                            if (tickPos < 800) {
                                val tickPhase = (2.0 * PI * 900.0 * tickPos) / SAMPLE_RATE
                                val env = (1.0 - (tickPos.toDouble() / 800.0))
                                tickSample = sin(tickPhase) * env * 0.4
                            }
                            leftSample = tickSample
                            rightSample = tickSample
                        }

                        AmbientSoundType.NONE -> {
                            leftSample = 0.0
                            rightSample = 0.0
                        }
                    }

                    val clampedL = (leftSample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                    val clampedR = (rightSample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()

                    pcmBuffer[i * 2] = clampedL
                    pcmBuffer[i * 2 + 1] = clampedR
                }

                val written = track.write(pcmBuffer, 0, pcmBuffer.size)
                if (written < 0) break
            }
        } catch (e: Exception) {
            // Ignore interruption
        }
    }
}
