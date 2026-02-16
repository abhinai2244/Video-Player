package com.animeplayer.app.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    
    // Track selection
    private val trackSelector: DefaultTrackSelector by lazy { 
        DefaultTrackSelector(context) 
    }

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updateState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState()
        }

        override fun onPlayerError(error: PlaybackException) {
            _playerState.update { it.copy(error = error.localizedMessage ?: "Unknown Error") }
        }

        override fun onTracksChanged(tracks: Tracks) {
            updateSubtitleTracks(tracks)
        }
    }

    init {
        initializePlayer()
    }

    @OptIn(UnstableApi::class) 
    private fun initializePlayer() {
        if (exoPlayer == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build()

            exoPlayer = ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .setAudioAttributes(audioAttributes, true)
                .build()
                .apply {
                    addListener(playerListener)
                    playWhenReady = true
                }
        }
    }

    fun prepare(uri: String) {
        initializePlayer()
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        updateState() // Immediate feedback
    }

    fun release() {
        exoPlayer?.apply {
            removeListener(playerListener)
            release()
        }
        exoPlayer = null
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    // Helper to update state snapshots
    fun updateState() {
        exoPlayer?.let { player ->
            _playerState.update {
                it.copy(
                    isPlaying = player.isPlaying,
                    isLoading = player.playbackState == Player.STATE_BUFFERING,
                    currentPosition = player.currentPosition,
                    duration = player.duration.coerceAtLeast(0L),
                    bufferedPercentage = player.bufferedPercentage
                )
            }
        }
    }

    private fun updateSubtitleTracks(tracks: Tracks) {
        val subtitleTracks = mutableListOf<SubtitleTrack>()
        
        // Add "Off" option
        subtitleTracks.add(SubtitleTrack(label = "Off", trackGroup = null, trackIndex = 0, isSelected = false))

        tracks.groups.forEach { group ->
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    val isSelected = group.isSelected
                    subtitleTracks.add(
                        SubtitleTrack(
                            label = format.label ?: format.language ?: "Unknown",
                            trackGroup = group.mediaTrackGroup,
                            trackIndex = i,
                            isSelected = isSelected
                        )
                    )
                }
            }
        }

        _playerState.update { it.copy(subtitleTracks = subtitleTracks) }
    }

    fun selectSubtitleTrack(track: SubtitleTrack) {
        trackSelector.parameters = trackSelector.buildUponParameters().apply {
            if (track.trackGroup == null) {
                // Clear selection (Turn off subtitles)
                setTrackSelectionOverrides(
                    TrackSelectionOverride.clearOverridesOfType(C.TRACK_TYPE_TEXT)
                )
                setRendererDisabled(C.TRACK_TYPE_TEXT, true)
            } else {
                // Select specific track
                setRendererDisabled(C.TRACK_TYPE_TEXT, false)
                setOverrideForType(
                    TrackSelectionOverride(track.trackGroup, track.trackIndex)
                )
            }
        }.build()
    }
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPercentage: Int = 0,
    val error: String? = null,
    val subtitleTracks: List<SubtitleTrack> = emptyList()
)

data class SubtitleTrack(
    val label: String,
    val trackGroup: androidx.media3.common.TrackGroup? = null, // Null for "Off"
    val trackIndex: Int = 0,
    val isSelected: Boolean = false
)
