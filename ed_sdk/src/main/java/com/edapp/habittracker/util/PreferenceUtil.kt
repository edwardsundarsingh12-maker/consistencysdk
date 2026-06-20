package com.edapp.habittracker.util

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtil {

    private const val PREF_NAME = "habit_prefs"
    private const val KEY_IS_ROW_VIEW = "is_row_view"
    private const val ENABLE_MONTH_VIEW = "enable_month_view"
    private const val SHOW_STREAK_COUNT = "show_streak_count"
    private const val SHOW_MONTH_LABELS = "show_month_labels"
    private const val ENABLE_CATEGORIES = "enable_categories"
    private const val HIGHLIGHT_CURRENT_DAY = "highlight_current_day"
    private const val ENABLE_DAY_TRACKER_LABEL = "enable_day_tracker_label"
    private const val THEME_PRIMARY_COLOR = "theme_primary_color"
    private const val THEME_IS_DARK = "theme_is_dark"
    private const val GLOBAL_PASSKEY = "global_passkey"
    private const val PASSKEY_SET = "passkey_set"
    private const val SECURITY_QUESTION = "security_question"
    private const val SECURITY_ANSWER = "security_answer"

    private fun pref(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setEnableMonthView(context: Context, value: Boolean) {
        pref(context).edit().putBoolean(ENABLE_MONTH_VIEW, value).apply()
    }

    fun isEnableMonthView(context: Context): Boolean {
        return pref(context).getBoolean(ENABLE_MONTH_VIEW, false)
    }

    fun setShowStreakCount(context: Context, value: Boolean) {
        pref(context).edit().putBoolean(SHOW_STREAK_COUNT, value).apply()
    }

    fun isShowStreakCount(context: Context): Boolean {
        return pref(context).getBoolean(SHOW_STREAK_COUNT, false)
    }

    fun setShowMonthLabels(context: Context, value: Boolean) {
        pref(context).edit().putBoolean(SHOW_MONTH_LABELS, value).apply()
    }

    fun isShowMonthLabels(context: Context): Boolean {
        return pref(context).getBoolean(SHOW_MONTH_LABELS, false)
    }

    fun setEnableCategories(context: Context, value: Boolean) {
        pref(context).edit().putBoolean(ENABLE_CATEGORIES, value).apply()
    }

    fun isEnableCategories(context: Context): Boolean {
        return pref(context).getBoolean(ENABLE_CATEGORIES, false)
    }

    fun setHighlightCurrentDay(context: Context, value: Boolean) {
        pref(context).edit().putBoolean(HIGHLIGHT_CURRENT_DAY, value).apply()
    }

    fun isHighlightCurrentDay(context: Context): Boolean {
        return pref(context).getBoolean(HIGHLIGHT_CURRENT_DAY, false)
    }

    fun setEnableDayTrackerLabel(context: Context, value: Boolean) {
        pref(context).edit().putBoolean(ENABLE_DAY_TRACKER_LABEL, value).apply()
    }

    fun isEnableDayTrackerLabel(context: Context): Boolean {
        return pref(context).getBoolean(ENABLE_DAY_TRACKER_LABEL, false)
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** Save isRowView state */
    fun setIsRowView( isRow: Boolean) {
        val context: Context = SDK.getAppContext()
        getPrefs(context).edit().putBoolean(KEY_IS_ROW_VIEW, isRow).apply()
    }

    /** Retrieve isRowView state (default = false). Gated by the SDK's canShowAllMonth config. */
    fun isRowView(): Boolean {
        val context: Context = SDK.getAppContext()
        return getPrefs(context).getBoolean(KEY_IS_ROW_VIEW, false) && SDK.config.canShowAllMonth
    }

    /** Optional: clear all preferences */
    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    // Theme persistence helpers
    fun setThemePrimaryColorLong(colorLong: Long?) {
        val ctx: Context = SDK.getAppContext()
        val prefs = getPrefs(ctx).edit()
        if (colorLong == null) prefs.remove(THEME_PRIMARY_COLOR) else prefs.putLong(THEME_PRIMARY_COLOR, colorLong)
        prefs.apply()
    }

    fun getThemePrimaryColorLong(): Long? {
        val ctx: Context = SDK.getAppContext()
        return if (getPrefs(ctx).contains(THEME_PRIMARY_COLOR)) getPrefs(ctx).getLong(THEME_PRIMARY_COLOR, 0L) else null
    }

    fun setIsDarkTheme(isDark: Boolean) {
        val ctx: Context = SDK.getAppContext()
        getPrefs(ctx).edit().putBoolean(THEME_IS_DARK, isDark).apply()
    }

    fun isDarkTheme(): Boolean? {
        val ctx: Context = SDK.getAppContext()
        return if (getPrefs(ctx).contains(THEME_IS_DARK)) getPrefs(ctx).getBoolean(THEME_IS_DARK, false) else null
    }

    // Global Passkey Management
    fun setGlobalPasskey(passkey: String) {
        val ctx: Context = SDK.getAppContext()
        getPrefs(ctx).edit()
            .putString(GLOBAL_PASSKEY, passkey)
            .putBoolean(PASSKEY_SET, true)
            .apply()
    }

    fun getGlobalPasskey(): String? {
        val ctx: Context = SDK.getAppContext()
        return getPrefs(ctx).getString(GLOBAL_PASSKEY, null)
    }

    fun isPasskeySet(): Boolean {
        val ctx: Context = SDK.getAppContext()
        return getPrefs(ctx).getBoolean(PASSKEY_SET, false)
    }

    fun clearGlobalPasskey() {
        val ctx: Context = SDK.getAppContext()
        getPrefs(ctx).edit()
            .remove(GLOBAL_PASSKEY)
            .remove(PASSKEY_SET)
            .remove(SECURITY_QUESTION)
            .remove(SECURITY_ANSWER)
            .apply()
    }

    // Security question (used to verify identity when resetting a forgotten passkey)
    fun setSecurityQuestion(question: String, answer: String) {
        val ctx: Context = SDK.getAppContext()
        getPrefs(ctx).edit()
            .putString(SECURITY_QUESTION, question)
            .putString(SECURITY_ANSWER, normalizeAnswer(answer))
            .apply()
    }

    fun getSecurityQuestion(): String? {
        val ctx: Context = SDK.getAppContext()
        return getPrefs(ctx).getString(SECURITY_QUESTION, null)
    }

    fun hasSecurityQuestion(): Boolean {
        return getSecurityQuestion() != null
    }

    fun verifySecurityAnswer(answer: String): Boolean {
        val ctx: Context = SDK.getAppContext()
        val savedAnswer = getPrefs(ctx).getString(SECURITY_ANSWER, null) ?: return false
        return savedAnswer == normalizeAnswer(answer)
    }

    private fun normalizeAnswer(answer: String): String = answer.trim().lowercase()
}
