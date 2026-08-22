package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.data.intelligence.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavDestination
import com.example.ui.viewmodel.SubjectStats
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.MockTestsViewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.TrendMetric
import com.example.ui.viewmodel.TimeRange
import com.example.ui.viewmodel.TrendDataPoint
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.SyllabusViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

@Composable
fun AnalyticsScreen(
    onNavigate: (NavDestination) -> Unit
) {
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val uiState by analyticsViewModel.uiState.collectAsState()

    val overallStats = uiState.overallStats
    val subjectStats = uiState.subjectStats
    val studySessions = uiState.studySessions
    val mockStats = uiState.mockStats
    val examPaceStats = uiState.examPaceStats
    val mockTests = uiState.mockTests
    val selectedTimeRange = uiState.selectedTimeRange
    val selectedMetric = uiState.selectedMetric
    val selectedSubjectIdForDetail = uiState.selectedSubjectIdForDetail
    val trendDataPoints = uiState.trendDataPoints
    val advAnalytics = uiState.advancedAnalytics
    val selectedAnalyticsWindow = uiState.selectedAnalyticsWindow

    var showMonthlyReviewDialog by remember { mutableStateOf(false) }

    if (showMonthlyReviewDialog && advAnalytics?.monthlyReview != null) {
        MonthlyReviewDialog(
            review = advAnalytics.monthlyReview,
            onDismiss = { showMonthlyReviewDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. HERO ANALYTICS OVERVIEW CARD
        item {
            GradientCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = listOf(BrandForestGreen, Color(0xFF162D10))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📈 Performance & Velocity",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandWarmCream
                            )
                            Text(
                                text = "Deep data insights, trends & syllabus completion trajectory",
                                fontSize = 12.sp,
                                color = BrandCreamDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMiniCard(
                            title = "Coverage",
                            value = "${overallStats.completionPercentage}%",
                            subtitle = "${overallStats.completedChapters}/${overallStats.totalChapters} Chapters",
                            icon = Icons.Default.CheckCircle,
                            iconTint = StatusCompleted,
                            modifier = Modifier.weight(1f)
                        )
                        StatMiniCard(
                            title = "Velocity",
                            value = "${String.format("%.1f", (overallStats.completedChapters.toFloat() / 7).coerceAtLeast(1.2f))}",
                            subtitle = "Chapters / week",
                            icon = Icons.Default.Speed,
                            iconTint = Color(0xFFFFB300),
                            modifier = Modifier.weight(1f)
                        )
                        StatMiniCard(
                            title = "Study Time",
                            value = "${overallStats.totalStudyMinutes / 60}h ${overallStats.totalStudyMinutes % 60}m",
                            subtitle = "Streak: ${overallStats.currentStreakDays}d 🔥",
                            icon = Icons.Default.LocalFireDepartment,
                            iconTint = BrandTerracotta,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        val isAnalyticsEmpty = overallStats.totalChapters == 0 ||
            (studySessions.isEmpty() && mockTests.isEmpty() && overallStats.completedChapters == 0 && overallStats.totalStudyMinutes == 0)

        if (isAnalyticsEmpty) {
            item {
                AnalyticsEmptyStateCard(
                    onStartTimer = { onNavigate(NavDestination.TIMER) },
                    onLogMockTest = { onNavigate(NavDestination.MOCK_TESTS) },
                    onBrowseSyllabus = { onNavigate(NavDestination.SYLLABUS) },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            // 1.5 SPRINT 5 ADVANCED ANALYTICS WINDOW SELECTOR & LONG-TERM METRICS
            if (advAnalytics != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Advanced Intelligence & Analytics",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (advAnalytics.monthlyReview != null) {
                            TextButton(
                                onClick = { showMonthlyReviewDialog = true },
                                modifier = Modifier.testTag("open_monthly_review_btn")
                            ) {
                                Text(
                                    text = "Monthly Review →",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandForestGreen
                                )
                            }
                        }
                    }

                    AdvancedAnalyticsWindowSelector(
                        selectedWindow = selectedAnalyticsWindow,
                        onWindowSelected = { analyticsViewModel.setAnalyticsWindow(it) }
                    )
                }
            }

            // Long-Term Performance Card for selected window
            item {
                val currentTrend = when (selectedAnalyticsWindow) {
                    AnalyticsTimeWindow.DAYS_7 -> advAnalytics.longTerm7D
                    AnalyticsTimeWindow.DAYS_15 -> advAnalytics.longTerm15D
                    AnalyticsTimeWindow.DAYS_30 -> advAnalytics.longTerm30D
                    AnalyticsTimeWindow.DAYS_90 -> advAnalytics.longTerm90D
                    AnalyticsTimeWindow.ALL_TIME -> advAnalytics.longTermAllTime
                }
                LongTermPerformanceCard(result = currentTrend)
            }

            // Mastery Growth Trajectory Card
            item {
                MasteryGrowthCard(growth = advAnalytics.masteryGrowth)
            }

            // Subject Health & Comparisons Ranking Card
            item {
                SubjectComparisonCard(result = advAnalytics.subjectComparisons)
            }

            // Study Consistency & Habit Adherence Card
            item {
                StudyConsistencyCard(consistency = advAnalytics.consistency)
            }

            // Meaningful Streaks & Compassionate Recovery Card
            item {
                MeaningfulStreaksCard(
                    studyStreak = advAnalytics.studyStreak,
                    revisionStreak = advAnalytics.revisionStreak
                )
            }

            // Quality-Adjusted Study Time Card
            item {
                QualityAdjustedStudyTimeCard(quality = advAnalytics.qualityStudyTime)
            }

            // Productivity Patterns & Peak Hours Card
            item {
                ProductivityPatternsCard(patterns = advAnalytics.productivityPatterns)
            }

            // Meaningful Achievements & Learning Milestones Card
            item {
                MeaningfulAchievementsCard(achievements = advAnalytics.achievements)
            }

            // Personal Records Wall (Bests)
            item {
                PersonalRecordsCard(records = advAnalytics.personalRecords)
            }
        }

        // 2. INTERACTIVE STUDY TRENDS & PROGRESS OVER TIME (D3/RECHARTS-STYLE CANVAS GRAPH)
        item {
            BentoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_trends_chart_card"),
                shape = RoundedCornerShape(20.dp),
                accentColor = MaterialTheme.colorScheme.primary
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header & Metric Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (selectedMetric == TrendMetric.STUDY_HOURS) "Study Time Trends" else "Syllabus Progress Curve",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Interactive trajectory curve with touch inspection",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Metric Switcher Chips
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedMetric == TrendMetric.STUDY_HOURS) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { analyticsViewModel.setMetric(TrendMetric.STUDY_HOURS) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Hours",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedMetric == TrendMetric.STUDY_HOURS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedMetric == TrendMetric.SYLLABUS_PROGRESS) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { analyticsViewModel.setMetric(TrendMetric.SYLLABUS_PROGRESS) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Progress %",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedMetric == TrendMetric.SYLLABUS_PROGRESS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Time Range Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeRange.values().forEach { range ->
                            val isSel = selectedTimeRange == range
                            FilterChip(
                                selected = isSel,
                                onClick = { analyticsViewModel.setTimeRange(range) },
                                label = { Text(range.label, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Smooth Area Curve Canvas Chart
                    NativeAreaTrendChart(
                        dataPoints = trendDataPoints,
                        metric = selectedMetric,
                        lineColor = if (selectedMetric == TrendMetric.STUDY_HOURS) BrandForestGreen else StatusCompleted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        // 3. COMPLETION PERCENTAGES PER SUBJECT (INTERACTIVE MULTI-SLICE DONUT & COMPARISON)
        item {
            BentoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subject_completion_donut_card"),
                shape = RoundedCornerShape(20.dp),
                accentColor = BrandForestGreen
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Subject Syllabus Distribution",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Completion percentages & chapter weightage",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Donut Chart + Legend Side-by-Side or Stacked
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Interactive Donut Chart
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SubjectCompletionDonutChart(
                                subjectStats = subjectStats,
                                selectedSubjectId = selectedSubjectIdForDetail,
                                onSelectSubject = { analyticsViewModel.setSelectedSubjectForDetail(it) },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Inner label
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val activeSubject = subjectStats.find { it.subject.id == selectedSubjectIdForDetail }
                                Text(
                                    text = if (activeSubject != null) "${activeSubject.completionPercentage}%" else "${overallStats.completionPercentage}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (activeSubject != null) activeSubject.subject.code.ifEmpty { "Selected" } else "Total",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Subject Legend Chips
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            subjectStats.take(5).forEach { stats ->
                                val col = parseColorSafe(stats.subject.colorHex)
                                val isSelected = selectedSubjectIdForDetail == stats.subject.id

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) col.copy(alpha = 0.16f) else Color.Transparent)
                                        .clickable {
                                            analyticsViewModel.setSelectedSubjectForDetail(if (isSelected) null else stats.subject.id)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(col)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stats.subject.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(
                                        text = "${stats.completionPercentage}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = col
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. COMPARATIVE SUBJECT PROGRESS & MASTERY BARS
        item {
            BentoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("comparative_bars_card"),
                shape = RoundedCornerShape(20.dp),
                accentColor = MaterialTheme.colorScheme.secondary
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Subject Mastery & Coverage Matrix",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Detailed breakdown by completed, in-progress, and weak areas",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    subjectStats.forEach { stats ->
                        val col = parseColorSafe(stats.subject.colorHex)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
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
                                            .background(col)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stats.subject.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${stats.completedChapters}/${stats.totalChapters} done (${stats.completionPercentage}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = col
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Stacked Tri-Color Progress Bar (Completed, In Progress, Left/Weak)
                            val completedFrac = (stats.completedChapters.toFloat() / stats.totalChapters.coerceAtLeast(1)).coerceIn(0f, 1f)
                            val inProgFrac = (stats.inProgressChapters.toFloat() / stats.totalChapters.coerceAtLeast(1)).coerceIn(0f, 1f)
                            val weakFrac = (stats.weakChapters.toFloat() / stats.totalChapters.coerceAtLeast(1)).coerceIn(0f, 1f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    if (completedFrac > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(completedFrac)
                                                .background(col)
                                        )
                                    }
                                    if (inProgFrac > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(inProgFrac)
                                                .background(StatusInProgress)
                                        )
                                    }
                                    if (weakFrac > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(weakFrac)
                                                .background(StatusWeak.copy(alpha = 0.8f))
                                        )
                                    }
                                    val remaining = (1f - completedFrac - inProgFrac - weakFrac).coerceAtLeast(0f)
                                    if (remaining > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(remaining)
                                                .background(Color.Transparent)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Confidence: ${String.format("%.1f", stats.averageConfidence)}/5 ★",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (stats.weakChapters > 0) "⚠️ ${stats.weakChapters} weak" else "✨ High Mastery",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (stats.weakChapters > 0) StatusWeak else StatusCompleted
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. 28-DAY STUDY CONSISTENCY HEATMAP GRID (D3-STYLE CALENDAR HEATMAP)
        item {
            BentoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_heatmap_card"),
                shape = RoundedCornerShape(20.dp),
                accentColor = BrandTerracotta
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Study Consistency Matrix",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Daily activity intensity over the last 4 weeks",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandTerracotta.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${overallStats.currentStreakDays} Day Streak 🔥",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTerracotta
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    StudyConsistencyHeatmap(
                        studySessions = studySessions,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Heatmap Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Less", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            BrandForestGreen.copy(alpha = 0.35f),
                            BrandForestGreen.copy(alpha = 0.65f),
                            BrandForestGreen,
                            BrandTerracotta
                        ).forEach { col ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(col)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("More", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 5.5 MOCK TEST & PERCENTILE ANALYTICS BENTO
        if (mockTests.isNotEmpty()) {
            item {
                BentoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(NavDestination.MOCK_TESTS) }
                        .testTag("analytics_mock_tests_bento"),
                    shape = RoundedCornerShape(20.dp),
                    accentColor = BrandForestGreen
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Quiz,
                                    contentDescription = null,
                                    tint = BrandForestGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mock Test Performance & Ranks",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandForestGreen.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Full Tracker →",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandForestGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatMiniCard(
                                title = "Avg Score",
                                value = String.format("%.1f", mockStats.averageScore),
                                subtitle = "Top: ${String.format("%.1f", mockStats.highestScore)}",
                                icon = Icons.Default.Score,
                                iconTint = BrandForestGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = "Percentile",
                                value = "${String.format("%.1f", mockStats.averagePercentile)}%",
                                subtitle = "Best: ${String.format("%.1f", mockStats.bestPercentile)}%",
                                icon = Icons.Default.AutoGraph,
                                iconTint = BrandTerracotta,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = "Cutoff Rate",
                                value = "${mockStats.cutoffClearanceRate}%",
                                subtitle = "${mockStats.clearedCutoffCount}/${mockStats.totalMocksCount} Passed",
                                icon = Icons.Default.Verified,
                                iconTint = StatusCompleted,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 6. SYLLABUS COMPLETION FORECAST & PACING PROJECTION
        item {
            BentoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("velocity_forecast_card"),
                shape = RoundedCornerShape(20.dp),
                accentColor = if (examPaceStats.isAheadOfSchedule) StatusMastered else BrandTerracotta
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (examPaceStats.isAheadOfSchedule) StatusMastered.copy(alpha = 0.15f)
                                    else BrandTerracotta.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (examPaceStats.isAheadOfSchedule) Icons.Default.EventAvailable else Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = if (examPaceStats.isAheadOfSchedule) StatusMastered else BrandTerracotta,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimated Completion Date",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (examPaceStats.isAheadOfSchedule) StatusCompleted.copy(alpha = 0.15f)
                                            else BrandTerracotta.copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${examPaceStats.daysRemaining}d to ${examPaceStats.examName}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (examPaceStats.isAheadOfSchedule) StatusCompleted else BrandTerracotta
                                    )
                                }
                            }

                            Text(
                                text = examPaceStats.estimatedCompletionDateStr,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Req: ${String.format(Locale.getDefault(), "%.1f", examPaceStats.requiredPaceChaptersPerDay)} chapters/day vs Current: ${String.format(Locale.getDefault(), "%.1f", examPaceStats.currentPaceChaptersPerDay)} chapters/day",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (examPaceStats.subjectPaceBreakdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Subject-Wise Required Velocity (${String.format(Locale.getDefault(), "%.1f", examPaceStats.weeksRemaining)} weeks left):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            examPaceStats.subjectPaceBreakdown.take(4).forEach { sub ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sub.subjectName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.1f", sub.requiredChaptersPerWeek)} chapters / week (${sub.remainingChapters} remaining)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
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

data class TrendDataPoint(
    val dateLabel: String,
    val fullDate: String,
    val value: Float,
    val unit: String
)

/**
 * Native Jetpack Compose Canvas Area Trend Chart (Recharts / D3 Style)
 * Supports smooth cubic Bézier curves, vertical gradient area fill, coordinate gridlines, and touch tooltip.
 */
@Composable
fun NativeAreaTrendChart(
    dataPoints: List<TrendDataPoint>,
    metric: TrendMetric,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints, metric) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    val maxVal = remember(dataPoints) {
        val mx = dataPoints.maxOfOrNull { it.value } ?: 10f
        if (metric == TrendMetric.SYLLABUS_PROGRESS) 100f else (ceil(mx) + 1f).coerceAtLeast(4f)
    }

    val minVal = 0f

    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(dataPoints) {
                    detectTapGestures(
                        onTap = { offset ->
                            val pointWidth = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                            val idx = ((offset.x / pointWidth) + 0.5f).toInt().coerceIn(0, dataPoints.lastIndex)
                            selectedPointIndex = if (selectedPointIndex == idx) null else idx
                        }
                    )
                }
        ) {
            val chartWidth = size.width
            val chartHeight = size.height - 30.dp.toPx()
            val pointSpacing = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

            // Draw horizontal dashed grid lines & Y-axis labels
            val gridSteps = 3
            for (i in 0..gridSteps) {
                val fraction = i / gridSteps.toFloat()
                val y = chartHeight - (fraction * chartHeight)
                val stepVal = minVal + (fraction * (maxVal - minVal))

                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.12f),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            if (dataPoints.size >= 2) {
                val path = Path()
                val fillPath = Path()

                val coords = dataPoints.mapIndexed { index, dp ->
                    val x = index * pointSpacing
                    val normalized = ((dp.value - minVal) / (maxVal - minVal).coerceAtLeast(1f)).coerceIn(0f, 1f)
                    val y = chartHeight - (normalized * chartHeight * animatedProgress.value)
                    Offset(x, y)
                }

                path.moveTo(coords[0].x, coords[0].y)
                fillPath.moveTo(coords[0].x, chartHeight)
                fillPath.lineTo(coords[0].x, coords[0].y)

                for (i in 0 until coords.size - 1) {
                    val p0 = coords[i]
                    val p1 = coords[i + 1]
                    val control1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                    val control2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                    path.cubicTo(control1.x, control1.y, control2.x, control2.y, p1.x, p1.y)
                    fillPath.cubicTo(control1.x, control1.y, control2.x, control2.y, p1.x, p1.y)
                }

                fillPath.lineTo(coords.last().x, chartHeight)
                fillPath.close()

                // Draw Gradient Area under Curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.35f * animatedProgress.value),
                            lineColor.copy(alpha = 0.02f)
                        ),
                        startY = 0f,
                        endY = chartHeight
                    )
                )

                // Draw Curve Stroke
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw Data Points
                coords.forEachIndexed { index, offset ->
                    val isSelected = selectedPointIndex == index
                    drawCircle(
                        color = surfaceColor,
                        radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = if (isSelected) BrandTerracotta else lineColor,
                        radius = if (isSelected) 5.dp.toPx() else 2.5.dp.toPx(),
                        center = offset
                    )
                }
            }

            // Draw X-axis date labels
            val labelInterval = if (dataPoints.size > 14) 5 else if (dataPoints.size > 7) 2 else 1
            dataPoints.forEachIndexed { index, dp ->
                if (index % labelInterval == 0 || index == dataPoints.lastIndex) {
                    val x = index * pointSpacing
                    val textLayout = textMeasurer.measure(
                        text = dp.dateLabel,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 9.sp,
                            color = onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            x = (x - (textLayout.size.width / 2f)).coerceIn(0f, chartWidth - textLayout.size.width),
                            y = chartHeight + 8.dp.toPx()
                        )
                    )
                }
            }
        }

        // Interactive Tooltip Overlay when a point is selected
        selectedPointIndex?.let { idx ->
            val point = dataPoints.getOrNull(idx)
            if (point != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-8).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.inverseSurface)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${point.fullDate}: ${String.format("%.1f", point.value)} ${point.unit}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

/**
 * Subject Completion Multi-Segment Donut Chart
 */
@Composable
fun SubjectCompletionDonutChart(
    subjectStats: List<SubjectStats>,
    selectedSubjectId: Long?,
    onSelectSubject: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedSweep = remember { Animatable(0f) }

    LaunchedEffect(subjectStats) {
        animatedSweep.snapTo(0f)
        animatedSweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val totalChapters = remember(subjectStats) { subjectStats.sumOf { it.totalChapters }.coerceAtLeast(1) }

    Canvas(
        modifier = modifier.pointerInput(subjectStats) {
            detectTapGestures { offset ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val touchAngle = (Math.toDegrees(atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())) + 360.0) % 360.0

                // Map touch angle to subject slice
                var currentAngle = 270.0
                var found: Long? = null
                for (stats in subjectStats) {
                    val sweep = (stats.totalChapters.toDouble() / totalChapters) * 360.0
                    val start = currentAngle % 360.0
                    val end = (currentAngle + sweep) % 360.0

                    val inside = if (start < end) {
                        touchAngle in start..end
                    } else {
                        touchAngle >= start || touchAngle <= end
                    }

                    if (inside) {
                        found = stats.subject.id
                        break
                    }
                    currentAngle += sweep
                }
                onSelectSubject(if (selectedSubjectId == found) null else found)
            }
        }
    ) {
        val strokeWidth = 16.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        var startAngle = -90f

        subjectStats.forEach { stats ->
            val color = parseColorSafe(stats.subject.colorHex)
            val isSelected = selectedSubjectId == stats.subject.id
            val sliceSweep = ((stats.totalChapters.toFloat() / totalChapters) * 360f) * animatedSweep.value

            drawArc(
                color = if (selectedSubjectId != null && !isSelected) color.copy(alpha = 0.3f) else color,
                startAngle = startAngle,
                sweepAngle = (sliceSweep - 2.5f).coerceAtLeast(1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = if (isSelected) strokeWidth * 1.25f else strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            startAngle += sliceSweep
        }
    }
}

/**
 * 28-Day Activity Consistency Heatmap Matrix
 */
@Composable
fun StudyConsistencyHeatmap(
    studySessions: List<com.example.data.model.StudySession>,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val daysData = remember(studySessions) {
        val list = mutableListOf<Pair<String, Int>>()
        for (i in 27 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dStr = dateFormat.format(cal.time)
            val mins = studySessions.filter {
                dateFormat.format(Date(it.timestamp)) == dStr
            }.sumOf { (it.durationSeconds / 60).toInt() }
            list.add(dStr to mins)
        }
        list
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val weeks = daysData.chunked(7)
        weeks.forEach { weekDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { (dateStr, mins) ->
                    val color = when {
                        mins >= 90 -> BrandTerracotta
                        mins >= 45 -> BrandForestGreen
                        mins >= 20 -> BrandForestGreen.copy(alpha = 0.65f)
                        mins > 0 -> BrandForestGreen.copy(alpha = 0.35f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

private fun parseColorSafe(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        BrandForestGreen
    }
}
