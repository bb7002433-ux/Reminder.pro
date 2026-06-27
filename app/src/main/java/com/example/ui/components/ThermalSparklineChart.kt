package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.TemperatureLog
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.TextGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ThermalSparklineChart(
    logs: List<TemperatureLog>,
    modifier: Modifier = Modifier,
    chartColor: Color = ElectricCyan
) {
    if (logs.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = "No data",
                    tint = TextGray.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No recorded logs yet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray
                )
                Text(
                    text = "Historical charts will populate dynamically.",
                    fontSize = 11.sp,
                    color = TextGray.copy(alpha = 0.7f)
                )
            }
        }
        return
    }

    // Sort chronologically (oldest to newest for left-to-right drawing)
    val sortedLogs = logs.sortedBy { it.timestamp }.takeLast(25) // show last 25 entries for clarity
    
    val temps = sortedLogs.map { it.cpuTemp }
    val maxTemp = (temps.maxOrNull() ?: 50f).coerceAtLeast(42f) + 2f
    val minTemp = (temps.minOrNull() ?: 20f).coerceAtMost(25f) - 2f
    val tempRange = maxTemp - minTemp

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            val width = size.width
            val height = size.height
            val pointsCount = sortedLogs.size

            if (pointsCount < 2) return@Canvas

            val stepX = width / (pointsCount - 1)
            val coords = sortedLogs.mapIndexed { index, log ->
                val x = index * stepX
                val normalizedY = (log.cpuTemp - minTemp) / tempRange
                val y = height - (normalizedY * height) // Invert Y coordinates
                Offset(x, y)
            }

            // 1. Draw smooth bezier curve line path
            val linePath = Path().apply {
                if (coords.isNotEmpty()) {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) {
                        val prev = coords[i - 1]
                        val curr = coords[i]
                        // Control points for bezier curve smoothing
                        val controlX = (prev.x + curr.x) / 2
                        cubicTo(
                            controlX, prev.y,
                            controlX, curr.y,
                            curr.x, curr.y
                        )
                    }
                }
            }

            // 2. Draw glowing background gradient shade under the curve
            val fillPath = Path().apply {
                addPath(linePath)
                if (coords.isNotEmpty()) {
                    lineTo(coords.last().x, height)
                    lineTo(coords.first().x, height)
                    close()
                }
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        chartColor.copy(alpha = 0.35f),
                        chartColor.copy(alpha = 0.0f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw line stroke
            drawPath(
                path = linePath,
                color = chartColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Draw dot indicators for peaks
            val maxIndex = temps.indexOf(temps.maxOrNull() ?: 0f)
            if (maxIndex in coords.indices) {
                val peakOffset = coords[maxIndex]
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = peakOffset
                )
                drawCircle(
                    color = chartColor,
                    radius = 3.dp.toPx(),
                    center = peakOffset
                )
            }
        }

        // Horizontal axes - Start and End timestamps
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            val startTimeStr = timeFormatter.format(Date(sortedLogs.first().timestamp))
            val endTimeStr = timeFormatter.format(Date(sortedLogs.last().timestamp))
            
            Text(
                text = startTimeStr,
                fontSize = 10.sp,
                color = TextGray,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            
            Text(
                text = "Live Timeline",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Center)
            )

            Text(
                text = endTimeStr,
                fontSize = 10.sp,
                color = TextGray,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}
