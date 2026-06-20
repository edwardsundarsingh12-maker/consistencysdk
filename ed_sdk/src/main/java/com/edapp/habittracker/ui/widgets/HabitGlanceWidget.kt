package com.edapp.habittracker.ui.widgets

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.GlanceModifier
import androidx.compose.runtime.Composable
import com.edapp.habittracker.domain.Habit
import com.edapp.habittracker.domain.HabitYear
import com.edapp.habittracker.util.HabitStatusEnum
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.min

@Composable
fun HabitWidgetListUtil(
    habits: List<Habit>,
    onHabitClick: (Habit) -> Unit
) {

    LazyColumn(

        modifier = GlanceModifier
            .fillMaxSize()
            .padding(10.dp)
    ) {

        items(habits) { habit ->

            Column(

                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .padding(12.dp)
                    .clickable {

                        onHabitClick(habit)
                    }
            ) {

                HabitStatus(

                    title = habit.title,

                    description = habit.description,

                    todayHabitStatus =
                        habit.todayHabitStatus,

                    streak = habit.streake
                )

                Spacer(
                    modifier = GlanceModifier
                        .height(10.dp)
                )


                WidgetConsistencyGrid(
                    years = habit.years,
                    activeColor = habit.color
                )

                Spacer(
                    modifier = GlanceModifier
                        .height(10.dp)
                )

                HabitStatsRow(
                    totalDays = habit.totalDays,
                    completedDays =  habit.completedDays,
                    streak = habit.streake
                )
            }
        }
    }
}

@Composable
fun HabitStatus(
    title: String,
    description: String,
    todayHabitStatus: HabitStatusEnum,
    streak: Int
) {

    Row(

        modifier = GlanceModifier
            .fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = when (todayHabitStatus) {

                HabitStatusEnum.NOT_DONE -> "⚪"

                HabitStatusEnum.PROGRESS -> "🟡"

                HabitStatusEnum.PARTIAL -> "🟠"

                HabitStatusEnum.DONE -> "🟢"

                HabitStatusEnum.STREAK -> "🔥"
            },

            style = TextStyle(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(
            modifier = GlanceModifier.width(12.dp)
        )

        Column(

            modifier = GlanceModifier
                .defaultWeight()
        ) {

            Text(

                text = title,

                style = TextStyle(
                    fontWeight = FontWeight.Bold
                ),

                maxLines = 1
            )

            Spacer(
                modifier = GlanceModifier.height(2.dp)
            )

            Text(
                text = description,
                maxLines = 1
            )
        }

        Spacer(
            modifier = GlanceModifier.width(8.dp)
        )

        Text(
            text = "🔥 $streak"
        )
    }
}

@Composable
fun HabitStatsRow(
    totalDays: Int,
    completedDays: Int,
    streak: Int,
    modifier: GlanceModifier = GlanceModifier
) {

    Row(

        modifier = modifier
            .fillMaxWidth(),

        horizontalAlignment =
            Alignment.Horizontal.CenterHorizontally,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        StatItem(
            text = "🔥 $streak",
            label = "Streak"
        )

        Spacer(
            modifier = GlanceModifier.width(20.dp)
        )

        StatItem(
            text =
                "✅ $completedDays/$totalDays",

            label = "Days"
        )
    }
}

@Composable
fun StatItem(
    text: String,
    label: String
) {

    Column(

        horizontalAlignment =
            Alignment.Horizontal.CenterHorizontally
    ) {

        Text(

            text = text,

            style = TextStyle(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(
            modifier = GlanceModifier.height(2.dp)
        )

        Text(
            text = label,
        )
    }
}

@Composable
fun WidgetConsistencyGrid(
    years: List<HabitYear>,
    activeColor: Color,
    modifier: GlanceModifier = GlanceModifier
) {
    // Get all habit logs from the latest year (most recent)
    val allLogs = years.lastOrNull()?.months?.flatMap { it.logs } ?: emptyList()

    if (allLogs.isEmpty()) {
        Text("No data available")
        return
    }

    // Sort logs by epochDay
    val sortedLogs = allLogs.sortedBy { it.epochDay }

    // Calculate weeks from logs
    val currentEpochDay = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        } else {
            sortedLogs.last().epochDay
        }
    } catch (_: Exception) {
        sortedLogs.last().epochDay
    }

    // Calculate number of weeks to display
    val firstEpochDay = sortedLogs.first().epochDay
    val daysSpan = (currentEpochDay - firstEpochDay + 1).toInt()
    val weeksToShow = (daysSpan + 6) / 7  // Round up to nearest week

    // Limit columns to only show weeks that have data + current week
    val maxWeeks = min(weeksToShow, 26) // Cap at 26 weeks for widget display

    // Create a map of epochDay to HabitLog status for quick lookup
    val logsMap = sortedLogs.associateBy { it.epochDay }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        // Days of week labels
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            val daysLabels = listOf("S", "M", "T", "W", "T", "F", "S")
            repeat(7) { dayIndex ->
                Box(
                    modifier = GlanceModifier
                        .size(12.dp)
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = daysLabels[dayIndex],
                        style = TextStyle(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        // Consistency grid - 7 rows (days of week) x N columns (weeks)
        Column(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            repeat(7) { rowIndex ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    repeat(maxWeeks) { weekIndex ->
                        val epochDayForCell = firstEpochDay + (weekIndex * 7) + rowIndex

                        // Only show if within data range or if it's today
                        if (epochDayForCell <= currentEpochDay) {
                            val log = logsMap[epochDayForCell]
                            val cellColor = when {
                                log == null -> ColorProvider(
                                    day = Color.Gray.copy(alpha = 0.1f),
                                    night = Color.Gray.copy(alpha = 0.1f)
                                )
                                log.status == HabitStatusEnum.STREAK -> ColorProvider(
                                    day = activeColor,
                                    night = activeColor
                                )
                                log.status == HabitStatusEnum.DONE -> ColorProvider(
                                    day = activeColor.copy(alpha = 0.7f),
                                    night = activeColor.copy(alpha = 0.7f)
                                )
                                log.status == HabitStatusEnum.PARTIAL -> ColorProvider(
                                    day = activeColor.copy(alpha = 0.5f),
                                    night = activeColor.copy(alpha = 0.5f)
                                )
                                log.status == HabitStatusEnum.PROGRESS -> ColorProvider(
                                    day = activeColor.copy(alpha = 0.3f),
                                    night = activeColor.copy(alpha = 0.3f)
                                )
                                else -> ColorProvider(
                                    day = Color.Gray.copy(alpha = 0.1f),
                                    night = Color.Gray.copy(alpha = 0.1f)
                                )
                            }

                            Box(
                                modifier = GlanceModifier
                                    .size(14.dp)
                                    .padding(1.dp)
                                    .cornerRadius(2.dp)
                                    .background(cellColor)
                            ) {}
                        } else {
                            // Empty space for cells beyond today
                            Box(
                                modifier = GlanceModifier
                                    .size(14.dp)
                                    .padding(1.dp)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}
