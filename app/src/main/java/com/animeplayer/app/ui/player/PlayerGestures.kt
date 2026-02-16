package com.animeplayer.app.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun PlayerGestures(
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onDoubleTap: (Offset) -> Unit,
    onHorizontalDrag: (Float) -> Unit,
    onVerticalDrag: (Float, Boolean) -> Unit // delta, isLeft
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { offset -> onDoubleTap(offset) }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    onHorizontalDrag(dragAmount)
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    val isLeft = change.position.x < size.width / 2
                    onVerticalDrag(dragAmount, isLeft)
                }
            }
    )
}
