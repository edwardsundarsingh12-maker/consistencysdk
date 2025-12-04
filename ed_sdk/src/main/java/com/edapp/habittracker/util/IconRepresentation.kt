package com.edapp.habittracker.util

import androidx.compose.ui.graphics.vector.ImageVector

sealed class IconRepresentation {
    data class Emoji(val value: String) : IconRepresentation()
    data class Vector(val icon: ImageVector) : IconRepresentation()
}
