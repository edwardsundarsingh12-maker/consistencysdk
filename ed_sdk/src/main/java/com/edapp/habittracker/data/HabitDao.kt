package com.edapp.habittracker.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // Habit (Master)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Query("SELECT * FROM habits where habitId = :habitID limit 1")
    suspend fun getHabitById(habitID: Long): HabitEntity?

    // Return habits ordered by orderIndex so UI can display them in desired order
    // Exclude archived habits from the main lists
    @Query("SELECT * FROM habits WHERE isArchived = 0 AND isLocked = 0 ORDER BY orderIndex ASC")
    fun getAllHabits(): Flow<List<HabitEntity>?>

    @Transaction
    @Query("SELECT * FROM habits WHERE isArchived = 0 AND isLocked = 0")
    fun getAllHabitsWithLogs(): Flow<List<HabitWithLogs>>

    @Query("SELECT MAX(orderIndex) FROM habits WHERE isArchived = 0 AND isLocked = 0")
    suspend fun getMaxOrderIndex(): Int?

    // Archived-specific queries
    @Query("SELECT * FROM habits WHERE isArchived = 1 AND isLocked = 0 ORDER BY orderIndex ASC")
    fun getArchivedHabits(): Flow<List<HabitEntity>?>

    @Transaction
    @Query("SELECT * FROM habits WHERE isArchived = 1 AND isLocked = 0")
    fun getArchivedHabitsWithLogs(): Flow<List<HabitWithLogs>>

    // Toggle archive flag
    @Query("UPDATE habits SET isArchived = :isArchived WHERE habitId = :habitId")
    suspend fun updateArchived(habitId: Long, isArchived: Boolean)

    // Locked-specific queries
    @Query("SELECT * FROM habits WHERE isLocked = 1 ORDER BY orderIndex ASC")
    fun getLockedHabits(): Flow<List<HabitEntity>?>

    @Transaction
    @Query("SELECT * FROM habits WHERE isLocked = 1")
    fun getLockedHabitsWithLogs(): Flow<List<HabitWithLogs>>

    // Toggle lock flag and set/clear passkey
    @Query("UPDATE habits SET isLocked = :isLocked, passKey = :passKey WHERE habitId = :habitId")
    suspend fun updateLocked(habitId: Long, isLocked: Boolean, passKey: String?)

    // Get passkey for a habit
    @Query("SELECT passKey FROM habits WHERE habitId = :habitId LIMIT 1")
    suspend fun getPassKeyForHabit(habitId: Long): String?

    // Update a single habit's order index
    @Query("UPDATE habits SET orderIndex = :newIndex WHERE habitId = :habitId")
    suspend fun updateOrderIndex(habitId: Long, newIndex: Int)

    // Habit Logs (Daily tracking)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity)

    @Query("SELECT * FROM habit_logs WHERE habitOwnerId = :habitId order by epochDay ")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>?>

    @Query("SELECT * FROM habit_logs WHERE habitOwnerId = :habitId AND epochDay = :day LIMIT 1")
    suspend fun getHabitLogForDay(habitId: Long, day: Long): HabitLogEntity?

    @Query("""UPDATE habit_logs SET status = :newStatus WHERE habitOwnerId = :habitOwnerId AND epochDay = :epochDay""")
    suspend fun updateStatus( habitOwnerId: Long, epochDay: Long, newStatus: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitTag(log: HabitTagEntity) : Long?

    @Query("Select * from habit_tag")
    fun getAllHabitsTag(): Flow<List<HabitTagEntity>?>

    @Query("select * from habits where tagId = :tagId")
    fun getHabitByTagId(tagId: Long) : Flow<List<HabitEntity>?>


    @Query("""
SELECT * FROM habit_logs
WHERE habitOwnerId = :habitId
""")
    suspend fun getLogsForHabitOnce(
        habitId: Long
    ): List<HabitLogEntity>


    // For export/import backup

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsOnce(): List<HabitEntity>

    @Query("SELECT * FROM habit_logs")
    suspend fun getAllLogsOnce(): List<HabitLogEntity>

    @Query("SELECT * FROM habit_tag")
    suspend fun getAllTagsOnce(): List<HabitTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(
        habits: List<HabitEntity>
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(
        logs: List<HabitLogEntity>
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(
        tags: List<HabitTagEntity>
    )

    // Delete a tag from the habit_tag table
    @Query("DELETE FROM habit_tag WHERE tagId = :tagId")
    suspend fun deleteHabitTag(tagId: Long)

}
