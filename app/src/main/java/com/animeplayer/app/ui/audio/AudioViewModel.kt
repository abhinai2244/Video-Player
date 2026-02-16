package com.animeplayer.app.ui.audio

import androidx.lifecycle.ViewModel
import com.animeplayer.app.player.audio.AudioEffectManager
import com.animeplayer.app.player.audio.AudioPreset
import com.animeplayer.app.player.audio.AudioState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioEffectManager: AudioEffectManager
) : ViewModel() {

    val audioState: StateFlow<AudioState> = audioEffectManager.audioState

    fun toggleEq(enable: Boolean) {
        audioEffectManager.toggleEq(enable)
    }

    fun setEqBandLevel(band: Short, level: Short) {
        audioEffectManager.setEqBandLevel(band, level)
    }

    fun toggleBass(enable: Boolean) {
        audioEffectManager.toggleBass(enable)
    }

    fun setBassStrength(strength: Short) {
        audioEffectManager.setBassStrength(strength)
    }

    fun toggleVirtualizer(enable: Boolean) {
        audioEffectManager.toggleVirtualizer(enable)
    }

    fun setVirtualizerStrength(strength: Short) {
        audioEffectManager.setVirtualizerStrength(strength)
    }

    fun toggleLoudness(enable: Boolean) {
        audioEffectManager.toggleLoudness(enable)
    }

    fun setLoudnessGain(gainmB: Int) {
        audioEffectManager.setLoudnessGain(gainmB)
    }

    fun applyPreset(preset: AudioPreset) {
        audioEffectManager.applyPreset(preset)
    }
}
