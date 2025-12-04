package com.edapp.habittracker.data
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import com.edapp.habittracker.di.HabitMapper
import com.edapp.habittracker.di.toDomain
import com.edapp.habittracker.di.toEntity
import com.edapp.habittracker.domain.Habit
import com.edapp.habittracker.domain.HabitLog
import com.edapp.habittracker.domain.HabitTag
import com.edapp.habittracker.domain.ReminderData
import com.edapp.habittracker.domain.ReminderReceiver
import com.edapp.habittracker.domain.UpdateHabit
import com.edapp.habittracker.util.DayOfWeek
import com.edapp.habittracker.util.HabitStatusEnum
import com.edapp.habittracker.util.SDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class HabitRepository @Inject constructor(
    private val dao: HabitDao
) {

    suspend fun insertOrUpdateHabit(updateHabit: UpdateHabit) {
        if (updateHabit.isNewHabit) {
            insertNewHabit(updateHabit)
        } else {
            editHabit(updateHabit)
        }
    }

    suspend fun insertNewHabit(newHabit: UpdateHabit) {
        val habitEntity = HabitEntity(
            title = newHabit.title,
            description = newHabit.description,
            iconName = newHabit.selectedHabitIcon,
            consistencyIconName = newHabit.selectedHabitConsistencyIcon,
            reminders = newHabit.reminderList ?: emptyList(),
            colorValue = newHabit.color.toColorLong(),
            uncheckedColorValue = newHabit.uncheckedColorValue.toColorLong(),
            tagId = newHabit.tagIds.toList()
        )
        val habitId = dao.insertHabit(habitEntity)
        newHabit.reminderList?.let {
            scheduleAllReminders(it, newHabit.title, newHabit.description , habitId)
        }
    }

    suspend fun updateHabitStatus(habitLog: HabitLog) {
        dao.insertHabitLog(habitLog.toEntity())
    }

    suspend fun editHabit(editHabit: UpdateHabit) {
        val oldHabit = dao.getHabitById(editHabit.oldHabitDbPrimaryKey)
        oldHabit?.reminders?.forEach {
            cancelReminder(it)
        }
        val habitEntity = HabitEntity(
            title = editHabit.title,
            description = editHabit.description,
            iconName = editHabit.selectedHabitIcon,
            consistencyIconName = editHabit.selectedHabitConsistencyIcon,
            reminders = editHabit.reminderList ?: emptyList(),
            colorValue = editHabit.color.toColorLong(),
            uncheckedColorValue = editHabit.uncheckedColorValue.toColorLong(),
            tagId = editHabit.tagIds.toList()
        )
        val habitId = dao.insertHabit(habitEntity)
        editHabit.reminderList?.let {
            scheduleAllReminders(it, editHabit.title, editHabit.description, habitId)
        }
    }

    suspend fun insertDummyHabits(count: Int) {
        withContext(Dispatchers.IO) {
            repeat(count) { i ->
                val habit = HabitEntity(
                    title = "Dummy Habit $i",
                    description = "Description for habit $i",
                    iconName = listOf("Star", "Check", "Favorite", "Alarm").random(),
                    consistencyIconName = listOf("Star", "Check", "Favorite", "Alarm").random(),
                    colorValue = Color.Blue.toColorLong(),
                    uncheckedColorValue = Color.Transparent.toColorLong(),
                    reminders = emptyList(),
                    tagId = emptyList()
                )
                insertHabit(habit) // uses auto-increment orderIndex
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertDummyHabitsWithLogs(count: Int, maxDays: Int = 100) {
        withContext(Dispatchers.IO) {
            repeat(count) { i ->
                // Insert habit
                val habit = HabitEntity(
                    title = "Dummy Habit $i",
                    description = "Description for habit $i",
                    iconName = listOf("Star", "Check", "Favorite", "Alarm").random(),
                    consistencyIconName = listOf("Star", "Check", "Favorite", "Alarm").random(),
                    colorValue = Color.Blue.toColorLong(),
                    uncheckedColorValue = Color.Transparent.toColorLong(),
                    reminders = emptyList(),
                    tagId = emptyList()
                )
                val habitId = insertHabit(habit)

                // Random number of days for logs
                val days = Random.nextInt(1, maxDays + 1)
                val today = LocalDate.now()

                repeat(days) { j ->
                    val logDate = today.minusDays(j.toLong())
                    val log = HabitLogEntity(
                        habitOwnerId = habitId,
                        epochDay = logDate.toEpochDay(),
                        status = HabitStatusEnum.values().random().percentage
                    )
                    insertHabitLog(log)
                }
            }
        }
    }


    // Habit
    // Insert a habit with auto-incremented orderIndex
    suspend fun insertHabit(habit: HabitEntity): Long {
        // Get current max orderIndex
        val maxIndex = dao.getMaxOrderIndex() ?: -1
        val nextIndex = maxIndex + 1

        // Copy habit with updated orderIndex
        val habitWithOrder = habit.copy(orderIndex = nextIndex)

        // Insert into DB
        return dao.insertHabit(habitWithOrder)
    }

    fun getAllHabitsTag() : Flow<List<HabitTag>?>  {
        return dao.getAllHabitsTag().map { it?.map { it.toDomain() } }
    }


    suspend
    fun insertHabitTag(log: HabitTag) : Long? {
        return dao.insertHabitTag(log.toEntity())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllHabits(): Flow<List<Habit>> {
        val currentTime1 = System.currentTimeMillis()
        return dao.getAllHabits().flatMapLatest { dbHabits ->
            val currentTime2 = System.currentTimeMillis()
            val currentTimeFinal = currentTime2 - currentTime1
            if (dbHabits.isNullOrEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    dbHabits.map { habitEntity ->
                        dao.getLogsForHabit(habitEntity.habitId) // must return Flow<List<HabitLogEntity>>
                            .map { logs ->
                                val mutableLogs = logs!!.toMutableList()

                                // Determine start and end dates
                                val startDate: LocalDate = if (mutableLogs.isNotEmpty()) {
                                    LocalDate.ofEpochDay(mutableLogs.minOf { it.epochDay }).minusMonths(3)
                                } else {
                                    LocalDate.now().minusMonths(3)
                                }

                                val endDate: LocalDate = if (mutableLogs.isNotEmpty()) {
                                    LocalDate.ofEpochDay(mutableLogs.minOf { it.epochDay }).minusDays(1)
                                } else {
                                    LocalDate.now()
                                }

                                // Fill missing days
                                var date = startDate
                                while (!date.isAfter(endDate)) {
                                    if (mutableLogs.none { it.epochDay == date.toEpochDay() }) {
                                        mutableLogs.add(
                                            HabitLogEntity(
                                                habitOwnerId = habitEntity.habitId,
                                                epochDay = date.toEpochDay(),
                                                status = HabitStatusEnum.NOT_DONE.percentage
                                            )
                                        )
                                    }
                                    date = date.plusDays(1)
                                }

                                // Sort logs ascending
                                val sortedLogs = mutableLogs.sortedBy { it.epochDay }

                                // Map to domain model
                                HabitMapper.mapToDomain(habitEntity, sortedLogs)
                            }
                    }
                ) { combinedHabits ->
                    combinedHabits.toList()
                }
            }
        }
    }

    suspend fun getAllHabitOriginalEntity() : List<HabitEntity>? {
        val dbHabits = dao.getAllHabits()
        return dbHabits.first()
    }

    suspend fun getHabitById(habitId: Long) : HabitEntity? {
        return dao.getHabitById(habitId)
    }


    // Logs
    suspend fun insertHabitLog(log: HabitLogEntity) {
        dao.insertHabitLog(log)
    }

    suspend fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>?> {
        return dao.getLogsForHabit(habitId)
    }

    suspend fun getHabitLogForDay(habitId: Long, epochDay: Long): HabitLogEntity? {
        return dao.getHabitLogForDay(habitId, epochDay)
    }


    companion object{
        fun scheduleAllReminders(reminders: List<ReminderData>, title: String, description: String, habitId: Long) {
            val context: Context = SDK.getAppContext()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            reminders.forEach { reminder ->
                reminder.selectedDays.forEach { day ->
                    val triggerTime = calculateNextTriggerTime(day, reminder.timeMillis).coerceAtLeast(System.currentTimeMillis())

                    val intent = Intent(context, ReminderReceiver::class.java).apply {
                        putExtra("title", title)
                        putExtra("description", description)
                        putExtra("id", reminder.reminderId)
                        putExtra("habitId", habitId)
                        putExtra("day", day.ordinal) // optional
                    }

                    val requestCode = reminder.reminderId * 10 + day.ordinal
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Schedule exact alarm
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                }
            }
        }


        fun cancelReminder( reminder: ReminderData) {
            val context: Context = SDK.getAppContext()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            reminder.selectedDays.forEach { day ->
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("title", reminder.reminderTitle)
                    putExtra("id", reminder.reminderId)
                }

                val requestCode = reminder.reminderId * 10 + day.ordinal

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel() // Clean up just to be safe
            }
        }



        fun calculateNextTriggerTime(dayOfWeek: DayOfWeek, timeMillis: Long): Long {
            val now = Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
            val reminderTime = Calendar.getInstance().apply { timeInMillis = timeMillis }

            val trigger = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, mapDayToCalendar(dayOfWeek))
                set(Calendar.HOUR_OF_DAY, reminderTime.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, reminderTime.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If the time already passed this week, move to next week
                if (before(now)) add(Calendar.WEEK_OF_YEAR, 1)
            }

            return trigger.timeInMillis
        }

        private fun mapDayToCalendar(dayOfWeek: DayOfWeek): Int = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> Calendar.SUNDAY
            DayOfWeek.MONDAY -> Calendar.MONDAY
            DayOfWeek.TUESDAY -> Calendar.TUESDAY
            DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
            DayOfWeek.THURSDAY -> Calendar.THURSDAY
            DayOfWeek.FRIDAY -> Calendar.FRIDAY
            DayOfWeek.SATURDAY -> Calendar.SATURDAY
        }
    }


}
