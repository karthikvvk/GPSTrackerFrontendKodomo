package com.example.kodomo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [DataclassDb::class], version = 1)
abstract class KodomoDatabase : RoomDatabase() {
    abstract fun coordinateLogDao(): DatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: KodomoDatabase? = null

        fun getDatabase(context: Context): KodomoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KodomoDatabase::class.java,
                    "kodomo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
