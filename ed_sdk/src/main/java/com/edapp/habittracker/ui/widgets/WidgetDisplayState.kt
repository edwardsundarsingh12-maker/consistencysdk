package com.edapp.habittracker.ui.widgets

import com.edapp.habittracker.domain.Habit

/**
 * Data class to hold widget display state
 */
data class WidgetDisplayState(
    val habit: Habit? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
