package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "optimization_logs")
data class OptimizationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val ramClearedMb: Int,
    val storageClearedMb: Int,
    val resultMessage: String
)
