package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.MetricsRepository
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MetricsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable Edge-to-Edge display support for a premium full-screen experience
        enableEdgeToEdge()

        // Initialize Room Database and system telemetry metrics repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MetricsRepository(database.logDao())

        setContent {
            MyApplicationTheme {
                // Instantiate the central application ViewModel using our modular repository Factory
                val metricsViewModel: MetricsViewModel = viewModel(
                    factory = MetricsViewModel.Factory(repository)
                )

                MainDashboardScreen(
                    viewModel = metricsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
