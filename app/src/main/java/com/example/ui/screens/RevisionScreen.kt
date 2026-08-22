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
import androidx.compose.ui.graphics.luminance
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

    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f

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
                shape = RoundedCornerShape(22.dp),
                colors = if (isDark) listOf(Color(0xFF1E1B38), Color(0xFF151426)) else listOf(Color(0xFFEDE9FE), Color(0xFFF5F3FF)),
                borderColor = Color(0xFFA78BFA).copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🔄 Smart Revision Engine",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Spaced Repetition: ${appSettings.revisionIntervals.joinToString(" → ")} days",
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFA78BFA).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${revisionState.dueTodayList.size + revisionState.overdueList.size}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFA78BFA)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color.Black.copy(alpha = 0.35f) else colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Due Today", fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                            Text("${revisionState.dueTodayList.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Overdue", fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                            Text("${revisionState.overdueList.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Upcoming", fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                            Text("${revisionState.upcomingList.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Revised", fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                            Text("${revisionState.recentlyRevisedList.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SoftMint)
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
                contentColor = ElectricBlue,
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
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == tab) ElectricBlue else colorScheme.onSurfaceVariant
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
                    shape = RoundedCornerShape(20.dp)
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
                            tint = SoftMint,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == RevisionTab.DUE_TODAY) "No revisions due today! 🎉" else "No chapters in this category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You're all caught up with your spaced repetition schedule.",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(activeList, key = { it.id }) { item ->
                val sub = subjectsMap[item.subjectId]
                val subColor = try { Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#6EC2FD")) } catch (e: Exception) { ElectricBlue }

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syllabusViewModel.selectChapter(item) },
                    shape = RoundedCornerShape(18.dp),
                    accentColor = if (item.isWeak) AlertRed else Color(0xFFA78BFA)
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
                                color = Color(0xFFA78BFA)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
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
                                        text = "Target: ${shortDateFormat.format(Date(item.nextRevisionTimestamp))}",
                                        fontSize = 11.sp,
                                        color = if (item.nextRevisionTimestamp < System.currentTimeMillis()) AlertRed else colorScheme.onSurfaceVariant
                                    )
                                }
                                if (item.lastStudiedTimestamp != null) {
                                    Text(
                                        text = "Last: ${shortDateFormat.format(Date(item.lastStudiedTimestamp))}",
                                        fontSize = 10.sp,
                                        color = colorScheme.onSurfaceVariant
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
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pomodoro", fontSize = 10.sp)
                                }

                                Text(
                                    text = "Log & Next:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurfaceVariant
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
                                                        1 -> Color(0xFFA78BFA).copy(alpha = 0.18f)
                                                        3 -> ElectricBlue.copy(alpha = 0.18f)
                                                        7 -> SoftMint.copy(alpha = 0.18f)
                                                        15 -> Color(0xFFF59E0B).copy(alpha = 0.18f)
                                                        else -> Color(0xFF10B981).copy(alpha = 0.18f)
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
                                                    1 -> Color(0xFFA78BFA)
                                                    3 -> ElectricBlue
                                                    7 -> SoftMint
                                                    15 -> Color(0xFFF59E0B)
                                                    else -> Color(0xFF10B981)
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
