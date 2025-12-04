package com.edapp.habittracker.data

import androidx.compose.ui.graphics.Color
import androidx.room.TypeConverter
import com.edapp.habittracker.domain.ReminderData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromReminderList(reminders: List<ReminderData>): String {
        return gson.toJson(reminders)
    }

    @TypeConverter
    fun toReminderList(data: String?): List<ReminderData> {
        if (data.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<ReminderData>>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun fromLongList(list: List<Long>?): String? {
        return list?.joinToString(",") // Convert List<Long> → "1,2,3"
    }

    @TypeConverter
    fun toLongList(data: String?): List<Long>? {
        return data?.split(",")?.mapNotNull {
            it.toLongOrNull()
        } // Convert "1,2,3" → List<Long>
    }
}
