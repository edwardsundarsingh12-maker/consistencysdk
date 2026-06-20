package com.edapp.habittracker.data

import android.content.Context
import android.graphics.Color
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class, HabitTagEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {

        // Migration 1 -> 2: add the SDK-specific per-habit unchecked color column.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE habits ADD COLUMN uncheckedColorValue INTEGER NOT NULL DEFAULT ${Color.TRANSPARENT}"
                )
            }
        }

        // Migration 2 -> 3: add isArchived boolean flag default false
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration 3 -> 4: add isLocked and passKey columns
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN `isLocked` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE habits ADD COLUMN `passKey` TEXT")
            }
        }

        @Volatile
        private var INSTANCE: HabitDatabase? = null

        // Direct accessor used outside of Hilt (e.g. Glance widgets / broadcast receivers).
        fun getDatabase(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
