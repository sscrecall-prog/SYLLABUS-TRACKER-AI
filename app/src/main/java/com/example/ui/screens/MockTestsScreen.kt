package com.example.ui.screens
import com.example.data.model.MistakeEntry

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MockTest
import com.example.data.model.MockTestType
import com.example.ui.theme.*
import com.example.ui.viewmodel.MockStats
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.MistakeNotebookViewModel
import com.example.ui.viewmodel.MockTestsViewModel
import com.example.ui.viewmodel.SubjectViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestsScreen(
    modifier: Modifier = Modifier
) {
    val mistakeNotebookViewModel: MistakeNotebookViewModel = viewModel()
    val mockTestsViewModel: MockTestsViewModel = viewModel()
    val subjectViewModel: SubjectViewModel = viewModel()

    val uiState by mockTestsViewModel.uiState.collectAsState()
    val mockTests = uiState.filteredMockTests
    val allMockTests = uiState.allMockTests
    val mockStats = uiState.mockStats
    val searchQuery = uiState.searchQuery
    val platformFilter = uiState.platformFilter
    val typeFilter = uiState.typeFilter
    val selectedMockTest = uiState.selectedMockTest
    val subjects by subjectViewModel.subjects.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var mockToEdit by remember { mutableStateOf<MockTest?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    val platforms = remember(allMockTests) {
        val base = listOf("All", "Testbook", "Oliveboard", "PracticeMock", "Gradeup", "Unacademy")
        val custom = allMockTests.map { it.testPlatform }
            .distinct()
            .filter { it.isNotBlank() && it !in base && it != "Custom" }
        base + custom
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    mockToEdit = null
                    showAddEditDialog = true
                },
                containerColor = BrandForestGreen,
                contentColor = BrandWarmCream,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Mock Test") },
                text = { Text("Log Mock Test", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_mock_test_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Screen Title & Subtitle
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Mock Test & Percentile Tracker",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Track all-India rank, score trend, accuracy & weak sections",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Hero Highlight & Performance KPIs
            item {
                MockPerformanceHero(
                    mockStats = mockStats,
                    latestMock = allMockTests.firstOrNull(),
                    onViewLatest = {
                        if (allMockTests.isNotEmpty()) {
                            mockTestsViewModel.selectMockTest(allMockTests.first())
                            showDetailDialog = true
                        }
                    }
                )
            }

            // Score Progression Graph
            if (allMockTests.isNotEmpty()) {
                item {
                    MockScoreTrendChart(
                        mockTests = allMockTests.sortedBy { it.timestamp },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Subject Wise Breakdown
            if (allMockTests.isNotEmpty()) {
                item {
                    MockSubjectAveragesCard(
                        mockStats = mockStats,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { mockTestsViewModel.setMockSearchQuery(it) },
                        placeholder = { Text("Search mocks by name, platform, weak chapters...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { mockTestsViewModel.setMockSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mock_search_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Platform Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(platforms) { p ->
                            val isSelected = (p == "All" && platformFilter == null) || (platformFilter.equals(p, ignoreCase = true))
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (p == "All") mockTestsViewModel.setMockPlatformFilter(null)
                                    else mockTestsViewModel.setMockPlatformFilter(p)
                                },
                                label = { Text(p, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandForestGreen,
                                    selectedLabelColor = BrandWarmCream
                                )
                            )
                        }
                    }

                    // Mock Type Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = typeFilter == null,
                                onClick = { mockTestsViewModel.setMockTypeFilter(null) },
                                label = { Text("All Types", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandTerracotta,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(MockTestType.values()) { type ->
                            val isSelected = typeFilter == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { mockTestsViewModel.setMockTypeFilter(type) },
                                label = { Text(type.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandTerracotta,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Results count banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attempted Mocks (${mockTests.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (mockTests.isNotEmpty()) {
                        Text(
                            text = "Avg Acc: ${String.format("%.1f", mockStats.averageAccuracy)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = StatusCompleted
                        )
                    }
                }
            }

            // List of Mock Tests
            if (mockTests.isEmpty()) {
                item {
                    EmptyMockTestsCard(
                        onAddClick = {
                            mockToEdit = null
                            showAddEditDialog = true
                        }
                    )
                }
            } else {
                items(mockTests, key = { it.id }) { mock ->
                    MockTestCard(
                        mockTest = mock,
                        onClick = {
                            mockTestsViewModel.selectMockTest(mock)
                            showDetailDialog = true
                        },
                        onEdit = {
                            mockToEdit = mock
                            showAddEditDialog = true
                        },
                        onDelete = {
                            mockTestsViewModel.deleteMockTest(mock)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // Add / Edit Mock Dialog
    if (showAddEditDialog) {
        AddEditMockTestDialog(
            initialMock = mockToEdit,
            onDismiss = {
                showAddEditDialog = false
                mockToEdit = null
            },
            onSave = { savedMock ->
                if (mockToEdit != null) {
                    mockTestsViewModel.updateMockTest(savedMock)
                } else {
                    mockTestsViewModel.addMockTest(savedMock)
                }
                showAddEditDialog = false
                mockToEdit = null
            }
        )
    }

    // Detail Dialog
    if (showDetailDialog && selectedMockTest != null) {
        MockTestDetailDialog(
            mockTest = selectedMockTest!!,
            onDismiss = {
                showDetailDialog = false
                mockTestsViewModel.selectMockTest(null)
            },
            onEdit = {
                mockToEdit = selectedMockTest
                showDetailDialog = false
                showAddEditDialog = true
            },
            onDelete = {
                mockTestsViewModel.deleteMockTest(selectedMockTest!!)
                showDetailDialog = false
            },
            onAddMistake = { question, wrong, correct, exp, cat ->
                val defaultSubId = subjects.firstOrNull()?.id ?: 1L
                val defaultSubName = subjects.firstOrNull()?.name ?: "General"
                mistakeNotebookViewModel.addMistake(MistakeEntry(
                    questionText = question,
                    yourWrongAnswer = wrong,
                    correctAnswer = correct,
                    explanationOrKeyConcept = exp,
                    subjectId = defaultSubId,
                    subjectName = defaultSubName,
                    chapterTitle = selectedMockTest!!.testName,
                    sourceMockOrBook = selectedMockTest!!.testPlatform,
                    category = cat
                ))
            }
        )
    }
}

/**
 * Hero KPI Card showcasing latest mock performance and quick summary metrics.
 */
@Composable
fun MockPerformanceHero(
    mockStats: MockStats,
    latestMock: MockTest?,
    onViewLatest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("mock_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Latest Mock Banner if present
            if (latestMock != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    BrandForestGreen,
                                    BrandForestGreenLight
                                )
                            )
                        )
                        .clickable { onViewLatest() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LATEST MOCK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandWarmCream.copy(alpha = 0.85f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = latestMock.testPlatform,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = latestMock.testName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${latestMock.marksScored} / ${latestMock.totalMarks.toInt()}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandWarmCream
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val diff = latestMock.marksScored - latestMock.cutoffMarks
                            val diffText = if (diff >= 0) "+${String.format("%.1f", diff)} above cutoff" else "${String.format("%.1f", diff)} below cutoff"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (diff >= 0) StatusCompleted else StatusWeak)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = diffText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Percentile Badge
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

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Target vs Actual Score Gauge Card
            var targetScore by remember { mutableStateOf(150f) }
            val averageScore = mockStats.averageScore
            val percentOfTarget = if (targetScore > 0) (averageScore / targetScore) * 100f else 0f

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
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

                        // Interactive Target Control Buttons
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

                    // Circular Gauge Canvas
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 10.dp.toPx()
                            val usableRadius = (size.minDimension - strokeWidth) / 2

                            // Background grey track
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Active colored progress arc
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

            // 4-Grid KPI Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avg Score
                KPISubCard(
                    title = "Avg Score",
                    value = String.format("%.1f", mockStats.averageScore),
                    subtext = "Peak: ${String.format("%.1f", mockStats.highestScore)}",
                    accentColor = BrandForestGreen,
                    icon = Icons.Default.Score,
                    modifier = Modifier.weight(1f)
                )

                // Avg Percentile
                KPISubCard(
                    title = "Avg Percentile",
                    value = "${String.format("%.1f", mockStats.averagePercentile)}%",
                    subtext = "Best: ${String.format("%.1f", mockStats.bestPercentile)}%",
                    accentColor = BrandTerracotta,
                    icon = Icons.Default.AutoGraph,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Accuracy
                KPISubCard(
                    title = "Avg Accuracy",
                    value = "${String.format("%.1f", mockStats.averageAccuracy)}%",
                    subtext = "${mockStats.totalMocksCount} tests logged",
                    accentColor = StatusCompleted,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )

                // Cutoff Cleared Rate
                KPISubCard(
                    title = "Cutoff Rate",
                    value = "${mockStats.cutoffClearanceRate}%",
                    subtext = "${mockStats.clearedCutoffCount}/${mockStats.totalMocksCount} cleared",
                    accentColor = if (mockStats.cutoffClearanceRate >= 70) StatusCompleted else StatusInProgress,
                    icon = Icons.Default.Verified,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun KPISubCard(
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

/**
 * Visual Canvas Line Graph tracking mock test scores over time with cutoff threshold line.
 */
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

                    // Draw grid lines
                    val gridSteps = 3
                    for (i in 0..gridSteps) {
                        val y = paddingTop + (usableHeight / gridSteps) * i
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1f
                        )
                    }

                    if (mockTests.size >= 2) {
                        val stepX = usableWidth / (mockTests.size - 1)

                        if (activeChartTab == 0) {
                            // Mode 0: Scores vs Cutoff
                            val range = if (maxScore - minScore > 0) maxScore - minScore else 1f
                            val scorePath = Path()
                            val cutoffPath = Path()

                            val scorePoints = mutableListOf<Offset>()
                            val cutoffPoints = mutableListOf<Offset>()

                            scores.forEachIndexed { index, score ->
                                val x = paddingLeft + index * stepX
                                val y = paddingTop + usableHeight * (1f - (score - minScore) / range)
                                scorePoints.add(Offset(x, y))
                                if (index == 0) scorePath.moveTo(x, y) else scorePath.lineTo(x, y)
                            }

                            cutoffs.forEachIndexed { index, cutoff ->
                                val x = paddingLeft + index * stepX
                                val y = paddingTop + usableHeight * (1f - (cutoff - minScore) / range)
                                cutoffPoints.add(Offset(x, y))
                                if (index == 0) cutoffPath.moveTo(x, y) else cutoffPath.lineTo(x, y)
                            }

                            // Draw Cutoff Dashed Line
                            drawPath(
                                path = cutoffPath,
                                color = cutoffColor,
                                style = Stroke(
                                    width = 2.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            )

                            // Draw Score Gradient Fill
                            val fillPath = Path()
                            fillPath.addPath(scorePath)
                            fillPath.lineTo(scorePoints.last().x, paddingTop + usableHeight)
                            fillPath.lineTo(scorePoints.first().x, paddingTop + usableHeight)
                            fillPath.close()

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        lineColor.copy(alpha = 0.25f),
                                        lineColor.copy(alpha = 0.02f)
                                    ),
                                    startY = paddingTop,
                                    endY = paddingTop + usableHeight
                                )
                            )

                            // Draw Score Solid Line
                            drawPath(
                                path = scorePath,
                                color = lineColor,
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )

                            // Draw point dots
                            scorePoints.forEachIndexed { index, pt ->
                                val isPassed = scores[index] >= cutoffs[index]
                                drawCircle(
                                    color = if (isPassed) lineColor else StatusWeak,
                                    radius = 6f,
                                    center = pt
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3f,
                                    center = pt
                                )
                            }
                        } else {
                            // Mode 1: Accuracy & Percentile (0 to 100%)
                            val percPath = Path()
                            val accPath = Path()

                            val percPoints = mutableListOf<Offset>()
                            val accPoints = mutableListOf<Offset>()

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

                            // Draw Percentile Line
                            drawPath(
                                path = percPath,
                                color = BrandTerracotta,
                                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                            )

                            // Draw Accuracy Line
                            drawPath(
                                path = accPath,
                                color = Color(0xFF3F51B5),
                                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                            )

                            // Draw Percentile Dots
                            percPoints.forEach { pt ->
                                drawCircle(color = BrandTerracotta, radius = 5f, center = pt)
                                drawCircle(color = Color.White, radius = 2.5f, center = pt)
                            }

                            // Draw Accuracy Dots
                            accPoints.forEach { pt ->
                                drawCircle(color = Color(0xFF3F51B5), radius = 5f, center = pt)
                                drawCircle(color = Color.White, radius = 2.5f, center = pt)
                            }
                        }
                    } else if (mockTests.size == 1) {
                        // Single point
                        val pt = Offset(width / 2f, height / 2f)
                        drawCircle(color = lineColor, radius = 7f, center = pt)
                        drawCircle(color = Color.White, radius = 3.5f, center = pt)
                    }
                }
            }

            // Labels row
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

/**
 * Subject-wise breakdown card showing average performance in Quant, Reasoning, English, GS.
 */
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

/**
 * Individual Mock Test Card item in the list.
 */
@Composable
fun MockTestCard(
    mockTest: MockTest,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCleared = mockTest.marksScored >= mockTest.cutoffMarks
    val diff = mockTest.marksScored - mockTest.cutoffMarks

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("mock_card_${mockTest.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isCleared) BrandForestGreen.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Platform Chip, Type Badge, and Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Platform badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandForestGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mockTest.testPlatform,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandForestGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mockTest.testType.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Date & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = mockTest.testDateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⏱️ ${mockTest.timeTakenMinutes}m",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Test Title
            Text(
                text = mockTest.testName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Score and Stats Grid Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Score Box
                Column {
                    Text(
                        text = "SCORE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${mockTest.marksScored}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCleared) BrandForestGreen else StatusWeak
                        )
                        Text(
                            text = " / ${mockTest.totalMarks.toInt()}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    val diffStr = if (diff >= 0) "+${String.format("%.1f", diff)} Cutoff" else "${String.format("%.1f", diff)} Cutoff"
                    Text(
                        text = diffStr,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (diff >= 0) StatusCompleted else StatusWeak
                    )
                }

                // Percentile
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PERCENTILE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${String.format("%.1f", mockTest.percentile)}%",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandTerracotta
                    )
                    if (mockTest.rank > 0 && mockTest.totalStudents > 0) {
                        Text(
                            text = "Rank #${mockTest.rank}/${(mockTest.totalStudents/1000)}k",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Accuracy
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ACCURACY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${String.format("%.1f", mockTest.accuracy)}%",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mockTest.accuracy >= 85) StatusCompleted else if (mockTest.accuracy >= 70) StatusInProgress else StatusWeak
                    )
                    Text(
                        text = "${mockTest.correctQuestions}C / ${mockTest.incorrectQuestions}W / ${mockTest.totalQuestions - mockTest.attemptedQuestions}L",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sectional Breakdown Pills if available
            if (mockTest.mathTotal > 0 || mockTest.englishTotal > 0 || mockTest.reasoningTotal > 0 || mockTest.gsTotal > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (mockTest.mathTotal > 0) {
                        SectionScorePill(
                            label = "Quant",
                            score = mockTest.mathScore,
                            total = mockTest.mathTotal,
                            color = Color(0xFFE27D60),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (mockTest.reasoningTotal > 0) {
                        SectionScorePill(
                            label = "Reas",
                            score = mockTest.reasoningScore,
                            total = mockTest.reasoningTotal,
                            color = Color(0xFF8E24AA),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (mockTest.englishTotal > 0) {
                        SectionScorePill(
                            label = "Eng",
                            score = mockTest.englishScore,
                            total = mockTest.englishTotal,
                            color = Color(0xFF3F51B5),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (mockTest.gsTotal > 0) {
                        SectionScorePill(
                            label = "GS",
                            score = mockTest.gsScore,
                            total = mockTest.gsTotal,
                            color = Color(0xFF2D4F1E),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Weak areas tag if provided
            if (mockTest.weakAreasIdentified.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusWeak,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Weak Areas: ${mockTest.weakAreasIdentified}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Actions Footer
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onClick() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deep Analysis", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusWeak, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun SectionScorePill(
    label: String,
    score: Float,
    total: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "${score.toInt()}/${total.toInt()}",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Empty state card when no mocks exist or match filters.
 */
@Composable
fun EmptyMockTestsCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BrandForestGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    tint = BrandForestGreen,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Mock Tests Recorded Yet",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Log your Testbook, Oliveboard, or PYQ mock test results to analyze your percentile, accuracy, and score trends.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandForestGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log First Mock Test")
            }
        }
    }
}

/**
 * Full Add / Edit Mock Test Form Dialog Modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMockTestDialog(
    initialMock: MockTest?,
    onDismiss: () -> Unit,
    onSave: (MockTest) -> Unit
) {
    val isEdit = initialMock != null
    val defaultPlatformsList = remember { listOf("Testbook", "Oliveboard", "PracticeMock", "Gradeup", "Unacademy") }
    val initialIsCustom = remember(initialMock) {
        initialMock != null && initialMock.testPlatform !in defaultPlatformsList
    }
    
    var testName by remember { mutableStateOf(initialMock?.testName ?: "") }
    var testPlatform by remember { mutableStateOf(initialMock?.testPlatform ?: "Testbook") }
    var showCustomInput by remember { mutableStateOf(initialIsCustom) }
    var customPlatformInput by remember { mutableStateOf(if (initialIsCustom) (initialMock?.testPlatform ?: "") else "") }
    var testType by remember { mutableStateOf(initialMock?.testType ?: MockTestType.FULL_LENGTH) }
    var testDateStr by remember {
        mutableStateOf(
            initialMock?.testDateStr ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }

    var totalMarksStr by remember { mutableStateOf(initialMock?.totalMarks?.toInt()?.toString() ?: "200") }
    var marksScoredStr by remember { mutableStateOf(initialMock?.marksScored?.toString() ?: "") }
    var cutoffMarksStr by remember { mutableStateOf(initialMock?.cutoffMarks?.toInt()?.toString() ?: "135") }
    var timeTakenStr by remember { mutableStateOf(initialMock?.timeTakenMinutes?.toString() ?: "60") }

    var totalQuestionsStr by remember { mutableStateOf(initialMock?.totalQuestions?.toString() ?: "100") }
    var attemptedStr by remember { mutableStateOf(initialMock?.attemptedQuestions?.toString() ?: "") }
    var correctStr by remember { mutableStateOf(initialMock?.correctQuestions?.toString() ?: "") }
    var incorrectStr by remember { mutableStateOf(initialMock?.incorrectQuestions?.toString() ?: "") }

    var percentileStr by remember { mutableStateOf(initialMock?.percentile?.toString() ?: "") }
    var rankStr by remember { mutableStateOf(initialMock?.rank?.takeIf { it > 0 }?.toString() ?: "") }
    var totalStudentsStr by remember { mutableStateOf(initialMock?.totalStudents?.takeIf { it > 0 }?.toString() ?: "") }

    // Sectional
    var mathScoreStr by remember { mutableStateOf(initialMock?.mathScore?.takeIf { it > 0 }?.toString() ?: "") }
    var reasScoreStr by remember { mutableStateOf(initialMock?.reasoningScore?.takeIf { it > 0 }?.toString() ?: "") }
    var engScoreStr by remember { mutableStateOf(initialMock?.englishScore?.takeIf { it > 0 }?.toString() ?: "") }
    var gsScoreStr by remember { mutableStateOf(initialMock?.gsScore?.takeIf { it > 0 }?.toString() ?: "") }

    var weakAreas by remember { mutableStateOf(initialMock?.weakAreasIdentified ?: "") }
    var analysisNotes by remember { mutableStateOf(initialMock?.analysisNotes ?: "") }



    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isEdit) "Edit Mock Test" else "Log Mock Test",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Form Fields
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Test Name
                    item {
                        OutlinedTextField(
                            value = testName,
                            onValueChange = { testName = it },
                            label = { Text("Mock Test Title / Name *") },
                            placeholder = { Text("e.g., SSC CGL Tier 1 Live Mock #14") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_mock_title")
                        )
                    }

                    // Platform Selection
                    item {
                        Text("Test Platform", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val defaultPlats = listOf("Testbook", "Oliveboard", "PracticeMock", "Gradeup", "Unacademy")
                            items(defaultPlats) { plat ->
                                FilterChip(
                                    selected = !showCustomInput && testPlatform.equals(plat, ignoreCase = true),
                                    onClick = { 
                                        showCustomInput = false
                                        testPlatform = plat 
                                    },
                                    label = { Text(plat, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandForestGreen,
                                        selectedLabelColor = BrandWarmCream
                                    )
                                )
                            }
                            
                            item {
                                FilterChip(
                                    selected = showCustomInput,
                                    onClick = { 
                                        showCustomInput = true
                                        testPlatform = customPlatformInput.ifBlank { "Custom" }
                                    },
                                    label = { 
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Text(if (customPlatformInput.isNotBlank()) customPlatformInput else "Other / Custom", fontSize = 12.sp)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandTerracotta,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        if (showCustomInput) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customPlatformInput,
                                onValueChange = { 
                                    customPlatformInput = it
                                    testPlatform = it.ifBlank { "Custom" }
                                },
                                label = { Text("Enter Custom Platform Name") },
                                placeholder = { Text("e.g. Pinnacle, Exampur, RBE, Neon") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("input_custom_platform")
                            )
                        }
                    }

                    // Test Type
                    item {
                        Text("Test Type", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(MockTestType.values()) { type ->
                                FilterChip(
                                    selected = testType == type,
                                    onClick = { testType = type },
                                    label = { Text(type.label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandTerracotta,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Date & Time Spent
                    item {
                        val context = LocalContext.current
                        val calendar = Calendar.getInstance()
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            sdf.parse(testDateStr)?.let {
                                calendar.time = it
                            }
                        } catch (e: Exception) {}

                        val datePickerDialog = remember {
                            android.app.DatePickerDialog(
                                context,
                                { _, selectedYear, selectedMonth, selectedDay ->
                                    testDateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = testDateStr,
                                    onValueChange = { testDateStr = it },
                                    label = { Text("Date (YYYY-MM-DD)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
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
                                value = timeTakenStr,
                                onValueChange = { timeTakenStr = it },
                                label = { Text("Time (Minutes)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Marks Scored & Total Marks & Cutoff
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = marksScoredStr,
                                onValueChange = { marksScoredStr = it },
                                label = { Text("Marks Scored *") },
                                placeholder = { Text("148.5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("input_marks_scored")
                            )
                            OutlinedTextField(
                                value = totalMarksStr,
                                onValueChange = { totalMarksStr = it },
                                label = { Text("Total Marks") },
                                placeholder = { Text("200") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cutoffMarksStr,
                                onValueChange = { cutoffMarksStr = it },
                                label = { Text("Cutoff") },
                                placeholder = { Text("135") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Question Breakdown
                    item {
                        Text("Question Attempt Breakdown", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = attemptedStr,
                                onValueChange = { attemptedStr = it },
                                label = { Text("Attempted") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = correctStr,
                                onValueChange = { correctStr = it },
                                label = { Text("Correct") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = incorrectStr,
                                onValueChange = { incorrectStr = it },
                                label = { Text("Wrong") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Percentile & Rank
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = percentileStr,
                                onValueChange = { percentileStr = it },
                                label = { Text("Percentile %") },
                                placeholder = { Text("94.5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = rankStr,
                                onValueChange = { rankStr = it },
                                label = { Text("AIR / Rank") },
                                placeholder = { Text("420") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = totalStudentsStr,
                                onValueChange = { totalStudentsStr = it },
                                label = { Text("Total Aspirants") },
                                placeholder = { Text("18500") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Sectional Scores Breakdown
                    item {
                        Text("Sectional Marks (Optional)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = mathScoreStr,
                                onValueChange = { mathScoreStr = it },
                                label = { Text("📐 Quant") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = reasScoreStr,
                                onValueChange = { reasScoreStr = it },
                                label = { Text("🧠 Reas") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = engScoreStr,
                                onValueChange = { engScoreStr = it },
                                label = { Text("📖 Eng") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = gsScoreStr,
                                onValueChange = { gsScoreStr = it },
                                label = { Text("🏛️ GS") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Weak Areas Identified
                    item {
                        OutlinedTextField(
                            value = weakAreas,
                            onValueChange = { weakAreas = it },
                            label = { Text("Weak Chapters / Silly Mistakes Identified") },
                            placeholder = { Text("e.g. Trigonometry Height & Distance, Polity Articles") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Analysis & Learning Notes
                    item {
                        OutlinedTextField(
                            value = analysisNotes,
                            onValueChange = { analysisNotes = it },
                            label = { Text("Post-Test Analysis Notes & Strategy") },
                            placeholder = { Text("e.g. Solved Quant in 22 mins, English RC was easy, need to avoid guessing in GS.") },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val scored = marksScoredStr.toFloatOrNull() ?: 0f
                            val total = totalMarksStr.toFloatOrNull() ?: 200f
                            val cutoff = cutoffMarksStr.toFloatOrNull() ?: 135f
                            val attempted = attemptedStr.toIntOrNull() ?: 0
                            val correct = correctStr.toIntOrNull() ?: 0
                            val incorrect = incorrectStr.toIntOrNull() ?: 0
                            val totalQ = totalQuestionsStr.toIntOrNull() ?: 100
                            val accuracy = if (attempted > 0) (correct.toFloat() / attempted.toFloat()) * 100f else 0f
                            val percentile = percentileStr.toFloatOrNull() ?: 0f
                            val rank = rankStr.toIntOrNull() ?: 0
                            val students = totalStudentsStr.toIntOrNull() ?: 0
                            val timeTaken = timeTakenStr.toIntOrNull() ?: 60

                            val math = mathScoreStr.toFloatOrNull() ?: 0f
                            val reas = reasScoreStr.toFloatOrNull() ?: 0f
                            val eng = engScoreStr.toFloatOrNull() ?: 0f
                            val gs = gsScoreStr.toFloatOrNull() ?: 0f

                            val newMock = (initialMock ?: MockTest(
                                testName = if (testName.isNotBlank()) testName else "Mock Test #${System.currentTimeMillis() % 1000}",
                                testDateStr = testDateStr
                            )).copy(
                                testName = if (testName.isNotBlank()) testName else "Mock Test",
                                testPlatform = testPlatform,
                                testType = testType,
                                testDateStr = testDateStr,
                                totalMarks = total,
                                marksScored = scored,
                                cutoffMarks = cutoff,
                                timeTakenMinutes = timeTaken,
                                totalQuestions = totalQ,
                                attemptedQuestions = attempted,
                                correctQuestions = correct,
                                incorrectQuestions = incorrect,
                                accuracy = accuracy,
                                percentile = percentile,
                                rank = rank,
                                totalStudents = students,
                                mathScore = math,
                                mathTotal = if (math > 0) 50f else 0f,
                                reasoningScore = reas,
                                reasoningTotal = if (reas > 0) 50f else 0f,
                                englishScore = eng,
                                englishTotal = if (eng > 0) 50f else 0f,
                                gsScore = gs,
                                gsTotal = if (gs > 0) 50f else 0f,
                                weakAreasIdentified = weakAreas,
                                analysisNotes = analysisNotes,
                                isClearedCutoff = scored >= cutoff
                            )
                            onSave(newMock)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandForestGreen),
                        modifier = Modifier.testTag("save_mock_button")
                    ) {
                        Text(if (isEdit) "Update Mock" else "Save Mock")
                    }
                }
            }
        }
    }
}

/**
 * Deep Analysis Modal Dialog for reviewing a specific mock test.
 */
@Composable
fun MockTestDetailDialog(
    mockTest: MockTest,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddMistake: (question: String, wrongAnswer: String, correctAnswer: String, explanation: String, category: com.example.data.model.MistakeCategory) -> Unit
) {
    val isCleared = mockTest.marksScored >= mockTest.cutoffMarks
    val diff = mockTest.marksScored - mockTest.cutoffMarks

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrandForestGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = mockTest.testPlatform,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandForestGreen
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mockTest.testDateStr,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mockTest.testName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Performance Hero in Details
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        if (isCleared) listOf(BrandForestGreen, BrandForestGreenLight)
                                        else listOf(Color(0xFFB71C1C), Color(0xFFE53935))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "TOTAL SCORE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "${mockTest.marksScored} / ${mockTest.totalMarks.toInt()}",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (isCleared) "✅ Qualified (+${String.format("%.1f", diff)} above cutoff)" else "❌ Missed Cutoff (${String.format("%.1f", diff)} below)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandWarmCream
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${String.format("%.1f", mockTest.percentile)}%",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrandWarmCream
                                        )
                                        Text(
                                            text = "Percentile",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                        if (mockTest.rank > 0) {
                                            Text(
                                                text = "AIR #${mockTest.rank} / ${mockTest.totalStudents}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Question Analytics breakdown
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Attempt & Accuracy Summary", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Attempted", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${mockTest.attemptedQuestions}/${mockTest.totalQuestions}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Correct", fontSize = 11.sp, color = StatusCompleted)
                                        Text("${mockTest.correctQuestions} (${String.format("%.1f", mockTest.accuracy)}%)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusCompleted)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Incorrect", fontSize = 11.sp, color = StatusWeak)
                                        Text("${mockTest.incorrectQuestions}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusWeak)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Speed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        val marksPerMin = if (mockTest.timeTakenMinutes > 0) mockTest.marksScored / mockTest.timeTakenMinutes else 0f
                                        Text("${String.format("%.2f", marksPerMin)} m/m", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Sectional Scores Detailed
                    if (mockTest.mathTotal > 0 || mockTest.englishTotal > 0 || mockTest.reasoningTotal > 0 || mockTest.gsTotal > 0) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Sectional Scores Breakdown", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (mockTest.mathTotal > 0) {
                                        SubjectBarRow("📐 Quantitative Aptitude", mockTest.mathScore, mockTest.mathTotal, Color(0xFFE27D60), "")
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    if (mockTest.reasoningTotal > 0) {
                                        SubjectBarRow("🧠 Reasoning & GI", mockTest.reasoningScore, mockTest.reasoningTotal, Color(0xFF8E24AA), "")
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    if (mockTest.englishTotal > 0) {
                                        SubjectBarRow("📖 English Comprehension", mockTest.englishScore, mockTest.englishTotal, Color(0xFF3F51B5), "")
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    if (mockTest.gsTotal > 0) {
                                        SubjectBarRow("🏛️ General Studies (GS)", mockTest.gsScore, mockTest.gsTotal, Color(0xFF2D4F1E), "")
                                    }
                                }
                            }
                        }
                    }

                    // Weak Areas
                    if (mockTest.weakAreasIdentified.isNotBlank()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = StatusWeak.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, StatusWeak.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWeak, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Identified Weak Chapters", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusWeak)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(mockTest.weakAreasIdentified, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    // Analysis & Learning Notes
                    if (mockTest.analysisNotes.isNotBlank()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = BrandForestGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Post-Test Strategic Learnings", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(mockTest.analysisNotes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    // Quick Log Mistakes section from this Mock Test
                    item {
                        var showQuickMistakeInput by remember { mutableStateOf(false) }
                        var questionText by remember { mutableStateOf("") }
                        var mistakeCategory by remember { mutableStateOf(com.example.data.model.MistakeCategory.CONCEPT_GAP) }
                        var yourWrongAns by remember { mutableStateOf("") }
                        var correctAns by remember { mutableStateOf("") }
                        var explanationText by remember { mutableStateOf("") }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandForestGreen.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, BrandForestGreen.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().testTag("quick_add_mistake_section")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.BookmarkAdd,
                                            contentDescription = null,
                                            tint = BrandForestGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Quick Log Mistake in Error Diary",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandForestGreen
                                        )
                                    }
                                    IconButton(
                                        onClick = { showQuickMistakeInput = !showQuickMistakeInput },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showQuickMistakeInput) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle Input",
                                            tint = BrandForestGreen
                                        )
                                    }
                                }

                                if (showQuickMistakeInput) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = questionText,
                                        onValueChange = { questionText = it },
                                        label = { Text("Wrong Question / Missed Concept", fontSize = 12.sp) },
                                        placeholder = { Text("e.g. Geometry tangent formula / Grammatical error in parallel structures...") },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("quick_question_field")
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = yourWrongAns,
                                            onValueChange = { yourWrongAns = it },
                                            label = { Text("Your Wrong Ans", fontSize = 11.sp) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = correctAns,
                                            onValueChange = { correctAns = it },
                                            label = { Text("Correct Ans", fontSize = 11.sp) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = explanationText,
                                        onValueChange = { explanationText = it },
                                        label = { Text("Post-Test Explanation / Core Concept", fontSize = 12.sp) },
                                        placeholder = { Text("Write formulas or learning trick...") },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Category selector chips in Quick-Add
                                    Text("Mistake Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(com.example.data.model.MistakeCategory.values()) { cat ->
                                            val isSelected = mistakeCategory == cat
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { mistakeCategory = cat },
                                                label = { Text(cat.label, fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = BrandTerracotta,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            if (questionText.isNotBlank()) {
                                                onAddMistake(questionText, yourWrongAns, correctAns, explanationText, mistakeCategory)
                                                // Reset fields
                                                questionText = ""
                                                yourWrongAns = ""
                                                correctAns = ""
                                                explanationText = ""
                                                showQuickMistakeInput = false
                                            }
                                        },
                                        enabled = questionText.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandForestGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Save to Mistake Notebook 📓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = StatusWeak)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }

                    Row {
                        OutlinedButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandForestGreen)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
