package com.nutr1t07.lifelog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nutr1t07.lifelog.helpers.Converters


@Database(entities = [Event::class], version = 3)
@TypeConverters(Converters::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        private var db: EventDatabase? = null

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE event ADD COLUMN nameSec TEXT")
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE event ADD COLUMN modifyTime INTEGER DEFAULT 1598520507 NOT NULL")
            }
        }

        fun getInstance(context: Context): EventDatabase {
            if (db == null) {
                synchronized(EventDatabase::class) {
                    if (db == null) {
                        db = Room.databaseBuilder(context, EventDatabase::class.java, "event.db")
                            .addMigrations(
                                MIGRATION_1_2,
                                MIGRATION_2_3
                            )
                            .build()
                    }
                }
            }
            return db!!
        }
    }
}
