package com.edapp.habittracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedToggleIcon(
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    initialState: Boolean = false,
    onToggle: (Boolean) -> Unit
) {
    var isEnabled by remember { mutableStateOf(initialState) }

    // Animate background color
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) activeColor else Color(0xFFB0BEC5),
        animationSpec = tween(durationMillis = 400)
    )

    // Spring animation for knob position
    val knobOffset by animateDpAsState(
        targetValue = if (isEnabled) 26.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Knob scale pulse when toggled
    val knobScale by animateFloatAsState(
        targetValue = if (isEnabled) 1.2f else 1f,
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = modifier
            .width(60.dp)
            .height(34.dp)
            .shadow(6.dp, RoundedCornerShape(50))
            .background(backgroundColor, RoundedCornerShape(50))
            .clickable {
                isEnabled = !isEnabled
                onToggle(isEnabled)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(28.dp)
                .scale(knobScale)
                .background(Color.White, CircleShape)
                .shadow(4.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isEnabled) activeColor else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
