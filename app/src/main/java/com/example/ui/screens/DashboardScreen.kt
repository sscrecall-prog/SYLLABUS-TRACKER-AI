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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    val studySessions by timerViewModel.studySessions.collectAsState()
    val todayPlans by plannerViewModel.todayPlans.collectAsState()
    val searchQuery by syllabusViewModel.searchQuery.collectAsState()
    val mockTests by mockTestsViewModel.mockTests.collectAsState()
    val mistakeStats by mistakeNotebookViewModel.mistakeStats.collectAsState()
    val appSettings by settingsViewModel.appSettings.collectAsState()
    val intelligenceSnapshot by intelligenceViewModel.snapshot.collectAsState()

    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f
    val focusManager = LocalFocusManager.current

    var showEditExamDialog by remember { mutableStateOf(false) }
    var selectedSearchFilter by remember { mutableStateOf(HomeSearchFilter.ALL) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var selectedDashboardGoalTab by remember { mutableIntStateOf(0) } // 0: Daily, 1: Weekly

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
        // 1. COMMAND CENTER TOP HEADER & TARGET EXAM CHIP
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
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "AI Study Command Center",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                // Target Exam Countdown Pill with Electric Cyan Glow
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.2.dp, ElectricBlue.copy(alpha = 0.4f)),
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
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = appSettings.targetExam.ifEmpty { "Target Exam" },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (intelligenceSnapshot.pace.daysRemaining > 0) {
                                Text(
                                    text = "${intelligenceSnapshot.pace.daysRemaining} days left",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Target Exam",
                            tint = ElectricBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // 2. MODERN ELEVATED GLASS SEARCH BAR WITH DYNAMIC GLOW & QUICK SUGGESTIONS
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isSearching || isSearchFocused) 10.dp else 3.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = if (isDark) Color.Black.copy(alpha = 0.7f) else Color(0x180F172A),
                            spotColor = if (isSearching || isSearchFocused) ElectricBlueGlow else if (isDark) Color.Black.copy(alpha = 0.6f) else Color(0x140F172A)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    color = if (isDark) DarkSurface else colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSearching || isSearchFocused) 1.5.dp else 1.dp,
                        color = if (isSearching || isSearchFocused) ElectricBlue else if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSearching || isSearchFocused) ElectricBlue.copy(alpha = 0.15f)
                                    else if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearching || isSearchFocused) ElectricBlue else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        TextField(
                            value = searchQuery,
                            onValueChange = { syllabusViewModel.searchQuery.value = it },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isSearchFocused = it.isFocused }
                                .testTag("home_search_bar"),
                            placeholder = {
                                Text(
                                    text = "Search chapters, PYQs, subjects, topics...",
                                    fontSize = 13.5.sp,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = colorScheme.onSurface,
                                unfocusedTextColor = colorScheme.onSurface
                            )
                        )

                        // Match Count Badge when searching
                        if (isSearching && totalMatchesCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ElectricBlue.copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = "$totalMatchesCount found",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (isSearching) {
                            IconButton(
                                onClick = {
                                    syllabusViewModel.searchQuery.value = ""
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .testTag("home_search_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
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
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricBlue.copy(alpha = 0.22f),
                                    selectedLabelColor = ElectricBlue,
                                    containerColor = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    labelColor = colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) ElectricBlue else if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                }

                // Quick Search Suggestions (When NOT searching)
                AnimatedVisibility(
                    visible = !isSearching,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val weakCount = items.count { it.isWeak || it.status == ChapterStatus.WEAK }
                    val revDueCount = items.count { it.isRevisionDue || it.status == ChapterStatus.REVISION_DUE }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                    ) {
                        if (weakCount > 0) {
                            item {
                                SuggestionChip(
                                    onClick = { syllabusViewModel.searchQuery.value = "weak" },
                                    label = { Text("⚡ Weak Topics ($weakCount)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = WarningOrange) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.4f)),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = WarningOrange.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                        if (revDueCount > 0) {
                            item {
                                SuggestionChip(
                                    onClick = { syllabusViewModel.searchQuery.value = "revision" },
                                    label = { Text("⏰ Revision Due ($revDueCount)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AmberGold) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = AmberGold.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                        item {
                            SuggestionChip(
                                onClick = { syllabusViewModel.searchQuery.value = "pyq" },
                                label = { Text("📝 PYQ Practice", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = ElectricBlue) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.4f)),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = ElectricBlue.copy(alpha = 0.1f)
                                )
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = { syllabusViewModel.searchQuery.value = "math" },
                                label = { Text("📐 Mathematics", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurfaceVariant) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = { syllabusViewModel.searchQuery.value = "gs" },
                                label = { Text("🌍 General Studies", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurfaceVariant) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // CONDITIONAL CONTENT: SEARCH RESULTS OR CLEAN DASHBOARD
        if (isSearching) {
            // === SEARCH RESULTS VIEW ===
            if (totalMatchesCount == 0) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("home_search_no_results"),
                        shape = RoundedCornerShape(20.dp)
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
                                    .background(if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No results matching \"$trimmedQuery\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching for a subject name, chapter, or revision keyword.",
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { syllabusViewModel.searchQuery.value = "" },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color(0xFF071B2B))
                            ) {
                                Text("Clear Search", fontWeight = FontWeight.Bold)
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
                            color = colorScheme.onSurface
                        )
                    }

                    items(matchedSubjects) { subject ->
                        val stats = subjectStats.find { it.subject.id == subject.id }
                        val subjectColor = try {
                            Color(android.graphics.Color.parseColor(subject.colorHex))
                        } catch (e: Exception) {
                            ElectricBlue
                        }

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSubject(subject.id) }
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
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(subjectColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getSubjectIcon(subject.iconName),
                                            contentDescription = null,
                                            tint = subjectColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = subject.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${stats?.totalChapters ?: 0} chapters • ${stats?.completionPercentage ?: 0}% done",
                                            fontSize = 11.sp,
                                            color = colorScheme.onSurfaceVariant
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
                            color = colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(matchedChapters) { chapter ->
                        val parentSubject = subjects.find { it.id == chapter.subjectId }
                        val subjectColor = parentSubject?.let {
                            try {
                                Color(android.graphics.Color.parseColor(it.colorHex))
                            } catch (e: Exception) {
                                ElectricBlue
                            }
                        } ?: ElectricBlue

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { syllabusViewModel.selectChapter(chapter) }
                                .testTag("search_result_chapter_${chapter.id}"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
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
                                    color = colorScheme.onSurface
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
                                        color = colorScheme.onSurfaceVariant
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
                                            tint = ElectricBlue,
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
            // === COMMAND CENTER DASHBOARD ===

            if (subjects.isEmpty() && items.isEmpty()) {
                item {
                    DashboardEmptyStateCard(
                        onLoadSampleSyllabus = { settingsViewModel.resetData() },
                        onAddSubject = { onNavigate(NavDestination.SUBJECTS) },
                        onSetExamTarget = { showEditExamDialog = true },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                // 3. MASTER HERO CARD: Obsidian & Cyan Gradient with Radial Progress Ring
                item {
                    GradientCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_hero_bento"),
                        shape = RoundedCornerShape(24.dp),
                        colors = if (isDark) listOf(Color(0xFF192538), Color(0xFF131B2A)) else listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE)),
                        borderColor = if (isDark) ElectricBlue.copy(alpha = 0.35f) else ElectricBlue.copy(alpha = 0.4f)
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
                                                .background(ElectricBlue.copy(alpha = 0.2f))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "SYLLABUS STATUS",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isDark) ElectricBlue else ElectricBlueDark,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SoftMint.copy(alpha = if (isDark) 0.18f else 0.4f))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "🔥 ${overallStats.currentStreakDays}d Streak",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) SoftMint else Color(0xFF047857)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Overall Preparation",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${overallStats.completedChapters} of ${overallStats.totalChapters} chapters completed",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }

                                // Glowing Radial Progress Ring
                                ProgressRing(
                                    progress = overallStats.completionPercentage / 100f,
                                    size = 80.dp,
                                    strokeWidth = 8.dp,
                                    primaryColor = ElectricBlue,
                                    secondaryColor = SoftMint,
                                    backgroundColor = if (isDark) Color(0xFF1E2838) else colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${overallStats.completionPercentage}%",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = colorScheme.onSurface
                                        )
                                        Text(
                                            text = "READY",
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) SoftMint else Color(0xFF059669)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Micro-Metrics Strip inside Hero
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDark) Color.Black.copy(alpha = 0.35f) else colorScheme.surface.copy(alpha = 0.8f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("In Progress", fontSize = 9.5.sp, color = colorScheme.onSurfaceVariant)
                                    Text("${overallStats.inProgressChapters}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                }
                                VerticalDivider(modifier = Modifier.height(18.dp), color = if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Revision Due", fontSize = 9.5.sp, color = colorScheme.onSurfaceVariant)
                                    Text("${overallStats.revisionDueChapters}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))
                                }
                                VerticalDivider(modifier = Modifier.height(18.dp), color = if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Weak Topics", fontSize = 9.5.sp, color = colorScheme.onSurfaceVariant)
                                    Text("${overallStats.weakChapters}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                                }
                                VerticalDivider(modifier = Modifier.height(18.dp), color = if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Study Time", fontSize = 9.5.sp, color = colorScheme.onSurfaceVariant)
                                    Text("${overallStats.todayStudyMinutes}m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) SoftMint else Color(0xFF059669))
                                }
                            }
                        }
                    }
                }

                // 3.4 POMODORO FOCUS SESSION CARD
                item {
                    PomodoroFocusSessionCard(
                        timerViewModel = timerViewModel,
                        subjects = subjects,
                        items = items,
                        onOpenFullTimer = { onNavigate(NavDestination.TIMER) }
                    )
                }

                // 3.5 DAILY & WEEKLY STUDY GOAL PROGRESS TRACKER SECTION
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color.Black.copy(alpha = 0.3f) else colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("☀️ Daily Target", "📅 Weekly Target").forEachIndexed { index, label ->
                                val isSelected = selectedDashboardGoalTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ElectricBlue else Color.Transparent)
                                        .clickable { selectedDashboardGoalTab = index }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF071B2B) else colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (selectedDashboardGoalTab == 0) {
                            DailyStudyGoalCard(
                                dailyTargetMinutes = appSettings.dailyTargetMinutes,
                                studySessions = studySessions,
                                onUpdateDailyTargetHours = { hours ->
                                    settingsViewModel.updateDailyTargetHours(hours)
                                },
                                onStartTimer = { onNavigate(NavDestination.TIMER) }
                            )
                        } else {
                            WeeklyStudyGoalCard(
                                weeklyTargetMinutes = appSettings.weeklyTargetMinutes,
                                studySessions = studySessions,
                                onUpdateWeeklyTargetHours = { hours ->
                                    settingsViewModel.updateWeeklyTargetHours(hours)
                                },
                                onStartTimer = { onNavigate(NavDestination.TIMER) }
                            )
                        }
                    }
                }

            // 4. SUBJECT PROGRESS COMMAND ROW (GS, English, Reasoning, Maths, Computer etc.)
            if (subjectStats.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subject Mastery",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            TextButton(
                                onClick = { onNavigate(NavDestination.SUBJECTS) },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("All Subjects", fontSize = 12.sp, color = ElectricBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(subjectStats) { stats ->
                                val subColor = try {
                                    Color(android.graphics.Color.parseColor(stats.subject.colorHex))
                                } catch (e: Exception) {
                                    ElectricBlue
                                }

                                BentoCard(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .testTag("home_subject_card_${stats.subject.id}"),
                                    shape = RoundedCornerShape(18.dp),
                                    accentColor = subColor,
                                    onClick = {
                                        onOpenSubject(stats.subject.id)
                                        onNavigate(NavDestination.SYLLABUS)
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(subColor.copy(alpha = 0.16f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = getSubjectIcon(stats.subject.iconName),
                                                    contentDescription = null,
                                                    tint = subColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Text(
                                                text = "${stats.completionPercentage}%",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = subColor
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = stats.subject.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${stats.completedChapters}/${stats.totalChapters} done",
                                            fontSize = 10.sp,
                                            color = colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LinearSyllabusBar(
                                            progress = stats.completionPercentage / 100f,
                                            height = 4.dp,
                                            barColor = subColor,
                                            backgroundColor = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. EXECUTIVE 4-TILE QUICK ACCESS GRID (Equal Golden Aspect Ratio & Uniform Proportions)
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
                            iconColor = ElectricBlue,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.35f)
                                .testTag("bento_subjects_tile"),
                            onClick = { onNavigate(NavDestination.SUBJECTS) }
                        )

                        BentoActionTile(
                            title = "Spaced Revision",
                            subtitle = "${overallStats.revisionDueChapters} chapters due",
                            badgeText = if (overallStats.revisionDueChapters > 0) "${overallStats.revisionDueChapters} DUE" else "REVISE",
                            icon = Icons.Default.Update,
                            iconColor = Color(0xFFA78BFA),
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.35f)
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
                            iconColor = SoftMintDark,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.35f)
                                .testTag("bento_planner_tile"),
                            onClick = { onNavigate(NavDestination.PLANNER) }
                        )

                        BentoActionTile(
                            title = "Mock Tests & Errors",
                            subtitle = "${mockTests.size} Tests • ${mistakeStats.totalMistakesCount} Logs",
                            badgeText = "PRACTICE",
                            icon = Icons.Default.Quiz,
                            iconColor = AlertRed,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.35f)
                                .testTag("bento_mocks_tile"),
                            onClick = { onNavigate(NavDestination.MOCK_TESTS) }
                        )
                    }
                }
            }

            // 6. TODAY'S PRIORITY FOCUS CARD (Clean, compact, actionable)
            if (priorityFocusChapters.isNotEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("priority_focus_card"),
                        shape = RoundedCornerShape(20.dp)
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
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Today's Priority Focus",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                }

                                TextButton(
                                    onClick = { onNavigate(NavDestination.REVISION) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "View All (${overallStats.revisionDueChapters + overallStats.weakChapters})",
                                        fontSize = 11.5.sp,
                                        color = ElectricBlue
                                    )
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
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                                                color = colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${parentSubject?.name ?: "Subject"} • ${if (isWeak) "Weak Topic" else "Revision Due"}",
                                                fontSize = 10.sp,
                                                color = if (isWeak) AlertRed else Color(0xFFA78BFA),
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
                                                tint = ElectricBlue,
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
}

    // Target Exam Edit Dialog
    if (showEditExamDialog) {
        var examNameInput by remember { mutableStateOf(appSettings.targetExam) }
        var targetDateInput by remember { mutableStateOf(appSettings.targetExamDateStr) }

        AlertDialog(
            onDismissRequest = { showEditExamDialog = false },
            containerColor = if (isDark) DarkSurface else colorScheme.surface,
            title = {
                Text(
                    text = "🎯 Set Target Exam & Date",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colorScheme.onSurface
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
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = if (isDark) DarkGlassBorder else colorScheme.outlineVariant,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface
                        )
                    )

                    OutlinedTextField(
                        value = targetDateInput,
                        onValueChange = { targetDateInput = it },
                        label = { Text("Exam Date (YYYY-MM-DD)") },
                        singleLine = true,
                        placeholder = { Text("2026-10-20") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = if (isDark) DarkGlassBorder else colorScheme.outlineVariant,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface
                        )
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color(0xFF071B2B))
                ) {
                    Text("Save Target", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditExamDialog = false }) {
                    Text("Cancel", color = colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
