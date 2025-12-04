package com.edapp.habittracker.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.edapp.habittracker.data.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: HabitRepository

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val allHabits = repository.getAllHabitOriginalEntity()
                allHabits?.forEach { habit ->
                    habit.reminders.let { reminders ->
                        HabitRepository.scheduleAllReminders(reminders, habit.title, habit.description, habit.habitId)
                    }
                }
            }
        }
    }
}
