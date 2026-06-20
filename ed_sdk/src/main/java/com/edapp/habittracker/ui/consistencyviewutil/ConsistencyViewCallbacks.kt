package com.edapp.habittracker.ui.consistencyviewutil

import com.edapp.habittracker.ui.consitency.HabitDropdownAction
import com.edapp.habittracker.util.HabitStatusEnum

/**
 * Unified callback interface for all ConsistencyView operations.
 * This is used for both MainScreen and ArchivedHabits screens with their respective ViewModels.
 * Supports different callback implementations for different screens.
 */
interface ConsistencyViewCallbacks {

    /**
     * Called when a habit's daily progress is updated (clicking on today's circle)
     */
    fun onUpdateTodayProgress(habitStatus: HabitStatusEnum, habitId: Int)

    /**
     * Called when a dropdown action is performed (Edit, Delete, Archive, etc.)
     */
    fun onHabitDropdownAction(habitId: Int, habitDropdownAction: HabitDropdownAction)

    /**
     * Called when progress is updated by selecting a specific epoch day
     */
    fun onUpdateProgressByEpoDay(habitStatus: HabitStatusEnum, habitOwnerId: Int, epochDay: Long)

    /**
     * Called when view layout is toggled (row vs grid)
     */
    fun onSetIsRowView(isRowView: Boolean)

    /**
     * Called when a tag is toggled for filtering
     */
    fun onToggleTag(tagId: Int)

    /**
     * Called to clear all tag selections
     */
    fun onClearSelection()

    /**
     * Called when AddAsWidget action is triggered
     */
    fun onAddAsWidget(habitId: Long, habitTitle: String, habitDescription: String)
}
