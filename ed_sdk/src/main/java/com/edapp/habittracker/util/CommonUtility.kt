package com.edapp.habittracker.util

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit


object CommonUtil {

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun haptic() {
        val context = SDK.getAppContext()
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // crescendo pattern: short → medium → strong
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 40, 80, 120, 180),   // delay, buzz, pause, buzz, pause
                intArrayOf(0, 100, 0, 180, 0),      // amplitude rises each time
                -1
            )
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 40, 80, 120), -1)
        }
    }

    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    @Composable
    fun dpToSp(dp: Dp): TextUnit {
        val density = LocalDensity.current
        return with(density) { dp.toSp() }
    }
}


fun Context.isDarkTheme(): Boolean {
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO,
        Configuration.UI_MODE_NIGHT_UNDEFINED -> false
        else -> false
    }
}


// Darken by a factor (0.0f = black, 1.0f = original color)
fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f),
        alpha = alpha
    )

}

fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}


fun Color.lightenSlightly(amount: Float = 0.05f): Color {
    return Color(
        red = (red + amount).coerceIn(0f, 1f),
        green = (green + amount).coerceIn(0f, 1f),
        blue = (blue + amount).coerceIn(0f, 1f),
        alpha = alpha
    )
}
