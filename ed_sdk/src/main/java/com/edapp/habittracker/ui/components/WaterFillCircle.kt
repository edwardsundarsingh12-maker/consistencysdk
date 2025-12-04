package com.edapp.habittracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.edapp.habittracker.util.CommonUtil
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.sin
@Composable
fun WaterFillCircle(
    percentage: Float, // 0..100
    sizeDp: Int = 64,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = Color.Gray.copy(alpha = 0.2f),
    waveAmplitude: Float = 3f,
    waveFrequency: Float = 2f,
    waveSpeed: Float = 1000f,
    onClick: () -> Unit
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 800)
    )

    // Only run wave animation if percentage is between 0 and 100
    val wavePhase by if (animatedPercentage in 1f..99f) {
        val infiniteTransition = rememberInfiniteTransition()
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = waveSpeed.toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        remember { mutableStateOf(0f) } // static wave phase
    }

    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()



    Box(
        modifier = Modifier
            .scale(scale.value)
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable{
                CommonUtil.haptic()
                onClick()
                coroutineScope.launch {
                    scale.animateTo(
                        1.3f,
                        animationSpec = tween(durationMillis = 350)
                    )
                    scale.animateTo(
                        1f,
                        animationSpec = tween(durationMillis = 350)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val newWaveLength = if (wavePhase == 0f || wavePhase == 100f) 0f else waveAmplitude

            val minY = newWaveLength
            val maxY = height - newWaveLength
            val waterLevel = maxY - (animatedPercentage / 100f) * (maxY - minY)

            val path = Path()
            path.moveTo(0f, height)

            var x = 0f
            while (x <= width) {
                val y = waterLevel + newWaveLength * sin((x / width) * waveFrequency * 2 * Math.PI + wavePhase)
                path.lineTo(x, y.toFloat())
                x += 1f
            }

            path.lineTo(width, height)
            path.close()

            drawPath(path = path, color = activeColor)
        }

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((sizeDp / 2).dp)
        )
    }
}
