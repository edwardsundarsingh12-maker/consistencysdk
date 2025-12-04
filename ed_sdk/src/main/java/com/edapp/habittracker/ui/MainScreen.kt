package com.edapp.habittracker.ui

import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.navigation.NavHostController
import com.edapp.habittracker.ui.components.ToolbarWithAnimation
import com.edapp.habittracker.ui.consitency.ConsistencysRow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.edapp.habittracker.ui.components.HabitCalendarBottomSheet
import com.edapp.habittracker.ui.components.WaterFillCircle
import com.edapp.habittracker.ui.consitency.MonthConsistencyCompose
import com.edapp.habittracker.util.FullScreenLoader
import com.edapp.habittracker.util.HabitStatusEnum
import com.edapp.habittracker.util.IconRepresentation
import com.edapp.habittracker.util.SDK
import com.edapp.habittracker.util.Screens
import com.edapp.habittracker.util.darken
import com.edapp.habittracker.util.isDarkTheme
import com.edapp.habittracker.util.lighten
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HabitViewModel,
    navController: NavHostController
) {

    val habits = viewModel.habits.collectAsState().value

    val context = LocalContext.current
    val habitBgColor = if (context.isDarkTheme()) {
        MaterialTheme.colorScheme.background.lighten(0.05f)
    } else {
        MaterialTheme.colorScheme.background.darken(0.95f)
    }

    val isRowView  = viewModel.isRowView.collectAsState()
    val todayHabitStatus by remember {
        mutableStateOf(HabitStatusEnum.NOT_DONE)
    }

    val openCalenderDatePicker = remember { mutableStateOf(false) }

    var selectedHabitIndex by remember { mutableStateOf(0) }


    Scaffold(
        topBar = {
            ToolbarWithAnimation(
                onSettingsClick = {},
                onAddClick = {
                    if (SDK.config.enableAddNewHabit){ navController.navigate(Screens.AddHabit.route) }
                }
            )
        },
        floatingActionButton = {
            CenterFloatingToggleButton(
                isRowView = isRowView.value,
                onToggle = {
                    viewModel.setIsRowView(!isRowView.value)
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        ConsistencyRowView(
            viewModel, padding
        )
    }
}

@Composable
fun ConsistencyRowView(
    viewModel: HabitViewModel,
    padding: PaddingValues,
) {

    val habits = viewModel.habits.collectAsState().value

    val context = LocalContext.current
    val habitBgColor = if (context.isDarkTheme()) {
        MaterialTheme.colorScheme.background.lighten(0.05f)
    } else {
        MaterialTheme.colorScheme.background.darken(0.95f)
    }

    val isRowView  = viewModel.isRowView.collectAsState()
    val todayHabitStatus by remember {
        mutableStateOf(HabitStatusEnum.NOT_DONE)
    }

    val openCalenderDatePicker = remember { mutableStateOf(false) }

    var selectedHabitIndex by remember { mutableStateOf(0) }

    if (isRowView.value){
        if (habits.isEmpty()) {
//                HabitGridShimmerFullWidth(habitBgColor)
            FullScreenLoader(color = MaterialTheme.colorScheme.primary)
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
            ) {
                items(habits.size) { index ->
                    val habit = habits[index]
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(habitBgColor)
                            .padding(8.dp)
                            .clickable(indication = null, interactionSource = null) {
                                if (SDK.config.enableRowEditOption){
                                    selectedHabitIndex = index
                                    openCalenderDatePicker.value = !openCalenderDatePicker.value
                                }
                            }
                    ) {
                        val progressValue = remember {
                            mutableFloatStateOf(50f)
                        }
                        HabitStatus(
                            title = habit.title,
                            description = habit.description,
                            isRowView = isRowView.value,
                            habitIcon = habit.icon,
                            activeColor = habit.color,
                            uncheckedColorValue = habit.uncheckedColorValue,
                            todayHabitStatus = habit.todayHabitStatus
                        ) {
                            viewModel.updateTodayProgress(it, habit.id)
                        }
                        ConsistencysRow(habit, viewModel)
                    }
                }
            }
        }
    }  else {
        if (habits.isEmpty()) {
//                HabitGridShimmer(habitBgColor)
            FullScreenLoader(color = MaterialTheme.colorScheme.primary)

        } else {
            LazyVerticalGrid(
                modifier = Modifier.padding(padding),
                columns = GridCells.Adaptive(140.dp)
            ) {
                items(habits.size) { index ->
                    val habit = habits[index]
                    val year = habit.years.lastOrNull()
                    val month = year?.months?.lastOrNull()
                    month?.let {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(habitBgColor)
                                .padding(8.dp)
                                .clickable(indication = null, interactionSource = null) {
                                    selectedHabitIndex = index
                                    openCalenderDatePicker.value = !openCalenderDatePicker.value
                                },
                        ) {
                            val progressValue = remember {
                                mutableFloatStateOf(50f)
                            }
                            HabitStatus(
                                title = habit.title,
                                description = habit.description,
                                habitIcon = habit.icon,
                                activeColor = habit.color,
                                todayHabitStatus = habit.todayHabitStatus,
                                isRowView = isRowView.value
                            ) {
                                viewModel.updateTodayProgress(it, habit.id)
                            }
                            MonthConsistencyCompose(
                                habitMonth = it,
                                year = year.year,
                                viewModel = viewModel,
                                showYear = true,
                                cellIcon = habit.consistencyIcon,
                                activeColor = habit.color,
                                uncheckedColorValue = habit. uncheckedColorValue,
                            )
                        }
                    }
                }
            }
        }
    }
    val sampleProgress = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mapOf(
                LocalDate.now().minusDays(2).toEpochDay() to 20,
                LocalDate.now().minusDays(1).toEpochDay() to 40,
                LocalDate.now().toEpochDay() to 70,
                LocalDate.now().plusDays(1).toEpochDay() to 100
            )
        } else {
            mapOf()
        }
    }

    // ---- DARK OVERLAY TO MAKE BG FULLY UNREADABLE ----
    if (openCalenderDatePicker.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp)
                .background(Color.Black.copy(alpha = 0.80f)) // darker + better freeze
        )
    }

    // ---- BOTTOM SHEET ----
    if (openCalenderDatePicker.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        HabitCalendarBottomSheet(
            habits[selectedHabitIndex],
            viewModel,
            onDismiss = {
                openCalenderDatePicker.value = false
            },
            onDayClick = { localDay, habitStatus, habitOwnerId ->
                val formatted = localDay.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                Log.e("123456789", "Called = ${formatted} eppo day =  ${localDay.toEpochDay()}")
                if(habitStatus != null && habitOwnerId != null) {
                    viewModel.updateProgressByEpoDay(habitStatus, habitOwnerId, localDay.toEpochDay())
                }
            }
        )
    }
}


@Composable
fun HabitStatus(
    isRowView: Boolean = false,
    habitIcon : IconRepresentation,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColorValue: Color = MaterialTheme.colorScheme.primary,
    todayHabitStatus: HabitStatusEnum,
    title: String = " Do Gym Today",
    description: String = "Do My Best ",
    updateHabitProgress: (HabitStatusEnum) -> Unit
) {
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isRowView)Arrangement.SpaceBetween else Arrangement.Start
    ){

        val currentDayPercentage = remember {
            mutableFloatStateOf(todayHabitStatus.percentage.toFloat())
        }

//        when(todayHabitStatus) {
//            HabitStatus.NOT_DONE -> {
//                0%
//            }
//            HabitStatus.PROGRESS -> {
//                0%
//            }
//            HabitStatus.PARTIAL -> {
//                25%
//            }
//            HabitStatus.DONE -> {
//                50%
//            }
//            HabitStatus.STREAK -> {
//                100%
//            }
//        }
        if (isRowView) {
            if (habitIcon is IconRepresentation.Vector) {
                Icon(
                    imageVector = habitIcon.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color = activeColor.copy(alpha = 0.5f))
                        .size(32.dp)
                        .padding(4.dp)
                )
            } else if(habitIcon is IconRepresentation.Emoji) {
                Text(
                    text = habitIcon.value,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .alpha(0.5f)
                        .padding(4.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = description,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            WaterFillCircle(
                percentage = currentDayPercentage.value,
                sizeDp = 32,
                activeColor = activeColor
            ) {
                if (currentDayPercentage.value < 50f) {
                    currentDayPercentage.value += 25f
                } else if (currentDayPercentage.value == 50f) {
                    currentDayPercentage.value += 50f
                } else {
                    currentDayPercentage.value = 0f
                }
                updateHabitProgress(HabitStatusEnum.getObjByPercentage(currentDayPercentage.value.toInt()))
            }
        } else {
            if (currentDayPercentage.value != 100f) {
                WaterFillCircle(
                    percentage = currentDayPercentage.value,
                    sizeDp = 32,
                    activeColor = activeColor
                ) {
                    if (currentDayPercentage.value < 50f) {
                        currentDayPercentage.value += 25f
                    } else if (currentDayPercentage.value == 50f) {
                        currentDayPercentage.value += 50f
                    } else {
                        currentDayPercentage.value = 0f
                    }
                    updateHabitProgress(HabitStatusEnum.getObjByPercentage(currentDayPercentage.value.toInt()))
                }
            } else {
                if (habitIcon is IconRepresentation.Vector) {
                    Icon(
                        imageVector = habitIcon.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color = activeColor)
                            .size(32.dp)
                            .clickable {
                                currentDayPercentage.value = 0f
                                updateHabitProgress(
                                    HabitStatusEnum.getObjByPercentage(
                                        currentDayPercentage.value.toInt()
                                    )
                                )
                            }
                            .padding(4.dp)
                    )
                } else if( habitIcon is IconRepresentation.Emoji) {
                    Text(
                        text = habitIcon.value,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                currentDayPercentage.value = 0f
                                updateHabitProgress(
                                    HabitStatusEnum.getObjByPercentage(
                                        currentDayPercentage.value.toInt()
                                    )
                                )
                            }
                            .padding(4.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp, top = 0.dp , bottom = 0.dp)
                )
                Text(
                    text = description,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp, top = 0.dp , bottom = 0.dp)
                )
            }
        }
    }
}


@Composable
fun FourPointGradientSlider(
    modifier: Modifier = Modifier,
    value: Float,
    trackHeight: Dp = 8.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onValueChange: (Float) -> Unit
) {
    val minValue = 0f
    val maxValue = 100f
    val steps = 3 // 4 discrete points: 0, 25, 50, 100

    // Gradient colors for track
    val startColor = MaterialTheme.colorScheme.background
    val endColor = activeColor

    Slider(
        value = value,
        onValueChange = {
            // Snap to nearest 0, 25, 50, 100
            val snapped = (it / 25).roundToInt() * 25f
            onValueChange(snapped)
        },
        valueRange = minValue..maxValue,
        steps = steps - 1,
        modifier = modifier
            .height(trackHeight)
            .fillMaxWidth(0.2f)
            .drawBehind {
                // Draw custom gradient track
                val trackWidth = size.width
                val trackRadius = trackHeight.toPx() / 2

                // Gradient from red to green
                val gradient = Brush.horizontalGradient(
                    colors = listOf(startColor, endColor),
                    startX = 0f,
                    endX = trackWidth
                )

                // Draw the track background
                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(0f, (size.height - trackHeight.toPx()) / 2),
                    size = androidx.compose.ui.geometry.Size(trackWidth, trackHeight.toPx()),
                    cornerRadius = CornerRadius(trackRadius, trackRadius)
                )
            },
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent
        )
    )
}


@Composable
fun CenterFloatingToggleButton(
    isRowView: Boolean,
    onToggle: () -> Unit
) {
    FloatingActionButton(
        onClick = { onToggle() },
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(8.dp)
    ) {
        val icon = if (isRowView) Icons.Default.GridView else Icons.Default.ViewList
        val description = if (isRowView) "Grid View" else "Row View"

        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(28.dp)
        )
    }
}

