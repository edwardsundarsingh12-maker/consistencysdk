package com.edapp.habittracker.ui.widgets

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import com.edapp.ed_sdk.R
import com.edapp.habittracker.data.HabitDatabase
import com.edapp.habittracker.data.HabitEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class HabitWidgetConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.activity_simple_habit_widget_config)

        CoroutineScope(Dispatchers.IO).launch {
            val dao = HabitDatabase.getDatabase(applicationContext).habitDao()
            val habits = dao.getAllHabitsOnce().filter { !it.isArchived }
            withContext(Dispatchers.Main) { populateHabits(habits) }
        }
    }

    private fun populateHabits(habits: List<HabitEntity>) {
        val container = findViewById<LinearLayout>(R.id.config_habits_container)
        val inflater = LayoutInflater.from(this)

        if (habits.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No habits found. Create some habits in the app first."
                setTextColor(0xAAFFFFFF.toInt())
                textSize = 14f
                setPadding(32, 48, 32, 48)
            }
            container.addView(empty)
            return
        }

        habits.forEach { habit ->
            val item = inflater.inflate(R.layout.item_widget_config_habit, container, false)
            item.findViewById<TextView>(R.id.item_habit_name).text = habit.title
            item.findViewById<TextView>(R.id.item_habit_desc).text =
                habit.description.ifEmpty { "No description" }

            val argb = ComposeColor(habit.colorValue.toULong()).toArgb()
            item.findViewById<View>(R.id.item_color_dot).background = GradientDrawable().apply {
                setColor(argb)
                cornerRadius = 7f * resources.displayMetrics.density
            }

            item.setOnClickListener { onHabitSelected(habit) }

            container.addView(item)
            container.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                ).also { it.setMargins(16, 0, 16, 0) }
                setBackgroundColor(0x22FFFFFF.toInt())
            })
        }
    }

    private fun onHabitSelected(habit: HabitEntity) {
        HabitWidgetProvider.saveHabitId(this, appWidgetId, habit.habitId)
        HabitWidgetProvider().onUpdate(
            this,
            AppWidgetManager.getInstance(this),
            intArrayOf(appWidgetId)
        )
        setResult(
            RESULT_OK,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        )
        finish()
    }
}
