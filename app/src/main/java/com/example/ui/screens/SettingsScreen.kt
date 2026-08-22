package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeMode
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.theme.motion.motionCardEntry
import com.example.ui.theme.motion.motionPress
import com.example.ui.viewmodel.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.ProfileViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onReplayOnboarding: (() -> Unit)? = null
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val appSettings by settingsViewModel.appSettings.collectAsState()
    val allBadges by profileViewModel.allBadges.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }
    var exportContent by remember { mutableStateOf("") }
    var exportType by remember { mutableStateOf("JSON") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importInput by remember { mutableStateOf("") }

    var showResetConfirm by remember { mutableStateOf(false) }

    var revisionIntervalsInput by remember { mutableStateOf(appSettings.revisionIntervalsCsv) }
    var dailyTargetInput by remember { mutableStateOf(appSettings.dailyTargetMinutes.toString()) }
    var weeklyTargetInput by remember { mutableStateOf(appSettings.weeklyTargetMinutes.toString()) }
    var targetExamNameInput by remember { mutableStateOf(appSettings.targetExam) }
    var targetExamDateInput by remember { mutableStateOf(appSettings.targetExamDateStr) }
    var targetExamShiftInput by remember { mutableStateOf(appSettings.targetExamShift) }

    val unlockedBadgesCount = remember(allBadges) { allBadges.count { it.isUnlocked } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Profile & Achievements Shortcut Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(0)
                    .motionPress(onClick = { mainViewModel.navigateTo(NavDestination.PROFILE) })
                    .testTag("settings_profile_card"),
                shape = RoundedCornerShape(18.dp),
                accentColor = BrandTerracotta
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appSettings.userAvatarEmoji,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${appSettings.userName}'s Profile & Badges",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target: ${appSettings.targetExam} • $unlockedBadgesCount Badges Unlocked",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Appearance & Motion Settings
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(1),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎨 Appearance & Mode",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customize the tracker color palette for optimal visual comfort. Low-light mode protects your eyes during late-night studies.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppThemeMode.values().forEach { mode ->
                            val isSelected = appSettings.themeMode == mode
                            val icon = when (mode) {
                                AppThemeMode.LIGHT -> Icons.Default.WbSunny
                                AppThemeMode.WARM_CREAM -> Icons.Default.Palette
                                AppThemeMode.DARK -> Icons.Default.NightsStay
                                AppThemeMode.SYSTEM -> Icons.Default.Settings
                            }
                            val tintColor = when (mode) {
                                AppThemeMode.LIGHT -> Color(0xFFFFB300)
                                AppThemeMode.WARM_CREAM -> Color(0xFFD97706)
                                AppThemeMode.DARK -> Color(0xFF9575CD)
                                AppThemeMode.SYSTEM -> MaterialTheme.colorScheme.primary
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable { settingsViewModel.updateThemeMode(mode) }
                                    .padding(vertical = 10.dp)
                                    .testTag("theme_mode_${mode.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else tintColor.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = when (mode) {
                                            AppThemeMode.SYSTEM -> "Auto"
                                            AppThemeMode.LIGHT -> "Light"
                                            AppThemeMode.WARM_CREAM -> "Cream"
                                            AppThemeMode.DARK -> "Dark"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Reduced Motion Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { settingsViewModel.updateReducedMotion(!appSettings.reducedMotion) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reduced Motion & Simple Transitions",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Minimize animations and use simple instant cross-fades",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = appSettings.reducedMotion,
                            onCheckedChange = { settingsViewModel.updateReducedMotion(it) },
                            modifier = Modifier.testTag("reduced_motion_switch")
                        )
                    }
                }
            }
        }

        // Spaced Repetition Settings
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(2),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔄 Spaced Repetition Cycle",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Interval days between consecutive revisions (e.g. 1, 3, 7, 21, 60)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = revisionIntervalsInput,
                        onValueChange = { revisionIntervalsInput = it },
                        label = { Text("Revision Interval Days (CSV)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { settingsViewModel.updateRevisionIntervals(revisionIntervalsInput.trim()) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .motionPress(onClick = { settingsViewModel.updateRevisionIntervals(revisionIntervalsInput.trim()) })
                    ) {
                        Text("Save Intervals")
                    }
                }
            }
        }

        // Daily & Weekly Study Targets
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(3),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎯 Daily & Weekly Targets",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dailyTargetInput,
                            onValueChange = { dailyTargetInput = it },
                            label = { Text("Daily Target (mins)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = weeklyTargetInput,
                            onValueChange = { weeklyTargetInput = it },
                            label = { Text("Weekly Target (mins)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val daily = dailyTargetInput.toIntOrNull() ?: 180
                            val weekly = weeklyTargetInput.toIntOrNull() ?: 1200
                            settingsViewModel.updateStudyTargets(daily, weekly)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .motionPress(onClick = {
                                val daily = dailyTargetInput.toIntOrNull() ?: 180
                                val weekly = weeklyTargetInput.toIntOrNull() ?: 1200
                                settingsViewModel.updateStudyTargets(daily, weekly)
                            })
                    ) {
                        Text("Save Targets")
                    }
                }
            }
        }

        // Target Exam & Countdown Date Configuration Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(4),
                shape = RoundedCornerShape(16.dp),
                accentColor = BrandTerracotta
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗓️ Exam Countdown & Stage Settings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Configures the live countdown & study pace calculator on your dashboard.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = targetExamNameInput,
                        onValueChange = { targetExamNameInput = it },
                        label = { Text("Exam Name (e.g. SSC CGL 2026)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val context = LocalContext.current
                        val calendar = java.util.Calendar.getInstance()
                        try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            sdf.parse(targetExamDateInput)?.let {
                                calendar.time = it
                            }
                        } catch (e: Exception) {}

                        val datePickerDialog = remember {
                            android.app.DatePickerDialog(
                                context,
                                { _, selectedYear, selectedMonth, selectedDay ->
                                    targetExamDateInput = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                                },
                                calendar.get(java.util.Calendar.YEAR),
                                calendar.get(java.util.Calendar.MONTH),
                                calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            )
                        }

                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = targetExamDateInput,
                                onValueChange = { targetExamDateInput = it },
                                label = { Text("Exam Date (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { datePickerDialog.show() }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { datePickerDialog.show() }
                            )
                        }
                        OutlinedTextField(
                            value = targetExamShiftInput,
                            onValueChange = { targetExamShiftInput = it },
                            label = { Text("Exam Stage / Shift") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            settingsViewModel.updateExamTarget(
                                examName = targetExamNameInput.trim(),
                                targetDateStr = targetExamDateInput.trim(),
                                examShift = targetExamShiftInput.trim()
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .motionPress(onClick = {
                                settingsViewModel.updateExamTarget(
                                    examName = targetExamNameInput.trim(),
                                    targetDateStr = targetExamDateInput.trim(),
                                    examShift = targetExamShiftInput.trim()
                                )
                            })
                    ) {
                        Text("Save Exam Target")
                    }
                }
            }
        }

        // Data Management (Export & Import)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(4),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💾 Data Backup & Restore",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Export your syllabus, progress, study notes, and logs to JSON or CSV",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    exportContent = settingsViewModel.getExportJson()
                                    exportType = "JSON"
                                    showExportDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export JSON")
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    exportContent = settingsViewModel.getExportCsv()
                                    exportType = "CSV"
                                    showExportDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.TableView, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import Syllabus Data (JSON)")
                    }
                }
            }
        }

        // Danger Zone: Reset Data
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(5),
                shape = RoundedCornerShape(16.dp),
                accentColor = MaterialTheme.colorScheme.error
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ Danger Zone",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reset all subjects, chapters, and sessions to the initial default standard syllabus.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.motionPress(onClick = { showResetConfirm = true })
                    ) {
                        Text("Reset All Data to Sample")
                    }
                }
            }
        }

        // App Branding & Version Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(6),
                shape = RoundedCornerShape(16.dp),
                accentColor = MaterialTheme.colorScheme.primary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_app_logo),
                        contentDescription = "Syllabus Tracker App Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Syllabus Tracker",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Adaptive Preparation & Intelligent Revision Engine",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0.0 (Production Release)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (onReplayOnboarding != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onReplayOnboarding,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("replay_welcome_tour_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Replay Welcome Tour", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export $exportType Content") },
            text = {
                Column {
                    Text("Your syllabus data has been exported successfully:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportContent,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 10.sp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Syllabus Tracker Export", exportContent)
                    clipboard.setPrimaryClip(clip)
                    mainViewModel.showSnackbar("Copied $exportType to clipboard!")
                    showExportDialog = false
                }) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Syllabus Data (JSON)") },
            text = {
                Column {
                    Text("Paste valid JSON export data below to import:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInput,
                        onValueChange = { importInput = it },
                        placeholder = { Text("Paste JSON here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 10.sp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        val success = settingsViewModel.importData(importInput.trim())
                        if (success) {
                            mainViewModel.showSnackbar("Syllabus imported successfully!")
                            showImportDialog = false
                            importInput = ""
                        } else {
                            mainViewModel.showSnackbar("Invalid JSON format. Check structure.")
                        }
                    }
                }) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset to Default Syllabus?") },
            text = {
                Text("This will wipe all custom chapters, notes, and study logs and restore the original 5-subject comprehensive syllabus. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.resetData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
