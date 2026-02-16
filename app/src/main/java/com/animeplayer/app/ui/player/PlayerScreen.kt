package com.animeplayer.app.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.animeplayer.app.player.PlayerManager

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    uri: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
    playerManager: PlayerManager // Injected or passed from VM
) {
    val playerState by viewModel.playerState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Keep screen on
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Load Media
    LaunchedEffect(uri) {
        viewModel.handleIntent(PlayerIntent.LoadMedia(uri))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.getPlayer()
                    useController = false // We use our own Compose UI
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = playerManager.getPlayer()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture Overlay
        PlayerGestures(
            onTap = { viewModel.handleIntent(PlayerIntent.ToggleControls) },
            onDoubleTap = { offset ->
                // Basic implementation: Left half rewind, Right half fast forward
                // In a real app, calculate screen width percentage
                viewModel.handleIntent(PlayerIntent.PlayPause) // Placeholder for center double tap
            },
            onHorizontalDrag = { delta ->
                // Implement precise seek based on delta
            },
            onVerticalDrag = { delta, isLeft ->
                // Implement volume/brightness
            }
        )

        // Controls Overlay
        PlayerControls(
            isVisible = uiState.showControls,
            playerState = playerState,
            title = uiState.title,
            onIntent = viewModel::handleIntent,
            onBack = onBack
        )

        // Subtitle Dialog
        if (uiState.showSubtitleDialog) {
            SubtitleSelectionDialog(
                playerState = playerState,
                onDismiss = { viewModel.handleIntent(PlayerIntent.ShowSubtitleDialog(false)) },
                onSelect = { track ->
                    viewModel.handleIntent(PlayerIntent.SelectSubtitle(track))
                    viewModel.handleIntent(PlayerIntent.ShowSubtitleDialog(false))
                }
            )
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
