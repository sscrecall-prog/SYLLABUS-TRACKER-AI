package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyPlan
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientCard
import com.example.ui.components.LinearSyllabusBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.TimerViewModel
import com.example.ui.viewmodel.SyllabusViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlannerScreen(
    onNavigate: (NavDestination) -> Unit
) {
    val subjectViewModel: SubjectViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()
    val syllabusViewModel: SyllabusViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()
    val plannerViewModel: PlannerViewModel = viewModel()

    val uiState by plannerViewModel.uiState.collectAsState()
    val todayPlans = uiState.todayPlans
    val subjects by subjectViewModel.subjects.collectAsState()
    val items by syllabusViewModel.items.collectAsState()

    var showAddPlanDialog by remember { mutableStateOf(false) }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 1L) }
    var planChapterTitle by remember { mutableStateOf("") }
    var planTime by remember { mutableStateOf("08:00") }
    var plannedMinutes by remember { mutableIntStateOf(60) }
    var planNotes by remember { mutableStateOf("") }

    val totalTasks = todayPlans.size
    val completedTasks = todayPlans.count { it.isCompleted }
    val plannedMins = todayPlans.sumOf { it.plannedMinutes }
    val actualMins = todayPlans.sumOf { if (it.isCompleted) it.plannedMinutes else it.actualMinutes }
    val completionPercent = if (totalTasks > 0) ((completedTasks.toFloat() / totalTasks) * 100).toInt() else 0

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Planner Header Card
        item {
            GradientCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = listOf(BrandForestGreen, Color(0xFF234417))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "📅 Daily Study Planner",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandWarmCream
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = todayFormatted,
                        fontSize = 12.sp,
                        color = BrandCreamDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearSyllabusBar(
                        progress = completionPercent / 100f,
                        height = 6.dp,
                        barColor = BrandTerracotta,
                        backgroundColor = Color.White.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Completion", fontSize = 11.sp, color = BrandCreamDark)
                            Text("$completionPercent%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandWarmCream)
                        }
                        Column {
                            Text("Tasks Done", fontSize = 11.sp, color = BrandCreamDark)
                            Text("$completedTasks of $totalTasks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandWarmCream)
                        }
                        Column {
                            Text("Planned Time", fontSize = 11.sp, color = BrandCreamDark)
                            Text("${plannedMins}m", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandWarmCream)
                        }
                        Column {
                            Text("Actual Logged", fontSize = 11.sp, color = BrandCreamDark)
                            Text("${actualMins}m", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandTerracottaLight)
                        }
                    }
                }
            }
        }

        // Action Bar: Add study task
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Study Schedule",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { showAddPlanDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task")
                }
            }
        }

        // Empty state or Task list
        if (todayPlans.isEmpty()) {
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
                        Icon(Icons.Default.EventNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No study sessions planned for today", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Plan your study blocks to stay disciplined and structured.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAddPlanDialog = true }) {
                            Text("+ Plan Today's Study")
                        }
                    }
                }
            }
        } else {
            items(todayPlans, key = { it.id }) { plan ->
                val sub = subjects.find { it.id == plan.subjectId }
                val subColor = try { Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#2D4F1E")) } catch (e: Exception) { BrandForestGreen }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = 2.dp,
                    accentColor = if (plan.isCompleted) StatusCompleted else subColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = plan.isCompleted,
                            onCheckedChange = { plannerViewModel.togglePlanCompleted(plan) },
                            colors = CheckboxDefaults.colors(checkedColor = StatusCompleted)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(subColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = plan.subjectName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = subColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${plan.timeStr} • ${plan.plannedMinutes} mins",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = plan.chapterTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (plan.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )

                            if (plan.goalNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = plan.goalNotes,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    timerViewModel.setTimerTargetById(sub?.id, items.find { it.title == plan.chapterTitle }?.id)
                                    mainViewModel.navigateTo(NavDestination.TIMER)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = "Start Timer", tint = BrandTerracotta, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { plannerViewModel.deletePlan(plan) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Plan Dialog
    if (showAddPlanDialog) {
        val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
        var planDateStr by remember { mutableStateOf(today) }
        val currentSub = subjects.find { it.id == selectedSubjectId }

        AlertDialog(
            onDismissRequest = { showAddPlanDialog = false },
            title = { Text("Add Study Session Block") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Subject", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    var subExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { subExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(currentSub?.name ?: "Select Subject")
                        }
                        DropdownMenu(expanded = subExpanded, onDismissRequest = { subExpanded = false }) {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        selectedSubjectId = s.id
                                        subExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = planChapterTitle,
                        onValueChange = { planChapterTitle = it },
                        label = { Text("Chapter (e.g. Algebra)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    val context = LocalContext.current
                    val calendar = Calendar.getInstance()
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.parse(planDateStr)?.let {
                            calendar.time = it
                        }
                    } catch (e: Exception) {}

                    val datePickerDialog = remember {
                        android.app.DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                planDateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = planDateStr,
                            onValueChange = { planDateStr = it },
                            label = { Text("Planned Date (YYYY-MM-DD)") },
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
                            value = planTime,
                            onValueChange = { planTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = plannedMinutes.toString(),
                            onValueChange = { plannedMinutes = it.toIntOrNull() ?: 60 },
                            label = { Text("Duration (mins)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = planNotes,
                        onValueChange = { planNotes = it },
                        label = { Text("Goal / Practice Target") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (planChapterTitle.isNotBlank()) {
                            plannerViewModel.addStudyPlan(
                                dateStr = planDateStr,
                                timeStr = planTime,
                                subjectId = selectedSubjectId,
                                subjectName = currentSub?.name ?: "Subject",
                                chapterTitle = planChapterTitle.trim(),
                                plannedMinutes = plannedMinutes,
                                notes = planNotes.trim()
                            )
                            planChapterTitle = ""
                            showAddPlanDialog = false
                        }
                    },
                    enabled = planChapterTitle.isNotBlank()
                ) {
                    Text("Add to Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlanDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
