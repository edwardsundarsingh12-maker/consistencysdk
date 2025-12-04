package com.edapp.habittracker.ui.consitency

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.edapp.habittracker.di.IconMapper
import com.edapp.habittracker.domain.Habit
import com.edapp.habittracker.domain.HabitMonth
import com.edapp.habittracker.ui.HabitViewModel
import com.edapp.habittracker.util.HabitStatusEnum
import com.edapp.habittracker.util.IconRepresentation
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.collections.forEach


@Composable
fun ConsistencysRow(
    habit: Habit,
    viewModel: HabitViewModel
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        val listState = rememberLazyListState()

        val totalMonths = habit.years.sumOf { it.months.size }

        LaunchedEffect(totalMonths) {
            if (totalMonths > 0) {
                listState.scrollToItem(totalMonths - 1)
            }
        }

        LazyRow(
            state = listState
        ) {
            habit.years.forEach { year ->
                items(year.months.size) { index ->
                    MonthConsistencyCompose(
                        habitMonth = year.months[index],
                        year = year.year,
                        viewModel = viewModel,
                        showYear = false,
                        cellIcon = habit.consistencyIcon,
                        activeColor = habit.color,
                        uncheckedColorValue = habit.uncheckedColorValue
                    )
                }
            }
        }

    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthConsistencyCompose(
    habitMonth: HabitMonth,
    year: Int,
    cellIcon: IconRepresentation,
    cellSize: Int = 24,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColorValue: Color = activeColor.copy(alpha = 0.05f),
    inactiveAlpha: Float = 0.2f,
    showYear: Boolean,
    viewModel: HabitViewModel
) {
    val month = habitMonth.month
    val yearMonth = YearMonth.of(year, month)
    val totalDays = yearMonth.lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val lastDayOfMonth = LocalDate.of(year, month, totalDays)
    val firstWeekdayIndex = (firstDayOfMonth.dayOfWeek.value % 7) // 0=Sun

    val logs = habitMonth.logs

    // Prepare list of cells
    val cells = mutableListOf<HabitStatusEnum?>()
    repeat(firstWeekdayIndex) { cells.add(null) }

    var logsCurrentIndex = 0

    (firstDayOfMonth.toEpochDay() .. lastDayOfMonth.toEpochDay()).forEach { currEpochDay ->
        if (logsCurrentIndex < logs.size && logs[logsCurrentIndex].epochDay == currEpochDay ) {
            cells.add(logs[logsCurrentIndex].status)
            logsCurrentIndex = logsCurrentIndex+1
        }
        else {
            cells.add(HabitStatusEnum.NOT_DONE)
        }
    }

//    repeat(totalDays) { cells.add(true) }
    val totalWeeks = (cells.size + 6) / 7

    // 🔹 Trigger state
    var trigger by remember { mutableStateOf(false) }

//    LaunchedEffect(true) {
//        while (true) {
//            delay(5000)
//            trigger = !trigger
//        }
//    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 8.dp), // toggle ripple animation
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            repeat(totalWeeks) { weekIndex ->
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    repeat(7) { dayIndex ->
                        val cellIndex = weekIndex * 7 + dayIndex
                        if (cellIndex < cells.size) {
                            // 🔹 Each cell has its own animatable
                            IconStrike(trigger, activeColor, uncheckedColorValue, cellIndex, cells[cellIndex], cellIcon, cellSize )
                        } else {
                            Spacer(modifier = Modifier.size(cellSize.dp))
                        }
                    }
                }
            }
        }
        Text(
            text = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + if (showYear) " $year" else "",
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PreviewConsistencyGraph(
    cellIcon: IconRepresentation = IconMapper.getIconByName("Star"),
    cellSize: Int = 16,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColorValue : Color = activeColor.copy(alpha = 0.05f)
) {
    val today = LocalDate.now()
    val month = today.month.value
    val yearMonth = YearMonth.of(today.year, month)
    val totalDays = yearMonth.lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(today.year, month, 1)
    val lastDayOfMonth = LocalDate.of(today.year, month, totalDays)
    val firstWeekdayIndex = (firstDayOfMonth.dayOfWeek.value % 7) // 0=Sun

    // Prepare list of cells
    val cells = mutableListOf<HabitStatusEnum?>()
    repeat(firstWeekdayIndex) { cells.add(null) }

    var logsCurrentIndex = 0

    (firstDayOfMonth.toEpochDay() .. lastDayOfMonth.toEpochDay()).forEach { currEpochDay ->
        try{ cells.add(HabitStatusEnum.values()[getDayOfMonthFromEpochDay(currEpochDay) % 5]) }
        catch (e: Exception) {
            HabitStatusEnum.random()
        }
    }

    val totalWeeks = (cells.size + 6) / 7


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 8.dp), // toggle ripple animation
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            repeat(totalWeeks) { weekIndex ->
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    repeat(7) { dayIndex ->
                        val cellIndex = weekIndex * 7 + dayIndex
                        if (cellIndex < cells.size) {
                            // 🔹 Each cell has its own animatable
                            IconStrike(false, activeColor, uncheckedColorValue, cellIndex, cells[cellIndex], cellIcon, cellSize )
                        } else {
                            Spacer(modifier = Modifier.size(cellSize.dp))
                        }
                    }
                }
            }
        }
        Text(
            text = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " ${today.year}" ,
            fontWeight = FontWeight.Medium,
            color = activeColor,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

fun getDayOfMonthFromEpochDay(epochDay: Long): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDate.ofEpochDay(epochDay).dayOfMonth
    } else {
        return 2
    }
}

@Composable
fun SampleConsistencyIcons(
    cellIcon: IconRepresentation = IconMapper.getIconByName("Star"),
    cellSize: Int = 16,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColorValue: Color = activeColor.copy(alpha = 0.05f),
) {

    Column {

        Row {
            IconStrike(false, activeColor,  uncheckedColorValue, 1, HabitStatusEnum.PROGRESS, cellIcon, cellSize)
            Text("Today ")
        }

        Row {
            IconStrike(false, activeColor,  uncheckedColorValue, 1, HabitStatusEnum.NOT_DONE, cellIcon, cellSize)
            Text("Not done (0%)")
        }

        Row {
            IconStrike(false, activeColor,  uncheckedColorValue, 1, HabitStatusEnum.PARTIAL, cellIcon, cellSize)
            Text("Partial (25%)")
        }

        Row {
            IconStrike(false, activeColor,  uncheckedColorValue, 1, HabitStatusEnum.DONE, cellIcon, cellSize)
            Text("Done (50%)")
        }

        Row {
            IconStrike(false, activeColor,  uncheckedColorValue, 1, HabitStatusEnum.STREAK, cellIcon, cellSize)
            Text("Streak (100%)")
        }

    }

}

@Composable
fun IconStrike(
    trigger: Boolean,
    activeColor: Color,
    uncheckedColorValue: Color,
    cellIndex: Int,
    cell: HabitStatusEnum?,
    cellIcon: IconRepresentation,
    cellSize: Int
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            // delay based on position → creates ripple
            delay(cellIndex * 30L)
            scale.animateTo(
                1.3f,
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                1f,
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            )
        }
    }

    var cellColorAlpha: Float = 1f
    when (cell) {
        HabitStatusEnum.NOT_DONE -> {
            cellColorAlpha = 0.05f
        }
        HabitStatusEnum.PARTIAL -> {
            cellColorAlpha = 0.20f
        }
        HabitStatusEnum.DONE -> {
            cellColorAlpha= 0.60f
        }
        HabitStatusEnum.STREAK -> {
            cellColorAlpha = 1f
        }
        null -> {
            cellColorAlpha = 0f
        }

        HabitStatusEnum.PROGRESS -> {
            cellColorAlpha =  0.05f
        }
    }
    val cellColor =
        if (cell == HabitStatusEnum.NOT_DONE)  uncheckedColorValue else activeColor.copy(alpha = cellColorAlpha)
//    listOf(0.05f, 0.3f, 0.65f, 1f).random().let { if (cellColor != Color.Transparent) { cellColor = activeColor.copy(it) } }

    // Glow effect only for STREAK
    Box(
        modifier = Modifier
            .size(cellSize.dp)
            .scale(scale.value),
        contentAlignment = Alignment.Center
    ) {
//        if ( cellColor.alpha == 1f) {
//            Box(
//                modifier = Modifier
//                    .matchParentSize()
//                    .graphicsLayer {
//                        shadowElevation = 20f
//                        shape = CircleShape
//                        clip = false
//                    }
//                    .background(
//                        activeColor.copy(alpha = 0.1f),
//                        shape = CircleShape
//                    )
//                    .blur(radius = 16.dp)
//            )
//        }
        if (cellIcon is IconRepresentation.Vector){
            if (cell == HabitStatusEnum.PROGRESS) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = cellIcon.icon,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size((cellSize + 12).dp)
                    )
                    Icon(
                        imageVector = cellIcon.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .size((cellSize - 5).dp)
                            .align(Alignment.Center)
                    )
                }


            } else {
                Icon(
                    imageVector = cellIcon.icon,
                    contentDescription = null,
                    tint = cellColor,
                    modifier = Modifier
                        .size(cellSize.dp)
                        .scale(scale.value)
                )
            }
        } else {
            val fontSize = with(LocalDensity.current) { 15.dp.toSp() }

            if (cell == HabitStatusEnum.PROGRESS) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (cellIcon as IconRepresentation.Emoji).value,
                        fontSize = fontSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .alpha(cellColorAlpha)
                            .size(cellSize.dp)
                            .scale(scale.value)
                    )
                }

            } else {
                Text(
                    text = (cellIcon as IconRepresentation.Emoji).value,
                    textAlign = TextAlign.Center,

                    fontSize = fontSize,
                    modifier = Modifier
                        .size(cellSize.dp)
                        .alpha(cellColorAlpha)
                        .scale(scale.value)


                )
            }
        }
    }
}


//@Composable
//fun IconWithExactVectorBorder(
//    icon: ImageVector,
//    iconColor: Color,
//    borderColor: Color,
//    borderWidth: Float,
//    sizeDp: Int
//) {
//    Box {
//        // Draw the border using the exact path of the vector
//        Canvas(modifier = Modifier.size(sizeDp.dp)) {
//            icon.root.clipPathData?.forEach { clipPath ->
//                val path = PathBuilder().apply {
//                    addPathNodes(clipPath.nodes)
//                }.build()
//
//                drawPath(
//                    path = path,
//                    color = borderColor,
//                    style = Stroke(width = borderWidth)
//                )
//            }
//        }
//
//        // Draw the icon on top
//        Icon(
//            imageVector = icon,
//            contentDescription = null,
//            tint = iconColor,
//            modifier = Modifier.size(sizeDp.dp)
//        )
//    }
//}
