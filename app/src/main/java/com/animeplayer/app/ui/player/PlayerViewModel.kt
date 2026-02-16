package com.animeplayer.app.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animeplayer.app.player.PlayerManager
import com.animeplayer.app.player.PlayerState
import com.animeplayer.app.player.SubtitleTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        startProgressLoop()
    }

    private fun startProgressLoop() {
        viewModelScope.launch {
            while (isActive) {
                if (playerState.value.isPlaying) {
                    playerManager.updateState()
                }
                delay(500) // Update UI every 500ms
            }
        }
    }

    fun handleIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.PlayPause -> {
                if (playerState.value.isPlaying) {
                    playerManager.pause()
                } else {
                    playerManager.play()
                }
            }
            is PlayerIntent.Seek -> {
                playerManager.seekTo(intent.position)
            }
            is PlayerIntent.FastForward -> {
                val newPos = (playerState.value.currentPosition + 10_000).coerceAtMost(playerState.value.duration)
                playerManager.seekTo(newPos)
            }
            is PlayerIntent.Rewind -> {
                val newPos = (playerState.value.currentPosition - 10_000).coerceAtLeast(0)
                playerManager.seekTo(newPos)
            }
            is PlayerIntent.LoadMedia -> {
                playerManager.prepare(intent.uri)
            }
            is PlayerIntent.ToggleControls -> {
                _uiState.value = _uiState.value.copy(showControls = !_uiState.value.showControls)
            }
            is PlayerIntent.SelectSubtitle -> {
                playerManager.selectSubtitleTrack(intent.track)
            }
            is PlayerIntent.ShowSubtitleDialog -> {
                _uiState.value = _uiState.value.copy(showSubtitleDialog = intent.show)
            }
            is PlayerIntent.ShowAudioSettings -> {
                _uiState.value = _uiState.value.copy(showAudioSettings = intent.show)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}

sealed class PlayerIntent {
    data object PlayPause : PlayerIntent()
    data class Seek(val position: Long) : PlayerIntent()
    data object FastForward : PlayerIntent()
    data object Rewind : PlayerIntent()
    data class LoadMedia(val uri: String) : PlayerIntent()
    data object ToggleControls : PlayerIntent()
    data class SelectSubtitle(val track: SubtitleTrack) : PlayerIntent()
    data class ShowSubtitleDialog(val show: Boolean) : PlayerIntent()
    data class ShowAudioSettings(val show: Boolean) : PlayerIntent()
}

data class PlayerUiState(
    val title: String = "",
    val showControls: Boolean = true,
    val showSubtitleDialog: Boolean = false,
    val showAudioSettings: Boolean = false
)
