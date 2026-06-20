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
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            tagId = newHabit.tagIds.toList(),
            isArchived = newHabit.isArchived,
            isLocked = newHabit.isLocked
        )
        // Use insertHabit so orderIndex is auto-managed (new habits appended to end)
        val habitId = insertHabit(habitEntity)
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
        // Keep the same habitId and orderIndex when editing an existing habit
        val habitEntity = HabitEntity(
            habitId = oldHabit?.habitId ?: 0,
            title = editHabit.title,
            description = editHabit.description,
            iconName = editHabit.selectedHabitIcon,
            consistencyIconName = editHabit.selectedHabitConsistencyIcon,
            reminders = editHabit.reminderList ?: emptyList(),
            orderIndex = oldHabit?.orderIndex ?: 0,
            colorValue = editHabit.color.toColorLong(),
            uncheckedColorValue = editHabit.uncheckedColorValue.toColorLong(),
            tagId = editHabit.tagIds.toList(),
            habitCategory = oldHabit?.habitCategory ?: 0,
            isArchived = oldHabit?.isArchived ?: editHabit.isArchived,
            isLocked = editHabit.isLocked
        )
        val habitId = dao.insertHabit(habitEntity) // REPLACE will update existing row
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllHabits(
        tags: List<Long>? = null
    ): Flow<List<Habit>> {

        return dao.getAllHabits()
            .flatMapLatest { dbHabits ->

                // FILTER TAGS HERE
                val filteredHabits = dbHabits?.filter { habitEntity ->
                    // SHOW ALL
                    if (tags.isNullOrEmpty()) {
                        return@filter true
                    }
                    val habitTags = habitEntity.tagId ?: emptyList()
                    // MATCH ANY TAG
                    habitTags.any { it in tags }
                }

                if (filteredHabits.isNullOrEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        filteredHabits.map { habitEntity ->
                            dao.getLogsForHabit(habitEntity.habitId)
                                .map { logs ->
                                    val mutableLogs = logs!!.toMutableList()

                                    // Determine start and end dates
                                    val startDate: LocalDate = if (mutableLogs.isNotEmpty()) {
                                        LocalDate.ofEpochDay(mutableLogs.minOf { it.epochDay }).minusMonths(3)
                                    } else {
                                        LocalDate.now().minusMonths(3)
                                    }

                                    val endDate: LocalDate = if (mutableLogs.isNotEmpty()) {
                                        LocalDate.ofEpochDay(mutableLogs.maxOf { it.epochDay })
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

    /** Return archived habits mapped to domain model (includes logs). */
    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getArchivedHabits(): Flow<List<Habit>> {
        return dao.getArchivedHabits().flatMapLatest { dbHabits ->
            if (dbHabits.isNullOrEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    dbHabits.map { habitEntity ->
                        dao.getLogsForHabit(habitEntity.habitId)
                            .map { logs ->
                                val mutableLogs = logs!!.toMutableList()

                                val startDate: LocalDate = if (mutableLogs.isNotEmpty()) {
                                    LocalDate.ofEpochDay(mutableLogs.minOf { it.epochDay }).minusMonths(3)
                                } else {
                                    LocalDate.now().minusMonths(3)
                                }

                                val endDate: LocalDate = if (mutableLogs.isNotEmpty()) {
                                    LocalDate.ofEpochDay(mutableLogs.maxOf { it.epochDay })
                                } else {
                                    LocalDate.now()
                                }

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

                                val sortedLogs = mutableLogs.sortedBy { it.epochDay }
                                HabitMapper.mapToDomain(habitEntity, sortedLogs)
                            }
                    }
                ) { combinedHabits ->
                    combinedHabits.toList()
                }
            }
        }
    }

    /** Toggle archive state for a habit. When unarchiving, place it at the end of active list. */
    suspend fun setHabitArchived(habitId: Long, isArchived: Boolean) {
        withContext(Dispatchers.IO) {
            if (!isArchived) {
                // unarchive -> set orderIndex to last among active habits
                val maxIndex = dao.getMaxOrderIndex() ?: -1
                val nextIndex = maxIndex + 1
                dao.updateOrderIndex(habitId, nextIndex)
            }
            dao.updateArchived(habitId, isArchived)
        }
    }

    /** Return locked habits mapped to domain model (includes logs). */
    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getLockedHabits(): Flow<List<Habit>> {
        return dao.getLockedHabits().flatMapLatest { dbHabits ->
            if (dbHabits.isNullOrEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    dbHabits.map { habitEntity ->
                        dao.getLogsForHabit(habitEntity.habitId)
                            .map { logs ->
                                val mutableLogs = logs!!.toMutableList()

                                val startDate: LocalDate = if (mutableLogs.isNotEmpty()) {
                                    LocalDate.ofEpochDay(mutableLogs.minOf { it.epochDay }).minusMonths(3)
                                } else {
                                    LocalDate.now().minusMonths(3)
                                }

                                val endDate: LocalDate = if (mutableLogs.isNotEmpty()) {
                                    LocalDate.ofEpochDay(mutableLogs.maxOf { it.epochDay })
                                } else {
                                    LocalDate.now()
                                }

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

                                val sortedLogs = mutableLogs.sortedBy { it.epochDay }
                                HabitMapper.mapToDomain(habitEntity, sortedLogs)
                            }
                    }
                ) { combinedHabits ->
                    combinedHabits.toList()
                }
            }
        }
    }

    /** Toggle lock state for a habit and set/clear passkey. */
    suspend fun setHabitLocked(habitId: Long, isLocked: Boolean, passKey: String? = null) {
        withContext(Dispatchers.IO) {
            dao.updateLocked(habitId, isLocked, passKey)
        }
    }

    /** Get passkey for a locked habit. */
    suspend fun getPassKeyForHabit(habitId: Long): String? {
        return withContext(Dispatchers.IO) {
            dao.getPassKeyForHabit(habitId)
        }
    }

    suspend fun getAllHabitOriginalEntity() : List<HabitEntity>? {
        val dbHabits = dao.getAllHabits()
        return dbHabits.first()
    }

    /**
     * Return flow of HabitEntity in the DB order (orderIndex ascending) for reordering UI.
     */
    fun getAllHabitsEntitiesForReorder(): Flow<List<HabitEntity>> {
        return dao.getAllHabits().map { it ?: emptyList() }
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

    /** Update a single habit's order index. */
    suspend fun updateOrderIndex(habitId: Long, newIndex: Int) {
        dao.updateOrderIndex(habitId, newIndex)
    }

    /** Update multiple order indices in a single coroutine. */
    suspend fun updateOrderIndices(updated: List<Pair<Long, Int>>) {
        updated.forEach { (id, index) ->
            dao.updateOrderIndex(id, index)
        }
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
            val now = Calendar.getInstance()
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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllHabitsAsList(): List<Habit> {

        val dbHabits = dao.getAllHabitsOnce()

        if (dbHabits.isNullOrEmpty()) {
            return emptyList()
        }

        return dbHabits.map { habitEntity ->

            val logs = dao.getLogsForHabitOnce(habitEntity.habitId) ?: emptyList()

            val mutableLogs = logs.toMutableList()

            // Determine start and end dates
            val startDate: LocalDate = if (mutableLogs.isNotEmpty()) {
                LocalDate.ofEpochDay(mutableLogs.minOf { it.epochDay }).minusMonths(3)
            } else {
                LocalDate.now().minusMonths(3)
            }

            val endDate: LocalDate = if (mutableLogs.isNotEmpty()) {
                LocalDate.ofEpochDay(mutableLogs.maxOf { it.epochDay }).plusDays(1)
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

            // Sort logs
            val sortedLogs = mutableLogs.sortedBy { it.epochDay }

            // Map to domain
            HabitMapper.mapToDomain(habitEntity, sortedLogs)
        }
    }

    /**
     * Delete a tag and remove it from all associated habits
     * @param tagId The ID of the tag to delete
     */
    suspend fun deleteTag(tagId: Long) {
        withContext(Dispatchers.IO) {
            try {
                // Get all habits that have this tag
                val habitsFlow = dao.getHabitByTagId(tagId)
                val habitsList = habitsFlow.first() ?: emptyList()

                // For each habit, remove this tag from its tagId list and update the habit
                habitsList.forEach { habit ->
                    val updatedTagIds = (habit.tagId ?: emptyList()).filterNot { it == tagId }
                    val updatedHabit = habit.copy(tagId = updatedTagIds)
                    dao.insertHabit(updatedHabit)
                }

                // Finally, delete the tag from the habit_tag table
                dao.deleteHabitTag(tagId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}
