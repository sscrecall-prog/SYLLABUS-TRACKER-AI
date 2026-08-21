package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AmbientSoundType
import com.example.data.model.ChapterStatus
import com.example.ui.components.*
import com.example.ui.components.dashboard.*
import com.example.ui.viewmodel.*
import androidx.lifecycle.viewmodel.compose.viewModel

enum class HomeSearchFilter(val label: String) {
    ALL("All Matches"),
    SUBJECTS("Subjects"),
    TOPICS("Chapters"),
    WEAK("Weak Only"),
    REVISION_DUE("Revision Due")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (NavDestination) -> Unit,
    onOpenSubject: (Long) -> Unit
) {
    val mistakeNotebookViewModel: MistakeNotebookViewModel = viewModel()
    val subjectViewModel: SubjectViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val mockTestsViewModel: MockTestsViewModel = viewModel()
    val syllabusViewModel: SyllabusViewModel = viewModel()
    val plannerViewModel: PlannerViewModel = viewModel()

    val overallStats by analyticsViewModel.overallStats.collectAsState()
    val subjectStats by subjectViewModel.subjectStatsList.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()
    val items by syllabusViewModel.items.collectAsState()
    val todayPlans by plannerViewModel.todayPlans.collectAsState()
    val searchQuery by syllabusViewModel.searchQuery.collectAsState()
    val mockTests by mockTestsViewModel.mockTests.collectAsState()
    val mistakeStats by mistakeNotebookViewModel.mistakeStats.collectAsState()
    val examPaceStats by analyticsViewModel.examPaceStats.collectAsState()
    val ambientSound by timerViewModel.ambientSound.collectAsState()
    val isAmbientPlaying by timerViewModel.isAmbientPlaying.collectAsState()

    var showEditExamDialog by remember { mutableStateOf(false) }
    var selectedSearchFilter by remember { mutableStateOf(HomeSearchFilter.ALL) }

    val revDueChapters = remember(items) { items.filter { it.isRevisionDue } }
    val weakChapters = remember(items) { items.filter { it.isWeak } }

    val trimmedQuery = searchQuery.trim()
    val isSearching = trimmedQuery.isNotEmpty()

    // Filtered subjects based on search query
    val matchedSubjects = remember(subjects, trimmedQuery) {
        if (trimmedQuery.isEmpty()) emptyList()
        else subjects.filter { sub ->
            sub.name.contains(trimmedQuery, ignoreCase = true) ||
            sub.code.contains(trimmedQuery, ignoreCase = true) ||
            sub.description.contains(trimmedQuery, ignoreCase = true)
        }
    }

    // Filtered chapters based on search query
    val matchedChapters = remember(items, trimmedQuery, selectedSearchFilter) {
        if (trimmedQuery.isEmpty()) emptyList()
        else items.filter { item ->
            val matchText = item.title.contains(trimmedQuery, ignoreCase = true) ||
                            item.notes.contains(trimmedQuery, ignoreCase = true) ||
                            item.tags.contains(trimmedQuery, ignoreCase = true)
            val matchFilter = when (selectedSearchFilter) {
                HomeSearchFilter.ALL, HomeSearchFilter.TOPICS -> true
                HomeSearchFilter.WEAK -> item.isWeak || item.status == ChapterStatus.WEAK
                HomeSearchFilter.REVISION_DUE -> item.isRevisionDue || item.status == ChapterStatus.REVISION_DUE
                HomeSearchFilter.SUBJECTS -> false
            }
            matchText && matchFilter
        }
    }

    val totalMatchesCount = matchedSubjects.size + matchedChapters.size
    val weakMatchesCount = remember(items, trimmedQuery) {
        items.count { (it.isWeak || it.status == ChapterStatus.WEAK) && it.title.contains(trimmedQuery, ignoreCase = true) }
    }
    val revisionDueMatchesCount = remember(items, trimmedQuery) {
        items.count { (it.isRevisionDue || it.status == ChapterStatus.REVISION_DUE) && it.title.contains(trimmedQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 0. PERSISTENT SEARCH BAR
        item {
            DashboardHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { syllabusViewModel.searchQuery.value = it },
                isSearching = isSearching,
                selectedSearchFilter = selectedSearchFilter,
                onSelectFilter = { selectedSearchFilter = it },
                totalMatchesCount = totalMatchesCount,
                matchedSubjectsCount = matchedSubjects.size,
                matchedChaptersCount = matchedChapters.size,
                weakMatchesCount = weakMatchesCount,
                revisionDueMatchesCount = revisionDueMatchesCount
            )
        }

        // CONDITIONAL CONTENT: SEARCH RESULTS OR HOME DASHBOARD
        if (isSearching) {
            if (totalMatchesCount == 0) {
                item {
                    DashboardSearchEmptyState(
                        trimmedQuery = trimmedQuery,
                        onClearSearch = { syllabusViewModel.searchQuery.value = "" }
                    )
                }
            } else {
                if (matchedSubjects.isNotEmpty() && (selectedSearchFilter == HomeSearchFilter.ALL || selectedSearchFilter == HomeSearchFilter.SUBJECTS)) {
                    item {
                        Text(
                            text = "📚 Matching Subjects (${matchedSubjects.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                    items(matchedSubjects) { subject ->
                        val stats = subjectStats.find { it.subject.id == subject.id }
                        if (stats != null) {
                            SubjectBentoCard(
                                stats = stats,
                                onClick = { onOpenSubject(subject.id) }
                            )
                        }
                    }
                }

                if (matchedChapters.isNotEmpty() && selectedSearchFilter != HomeSearchFilter.SUBJECTS) {
                    item {
                        Text(
                            text = "📖 Matching Chapters (${matchedChapters.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    items(matchedChapters) { chapter ->
                        val parentSubject = subjects.find { it.id == chapter.subjectId }
                        TopicItemRow(
                            item = chapter,
                            subjectName = parentSubject?.name ?: "General",
                            onClick = { onOpenSubject(chapter.subjectId) },
                            onToggleStatus = { nextStatus ->
                                syllabusViewModel.updateItemStatus(chapter.id, nextStatus)
                            },
                            onToggleBookmark = {
                                syllabusViewModel.toggleBookmark(chapter.id)
                            }
                        )
                    }
                }
            }
        } else {
            // === STANDARD HOME DASHBOARD VIEW ===
            item {
                DailyMindsetCard()
            }

            item {
                ExamCountdownPaceCard(
                    examPaceStats = examPaceStats,
                    onEditExamTarget = { showEditExamDialog = true }
                )
            }

            item {
                StudyStatisticsCard(
                    streakDays = overallStats.currentStreakDays,
                    totalStudyMins = overallStats.totalStudyMinutes,
                    completedChapters = overallStats.completedChapters,
                    totalChapters = overallStats.totalChapters,
                    mockCount = mockTests.size,
                    resolvedMistakesCount = mistakeStats.understoodCount + mistakeStats.masteredCount
                )
            }

            item {
                ProgressOverviewCard(overallStats = overallStats)
            }

            item {
                QuickActions(
                    overallStats = overallStats,
                    todayPlans = todayPlans,
                    mockTests = mockTests,
                    mistakeStats = mistakeStats,
                    ambientSound = ambientSound,
                    isAmbientPlaying = isAmbientPlaying,
                    onToggleAmbientSound = {
                        if (ambientSound == AmbientSoundType.NONE) {
                            timerViewModel.selectAmbientSound(AmbientSoundType.BROWN_NOISE)
                        } else {
                            timerViewModel.toggleAmbientPlayPause()
                        }
                    },
                    onNavigate = onNavigate
                )
            }

            if (revDueChapters.isNotEmpty() || weakChapters.isNotEmpty()) {
                item {
                    RevisionDueCard(
                        revDueChapters = revDueChapters,
                        weakChapters = weakChapters,
                        onNavigate = onNavigate
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📚 Core Subject Modules",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { onNavigate(NavDestination.SUBJECTS) }) {
                        Text("View All →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (subjectStats.isEmpty()) {
                item {
                    DashboardEmptyState(
                        onAddSubject = { onNavigate(NavDestination.SUBJECTS) }
                    )
                }
            } else {
                items(subjectStats) { stats ->
                    SubjectBentoCard(
                        stats = stats,
                        onClick = { onOpenSubject(stats.subject.id) }
                    )
                }
            }

            if (todayPlans.isNotEmpty()) {
                item {
                    TodayPlanCard(
                        todayPlans = todayPlans,
                        onTogglePlanCompleted = { plannerViewModel.togglePlanCompleted(it) },
                        onNavigate = onNavigate
                    )
                }
            }
        }
    }

    // Edit Exam Target & Countdown Modal Dialog
    if (showEditExamDialog) {
        var examNameInput by remember { mutableStateOf(examPaceStats.examName) }
        var examDateInput by remember { mutableStateOf(examPaceStats.examDateStr) }
        var examShiftInput by remember { mutableStateOf(examPaceStats.examShift) }
        val commonExams = listOf(
            "SSC CGL 2026",
            "UPSC CSE 2026",
            "IBPS PO / Clerk",
            "RRB NTPC",
            "State PSC",
            "NDA / CDS",
            "GATE / ESE"
        )
        AlertDialog(
            onDismissRequest = { showEditExamDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯 Target Exam & Countdown Date")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Set your target exam date so the daily study pace calculator updates your syllabus completion velocity accurately.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = examNameInput,
                        onValueChange = { examNameInput = it },
                        label = { Text("Exam Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        commonExams.take(3).forEach { exam ->
                            AssistChip(
                                onClick = { examNameInput = exam },
                                label = { Text(exam, fontSize = 10.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = examDateInput,
                        onValueChange = { examDateInput = it },
                        label = { Text("Target Exam Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("2026-09-15") }
                    )
                    OutlinedTextField(
                        value = examShiftInput,
                        onValueChange = { examShiftInput = it },
                        label = { Text("Exam Stage / Shift (e.g. Tier-1 / Prelims)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (examNameInput.isNotBlank() && examDateInput.isNotBlank()) {
                            settingsViewModel.updateExamTarget(
                                examName = examNameInput.trim(),
                                targetDateStr = examDateInput.trim(),
                                examShift = examShiftInput.trim()
                            )
                            showEditExamDialog = false
                        }
                    }
                ) {
                    Text("Save & Recalculate Pace")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditExamDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
