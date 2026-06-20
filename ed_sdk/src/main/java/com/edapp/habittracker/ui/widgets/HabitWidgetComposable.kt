package com.edapp.habittracker.ui.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.compose.ui.graphics.Color
import androidx.glance.background
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import com.edapp.habittracker.domain.Habit
import com.edapp.habittracker.util.HabitStatusEnum
import androidx.compose.material.icons.Icons


/**
 * Composable for rendering a single habit widget with consistency view
 * Shows habit name, description, today's status, and consistency row
 * Allows updates to today's progress but no long press actions
 */
@Composable
fun HabitWidgetComposable(
    habit: Habit,
    onProgressUpdate: (HabitStatusEnum) -> Unit
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(8.dp)
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.surface)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Habit Title and Description
            Text(
                text = habit.title,
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = habit.description,
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Today's Status Row
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today: ${habit.todayHabitStatus.name}"
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Progress percentage display
                Text(
                    text = "${habit.todayHabitStatus.percentage}%"
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Stats Row
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Text(
                    text = "🔥 ${habit.streake}",

                )

                Text(
                    text = "✅ ${habit.completedDays}/${habit.totalDays}",

                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Consistency mini-display text
            Text(
                text = "Last ${habit.streake} days consistent",
                style = TextStyle(
                    color = GlanceTheme.colors.tertiary
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }
}
