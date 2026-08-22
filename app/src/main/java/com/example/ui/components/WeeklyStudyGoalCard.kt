package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudySession
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class DayStudyRecord(
    val dayShort: String, // "M", "T", "W", "T", "F", "S", "S"
    val dayName: String,  // "Mon", "Tue", etc.
    val dateStr: String,  // "yyyy-MM-dd"
    val studyMinutes: Int,
    val isToday: Boolean,
    val isPastOrToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyStudyGoalCard(
    weeklyTargetMinutes: Int,
    studySessions: List<StudySession>,
    onUpdateWeeklyTargetHours: (Int) -> Unit,
    onStartTimer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val colorScheme = MaterialTheme.colorScheme

    var showEditGoalDialog by remember { mutableStateOf(false) }

    // 1. Calculate Current Week Start (Monday 00:00) and End (Sunday 23:59)
    val calendar = Calendar.getInstance()
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

    val currentWeekDays = remember(studySessions, todayDateStr) {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        // Move to Monday of current week
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dayShorts = listOf("M", "T", "W", "T", "F", "S", "S")

        val daysList = mutableListOf<DayStudyRecord>()
        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 23)
        todayCal.set(Calendar.MINUTE, 59)

        for (i in 0 until 7) {
            val dStr = dateFormat.format(cal.time)
            val isToday = dStr == todayDateStr
            val isPastOrToday = cal.timeInMillis <= todayCal.timeInMillis

            // Sum study session seconds for this date
            val daySeconds = studySessions.filter {
                dateFormat.format(Date(it.timestamp)) == dStr
            }.sumOf { it.durationSeconds }

            daysList.add(
                DayStudyRecord(
                    dayShort = dayShorts[i],
                    dayName = dayNames[i],
                    dateStr = dStr,
                    studyMinutes = (daySeconds / 60).toInt(),
                    isToday = isToday,
                    isPastOrToday = isPastOrToday
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        daysList
    }

    // Total minutes studied this week
    val weeklyCompletedMinutes = remember(currentWeekDays) {
        currentWeekDays.sumOf { it.studyMinutes }
    }

    val targetHours = (weeklyTargetMinutes / 60).coerceAtLeast(1)
    val completedHours = weeklyCompletedMinutes / 60f
    val progressFraction = (weeklyCompletedMinutes.toFloat() / weeklyTargetMinutes.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val progressPercentage = ((weeklyCompletedMinutes.toFloat() / weeklyTargetMinutes.coerceAtLeast(1).toFloat()) * 100).toInt()

    val remainingMinutes = (weeklyTargetMinutes - weeklyCompletedMinutes).coerceAtLeast(0)
    val remainingHours = remainingMinutes / 60f

    // Days remaining in this week including today
    val daysRemainingInWeek = remember(currentWeekDays) {
        val todayIndex = currentWeekDays.indexOfFirst { it.isToday }.coerceAtLeast(0)
        (7 - todayIndex).coerceAtLeast(1)
    }
    val dailyHoursNeeded = if (remainingHours > 0f) remainingHours / daysRemainingInWeek else 0f

    // Status Badge & Text
    val statusInfo = when {
        progressFraction >= 1f -> Triple("GOAL CRUSHED! 🎉", SoftMint, SoftMintDark)
        progressFraction >= 0.75f -> Triple("ALMOST THERE 🔥", ElectricBlue, ElectricBlueDark)
        progressFraction >= 0.40f -> Triple("ON TRACK ⚡", Color(0xFFFBBF24), Color(0xFFD97706))
        else -> Triple("IN PROGRESS 🚀", ElectricBlue, ElectricBlueDark)
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_study_goal_card"),
        shape = RoundedCornerShape(22.dp),
        accentColor = if (progressFraction >= 1f) SoftMint else ElectricBlue
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title, Target Hour Chip & Edit Button
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
                            .background(if (progressFraction >= 1f) SoftMint.copy(alpha = 0.2f) else ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = if (progressFraction >= 1f) SoftMintDark else ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Weekly Study Goal",
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
                            text = "Target: ${targetHours}h per week • Mon-Sun cycle",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Edit Target Button
                OutlinedButton(
                    onClick = { showEditGoalDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentColor = colorScheme.onSurface
                    ),
                    modifier = Modifier.height(32.dp).testTag("edit_weekly_goal_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Edit Weekly Target",
                        modifier = Modifier.size(13.dp),
                        tint = ElectricBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${targetHours}h",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Metrics Section: Progress Ring & Stats Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Column: Hours Numbers & Pace Guidance
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", completedHours),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = " / ${targetHours} hrs",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (progressFraction >= 1f) {
                        Text(
                            text = "🎯 You hit 100% of your weekly target! Extra hours boost mastery.",
                            fontSize = 11.5.sp,
                            color = if (isDark) SoftMint else Color(0xFF047857),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp
                        )
                    } else {
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", remainingHours)}h left • ~${String.format(Locale.getDefault(), "%.1f", dailyHoursNeeded)}h/day over ${daysRemainingInWeek}d",
                            fontSize = 11.5.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Right: Glowing Radial Progress Ring with Percentage
                ProgressRing(
                    progress = progressFraction,
                    size = 76.dp,
                    strokeWidth = 7.5.dp,
                    primaryColor = if (progressFraction >= 1f) SoftMintDark else ElectricBlue,
                    secondaryColor = if (progressFraction >= 1f) SoftMint else SoftMintDark,
                    backgroundColor = if (isDark) Color(0xFF1E2838) else colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$progressPercentage%",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = if (progressFraction >= 1f) "DONE" else "WEEK",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (progressFraction >= 1f) (if (isDark) SoftMint else Color(0xFF047857)) else colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day-by-Day (Mon-Sun) Micro Visualizer Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color.Black.copy(alpha = 0.35f) else colorScheme.surface.copy(alpha = 0.8f))
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentWeekDays.forEach { day ->
                    val dayHours = day.studyMinutes / 60f
                    val hasStudied = day.studyMinutes > 0

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Day Label
                        Text(
                            text = day.dayShort,
                            fontSize = 10.sp,
                            fontWeight = if (day.isToday) FontWeight.Black else FontWeight.Bold,
                            color = if (day.isToday) ElectricBlue else colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Mini Vertical Bar / Indicator
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    when {
                                        day.isToday && hasStudied -> ElectricBlue.copy(alpha = 0.85f)
                                        day.isToday -> ElectricBlue.copy(alpha = 0.25f)
                                        hasStudied -> SoftMintDark.copy(alpha = 0.75f)
                                        day.isPastOrToday -> (if (isDark) Color(0xFF263040) else Color(0xFFE2E8F0))
                                        else -> (if (isDark) Color(0xFF1B202A) else Color(0xFFF1F5F9))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasStudied) {
                                Text(
                                    text = if (dayHours >= 1f) "${dayHours.toInt()}h" else "${day.studyMinutes}m",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF041E10)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Small today marker dot
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (day.isToday) ElectricBlue else Color.Transparent)
                        )
                    }
                }
            }

            if (onStartTimer != null && progressFraction < 1f) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onStartTimer,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Start Focus Session →",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    }
                }
            }
        }
    }

    // Target Hours Edit Dialog
    if (showEditGoalDialog) {
        var selectedHours by remember { mutableIntStateOf(targetHours) }
        val presetTargets = listOf(14, 20, 25, 30, 35, 42)

        AlertDialog(
            onDismissRequest = { showEditGoalDialog = false },
            containerColor = if (isDark) DarkSurface else colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set Weekly Study Target",
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
                        text = "Choose your committed target hours per week. Your progress ring and daily study pace will automatically calibrate.",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    // Target Hours Stepper
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (selectedHours > 5) selectedHours -= 1 },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF2C3545) else Color.White)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = colorScheme.onSurface)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$selectedHours Hours",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricBlue
                            )
                            Text(
                                text = "~${String.format(Locale.getDefault(), "%.1f", selectedHours / 7f)}h per day",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { if (selectedHours < 90) selectedHours += 1 },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF2C3545) else Color.White)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = colorScheme.onSurface)
                        }
                    }

                    // Quick Preset Chips
                    Column {
                        Text(
                            text = "Quick Presets",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presetTargets.forEach { preset ->
                                val isSelected = selectedHours == preset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) ElectricBlue else if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .clickable { selectedHours = preset }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${preset}h",
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
                    onClick = {
                        onUpdateWeeklyTargetHours(selectedHours)
                        showEditGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = Color(0xFF071B2B)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Goal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoalDialog = false }) {
                    Text("Cancel", color = colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
