package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SyllabusItem
import com.example.data.model.Subject
import com.example.data.model.TimerMode
import com.example.ui.components.BentoCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientCard
import com.example.ui.components.ProgressRing
import com.example.ui.components.StatMiniCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.TimerViewModel
import com.example.ui.viewmodel.SyllabusViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen() {
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val subjectViewModel: SubjectViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()
    val syllabusViewModel: SyllabusViewModel = viewModel()

    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f

    val timerMode by timerViewModel.timerMode.collectAsState()
    val remainingSeconds by timerViewModel.timerRemainingSeconds.collectAsState()
    val totalDurationSeconds by timerViewModel.timerTotalDurationSeconds.collectAsState()
    val isRunning by timerViewModel.isTimerRunning.collectAsState()
    val activeSubject by timerViewModel.timerSubject.collectAsState()
    val activeChapter by timerViewModel.timerChapter.collectAsState()
    val pomodoroCycles by timerViewModel.pomodoroCyclesCompleted.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()
    val items by syllabusViewModel.items.collectAsState()
    val studySessions by syllabusViewModel.studySessions.collectAsState()
    val overallStats by analyticsViewModel.overallStats.collectAsState()

    var showSubjectSelector by remember { mutableStateOf(false) }

    val formattedMinutes = remember(remainingSeconds) {
        val mins = remainingSeconds / 60
        String.format("%02d", mins)
    }
    val formattedSeconds = remember(remainingSeconds) {
        val secs = remainingSeconds % 60
        String.format("%02d", secs)
    }

    val progress = remember(remainingSeconds, totalDurationSeconds, timerMode) {
        if (timerMode == TimerMode.STOPWATCH) {
            val hourSecs = 3600f
            ((remainingSeconds % 3600) / hourSecs).coerceIn(0f, 1f)
        } else {
            if (totalDurationSeconds > 0) (remainingSeconds.toFloat() / totalDurationSeconds).coerceIn(0f, 1f) else 1f
        }
    }

    val activeSubjectColor = remember(activeSubject) {
        if (activeSubject != null) {
            try {
                Color(android.graphics.Color.parseColor(activeSubject!!.colorHex))
            } catch (e: Exception) {
                ElectricBlue
            }
        } else {
            ElectricBlue
        }
    }

    val availableChapters = remember(activeSubject, items) {
        if (activeSubject == null) emptyList()
        else items.filter { it.subjectId == activeSubject?.id }
    }

    // Recent sessions for today
    val todaySessions = remember(studySessions) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        studySessions.filter { dateFormat.format(Date(it.timestamp)) == todayStr }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("pomodoro_timer_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. TOP TIMER MODE SWITCHER (Segmented Pills)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceElevated)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TimerMode.values().forEach { mode ->
                    val isSelected = timerMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) ElectricBlue
                                else Color.Transparent
                            )
                            .clickable {
                                timerViewModel.pauseTimer()
                                timerViewModel.timerMode.value = mode
                                when (mode) {
                                    TimerMode.STOPWATCH -> timerViewModel.timerRemainingSeconds.value = 0L
                                    TimerMode.POMODORO -> timerViewModel.setTimerPreset(25)
                                    TimerMode.CUSTOM -> timerViewModel.setTimerPreset(45)
                                }
                            }
                            .padding(vertical = 9.dp)
                            .testTag("timer_mode_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (mode) {
                                    TimerMode.POMODORO -> Icons.Default.Timer
                                    TimerMode.STOPWATCH -> Icons.Default.HourglassBottom
                                    TimerMode.CUSTOM -> Icons.Default.Tune
                                },
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF071B2B) else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = mode.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF071B2B) else colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. LINKED TARGET SELECTOR CARD (Subject & Chapter)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timer_target_card"),
                shape = RoundedCornerShape(20.dp),
                accentColor = activeSubjectColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(activeSubjectColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (activeChapter != null) Icons.Default.MenuBook else Icons.Default.School,
                                contentDescription = null,
                                tint = activeSubjectColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FOCUS STUDY TARGET",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeSubjectColor,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = activeSubject?.name ?: "All Subjects (Free Focus)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (activeChapter != null) "Chapter: ${activeChapter?.title}" else "Tap Change to link chapter",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showSubjectSelector = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ElectricBlue.copy(alpha = 0.2f),
                            contentColor = ElectricBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("timer_change_target_btn")
                    ) {
                        Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. MAIN POMODORO CIRCULAR TIMER DIAL & PRESETS
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pomodoro_dial_card"),
                shape = RoundedCornerShape(26.dp),
                accentColor = if (isRunning) AlertRed else activeSubjectColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quick Interval Presets Row
                    if (timerMode == TimerMode.POMODORO) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                15 to "15 min",
                                25 to "25 min (Standard)",
                                45 to "45 min",
                                60 to "60 min"
                            ).forEach { (mins, label) ->
                                val isCurrent = totalDurationSeconds == mins * 60L
                                FilterChip(
                                    selected = isCurrent,
                                    onClick = { timerViewModel.setTimerPreset(mins) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = activeSubjectColor.copy(alpha = 0.2f),
                                        selectedLabelColor = activeSubjectColor,
                                        containerColor = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        labelColor = colorScheme.onSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isCurrent) activeSubjectColor else if (isDark) DarkGlassBorder else colorScheme.outlineVariant,
                                        enabled = true,
                                        selected = isCurrent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("preset_${mins}m")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Dial Canvas Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(240.dp)
                    ) {
                        ProgressRing(
                            progress = progress,
                            size = 230.dp,
                            strokeWidth = 14.dp,
                            primaryColor = if (isRunning) ElectricBlue else activeSubjectColor,
                            secondaryColor = if (isRunning) SoftMint else StatusCompleted,
                            backgroundColor = if (isDark) Color(0xFF1E2838) else colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Monospace-style time counter
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formattedMinutes,
                                        fontSize = 46.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = ":",
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricBlue,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = formattedSeconds,
                                        fontSize = 46.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Status Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isRunning) ElectricBlue.copy(alpha = 0.18f)
                                            else if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = when {
                                            timerMode == TimerMode.STOPWATCH -> "STOPWATCH ACTIVE"
                                            isRunning -> "FOCUSING • STAY LOCKED IN"
                                            else -> "READY TO FOCUS"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRunning) ElectricBlue else colorScheme.onSurfaceVariant
                                    )
                                }

                                if (pomodoroCycles > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "🍅 $pomodoroCycles pomodoros completed",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SoftMint
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Controls Row (Reset, Main Play/Pause FAB, Save Session)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset Button
                        FilledIconButton(
                            onClick = { timerViewModel.resetTimer() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("timer_reset_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = DarkSurfaceElevated,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Timer", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Large Play / Pause FAB
                        LargeFloatingActionButton(
                            onClick = {
                                if (isRunning) timerViewModel.pauseTimer() else timerViewModel.startTimer()
                            },
                            containerColor = if (isRunning) AlertRed else ElectricBlue,
                            contentColor = if (isRunning) Color.White else Color(0xFF071B2B),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(72.dp)
                                .testTag("timer_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Start",
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Save & Log Session Button
                        FilledIconButton(
                            onClick = { timerViewModel.finishAndSaveTimerSession() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("timer_save_session_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = SoftMint.copy(alpha = 0.2f),
                                contentColor = SoftMint
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save & Log Session",
                                tint = SoftMint
                            )
                        }
                    }
                }
            }
        }

        // 4. TODAY'S LOGGED STUDY STATS & RECENT LOGS
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_timer_stats_card"),
                shape = RoundedCornerShape(20.dp),
                accentColor = SoftMint
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Study Log",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "${overallStats.todayStudyMinutes} mins logged today across ${todaySessions.size} sessions",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SoftMint.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${overallStats.todayStudyMinutes / 60}h ${overallStats.todayStudyMinutes % 60}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftMint
                            )
                        }
                    }

                    if (todaySessions.isEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sessions logged yet today. Start the timer to begin tracking!",
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            todaySessions.take(4).forEach { session ->
                                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(session.timestamp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(activeSubjectColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = session.subjectName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.onSurface
                                            )
                                            Text(
                                                text = session.chapterTitle,
                                                fontSize = 10.sp,
                                                color = colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${session.durationSeconds / 60} mins",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricBlue
                                        )
                                        Text(
                                            text = timeStr,
                                            fontSize = 9.sp,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Subject & Chapter Selector Dialog
    if (showSubjectSelector) {
        AlertDialog(
            onDismissRequest = { showSubjectSelector = false },
            containerColor = if (isDark) DarkSurface else colorScheme.surface,
            title = { Text("Link Study Session Target", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = colorScheme.onSurface) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Subject", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects) { s ->
                            val isSel = activeSubject?.id == s.id
                            FilterChip(
                                selected = isSel,
                                onClick = { timerViewModel.setTimerTargetById(s.id, null) },
                                label = { Text(s.name, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = ElectricBlue,
                                    containerColor = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    labelColor = colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    if (availableChapters.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Select Chapter (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(availableChapters.take(20)) { top ->
                                val isChapterSel = activeChapter?.id == top.id
                                FilterChip(
                                    selected = isChapterSel,
                                    onClick = { timerViewModel.setTimerTargetById(activeSubject?.id, if (isChapterSel) null else top.id) },
                                    label = { Text(top.title, fontSize = 10.sp, maxLines = 1) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = ElectricBlue,
                                        containerColor = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        labelColor = colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSubjectSelector = false },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color(0xFF071B2B))
                ) {
                    Text("Apply Target", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
