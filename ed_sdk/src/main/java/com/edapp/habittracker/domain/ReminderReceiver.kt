package com.edapp.habittracker.domain

import android.Manifest
import android.R
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.edapp.habittracker.data.HabitRepository
import com.edapp.habittracker.util.SDK
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: HabitRepository

    override fun onReceive(context: Context, intent: Intent?) {
        if (!SDK.config.enableReminderNotification) return
        val title = intent?.getStringExtra("title") ?: "Habit Reminder"
        val description1 = intent?.getStringExtra("description") ?: "Habit description"
        val id = intent?.getIntExtra("id", 0) ?: 0
        val habitId = intent?.getLongExtra("habitId", 0) ?: 0

        Log.e("123456789", " ReminderReceiver title = $title")
        // Intent to open app when notification is tapped
        //EDWARD OPEN APP
//        val openAppIntent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            putExtra("reminder_id", id)
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            context,
//            id, // unique ID per reminder
//            openAppIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(description1)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(null)

        // Get NotificationManager
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel (for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = description1
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Show notification
        notificationManager.notify(id, builder.build())

        CoroutineScope(Dispatchers.IO).launch{
            // Schedule next occurrence
            val reminder = repository.getHabitById(habitId) ?: return@launch
            HabitRepository.scheduleAllReminders(reminder.reminders, reminder.title, reminder.description, habitId)
        }
    }
}

