package com.edapp.habittracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamicColorScheme
import com.materialkolor.PaletteStyle

val seedColor: Color = Color(0xFF6750A4)

// Light scheme
val lightScheme = dynamicColorScheme(
    seedColor = seedColor,
    isDark = false,
    style = PaletteStyle.TonalSpot
)

val darkScheme = dynamicColorScheme(
    seedColor = seedColor,
    isDark = true,
    style = PaletteStyle.TonalSpot
)


val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)