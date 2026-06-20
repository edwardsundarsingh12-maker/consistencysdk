package com.edapp.habittracker.ui.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.edapp.habittracker.data.HabitRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

/**
 * Utility class for managing widget operations.
 * Triggers broadcast updates to all active HabitWidgetProvider instances.
 */
class HabitWidgetManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitRepository: HabitRepository,
    private val widgetDataStore: WidgetDataStore
) {

    suspend fun setWidgetHabit(habitId: Long) {
        widgetDataStore.saveWidgetHabitId(habitId)
        updateAllWidgets()
    }

    suspend fun clearWidget() {
        widgetDataStore.clearWidgetData()
        updateAllWidgets()
    }

    fun updateAllWidgets() {
        try {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, HabitWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                context.sendBroadcast(
                    Intent(context, HabitWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getDisplayedHabitId(): Long =
        widgetDataStore.getWidgetHabitId().firstOrNull() ?: -1L

    suspend fun updateHabitProgress(habitId: Long, newStatus: com.edapp.habittracker.util.HabitStatusEnum) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                habitRepository.updateHabitStatus(
                    com.edapp.habittracker.domain.HabitLog(
                        habitOwnerId = habitId,
                        epochDay = LocalDate.now().toEpochDay(),
                        status = newStatus
                    )
                )
                updateAllWidgets()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
