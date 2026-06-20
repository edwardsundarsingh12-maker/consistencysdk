package com.edapp.habittracker.ui.consistencyviewutil

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edapp.habittracker.ui.HabitStatus
import com.edapp.habittracker.ui.HabitStatsRow
import com.edapp.habittracker.ui.components.HabitCalendarBottomSheet
import com.edapp.habittracker.ui.components.HabitTagRow
import com.edapp.habittracker.ui.components.ToolbarWithAnimation
import com.edapp.habittracker.ui.consitency.ConsistencysRow
import com.edapp.habittracker.ui.consitency.HabitDropdownAction
import com.edapp.habittracker.ui.consitency.HabitDropdownMenu
import com.edapp.habittracker.ui.consitency.MonthConsistencyCompose
import com.edapp.habittracker.util.FullScreenLoader
import com.edapp.habittracker.util.Screens
import com.edapp.habittracker.util.darken
import com.edapp.habittracker.util.isDarkTheme
import com.edapp.habittracker.util.lighten
import java.time.format.DateTimeFormatter

/**
 * Main utility composable for displaying habits in consistency view
 * Reusable across MainScreen and ArchivedHabits screens
 *
 * This component handles the UI rendering without tying to a specific ViewModel
 * All interactions are delegated to callbacks, allowing different screens to handle
 * operations according to their own business logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsistencyViewComposable(
    state: ConsistencyViewState,
    callbacks: ConsistencyViewCallbacks,
    navController: NavHostController,
    onSettingsClick: () -> Unit = {
        navController.navigate(Screens.Settings.route)
    },
    onAddClick: () -> Unit = {
        navController.navigate(Screens.AddHabit.route)
    }
) {
    Scaffold(
        topBar = {
            Column {
                ToolbarWithAnimation(
                    onSettingsClick = onSettingsClick,
                    onAddClick = onAddClick
                )

                HabitTagRow(
                    tags = state.allTags,
                    selectedTags = state.selectedTags,
                    onTagClick = { tag ->
                        if (tag == null) {
                            callbacks.onClearSelection()
                        } else {
                            callbacks.onToggleTag(tag.tagId.toInt())
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            ConsistencyViewFloatingButton(
                isRowView = state.isRowView,
                onToggle = {
                    callbacks.onSetIsRowView(!state.isRowView)
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        ConsistencyHabitsList(
            state = state,
            callbacks = callbacks,
            modifier = Modifier.padding(padding)
        )
    }
}

/**
 * Composable that renders the habits list based on view mode
 * Supports both row view and grid view
 */
@Composable
private fun ConsistencyHabitsList(
    state: ConsistencyViewState,
    callbacks: ConsistencyViewCallbacks,
    modifier: Modifier
) {
    val context = LocalContext.current
    val habitBgColor = if (context.isDarkTheme()) {
        MaterialTheme.colorScheme.background.lighten(0.05f)
    } else {
        MaterialTheme.colorScheme.background.darken(0.95f)
    }

    val openCalenderDatePicker = remember { mutableStateOf(false) }
    var selectedHabitIndex by remember { mutableStateOf(0) }

    if (state.isRowView) {
        if (state.habits.isEmpty()) {
            FullScreenLoader(color = MaterialTheme.colorScheme.primary)
        } else {
            LazyColumn(modifier = modifier) {
                items(state.habits.size) { index ->
                    val habit = state.habits[index]
                    var showMenu by remember { mutableStateOf(false) }
                    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
                    var itemPosition by remember { mutableStateOf(Offset.Zero) }

                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(habitBgColor)
                            .padding(8.dp)
                            .onGloballyPositioned { coordinates ->
                                itemPosition = coordinates.positionInWindow()
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        selectedHabitIndex = index
                                        openCalenderDatePicker.value = true
                                    },
                                    onLongPress = { offset ->
                                        with(density) {
                                            menuOffset = DpOffset(
                                                x = (itemPosition.x + offset.x).toDp(),
                                                y = (itemPosition.y + offset.y).toDp()
                                            )
                                        }
                                        showMenu = true
                                    }
                                )
                            }
                    ) {
                        HabitStatus(
                            title = habit.title,
                            description = habit.description,
                            isRowView = state.isRowView,
                            habitIcon = habit.icon,
                            activeColor = habit.color,
                            todayHabitStatus = habit.todayHabitStatus
                        ) {
                            callbacks.onUpdateTodayProgress(it, habit.id.toInt())
                        }
                        ConsistencysRow(habit)
                        HabitStatsRow(
                            totalDays = habit.totalDays,
                            completedDays = habit.completedDays,
                            streak = habit.streake
                        )
                    }

                    HabitDropdownMenu(
                        expanded = showMenu,
                        actions = listOf(
                            HabitDropdownAction.Edit,
                            HabitDropdownAction.Archive,
                            HabitDropdownAction.Lock,
                            HabitDropdownAction.Delete,
                            HabitDropdownAction.AddAsWidget
                        ),
                        onDismiss = { showMenu = false },
                        offset = menuOffset,
                        onActionClick = { action ->
                            callbacks.onHabitDropdownAction(habit.id.toInt(), action)
                        },
                    )
                }
            }
        }
    } else {
        if (state.habits.isEmpty()) {
            FullScreenLoader(color = MaterialTheme.colorScheme.primary)
        } else {
            LazyVerticalGrid(
                modifier = modifier,
                columns = GridCells.Adaptive(140.dp)
            ) {
                items(state.habits.size) { index ->
                    val habit = state.habits[index]
                    val year = habit.years.lastOrNull()
                    val month = year?.months?.lastOrNull()

                    month?.let {
                        var showMenu by remember { mutableStateOf(false) }
                        var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
                        var itemPosition by remember { mutableStateOf(Offset.Zero) }

                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(habitBgColor)
                                .padding(8.dp)
                                .onGloballyPositioned { coordinates ->
                                    itemPosition = coordinates.positionInWindow()
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            selectedHabitIndex = index
                                            openCalenderDatePicker.value = true
                                        },
                                        onLongPress = { offset ->
                                            with(density) {
                                                menuOffset = DpOffset(
                                                    x = (itemPosition.x + offset.x).toDp(),
                                                    y = (itemPosition.y + offset.y).toDp()
                                                )
                                            }
                                            showMenu = true
                                        }
                                    )
                                }
                        ) {
                            HabitStatus(
                                title = habit.title,
                                description = habit.description,
                                habitIcon = habit.icon,
                                activeColor = habit.color,
                                todayHabitStatus = habit.todayHabitStatus,
                                isRowView = state.isRowView
                            ) {
                                callbacks.onUpdateTodayProgress(it, habit.id.toInt())
                            }
                            MonthConsistencyCompose(
                                habitMonth = it,
                                year = year.year,
                                showYear = true,
                                cellIcon = habit.consistencyIcon,
                                activeColor = habit.color
                            )
                            HabitStatsRow(
                                totalDays = habit.totalDays,
                                completedDays = habit.completedDays,
                                streak = habit.streake
                            )
                        }

                        HabitDropdownMenu(
                            expanded = showMenu,
                            actions = listOf(
                                HabitDropdownAction.Edit,
                                HabitDropdownAction.Archive,
                                HabitDropdownAction.Lock,
                                HabitDropdownAction.Delete,
                                HabitDropdownAction.AddAsWidget
                            ),
                            onDismiss = { showMenu = false },
                            offset = menuOffset,
                            onActionClick = { action ->
                                callbacks.onHabitDropdownAction(habit.id.toInt(), action)
                            },
                        )
                    }
                }
            }
        }
    }

    // Dark overlay for calendar picker
    if (openCalenderDatePicker.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp)
                .background(Color.Black.copy(alpha = 0.80f))
        )
    }

    // Calendar bottom sheet
    if (openCalenderDatePicker.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        HabitCalendarBottomSheet(
            state.habits[selectedHabitIndex],
            onDismiss = {
                openCalenderDatePicker.value = false
            },
            onDayClick = { localDay, habitStatus, habitOwnerId ->
                val formatted = localDay.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                Log.e("ConsistencyView", "Day clicked = $formatted, epoch day = ${localDay.toEpochDay()}")
                if (habitStatus != null && habitOwnerId != null) {
                    callbacks.onUpdateProgressByEpoDay(habitStatus, habitOwnerId.toInt(), localDay.toEpochDay())
                }
            }
        )
    }
}

@Composable
private fun ConsistencyViewFloatingButton(
    isRowView: Boolean,
    onToggle: () -> Unit
) {
    FloatingActionButton(
        onClick = { onToggle() },
        shape = RoundedCornerShape(50),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        contentColor = MaterialTheme.colorScheme.primary,
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
