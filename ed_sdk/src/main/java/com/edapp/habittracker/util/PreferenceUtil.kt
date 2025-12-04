package com.edapp.habittracker.util

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtil {

    private const val PREF_NAME = "habit_prefs"
    private const val KEY_IS_ROW_VIEW = "is_row_view"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** Save isRowView state */
    fun setIsRowView( isRow: Boolean) {
        val context: Context = SDK.getAppContext()
        getPrefs(context).edit().putBoolean(KEY_IS_ROW_VIEW, isRow).apply()
    }

    /** Retrieve isRowView state (default = false) */
    fun isRowView(): Boolean {
        val context: Context = SDK.getAppContext()

        return getPrefs(context).getBoolean(KEY_IS_ROW_VIEW, false) && SDK.config.canShowAllMonth
    }

    /** Optional: clear all preferences */
    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}