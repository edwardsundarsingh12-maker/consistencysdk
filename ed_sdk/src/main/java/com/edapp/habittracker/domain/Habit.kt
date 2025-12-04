package com.edapp.habittracker.domain

import android.os.Build
import androidx.compose.ui.graphics.Color
import com.edapp.habittracker.util.HabitStatusEnum
import com.edapp.habittracker.util.IconRepresentation
import java.time.YearMonth

data class Habit(
    val id: Long,
    val title: String,
    val description: String,
    val icon: IconRepresentation,          // Actual Compose icon
    val consistencyIcon: IconRepresentation,
    val color: Color,
    val uncheckedColorValue: Color,
    val years: List<HabitYear>,      // Grouped logs
    val todayHabitStatus: HabitStatusEnum
) {

    fun getHabitMonth(yearMonth: YearMonth) : HabitMonth? {
        val year = years.find { Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && yearMonth.year == it.year }
        val month = year?.months?.find { Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && it.month == yearMonth.month.value }
        return month
    }

    fun getHabitOwnerId() : Long = years.first().months.first().logs.first().habitOwnerId
}

data class HabitYear(
    val year: Int,
    val months: List<HabitMonth>
)

data class HabitMonth(
    val month: Int,                 // 1 = January ... 12 = December
    val logs: List<HabitLog>
)

data class HabitLog(
    val habitOwnerId: Long,
    val epochDay: Long,
    val status: HabitStatusEnum                  // 0 = NotDone, 25 = Partial, 50 = Done, 100 = Streak
)


enum class Category(val type: Int) { GOOD(0), BAD(1), NEUTRAL(2) }

data class HabitTag(
    val tagId: Long = 0,
    val title: String,
    val icon: String,
    val colorValue: Long
)
