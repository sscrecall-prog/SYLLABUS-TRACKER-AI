package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockTest
import com.example.data.model.MockTestType
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Subject Performance stats for Full Length Mocks
 */
data class FullSubjectStat(
    val subjectName: String,
    val shortName: String,
    val averageScore: Float,
    val maxMarks: Float,
    val averageAccuracy: Float,
    val bestScore: Float,
    val latestScore: Float,
    val color: Color,
    val icon: String
)

enum class FullTrendInterpretation(val label: String, val color: Color, val icon: ImageVector, val description: String) {
    IMPROVING("Improving Trend", SoftMint, Icons.Default.TrendingUp, "Scores are consistently progressing upward compared to initial attempts."),
    STABLE("Stable Performance", ElectricBlue, Icons.Default.TrendingFlat, "Scores have remained steady within a consistent range."),
    DECLINING("Needs Attention", AlertRed, Icons.Default.TrendingDown, "Recent mock scores show a slight dip. Focus on weak area revisions.")
}

/**
 * Calculated Full-Length Analysis Dataset
 */
class FullLengthAnalytics(
    val fullMocks: List<MockTest>
) {
    // Chronological (oldest to newest) for trend charts
    val chronological: List<MockTest> = fullMocks.sortedBy { it.timestamp }
    // Newest first for latest metrics and history
    val newestFirst: List<MockTest> = fullMocks.sortedByDescending { it.timestamp }

    val totalCount: Int = fullMocks.size
    val latestMock: MockTest? = newestFirst.firstOrNull()
    val firstMock: MockTest? = chronological.firstOrNull()

    // 1. KPI Stats
    val averageScore: Float = if (fullMocks.isNotEmpty()) fullMocks.map { it.marksScored }.average().toFloat() else 0f
    val bestScore: Float = fullMocks.maxOfOrNull { it.marksScored } ?: 0f
    val latestScore: Float = latestMock?.marksScored ?: 0f
    val firstScore: Float = firstMock?.marksScored ?: 0f

    val averageAccuracy: Float = if (fullMocks.isNotEmpty()) fullMocks.map { it.accuracy }.average().toFloat() else 0f
    val latestAccuracy: Float = latestMock?.accuracy ?: 0f
    val averagePercentile: Float = if (fullMocks.isNotEmpty()) fullMocks.map { it.percentile }.average().toFloat() else 0f
    val bestPercentile: Float = fullMocks.maxOfOrNull { it.percentile } ?: 0f
    val latestPercentile: Float = latestMock?.percentile ?: 0f

    val bestRank: Int = fullMocks.filter { it.rank > 0 }.minOfOrNull { it.rank } ?: 0
    val latestRank: Int = latestMock?.rank ?: 0
    val latestTotalStudents: Int = latestMock?.totalStudents ?: 0

    // Personal Baseline Comparisons (Current Mock vs Historical Baseline of all previous Full Length Mocks)
    val previousFullMocks: List<MockTest> = if (chronological.size > 1) chronological.dropLast(1) else emptyList()
    val hasHistoricalBaseline: Boolean = previousFullMocks.isNotEmpty()

    // Baseline Average Score derived from previous Full Length mocks (or overall average if 1 mock)
    val baselineAverageScore: Float = if (previousFullMocks.isNotEmpty()) {
        previousFullMocks.map { it.marksScored }.average().toFloat()
    } else averageScore

    // Baseline Average Accuracy derived from previous Full Length mocks (or overall average if 1 mock)
    val baselineAverageAccuracy: Float = if (previousFullMocks.isNotEmpty()) {
        previousFullMocks.map { it.accuracy }.average().toFloat()
    } else averageAccuracy

    // Score comparison vs baseline
    val scoreDeltaFromBaseline: Float = if (hasHistoricalBaseline) latestScore - baselineAverageScore else 0f
    val scorePercentChangeFromBaseline: Float = if (hasHistoricalBaseline && baselineAverageScore > 0) {
        ((latestScore - baselineAverageScore) / baselineAverageScore) * 100f
    } else 0f
    val isScoreAboveBaseline: Boolean = scoreDeltaFromBaseline >= 0f

    // Accuracy comparison vs baseline
    val accuracyDeltaFromBaseline: Float = if (hasHistoricalBaseline) latestAccuracy - baselineAverageAccuracy else 0f
    val isAccuracyAboveBaseline: Boolean = accuracyDeltaFromBaseline >= 0f

    // Cutoff Stats & Margins
    val latestCutoff: Float = latestMock?.cutoffMarks ?: 135f
    val latestDifference: Float = latestScore - latestCutoff
    val isLatestCleared: Boolean = latestMock?.let { it.marksScored >= it.cutoffMarks } ?: false
    val clearedCount: Int = fullMocks.count { it.isClearedCutoff }
    val notClearedCount: Int = totalCount - clearedCount
    val cutoffClearancePercent: Int = if (totalCount > 0) ((clearedCount.toFloat() / totalCount.toFloat()) * 100).toInt() else 0

    // Advanced Margin Calculations
    val averageCutoff: Float = if (fullMocks.isNotEmpty()) fullMocks.map { it.cutoffMarks }.average().toFloat() else 135f
    val averageMargin: Float = averageScore - averageCutoff
    val bestMargin: Float = fullMocks.maxOfOrNull { it.marksScored - it.cutoffMarks } ?: 0f
    val lowestMargin: Float = fullMocks.minOfOrNull { it.marksScored - it.cutoffMarks } ?: 0f
    val clearedMocks: List<MockTest> = fullMocks.filter { it.isClearedCutoff }
    val notClearedMocks: List<MockTest> = fullMocks.filter { !it.isClearedCutoff }
    val averageLeadWhenCleared: Float = if (clearedMocks.isNotEmpty()) clearedMocks.map { it.marksScored - it.cutoffMarks }.average().toFloat() else 0f
    val averageDeficitWhenFailed: Float = if (notClearedMocks.isNotEmpty()) notClearedMocks.map { it.cutoffMarks - it.marksScored }.average().toFloat() else 0f

    // Consecutive clearance streak
    val currentStreak: Int = run {
        var streak = 0
        for (m in newestFirst) {
            if (m.isClearedCutoff) streak++ else break
        }
        streak
    }
    val bestStreak: Int = run {
        var maxS = 0
        var currentS = 0
        for (m in chronological) {
            if (m.isClearedCutoff) {
                currentS++
                if (currentS > maxS) maxS = currentS
            } else {
                currentS = 0
            }
        }
        maxS
    }

    // Subject Stats
    val mathStat: FullSubjectStat = calculateSubjectStat(
        name = "Mathematics (Quant)",
        shortName = "Math",
        scores = fullMocks.map { it.mathScore },
        maxMarks = 50f,
        color = Color(0xFF38BDF8),
        icon = "📐"
    )

    val reasoningStat: FullSubjectStat = calculateSubjectStat(
        name = "Reasoning & GI",
        shortName = "Reasoning",
        scores = fullMocks.map { it.reasoningScore },
        maxMarks = 50f,
        color = Color(0xFFF472B6),
        icon = "🧠"
    )

    val englishStat: FullSubjectStat = calculateSubjectStat(
        name = "English Comprehension",
        shortName = "English",
        scores = fullMocks.map { it.englishScore },
        maxMarks = 50f,
        color = Color(0xFFA78BFA),
        icon = "📖"
    )

    val gsStat: FullSubjectStat = calculateSubjectStat(
        name = "General Awareness / GS",
        shortName = "GS/GK",
        scores = fullMocks.map { it.gsScore },
        maxMarks = 50f,
        color = Color(0xFF6EC2FD),
        icon = "🏛️"
    )

    val allSubjectStats = listOf(mathStat, reasoningStat, englishStat, gsStat)

    val strongestSubject: FullSubjectStat? = allSubjectStats.filter { it.averageScore > 0 }
        .maxByOrNull { it.averageScore / it.maxMarks }

    val weakestSubject: FullSubjectStat? = allSubjectStats.filter { it.averageScore > 0 }
        .minByOrNull { it.averageScore / it.maxMarks }

    // Consistency Stats
    val last5Mocks: List<MockTest> = newestFirst.take(5)
    val last10Mocks: List<MockTest> = newestFirst.take(10)

    val last5AvgScore: Float = if (last5Mocks.isNotEmpty()) last5Mocks.map { it.marksScored }.average().toFloat() else 0f
    val last10AvgScore: Float = if (last10Mocks.isNotEmpty()) last10Mocks.map { it.marksScored }.average().toFloat() else 0f

    val scoreImprovement: Float = if (chronological.size >= 2) latestScore - firstScore else 0f

    val scoreVariation: Float = calculateStandardDeviation(fullMocks.map { it.marksScored })
    val accuracyConsistency: Float = calculateStandardDeviation(fullMocks.map { it.accuracy })

    val trendInterpretation: FullTrendInterpretation = when {
        totalCount < 2 -> FullTrendInterpretation.STABLE
        scoreImprovement >= 4.0f -> FullTrendInterpretation.IMPROVING
        scoreImprovement <= -4.0f -> FullTrendInterpretation.DECLINING
        else -> FullTrendInterpretation.STABLE
    }

    private fun calculateSubjectStat(
        name: String,
        shortName: String,
        scores: List<Float>,
        maxMarks: Float,
        color: Color,
        icon: String
    ): FullSubjectStat {
        val nonZeroScores = scores.filter { it > 0 }
        val avg = if (nonZeroScores.isNotEmpty()) nonZeroScores.average().toFloat() else 0f
        val best = nonZeroScores.maxOrNull() ?: 0f
        val latest = scores.firstOrNull() ?: 0f
        val accuracyPct = if (maxMarks > 0) (avg / maxMarks) * 100f else 0f

        return FullSubjectStat(
            subjectName = name,
            shortName = shortName,
            averageScore = avg,
            maxMarks = maxMarks,
            averageAccuracy = accuracyPct,
            bestScore = best,
            latestScore = latest,
            color = color,
            icon = icon
        )
    }

    private fun calculateStandardDeviation(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val mean = values.average()
        val variance = values.map { (it - mean).pow(2) }.average()
        return sqrt(variance).toFloat()
    }
}

/**
 * Dedicated Full Length Mock Analysis Content Scope for LazyColumn
 */
fun LazyListScope.fullLengthAnalysisContent(
    allMockTests: List<MockTest>,
    onAddFullMockClick: () -> Unit,
    onMockClick: (MockTest) -> Unit,
    onEditMock: (MockTest) -> Unit,
    onDeleteMock: (MockTest) -> Unit
) {
    // 10. STRICT DATA FILTERING RULE: FILTER ONLY MockTestType.FULL_LENGTH
    val fullLengthMocks = allMockTests.filter { it.testType == MockTestType.FULL_LENGTH }
    val analytics = FullLengthAnalytics(fullLengthMocks)

    if (fullLengthMocks.isEmpty()) {
        item(key = "full_length_empty") {
            FullLengthEmptyState(onAddClick = onAddFullMockClick)
        }
    } else {
        // 3. KPI CARDS SECTION (8 key metrics)
        item(key = "full_length_kpis") {
            FullLengthKpiGrid(analytics = analytics)
        }

        // 4. SCORE PERFORMANCE & PROGRESSION CHART
        item(key = "full_length_score_trend") {
            FullLengthScoreTrendCard(analytics = analytics)
        }

        // 4b. PERSONAL BASELINE COMPARISON CARD (Current Mock vs Historical Baseline Score & Accuracy)
        item(key = "full_length_baseline_comparison") {
            FullLengthBaselineComparisonCard(analytics = analytics)
        }

        // 5. PERCENTILE & RANK ANALYSIS
        item(key = "full_length_percentile_rank") {
            FullLengthPercentileRankCard(analytics = analytics)
        }

        // 6. CUTOFF ANALYSIS CARD
        item(key = "full_length_cutoff_analysis") {
            FullLengthCutoffAnalysisCard(analytics = analytics)
        }

        // 7. SUBJECT-WISE FULL MOCK ANALYSIS
        item(key = "full_length_subject_analysis") {
            FullLengthSubjectAnalysisCard(analytics = analytics)
        }

        // 8. CONSISTENCY & TREND INTERPRETATION
        item(key = "full_length_consistency_analysis") {
            FullLengthConsistencyCard(analytics = analytics)
        }

        // 9. FULL LENGTH MOCK HISTORY HEADER
        item(key = "full_length_history_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Full Length Mock History (${analytics.totalCount})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Newest First",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 9. FULL LENGTH MOCK HISTORY ROWS
        items(analytics.newestFirst, key = { "full_mock_${it.id}" }) { mock ->
            FullLengthMockHistoryCard(
                mock = mock,
                onClick = { onMockClick(mock) },
                onEdit = { onEditMock(mock) },
                onDelete = { onDeleteMock(mock) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
            )
        }
    }
}

/**
 * 15. Clean & Encouraging Empty State
 */
@Composable
fun FullLengthEmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag("full_length_empty_state"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Start Your Full Length Journey",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Attempt your first full length mock to see your score, percentile, rank and performance analysis here.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = Modifier.testTag("add_first_full_mock_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DarkBg)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Full Length Mock",
                    fontWeight = FontWeight.Bold,
                    color = DarkBg,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 3. 8-KPI Overview Cards Grid
 */
@Composable
fun FullLengthKpiGrid(
    analytics: FullLengthAnalytics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Total Mocks, Avg Score, Best Score, Avg Accuracy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FullKpiTile(
                title = "Total Full Mocks",
                value = "${analytics.totalCount}",
                subtext = "Attempts",
                accentColor = ElectricBlue,
                icon = Icons.Default.Quiz,
                modifier = Modifier.weight(1f)
            )
            FullKpiTile(
                title = "Average Score",
                value = String.format("%.1f", analytics.averageScore),
                subtext = "out of 200",
                accentColor = Color(0xFF38BDF8),
                icon = Icons.Default.Equalizer,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Best Score, Avg Accuracy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FullKpiTile(
                title = "Best Score",
                value = String.format("%.1f", analytics.bestScore),
                subtext = "Peak Marks",
                accentColor = SoftMint,
                icon = Icons.Default.EmojiEvents,
                modifier = Modifier.weight(1f)
            )
            FullKpiTile(
                title = "Avg Accuracy",
                value = "${String.format("%.1f", analytics.averageAccuracy)}%",
                subtext = "Attempted Qs",
                accentColor = Color(0xFFA78BFA),
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Avg Percentile, Best Percentile
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FullKpiTile(
                title = "Avg Percentile",
                value = if (analytics.averagePercentile > 0) "${String.format("%.1f", analytics.averagePercentile)}%" else "N/A",
                subtext = "All-India",
                accentColor = Color(0xFFF472B6),
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f)
            )
            FullKpiTile(
                title = "Best Percentile",
                value = if (analytics.bestPercentile > 0) "${String.format("%.1f", analytics.bestPercentile)}%" else "N/A",
                subtext = "Top Standing",
                accentColor = AmberGold,
                icon = Icons.Default.Stars,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: Best Rank, Cutoff Clearance %
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FullKpiTile(
                title = "Best Rank",
                value = if (analytics.bestRank > 0) "#${analytics.bestRank}" else "N/A",
                subtext = if (analytics.latestTotalStudents > 0) "in ${analytics.latestTotalStudents}" else "Overall",
                accentColor = WarningOrange,
                icon = Icons.Default.MilitaryTech,
                modifier = Modifier.weight(1f)
            )
            FullKpiTile(
                title = "Cutoff Clearance",
                value = "${analytics.cutoffClearancePercent}%",
                subtext = "${analytics.clearedCount}/${analytics.totalCount} Cleared",
                accentColor = if (analytics.cutoffClearancePercent >= 60) SoftMint else AlertRed,
                icon = Icons.Default.Verified,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FullKpiTile(
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtext,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 4. SCORE PERFORMANCE & PROGRESSION CHART (Canvas)
 */
@Composable
fun FullLengthScoreTrendCard(
    analytics: FullLengthAnalytics,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val chronological = analytics.chronological

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("full_length_score_trend_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Score Performance Trend",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "First → Latest",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score Summary Pills (Best, Avg, Latest)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreHighlightPill(
                    label = "Best Score",
                    score = "${String.format("%.1f", analytics.bestScore)}",
                    color = SoftMint,
                    modifier = Modifier.weight(1f)
                )
                ScoreHighlightPill(
                    label = "Avg Score",
                    score = "${String.format("%.1f", analytics.averageScore)}",
                    color = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
                ScoreHighlightPill(
                    label = "Latest Score",
                    score = "${String.format("%.1f", analytics.latestScore)}",
                    color = AmberGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart
            if (chronological.isNotEmpty()) {
                val scores = chronological.map { it.marksScored }
                val maxScore = max(200f, (scores.maxOrNull() ?: 200f) + 15f)
                val minScore = 0f
                val cutoffVal = chronological.map { it.cutoffMarks }.average().toFloat().coerceIn(0f, maxScore)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(chronological) {
                                detectTapGestures { offset ->
                                    val count = chronological.size
                                    if (count > 0) {
                                        val stepX = size.width / if (count > 1) (count - 1) else 1
                                        val clickedIdx = (offset.x / stepX).toInt().coerceIn(0, count - 1)
                                        selectedIndex = clickedIdx
                                    }
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val paddingBottom = 26.dp.toPx()
                        val paddingTop = 12.dp.toPx()
                        val usableHeight = h - paddingBottom - paddingTop

                        // Horizontal reference lines (e.g. 50, 100, 150, 200)
                        val refSteps = listOf(50f, 100f, 150f, 200f)
                        refSteps.forEach { ref ->
                            if (ref <= maxScore) {
                                val yRef = paddingTop + usableHeight * (1f - (ref - minScore) / (maxScore - minScore))
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.15f),
                                    start = Offset(0f, yRef),
                                    end = Offset(w, yRef),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }

                        // Cutoff reference dashed line
                        val yCutoff = paddingTop + usableHeight * (1f - (cutoffVal - minScore) / (maxScore - minScore))
                        drawLine(
                            color = AlertRed.copy(alpha = 0.5f),
                            start = Offset(0f, yCutoff),
                            end = Offset(w, yCutoff),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )

                        // Calculate points
                        val points = mutableListOf<Offset>()
                        val count = chronological.size
                        for (i in chronological.indices) {
                            val x = if (count > 1) (w / (count - 1)) * i else w / 2f
                            val y = paddingTop + usableHeight * (1f - (chronological[i].marksScored - minScore) / (maxScore - minScore))
                            points.add(Offset(x, y))
                        }

                        // Draw smooth fill area under curve
                        if (points.size >= 2) {
                            val fillPath = Path()
                            fillPath.moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val midX = (prev.x + curr.x) / 2
                                fillPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                            fillPath.lineTo(points.last().x, h - paddingBottom)
                            fillPath.lineTo(points.first().x, h - paddingBottom)
                            fillPath.close()

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        ElectricBlue.copy(alpha = 0.35f),
                                        ElectricBlue.copy(alpha = 0.02f)
                                    ),
                                    startY = paddingTop,
                                    endY = h - paddingBottom
                                )
                            )

                            // Stroke line
                            val strokePath = Path()
                            strokePath.moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val midX = (prev.x + curr.x) / 2
                                strokePath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                            drawPath(
                                path = strokePath,
                                color = ElectricBlue,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Draw points
                        points.forEachIndexed { idx, pt ->
                            val isSelected = selectedIndex == idx
                            val pointColor = if (chronological[idx].isClearedCutoff) SoftMint else AlertRed

                            drawCircle(
                                color = if (isSelected) ElectricBlue else pointColor,
                                radius = if (isSelected) 7.dp.toPx() else 4.5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = DarkBg,
                                radius = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }

                // Selected point info overlay
                selectedIndex?.let { idx ->
                    if (idx in chronological.indices) {
                        val mock = chronological[idx]
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Mock #${idx + 1}: ${mock.testName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${mock.testDateStr} • ${mock.testPlatform}",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${mock.marksScored} / ${mock.totalMarks.toInt()}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (mock.isClearedCutoff) SoftMint else AlertRed
                                    )
                                    val diff = mock.marksScored - mock.cutoffMarks
                                    Text(
                                        text = if (diff >= 0) "+${String.format("%.1f", diff)} vs Cutoff" else "${String.format("%.1f", diff)} vs Cutoff",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (diff >= 0) SoftMint else AlertRed
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

@Composable
fun ScoreHighlightPill(
    label: String,
    score: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = score,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

/**
 * 5. PERCENTILE & RANK ANALYSIS SECTION
 */
@Composable
fun FullLengthPercentileRankCard(
    analytics: FullLengthAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("full_length_percentile_rank_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Percentile & All-India Rank",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Full Length Only",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Card Mini Grid for Percentile & Rank
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PercentileRankTile(
                    title = "Best Percentile",
                    value = if (analytics.bestPercentile > 0) "${String.format("%.1f", analytics.bestPercentile)}%" else "N/A",
                    accentColor = SoftMint,
                    modifier = Modifier.weight(1f)
                )
                PercentileRankTile(
                    title = "Latest Percentile",
                    value = if (analytics.latestPercentile > 0) "${String.format("%.1f", analytics.latestPercentile)}%" else "N/A",
                    accentColor = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PercentileRankTile(
                    title = "Best AIR Rank",
                    value = if (analytics.bestRank > 0) "#${analytics.bestRank}" else "N/A",
                    accentColor = AmberGold,
                    modifier = Modifier.weight(1f)
                )
                PercentileRankTile(
                    title = "Latest AIR Rank",
                    value = if (analytics.latestRank > 0) "#${analytics.latestRank}" else "N/A",
                    accentColor = Color(0xFFF472B6),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Percentile Progression Timeline Bar
            val percentiles = analytics.chronological.map { it.percentile }.filter { it > 0 }
            if (percentiles.isNotEmpty()) {
                Text(
                    text = "Percentile Trend (${percentiles.size} tests recorded)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(analytics.chronological.filter { it.percentile > 0 }) { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${String.format("%.1f", item.percentile)}%",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.percentile >= 90f) SoftMint else if (item.percentile >= 75f) ElectricBlue else AmberGold
                                )
                                Text(
                                    text = item.testDateStr.takeLast(5),
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PercentileRankTile(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.1f))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

/**
 * PERSONAL BASELINE COMPARISON CARD
 * Compares current mock performance against historical 'Average Score' and 'Average Accuracy'
 * derived from all previous Full Length Mocks to show if user is trending above or below baseline.
 */
@Composable
fun FullLengthBaselineComparisonCard(
    analytics: FullLengthAnalytics,
    modifier: Modifier = Modifier
) {
    val hasBaseline = analytics.hasHistoricalBaseline
    val scoreDelta = analytics.scoreDeltaFromBaseline
    val isScoreAbove = analytics.isScoreAboveBaseline
    val accDelta = analytics.accuracyDeltaFromBaseline
    val isAccAbove = analytics.isAccuracyAboveBaseline

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("full_length_baseline_comparison_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isScoreAbove) SoftMint.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isScoreAbove) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isScoreAbove) SoftMint else AlertRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Personal Baseline Comparison",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (hasBaseline) "Current mock vs Avg of prior ${analytics.previousFullMocks.size} full mocks" else "First full mock (Baseline established)",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (!hasBaseline) ElectricBlue.copy(alpha = 0.15f)
                            else if (scoreDelta >= 0) SoftMint.copy(alpha = 0.15f)
                            else AlertRed.copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.2.dp,
                        if (!hasBaseline) ElectricBlue.copy(alpha = 0.5f)
                        else if (scoreDelta >= 0) SoftMint.copy(alpha = 0.5f)
                        else AlertRed.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (!hasBaseline) Icons.Default.Flag
                                         else if (scoreDelta >= 0) Icons.Default.NorthEast
                                         else Icons.Default.SouthEast,
                            contentDescription = null,
                            tint = if (!hasBaseline) ElectricBlue else if (scoreDelta >= 0) SoftMint else AlertRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (!hasBaseline) "BASELINE SET"
                                   else if (scoreDelta >= 0) "ABOVE BASELINE"
                                   else "BELOW BASELINE",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (!hasBaseline) ElectricBlue else if (scoreDelta >= 0) SoftMint else AlertRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dual Baseline Metric Comparison Columns: Score vs Baseline & Accuracy vs Baseline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Column 1: Average Score vs Baseline
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        1.dp,
                        if (isScoreAbove) SoftMint.copy(alpha = 0.35f) else AlertRed.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Score vs Baseline",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isScoreAbove) SoftMint.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (!hasBaseline) "Base"
                                           else if (scoreDelta >= 0) "+${String.format("%.1f", scoreDelta)}"
                                           else String.format("%.1f", scoreDelta),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isScoreAbove) SoftMint else AlertRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Current Mock",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${analytics.latestScore}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Historical Baseline",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format("%.1f", analytics.baselineAverageScore),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Visual Comparison Indicator Bar
                        val scoreRatio = if (analytics.baselineAverageScore > 0) {
                            (analytics.latestScore / (analytics.baselineAverageScore * 1.25f)).coerceIn(0f, 1f)
                        } else 0.5f

                        LinearProgressIndicator(
                            progress = { scoreRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isScoreAbove) SoftMint else AlertRed,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (!hasBaseline) "Initial baseline benchmark"
                                   else if (isScoreAbove) "▲ Trending above personal avg"
                                   else "▼ Below personal score avg",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isScoreAbove) SoftMint else AlertRed
                        )
                    }
                }

                // Column 2: Average Accuracy vs Baseline
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        1.dp,
                        if (isAccAbove) SoftMint.copy(alpha = 0.35f) else AlertRed.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Accuracy vs Baseline",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isAccAbove) SoftMint.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (!hasBaseline) "Base"
                                           else if (accDelta >= 0) "+${String.format("%.1f", accDelta)}%"
                                           else "${String.format("%.1f", accDelta)}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isAccAbove) SoftMint else AlertRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Current Mock",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%.1f", analytics.latestAccuracy)}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Historical Baseline",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%.1f", analytics.baselineAverageAccuracy)}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val accRatio = (analytics.latestAccuracy / 100f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { accRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isAccAbove) SoftMint else AlertRed,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (!hasBaseline) "Initial baseline benchmark"
                                   else if (isAccAbove) "▲ Accuracy higher than avg"
                                   else "▼ Accuracy below avg",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isAccAbove) SoftMint else AlertRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject-Level Baseline Matrix (Math, Reasoning, English, GS)
            if (analytics.allSubjectStats.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Subject Performance vs Baseline Average",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            analytics.allSubjectStats.forEach { subj ->
                                val subjDelta = subj.latestScore - subj.averageScore
                                val isSubjAbove = subjDelta >= 0f

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSubjAbove) SoftMint.copy(alpha = 0.3f) else AlertRed.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = subj.shortName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${subj.latestScore.toInt()}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (subjDelta >= 0) "+${String.format("%.1f", subjDelta)}" else String.format("%.1f", subjDelta),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSubjAbove) SoftMint else AlertRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Coaching Baseline Insight
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (!hasBaseline) ElectricBlue.copy(alpha = 0.08f)
                        else if (isScoreAbove && isAccAbove) SoftMint.copy(alpha = 0.08f)
                        else if (isScoreAbove) ElectricBlue.copy(alpha = 0.08f)
                        else AlertRed.copy(alpha = 0.08f),
                border = BorderStroke(
                    1.dp,
                    if (!hasBaseline) ElectricBlue.copy(alpha = 0.25f)
                    else if (isScoreAbove && isAccAbove) SoftMint.copy(alpha = 0.25f)
                    else if (isScoreAbove) ElectricBlue.copy(alpha = 0.25f)
                    else AlertRed.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (!hasBaseline) "🏁"
                               else if (isScoreAbove && isAccAbove) "🚀"
                               else if (isScoreAbove) "📈"
                               else "💡",
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!hasBaseline) {
                            "First Full Length Mock recorded! Subsequent mocks will automatically compare against your established baseline of ${analytics.latestScore} marks and ${String.format("%.1f", analytics.latestAccuracy)}% accuracy."
                        } else if (isScoreAbove && isAccAbove) {
                            "Outstanding form! You are beating your personal baseline by +${String.format("%.1f", scoreDelta)} marks with a +${String.format("%.1f", accDelta)}% accuracy boost."
                        } else if (isScoreAbove) {
                            "Score is trending +${String.format("%.1f", scoreDelta)} marks above your personal baseline. Focus on accuracy (${String.format("%.1f", analytics.latestAccuracy)}% vs ${String.format("%.1f", analytics.baselineAverageAccuracy)}% avg) to lock in gains."
                        } else {
                            "Current mock is ${String.format("%.1f", -scoreDelta)} marks below your historical baseline average of ${String.format("%.1f", analytics.baselineAverageScore)}. Review mistake notebook questions to bounce back!"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * 6. DEDICATED FULL-LENGTH CUTOFF ANALYSIS CARD
 * Computes and displays performance relative to cutoff scores (Cleared/Not Cleared status and margin)
 */
@Composable
fun FullLengthCutoffAnalysisCard(
    analytics: FullLengthAnalytics,
    modifier: Modifier = Modifier
) {
    val diff = analytics.latestDifference
    val isCleared = analytics.isLatestCleared
    var showCutoffInfoDialog by remember { mutableStateOf(false) }

    // Explanation Dialog for Cutoff & Margin Calculation
    if (showCutoffInfoDialog) {
        AlertDialog(
            onDismissRequest = { showCutoffInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cutoff & Margin Explained",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "How 'Marks Above/Below Cutoff' is calculated based on your score vs. the set cutoff:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Formula Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SCORE MARGIN FORMULA",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Margin = Your Score − Set Cutoff Marks",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Cleared explanation
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SoftMint,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Column {
                            Text(
                                text = "Cleared Status (Green Indicator)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftMint
                            )
                            Text(
                                text = "When your score is ≥ set cutoff. A positive margin (e.g. +14.5) indicates your safety lead over the cutoff.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Not Cleared explanation
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = AlertRed,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Column {
                            Text(
                                text = "Not Cleared Status (Red Indicator)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                            Text(
                                text = "When your score is < set cutoff. A negative margin (e.g. -6.0) indicates the deficit marks required to reach the cutoff.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberGold.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Tip: You can adjust the Target Cutoff for each mock test to reflect category-wise or year-specific SSC CGL cutoffs.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp),
                            lineHeight = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showCutoffInfoDialog = false },
                    modifier = Modifier.testTag("dismiss_cutoff_info_dialog")
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold, color = ElectricBlue)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("full_length_cutoff_analysis_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Clearance Badge & Info Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isCleared) SoftMint.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCleared) Icons.Default.Verified else Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isCleared) SoftMint else AlertRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cutoff & Margin Analysis",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Info Icon to view calculation explanation
                            IconButton(
                                onClick = { showCutoffInfoDialog = true },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("cutoff_calculation_info_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Explain Cutoff Calculation",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "Performance relative to Tier 1 Cutoffs",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCleared) SoftMint.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.2.dp, if (isCleared) SoftMint.copy(alpha = 0.6f) else AlertRed.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCleared) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isCleared) SoftMint else AlertRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCleared) "CLEARED" else "NOT CLEARED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCleared) SoftMint else AlertRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Latest Mock Cutoff Comparison Showcase Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, if (isCleared) SoftMint.copy(alpha = 0.3f) else AlertRed.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Latest Mock: ${analytics.latestMock?.testName ?: "Full Mock"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = analytics.latestMock?.testDateStr ?: "",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Score Achieved",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${analytics.latestScore}",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Cutoff Threshold",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${analytics.latestCutoff.toInt()}",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (diff >= 0) "Clearance Margin" else "Deficit Margin",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (diff >= 0) SoftMint else AlertRed
                            )
                            Text(
                                text = if (diff >= 0) "+${String.format("%.1f", diff)}" else String.format("%.1f", diff),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = if (diff >= 0) SoftMint else AlertRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // LAST 5 FULL LENGTH MOCKS STATUS SUMMARY ROW WITH 5-CIRCLE SEQUENCE INDICATORS
            val last5 = analytics.last5Mocks
            val last5ClearedCount = last5.count { it.isClearedCutoff }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("last_5_full_mocks_status_summary_row")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Last 5 Mocks Sequence Status",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (last5ClearedCount >= (last5.size / 2.0).toInt().coerceAtLeast(1)) SoftMint.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (last5ClearedCount >= (last5.size / 2.0).toInt().coerceAtLeast(1)) SoftMint.copy(alpha = 0.4f) else AlertRed.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "$last5ClearedCount/${last5.size} Cleared",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (last5ClearedCount >= (last5.size / 2.0).toInt().coerceAtLeast(1)) SoftMint else AlertRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sequence of 5 Small Colored Circles (Green for Cleared, Red for Not Cleared)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Chronological sequence (or newest-first sequence)
                        last5.forEachIndexed { index, mock ->
                            val cleared = mock.isClearedCutoff
                            val margin = mock.marksScored - mock.cutoffMarks
                            val indicatorColor = if (cleared) SoftMint else AlertRed

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = indicatorColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.2.dp, indicatorColor.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("recent_mock_status_indicator_$index")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Colored Circle (Green for Cleared, Red for Not Cleared)
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(indicatorColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (cleared) Icons.Default.Check else Icons.Default.Close,
                                            contentDescription = if (cleared) "Cleared" else "Not Cleared",
                                            tint = DarkBg,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = if (index == 0) "M1 (Latest)" else "M${index + 1}",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )

                                    Text(
                                        text = if (margin >= 0) "+${String.format("%.0f", margin)}" else String.format("%.0f", margin),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = indicatorColor
                                    )
                                }
                            }
                        }

                        // Placeholder slots if fewer than 5 mocks attempted
                        if (last5.size < 5) {
                            for (slot in (last5.size + 1)..5) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "M$slot",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )

                                        Text(
                                            text = "—",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Key Metrics Grid (Avg Margin, Best Margin, Clearance Rate, Current Streak)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CutoffMetricPill(
                    title = "Avg Margin",
                    value = if (analytics.averageMargin >= 0) "+${String.format("%.1f", analytics.averageMargin)}" else String.format("%.1f", analytics.averageMargin),
                    subtext = "vs Cutoff",
                    accentColor = if (analytics.averageMargin >= 0) SoftMint else AlertRed,
                    icon = Icons.Default.ShowChart,
                    modifier = Modifier.weight(1f)
                )

                CutoffMetricPill(
                    title = "Peak Margin",
                    value = if (analytics.bestMargin >= 0) "+${String.format("%.1f", analytics.bestMargin)}" else String.format("%.1f", analytics.bestMargin),
                    subtext = "Best Lead",
                    accentColor = ElectricBlue,
                    icon = Icons.Default.EmojiEvents,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CutoffMetricPill(
                    title = "Success Rate",
                    value = "${analytics.cutoffClearancePercent}%",
                    subtext = "${analytics.clearedCount}/${analytics.totalCount} Mocks",
                    accentColor = if (analytics.cutoffClearancePercent >= 60) SoftMint else WarningOrange,
                    icon = Icons.Default.PieChart,
                    modifier = Modifier.weight(1f)
                )

                CutoffMetricPill(
                    title = "Clear Streak",
                    value = "${analytics.currentStreak} in a row",
                    subtext = "Best: ${analytics.bestStreak}",
                    accentColor = AmberGold,
                    icon = Icons.Default.Whatshot,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clearance Ratio Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cutoff Clearance Track Record",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${analytics.clearedCount} Cleared • ${analytics.notClearedCount} Missed",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (analytics.cutoffClearancePercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = if (analytics.cutoffClearancePercent >= 60) SoftMint else AlertRed,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lead vs Deficit Comparison Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftMint.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SoftMint.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Avg Lead when Cleared",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (analytics.clearedCount > 0) "+${String.format("%.1f", analytics.averageLeadWhenCleared)} marks" else "N/A",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftMint
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertRed.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Avg Gap when Failed",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (analytics.notClearedCount > 0) "-${String.format("%.1f", analytics.averageDeficitWhenFailed)} marks" else "0 marks",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Timeline Ribbon of Full Mocks Clearance Status
            if (analytics.chronological.isNotEmpty()) {
                Text(
                    text = "Historical Clearance Timeline (Oldest → Latest)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(analytics.chronological) { mock ->
                        val itemDiff = mock.marksScored - mock.cutoffMarks
                        val itemCleared = mock.isClearedCutoff
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (itemCleared) SoftMint.copy(alpha = 0.12f) else AlertRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, if (itemCleared) SoftMint.copy(alpha = 0.35f) else AlertRed.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (itemCleared) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (itemCleared) SoftMint else AlertRed,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (itemDiff >= 0) "+${String.format("%.1f", itemDiff)}" else String.format("%.1f", itemDiff),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (itemCleared) SoftMint else AlertRed
                                    )
                                }
                                Text(
                                    text = "${mock.marksScored.toInt()}/${mock.cutoffMarks.toInt()}",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CutoffMetricPill(
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
                Text(
                    text = subtext,
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 7. SUBJECT-WISE FULL MOCK ANALYSIS CARD
 */
@Composable
fun FullLengthSubjectAnalysisCard(
    analytics: FullLengthAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("full_length_subject_analysis_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Subject-Wise Full Mock Analysis",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Max 50/sub",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Strongest & Weakest Subject Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                analytics.strongestSubject?.let { strong ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoftMint.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, SoftMint.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏆", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Strongest", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SoftMint)
                                Text(strong.shortName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                            }
                        }
                    }
                }

                analytics.weakestSubject?.let { weak ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AlertRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Weakest", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                                Text(weak.shortName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subject Rows
            analytics.allSubjectStats.forEachIndexed { index, stat ->
                FullSubjectRow(stat = stat)
                if (index < analytics.allSubjectStats.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun FullSubjectRow(stat: FullSubjectStat) {
    val progress = (stat.averageScore / stat.maxMarks).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stat.icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stat.subjectName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "${String.format("%.1f", stat.averageScore)} / ${stat.maxMarks.toInt()}",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = stat.color
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = stat.color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Acc: ${String.format("%.1f", stat.averageAccuracy)}%",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Best: ${stat.bestScore.toInt()} • Latest: ${stat.latestScore.toInt()}",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 8. CONSISTENCY ANALYSIS SECTION
 */
@Composable
fun FullLengthConsistencyCard(
    analytics: FullLengthAnalytics,
    modifier: Modifier = Modifier
) {
    val interpretation = analytics.trendInterpretation

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("full_length_consistency_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Consistency & Improvement",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = interpretation.color.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, interpretation.color.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = interpretation.icon,
                            contentDescription = null,
                            tint = interpretation.color,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = interpretation.label,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = interpretation.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = interpretation.description,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Grid (Last 5, Last 10, Score Improvement, Score Variation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConsistencyMetricBox(
                    title = "Last 5 Mocks Avg",
                    value = if (analytics.last5AvgScore > 0) String.format("%.1f", analytics.last5AvgScore) else "N/A",
                    subtext = "${analytics.last5Mocks.size} tests",
                    accentColor = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
                ConsistencyMetricBox(
                    title = "Last 10 Mocks Avg",
                    value = if (analytics.last10AvgScore > 0) String.format("%.1f", analytics.last10AvgScore) else "N/A",
                    subtext = "${analytics.last10Mocks.size} tests",
                    accentColor = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConsistencyMetricBox(
                    title = "Score Delta",
                    value = if (analytics.chronological.size >= 2) {
                        (if (analytics.scoreImprovement >= 0) "+${String.format("%.1f", analytics.scoreImprovement)}" else String.format("%.1f", analytics.scoreImprovement))
                    } else "0.0",
                    subtext = "Latest vs 1st Mock",
                    accentColor = if (analytics.scoreImprovement >= 0) SoftMint else AlertRed,
                    modifier = Modifier.weight(1f)
                )
                ConsistencyMetricBox(
                    title = "Score Variation",
                    value = "±${String.format("%.1f", analytics.scoreVariation)}",
                    subtext = "Std Deviation",
                    accentColor = AmberGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ConsistencyMetricBox(
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = subtext,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 9. DEDICATED FULL LENGTH MOCK HISTORY ROW CARD
 */
@Composable
fun FullLengthMockHistoryCard(
    mock: MockTest,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diff = mock.marksScored - mock.cutoffMarks
    val isCleared = mock.isClearedCutoff
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("full_mock_history_card_${mock.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Platform pill + Date + More menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricBlue.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = mock.testPlatform,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = mock.testDateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Analysis") },
                            onClick = {
                                showMenu = false
                                onClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Mock") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Mock", color = AlertRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AlertRed) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = mock.testName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Score & Status Highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${mock.marksScored}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " / ${mock.totalMarks.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCleared) SoftMint.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isCleared) SoftMint.copy(alpha = 0.4f) else AlertRed.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (isCleared) "CLEARED (+${String.format("%.1f", diff)})" else "NOT CLEARED (${String.format("%.1f", diff)})",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCleared) SoftMint else AlertRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row: Accuracy, Percentile, Rank, Time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Acc: ${String.format("%.1f", mock.accuracy)}%",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (mock.percentile > 0) {
                    Text(
                        text = "Pct: ${String.format("%.1f", mock.percentile)}%",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA78BFA)
                    )
                }
                if (mock.rank > 0) {
                    Text(
                        text = "AIR #${mock.rank}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberGold
                    )
                }
                Text(
                    text = "${mock.timeTakenMinutes} mins",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
