package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.intelligence.*
import com.example.ui.theme.*
import kotlin.math.roundToInt

/**
 * Exam Readiness Card showing composite score, component breakdown, and actionable warnings.
 */
@Composable
fun ExamReadinessCard(
    readiness: ExamReadinessResult,
    lastDaysMode: LastDaysExamMode,
    modifier: Modifier = Modifier
) {
    val levelColor = when (readiness.level) {
        ReadinessLevel.EXAM_READY -> Color(0xFF10B981) // Emerald
        ReadinessLevel.STRONG -> Color(0xFF3B82F6) // Blue
        ReadinessLevel.PREPARING -> Color(0xFFF59E0B) // Amber
        ReadinessLevel.WEAK -> Color(0xFFF97316) // Orange
        ReadinessLevel.CRITICAL -> Color(0xFFEF4444) // Red
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_readiness_card"),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title, Mode Chip, Level Badge
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
                            .background(levelColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Exam Readiness",
                            tint = levelColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Exam Readiness",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${lastDaysMode.label} • ${lastDaysMode.focusAreas}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Level Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = levelColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, levelColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(levelColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = readiness.level.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Calculated Readiness",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${readiness.score.roundToInt()}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = levelColor
                        )
                        Text(
                            text = " / 100",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Data Reliability",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${readiness.confidence.roundToInt()}% Verified",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Component Breakdown Bars
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReadinessComponentBar(
                    label = "Syllabus Coverage (15%)",
                    value = readiness.components.syllabusCoverage,
                    color = Color(0xFF6366F1)
                )
                ReadinessComponentBar(
                    label = "Topic Mastery (25%)",
                    value = readiness.components.mastery,
                    color = Color(0xFF10B981)
                )
                ReadinessComponentBar(
                    label = "PYQ Performance (20%)",
                    value = if (readiness.components.pyqPerformance >= 0) readiness.components.pyqPerformance else null,
                    color = Color(0xFF3B82F6),
                    emptyLabel = "No PYQ drills recorded"
                )
                ReadinessComponentBar(
                    label = "Spaced Revision Coverage (15%)",
                    value = readiness.components.revisionCoverage,
                    color = Color(0xFFEC4899)
                )
                ReadinessComponentBar(
                    label = "Mistake Control (10%)",
                    value = readiness.components.mistakeControl,
                    color = Color(0xFFF59E0B)
                )
                ReadinessComponentBar(
                    label = "Mock Test Performance (15%)",
                    value = if (readiness.components.mockPerformance >= 0) readiness.components.mockPerformance else null,
                    color = Color(0xFF8B5CF6),
                    emptyLabel = "No mock tests recorded"
                )
            }

            // Warnings section
            if (readiness.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    readiness.warnings.forEach { warning ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(15.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = warning,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = readiness.disclaimer,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun ReadinessComponentBar(
    label: String,
    value: Double?,
    color: Color,
    emptyLabel: String = "No Data"
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (value != null) "${value.roundToInt()}%" else emptyLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (value != null) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { if (value != null) (value / 100.0).toFloat().coerceIn(0f, 1f) else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Today's Adaptive Plan Card showing prioritized tasks with time budget & completion controls.
 */
@Composable
fun TodaysAdaptivePlanCard(
    plan: TodaysPlanResult,
    selectedBudgetMinutes: Int,
    onBudgetChanged: (Int) -> Unit,
    onActionCompleted: (PlanActionItem) -> Unit,
    onActionClick: (PlanActionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val budgets = listOf(30, 60, 90, 120, 180)

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("todays_adaptive_plan_card"),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title & Total Planned Duration
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Today's Adaptive Plan",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Today's Adaptive Plan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target: ${plan.totalMinutes} / ${plan.availableMinutes} mins scheduled",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${plan.items.size} TASKS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Budget Selector Chips
            Text(
                text = "Available Study Time Today",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                budgets.forEach { b ->
                    val isSelected = selectedBudgetMinutes == b
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onBudgetChanged(b) }
                            .testTag("budget_chip_$b")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${b}m",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (plan.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = plan.notes,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List of Adaptive Plan Actions
            if (plan.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All scheduled priorities completed for today!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    plan.items.forEach { item ->
                        PlanActionItemRow(
                            item = item,
                            onCompleted = { onActionCompleted(item) },
                            onClick = { onActionClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanActionItemRow(
    item: PlanActionItem,
    onCompleted: () -> Unit,
    onClick: () -> Unit
) {
    val (typeColor, typeIcon) = when (item.actionType) {
        PlanActionType.WEAK_TOPIC -> Pair(Color(0xFFEF4444), Icons.Default.PriorityHigh)
        PlanActionType.REVISION -> Pair(Color(0xFFEC4899), Icons.Default.Refresh)
        PlanActionType.PYQ_PRACTICE -> Pair(Color(0xFF3B82F6), Icons.Default.Quiz)
        PlanActionType.MISTAKE_REVIEW -> Pair(Color(0xFFF59E0B), Icons.Default.BugReport)
        PlanActionType.MOCK_TEST -> Pair(Color(0xFF8B5CF6), Icons.Default.Assessment)
        PlanActionType.CONCEPT_REVIEW -> Pair(Color(0xFF10B981), Icons.Default.MenuBook)
        PlanActionType.MAINTENANCE -> Pair(Color(0xFF6B7280), Icons.Default.CheckCircle)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, typeColor.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("plan_action_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(typeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = item.actionType.label,
                        tint = typeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.topicTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = typeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = item.actionType.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = typeColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${item.subjectName} • ${item.reason}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "${item.estimatedMinutes}m",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onCompleted,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete Action",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Adaptive Exam Pace Card showing progress vs target & realistic recovery plans.
 */
@Composable
fun AdaptiveExamPaceCard(
    pace: ExamPaceResult,
    targetExamName: String,
    onEditExamDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (pace.status) {
        PaceStatus.AHEAD -> Color(0xFF10B981)
        PaceStatus.ON_TRACK -> Color(0xFF3B82F6)
        PaceStatus.BEHIND -> Color(0xFFF97316)
        PaceStatus.CRITICAL -> Color(0xFFEF4444)
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("adaptive_exam_pace_card"),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
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
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Exam Pace",
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = targetExamName.ifEmpty { "Target Exam" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = onEditExamDate, modifier = Modifier.size(22.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Exam Date",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = "${pace.daysRemaining} days remaining • Target: ${pace.targetCompletionDateStr}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = pace.status.label.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pace Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Completed vs Expected
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Progress vs Target", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${pace.completedPercentage.roundToInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " / ${pace.expectedPercentage.roundToInt()}% Exp",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 1.dp, start = 2.dp)
                            )
                        }
                    }
                }

                // Daily Velocity
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Velocity (Ch/Day)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", pace.currentDailyPace),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                text = " / ${String.format(java.util.Locale.US, "%.1f", pace.requiredDailyPace)} Req",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 1.dp, start = 2.dp)
                            )
                        }
                    }
                }
            }

            // Recovery Recommendation if behind schedule
            if (pace.recovery.isNeeded) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (pace.recovery.isRealistic) Color(0xFFF97316).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f))
                        .border(
                            1.dp,
                            if (pace.recovery.isRealistic) Color(0xFFF97316).copy(alpha = 0.3f) else Color(0xFFEF4444).copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (pace.recovery.isRealistic) Icons.Default.TrendingUp else Icons.Default.AltRoute,
                            contentDescription = null,
                            tint = if (pace.recovery.isRealistic) Color(0xFFF97316) else Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (pace.recovery.isRealistic) "Adaptive Recovery Plan" else "Strategic Triage Mode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pace.recovery.isRealistic) Color(0xFFF97316) else Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pace.recovery.recommendationText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Focus: ${pace.recovery.strategicFocus}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Sprint 3: Compact Performance Trend & Feedback Card for Dashboard.
 */
@Composable
fun PerformanceTrendDashboardCard(
    trendResult: PerformanceTrendResult?,
    weeklyReport: WeeklyPerformanceReport?,
    recurringMistakes: List<RecurringMistakeGroup>,
    onOpenWeeklyReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (trendResult == null && weeklyReport == null) return

    val trend = trendResult ?: return

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("performance_trend_dashboard_card"),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title & Report button
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Performance Trend",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Performance Trend",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${trend.window.label} Velocity & Feedback",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (weeklyReport != null && weeklyReport.hasSufficientData) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { onOpenWeeklyReport() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Report",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Metric Trend Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrendMetricBox(
                    label = "Mastery",
                    metric = trend.masteryTrend,
                    modifier = Modifier.weight(1f)
                )
                TrendMetricBox(
                    label = "PYQ Accuracy",
                    metric = trend.pyqAccuracyTrend,
                    modifier = Modifier.weight(1f)
                )
                TrendMetricBox(
                    label = "Mock Score",
                    metric = trend.mockScoreTrend,
                    modifier = Modifier.weight(1f)
                )
                TrendMetricBox(
                    label = "Mistakes",
                    metric = trend.mistakesTrend,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Top Improvement & Needs Attention Highlight Strip
            val topImprovement = weeklyReport?.biggestImprovementTopic ?: "Steady Progress"
            val needsAttention = recurringMistakes.firstOrNull()?.topicTitle ?: weeklyReport?.biggestDeclineTopic ?: "None"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("TOP IMPROVEMENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        Text(
                            text = topImprovement,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("NEEDS ATTENTION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        Text(
                            text = needsAttention,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Recurring Mistake Alert Banner (if recurring errors exist)
            val topRecurring = recurringMistakes.firstOrNull { it.level == RecurringMistakeLevel.RECURRING || it.level == RecurringMistakeLevel.REPEATED }
            if (topRecurring != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recurring Error: ${topRecurring.topicTitle} — ${topRecurring.primaryCategory?.label ?: "Concept Gap"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendMetricBox(
    label: String,
    metric: TrendMetric,
    modifier: Modifier = Modifier
) {
    val dirColor = when (metric.direction) {
        TrendDirection.IMPROVING -> Color(0xFF10B981)
        TrendDirection.DECLINING -> Color(0xFFEF4444)
        TrendDirection.STABLE -> Color(0xFF3B82F6)
        TrendDirection.INSUFFICIENT_DATA -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (metric.hasSufficientData) metric.formattedDisplay else "–",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = dirColor
        )
    }
}

/**
 * Weekly Intelligence Report Dialog.
 */
@Composable
fun WeeklyReportDialog(
    report: WeeklyPerformanceReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Weekly Intelligence Report",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Headline Summary
                Text(
                    text = report.headlineSummary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Key Metric Rows
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    WeeklyReportRow("Mastery", report.masteryMetric.before, report.masteryMetric.after, report.masteryMetric.delta)
                    WeeklyReportRow("PYQ Accuracy", report.pyqAccuracyMetric.before, report.pyqAccuracyMetric.after, report.pyqAccuracyMetric.delta, "%")
                    WeeklyReportRow("Mock Score", report.mockScoreMetric.before, report.mockScoreMetric.after, report.mockScoreMetric.delta, "%")
                    WeeklyReportRow("Active Mistakes", report.mistakesMetric.before, report.mistakesMetric.after, report.mistakesMetric.delta, isInverted = true)
                    WeeklyReportRow("Weak Topics", report.weakTopicsMetric.before, report.weakTopicsMetric.after, report.weakTopicsMetric.delta, isInverted = true)
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Effectiveness Rating Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Study Effectiveness:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${report.overallEffectivenessScore.roundToInt()} / 100 • ${report.overallEffectivenessLevel.label}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Actionable Takeaways
                if (report.actionableTakeaways.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Actionable Recommendations:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    for (t in report.actionableTakeaways) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("• ", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(t, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Close Report")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun WeeklyReportRow(
    label: String,
    before: Double,
    after: Double,
    delta: Double,
    unit: String = "",
    isInverted: Boolean = false
) {
    val isPositive = if (isInverted) delta < 0 else delta > 0
    val deltaColor = if (isPositive) Color(0xFF10B981) else if (delta == 0.0) Color(0xFF3B82F6) else Color(0xFFEF4444)
    val prefix = if (delta > 0) "+" else ""

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${String.format(java.util.Locale.US, "%.0f", before)}$unit → ${String.format(java.util.Locale.US, "%.0f", after)}$unit",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$prefix${String.format(java.util.Locale.US, "%.0f", delta)}$unit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = deltaColor
            )
        }
    }
}
