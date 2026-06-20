package com.edapp.habittracker.util

sealed class Screens(val route: String) {
    object Main: Screens("main_screen")
    object AddHabit: Screens("add_or_edit_screen")
    object EditHabit: Screens("edit_habit_screen")
    object Settings: Screens("settings_screen")
    object GeneralSettings: Screens("general_settings")

    data object DailyReminderSettings :
        Screens("daily_reminder_settings")

    data object ThemeSettings :
        Screens("theme_settings")

    data object Subscription :
        Screens("subscription")

    data object ArchivedHabits :
        Screens("archived_habits")

    data object LockedHabits :
        Screens("locked_habits")

    data object LockedHabitsList :
        Screens("locked_habits_list")

    data object LockedHabitScreen :
        Screens("locked_habit_screen")

    data object ResetPasskey :
        Screens("reset_passkey_screen")

    data object ImportExport :
        Screens("import_export")

    data object ReorderHabits :
        Screens("reorder_habits")

    data object Onboarding :
        Screens("onboarding")

    data object WhatsNew :
        Screens("whats_new")

    data object SendFeedback :
        Screens("send_feedback")

    data object Website :
        Screens("website")

    data object PrivacyPolicy :
        Screens("privacy_policy")

    data object TermsOfService :
        Screens("terms_of_service")

    data object RateApp :
        Screens("rate_app")
}
