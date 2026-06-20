package com.edapp.habittracker.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.edapp.habittracker.domain.ReminderData

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val habitId: Long = 0,
    val title: String,
    val description: String,
    val iconName: String,
    val consistencyIconName: String,
    val reminders: List<ReminderData>, // Room will store as JSON
    val orderIndex: Int = 0, // auto-managed in repository
    val colorValue: Long,  // stored as Long in DB
    val uncheckedColorValue: Long,  // stored as Long in DB (SDK-specific per-habit unchecked color)
    val tagId: List<Long>?,
    val habitCategory : Int = 0,
    // New flag to mark archived habits. Default false for existing rows when migrating
    val isArchived: Boolean = false,
    // New flags for locked habits
    val isLocked: Boolean = false,
    val passKey: String? = null
)

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habitOwnerId", "epochDay"] // 👈 composite PK  select * from habit_logs 20286
)
data class HabitLogEntity(
    val habitOwnerId: Long,  // Foreign key to HabitEntity.habitId
    val epochDay: Long,      // LocalDate.toEpochDay()
    val status: Int
)


@Entity(tableName = "habit_tag")
data class HabitTagEntity(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    val title: String,
    val icon: String,
    val colorValue: Long
)

data class HabitWithLogs(
    @Embedded val habit: HabitEntity,
    @Relation(
        parentColumn = "habitId",
        entityColumn = "habitOwnerId"
    )
    val logs: List<HabitLogEntity>
)
