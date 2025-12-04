package com.edapp.habittracker.domain

import com.edapp.habittracker.util.DayOfWeek


data class ReminderData(
    val reminderId: Int = 0,
    val reminderTitle: String = "",
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val timeMillis: Long = 0L
)


