package com.nutr1t07.lifelog.data

import androidx.room.*
import java.util.*

@Dao
interface EventDao {
    @Query("SELECT * FROM event ORDER BY startTime ASC")
    fun getEvents(): MutableList<Event>

    @Query("SELECT * FROM event WHERE modifyTime > :since")
    fun getEventModified(since: Date): MutableList<Event>

    @Query("SELECT * FROM event WHERE startTime BETWEEN :from AND :to")
    fun getEventBetweenDate(from: Date, to: Date): MutableList<Event>

    @Query("SELECT * FROM event WHERE startTime BETWEEN :from AND :to ORDER BY id DESC")
    fun getEventsBetweenId(from: Int, to: Int): MutableList<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateEvent(event: Event): Long

    @Delete
    fun deleteEvent(event: Event)
}