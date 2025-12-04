package com.edapp.habittracker.util

sealed class Screens(val route: String) {
    object Main: Screens("main_screen")
    object AddHabit: Screens("add_or_edit_screen")
    object Settings: Screens("settings_screen")
//    object Board: Screens("board_screen")
}