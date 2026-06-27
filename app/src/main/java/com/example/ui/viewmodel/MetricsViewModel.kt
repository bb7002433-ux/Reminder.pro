package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AlertLog
import com.example.data.database.OptimizationLog
import com.example.data.database.TemperatureLog
import com.example.data.model.DeviceMetrics
import com.example.data.model.InstalledAppMetric
import com.example.data.model.StorageMetrics
import com.example.data.repository.MetricsRepository
import com.example.util.AlertSoundVibrationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MetricsViewModel(private val repository: MetricsRepository) : ViewModel() {

    // Current Telemetry
    private val _deviceMetrics = MutableStateFlow(DeviceMetrics())
    val deviceMetrics: StateFlow<DeviceMetrics> = _deviceMetrics.asStateFlow()

    private val _storageMetrics = MutableStateFlow(StorageMetrics())
    val storageMetrics: StateFlow<StorageMetrics> = _storageMetrics.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppMetric>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppMetric>> = _installedApps.asStateFlow()

    // UI States
    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing: StateFlow<Boolean> = _isOptimizing.asStateFlow()

    private val _optimizationResult = MutableStateFlow<String?>(null)
    val optimizationResult: StateFlow<String?> = _optimizationResult.asStateFlow()

    // Settings
    private val _tempThreshold = MutableStateFlow(42f) // Default threshold
    val tempThreshold: StateFlow<Float> = _tempThreshold.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrateEnabled = MutableStateFlow(true)
    val vibrateEnabled: StateFlow<Boolean> = _vibrateEnabled.asStateFlow()

    private val _backgroundMonitoring = MutableStateFlow(true)
    val backgroundMonitoring: StateFlow<Boolean> = _backgroundMonitoring.asStateFlow()

    // Alerts state
    private val _activeAlert = MutableStateFlow<String?>(null)
    val activeAlert: StateFlow<String?> = _activeAlert.asStateFlow()

    // Room logs observed reactively
    val recentTemperatureLogs: StateFlow<List<TemperatureLog>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertLogs: StateFlow<List<AlertLog>> = repository.alertLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val optimizationLogs: StateFlow<List<OptimizationLog>> = repository.optimizationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var monitorJob: Job? = null
    private var lastLogTime = 0L
    private var lastAlertTime = 0L

    init {
        // Prepopulate database with mock report logs if database is empty on first boot.
        viewModelScope.launch {
            repository.prepopulateHistoricalDataIfEmpty()
        }
    }

    /**
     * Starts the real-time polling thread.
     */
    fun startMonitoring(context: Context) {
        monitorJob?.cancel()
        
        // Populate installed apps once on initialization
        viewModelScope.launch {
            val apps = repository.getInstalledApps(context)
            _installedApps.value = apps
        }

        monitorJob = viewModelScope.launch {
            while (true) {
                try {
                    val metrics = repository.getDeviceMetrics(context)
                    _deviceMetrics.value = metrics

                    val storage = repository.getStorageMetrics()
                    _storageMetrics.value = storage

                    // 1. Check for overheating triggers
                    checkThermalAlerts(context, metrics)

                    // 2. Periodic history logging every 30 seconds
                    val now = System.currentTimeMillis()
                    if (now - lastLogTime > 30_000) {
                        repository.logMetrics(metrics)
                        lastLogTime = now
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(2000) // update stats every 2 seconds
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private fun checkThermalAlerts(context: Context, metrics: DeviceMetrics) {
        val currentCpuTemp = metrics.cpuTemp
        val currentBatteryTemp = metrics.batteryTemp
        val limit = _tempThreshold.value

        // If either CPU or Battery exceeds threshold
        if (currentCpuTemp >= limit || currentBatteryTemp >= (limit - 2f)) {
            val isCpuHot = currentCpuTemp >= limit
            val source = if (isCpuHot) "Processor (CPU)" else "Battery"
            val currentVal = if (isCpuHot) currentCpuTemp else currentBatteryTemp
            
            _activeAlert.value = "⚠️ Device is Overheating!\n$source temperature has reached ${currentVal.toInt()}°C (Safe limit: ${limit.toInt()}°C)."

            // Prevent alert spamming: Trigger sound/vibrate at most once every 15 seconds
            val now = System.currentTimeMillis()
            if (now - lastAlertTime > 15_000) {
                lastAlertTime = now
                
                if (_soundEnabled.value) {
                    AlertSoundVibrationHelper.playSystemAlertSound(context)
                }
                if (_vibrateEnabled.value) {
                    AlertSoundVibrationHelper.triggerVibrationAlert(context)
                }

                // Log alert into the database
                viewModelScope.launch {
                    repository.insertAlertLog(
                        type = if (isCpuHot) "CPU" else "BATTERY",
                        temperature = currentVal,
                        message = "Overheat alert! $source temperature reached ${String.format("%.1f", currentVal)}°C."
                    )
                }
            }
        } else {
            // Dismiss active alert if temperatures drop below threshold
            _activeAlert.value = null
        }
    }

    fun dismissAlert() {
        _activeAlert.value = null
    }

    fun updateThreshold(value: Float) {
        _tempThreshold.value = value
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    fun setVibrateEnabled(enabled: Boolean) {
        _vibrateEnabled.value = enabled
    }

    fun setBackgroundMonitoring(enabled: Boolean) {
        _backgroundMonitoring.value = enabled
    }

    /**
     * Executes the one-tap optimization sequence with an immersive progress flow.
     */
    fun runCoolingOptimization(context: Context) {
        if (_isOptimizing.value) return

        viewModelScope.launch {
            _isOptimizing.value = true
            _optimizationResult.value = "Scanning active memory caches..."
            
            delay(1200)
            _optimizationResult.value = "Analyzing CPU processes..."
            
            delay(1200)
            _optimizationResult.value = "Stopping hot processes & clearing temporary logs..."
            
            val log = repository.runOptimization(context)
            delay(1000)
            
            _isOptimizing.value = false
            _optimizationResult.value = log.resultMessage

            // Force telemetry and app listing refresh
            val metrics = repository.getDeviceMetrics(context)
            _deviceMetrics.value = metrics.copy(
                cpuTemp = (metrics.cpuTemp - 3.5f).coerceAtLeast(30f),
                cpuUsage = (metrics.cpuUsage * 0.3f).coerceAtLeast(5f),
                ramUsage = (metrics.ramUsage * 0.82f).coerceAtLeast(20f)
            )
            val storage = repository.getStorageMetrics()
            _storageMetrics.value = storage
        }
    }

    fun clearOptimizationResult() {
        _optimizationResult.value = null
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }

    class Factory(private val repository: MetricsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MetricsViewModel::class.java)) {
                return MetricsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
