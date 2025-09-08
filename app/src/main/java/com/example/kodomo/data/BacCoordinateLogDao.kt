package com.example.kodomo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface BacDatabaseDao {

    @Query("SELECT * FROM bacdataclassdb")
    suspend fun getAll(): List<BacDataclassDb>

    @Upsert
    suspend fun upsertLog(log: BacDataclassDb)

    @Delete
    suspend fun deleteLog(log: BacDataclassDb)

    @Query("SELECT DISTINCT simdate FROM bacdataclassdb")
    suspend fun getDays(): List<String>


}