package com.example.kbbqreview.data.roomplaces

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StoredPlace::class], version = 1, exportSchema = false)
abstract class StoredPlaceDatabase : RoomDatabase() {
    abstract fun userDao(): StoredPlaceDao

    companion object {
        @Volatile
        private var INSTANCE: StoredPlaceDatabase? = null

        fun getDatabase(context: Context): StoredPlaceDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StoredPlaceDatabase::class.java,
                    "user_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}