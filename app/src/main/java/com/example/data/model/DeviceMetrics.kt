package com.example.data.model

data class DeviceMetrics(
    val batteryTemp: Float = 0f,
    val cpuTemp: Float = 0f,
    val cpuUsage: Float = 0f,
    val ramUsage: Float = 0f,
    val totalRamGb: Float = 0f,
    val availableRamGb: Float = 0f,
    val batteryHealth: String = "Good",
    val isCharging: Boolean = false,
    val batteryLevel: Int = 0,
    val voltageMv: Int = 0,
    val chargeType: String = "Unknown",
    val isOverheating: Boolean = false
)

data class StorageMetrics(
    val totalSpaceGb: Float = 0f,
    val usedSpaceGb: Float = 0f,
    val freeSpaceGb: Float = 0f,
    val usagePercent: Float = 0f,
    val cacheSizeMb: Float = 0f,
    val tempFilesSizeMb: Float = 0f,
    val logsSizeMb: Float = 0f,
    val totalClearableMb: Float = 0f
)

data class InstalledAppMetric(
    val name: String,
    val packageName: String,
    val cpuImpact: Float, // 0.0 - 100.0%
    val ramImpactMb: Float, // MBs
    val batteryImpact: Float, // 1-10
    val isSystemApp: Boolean
)
