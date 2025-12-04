package com.edapp.habittracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.edapp.habittracker.util.CommonUtil
import com.edapp.habittracker.util.isDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarWithAnimation(
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit
) {

    val context = LocalContext.current
    TopAppBar(

        title = {
            Text(
                text = "My Toolbar",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            RotatingSettingsIcon(onClick = onSettingsClick)
        },
        actions = {
            RotatingAddIcon(Icons.Default.Add,onClick = onAddClick)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (context.isDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun RotatingSettingsIcon(onClick: () -> Unit) {
    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 360f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "rotateAnim"
    )

    IconButton(
        onClick = {
            rotated = !rotated
            onClick()
        }
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            modifier = Modifier.rotate(rotation)
        )
    }
}


@Composable
fun RotatingAddIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.primary,
    buttonSize: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    onClick: () -> Unit
) {
    var clicked by remember { mutableStateOf(false) }
    var rotationTarget by remember { mutableStateOf(0f) }
    var shakeDirection by remember { mutableStateOf(0f) }

    // Animate rotation
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
    )

    // Animate tint color
    val currentTint by animateColorAsState(
        targetValue = if (isError && clicked) Color.Red else tint,
        animationSpec = tween(200)
    )

    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            clicked = true
            rotationTarget += 360f
            if (isError) {
                CommonUtil.haptic()
            } else {
                onClick()
            }
            scope.launch {
                delay(500L)
                clicked = false
            }
        },
        modifier = modifier.size(buttonSize)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Add",
            tint = currentTint,
            modifier = Modifier
                .size(iconSize)
                .rotate(rotation)
        )
    }
}