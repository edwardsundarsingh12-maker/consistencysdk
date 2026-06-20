package com.edapp.habittracker.ui.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import com.edapp.ed_sdk.R
import com.edapp.habittracker.data.HabitDatabase
import com.edapp.habittracker.data.HabitLogEntity
import com.edapp.habittracker.util.HabitStatusEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_CHECKIN = "com.edapp.habittracker.ACTION_HABIT_WIDGET_CHECKIN"
        const val EXTRA_HABIT_ID = "habit_widget_habit_id"
        const val EXTRA_WIDGET_ID = "habit_widget_appwidget_id"
        private const val PREFS_NAME = "habit_widget_prefs"

        fun saveHabitId(context: Context, appWidgetId: Int, habitId: Long) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putLong("habit_$appWidgetId", habitId).apply()
        }

        fun loadHabitId(context: Context, appWidgetId: Int): Long =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong("habit_$appWidgetId", -1L)

        fun removeHabitId(context: Context, appWidgetId: Int) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().remove("habit_$appWidgetId").apply()
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) refreshWidget(context, appWidgetManager, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_CHECKIN) {
            val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
            val appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (habitId == -1L || appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) performCheckIn(context, habitId)
                    refreshWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (id in appWidgetIds) removeHabitId(context, id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun performCheckIn(context: Context, habitId: Long) {
        val dao = HabitDatabase.getDatabase(context).habitDao()
        val today = LocalDate.now().toEpochDay()
        val existing = dao.getHabitLogForDay(habitId, today)
        if (existing == null || existing.status == HabitStatusEnum.NOT_DONE.percentage) {
            dao.insertHabitLog(HabitLogEntity(habitId, today, HabitStatusEnum.DONE.percentage))
        }
    }

    private fun refreshWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val habitId = loadHabitId(context, appWidgetId)
        val views = RemoteViews(context.packageName, R.layout.simple_habit_widget)

        // Library-safe "open app": launch the host application's launcher activity
        // instead of referencing a concrete MainActivity (which does not exist in the SDK).
        val openAppIntent = (context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPi = PendingIntent.getActivity(
            context,
            appWidgetId * 1000,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppPi)

        if (habitId == -1L) {
            views.setTextViewText(R.id.widget_habit_name, "Tap to configure")
            views.setTextViewText(R.id.widget_habit_description, "Remove & re-add widget to select a habit")
            manager.updateAppWidget(appWidgetId, views)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = HabitDatabase.getDatabase(context).habitDao()
                val habit = dao.getHabitById(habitId) ?: return@launch
                val logs = dao.getLogsForHabitOnce(habitId)

                val habitArgb = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ComposeColor(habit.colorValue.toULong()).toArgb()
                else Color.parseColor("#6750A4")

                val todayStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    dao.getHabitLogForDay(habitId, LocalDate.now().toEpochDay())?.status
                        ?: HabitStatusEnum.NOT_DONE.percentage
                else HabitStatusEnum.NOT_DONE.percentage

                val (streak, completedDays) = calcStats(logs)
                val checkedIn = todayStatus != HabitStatusEnum.NOT_DONE.percentage

                val gridBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    buildGridBitmap(context, logs, habitArgb) else null

                views.setTextViewText(R.id.widget_habit_name, habit.title)
                views.setTextViewText(R.id.widget_habit_description, habit.description.ifEmpty { " " })
                views.setTextViewText(R.id.widget_streak, "🔥 $streak")
                views.setTextViewText(R.id.widget_done_days, "✓ $completedDays days")
                if (gridBitmap != null) views.setImageViewBitmap(R.id.widget_consistency_grid, gridBitmap)

                val btnColor = if (checkedIn) blendWithBlack(habitArgb, 0.5f) else habitArgb
                val btnText = if (checkedIn) "✓" else "▶"
                views.setImageViewBitmap(R.id.widget_checkin_button, buildCircleButtonBitmap(context, btnText, btnColor))

                val checkInPi = PendingIntent.getBroadcast(
                    context, appWidgetId,
                    Intent(context, HabitWidgetProvider::class.java).apply {
                        action = ACTION_CHECKIN
                        putExtra(EXTRA_HABIT_ID, habitId)
                        putExtra(EXTRA_WIDGET_ID, appWidgetId)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_checkin_button, checkInPi)

                manager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) { }
        }
    }

    private fun calcStats(logs: List<HabitLogEntity>): Pair<Int, Int> {
        val completedDays = logs.count { it.status != HabitStatusEnum.NOT_DONE.percentage }
        var streak = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val logsMap = logs.associateBy { it.epochDay }
            var day = LocalDate.now().toEpochDay()
            while (true) {
                val s = logsMap[day]?.status ?: break
                if (s == HabitStatusEnum.NOT_DONE.percentage) break
                streak++
                day--
            }
        }
        return Pair(streak, completedDays)
    }

    // Cells are always square: derive height from cell size computed from width.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildGridBitmap(context: Context, logs: List<HabitLogEntity>, habitArgb: Int): Bitmap {
        val density = context.resources.displayMetrics.density
        val numWeeks = 26
        val labelW = 10f * density
        val gap = 2f * density
        val bmpWidth = (280 * density).toInt().coerceAtLeast(280)

        val cellSize = (bmpWidth - labelW - gap * (numWeeks - 1)) / numWeeks
        val bmpHeight = (cellSize * 7f + gap * 6f + density).toInt().coerceAtLeast(1)

        val bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val logsMap = logs.associateBy { it.epochDay }
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        val todayDow = today.dayOfWeek.value % 7
        val startEpoch = todayEpochDay - todayDow - 25L * 7

        val topOff = density * 0.5f
        val radius = 2f * density

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x88888888.toInt()
            textSize = 6.5f * density
            typeface = Typeface.DEFAULT
        }

        arrayOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { row, label ->
            canvas.drawText(label, 0f, topOff + row * (cellSize + gap) + cellSize * 0.75f, textPaint)
        }

        for (col in 0 until numWeeks) {
            for (row in 0..6) {
                val epochDay = startEpoch + col * 7L + row
                if (epochDay > todayEpochDay) continue
                val x = labelW + col * (cellSize + gap)
                val y = topOff + row * (cellSize + gap)

                paint.style = Paint.Style.FILL
                paint.color = when (logsMap[epochDay]?.status) {
                    HabitStatusEnum.STREAK.percentage   -> habitArgb
                    HabitStatusEnum.DONE.percentage     -> withAlpha(habitArgb, 200)
                    HabitStatusEnum.PARTIAL.percentage  -> withAlpha(habitArgb, 120)
                    HabitStatusEnum.PROGRESS.percentage -> withAlpha(habitArgb, 60)
                    else -> 0x33FFFFFF.toInt()
                }
                canvas.drawRoundRect(x, y, x + cellSize, y + cellSize, radius, radius, paint)

                if (epochDay == todayEpochDay) {
                    paint.color = Color.WHITE
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.2f * density
                    canvas.drawRoundRect(x, y, x + cellSize, y + cellSize, radius, radius, paint)
                }
            }
        }
        return bmp
    }

    private fun buildCircleButtonBitmap(context: Context, text: String, bgColor: Int): Bitmap {
        val dp = context.resources.displayMetrics.density
        val size = (32 * dp).toInt().coerceAtLeast(32)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        paint.apply {
            color = Color.WHITE
            textSize = 11f * dp
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val yPos = size / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(text, size / 2f, yPos, paint)
        return bmp
    }

    private fun withAlpha(argb: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(argb), Color.green(argb), Color.blue(argb))

    private fun blendWithBlack(argb: Int, factor: Float): Int {
        val f = 1f - factor
        return Color.argb(
            Color.alpha(argb),
            (Color.red(argb) * f).toInt(),
            (Color.green(argb) * f).toInt(),
            (Color.blue(argb) * f).toInt()
        )
    }
}
