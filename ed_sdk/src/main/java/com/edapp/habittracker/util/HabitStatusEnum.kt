package com.edapp.habittracker.util

enum class HabitStatusEnum(val percentage: Int) {
    NOT_DONE(0),
    PROGRESS(1), // today
    PARTIAL(25),
    DONE(50),
    STREAK(100);
    companion object {
        fun random(): HabitStatusEnum {
            return entries.random()
        }
        fun getObjByPercentage(percentage: Int): HabitStatusEnum {
            return values().firstOrNull { it.percentage == percentage }
                ?: NOT_DONE // default fallback
        }
    }
}
