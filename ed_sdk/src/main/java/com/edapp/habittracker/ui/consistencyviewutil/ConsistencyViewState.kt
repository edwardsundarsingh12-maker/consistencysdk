package com.edapp.habittracker.ui.consistencyviewutil

import com.edapp.habittracker.domain.Habit
import com.edapp.habittracker.domain.HabitTag

/**
 * Data class holding all the information needed for displaying habits in ConsistencyView.
 * This replaces passing the entire ViewModel and ensures consistency across different screens.
 */
data class ConsistencyViewState(
    val habits: List<Habit>,
    val isRowView: Boolean,
    val allTags: List<HabitTag>,
    val selectedTags: List<Long>
)
