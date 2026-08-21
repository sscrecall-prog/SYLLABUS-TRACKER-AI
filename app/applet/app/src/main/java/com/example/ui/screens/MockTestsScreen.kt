package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.MistakeEntry
import com.example.data.model.MockTest
import com.example.ui.components.mocktests.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MistakeNotebookViewModel
import com.example.ui.viewmodel.MockTestsViewModel
import com.example.ui.viewmodel.SubjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestsScreen(
    modifier: Modifier = Modifier
) {
    val mistakeNotebookViewModel: MistakeNotebookViewModel = viewModel()
    val mockTestsViewModel: MockTestsViewModel = viewModel()
    val subjectViewModel: SubjectViewModel = viewModel()

    val mockTests by mockTestsViewModel.filteredMockTests.collectAsState()
    val allMockTests by mockTestsViewModel.mockTests.collectAsState()
    val mockStats by mockTestsViewModel.mockStats.collectAsState()
    val searchQuery by mockTestsViewModel.mockSearchQuery.collectAsState()
    val platformFilter by mockTestsViewModel.mockPlatformFilter.collectAsState()
    val typeFilter by mockTestsViewModel.mockTypeFilter.collectAsState()
    val selectedMockTest by mockTestsViewModel.selectedMockTest.collectAsState()
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
                    Text(
                        text = "🎯 Mock Test Hub & Percentile Tracker",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Track Testbook, Oliveboard, & PYQ mock scores, analyze cutoffs, and pinpoint weak areas.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Statistics section
            if (allMockTests.isNotEmpty()) {
                item {
                    MockTestStatistics(
                        mockStats = mockStats,
                        mockTests = allMockTests,
                        latestMock = allMockTests.maxByOrNull { it.id },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Search Bar & Filter Chips
            item {
                MockTestFilters(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { mockTestsViewModel.setMockSearchQuery(it) },
                    platforms = platforms,
                    platformFilter = platformFilter,
                    onPlatformFilterChange = { mockTestsViewModel.setMockPlatformFilter(it) },
                    typeFilter = typeFilter,
                    onTypeFilterChange = { mockTestsViewModel.setMockTypeFilter(it) }
                )
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
                    MockTestEmptyState(
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

    // Modal Dialogs
    MockTestDialogs(
        showAddEditDialog = showAddEditDialog,
        mockToEdit = mockToEdit,
        onDismissAddEdit = {
            showAddEditDialog = false
            mockToEdit = null
        },
        onSaveMock = { savedMock ->
            if (mockToEdit != null) {
                mockTestsViewModel.updateMockTest(savedMock)
            } else {
                mockTestsViewModel.addMockTest(savedMock)
            }
            showAddEditDialog = false
            mockToEdit = null
        },
        showDetailDialog = showDetailDialog,
        selectedMockTest = selectedMockTest,
        onDismissDetail = {
            showDetailDialog = false
            mockTestsViewModel.selectMockTest(null)
        },
        onEditFromDetail = {
            mockToEdit = selectedMockTest
            showDetailDialog = false
            showAddEditDialog = true
        },
        onDeleteFromDetail = {
            if (selectedMockTest != null) {
                mockTestsViewModel.deleteMockTest(selectedMockTest!!)
            }
            showDetailDialog = false
        },
        onAddMistakeFromDetail = { question, wrong, correct, exp, cat ->
            val defaultSubId = subjects.firstOrNull()?.id ?: 1L
            val defaultSubName = subjects.firstOrNull()?.name ?: "General"
            if (selectedMockTest != null) {
                mistakeNotebookViewModel.addMistake(
                    MistakeEntry(
                        questionText = question,
                        yourWrongAnswer = wrong,
                        correctAnswer = correct,
                        explanationOrKeyConcept = exp,
                        subjectId = defaultSubId,
                        subjectName = defaultSubName,
                        chapterTitle = selectedMockTest!!.testName,
                        sourceMockOrBook = selectedMockTest!!.testPlatform,
                        category = cat
                    )
                )
            }
        }
    )
}
