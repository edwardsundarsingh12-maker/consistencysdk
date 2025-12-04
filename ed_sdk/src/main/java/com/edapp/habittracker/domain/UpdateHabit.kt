package com.edapp.habittracker.domain

import androidx.compose.ui.graphics.Color

data class UpdateHabit(
    val title: String = "",
    val description: String = "",
    val selectedHabitConsistencyIcon: String = "Star",
    val selectedHabitIcon: String = "Work`" ,
    val reminderList: List<ReminderData>? = null,
    val oldHabitDbPrimaryKey: Long = 0,
    val isNewHabit: Boolean = true,
    val color: Color = Color.Blue,
    val uncheckedColorValue: Color = color.copy(alpha = 0.05f),
    val tagIds: Set<Long> = emptySet<Long>()
)