package com.example.ui.components.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BentoCard
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusInProgress
import com.example.ui.theme.StatusWeak
import com.example.ui.viewmodel.OverallStats
import com.example.ui.viewmodel.SubjectStats
import kotlin.math.atan2

@Composable
fun SubjectProgressChart(
    subjectStats: List<SubjectStats>,
    overallStats: OverallStats,
    selectedSubjectIdForDetail: Long?,
    onSelectSubjectForDetail: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Donut & Legend Bento Card
        BentoCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("donut_chart_card"),
            shape = RoundedCornerShape(20.dp),
            accentColor = BrandForestGreen
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Syllabus Distribution & Allocation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Proportional total chapter count per core subject module",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SubjectCompletionDonutChart(
                            subjectStats = subjectStats,
                            selectedSubjectId = selectedSubjectIdForDetail,
                            onSelectSubject = onSelectSubjectForDetail,
                            modifier = Modifier.fillMaxSize()
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val activeSubject = subjectStats.find { it.subject.id == selectedSubjectIdForDetail }
                            Text(
                                text = if (activeSubject != null) "${activeSubject.completionPercentage}%" else "${overallStats.completionPercentage}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (activeSubject != null) activeSubject.subject.code.ifEmpty { "Selected" } else "Total",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjectStats.take(5).forEach { stats ->
                            val col = parseColorSafe(stats.subject.colorHex)
                            val isSelected = selectedSubjectIdForDetail == stats.subject.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) col.copy(alpha = 0.16f) else Color.Transparent)
                                    .clickable {
                                        onSelectSubjectForDetail(if (isSelected) null else stats.subject.id)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stats.subject.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${stats.completionPercentage}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = col
                                )
                            }
                        }
                    }
                }
            }
        }

        // Comparative Subject Progress Matrix Card
        BentoCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("comparative_bars_card"),
            shape = RoundedCornerShape(20.dp),
            accentColor = MaterialTheme.colorScheme.secondary
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Subject Mastery & Coverage Matrix",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Detailed breakdown by completed, in-progress, and weak areas",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                subjectStats.forEach { stats ->
                    val col = parseColorSafe(stats.subject.colorHex)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stats.subject.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${stats.completedChapters}/${stats.totalChapters} done (${stats.completionPercentage}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = col
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Stacked Tri-Color Progress Bar (Completed, In Progress, Left)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            val doneWeight = stats.completedChapters.toFloat().coerceAtLeast(0.01f)
                            val inProgWeight = stats.inProgressChapters.toFloat().coerceAtLeast(0f)
                            val leftWeight = stats.unstartedChapters.toFloat().coerceAtLeast(0f)

                            if (stats.completedChapters > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(doneWeight)
                                        .fillMaxHeight()
                                        .background(StatusCompleted)
                                )
                            }
                            if (stats.inProgressChapters > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(inProgWeight)
                                        .fillMaxHeight()
                                        .background(StatusInProgress)
                                )
                            }
                            if (stats.unstartedChapters > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(leftWeight)
                                        .fillMaxHeight()
                                        .background(StatusWeak.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectCompletionDonutChart(
    subjectStats: List<SubjectStats>,
    selectedSubjectId: Long?,
    onSelectSubject: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedSweep = remember { Animatable(0f) }
    LaunchedEffect(subjectStats) {
        animatedSweep.snapTo(0f)
        animatedSweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val totalChapters = remember(subjectStats) { subjectStats.sumOf { it.totalChapters }.coerceAtLeast(1) }

    Canvas(
        modifier = modifier.pointerInput(subjectStats) {
            detectTapGestures { offset ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val touchAngle = (Math.toDegrees(atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())) + 360.0) % 360.0

                var currentAngle = 270.0
                var found: Long? = null
                for (stats in subjectStats) {
                    val sweep = (stats.totalChapters.toDouble() / totalChapters) * 360.0
                    val start = currentAngle % 360.0
                    val end = (currentAngle + sweep) % 360.0
                    val inside = if (start < end) {
                        touchAngle in start..end
                    } else {
                        touchAngle >= start || touchAngle <= end
                    }
                    if (inside) {
                        found = stats.subject.id
                        break
                    }
                    currentAngle += sweep
                }
                onSelectSubject(if (selectedSubjectId == found) null else found)
            }
        }
    ) {
        val strokeWidth = 16.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f

        subjectStats.forEach { stats ->
            val color = parseColorSafe(stats.subject.colorHex)
            val isSelected = selectedSubjectId == stats.subject.id
            val sliceSweep = ((stats.totalChapters.toFloat() / totalChapters) * 360f) * animatedSweep.value
            drawArc(
                color = if (selectedSubjectId != null && !isSelected) color.copy(alpha = 0.3f) else color,
                startAngle = startAngle,
                sweepAngle = (sliceSweep - 2.5f).coerceAtLeast(1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = if (isSelected) strokeWidth * 1.25f else strokeWidth,
                    cap = StrokeCap.Round
                )
            )
            startAngle += sliceSweep
        }
    }
}

private fun parseColorSafe(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        BrandForestGreen
    }
}
