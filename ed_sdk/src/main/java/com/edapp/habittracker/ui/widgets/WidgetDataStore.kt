package com.edapp.habittracker.ui.widgets

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val WIDGET_PREFERENCES = "widget_preferences"
private val appContext = preferencesDataStore(name = WIDGET_PREFERENCES)

/**
 * DataStore for managing widget-related data such as which habit is currently displayed
 */
class WidgetDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by appContext

    // Key for storing the habit ID currently displayed in widget
    companion object {
        private val WIDGET_HABIT_ID = longPreferencesKey("widget_habit_id")
    }

    /**
     * Save the habit ID that should be displayed in the widget
     */
    suspend fun saveWidgetHabitId(habitId: Long) {
        context.dataStore.edit { preferences ->
            preferences[WIDGET_HABIT_ID] = habitId
        }
    }

    /**
     * Get the habit ID currently displayed in the widget
     */
    fun getWidgetHabitId(): Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[WIDGET_HABIT_ID] ?: -1L
        }

    /**
     * Clear widget data
     */
    suspend fun clearWidgetData() {
        context.dataStore.edit { preferences ->
            preferences.remove(WIDGET_HABIT_ID)
        }
    }
}
