package com.edapp.habittracker.util

enum class DayOfWeek(val shortName: String, val fullName: String) {
    MONDAY("Mon", "Monday"),
    TUESDAY("Tue", "Tuesday"),
    WEDNESDAY("Wed", "Wednesday"),
    THURSDAY("Thu", "Thursday"),
    FRIDAY("Fri", "Friday"),
    SATURDAY("Sat", "Saturday"),
    SUNDAY("Sun", "Sunday");

    companion object {
        fun fromShortName(short: String): DayOfWeek? =
            values().find { it.shortName.equals(short, ignoreCase = true) }

        fun allShortNames(): List<String> = values().map { it.shortName }

        fun allFullNames(): List<String> = values().map { it.fullName }
    }
}
