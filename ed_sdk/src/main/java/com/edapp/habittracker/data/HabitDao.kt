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

    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<HabitEntity>?>

    @Query("SELECT * FROM habits LIMIT 1")
    fun getLastMonthHabit(): Flow<List<HabitEntity>>

    @Transaction
    @Query("SELECT * FROM habits")
    fun getAllHabitsWithLogs(): Flow<List<HabitWithLogs>>

    @Query("SELECT MAX(orderIndex) FROM habits")
    suspend fun getMaxOrderIndex(): Int?

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


}
