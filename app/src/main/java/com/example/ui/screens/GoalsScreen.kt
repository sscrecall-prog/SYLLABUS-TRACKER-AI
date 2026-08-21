package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Goal
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientCard
import com.example.ui.components.LinearSyllabusBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.GoalsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GoalsScreen(
    onNavigate: (NavDestination) -> Unit
) {
    val subjectViewModel: SubjectViewModel = viewModel()
    val goalsViewModel: GoalsViewModel = viewModel()

    val goals by goalsViewModel.goals.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalTitle by remember { mutableStateOf("") }
    var goalTargetDate by remember { mutableStateOf("2026-09-30") }
    var goalTargetChapters by remember { mutableIntStateOf(25) }
    var goalTargetHours by remember { mutableFloatStateOf(50f) }
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val now = System.currentTimeMillis()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        item {
            GradientCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = listOf(BrandForestGreen, Color(0xFF1B4332))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🎯 Target Deadlines & Goals",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandWarmCream
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Set structured milestones to complete subjects on schedule",
                        fontSize = 12.sp,
                        color = BrandCreamDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active Goals", fontSize = 11.sp, color = BrandCreamDark)
                            Text("${goals.count { !it.isCompleted }}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandWarmCream)
                        }
                        Column {
                            Text("Completed", fontSize = 11.sp, color = BrandCreamDark)
                            Text("${goals.count { it.isCompleted }}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StatusCompleted)
                        }
                        Column {
                            Text("Total Milestones", fontSize = 11.sp, color = BrandCreamDark)
                            Text("${goals.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandWarmCream)
                        }
                    }
                }
            }
        }

        // Add Goal Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exam Milestones",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { showAddGoalDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Goal")
                }
            }
        }

        // Goal Cards
        if (goals.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.TrackChanges, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Goals Set Yet", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add targets to keep your exam preparation focused and timely.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAddGoalDialog = true }) {
                            Text("+ Create First Goal")
                        }
                    }
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                val targetTime = try {
                    dateFormat.parse(goal.targetDateStr)?.time ?: now
                } catch (e: Exception) {
                    now
                }
                val daysRemaining = ((targetTime - now) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                val requiredDailyChapters = if (daysRemaining > 0) String.format("%.1f", (goal.targetChaptersCount - goal.completedChaptersCount).toFloat() / daysRemaining) else "0"
                val progressFraction = if (goal.targetChaptersCount > 0) (goal.completedChaptersCount.toFloat() / goal.targetChaptersCount).coerceIn(0f, 1f) else 0f

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = 2.dp,
                    accentColor = if (goal.isCompleted) StatusCompleted else BrandForestGreen
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = goal.isCompleted,
                                    onCheckedChange = { goalsViewModel.updateGoal(goal.copy(isCompleted = !goal.isCompleted)) },
                                    colors = CheckboxDefaults.colors(checkedColor = StatusCompleted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = goal.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${goal.subjectName} • Target: ${goal.targetDateStr}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { goalsViewModel.deleteGoal(goal) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearSyllabusBar(
                            progress = progressFraction,
                            height = 6.dp,
                            barColor = if (goal.isCompleted) StatusCompleted else BrandForestGreen
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${goal.completedChaptersCount}/${goal.targetChaptersCount} Chapters",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!goal.isCompleted) {
                                Text(
                                    text = "⏳ $daysRemaining days left (Pace: $requiredDailyChapters/day)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (daysRemaining < 7) StatusWeak else BrandTerracotta
                                )
                            } else {
                                Text(
                                    text = "🎉 Milestone Achieved!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusCompleted
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        val currentSub = subjects.find { it.id == selectedSubjectId }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Set New Milestone") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Goal Title (e.g. Master Indian Polity)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    val context = LocalContext.current
                    val calendar = Calendar.getInstance()
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.parse(goalTargetDate)?.let {
                            calendar.time = it
                        }
                    } catch (e: Exception) {}

                    val datePickerDialog = remember {
                        android.app.DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                goalTargetDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = goalTargetDate,
                            onValueChange = { goalTargetDate = it },
                            label = { Text("Target Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
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

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = goalTargetChapters.toString(),
                            onValueChange = { goalTargetChapters = it.toIntOrNull() ?: 20 },
                            label = { Text("Target Chapters") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = goalTargetHours.toString(),
                            onValueChange = { goalTargetHours = it.toFloatOrNull() ?: 30f },
                            label = { Text("Study Hours") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalTitle.isNotBlank()) {
                            goalsViewModel.addGoal(com.example.data.model.Goal(
                                title = goalTitle.trim(),
                                targetDateStr = goalTargetDate.trim(),
                                subjectId = selectedSubjectId,
                                subjectName = currentSub?.name ?: "All Subjects",
                                targetChaptersCount = goalTargetChapters,
                                targetStudyHours = goalTargetHours
                            ))
                            goalTitle = ""
                            showAddGoalDialog = false
                        }
                    },
                    enabled = goalTitle.isNotBlank()
                ) {
                    Text("Create Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
