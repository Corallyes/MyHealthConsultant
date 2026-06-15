package com.example.myhealthconsultant.presentation.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DRAG_THRESHOLD = 10f

@Composable
fun DraggableFloatingButton(
    onClick: () -> Unit,
    onPositionChange: (Float, Float) -> Unit,
    savedPosition: Pair<Float, Float> = Pair(Float.NaN, Float.NaN),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }
    var containerWidth by remember { mutableIntStateOf(0) }
    var containerHeight by remember { mutableIntStateOf(0) }

    val buttonTotalPx = with(density) { 88.dp.roundToPx() }

    fun clampOffset(x: Float, y: Float): Pair<Float, Float> {
        val maxX = (containerWidth - buttonTotalPx).coerceAtLeast(0).toFloat()
        val maxY = (containerHeight - buttonTotalPx).coerceAtLeast(0).toFloat()
        return Pair(x.coerceIn(0f, maxX), y.coerceIn(0f, maxY))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                containerWidth = size.width
                containerHeight = size.height
                if (initialized) {
                    val (cx, cy) = clampOffset(offsetX, offsetY)
                    if (cx != offsetX || cy != offsetY) {
                        offsetX = cx
                        offsetY = cy
                    }
                }
            }
            .offset {
                IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
            }
    ) {
        // 用 Box 包裹，手势放在外层 Box 上，FAB 不设 onClick
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(56.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var totalDragX = 0f
                        var totalDragY = 0f
                        var isDragging = false

                        drag(down.id) { change ->
                            val dx = change.positionChange().x
                            val dy = change.positionChange().y
                            totalDragX += dx
                            totalDragY += dy

                            if (!isDragging && (abs(totalDragX) > DRAG_THRESHOLD || abs(totalDragY) > DRAG_THRESHOLD)) {
                                isDragging = true
                            }

                            if (isDragging) {
                                change.consume()
                                val (nx, ny) = clampOffset(offsetX + dx, offsetY + dy)
                                offsetX = nx
                                offsetY = ny
                            }
                        }

                        if (isDragging) {
                            scope.launch {
                                onPositionChange(offsetX, offsetY)
                            }
                        } else {
                            onClick()
                        }
                    }
                }
        ) {
            FloatingActionButton(
                onClick = { /* 手势已处理 */ },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(56.dp)
            ) {
                content()
            }
        }
    }

    // 初始化位置：有保存的位置则恢复，否则设为右下角
    LaunchedEffect(savedPosition, containerWidth, containerHeight) {
        if (!initialized && containerWidth > 0 && containerHeight > 0) {
            val (sx, sy) = savedPosition
            if (!sx.isNaN() && !sy.isNaN()) {
                val (cx, cy) = clampOffset(sx, sy)
                offsetX = cx
                offsetY = cy
            } else {
                // 默认右下角
                val maxX = (containerWidth - buttonTotalPx).coerceAtLeast(0).toFloat()
                val maxY = (containerHeight - buttonTotalPx).coerceAtLeast(0).toFloat()
                offsetX = maxX
                offsetY = maxY
            }
            initialized = true
        }
    }
}
