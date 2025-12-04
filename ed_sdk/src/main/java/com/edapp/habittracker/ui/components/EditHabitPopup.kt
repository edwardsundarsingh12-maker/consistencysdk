package com.edapp.habittracker.ui.components

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edapp.habittracker.domain.Habit
import com.edapp.habittracker.domain.HabitLog
import com.edapp.habittracker.domain.HabitMonth
import com.edapp.habittracker.ui.HabitStatus
import com.edapp.habittracker.ui.HabitViewModel
import com.edapp.habittracker.ui.consitency.ConsistencysRow
import com.edapp.habittracker.util.HabitStatusEnum
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// ------------------------------------------------------------
// MAIN ENTRY CALL: CALL THIS TO SHOW BOTTOM SHEET
// ------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitCalendarBottomSheet(
    habit: Habit,
    viewModel: HabitViewModel,
    onDismiss: () -> Unit,
    onDayClick: (LocalDate, HabitStatusEnum?, Long?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState
    ) {
//        Column(
//            modifier = Modifier
//                .padding(8.dp)
//                .heightIn(max = 260.dp)
//                .clip(RoundedCornerShape(16.dp))
//                .padding(8.dp)
//        ) {
//            val progressValue = remember {
//                mutableFloatStateOf(50f)
//            }
//            HabitStatus(
//                title = habit.title,
//                description = habit.description,
//                isRowView = true,
//                habitIcon = habit.icon,
//                activeColor = habit.color,
//                todayHabitStatus = habit.todayHabitStatus
//            ) {
//
//            }
//            ConsistencysRow(habit, viewModel)
//        }
        CalendarPagerContent(
            habit = habit,
            onDayClick = onDayClick
        )
    }
}

// ------------------------------------------------------------
// PAGER CONTENT
// ------------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarPagerContent(
    habit: Habit,
    onDayClick: (LocalDate, HabitStatusEnum?, Long?) -> Unit
) {
    val today = LocalDate.now()
    val startMonth = YearMonth.from(today)

    // Create large range: 240 months (20 years)
    val totalPages = 240
    val startPage = totalPages / 2

    val pagerState = rememberPagerState(initialPage = startPage) { totalPages }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
    ) {
        // Top Month with Arrows
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous Month"
                )
            }

            val currentMonth = remember(pagerState.currentPage) {
                startMonth.plusMonths((pagerState.currentPage - startPage).toLong())
            }

            Text(
                text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        + " " + currentMonth.year.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next Month"
                )
            }
        }

        // Weekday Row
        // Weekday Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->

            val month = startMonth.plusMonths((page - startPage).toLong())
            CalendarMonthGrid(
                habitOwnerId = habit.getHabitOwnerId(),
                month = month,
                today = today,
                habitMonth = habit.getHabitMonth(month),
                onDayClick = onDayClick
            )
        }
    }
}

// ------------------------------------------------------------
// MONTH GRID
// ------------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarMonthGrid(
    habitOwnerId: Long,
    month: YearMonth,
    today: LocalDate,
    habitMonth:  HabitMonth?,
    onDayClick: (LocalDate, HabitStatusEnum?, Long?) -> Unit
) {
    val firstDay = month.atDay(1)
    val firstDayOfWeek = firstDay.dayOfWeek.value % 7
    val totalDays = month.lengthOfMonth()
    val habitLogList = habitMonth?.logs

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        var dayNumber = 1
        var cellIndex = 0

        for (week in 0 until 6) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                for (dayOfWeek in 0..6) {
                    val dayDate = if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        null
                    } else if (dayNumber > totalDays) {
                        null
                    } else {
                        month.atDay(dayNumber++)
                    }


                    val habitLog = habitLogList
                        ?.getOrNull(cellIndex)
                        ?.takeIf {
                            if(it.epochDay == dayDate?.toEpochDay() ){
                                cellIndex++
                            }
                            it.epochDay == dayDate?.toEpochDay()
                        }

                    DayCell(
                        date = dayDate,
                        today = today,
                        habitOwnerId = habitOwnerId,
                        habitLog = habitLog,
                        onDayClick = onDayClick
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------
// DAY CELL
// ------------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayCell(
    date: LocalDate?,
    today: LocalDate,
    habitOwnerId: Long,
    habitLog: HabitLog?,
    onDayClick: (LocalDate, HabitStatusEnum?, Long?) -> Unit
) {
    val isFuture = date != null && date.isAfter(today)
    val progress = if (date != null) habitLog?.status?.percentage ?: 0 else 0

    val bgColor by animateColorAsState(
        targetValue = if (date != null && !isFuture && progress > 0)
            Color(0xFF4CAF50).copy(alpha = progress / 100f)
        else Color.Transparent
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bgColor, shape = CircleShape)
            .clickable(enabled = date != null && !isFuture) {
                val nextHabit = when(habitLog?.status) {
                    HabitStatusEnum.NOT_DONE -> HabitStatusEnum.PARTIAL
                    HabitStatusEnum.PROGRESS -> HabitStatusEnum.PARTIAL
                    HabitStatusEnum.PARTIAL -> HabitStatusEnum.DONE
                    HabitStatusEnum.DONE -> HabitStatusEnum.STREAK
                    HabitStatusEnum.STREAK -> HabitStatusEnum.NOT_DONE
                    null -> HabitStatusEnum.PARTIAL
                }
                if (date != null) onDayClick(date, nextHabit, habitOwnerId)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date?.dayOfMonth?.toString() ?: "",
            color = if (isFuture) Color.Gray.copy(alpha = 0.4f) else Color.Unspecified,
            fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal
        )
    }
}
