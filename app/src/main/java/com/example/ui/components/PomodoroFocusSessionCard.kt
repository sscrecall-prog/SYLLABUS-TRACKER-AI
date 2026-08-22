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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AmbientSoundType
import com.example.data.model.Subject
import com.example.data.model.SyllabusItem
import com.example.data.model.TimerMode
import com.example.ui.theme.*
import com.example.ui.viewmodel.TimerViewModel
import java.util.Locale

/**
 * High-fidelity Pomodoro Focus Session Timer Card
 * - Tracks focused study minutes with 1-tap start/pause/log
 * - Automatically records completed sessions to Room DB
 * - Live synchronization with Daily & Weekly Goal trackers
 * - Quick presets (25m Pomodoro, 15m Sprint, 45m Deep Work, 5m Break)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroFocusSessionCard(
    timerViewModel: TimerViewModel,
    subjects: List<Subject>,
    items: List<SyllabusItem>,
    onOpenFullTimer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f

    val isRunning by timerViewModel.isTimerRunning.collectAsState()
    val remainingSeconds by timerViewModel.timerRemainingSeconds.collectAsState()
    val totalDurationSeconds by timerViewModel.timerTotalDurationSeconds.collectAsState()
    val timerMode by timerViewModel.timerMode.collectAsState()
    val activeSubject by timerViewModel.timerSubject.collectAsState()
    val activeChapter by timerViewModel.timerChapter.collectAsState()
    val pomodoroCycles by timerViewModel.pomodoroCyclesCompleted.collectAsState()
    val isAmbientPlaying by timerViewModel.isAmbientPlaying.collectAsState()
    val currentAmbientSound by timerViewModel.ambientSound.collectAsState()

    var showSubjectDropdown by remember { mutableStateOf(false) }

    val formattedTime = remember(remainingSeconds) {
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    val elapsedSeconds = remember(remainingSeconds, totalDurationSeconds, timerMode) {
        if (timerMode == TimerMode.STOPWATCH) remainingSeconds
        else (totalDurationSeconds - remainingSeconds).coerceAtLeast(0L)
    }

    val progressFraction = remember(remainingSeconds, totalDurationSeconds, timerMode) {
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

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pomodoro_focus_session_card"),
        shape = RoundedCornerShape(22.dp),
        accentColor = if (isRunning) ElectricBlue else SoftMint
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title, Session Badge, and Fullscreen / Detail shortcut
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
                                if (isRunning) ElectricBlue.copy(alpha = 0.2f)
                                else SoftMint.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (isRunning) ElectricBlue else SoftMintDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Focus Session",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isRunning) ElectricBlue.copy(alpha = if (isDark) 0.2f else 0.4f)
                                        else SoftMint.copy(alpha = if (isDark) 0.2f else 0.4f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isRunning) "ACTIVE ⏱️" else "READY 🎯",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isRunning) ElectricBlue else SoftMintDark,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                        Text(
                            text = if (pomodoroCycles > 0) "🍅 $pomodoroCycles Pomodoro${if (pomodoroCycles > 1) "s" else ""} done today" else "Boost concentration & log study time",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onOpenFullTimer != null) {
                    IconButton(
                        onClick = onOpenFullTimer,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .testTag("open_full_timer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Expand Full Timer",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subject & Topic Target Selector Pill
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSubjectDropdown = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(activeSubjectColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeSubject?.name ?: "All Subjects / General Study",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (activeChapter != null) {
                                Text(
                                    text = " • ${activeChapter!!.title}",
                                    fontSize = 11.5.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change Subject",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSubjectDropdown,
                    onDismissRequest = { showSubjectDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("General Study (No Specific Subject)") },
                        onClick = {
                            timerViewModel.selectTimerSubject(null)
                            timerViewModel.selectTimerChapter(null)
                            showSubjectDropdown = false
                        }
                    )
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.name) },
                            onClick = {
                                timerViewModel.selectTimerSubject(subject)
                                timerViewModel.selectTimerChapter(null)
                                showSubjectDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Display: Circular Progress Arc + Time Digits + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time digits and active session metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedTime,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.onSurface,
                        letterSpacing = (-1).sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = when {
                            isRunning -> "🔥 Focused study active • Auto-saving to trackers"
                            elapsedSeconds > 0 -> "⏸️ Paused • ${(elapsedSeconds / 60)}m focused"
                            else -> "⚡ Select duration & tap start to focus"
                        },
                        fontSize = 11.5.sp,
                        color = if (isRunning) ElectricBlue else colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    if (elapsedSeconds >= 60) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 Tap 'Finish & Log' below to credit ${(elapsedSeconds / 60)}m to your daily goal.",
                            fontSize = 10.5.sp,
                            color = if (isDark) SoftMint else Color(0xFF047857),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Circular Progress Arc
                PomodoroCircularRing(
                    progress = progressFraction,
                    isRunning = isRunning,
                    isDark = isDark,
                    size = 78.dp,
                    strokeWidth = 7.5.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Duration Presets Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color.Black.copy(alpha = 0.35f) else colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val presets = listOf(
                    Triple(25, "25m", "Pomodoro"),
                    Triple(15, "15m", "Sprint"),
                    Triple(45, "45m", "Deep"),
                    Triple(50, "50m", "Intense"),
                    Triple(5, "5m", "Break")
                )

                presets.forEach { (minutes, label, _) ->
                    val isSelected = totalDurationSeconds == minutes * 60L && timerMode == TimerMode.POMODORO
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) ElectricBlue
                                else Color.Transparent
                            )
                            .clickable {
                                timerViewModel.setTimerPreset(minutes, TimerMode.POMODORO)
                            }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) Color(0xFF071B2B) else colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Control Buttons (Play/Pause, Log & Save, Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                Button(
                    onClick = {
                        if (isRunning) timerViewModel.pauseTimer()
                        else timerViewModel.startTimer()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color(0xFFE11D48) else ElectricBlue,
                        contentColor = if (isRunning) Color.White else Color(0xFF071B2B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("pomodoro_card_play_pause_btn")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "Pause" else "Start Focus",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Finish & Log Session (credits study minutes to daily goal tracker immediately)
                if (elapsedSeconds >= 60 || isRunning) {
                    FilledTonalButton(
                        onClick = { timerViewModel.finishAndLogTimer() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isDark) SoftMintDark.copy(alpha = 0.25f) else SoftMint.copy(alpha = 0.35f),
                            contentColor = if (isDark) SoftMint else SoftMintDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("pomodoro_card_finish_log_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Log ${(elapsedSeconds / 60).coerceAtLeast(1)}m",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Reset Button
                IconButton(
                    onClick = { timerViewModel.resetTimer() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .testTag("pomodoro_card_reset_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Animated Circular Countdown Arc for Pomodoro Focus Session Card
 */
@Composable
fun PomodoroCircularRing(
    progress: Float,
    isRunning: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 78.dp,
    strokeWidth: Dp = 7.5.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
        label = "pomodoroProgressAnim"
    )

    val primaryColor = if (isRunning) ElectricBlue else SoftMintDark
    val secondaryColor = if (isRunning) Color(0xFF38BDF8) else SoftMint
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

            // Background Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = stroke)
            )

            // Progress Arc
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

        // Center Icon
        Icon(
            imageVector = if (isRunning) Icons.Default.LocalFireDepartment else Icons.Default.Timer,
            contentDescription = null,
            tint = if (isRunning) ElectricBlue else SoftMintDark,
            modifier = Modifier.size(22.dp)
        )
    }
}
