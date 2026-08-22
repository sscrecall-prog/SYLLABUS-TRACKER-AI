package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudySession
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * High-fidelity Daily Study Goal Card
 * - Interactive input for daily target study hours
 * - Circular progress indicator with gradient fill & percentage
 * - Live study tracking from today's logged sessions
 * - Quick hour presets and customization modal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyStudyGoalCard(
    dailyTargetMinutes: Int,
    studySessions: List<StudySession>,
    onUpdateDailyTargetHours: (Float) -> Unit,
    onStartTimer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val colorScheme = MaterialTheme.colorScheme

    var showEditGoalDialog by remember { mutableStateOf(false) }

    // 1. Calculate today's study minutes from study sessions
    val calendar = Calendar.getInstance()
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

    val todayStudyData = remember(studySessions, todayDateStr) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todaySessions = studySessions.filter {
            dateFormat.format(Date(it.timestamp)) == todayDateStr
        }
        val totalSeconds = todaySessions.sumOf { it.durationSeconds }
        val sessionCount = todaySessions.size
        Pair((totalSeconds / 60).toInt(), sessionCount)
    }

    val completedMinutes = todayStudyData.first
    val completedSessionsCount = todayStudyData.second

    val targetHours = (dailyTargetMinutes / 60f).coerceAtLeast(0.25f)
    val completedHours = completedMinutes / 60f
    val progressFraction = (completedMinutes.toFloat() / dailyTargetMinutes.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val progressPercentage = ((completedMinutes.toFloat() / dailyTargetMinutes.coerceAtLeast(1).toFloat()) * 100).toInt()

    val remainingMinutes = (dailyTargetMinutes - completedMinutes).coerceAtLeast(0)
    val remainingHours = remainingMinutes / 60f
    val surplusMinutes = (completedMinutes - dailyTargetMinutes).coerceAtLeast(0)

    // Status Badge & Colors
    val isGoalAchieved = progressFraction >= 1f
    val statusInfo = when {
        isGoalAchieved -> Triple("DAILY GOAL MET! 🌟", SoftMint, SoftMintDark)
        progressFraction >= 0.75f -> Triple("75% CRUSHED 🔥", ElectricBlue, ElectricBlueDark)
        progressFraction >= 0.50f -> Triple("HALFWAY ⚡", Color(0xFFFBBF24), Color(0xFFD97706))
        progressFraction > 0f -> Triple("IN PROGRESS 🚀", ElectricBlue, ElectricBlueDark)
        else -> Triple("READY TO STUDY 🎯", colorScheme.onSurfaceVariant, colorScheme.onSurfaceVariant)
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_study_goal_card"),
        shape = RoundedCornerShape(22.dp),
        accentColor = if (isGoalAchieved) SoftMint else ElectricBlue
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Icon, Title, Status Tag, and Target Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isGoalAchieved) SoftMint.copy(alpha = 0.2f)
                                else ElectricBlue.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (isGoalAchieved) SoftMintDark else ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Daily Study Goal",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusInfo.second.copy(alpha = if (isDark) 0.18f else 0.35f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = statusInfo.first,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) statusInfo.second else statusInfo.third,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                        Text(
                            text = "Target: ${formatHours(targetHours)} today • ${if (completedSessionsCount > 0) "$completedSessionsCount session${if (completedSessionsCount > 1) "s" else ""}" else "No sessions yet"}",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Edit Target Hours Button
                OutlinedButton(
                    onClick = { showEditGoalDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentColor = colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("edit_daily_goal_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Daily Target",
                        modifier = Modifier.size(13.dp),
                        tint = if (isGoalAchieved) SoftMintDark else ElectricBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatHours(targetHours),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalAchieved) SoftMintDark else ElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content: Left Hours Text & Guidance, Right Circular Progress Ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left metrics column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatHours(completedHours),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = " / ${formatHours(targetHours)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isGoalAchieved) {
                        Text(
                            text = if (surplusMinutes > 0)
                                "🏆 Fantastic! Target reached with +${formatMinutes(surplusMinutes)} bonus study."
                            else
                                "🎉 Target achieved for today! Outstanding discipline.",
                            fontSize = 11.5.sp,
                            color = if (isDark) SoftMint else Color(0xFF047857),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp
                        )
                    } else {
                        Text(
                            text = if (completedMinutes == 0)
                                "⏱️ ${formatMinutes(dailyTargetMinutes)} remaining to hit your daily commitment."
                            else
                                "⚡ ${formatHours(remainingHours)} (${remainingMinutes}m) left to complete today's target.",
                            fontSize = 11.5.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Right: Circular Progress Indicator Component
                DailyCircularProgressIndicator(
                    progress = progressFraction,
                    progressPercentage = progressPercentage,
                    isGoalAchieved = isGoalAchieved,
                    isDark = isDark,
                    size = 78.dp,
                    strokeWidth = 8.dp,
                    modifier = Modifier.testTag("daily_goal_progress_ring")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Target Presets Row (1-Tap Hour Adjustment)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color.Black.copy(alpha = 0.35f) else colorScheme.surface.copy(alpha = 0.8f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Target:",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant
                )

                val quickHours = listOf(2f, 3f, 4f, 5f, 6f, 8f)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    quickHours.forEach { h ->
                        val isSelected = Math.abs(targetHours - h) < 0.1f
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) ElectricBlue
                                    else if (isDark) Color(0xFF263040)
                                    else Color(0xFFE2E8F0)
                                )
                                .clickable { onUpdateDailyTargetHours(h) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${h.toInt()}h",
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color(0xFF071B2B) else colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Quick Start Study Session Action
            if (onStartTimer != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (completedSessionsCount > 0)
                            "$completedSessionsCount focus session${if (completedSessionsCount > 1) "s" else ""} logged today"
                        else
                            "Start your first session today",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = onStartTimer,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("start_daily_study_timer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isGoalAchieved) SoftMintDark else ElectricBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGoalAchieved) "Keep Studying →" else "Start Session →",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoalAchieved) SoftMintDark else ElectricBlue
                        )
                    }
                }
            }
        }
    }

    // Input Modal / Target Study Hours Customization Dialog
    if (showEditGoalDialog) {
        DailyTargetHoursInputDialog(
            currentTargetHours = targetHours,
            onDismiss = { showEditGoalDialog = false },
            onConfirm = { hours ->
                onUpdateDailyTargetHours(hours)
                showEditGoalDialog = false
            }
        )
    }
}

/**
 * Dedicated Circular Progress Indicator Component for Daily Study Goal
 */
@Composable
fun DailyCircularProgressIndicator(
    progress: Float,
    progressPercentage: Int,
    isGoalAchieved: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 78.dp,
    strokeWidth: Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "dailyCircularProgressAnim"
    )

    val primaryColor = if (isGoalAchieved) SoftMintDark else ElectricBlue
    val secondaryColor = if (isGoalAchieved) SoftMint else Color(0xFF38BDF8)
    val trackColor = if (isDark) Color(0xFF1E2838) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - stroke) / 2
            val center = Offset(this.size.width / 2, this.size.height / 2)

            // 1. Background Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = stroke)
            )

            // 2. Animated Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to primaryColor,
                        0.5f to secondaryColor,
                        1.0f to primaryColor,
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content: Percentage & Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isGoalAchieved) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isDark) SoftMint else SoftMintDark,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "100%",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) SoftMint else SoftMintDark
                )
            } else {
                Text(
                    text = "$progressPercentage%",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "TODAY",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Interactive Input Dialog for Target Daily Study Hours
 */
@Composable
fun DailyTargetHoursInputDialog(
    currentTargetHours: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val colorScheme = MaterialTheme.colorScheme
    var selectedHours by remember { mutableFloatStateOf(currentTargetHours) }

    val presetOptions = listOf(1f, 2f, 3f, 4f, 5f, 6f, 8f, 10f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) DarkSurface else colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set Daily Study Target",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Specify your committed target study hours for each day. Your daily progress ring and remaining focus time will adjust accordingly.",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )

                // Stepper + Hour Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedHours > 0.5f) {
                                selectedHours = (Math.round((selectedHours - 0.5f) * 10f) / 10f).coerceAtLeast(0.5f)
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF2C3545) else Color.White)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = colorScheme.onSurface)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatHours(selectedHours),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricBlue
                        )
                        val totalMinutes = (selectedHours * 60).toInt()
                        Text(
                            text = "$totalMinutes minutes / day",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            if (selectedHours < 16f) {
                                selectedHours = (Math.round((selectedHours + 0.5f) * 10f) / 10f).coerceAtMost(16f)
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF2C3545) else Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = colorScheme.onSurface)
                    }
                }

                // Slider for fine tuning
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fine-tune Target", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                        Text("${formatHours(selectedHours)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                    }
                    Slider(
                        value = selectedHours,
                        onValueChange = { newValue ->
                            selectedHours = (Math.round(newValue * 2f) / 2f).coerceIn(0.5f, 14f)
                        },
                        valueRange = 0.5f..14f,
                        steps = 26,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricBlue,
                            activeTrackColor = ElectricBlue,
                            inactiveTrackColor = if (isDark) Color(0xFF242E3E) else Color(0xFFE2E8F0)
                        )
                    )
                }

                // Quick Presets
                Column {
                    Text(
                        text = "Common Presets",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetOptions.take(4).forEach { preset ->
                            val isSelected = Math.abs(selectedHours - preset) < 0.1f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) ElectricBlue
                                        else if (isDark) DarkSurfaceElevated
                                        else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { selectedHours = preset }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${preset.toInt()}h",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF071B2B) else colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetOptions.drop(4).forEach { preset ->
                            val isSelected = Math.abs(selectedHours - preset) < 0.1f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) ElectricBlue
                                        else if (isDark) DarkSurfaceElevated
                                        else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { selectedHours = preset }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${preset.toInt()}h",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF071B2B) else colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedHours) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = Color(0xFF071B2B)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Daily Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colorScheme.onSurfaceVariant)
            }
        }
    )
}

private fun formatHours(hours: Float): String {
    return if (hours % 1f == 0f) {
        "${hours.toInt()}h"
    } else {
        String.format(Locale.getDefault(), "%.1fh", hours)
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}
