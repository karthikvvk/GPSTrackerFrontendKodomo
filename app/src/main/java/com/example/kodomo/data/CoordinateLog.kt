package com.example.kodomo.data

import android.icu.text.SimpleDateFormat
import androidx.room.PrimaryKey
import androidx.room.Entity
import java.util.Date
import java.util.Locale


fun getCurrentFormattedTime(): String {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    return formatter.format(java.util.Date())
}


@Entity(tableName = "dataclassdb")
data class DataclassDb(
    @PrimaryKey(autoGenerate = false) val loggedTime: String = getCurrentFormattedTime(),
    val xCord: Double,
    val yCord: Double,
    val simdate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
)

@Entity(tableName = "bacdataclassdb")
data class BacDataclassDb(
    @PrimaryKey(autoGenerate = false) val loggedTime: String = getCurrentFormattedTime(),
    val xCord: Double,
    val yCord: Double,
    val simdate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
)