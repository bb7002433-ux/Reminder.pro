package com.example.ui.screens

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AlertLog
import com.example.data.database.OptimizationLog
import com.example.data.database.TemperatureLog
import com.example.data.model.DeviceMetrics
import com.example.data.model.InstalledAppMetric
import com.example.data.model.StorageMetrics
import com.example.ui.components.ThermalGauge
import com.example.ui.components.ThermalSparklineChart
import com.example.ui.theme.BorderGray
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.TextGray
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MetricsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class DashboardTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    ANALYTICS("Reports", Icons.Default.Assessment),
    APPS("App Monitor", Icons.Default.Apps),
    CLEANER("Storage", Icons.Default.CleaningServices),
    SETTINGS("Thermal Alerts", Icons.Default.Settings)
}

@Composable
fun MainDashboardScreen(
    viewModel: MetricsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(DashboardTab.DASHBOARD) }

    // Start background telemetry loops
    LaunchedEffect(Unit) {
        viewModel.startMonitoring(context)
    }

    // Capture telemetry states
    val deviceMetrics by viewModel.deviceMetrics.collectAsState()
    val storageMetrics by viewModel.storageMetrics.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()
    val optimizationResult by viewModel.optimizationResult.collectAsState()
    val activeAlert by viewModel.activeAlert.collectAsState()

    // Capture settings states
    val tempThreshold by viewModel.tempThreshold.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrateEnabled by viewModel.vibrateEnabled.collectAsState()
    val backgroundMonitoring by viewModel.backgroundMonitoring.collectAsState()

    // Capture database logs
    val recentLogs by viewModel.recentTemperatureLogs.collectAsState()
    val alertLogs by viewModel.alertLogs.collectAsState()
    val optimizationLogs by viewModel.optimizationLogs.collectAsState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isTablet = maxWidth > 600.dp

        if (isTablet) {
            // Responsive layout: Side Navigation Rail for tablets/large displays
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "HitAlert",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "HitAlert",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    DashboardTab.values().forEachIndexed { index, tab ->
                        NavigationRailItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                            label = { Text(text = tab.label, fontSize = 11.sp) },
                            alwaysShowLabel = true,
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.background,
                                selectedTextColor = PrimaryTeal,
                                indicatorColor = PrimaryTeal,
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray
                            ),
                            modifier = Modifier.testTag("nav_item_$index")
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Inner Main content area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                ) {
                    TabContent(
                        tab = selectedTab,
                        deviceMetrics = deviceMetrics,
                        storageMetrics = storageMetrics,
                        installedApps = installedApps,
                        tempThreshold = tempThreshold,
                        soundEnabled = soundEnabled,
                        vibrateEnabled = vibrateEnabled,
                        backgroundMonitoring = backgroundMonitoring,
                        recentLogs = recentLogs,
                        alertLogs = alertLogs,
                        optimizationLogs = optimizationLogs,
                        viewModel = viewModel
                    )
                }
            }
        } else {
            // Responsive layout: Bottom Navigation for typical mobile phones
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        DashboardTab.values().forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                                label = { Text(text = tab.label, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.background,
                                    selectedTextColor = PrimaryTeal,
                                    indicatorColor = PrimaryTeal,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray
                                ),
                                modifier = Modifier.testTag("nav_item_$index")
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                ) {
                    TabContent(
                        tab = selectedTab,
                        deviceMetrics = deviceMetrics,
                        storageMetrics = storageMetrics,
                        installedApps = installedApps,
                        tempThreshold = tempThreshold,
                        soundEnabled = soundEnabled,
                        vibrateEnabled = vibrateEnabled,
                        backgroundMonitoring = backgroundMonitoring,
                        recentLogs = recentLogs,
                        alertLogs = alertLogs,
                        optimizationLogs = optimizationLogs,
                        viewModel = viewModel
                    )
                }
            }
        }

        // --- OVERLAY: Immersive Cooling Progress & Scan Animation ---
        AnimatedVisibility(
            visible = isOptimizing,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable(enabled = false) {}, // Scrim block clickthrough
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "cool_rotation")
                val rotationAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotate"
                )

                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .border(2.dp, PrimaryTeal, CircleShape)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Cooling down",
                            tint = PrimaryTeal,
                            modifier = Modifier
                                .size(90.dp)
                                .rotate(rotationAngle)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "THERMAL OPTIMIZATION IN PROGRESS",
                        color = PrimaryTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = optimizationResult ?: "Initializing security parameters...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LinearProgressIndicator(
                        color = PrimaryTeal,
                        trackColor = BorderGray,
                        modifier = Modifier
                            .width(220.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        // --- OVERLAY: Dynamic Alerts Pop-up Banner ---
        AnimatedVisibility(
            visible = activeAlert != null && !isOptimizing,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DangerCrimson),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp)
                    .shadow(12.dp, RoundedCornerShape(12.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Thermal Warning",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "THERMAL CEILING REACHED",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = activeAlert ?: "",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.dismissAlert() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null,
                        modifier = Modifier.testTag("dismiss_alert_button")
                    ) {
                        Text("Mute", color = DangerCrimson, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // --- OVERLAY: Cooling Complete Toast ---
        var showCompleteToast by remember { mutableStateOf(false) }
        var toastMessage by remember { mutableStateOf("") }
        LaunchedEffect(optimizationResult, isOptimizing) {
            if (!isOptimizing && optimizationResult != null && !optimizationResult!!.contains("...")) {
                toastMessage = optimizationResult!!
                showCompleteToast = true
                delay(5000)
                showCompleteToast = false
                viewModel.clearOptimizationResult()
            }
        }

        AnimatedVisibility(
            visible = showCompleteToast,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .padding(horizontal = 24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(12.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = DarkBackground,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "COOLING OPTIMIZATION SUCCESSFUL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = DarkBackground,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = toastMessage,
                            fontSize = 12.sp,
                            color = DarkBackground.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = { showCompleteToast = false }) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Close",
                            tint = DarkBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabContent(
    tab: DashboardTab,
    deviceMetrics: DeviceMetrics,
    storageMetrics: StorageMetrics,
    installedApps: List<InstalledAppMetric>,
    tempThreshold: Float,
    soundEnabled: Boolean,
    vibrateEnabled: Boolean,
    backgroundMonitoring: Boolean,
    recentLogs: List<TemperatureLog>,
    alertLogs: List<AlertLog>,
    optimizationLogs: List<OptimizationLog>,
    viewModel: MetricsViewModel
) {
    when (tab) {
        DashboardTab.DASHBOARD -> DashboardTabScreen(deviceMetrics, tempThreshold, viewModel)
        DashboardTab.ANALYTICS -> AnalyticsTabScreen(recentLogs, alertLogs, optimizationLogs, viewModel)
        DashboardTab.APPS -> AppsTabScreen(installedApps, viewModel)
        DashboardTab.CLEANER -> StorageTabScreen(storageMetrics, viewModel)
        DashboardTab.SETTINGS -> SettingsTabScreen(
            tempThreshold = tempThreshold,
            soundEnabled = soundEnabled,
            vibrateEnabled = vibrateEnabled,
            backgroundMonitoring = backgroundMonitoring,
            viewModel = viewModel
        )
    }
}

// ==========================================
// 1. CORE DASHBOARD TAB
// ==========================================
@Composable
fun DashboardTabScreen(
    metrics: DeviceMetrics,
    threshold: Float,
    viewModel: MetricsViewModel
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_glow")
    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        item {
            // Header
            Column {
                Text(
                    text = "HIT ALERT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryTeal,
                    letterSpacing = 2.5.sp
                )
                Text(
                    text = "Thermal Control Hub",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Large Center Radial Meter Gauge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ThermalGauge(
                        temperature = metrics.cpuTemp,
                        threshold = threshold
                    )
                }
            }
        }

        // Smart Suggestion Quick Tip Card
        item {
            val suggestionText = when {
                metrics.cpuTemp >= threshold -> "⚠️ Device Overheating! Run cooling optimizer and close background programs immediately."
                metrics.isCharging && metrics.cpuTemp > (threshold - 4f) -> "⚡ High temperature while charging. Consider removing device cover or screen timeout reductions."
                metrics.ramUsage > 75f -> "💾 RAM consumption exceeds 75%. Optimize to clear system cached heap."
                else -> "🛡️ Core processors are safe and shielded. Device health is optimal."
            }

            val tipColor = when {
                metrics.cpuTemp >= threshold -> DangerCrimson
                metrics.cpuTemp >= (threshold - 4f) -> WarningAmber
                else -> PrimaryTeal
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = tipColor.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, tipColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield Guard",
                        tint = tipColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = suggestionText,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Quick cooling pulse optimization button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val buttonColor = if (metrics.cpuTemp >= threshold) DangerCrimson else PrimaryTeal
                Button(
                    onClick = { viewModel.runCoolingOptimization(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 2.dp,
                            color = buttonColor.copy(alpha = borderGlowAlpha),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("one_tap_optimize_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Cool device",
                            tint = DarkBackground,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ONE-TAP COOLING OPTIMIZE",
                            color = DarkBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Live Telemetry Grid cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Battery Temperature card
                TelemetryCard(
                    title = "Battery Core",
                    value = "${String.format("%.1f", metrics.batteryTemp)}°C",
                    indicatorText = "Health: ${metrics.batteryHealth}",
                    icon = Icons.Default.Bolt,
                    color = if (metrics.batteryTemp >= (threshold - 2f)) DangerCrimson else PrimaryTeal,
                    modifier = Modifier.weight(1f)
                )

                // CPU Usage Percentage card
                TelemetryCard(
                    title = "CPU Processing",
                    value = "${metrics.cpuUsage.roundToInt()}%",
                    indicatorText = "Load State",
                    icon = Icons.Default.Speed,
                    color = if (metrics.cpuUsage > 75f) WarningAmber else ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // RAM usage card
                TelemetryCard(
                    title = "System RAM",
                    value = "${metrics.ramUsage.roundToInt()}%",
                    indicatorText = "${String.format("%.1f", metrics.availableRamGb)}GB Free of ${String.format("%.1f", metrics.totalRamGb)}GB",
                    icon = Icons.Default.Memory,
                    color = if (metrics.ramUsage > 80f) DangerCrimson else ElectricCyan,
                    modifier = Modifier.weight(1f)
                )

                // Charger / Voltage Card
                TelemetryCard(
                    title = "Power Status",
                    value = if (metrics.isCharging) "Charging" else "Unplugged",
                    indicatorText = "${metrics.voltageMv / 1000f} V (${metrics.chargeType})",
                    icon = Icons.Default.Bolt,
                    color = if (metrics.isCharging) WarningAmber else PrimaryTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Privacy Commitment Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Privacy Shield",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Privacy commitment: All telemetry and system data remains strictly local, offline, and private in your database.",
                        fontSize = 11.sp,
                        color = TextGray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    indicatorText: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = indicatorText,
                fontSize = 11.sp,
                color = TextGray
            )
        }
    }
}

// ==========================================
// 2. ANALYTICS & HISTORICAL REPORTS TAB
// ==========================================
@Composable
fun AnalyticsTabScreen(
    recentLogs: List<TemperatureLog>,
    alertLogs: List<AlertLog>,
    optimizationLogs: List<OptimizationLog>,
    viewModel: MetricsViewModel
) {
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    // Calculate aggregated reports data dynamically from database entries
    val avgCpuTemp = if (recentLogs.isNotEmpty()) recentLogs.map { it.cpuTemp }.average().toFloat() else 0f
    val peakCpuTemp = if (recentLogs.isNotEmpty()) recentLogs.maxOf { it.cpuTemp } else 0f
    val alertCount = alertLogs.size
    val lastOptimized = if (optimizationLogs.isNotEmpty()) {
        dateFormatter.format(Date(optimizationLogs.first().timestamp))
    } else {
        "Never"
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ANALYTICS REPORT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryTeal,
                        letterSpacing = 2.5.sp
                    )
                    Text(
                        text = "Thermal Metrics History",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Clear history action
                IconButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Clear analytics history",
                        tint = DangerCrimson
                    )
                }
            }
        }

        // Live Sparkline Chart Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROCESSOR THERMAL TRENDS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(PrimaryTeal.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "REAL-TIME",
                                fontSize = 9.sp,
                                color = PrimaryTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ThermalSparklineChart(
                        logs = recentLogs,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Performance Reports Summary Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PERFORMANCE OVERVIEW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ReportStatItem(
                            label = "Average Temp",
                            value = if (avgCpuTemp > 0) "${String.format("%.1f", avgCpuTemp)}°C" else "N/A",
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatItem(
                            label = "Peak Core Temp",
                            value = if (peakCpuTemp > 0) "${String.format("%.1f", peakCpuTemp)}°C" else "N/A",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ReportStatItem(
                            label = "Overheat Alerts Triggered",
                            value = "$alertCount Logs",
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatItem(
                            label = "Cooled / Optimized",
                            value = if (lastOptimized == "Never") "Never" else lastOptimized.substringAfter(" "),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Overheat log lists
        item {
            Text(
                text = "ALERTS HISTORY LOG (${alertCount})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (alertLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No overheating incidents recorded. Excellent thermal state!",
                        fontSize = 13.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(alertLogs.take(10)) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DangerCrimson.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert log",
                            tint = DangerCrimson,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = log.message,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateFormatter.format(Date(log.timestamp)),
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ==========================================
// 3. APP RESOURCE MONITOR TAB
// ==========================================
@Composable
fun AppsTabScreen(
    installedApps: List<InstalledAppMetric>,
    viewModel: MetricsViewModel
) {
    val context = LocalContext.current
    var filterSystem by remember { mutableStateOf(false) }
    val filteredApps = remember(installedApps, filterSystem) {
        if (filterSystem) installedApps else installedApps.filter { !it.isSystemApp }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "APP MONITORING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryTeal,
                    letterSpacing = 2.5.sp
                )
                Text(
                    text = "Resource Contribution",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Toggle System Apps filter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Include System Services",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Display standard built-in operating processes",
                            fontSize = 10.sp,
                            color = TextGray
                        )
                    }
                }

                Switch(
                    checked = filterSystem,
                    onCheckedChange = { filterSystem = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElectricCyan,
                        checkedTrackColor = ElectricCyan.copy(alpha = 0.4f),
                        uncheckedThumbColor = TextGray,
                        uncheckedTrackColor = BorderGray
                    ),
                    modifier = Modifier.testTag("system_apps_toggle")
                )
            }
        }

        if (filteredApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading background processes...",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }
            }
        } else {
            items(filteredApps) { app ->
                AppResourceCard(
                    app = app,
                    onForceCool = {
                        // Triggers optimization conceptually on single app
                        viewModel.runCoolingOptimization(context)
                    }
                )
            }
        }
    }
}

@Composable
fun AppResourceCard(
    app: InstalledAppMetric,
    onForceCool: () -> Unit
) {
    val impactColor = when {
        app.cpuImpact > 18f || app.batteryImpact > 6 -> DangerCrimson
        app.cpuImpact > 8f || app.batteryImpact > 3 -> WarningAmber
        else -> PrimaryTeal
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular icon placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(impactColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.name.take(1).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = impactColor,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        color = TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Cool down action button
                Button(
                    onClick = onForceCool,
                    colors = ButtonDefaults.buttonColors(containerColor = impactColor.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = null,
                    modifier = Modifier
                        .height(32.dp)
                        .border(1.dp, impactColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .testTag("cool_app_button_${app.packageName}")
                ) {
                    Text(
                        text = "Freeze",
                        color = impactColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resource Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // CPU load bar
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "CPU Load", fontSize = 10.sp, color = TextGray)
                        Text(text = "${String.format("%.1f", app.cpuImpact)}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (app.cpuImpact / 100f).coerceIn(0f, 1f) },
                        color = impactColor,
                        trackColor = BorderGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }

                // RAM footprint
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Memory Usage", fontSize = 10.sp, color = TextGray)
                        Text(text = "${app.ramImpactMb.roundToInt()} MB", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (app.ramImpactMb / 512f).coerceIn(0f, 1f) },
                        color = ElectricCyan,
                        trackColor = BorderGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. STORAGE CLEANER & ANALYZER TAB
// ==========================================
@Composable
fun StorageTabScreen(
    storage: StorageMetrics,
    viewModel: MetricsViewModel
) {
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "STORAGE CLEANER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryTeal,
                    letterSpacing = 2.5.sp
                )
                Text(
                    text = "Clean & Optimization",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Storage Circle Meter Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "DEVICE DISK HEALTH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "${storage.usagePercent.roundToInt()}% USED",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (storage.usagePercent / 100f).coerceIn(0f, 1f) },
                        color = if (storage.usagePercent > 85f) DangerCrimson else ElectricCyan,
                        trackColor = BorderGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Used: ${String.format("%.1f", storage.usedSpaceGb)} GB",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                        Text(
                            text = "Total Space: ${String.format("%.1f", storage.totalSpaceGb)} GB",
                            fontSize = 12.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Breakdown card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CLEARABLE JUNK DETECTED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    JunkBreakdownRow(
                        label = "Temporary Caches",
                        value = "${storage.cacheSizeMb.roundToInt()} MB",
                        color = ElectricCyan
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

                    JunkBreakdownRow(
                        label = "System Temporary Logs",
                        value = "${storage.tempFilesSizeMb.roundToInt()} MB",
                        color = WarningAmber
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

                    JunkBreakdownRow(
                        label = "App Error Dumps",
                        value = "${storage.logsSizeMb.roundToInt()} MB",
                        color = DangerCrimson
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.runCoolingOptimization(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("clean_storage_button")
                    ) {
                        Text(
                            text = "CLEAN STORAGE JUNK (${storage.totalClearableMb.roundToInt()} MB)",
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JunkBreakdownRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==========================================
// 5. THERMAL ALERTS & SETTINGS TAB
// ==========================================
@Composable
fun SettingsTabScreen(
    tempThreshold: Float,
    soundEnabled: Boolean,
    vibrateEnabled: Boolean,
    backgroundMonitoring: Boolean,
    viewModel: MetricsViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "THERMAL SETTINGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryTeal,
                    letterSpacing = 2.5.sp
                )
                Text(
                    text = "Smart Overheat Alarms",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Custom Threshold Slider Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ALERT THRESHOLD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${tempThreshold.toInt()}°C",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryTeal
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = tempThreshold,
                        onValueChange = { viewModel.updateThreshold(it) },
                        valueRange = 38f..52f,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryTeal,
                            activeTrackColor = PrimaryTeal,
                            inactiveTrackColor = BorderGray
                        ),
                        modifier = Modifier.testTag("temp_threshold_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Trigger real-time notifications and system safety alerts when either the central CPU processor or physical battery exceeds this ceiling.",
                        fontSize = 11.sp,
                        color = TextGray,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Alarm Toggles
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ALERT ANNOUNCEMENT CHANNELS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Sound switch
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Audio Alarm Sounds",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Play default ringtone when overheating",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { viewModel.setSoundEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryTeal,
                                checkedTrackColor = PrimaryTeal.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("sound_alert_toggle")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 16.dp))

                    // Vibration switch
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Haptic Vibration Warning",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Vibrate device using critical warning beats",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                        Switch(
                            checked = vibrateEnabled,
                            onCheckedChange = { viewModel.setVibrateEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryTeal,
                                checkedTrackColor = PrimaryTeal.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("vibrate_alert_toggle")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 16.dp))

                    // Background service toggle
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Continuous Background Guard",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Perform micro-polling while running offline",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                        Switch(
                            checked = backgroundMonitoring,
                            onCheckedChange = { viewModel.setBackgroundMonitoring(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryTeal,
                                checkedTrackColor = PrimaryTeal.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("background_monitoring_toggle")
                        )
                    }
                }
            }
        }
    }
}
