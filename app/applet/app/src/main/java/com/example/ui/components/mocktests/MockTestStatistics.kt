package com.example.ui.components.mocktests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockTest
import com.example.ui.theme.*
import com.example.ui.viewmodel.MockStats
import kotlin.math.max

@Composable
fun MockTestStatistics(
    mockStats: MockStats,
    mockTests: List<MockTest>,
    latestMock: MockTest?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (latestMock != null) {
            MockPerformanceHero(
                latestMock = latestMock,
                mockStats = mockStats
            )
        }

        if (mockTests.size >= 2) {
            MockScoreTrendChart(mockTests = mockTests)
        }

        MockSubjectAveragesCard(mockStats = mockStats)
    }
}

@Composable
fun MockPerformanceHero(
    latestMock: MockTest,
    mockStats: MockStats,
    modifier: Modifier = Modifier
) {
    val isCleared = latestMock.marksScored >= latestMock.cutoffMarks
    val diff = latestMock.marksScored - latestMock.cutoffMarks

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandForestGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(BrandForestGreen, BrandForestGreenLight)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandWarmCream.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "LATEST ATTEMPT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandWarmCream
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = latestMock.testPlatform,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Text(
                            text = latestMock.testDateStr,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = latestMock.testName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "MARKS SCORED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 0.5.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${latestMock.marksScored}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = " / ${latestMock.totalMarks.toInt()}",
                                    fontSize = 14.sp,
                                    color = BrandWarmCream.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                            Text(
                                text = if (isCleared) "Cutoff Cleared (+${String.format("%.1f", diff)}) ✓" else "Below Cutoff (${String.format("%.1f", diff)}) ✗",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCleared) BrandWarmCream else Color(0xFFFF8A80)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "${String.format("%.1f", latestMock.percentile)}%",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandWarmCream
                            )
                            Text(
                                text = "Percentile",
                                fontSize = 11.sp,
                                color = BrandWarmCream.copy(alpha = 0.8f)
                            )
                            if (latestMock.rank > 0 && latestMock.totalStudents > 0) {
                                Text(
                                    text = "Rank #${latestMock.rank}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Target vs Actual Score Gauge Card
        var targetScore by remember { mutableStateOf(150f) }
        val averageScore = mockStats.averageScore
        val percentOfTarget = if (targetScore > 0) (averageScore / targetScore) * 100f else 0f

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = BrandTerracotta,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Target vs Average Score",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (averageScore >= targetScore) {
                            "🏆 Brilliant! You've crossed your target! Raise the bar!"
                        } else {
                            "💡 You are just ${String.format("%.1f", targetScore - averageScore)} marks away from your target. Keep analyzing errors!"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Set Target:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        IconButton(
                            onClick = { if (targetScore > 50) targetScore -= 5 },
                            modifier = Modifier
                                .size(26.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(12.dp))
                        }
                        Text(
                            text = "${targetScore.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { if (targetScore < 200) targetScore += 5 },
                            modifier = Modifier
                                .size(26.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 10.dp.toPx()

                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        val sweepAngle = (percentOfTarget / 100f).coerceIn(0f, 1f) * 270f
                        val activeBrush = Brush.sweepGradient(
                            colors = listOf(BrandTerracotta, BrandForestGreen, BrandForestGreenLight)
                        )
                        drawArc(
                            brush = activeBrush,
                            startAngle = 135f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${String.format("%.1f", averageScore)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandForestGreen
                        )
                        Text(
                            text = "Avg / ${targetScore.toInt()}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${percentOfTarget.toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTerracotta
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 Grid Sub Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KPISubCard(
                title = "Total Mocks",
                value = "${mockStats.totalTests}",
                subtext = "Attempts Logged",
                icon = Icons.Default.Quiz,
                accentColor = BrandForestGreen,
                modifier = Modifier.weight(1f)
            )
            KPISubCard(
                title = "Avg Accuracy",
                value = "${String.format("%.1f", mockStats.averageAccuracy)}%",
                subtext = if (mockStats.averageAccuracy >= 80) "🎯 High Precision" else "⚠️ Target 85%+",
                icon = Icons.Default.PrecisionManufacturing,
                accentColor = if (mockStats.averageAccuracy >= 80) StatusCompleted else StatusWeak,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KPISubCard(
                title = "Avg Percentile",
                value = "${String.format("%.1f", mockStats.averagePercentile)}%",
                subtext = if (mockStats.averagePercentile >= 90) "🔥 Top Tier Rank" else "📈 Aim for Top 10%",
                icon = Icons.Default.Leaderboard,
                accentColor = BrandTerracotta,
                modifier = Modifier.weight(1f)
            )
            KPISubCard(
                title = "Cutoff Pass Rate",
                value = "${mockStats.testsCleared}/${mockStats.totalTests}",
                subtext = "${((mockStats.testsCleared.toFloat() / max(1, mockStats.totalTests)) * 100).toInt()}% Cutoffs Cleared",
                icon = Icons.Default.CheckCircle,
                accentColor = StatusCompleted,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KPISubCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MockScoreTrendChart(
    mockTests: List<MockTest>,
    modifier: Modifier = Modifier
) {
    var activeChartTab by remember { mutableStateOf(0) } // 0 = Scores vs Cutoff, 1 = Accuracy & Percentile

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = BrandForestGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Performance Analytics Trend",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (activeChartTab == 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrandForestGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Score", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StatusWeak)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cutoff", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrandTerracotta)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Percentile", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3F51B5))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accuracy", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Selector Tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeChartTab == 0) BrandForestGreen else Color.Transparent)
                            .clickable { activeChartTab = 0 }
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Score vs Cutoff",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeChartTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeChartTab == 1) BrandForestGreen else Color.Transparent)
                            .clickable { activeChartTab = 1 }
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Accuracy & Percentile",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeChartTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Chart
            val scores = remember(mockTests) { mockTests.map { it.marksScored } }
            val cutoffs = remember(mockTests) { mockTests.map { it.cutoffMarks } }
            val percentiles = remember(mockTests) { mockTests.map { it.percentile } }
            val accuracies = remember(mockTests) { mockTests.map { it.accuracy } }

            val maxScore = remember(scores, cutoffs) {
                max(200f, (scores + cutoffs).maxOrNull() ?: 200f) + 10f
            }
            val minScore = remember(scores, cutoffs) {
                max(0f, ((scores + cutoffs).minOrNull() ?: 100f) - 20f)
            }

            val lineColor = BrandForestGreen
            val cutoffColor = StatusWeak.copy(alpha = 0.7f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val paddingLeft = 30f
                    val paddingRight = 30f
                    val paddingTop = 20f
                    val paddingBottom = 25f
                    val usableWidth = width - paddingLeft - paddingRight
                    val usableHeight = height - paddingTop - paddingBottom

                    if (mockTests.size >= 2) {
                        val stepX = usableWidth / (mockTests.size - 1)

                        if (activeChartTab == 0) {
                            val scorePoints = mutableListOf<Offset>()
                            val cutoffPoints = mutableListOf<Offset>()

                            val scorePath = Path()
                            val cutoffPath = Path()

                            scores.forEachIndexed { index, score ->
                                val x = paddingLeft + index * stepX
                                val yRatio = (score - minScore) / (maxScore - minScore)
                                val y = paddingTop + usableHeight * (1f - yRatio.coerceIn(0f, 1f))
                                scorePoints.add(Offset(x, y))
                                if (index == 0) scorePath.moveTo(x, y) else scorePath.lineTo(x, y)
                            }

                            cutoffs.forEachIndexed { index, cutoff ->
                                val x = paddingLeft + index * stepX
                                val yRatio = (cutoff - minScore) / (maxScore - minScore)
                                val y = paddingTop + usableHeight * (1f - yRatio.coerceIn(0f, 1f))
                                cutoffPoints.add(Offset(x, y))
                                if (index == 0) cutoffPath.moveTo(x, y) else cutoffPath.lineTo(x, y)
                            }

                            drawPath(
                                path = cutoffPath,
                                color = cutoffColor,
                                style = Stroke(
                                    width = 2.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                                )
                            )

                            drawPath(
                                path = scorePath,
                                color = lineColor,
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )

                            scorePoints.forEach { pt ->
                                drawCircle(color = lineColor, radius = 6f, center = pt)
                                drawCircle(color = Color.White, radius = 3f, center = pt)
                            }
                        } else {
                            val percPoints = mutableListOf<Offset>()
                            val accPoints = mutableListOf<Offset>()

                            val percPath = Path()
                            val accPath = Path()

                            percentiles.forEachIndexed { index, p ->
                                val x = paddingLeft + index * stepX
                                val y = paddingTop + usableHeight * (1f - (p / 100f))
                                percPoints.add(Offset(x, y))
                                if (index == 0) percPath.moveTo(x, y) else percPath.lineTo(x, y)
                            }

                            accuracies.forEachIndexed { index, a ->
                                val x = paddingLeft + index * stepX
                                val y = paddingTop + usableHeight * (1f - (a / 100f))
                                accPoints.add(Offset(x, y))
                                if (index == 0) accPath.moveTo(x, y) else accPath.lineTo(x, y)
                            }

                            drawPath(
                                path = percPath,
                                color = BrandTerracotta,
                                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                            )

                            drawPath(
                                path = accPath,
                                color = Color(0xFF3F51B5),
                                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                            )

                            percPoints.forEach { pt ->
                                drawCircle(color = BrandTerracotta, radius = 5f, center = pt)
                                drawCircle(color = Color.White, radius = 2.5f, center = pt)
                            }

                            accPoints.forEach { pt ->
                                drawCircle(color = Color(0xFF3F51B5), radius = 5f, center = pt)
                                drawCircle(color = Color.White, radius = 2.5f, center = pt)
                            }
                        }
                    } else if (mockTests.size == 1) {
                        val pt = Offset(width / 2f, height / 2f)
                        drawCircle(color = lineColor, radius = 7f, center = pt)
                        drawCircle(color = Color.White, radius = 3.5f, center = pt)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                mockTests.forEach { test ->
                    Text(
                        text = test.testDateStr.takeLast(5),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MockSubjectAveragesCard(
    mockStats: MockStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = BrandTerracotta,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Subject-Wise Score Average",
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

            Spacer(modifier = Modifier.height(14.dp))

            SubjectBarRow(
                subjectName = "Quantitative Aptitude (Maths)",
                avgScore = mockStats.averageQuantScore,
                maxScore = 50f,
                color = Color(0xFFE27D60),
                icon = "📐"
            )
            Spacer(modifier = Modifier.height(10.dp))
            SubjectBarRow(
                subjectName = "Reasoning & GI",
                avgScore = mockStats.averageReasoningScore,
                maxScore = 50f,
                color = Color(0xFF8E24AA),
                icon = "🧠"
            )
            Spacer(modifier = Modifier.height(10.dp))
            SubjectBarRow(
                subjectName = "English Comprehension",
                avgScore = mockStats.averageEnglishScore,
                maxScore = 50f,
                color = Color(0xFF3F51B5),
                icon = "📖"
            )
            Spacer(modifier = Modifier.height(10.dp))
            SubjectBarRow(
                subjectName = "General Studies (GS/GK)",
                avgScore = mockStats.averageGsScore,
                maxScore = 50f,
                color = Color(0xFF2D4F1E),
                icon = "🏛️",
                isWeakAlert = mockStats.averageGsScore < 20f && mockStats.averageGsScore > 0f
            )
        }
    }
}

@Composable
fun SubjectBarRow(
    subjectName: String,
    avgScore: Float,
    maxScore: Float,
    color: Color,
    icon: String,
    isWeakAlert: Boolean = false
) {
    val progress = if (maxScore > 0) (avgScore / maxScore).coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subjectName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isWeakAlert) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusWeak.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Scope for Growth",
                            fontSize = 9.sp,
                            color = StatusWeak,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = "${String.format("%.1f", avgScore)} / ${maxScore.toInt()} ($percent%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (percent >= 75) StatusCompleted else if (percent >= 50) StatusInProgress else StatusWeak
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
