package com.edapp.habittracker.ui.components

import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edapp.habittracker.di.IconMapper
import com.edapp.habittracker.domain.ReminderData
import com.edapp.habittracker.ui.HabitViewModel
import com.edapp.habittracker.util.DayOfWeek
import com.edapp.habittracker.util.darken
import com.edapp.habittracker.util.isDarkTheme
import com.edapp.habittracker.util.lighten
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReminderListScreen(viewModel: HabitViewModel) {

    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        viewModel.editOrAddHabit.collectAsState().value.reminderList?.forEach { reminder ->
            ReminderCard(
                reminder = reminder,
                isPopup = false,
                onDelete = { viewModel.deleteReminder(reminder) },
                onUpdate = { updated ->
                    viewModel.updateReminder(updated)
                }
            )

        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReminderCard(
    reminder: ReminderData,
    isPopup: Boolean,
    onDelete: () -> Unit,
    onUpdate: (ReminderData) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedDays by remember { mutableStateOf(reminder.selectedDays.toMutableSet()) }
    var timeMillis by remember { mutableStateOf(reminder.timeMillis) }


    fun toggleDay(day: DayOfWeek) {
        selectedDays = selectedDays.toMutableSet().apply {
            if (contains(day)) remove(day) else add(day)
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Convert Long → formatted time
    val formattedTime = remember(timeMillis) {
        if (timeMillis == 0L) "Pick Time"
        else {
            val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
            fmt.format(Date(timeMillis))
        }
    }

    // Time Picker Dialog (12-hour)
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            timeMillis = cal.timeInMillis
            onUpdate(reminder.copy(timeMillis = timeMillis))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    val glowColor by animateColorAsState(
        targetValue = Color.Transparent,
        animationSpec = spring(dampingRatio = 0.5f)
    )
    val edittextBg = if (context.isDarkTheme()) {
        MaterialTheme.colorScheme.background.lighten(0.05f)
    } else {
        MaterialTheme.colorScheme.background.darken(0.95f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(
            Brush.horizontalGradient(
                listOf(glowColor, edittextBg, glowColor)
            ))) {

            // Delete icon (only for list view)
            if (!isPopup) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                if (reminder.reminderTitle.isNotBlank()) {
                    Text(
                        text = reminder.reminderTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Animated days + time chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Day chips
                    DayOfWeek.entries.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        val color by animateColorAsState(
                            targetValue = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface,
                            animationSpec = tween(300)
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.1f else 1f,
                            animationSpec = tween(150, easing = FastOutLinearInEasing)
                        )

                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .background(color, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .clickable {
                                    toggleDay(day)
                                    onUpdate(reminder.copy(selectedDays = selectedDays))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.name.take(3),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Time picker chip with blinking animation
                    val infiniteTransition = rememberInfiniteTransition()
                    val blinkAlpha by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.6f,
                        animationSpec = infiniteRepeatable(
                            tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    ElevatedButton(
                        onClick = { timePickerDialog.show() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Select Time",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formattedTime,
                            modifier = Modifier.alpha(blinkAlpha),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ReminderPopup(
    viewModel: HabitViewModel,
    cardTitle: String = "",
    onDismiss: () -> Unit,
    onSave: (newReminder: ReminderData) -> Unit
) {
    var newReminder by remember {
        mutableStateOf(ReminderData(reminderId = System.currentTimeMillis().hashCode()))
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.85f),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f)
    ) {
        // Dimmed background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    onDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            // Popup card container
            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable(enabled = false) {} // prevent dismiss when clicking inside
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .widthIn(min = 300.dp, max = 400.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = if (cardTitle.isNotEmpty()) cardTitle else "New Reminder",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // The Reminder Card UI
                    ReminderCard(
                        reminder = newReminder,
                        isPopup = true,
                        onDelete = {},
                        onUpdate = { reminder ->
                            newReminder = reminder
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { onSave(newReminder) },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

