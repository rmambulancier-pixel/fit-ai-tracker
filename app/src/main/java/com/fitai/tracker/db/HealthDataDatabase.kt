package com.fitai.tracker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HealthDataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HealthDataDatabase : RoomDatabase() {
    abstract fun healthDataDao(): HealthDataDao

    companion object {
        @Volatile
        private var INSTANCE: HealthDataDatabase? = null

        fun getInstance(context: Context): HealthDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDataDatabase::class.java,
                    "health_data_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
