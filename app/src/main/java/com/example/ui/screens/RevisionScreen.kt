package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SyllabusItem
import com.example.data.model.ChapterStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.TimerViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.SyllabusViewModel
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class RevisionTab(val label: String) {
    DUE_TODAY("Due Today"),
    OVERDUE("Overdue"),
    UPCOMING("Upcoming (7 Days)"),
    RECENTLY_REVISED("Recently Revised"),
    WEAK_NEEDS_REVIEW("Weak Chapters")
}

@Composable
fun RevisionScreen(
    onNavigate: (NavDestination) -> Unit
) {
    val subjectViewModel: SubjectViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val syllabusViewModel: SyllabusViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()

    val revisionState by syllabusViewModel.revisionState.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()
    val appSettings by settingsViewModel.appSettings.collectAsState()

    var selectedTab by remember { mutableStateOf(RevisionTab.DUE_TODAY) }

    val subjectsMap = remember(subjects) { subjects.associateBy { it.id } }

    val activeList = when (selectedTab) {
        RevisionTab.DUE_TODAY -> revisionState.dueTodayList
        RevisionTab.OVERDUE -> revisionState.overdueList
        RevisionTab.UPCOMING -> revisionState.upcomingList
        RevisionTab.RECENTLY_REVISED -> revisionState.recentlyRevisedList
        RevisionTab.WEAK_NEEDS_REVIEW -> revisionState.weakList
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val shortDateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Revision Banner
        item {
            GradientCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = listOf(Color(0xFF6A1B9A), Color(0xFF4A148C))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🔄 Smart Revision Center",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandWarmCream
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Spaced Repetition: ${appSettings.revisionIntervals.joinToString(" → ")} days",
                                fontSize = 12.sp,
                                color = BrandCreamDark
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${revisionState.dueTodayList.size + revisionState.overdueList.size}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandWarmCream
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Due Today", fontSize = 10.sp, color = BrandCreamDark)
                            Text("${revisionState.dueTodayList.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandWarmCream)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Overdue", fontSize = 10.sp, color = BrandCreamDark)
                            Text("${revisionState.overdueList.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusWeak)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Upcoming", fontSize = 10.sp, color = BrandCreamDark)
                            Text("${revisionState.upcomingList.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandWarmCream)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Revised", fontSize = 10.sp, color = BrandCreamDark)
                            Text("${revisionState.recentlyRevisedList.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusCompleted)
                        }
                    }
                }
            }
        }

        // Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                RevisionTab.values().forEach { tab ->
                    val count = when (tab) {
                        RevisionTab.DUE_TODAY -> revisionState.dueTodayList.size
                        RevisionTab.OVERDUE -> revisionState.overdueList.size
                        RevisionTab.UPCOMING -> revisionState.upcomingList.size
                        RevisionTab.RECENTLY_REVISED -> revisionState.recentlyRevisedList.size
                        RevisionTab.WEAK_NEEDS_REVIEW -> revisionState.weakList.size
                    }
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = "${tab.label} ($count)",
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }

        // List of items
        if (activeList.isEmpty()) {
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
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = StatusCompleted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == RevisionTab.DUE_TODAY) "No revisions due today! 🎉" else "No chapters in this category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You're all caught up with your spaced repetition schedule.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(activeList, key = { it.id }) { item ->
                val sub = subjectsMap[item.subjectId]
                val subColor = try { Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#2D4F1E")) } catch (e: Exception) { BrandForestGreen }

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syllabusViewModel.selectChapter(item) },
                    shape = RoundedCornerShape(14.dp),
                    elevation = 2.dp,
                    accentColor = if (item.isWeak) StatusWeak else StatusRevisionDue
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(subColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sub?.name ?: "Subject",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = subColor
                                )
                            }

                            Text(
                                text = "Revision #${item.revisionCount + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusRevisionDue
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                if (item.nextRevisionTimestamp != null) {
                                    Text(
                                        text = "Target Date: ${shortDateFormat.format(Date(item.nextRevisionTimestamp))}",
                                        fontSize = 11.sp,
                                        color = if (item.nextRevisionTimestamp < System.currentTimeMillis()) StatusWeak else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (item.lastStudiedTimestamp != null) {
                                    Text(
                                        text = "Last Revised: ${shortDateFormat.format(Date(item.lastStudiedTimestamp))}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        timerViewModel.setTimerTargetById(sub?.id, item.id)
                                        mainViewModel.navigateTo(NavDestination.TIMER)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pomodoro", fontSize = 10.sp)
                                }

                                Text(
                                    text = "Log & Schedule Next:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val intervals = listOf(1, 3, 7, 15, 30)
                                    intervals.forEach { days ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (days) {
                                                        1 -> Color(0xFF9C27B0).copy(alpha = 0.12f)
                                                        3 -> Color(0xFF3F51B5).copy(alpha = 0.12f)
                                                        7 -> Color(0xFF00BCD4).copy(alpha = 0.12f)
                                                        15 -> Color(0xFF009688).copy(alpha = 0.12f)
                                                        else -> Color(0xFF4CAF50).copy(alpha = 0.12f)
                                                    }
                                                )
                                                .clickable {
                                                    syllabusViewModel.completeRevision(item, days)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${days}d",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when (days) {
                                                    1 -> Color(0xFF9C27B0)
                                                    3 -> Color(0xFF3F51B5)
                                                    7 -> Color(0xFF00BCD4)
                                                    15 -> Color(0xFF009688)
                                                    else -> Color(0xFF4CAF50)
                                                }
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
    }
}
