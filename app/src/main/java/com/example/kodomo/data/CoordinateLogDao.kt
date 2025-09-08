package com.example.kodomo.data

import androidx.room.*



@Dao
interface DatabaseDao {

    @Query("SELECT * FROM dataclassdb")
    suspend fun getAll(): List<DataclassDb>

    @Upsert
    suspend fun upsertLog(log: DataclassDb)

    @Delete
    suspend fun deleteLog(log: DataclassDb)

    @Query("SELECT DISTINCT simdate FROM dataclassdb")
    suspend fun getDays(): List<String>


}


