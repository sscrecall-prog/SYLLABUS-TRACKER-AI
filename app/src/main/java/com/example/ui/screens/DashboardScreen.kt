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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ChapterStatus
import com.example.data.model.Subject
import com.example.data.model.SyllabusItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

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
    val intelligenceViewModel: IntelligenceViewModel = viewModel()

    val overallStats by analyticsViewModel.overallStats.collectAsState()
    val subjectStats by subjectViewModel.subjectStatsList.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()
    val items by syllabusViewModel.items.collectAsState()
    val todayPlans by plannerViewModel.todayPlans.collectAsState()
    val searchQuery by syllabusViewModel.searchQuery.collectAsState()
    val mockTests by mockTestsViewModel.mockTests.collectAsState()
    val mistakeStats by mistakeNotebookViewModel.mistakeStats.collectAsState()
    val appSettings by settingsViewModel.appSettings.collectAsState()
    val intelligenceSnapshot by intelligenceViewModel.snapshot.collectAsState()

    var showEditExamDialog by remember { mutableStateOf(false) }
    var selectedSearchFilter by remember { mutableStateOf(HomeSearchFilter.ALL) }

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

    // High Priority Focus Items (Due for revision or weak)
    val priorityFocusChapters = remember(items) {
        items.filter { it.isRevisionDue || it.status == ChapterStatus.REVISION_DUE || it.isWeak || it.status == ChapterStatus.WEAK }
            .take(4)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp)
    ) {
        // 1. PROFESSIONAL TOP HEADER & TARGET EXAM CHIP
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (appSettings.userName.isNotBlank()) "Hello, ${appSettings.userName} 👋" else "Hello, Aspirant 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Your Syllabus & Exam Command Center",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Target Exam Countdown Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BrandForestGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.2.dp, BrandForestGreen.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clickable { showEditExamDialog = true }
                        .testTag("target_exam_header_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = BrandForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = appSettings.targetExam.ifEmpty { "Target Exam" },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandForestGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (intelligenceSnapshot.pace.daysRemaining > 0) {
                                Text(
                                    text = "${intelligenceSnapshot.pace.daysRemaining} days left",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Target Exam",
                            tint = BrandForestGreen.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // 2. ULTRA-PROFESSIONAL SEARCH BAR
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isSearching) 6.dp else 2.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSearching) 1.5.dp else 1.dp,
                        color = if (isSearching) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearching) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        TextField(
                            value = searchQuery,
                            onValueChange = { syllabusViewModel.searchQuery.value = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_search_bar"),
                            placeholder = {
                                Text(
                                    text = "Search chapters, PYQs, subjects, topics...",
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        if (isSearching) {
                            IconButton(
                                onClick = { syllabusViewModel.searchQuery.value = "" },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("home_search_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Instant Filter Chips (When Searching)
                AnimatedVisibility(
                    visible = isSearching,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
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

        // CONDITIONAL CONTENT: SEARCH RESULTS OR CLEAN DASHBOARD
        if (isSearching) {
            // === CLEAN SEARCH RESULTS VIEW ===
            if (totalMatchesCount == 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("home_search_no_results"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No results matching \"$trimmedQuery\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching for a subject name, chapter, or revision keyword.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
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
                // Matching Subjects
                if (matchedSubjects.isNotEmpty() && (selectedSearchFilter == HomeSearchFilter.ALL || selectedSearchFilter == HomeSearchFilter.SUBJECTS)) {
                    item {
                        Text(
                            text = "📚 Matching Subjects (${matchedSubjects.size})",
                            fontSize = 14.sp,
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

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSubject(subject.id) }
                                .testTag("search_result_subject_${subject.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, subjectColor.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(subjectColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getSubjectIcon(subject.iconName),
                                            contentDescription = null,
                                            tint = subjectColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = subject.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${stats?.totalChapters ?: 0} chapters • ${stats?.completionPercentage ?: 0}% done",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = subjectColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Matching Chapters
                if (matchedChapters.isNotEmpty() && selectedSearchFilter != HomeSearchFilter.SUBJECTS) {
                    item {
                        Text(
                            text = "📝 Matching Chapters (${matchedChapters.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
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

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { syllabusViewModel.selectChapter(chapter) }
                                .testTag("search_result_chapter_${chapter.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
                                        Text(
                                            text = parentSubject.name.uppercase(),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = subjectColor
                                        )
                                    }
                                    StatusBadge(status = chapter.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = chapter.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Confidence: ${chapter.confidence}/5 ★ • PYQs: ${chapter.pyqCorrect}/${chapter.pyqAttempted}",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(
                                        onClick = {
                                            timerViewModel.setTimerTargetById(parentSubject?.id, chapter.id)
                                            onNavigate(NavDestination.TIMER)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = "Start Timer",
                                            tint = BrandTerracotta,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // === CLEAN & FOCUSED DASHBOARD ===

            // 3. MASTER HERO CARD: Syllabus Mastery & Countdown
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
                                            text = "SYLLABUS MASTERY",
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
                                    text = "Overall Preparation",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandWarmCream
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${overallStats.completedChapters} of ${overallStats.totalChapters} chapters completed",
                                    fontSize = 12.sp,
                                    color = BrandCreamDark
                                )
                            }

                            // Circular Progress Ring
                            ProgressRing(
                                progress = overallStats.completionPercentage / 100f,
                                size = 76.dp,
                                strokeWidth = 7.dp,
                                primaryColor = BrandWarmCream,
                                secondaryColor = BrandTerracotta,
                                backgroundColor = Color.White.copy(alpha = 0.18f)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${overallStats.completionPercentage}%",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandWarmCream
                                    )
                                    Text(
                                        text = "READY",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandCreamLight
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Micro-Metrics Strip inside Hero
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
                                Text("In Progress", fontSize = 9.5.sp, color = BrandCreamDark)
                                Text("${overallStats.inProgressChapters}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusInProgress)
                            }
                            VerticalDivider(modifier = Modifier.height(18.dp), color = Color.White.copy(alpha = 0.15f))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Revision Due", fontSize = 9.5.sp, color = BrandCreamDark)
                                Text("${overallStats.revisionDueChapters}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusRevisionDue)
                            }
                            VerticalDivider(modifier = Modifier.height(18.dp), color = Color.White.copy(alpha = 0.15f))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Weak Topics", fontSize = 9.5.sp, color = BrandCreamDark)
                                Text("${overallStats.weakChapters}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusWeak)
                            }
                            VerticalDivider(modifier = Modifier.height(18.dp), color = Color.White.copy(alpha = 0.15f))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Study Time", fontSize = 9.5.sp, color = BrandCreamDark)
                                Text("${overallStats.todayStudyMinutes}m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandWarmCream)
                            }
                        }
                    }
                }
            }

            // 4. EXECUTIVE 4-TILE QUICK ACCESS GRID
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoActionTile(
                            title = "Syllabus Tracker",
                            subtitle = "${subjects.size} Subjects • ${overallStats.totalChapters} Ch",
                            badgeText = "EXPLORE",
                            icon = Icons.Default.MenuBook,
                            iconColor = BrandForestGreen,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bento_subjects_tile"),
                            onClick = { onNavigate(NavDestination.SUBJECTS) }
                        )

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
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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

                        BentoActionTile(
                            title = "Mock Tests & Errors",
                            subtitle = "${mockTests.size} Tests • ${mistakeStats.totalMistakesCount} Logs",
                            badgeText = "PRACTICE",
                            icon = Icons.Default.Quiz,
                            iconColor = BrandTerracotta,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bento_mocks_tile"),
                            onClick = { onNavigate(NavDestination.MOCK_TESTS) }
                        )
                    }
                }
            }

            // 5. TODAY'S PRIORITY FOCUS CARD (Clean, compact, actionable)
            if (priorityFocusChapters.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("priority_focus_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = StatusRevisionDue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Today's Priority Focus",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                TextButton(
                                    onClick = { onNavigate(NavDestination.REVISION) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("View All (${overallStats.revisionDueChapters + overallStats.weakChapters})", fontSize = 11.5.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                priorityFocusChapters.forEach { chapter ->
                                    val parentSubject = subjects.find { it.id == chapter.subjectId }
                                    val isWeak = chapter.isWeak || chapter.status == ChapterStatus.WEAK

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                            .clickable { syllabusViewModel.selectChapter(chapter) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = chapter.title,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${parentSubject?.name ?: "Subject"} • ${if (isWeak) "Weak Topic" else "Revision Due"}",
                                                fontSize = 10.sp,
                                                color = if (isWeak) StatusWeak else StatusRevisionDue,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                timerViewModel.setTimerTargetById(parentSubject?.id, chapter.id)
                                                onNavigate(NavDestination.TIMER)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Study Now",
                                                tint = BrandForestGreen,
                                                modifier = Modifier.size(18.dp)
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

    // Target Exam Edit Dialog
    if (showEditExamDialog) {
        var examNameInput by remember { mutableStateOf(appSettings.targetExam) }
        var targetDateInput by remember { mutableStateOf(appSettings.targetExamDateStr) }

        AlertDialog(
            onDismissRequest = { showEditExamDialog = false },
            title = {
                Text(
                    text = "🎯 Set Target Exam & Date",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = examNameInput,
                        onValueChange = { examNameInput = it },
                        label = { Text("Exam Name (e.g. SSC CGL 2026, UPSC, JEE)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetDateInput,
                        onValueChange = { targetDateInput = it },
                        label = { Text("Exam Date (YYYY-MM-DD)") },
                        singleLine = true,
                        placeholder = { Text("2026-10-20") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.updateExamTarget(
                            examName = examNameInput.trim(),
                            targetDateStr = targetDateInput.trim(),
                            examShift = appSettings.targetExamShift
                        )
                        showEditExamDialog = false
                    }
                ) {
                    Text("Save Target")
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
