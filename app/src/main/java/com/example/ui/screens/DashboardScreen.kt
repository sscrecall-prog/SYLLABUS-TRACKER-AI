package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemType
import com.example.data.model.Subject
import com.example.data.model.SyllabusItem
import com.example.data.model.ChapterStatus
import com.example.data.model.AmbientSoundType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.MistakeNotebookViewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.TimerViewModel
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.MockTestsViewModel
import com.example.ui.viewmodel.SyllabusViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PlannerViewModel
import com.example.ui.viewmodel.IntelligenceViewModel
import com.example.data.intelligence.*

enum class HomeSearchFilter(val label: String) {
    ALL("All Matches"),
    SUBJECTS("Subjects"),
    TOPICS("Chapters"),
    WEAK("Weak Only"),
    REVISION_DUE("Revision Due")
}

enum class HomeDashboardTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    TODAYS_PLAN("Daily Plan", Icons.Default.EventNote),
    SUBJECTS("Subjects", Icons.Default.MenuBook),
    INTELLIGENCE("Exam Insights", Icons.Default.AutoGraph)
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
    val mainViewModel: MainViewModel = viewModel()
    val plannerViewModel: PlannerViewModel = viewModel()
    val intelligenceViewModel: IntelligenceViewModel = viewModel()

    val overallStats by analyticsViewModel.overallStats.collectAsState()
    val subjectStats by subjectViewModel.subjectStatsList.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()
    val items by syllabusViewModel.items.collectAsState()
    val todayPlans by plannerViewModel.todayPlans.collectAsState()
    val searchQuery by syllabusViewModel.searchQuery.collectAsState()
    val mockTests by mockTestsViewModel.mockTests.collectAsState()
    val mockStats by mockTestsViewModel.mockStats.collectAsState()
    val mistakeStats by mistakeNotebookViewModel.mistakeStats.collectAsState()
    val examPaceStats by analyticsViewModel.examPaceStats.collectAsState()
    val ambientSound by timerViewModel.ambientSound.collectAsState()
    val isAmbientPlaying by timerViewModel.isAmbientPlaying.collectAsState()
    val appSettings by settingsViewModel.appSettings.collectAsState()
    val intelligenceSnapshot by intelligenceViewModel.snapshot.collectAsState()
    val dailyBudget by intelligenceViewModel.dailyBudgetMinutes.collectAsState()

    var activeTab by remember { mutableStateOf(HomeDashboardTab.OVERVIEW) }
    var showEditExamDialog by remember { mutableStateOf(false) }
    var showWeeklyReportDialog by remember { mutableStateOf(false) }
    var selectedSearchFilter by remember { mutableStateOf(HomeSearchFilter.ALL) }

    val revDueChapters by syllabusViewModel.revisionDueChapters.collectAsState()
    val weakChapters by syllabusViewModel.weakChapters.collectAsState()

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // 0. TOP GREETING & TARGET EXAM BAR
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (appSettings.userName.isNotBlank()) "Hello, ${appSettings.userName} 👋" else "Hello, Aspirant 👋",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Track your syllabus, pace & daily revision",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Target Exam Countdown Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandForestGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, BrandForestGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.clickable { showEditExamDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = BrandForestGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Column {
                            Text(
                                text = appSettings.targetExam.ifEmpty { "Target Exam" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandForestGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (intelligenceSnapshot.pace.daysRemaining > 0) {
                                Text(
                                    text = "${intelligenceSnapshot.pace.daysRemaining} days left",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Target",
                            tint = BrandForestGreen.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // 1. CLEAN PERSISTENT SEARCH BAR
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { syllabusViewModel.searchQuery.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar"),
                    placeholder = {
                        Text(
                            text = "Search subjects, chapters, PYQs, or notes...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearching) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (isSearching) {
                            IconButton(
                                onClick = { syllabusViewModel.searchQuery.value = "" },
                                modifier = Modifier.testTag("home_search_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                )

                // Search Filter Chips (Active when user is typing/searching)
                AnimatedVisibility(
                    visible = isSearching,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(HomeSearchFilter.values()) { filter ->
                            val isSelected = selectedSearchFilter == filter
                            val count = when (filter) {
                                HomeSearchFilter.ALL -> totalMatchesCount
                                HomeSearchFilter.SUBJECTS -> matchedSubjects.size
                                HomeSearchFilter.TOPICS -> matchedChapters.size
                                HomeSearchFilter.WEAK -> items.count { (it.isWeak || it.status == ChapterStatus.WEAK) && it.title.contains(trimmedQuery, ignoreCase = true) }
                                HomeSearchFilter.REVISION_DUE -> items.count { (it.isRevisionDue || it.status == ChapterStatus.REVISION_DUE) && it.title.contains(trimmedQuery, ignoreCase = true) }
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSearchFilter = filter },
                                label = {
                                    Text(
                                        text = "${filter.label} ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // CONDITIONAL CONTENT: SEARCH RESULTS OR HOME DASHBOARD
        if (isSearching) {
            // === SEARCH RESULTS VIEW ===
            if (totalMatchesCount == 0) {
                // No Results Found State
                item {
                    BentoCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("home_search_no_results"),
                        shape = RoundedCornerShape(20.dp),
                        accentColor = MaterialTheme.colorScheme.outline
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No results matching \"$trimmedQuery\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching for a different subject (e.g. GS, Maths), chapter, keyword, or note.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { syllabusViewModel.searchQuery.value = "" },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Clear Search")
                            }
                        }
                    }
                }
            } else {
                // 1. MATCHING SUBJECTS
                if (matchedSubjects.isNotEmpty() && (selectedSearchFilter == HomeSearchFilter.ALL || selectedSearchFilter == HomeSearchFilter.SUBJECTS)) {
                    item {
                        Text(
                            text = "📚 Matching Subjects (${matchedSubjects.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(matchedSubjects) { subject ->
                        val stats = subjectStats.find { it.subject.id == subject.id }
                        val subjectColor = try {
                            Color(android.graphics.Color.parseColor(subject.colorHex))
                        } catch (e: Exception) {
                            BrandForestGreen
                        }

                        BentoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOpenSubject(subject.id)
                                }
                                .testTag("search_result_subject_${subject.id}"),
                            shape = RoundedCornerShape(16.dp),
                            accentColor = subjectColor
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(subjectColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getSubjectIcon(subject.iconName),
                                            contentDescription = null,
                                            tint = subjectColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = subject.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (subject.code.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(subjectColor.copy(alpha = 0.12f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = subject.code,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = subjectColor
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "${stats?.totalChapters ?: 0} chapters • ${stats?.completionPercentage ?: 0}% completed",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(subjectColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Open Syllabus →",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = subjectColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. MATCHING TOPICS & SUBTOPICS
                if (matchedChapters.isNotEmpty() && selectedSearchFilter != HomeSearchFilter.SUBJECTS) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📝 Matching Chapters (${matchedChapters.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(matchedChapters) { chapter ->
                        val parentSubject = subjects.find { it.id == chapter.subjectId }
                        val subjectColor = parentSubject?.let {
                            try {
                                Color(android.graphics.Color.parseColor(it.colorHex))
                            } catch (e: Exception) {
                                BrandForestGreen
                            }
                        } ?: BrandForestGreen

                        BentoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    syllabusViewModel.selectChapter(chapter)
                                }
                                .testTag("search_result_chapter_${chapter.id}"),
                            shape = RoundedCornerShape(14.dp),
                            accentColor = if (chapter.isWeak || chapter.status == ChapterStatus.WEAK) StatusWeak else if (chapter.isRevisionDue || chapter.status == ChapterStatus.REVISION_DUE) StatusRevisionDue else subjectColor
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (parentSubject != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(subjectColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = parentSubject.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = subjectColor
                                            )
                                        }
                                    }

                                    StatusBadge(status = chapter.status)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = chapter.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (chapter.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = chapter.notes,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Confidence: ${chapter.confidence}/5 ★",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (chapter.studyTimeMinutes > 0) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "⏱️ ${chapter.studyTimeMinutes}m",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilledTonalIconButton(
                                            onClick = {
                                                timerViewModel.setTimerTargetById(parentSubject?.id, chapter.id)
                                                mainViewModel.navigateTo(NavDestination.TIMER)
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Timer,
                                                contentDescription = "Start Timer",
                                                modifier = Modifier.size(16.dp),
                                                tint = BrandTerracotta
                                            )
                                        }

                                        FilledTonalIconButton(
                                            onClick = {
                                                val nextStatus = when (chapter.status) {
                                                    ChapterStatus.NOT_STARTED -> ChapterStatus.IN_PROGRESS
                                                    ChapterStatus.IN_PROGRESS, ChapterStatus.LEARNING -> ChapterStatus.COMPLETED
                                                    ChapterStatus.COMPLETED -> ChapterStatus.MASTERED
                                                    ChapterStatus.MASTERED, ChapterStatus.REVISION_DUE, ChapterStatus.WEAK -> ChapterStatus.NOT_STARTED
                                                }
                                                syllabusViewModel.updateChapterStatus(chapter, nextStatus)
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Toggle Status",
                                                modifier = Modifier.size(16.dp),
                                                tint = StatusCompleted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // === CLEAN, PROFESSIONAL HOME DASHBOARD ===

            // 2. DASHBOARD SECTION NAVIGATION PILLS
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HomeDashboardTab.values()) { tab ->
                        val isSelected = activeTab == tab
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.clickable { activeTab = tab }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = tab.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // === TAB SPECIFIC CONTENT ===
            when (activeTab) {
                HomeDashboardTab.OVERVIEW -> {
                    // 1. HERO BENTO CARD: Master Progress & Velocity
                    item {
                        GradientCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dashboard_hero_bento"),
                            shape = RoundedCornerShape(22.dp),
                            colors = listOf(BrandForestGreen, Color(0xFF0B4A26))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(BrandTerracotta)
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "MASTER SYLLABUS",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White.copy(alpha = 0.15f))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "🔥 ${overallStats.currentStreakDays}d Streak",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BrandWarmCream
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Syllabus Mastery",
                                            fontSize = 21.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandWarmCream
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${overallStats.completedChapters} of ${overallStats.totalChapters} chapters prepared",
                                            fontSize = 12.sp,
                                            color = BrandCreamDark
                                        )
                                    }

                                    // Circular Dual Progress Ring
                                    ProgressRing(
                                        progress = overallStats.completionPercentage / 100f,
                                        size = 80.dp,
                                        strokeWidth = 7.dp,
                                        primaryColor = BrandWarmCream,
                                        secondaryColor = BrandTerracotta,
                                        backgroundColor = Color.White.copy(alpha = 0.18f)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${overallStats.completionPercentage}%",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandWarmCream
                                            )
                                            Text(
                                                text = "READY",
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandCreamLight
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Bento Micro-Metrics Bar inside Hero
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.22f))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Active", fontSize = 9.5.sp, color = BrandCreamDark)
                                        Text("${overallStats.inProgressChapters}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusInProgress)
                                    }
                                    VerticalDivider(modifier = Modifier.height(18.dp), color = Color.White.copy(alpha = 0.15f))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Revision Due", fontSize = 9.5.sp, color = BrandCreamDark)
                                        Text("${overallStats.revisionDueChapters}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusRevisionDue)
                                    }
                                    VerticalDivider(modifier = Modifier.height(18.dp), color = Color.White.copy(alpha = 0.15f))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Weak Topics", fontSize = 9.5.sp, color = BrandCreamDark)
                                        Text("${overallStats.weakChapters}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusWeak)
                                    }
                                    VerticalDivider(modifier = Modifier.height(18.dp), color = Color.White.copy(alpha = 0.15f))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Today's Study", fontSize = 9.5.sp, color = BrandCreamDark)
                                        Text("${overallStats.todayStudyMinutes}m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandWarmCream)
                                    }
                                }
                            }
                        }
                    }

                    // 2. QUICK ACTION TILES GRID (2x2 Modular Grid)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BentoActionTile(
                                    title = "Spaced Revision",
                                    subtitle = "${overallStats.revisionDueChapters} chapters due",
                                    badgeText = if (overallStats.revisionDueChapters > 0) "DUE NOW" else null,
                                    icon = Icons.Default.Update,
                                    iconColor = StatusRevisionDue,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bento_revision_tile"),
                                    onClick = { onNavigate(NavDestination.REVISION) }
                                )

                                BentoActionTile(
                                    title = "Daily Planner",
                                    subtitle = "${todayPlans.count { it.isCompleted }}/${todayPlans.size} tasks done",
                                    badgeText = "${todayPlans.size} TODAY",
                                    icon = Icons.Default.CalendarMonth,
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bento_planner_tile"),
                                    onClick = { onNavigate(NavDestination.PLANNER) }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BentoActionTile(
                                    title = "Pomodoro Focus",
                                    subtitle = "Log & timer study",
                                    badgeText = "25m / 50m",
                                    icon = Icons.Default.Timer,
                                    iconColor = BrandTerracotta,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bento_timer_tile"),
                                    onClick = { onNavigate(NavDestination.TIMER) }
                                )

                                BentoActionTile(
                                    title = "Exam Readiness",
                                    subtitle = "${overallStats.masteredChapters} mastered",
                                    badgeText = "ANALYTICS",
                                    icon = Icons.Default.Analytics,
                                    iconColor = SubjectEnglish,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bento_analytics_tile"),
                                    onClick = { onNavigate(NavDestination.ANALYTICS) }
                                )
                            }

                            // Mock Test & Percentile Hub Tile
                            BentoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigate(NavDestination.MOCK_TESTS) }
                                    .testTag("dashboard_mock_test_hub_card"),
                                shape = RoundedCornerShape(16.dp),
                                accentColor = BrandForestGreen
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(BrandForestGreen.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Quiz,
                                                contentDescription = null,
                                                tint = BrandForestGreen,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Mock Tests & Percentiles",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(BrandTerracotta.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "${mockTests.size} MOCKS",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BrandTerracotta
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            val latest = mockTests.firstOrNull()
                                            val subText = if (latest != null) {
                                                "Latest: ${latest.marksScored.toInt()}/${latest.totalMarks.toInt()} • ${String.format("%.1f", latest.percentile)}%ile"
                                            } else {
                                                "Track Testbook, Oliveboard scores & rank"
                                            }

                                            Text(
                                                text = subText,
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BrandForestGreen.copy(alpha = 0.12f))
                                            .padding(horizontal = 9.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "Track →",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandForestGreen
                                        )
                                    }
                                }
                            }

                            // Digital Error Diary Tile
                            BentoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigate(NavDestination.MISTAKES) }
                                    .testTag("dashboard_mistake_notebook_card"),
                                shape = RoundedCornerShape(16.dp),
                                accentColor = if (mistakeStats.reviewDueCount > 0) StatusWeak else StatusInProgress
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (mistakeStats.reviewDueCount > 0) StatusWeak.copy(alpha = 0.15f)
                                                    else StatusInProgress.copy(alpha = 0.15f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BookmarkRemove,
                                                contentDescription = null,
                                                tint = if (mistakeStats.reviewDueCount > 0) StatusWeak else StatusInProgress,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Digital Error Diary",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (mistakeStats.reviewDueCount > 0) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(StatusWeak.copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "${mistakeStats.reviewDueCount} DUE",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = StatusWeak
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            val subText = if (mistakeStats.totalMistakesCount == 0) {
                                                "Log PYQ & mock exam mistakes"
                                            } else if (mistakeStats.reviewDueCount > 0) {
                                                "Drill ${mistakeStats.reviewDueCount} weak concepts today"
                                            } else {
                                                "${mistakeStats.totalMistakesCount} errors logged • ${mistakeStats.resolutionRatePercent}% resolved"
                                            }

                                            Text(
                                                text = subText,
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (mistakeStats.reviewDueCount > 0) StatusWeak.copy(alpha = 0.12f)
                                                else StatusInProgress.copy(alpha = 0.12f)
                                            )
                                            .padding(horizontal = 9.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "Drill →",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (mistakeStats.reviewDueCount > 0) StatusWeak else StatusInProgress
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. HIGH PRIORITY FOCUS ALERT (If Due or Weak Chapters exist)
                    if (revDueChapters.isNotEmpty() || weakChapters.isNotEmpty()) {
                        item {
                            BentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                accentColor = BrandTerracotta
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Adjust,
                                                contentDescription = null,
                                                tint = BrandTerracotta,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "🎯 Priority Revision & Weak Areas",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (revDueChapters.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(StatusRevisionDue.copy(alpha = 0.12f))
                                                .clickable { onNavigate(NavDestination.REVISION) }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Update, contentDescription = null, tint = StatusRevisionDue, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${revDueChapters.size} chapters need spaced repetition",
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Top: ${revDueChapters.first().title}",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(StatusRevisionDue)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "Revise",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    if (weakChapters.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(StatusWeak.copy(alpha = 0.12f))
                                                .clickable { onNavigate(NavDestination.WEAK_TOPICS) }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = StatusWeak, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${weakChapters.size} weak chapters need reinforcement",
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Drill confidence & practice PYQs",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(StatusWeak)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "Drill",
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
                    }

                    // 4. CORE SUBJECTS HIGHLIGHT (Top 3 active subjects)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📚 Core Subject Modules",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = { activeTab = HomeDashboardTab.SUBJECTS }) {
                                Text("View All (${subjectStats.size}) →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(subjectStats.take(3)) { stats ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(stats.subject.colorHex))
                        } catch (e: Exception) {
                            BrandForestGreen
                        }

                        BentoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSubject(stats.subject.id) },
                            shape = RoundedCornerShape(16.dp),
                            elevation = 2.dp,
                            accentColor = color
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
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(color.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stats.subject.code.ifEmpty { stats.subject.name.take(2).uppercase() },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = color
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = stats.subject.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${stats.totalChapters} Chapters • ${stats.completedChapters} Done",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(color.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${stats.completionPercentage}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearSyllabusBar(
                                    progress = stats.completionPercentage / 100f,
                                    height = 6.dp,
                                    barColor = color
                                )
                            }
                        }
                    }

                    // 5. AMBIENT FOCUS AUDIO PLAYER
                    item {
                        BentoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(NavDestination.TIMER) }
                                .testTag("dashboard_ambient_audio_quick_tile"),
                            shape = RoundedCornerShape(16.dp),
                            accentColor = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE) Color(0xFFAB47BC) else BrandTerracotta
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE)
                                                    Color(0xFFAB47BC).copy(alpha = 0.15f)
                                                else BrandTerracotta.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (ambientSound != AmbientSoundType.NONE) ambientSound.emoji else "🎧",
                                            fontSize = 20.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Focus White Noise",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE)
                                                            Color(0xFFAB47BC).copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE) "PLAYING" else "OFFLINE",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE) Color(0xFFAB47BC) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = if (ambientSound != AmbientSoundType.NONE) "${ambientSound.title} • Tap to tune" else "Rain, Brown Noise, Binaural Beats",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                FilledIconButton(
                                    onClick = {
                                        if (ambientSound == AmbientSoundType.NONE) {
                                            timerViewModel.selectAmbientSound(AmbientSoundType.BROWN_NOISE)
                                        } else {
                                            timerViewModel.toggleAmbientPlayPause()
                                        }
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE)
                                            Color(0xFFAB47BC)
                                        else MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE)
                                            Icons.Default.VolumeUp
                                        else Icons.Default.VolumeOff,
                                        contentDescription = "Toggle Ambient Sound",
                                        tint = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE)
                                            Color.White
                                        else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 6. GAMIFIED HABIT & BADGES CARD
                    item {
                        GamifiedAspirantCard(
                            streakDays = overallStats.currentStreakDays,
                            totalStudyMins = overallStats.totalStudyMinutes,
                            completedChapters = overallStats.completedChapters,
                            totalChapters = overallStats.totalChapters,
                            mockCount = mockTests.size,
                            resolvedMistakesCount = mistakeStats.understoodCount + mistakeStats.masteredCount
                        )
                    }
                }

                HomeDashboardTab.TODAYS_PLAN -> {
                    // 1. TODAY'S ADAPTIVE PLAN ENGINE CARD
                    item {
                        TodaysAdaptivePlanCard(
                            plan = intelligenceSnapshot.todaysPlan,
                            selectedBudgetMinutes = dailyBudget,
                            onBudgetChanged = { intelligenceViewModel.setDailyBudgetMinutes(it) },
                            onActionCompleted = { intelligenceViewModel.markPlanActionCompleted(it) },
                            onActionClick = { item ->
                                val ch = items.find { it.id == item.topicId }
                                if (ch != null) syllabusViewModel.selectChapter(ch)
                            }
                        )
                    }

                    // 2. STUDY SCHEDULE ITEMS
                    if (todayPlans.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📅 Scheduled Tasks (${todayPlans.size})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = { onNavigate(NavDestination.PLANNER) }) {
                                    Text("Open Planner →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        items(todayPlans) { plan ->
                            BentoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { plannerViewModel.togglePlanCompleted(plan) },
                                shape = RoundedCornerShape(14.dp)
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
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = plan.chapterTitle,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (plan.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${plan.timeStr} • ${plan.subjectName} • ${plan.plannedMinutes} mins",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (plan.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = StatusCompleted,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HomeDashboardTab.SUBJECTS -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📚 All Subjects (${subjectStats.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = { onNavigate(NavDestination.SUBJECTS) }) {
                                Text("Manage →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(subjectStats) { stats ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(stats.subject.colorHex))
                        } catch (e: Exception) {
                            BrandForestGreen
                        }

                        BentoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSubject(stats.subject.id) },
                            shape = RoundedCornerShape(16.dp),
                            elevation = 2.dp,
                            accentColor = color
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
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(color.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stats.subject.code.ifEmpty { stats.subject.name.take(2).uppercase() },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = color
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = stats.subject.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${stats.totalSections} Sections • ${stats.totalChapters} Chapters",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(color.copy(alpha = 0.12f))
                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${stats.completionPercentage}%",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearSyllabusBar(
                                    progress = stats.completionPercentage / 100f,
                                    height = 7.dp,
                                    barColor = color
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "✅ ${stats.completedChapters} Done",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "⏳ ${stats.inProgressChapters} Active",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (stats.weakChapters > 0) {
                                            Text(
                                                text = "🔴 ${stats.weakChapters} Weak",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StatusWeak
                                            )
                                        }
                                    }

                                    if (stats.pyqAttempted > 0) {
                                        Text(
                                            text = "PYQ: ${stats.pyqAccuracy}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (stats.pyqAccuracy >= 70) StatusCompleted else StatusWeak
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HomeDashboardTab.INTELLIGENCE -> {
                    // 1. ADAPTIVE EXAM PACE & RECOVERY CARD
                    item {
                        AdaptiveExamPaceCard(
                            pace = intelligenceSnapshot.pace,
                            targetExamName = appSettings.targetExam,
                            onEditExamDate = { showEditExamDialog = true }
                        )
                    }

                    // 2. EXAM READINESS ENGINE CARD
                    item {
                        ExamReadinessCard(
                            readiness = intelligenceSnapshot.readiness,
                            lastDaysMode = intelligenceSnapshot.lastDaysMode
                        )
                    }

                    // 3. PERFORMANCE TREND & FEEDBACK CARD
                    item {
                        PerformanceTrendDashboardCard(
                            trendResult = intelligenceSnapshot.performanceTrends,
                            weeklyReport = intelligenceSnapshot.weeklyReport,
                            recurringMistakes = intelligenceSnapshot.recurringMistakes,
                            onOpenWeeklyReport = { showWeeklyReportDialog = true }
                        )
                    }

                    // 4. DAILY MINDSET & COGNITIVE STUDY HACKS
                    item {
                        com.example.ui.components.DailyMindsetCard(
                            userName = appSettings.userName,
                            currentAmbient = ambientSound,
                            isAmbientPlaying = isAmbientPlaying,
                            onSelectAmbient = { type -> timerViewModel.selectAmbientSound(type) },
                            onToggleAmbient = { timerViewModel.toggleAmbientPlayPause() }
                        )
                    }
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

                    // Quick suggestions
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

    if (showWeeklyReportDialog && intelligenceSnapshot.weeklyReport != null) {
        WeeklyReportDialog(
            report = intelligenceSnapshot.weeklyReport!!,
            onDismiss = { showWeeklyReportDialog = false }
        )
    }
}

@Composable
fun GamifiedAspirantCard(
    streakDays: Int,
    totalStudyMins: Int,
    completedChapters: Int,
    totalChapters: Int,
    mockCount: Int,
    resolvedMistakesCount: Int,
    modifier: Modifier = Modifier
) {
    // 1. Calculate badge states
    val isMockKing = mockCount >= 3
    val isErrorEliminator = resolvedMistakesCount >= 5
    val isStudyMonk = totalStudyMins >= 180
    val isSyllabusConqueror = if (totalChapters > 0) (completedChapters.toFloat() / totalChapters) >= 0.50f else false

    // 2. Calculate Aspirant Level
    var unlockedCount = 0
    if (isMockKing) unlockedCount++
    if (isErrorEliminator) unlockedCount++
    if (isStudyMonk) unlockedCount++
    if (isSyllabusConqueror) unlockedCount++

    val (levelTitle, levelDesc, levelIcon) = when (unlockedCount) {
        0 -> Triple("Beginner Aspirant (आरंभिक छात्र)", "Kickstart your prep! Log study sessions & mock tests to unlock your first badge.", "🌱")
        1 -> Triple("Active Warrior (सक्रिय योद्धा)", "Keep going! You're building solid habit loops. Log more mock analysis.", "🛡️")
        2 -> Triple("Dedicated Scholar (समर्पित साधक)", "Impressive dedication! You are tackling weaknesses and mistakes.", "📖")
        3 -> Triple("Expert Competitor (कुशल प्रतियोगी)", "Superb! You are in the top tier of active aspirants. Keep pushing!", "⚡")
        else -> Triple("Ultimate Syllabus Conqueror (अपराजेय सम्राट)", "Incredible! All badges unlocked. You are completely ready to ace the exam!", "👑")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Level and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BrandTerracotta.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(levelIcon, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Level $unlockedCount: $levelTitle",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandTerracotta
                        )
                        Text(
                            text = levelDesc,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Streak Flame & Progress Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (streakDays >= 8) listOf(Color(0xFFFFD700), Color(0xFFFF4500).copy(alpha = 0.2f))
                                    else if (streakDays >= 4) listOf(Color(0xFFC0C0C0), Color(0xFFFF4500).copy(alpha = 0.15f))
                                    else listOf(Color(0xFFCD7F32), Color(0xFFFF4500).copy(alpha = 0.1f))
                                )
                            )
                    ) {
                        Text(
                            text = if (streakDays >= 8) "🔥" else if (streakDays >= 4) "⚡" else "🔥",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$streakDays Day Streak",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrandForestGreen.copy(alpha = 0.12f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (streakDays >= 8) "GOLDEN GLOW" else if (streakDays >= 4) "SILVER RUSH" else "BRONZE HABIT",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandForestGreen
                                )
                            }
                        }
                        Text(
                            text = "Longest: ${streakDays + 5} days streak record",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges Showcase Grid
            Text(
                text = "🏆 Unlocked Badges (${unlockedCount}/4)",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 5.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BadgeItem(
                    title = "Mock King",
                    hindi = "मॉक किंग",
                    emoji = "🥇",
                    isUnlocked = isMockKing,
                    progress = "$mockCount/3",
                    modifier = Modifier.weight(1f)
                )

                BadgeItem(
                    title = "Error Slayer",
                    hindi = "गलती सुधारक",
                    emoji = "🧠",
                    isUnlocked = isErrorEliminator,
                    progress = "$resolvedMistakesCount/5",
                    modifier = Modifier.weight(1f)
                )

                BadgeItem(
                    title = "Study Monk",
                    hindi = "तपस्वी",
                    emoji = "⏳",
                    isUnlocked = isStudyMonk,
                    progress = "${totalStudyMins}m/180m",
                    modifier = Modifier.weight(1f)
                )

                val currentPct = if (totalChapters > 0) ((completedChapters.toFloat() / totalChapters) * 100).toInt() else 0
                BadgeItem(
                    title = "Conqueror",
                    hindi = "विजेता",
                    emoji = "🎯",
                    isUnlocked = isSyllabusConqueror,
                    progress = "$currentPct%/50%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BadgeItem(
    title: String,
    hindi: String,
    emoji: String,
    isUnlocked: Boolean,
    progress: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) BrandForestGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) BrandForestGreen.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 3.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) BrandForestGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)
                    )
            ) {
                Text(
                    text = emoji,
                    fontSize = 16.sp,
                    color = if (isUnlocked) Color.Unspecified else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) BrandForestGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = hindi,
                fontSize = 7.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = progress,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) BrandForestGreen else Color.Gray
            )
        }
    }
}
