package com.nutr1t07.lifelog.helpers

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun timestamp2Date(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun date2Timestamp(date: Date?): Long? {
        return date?.time
    }
}