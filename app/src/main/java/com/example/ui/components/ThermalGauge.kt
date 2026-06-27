package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.WarningAmber
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ThermalGauge(
    temperature: Float,
    threshold: Float,
    modifier: Modifier = Modifier
) {
    // Coerce values to a safe range for visual meter scaling (15 to 60 degrees)
    val minTemp = 15f
    val maxTemp = 60f
    val sweepAngleBase = 270f
    val startAngle = 135f

    val fraction = ((temperature - minTemp) / (maxTemp - minTemp)).coerceIn(0f, 100f)
    val targetSweepAngle = sweepAngleBase * fraction

    // Animate transition on temperature changes for a premium, liquid look
    val animatedSweepAngle = remember { Animatable(0f) }
    LaunchedEffect(targetSweepAngle) {
        animatedSweepAngle.animateTo(
            targetValue = targetSweepAngle,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )
    }

    // Determine color state based on temp
    val gaugeColor = when {
        temperature >= threshold -> DangerCrimson
        temperature >= (threshold - 3f) -> WarningAmber
        else -> PrimaryTeal
    }

    // Overheat warning scale animation (pulse when hot)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_warning")
    val warningPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (temperature >= threshold) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val strokeWidth = 22.dp.toPx()
            val radius = (width / 2) - strokeWidth
            val center = Offset(width / 2, height / 2)

            // 1. Background Arc (Unfilled track)
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = startAngle,
                sweepAngle = sweepAngleBase,
                useCenter = false,
                topLeft = Offset(strokeWidth, strokeWidth),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth - 6f, cap = StrokeCap.Round)
            )

            // 2. Glowing Gradient Track (Fills based on temperature)
            val brush = Brush.sweepGradient(
                colors = listOf(
                    PrimaryTeal.copy(alpha = 0.5f),
                    WarningAmber.copy(alpha = 0.8f),
                    DangerCrimson,
                    PrimaryTeal.copy(alpha = 0.5f) // Wrap around gracefully
                ),
                center = center
            )

            drawArc(
                brush = brush,
                startAngle = startAngle,
                sweepAngle = animatedSweepAngle.value,
                useCenter = false,
                topLeft = Offset(strokeWidth, strokeWidth),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Inner accent neon glowing line
            drawArc(
                color = gaugeColor.copy(alpha = 0.8f),
                startAngle = startAngle,
                sweepAngle = animatedSweepAngle.value,
                useCenter = false,
                topLeft = Offset(strokeWidth + 8f, strokeWidth + 8f),
                size = Size((radius - 8f) * 2, (radius - 8f) * 2),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Little needle indicator dot at the tip of the sweep
            val angleRad = Math.toRadians((startAngle + animatedSweepAngle.value).toDouble())
            val needleX = center.x + radius * cos(angleRad).toFloat()
            val needleY = center.y + radius * sin(angleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(needleX, needleY)
            )
            drawCircle(
                color = gaugeColor,
                radius = 4.dp.toPx(),
                center = Offset(needleX, needleY)
            )
        }

        // Inside Info display text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = "Temperature",
                tint = gaugeColor,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))

            // Large digital temperature readings
            Text(
                text = "${String.format("%.1f", temperature)}°C",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.SansSerif
            )

            val statusText = when {
                temperature >= threshold -> "OVERHEATING"
                temperature >= (threshold - 3f) -> "WARNING"
                else -> "COOL & SAFE"
            }

            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = gaugeColor,
                letterSpacing = 1.5.sp
            )
        }
    }
}
