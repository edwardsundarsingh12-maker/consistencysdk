package com.edapp.habittracker.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity

@Composable
fun rememberKeyboardVisibility(): Boolean {
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(imeInsets) {
        // Convert to pixels
        val bottomPx = with(density) { imeInsets.getBottom(density) }
        isVisible = bottomPx > 0
    }

    return isVisible
}


@Composable
fun keyboardAsState(): Boolean {
    val ime = WindowInsets.ime
    val density = LocalDensity.current

    var keyboardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(ime) {
        val bottom = with(density) { ime.getBottom(density) }
        keyboardVisible = bottom > 0
    }

    return keyboardVisible
}