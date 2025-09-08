package com.example.kodomo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [BacDataclassDb::class], version = 1)
abstract class BacKodomoDatabase : RoomDatabase() {
    abstract fun coordinateLogDao(): BacDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: BacKodomoDatabase? = null

        fun bacgetDatabase(context: Context): BacKodomoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BacKodomoDatabase::class.java,
                    "bac_kodomo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
