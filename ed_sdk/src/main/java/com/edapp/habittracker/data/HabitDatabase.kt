package com.edapp.habittracker.data

import android.graphics.Color
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class, HabitTagEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(dataBase: SupportSQLiteDatabase) {
                dataBase.execSQL(
                    "ALTER TABLE habits ADD COLUMN uncheckedColorValue INTEGER NOT NULL DEFAULT ${Color.TRANSPARENT}"
                )
            }
        }
    }

}
