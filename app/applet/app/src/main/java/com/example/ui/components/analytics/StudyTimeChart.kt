package com.example.ui.components.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BentoCard
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.BrandTerracotta
import kotlin.math.abs

enum class TrendMetric {
    STUDY_HOURS,
    SYLLABUS_PROGRESS
}

enum class TimeRange(val label: String, val days: Int) {
    LAST_7_DAYS("7 Days", 7),
    LAST_14_DAYS("14 Days", 14),
    LAST_30_DAYS("30 Days", 30)
}

data class TrendDataPoint(
    val dateLabel: String,
    val fullDate: String,
    val value: Float,
    val unit: String
)

@Composable
fun StudyTimeChart(
    trendDataPoints: List<TrendDataPoint>,
    selectedMetric: TrendMetric,
    onMetricChange: (TrendMetric) -> Unit,
    selectedTimeRange: TimeRange,
    onTimeRangeChange: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_trends_card"),
        shape = RoundedCornerShape(20.dp),
        accentColor = BrandForestGreen
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title & Range Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = BrandForestGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Study Velocity & Trajectory",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TimeRange.values().forEach { range ->
                        val isSelected = selectedTimeRange == range
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) BrandForestGreen
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { onTimeRangeChange(range) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = range.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle Metric Tabs (Study Hours vs Coverage %)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedMetric == TrendMetric.STUDY_HOURS) BrandForestGreen else Color.Transparent)
                        .clickable { onMetricChange(TrendMetric.STUDY_HOURS) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏱️ Daily Hours Spent",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedMetric == TrendMetric.STUDY_HOURS) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedMetric == TrendMetric.SYLLABUS_PROGRESS) BrandForestGreen else Color.Transparent)
                        .clickable { onMetricChange(TrendMetric.SYLLABUS_PROGRESS) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📈 Cumulative Syllabus %",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedMetric == TrendMetric.SYLLABUS_PROGRESS) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Graph Display
            StudyCanvasGraph(
                dataPoints = trendDataPoints,
                lineColor = if (selectedMetric == TrendMetric.STUDY_HOURS) BrandForestGreen else BrandTerracotta,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }
    }
}

@Composable
fun StudyCanvasGraph(
    dataPoints: List<TrendDataPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints, lineColor) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    val maxValue = remember(dataPoints) {
        (dataPoints.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f) * 1.15f
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(dataPoints) {
                    detectTapGestures { offset ->
                        val chartWidth = size.width
                        val pointSpacing = if (dataPoints.size > 1) chartWidth / (dataPoints.size - 1) else chartWidth
                        val closestIdx = dataPoints.indices.minByOrNull { i ->
                            abs(i * pointSpacing - offset.x)
                        }
                        selectedPointIndex = if (selectedPointIndex == closestIdx) null else closestIdx
                    }
                }
        ) {
            val chartWidth = size.width
            val chartHeight = size.height - 24.dp.toPx()
            val pointSpacing = if (dataPoints.size > 1) chartWidth / (dataPoints.size - 1) else chartWidth

            val coords = dataPoints.mapIndexed { index, dp ->
                val x = index * pointSpacing
                val normalizedY = (dp.value / maxValue).coerceIn(0f, 1f)
                val y = chartHeight - (normalizedY * chartHeight * animatedProgress.value)
                Offset(x, y)
            }

            if (coords.size >= 2) {
                val path = Path()
                val fillPath = Path()

                path.moveTo(coords[0].x, coords[0].y)
                fillPath.moveTo(coords[0].x, chartHeight)
                fillPath.lineTo(coords[0].x, coords[0].y)

                for (i in 0 until coords.size - 1) {
                    val p1 = coords[i]
                    val p2 = coords[i + 1]
                    val controlX = (p1.x + p2.x) / 2f
                    path.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                    fillPath.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                }

                fillPath.lineTo(coords.last().x, chartHeight)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.35f * animatedProgress.value),
                            lineColor.copy(alpha = 0.02f)
                        ),
                        startY = 0f,
                        endY = chartHeight
                    )
                )

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                coords.forEachIndexed { index, offset ->
                    val isSelected = selectedPointIndex == index
                    drawCircle(
                        color = surfaceColor,
                        radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = if (isSelected) BrandTerracotta else lineColor,
                        radius = if (isSelected) 5.dp.toPx() else 2.5.dp.toPx(),
                        center = offset
                    )
                }
            }

            val labelInterval = if (dataPoints.size > 14) 5 else if (dataPoints.size > 7) 2 else 1
            dataPoints.forEachIndexed { index, dp ->
                if (index % labelInterval == 0 || index == dataPoints.lastIndex) {
                    val x = index * pointSpacing
                    val textLayout = textMeasurer.measure(
                        text = dp.dateLabel,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 9.sp,
                            color = onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            x = (x - (textLayout.size.width / 2f)).coerceIn(0f, chartWidth - textLayout.size.width),
                            y = chartHeight + 8.dp.toPx()
                        )
                    )
                }
            }
        }

        selectedPointIndex?.let { idx ->
            val point = dataPoints.getOrNull(idx)
            if (point != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-8).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.inverseSurface)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${point.fullDate}: ${String.format("%.1f", point.value)} ${point.unit}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}
