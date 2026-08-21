package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.SyllabusViewModel
import com.example.ui.viewmodel.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen() {
    val syllabusViewModel: SyllabusViewModel = viewModel()
    val plannerViewModel: PlannerViewModel = viewModel()

    val items by syllabusViewModel.items.collectAsState()
    val studySessions by syllabusViewModel.studySessions.collectAsState()
    val allPlans by plannerViewModel.allPlans.collectAsState()

    var calendarMonthOffset by remember { mutableIntStateOf(0) }
    var selectedDayStr by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    val cal = remember(calendarMonthOffset) {
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, calendarMonthOffset)
        c.set(Calendar.DAY_OF_MONTH, 1)
        c
    }

    val monthName = remember(cal) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday

    // Days grid items
    val dayCells = remember(cal, daysInMonth, firstDayOfWeek) {
        val list = mutableListOf<String?>()
        // Leading blanks (assuming week starts on Sunday)
        for (i in 1 until firstDayOfWeek) {
            list.add(null)
        }
        for (day in 1..daysInMonth) {
            val c = cal.clone() as Calendar
            c.set(Calendar.DAY_OF_MONTH, day)
            list.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time))
        }
        list
    }

    // Map study activity dates
    val studyDates = remember(studySessions) {
        studySessions.map {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
        }.toSet()
    }

    // Map planned dates
    val plannedDates = remember(allPlans) {
        allPlans.map { it.dateStr }.toSet()
    }

    // Selected Day Data
    val dayPlans = remember(selectedDayStr, allPlans) {
        allPlans.filter { it.dateStr == selectedDayStr }
    }

    val daySessions = remember(selectedDayStr, studySessions) {
        studySessions.filter {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == selectedDayStr
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Header
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { calendarMonthOffset -= 1 }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                        }

                        Text(
                            text = monthName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(onClick = { calendarMonthOffset += 1 }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day of Week Headers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { d ->
                            Text(
                                text = d,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid in fixed chunk rows
                    dayCells.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (i in 0 until 7) {
                                val dStr = week.getOrNull(i)
                                if (dStr != null) {
                                    val dayNum = dStr.split("-").last().toIntOrNull() ?: 1
                                    val isSelected = dStr == selectedDayStr
                                    val hasStudied = studyDates.contains(dStr)
                                    val hasPlan = plannedDates.contains(dStr)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else if (hasStudied) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                                else Color.Transparent
                                            )
                                            .clickable { selectedDayStr = dStr },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (hasStudied || hasPlan) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    if (hasStudied) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                                        )
                                                    }
                                                    if (hasPlan) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) BrandWarmCream else BrandTerracotta)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Details for Selected Day
        item {
            Text(
                text = "Day Schedule: $selectedDayStr",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (dayPlans.isEmpty() && daySessions.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No sessions logged for this day", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        items(dayPlans) { plan ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (plan.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (plan.isCompleted) StatusCompleted else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = plan.chapterTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${plan.subjectName} • ${plan.timeStr} • ${plan.plannedMinutes} mins", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        items(daySessions) { session ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = BrandTerracotta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = session.chapterTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${session.subjectName} • Logged ${(session.durationSeconds / 60)} mins • ${session.mode.label}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
