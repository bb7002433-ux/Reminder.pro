package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemperatureLog(log: TemperatureLog)

    @Query("SELECT * FROM temperature_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentTemperatureLogs(): Flow<List<TemperatureLog>>

    @Query("SELECT * FROM temperature_logs ORDER BY timestamp DESC")
    fun getAllTemperatureLogs(): Flow<List<TemperatureLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlertLog(log: AlertLog)

    @Query("SELECT * FROM alert_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllAlertLogs(): Flow<List<AlertLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptimizationLog(log: OptimizationLog)

    @Query("SELECT * FROM optimization_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllOptimizationLogs(): Flow<List<OptimizationLog>>

    @Query("DELETE FROM temperature_logs")
    suspend fun clearAllTemperatureLogs()

    @Query("DELETE FROM alert_logs")
    suspend fun clearAllAlertLogs()

    @Query("DELETE FROM optimization_logs")
    suspend fun clearAllOptimizationLogs()
}
