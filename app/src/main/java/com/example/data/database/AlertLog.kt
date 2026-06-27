package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_logs")
data class AlertLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val temperature: Float,
    val alertType: String, // "BATTERY", "CPU", "SYSTEM"
    val message: String
)
