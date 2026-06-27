package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "temperature_logs")
data class TemperatureLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val batteryTemp: Float,
    val cpuTemp: Float,
    val cpuUsage: Float,
    val ramUsage: Float,
    val isCharging: Boolean
)
