package com.animeplayer.app.player.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AudioEffectManager @Inject constructor() {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    
    private var currentSessionId: Int = 0

    private val _audioState = MutableStateFlow(AudioState())
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    fun attachToSession(audioSessionId: Int) {
        if (audioSessionId == 0) return
        if (currentSessionId == audioSessionId) return
        
        release()
        currentSessionId = audioSessionId

        try {
            // Equalizer
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _audioState.value.isEqEnabled
                val bands = mutableListOf<EqBand>()
                val numBands = numberOfBands
                val minLevel = bandLevelRange[0]
                val maxLevel = bandLevelRange[1]
                
                for (i in 0 until numBands) {
                    val centerFreq = getCenterFreq(i.toShort())
                    bands.add(EqBand(i.toShort(), centerFreq, getBandLevel(i.toShort()), minLevel, maxLevel))
                }
                _audioState.update { it.copy(eqBands = bands) }
            }

            // Bass Boost
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _audioState.value.isBassEnabled && strengthSupported
                if (strengthSupported) {
                    setStrength(_audioState.value.bassStrength)
                }
            }

            // Virtualizer
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _audioState.value.isVirtualizerEnabled && strengthSupported
                if (strengthSupported) {
                    setStrength(_audioState.value.virtualizerStrength)
                }
            }

            // Loudness Enhancer
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = _audioState.value.isLoudnessEnabled
                setTargetGain(_audioState.value.loudnessGain)
            }
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Failed to attach audio effects", e)
        }
    }

    fun setEqBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
        // Update local state
        _audioState.update { state ->
            val updatedBands = state.eqBands.map {
                if (it.index == band) it.copy(level = level) else it
            }
            state.copy(eqBands = updatedBands)
        }
    }

    fun setBassStrength(strength: Short) {
        bassBoost?.takeIf { it.strengthSupported }?.setStrength(strength)
        _audioState.update { it.copy(bassStrength = strength) }
    }

    fun setVirtualizerStrength(strength: Short) {
        virtualizer?.takeIf { it.strengthSupported }?.setStrength(strength)
        _audioState.update { it.copy(virtualizerStrength = strength) }
    }

    fun setLoudnessGain(gainmB: Int) {
        loudnessEnhancer?.setTargetGain(gainmB)
        _audioState.update { it.copy(loudnessGain = gainmB) }
    }

    fun toggleEq(enable: Boolean) {
        equalizer?.enabled = enable
        _audioState.update { it.copy(isEqEnabled = enable) }
    }

    fun toggleBass(enable: Boolean) {
        bassBoost?.enabled = enable
        _audioState.update { it.copy(isBassEnabled = enable) }
    }

    fun toggleVirtualizer(enable: Boolean) {
        virtualizer?.enabled = enable
        _audioState.update { it.copy(isVirtualizerEnabled = enable) }
    }
    
    fun toggleLoudness(enable: Boolean) {
        loudnessEnhancer?.enabled = enable
        _audioState.update { it.copy(isLoudnessEnabled = enable) }
    }

    fun applyPreset(preset: AudioPreset) {
        // Apply EQ levels
        _audioState.value.eqBands.forEach { band ->
            // Simple mapping: Find closest target freq in preset
            val targetGain = findClosestGain(band.centerFreq, preset.gains)
            setEqBandLevel(band.index, targetGain)
        }
        
        // Apply effects
        setBassStrength(preset.bassStrength)
        setVirtualizerStrength(preset.virtualizerStrength)
        setLoudnessGain(preset.loudnessGain)
        
        toggleBass(preset.bassStrength > 0)
        toggleVirtualizer(preset.virtualizerStrength > 0)
        toggleLoudness(preset.loudnessGain > 0)
    }

    private fun findClosestGain(centerFreqmHz: Int, gains: Map<Int, Short>): Short {
        // gains keys are in Hz, centerFreq is in mHz (milliHertz)
        val centerFreqHz = centerFreqmHz / 1000
        val closestKey = gains.keys.minByOrNull { abs(it - centerFreqHz) } ?: return 0
        return gains[closestKey] ?: 0
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        loudnessEnhancer?.release()
        
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }
}

data class AudioState(
    val isEqEnabled: Boolean = true,
    val isBassEnabled: Boolean = false,
    val isVirtualizerEnabled: Boolean = false,
    val isLoudnessEnabled: Boolean = false,
    val eqBands: List<EqBand> = emptyList(),
    val bassStrength: Short = 0, // 0-1000
    val virtualizerStrength: Short = 0, // 0-1000
    val loudnessGain: Int = 0 // mB
)

data class EqBand(
    val index: Short,
    val centerFreq: Int, // mHz
    val level: Short,
    val minLevel: Short,
    val maxLevel: Short
)

data class AudioPreset(
    val name: String,
    val gains: Map<Int, Short>, // Freq (Hz) -> Gain (mB)
    val bassStrength: Short = 0,
    val virtualizerStrength: Short = 0,
    val loudnessGain: Int = 0
)

object AudioPresets {
    val MOVIE = AudioPreset(
        "Movie",
        mapOf(31 to 400, 62 to 400, 125 to 200, 250 to 200, 500 to 0, 1000 to 0, 2000 to 200, 4000 to 300, 8000 to 300, 16000 to 200),
        bassStrength = 400,
        virtualizerStrength = 600,
        loudnessGain = 200
    )
    val ANIME = AudioPreset(
        "Anime",
        mapOf(31 to 200, 62 to 200, 125 to 0, 250 to 400, 500 to 400, 1000 to 400, 2000 to 200, 4000 to 300, 8000 to 300, 16000 to 200),
        bassStrength = 200,
        virtualizerStrength = 300,
        loudnessGain = 100
    )
    val FLAT = AudioPreset(
        "Flat",
        mapOf(31 to 0, 62 to 0, 125 to 0, 250 to 0, 500 to 0, 1000 to 0, 2000 to 0, 4000 to 0, 8000 to 0, 16000 to 0),
        bassStrength = 0,
        virtualizerStrength = 0,
        loudnessGain = 0
    )
}
