package com.edapp.habittracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.edapp.habittracker.data.HabitDao
import com.edapp.habittracker.data.HabitDatabase
import com.edapp.habittracker.data.HabitDatabase.Companion.MIGRATION_1_2
import com.edapp.habittracker.data.HabitRepository
import com.edapp.habittracker.util.SDK
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase {
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    IconMapper.defaultTags.forEach { tag ->
                        db.execSQL(
                            "INSERT INTO habit_tag (title, icon, colorValue) VALUES (?, ?, ?)",
                            arrayOf(tag.title, tag.icon, tag.colorValue)
                        )
                    }
                }
            })
            .addMigrations(MIGRATION_1_2)
            .build()
    }


    @Provides
    @Singleton
    fun provideHabitDao(db: HabitDatabase): HabitDao = db.habitDao()

    @Provides
    @Singleton
    fun provideHabitRepository(dao: HabitDao): HabitRepository {
        val repo = HabitRepository(dao)
        SDK.initRepository(repo)
        return repo
    }
}
