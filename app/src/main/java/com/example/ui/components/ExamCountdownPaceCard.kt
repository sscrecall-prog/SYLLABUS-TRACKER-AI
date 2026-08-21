package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExamPaceStats
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExamCountdownPaceCard(
    paceStats: ExamPaceStats,
    onEditExam: () -> Unit,
    onOpenAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPaceCalculatorDialog by remember { mutableStateOf(false) }

    GradientCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_countdown_pace_card"),
        shape = RoundedCornerShape(22.dp),
        colors = listOf(
            Color(0xFF1E293B),
            Color(0xFF0F172A)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Exam Title, Edit Action, Days Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandTerracotta.copy(alpha = 0.2f))
                            .border(1.dp, BrandTerracotta.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎯", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = paceStats.examName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandWarmCream,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onEditExam,
                                modifier = Modifier.size(24.dp).testTag("edit_exam_target_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = "Edit Target Date",
                                    tint = BrandCreamDark,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Text(
                            text = "${paceStats.examShift} • Target: ${paceStats.examDateStr}",
                            fontSize = 11.sp,
                            color = BrandCreamDark
                        )
                    }
                }

                // Giant Countdown Days Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(BrandTerracotta, Color(0xFFC84B31))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("exam_days_remaining_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${paceStats.daysRemaining}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = "DAYS LEFT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pace & Velocity Metrics Matrix
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric 1: Required Daily Pace
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Required Pace", fontSize = 10.sp, color = BrandCreamDark)
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", paceStats.requiredPaceChaptersPerDay)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandWarmCream
                    )
                    Text("chapters / day", fontSize = 9.sp, color = BrandCreamDark.copy(alpha = 0.8f))
                }

                VerticalDivider(modifier = Modifier.height(24.dp), color = Color.White.copy(alpha = 0.15f))

                // Metric 2: Remaining Chapters
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Syllabus Left", fontSize = 10.sp, color = BrandCreamDark)
                    Text(
                        text = "${paceStats.remainingChapters}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (paceStats.remainingChapters > 20) StatusWeak else StatusCompleted
                    )
                    Text("of ${paceStats.totalChapters} chapters", fontSize = 9.sp, color = BrandCreamDark.copy(alpha = 0.8f))
                }

                VerticalDivider(modifier = Modifier.height(24.dp), color = Color.White.copy(alpha = 0.15f))

                // Metric 3: Target Hours / Day
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Study Needed", fontSize = 10.sp, color = BrandCreamDark)
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", paceStats.requiredPaceHoursPerDay)}h",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D)
                    )
                    Text("per day", fontSize = 9.sp, color = BrandCreamDark.copy(alpha = 0.8f))
                }

                VerticalDivider(modifier = Modifier.height(24.dp), color = Color.White.copy(alpha = 0.15f))

                // Metric 4: Pacing Status
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Status", fontSize = 10.sp, color = BrandCreamDark)
                    val statusText = if (paceStats.isAheadOfSchedule) "On Track ✨" else "Speed Up ⚡"
                    val statusColor = if (paceStats.isAheadOfSchedule) StatusCompleted else BrandTerracotta
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text("Est: ${paceStats.estimatedCompletionDateStr.take(6)}", fontSize = 9.sp, color = BrandCreamDark.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Open Pace Calculator & Detailed Forecast
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showPaceCalculatorDialog = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("open_pace_calculator_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = BrandTerracotta,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Interactive Pace Calculator",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandWarmCream
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { onOpenAnalytics() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("view_velocity_analytics_btn")
                ) {
                    Text(
                        text = "Forecast →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandWarmCream
                    )
                }
            }
        }
    }

    // Interactive Daily Study Pace Calculator Modal Dialog
    if (showPaceCalculatorDialog) {
        InteractivePaceCalculatorModal(
            paceStats = paceStats,
            onDismiss = { showPaceCalculatorDialog = false }
        )
    }
}

@Composable
fun InteractivePaceCalculatorModal(
    paceStats: ExamPaceStats,
    onDismiss: () -> Unit
) {
    var customTargetDays by remember { mutableFloatStateOf(paceStats.daysRemaining.toFloat().coerceIn(7f, 180f)) }
    var customHoursPerDay by remember { mutableFloatStateOf(4f) }
    var customDailyMinsPerChapter by remember { mutableFloatStateOf(45f) }

    val calculatedRequiredChaptersPerDay = remember(customTargetDays, paceStats.remainingChapters) {
        if (customTargetDays > 0) paceStats.remainingChapters / customTargetDays else 0f
    }
    val calculatedRequiredChaptersPerWeek = calculatedRequiredChaptersPerDay * 7f
    val calculatedDailyStudyHours = (calculatedRequiredChaptersPerDay * (customDailyMinsPerChapter / 60f)).coerceAtLeast(0.5f)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pace_calculator_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daily Study Pace Calculator",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Simulate different timeframes & chapter durations to hit your target before ${paceStats.examName}.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Calculated Output Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Required Target Pace",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", calculatedRequiredChaptersPerDay)} Chapters / Day",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Daily Focus Time",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", calculatedDailyStudyHours)} Hours / Day",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandTerracotta
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Weekly Target: ~${String.format(Locale.getDefault(), "%.0f", calculatedRequiredChaptersPerWeek)} chapters",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${paceStats.remainingChapters} chapters left to cover",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Slider 1: Days to Finish
                Text(
                    text = "Days Available to Finish: ${customTargetDays.toInt()} Days",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = customTargetDays,
                    onValueChange = { customTargetDays = it },
                    valueRange = 7f..180f,
                    steps = 24,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Slider 2: Average Minutes per Chapter
                Text(
                    text = "Average Study Time per Chapter: ${customDailyMinsPerChapter.toInt()} mins",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = customDailyMinsPerChapter,
                    onValueChange = { customDailyMinsPerChapter = it },
                    valueRange = 20f..120f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandTerracotta,
                        activeTrackColor = BrandTerracotta
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subject-wise target breakdown
                if (paceStats.subjectPaceBreakdown.isNotEmpty()) {
                    Text(
                        text = "Subject-Wise Weekly Targets (${customTargetDays.toInt()} days schedule):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val calculatedWeeks = (customTargetDays / 7f).coerceAtLeast(1f)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        paceStats.subjectPaceBreakdown.take(4).forEach { sub ->
                            val subWeekly = (sub.remainingChapters / calculatedWeeks).coerceAtLeast(0.2f)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sub.subjectName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", subWeekly)} chapters/wk (${sub.remainingChapters} left)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Strategy")
                }
            }
        }
    }
}
