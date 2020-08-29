package com.nutr1t07.lifelog.helpers

import java.text.SimpleDateFormat
import java.util.*

val TickInSecond = 1000L

fun getDateFromHHmm(str: String): Date? {
    try {
        val strs = str.split(':')
        val hrs = strs[0].toInt()
        val mins = strs[1].toInt()

        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hrs)
        c.set(Calendar.MINUTE, mins)
        return c.time
    } catch (e: Exception) {
        return null
    }
}

fun getDateFromyyyyMMddHHmm(str: String): Date? {
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        return dateFormat.parse(str)
    } catch (e: Exception) {
        return null
    }
}

fun getyyyyMMddHHmmFromDate(date: Date): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    return dateFormat.format(date)
}

fun getyyyyMMddFromDate(date: Date): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return dateFormat.format(date)
}

fun modyyyyMMddFromDate(d: Date): Date {
    val year = d.year
    val month = d.month
    val date = d.date
    return Date(year, month, date, 0, 0)
}