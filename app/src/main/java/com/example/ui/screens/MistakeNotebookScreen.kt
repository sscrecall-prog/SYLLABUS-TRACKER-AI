package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MistakeCategory
import com.example.data.model.MistakeEntry
import com.example.data.model.MistakeResolutionStatus
import com.example.ui.components.AddEditMistakeDialog
import com.example.ui.components.MistakeEntryCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.MistakeNotebookViewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MistakeNotebookScreen(
    modifier: Modifier = Modifier
) {
    val mistakeNotebookViewModel: MistakeNotebookViewModel = viewModel()
    val subjectViewModel: SubjectViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()

    val uiState by mistakeNotebookViewModel.uiState.collectAsState()
    val mistakes = uiState.filteredMistakes
    val allMistakesList = uiState.allMistakes
    val mistakeStats = uiState.mistakeStats
    val subjects by subjectViewModel.subjects.collectAsState()
    val selectedSubjectFilter = uiState.subjectId
    val selectedCategoryFilter = uiState.category
    val selectedStatusFilter = uiState.status
    val onlyStarredFilter = uiState.onlyStarred
    val onlyReviewDueFilter = uiState.onlyReviewDue
    val searchQuery = uiState.query

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingMistake by remember { mutableStateOf<MistakeEntry?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<MistakeEntry?>(null) }
    var showQuizReviewMode by remember { mutableStateOf(false) }

    // Android System Back & App Back Navigation integration
    BackHandler {
        mainViewModel.navigateBack()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("mistake_notebook_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Digital Error Diary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${mistakes.size} mistakes logged • ${mistakeStats.reviewDueCount} due for review",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { mainViewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button_mistake_screen")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Practice / Flashcard review button if there are mistakes
                    if (allMistakesList.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = { showQuizReviewMode = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("btn_drill_review_mode")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Drill Due", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingMistake = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_mistake")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Log Mistake")
                    Text("Log Mistake", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. Error Analytics & Vulnerability Overview Card
            item {
                MistakeAnalyticsSummaryCard(
                    stats = mistakeStats,
                    onDueReviewClick = { mistakeNotebookViewModel.toggleMistakeFilterReviewDue() },
                    isDueFiltered = onlyReviewDueFilter,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 2. Search & Filter Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { mistakeNotebookViewModel.setMistakeSearchQuery(it) },
                        placeholder = { Text("Search question, concept, formula...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { mistakeNotebookViewModel.setMistakeSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mistake_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips Row: Quick Filters
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        // Review Due Chip
                        item {
                            FilterChip(
                                selected = onlyReviewDueFilter,
                                onClick = { mistakeNotebookViewModel.toggleMistakeFilterReviewDue() },
                                label = { Text("⏰ Due Review (${mistakeStats.reviewDueCount})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StatusWeak.copy(alpha = 0.2f),
                                    selectedLabelColor = StatusWeak
                                )
                            )
                        }

                        // Starred Chip
                        item {
                            FilterChip(
                                selected = onlyStarredFilter,
                                onClick = { mistakeNotebookViewModel.toggleMistakeFilterStarred() },
                                label = { Text("⭐ Starred (${mistakeStats.starredCount})") }
                            )
                        }

                        // Subject Filter Chips
                        item {
                            FilterChip(
                                selected = selectedSubjectFilter == null,
                                onClick = { mistakeNotebookViewModel.setMistakeFilterSubjectId(null) },
                                label = { Text("All Subjects") }
                            )
                        }

                        items(subjects) { sub ->
                            FilterChip(
                                selected = selectedSubjectFilter == sub.id,
                                onClick = {
                                    mistakeNotebookViewModel.setMistakeFilterSubjectId(
                                        if (selectedSubjectFilter == sub.id) null else sub.id
                                    )
                                },
                                label = { Text(sub.name) }
                            )
                        }
                    }

                    // Category Filter Pills Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { mistakeNotebookViewModel.setMistakeFilterCategory(null) },
                                label = { Text("All Causes") }
                            )
                        }

                        items(MistakeCategory.values()) { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = {
                                    mistakeNotebookViewModel.setMistakeFilterCategory(
                                        if (selectedCategoryFilter == cat) null else cat
                                    )
                                },
                                label = { Text("${cat.emoji} ${cat.label}") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3. Mistake Entries List or Empty State
            if (mistakes.isEmpty()) {
                item {
                    MistakeEmptyState(
                        hasAnyMistake = allMistakesList.isNotEmpty(),
                        onAddClick = {
                            editingMistake = null
                            showAddEditDialog = true
                        },
                        onClearFilters = {
                            mistakeNotebookViewModel.setMistakeFilterSubjectId(null)
                            mistakeNotebookViewModel.setMistakeFilterCategory(null)
                            mistakeNotebookViewModel.setMistakeFilterStatus(null)
                            mistakeNotebookViewModel.setMistakeSearchQuery("")
                            if (onlyStarredFilter) mistakeNotebookViewModel.toggleMistakeFilterStarred()
                            if (onlyReviewDueFilter) mistakeNotebookViewModel.toggleMistakeFilterReviewDue()
                        }
                    )
                }
            } else {
                items(
                    items = mistakes,
                    key = { it.id }
                ) { mistake ->
                    MistakeEntryCard(
                        mistake = mistake,
                        onCardClick = {
                            editingMistake = mistake
                            showAddEditDialog = true
                        },
                        onStatusChange = { newStatus ->
                            mistakeNotebookViewModel.markMistakeReviewed(mistake, newStatus)
                        },
                        onToggleStar = {
                            mistakeNotebookViewModel.toggleMistakeStar(mistake)
                        },
                        onDelete = {
                            showDeleteConfirmDialog = mistake
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .animateItemPlacement()
                    )
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditMistakeDialog(
            subjects = subjects,
            initialMistake = editingMistake,
            onDismiss = { showAddEditDialog = false },
            onSave = { q, w, c, exp, subId, subName, chapter, src, cat, star, tags ->
                if (editingMistake != null) {
                    val updated = editingMistake!!.copy(
                        questionText = q,
                        yourWrongAnswer = w,
                        correctAnswer = c,
                        explanationOrKeyConcept = exp,
                        subjectId = subId,
                        subjectName = subName,
                        chapterTitle = chapter,
                        sourceMockOrBook = src,
                        category = cat,
                        importanceStar = star,
                        tagsCsv = tags
                    )
                    mistakeNotebookViewModel.updateMistake(updated)
                } else {
                    mistakeNotebookViewModel.addMistake(MistakeEntry(
                        questionText = q,
                        yourWrongAnswer = w,
                        correctAnswer = c,
                        explanationOrKeyConcept = exp,
                        subjectId = subId,
                        subjectName = subName,
                        chapterTitle = chapter,
                        sourceMockOrBook = src,
                        category = cat,
                        importanceStar = star,
                        tagsCsv = tags
                    ))
                }
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { mistakeToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Mistake Entry?") },
            text = { Text("Are you sure you want to permanently remove this error entry from your Error Diary?") },
            confirmButton = {
                Button(
                    onClick = {
                        mistakeNotebookViewModel.deleteMistake(mistakeToDelete)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Interactive Drill Review Dialog / Flashcard Mode
    if (showQuizReviewMode) {
        MistakeDrillDialog(
            mistakes = mistakes.filter { it.resolutionStatus != MistakeResolutionStatus.MASTERED },
            onDismiss = { showQuizReviewMode = false },
            onMarkStatus = { mistake, newStatus ->
                mistakeNotebookViewModel.markMistakeReviewed(mistake, newStatus)
            }
        )
    }
}

@Composable
fun MistakeAnalyticsSummaryCard(
    stats: com.example.data.model.MistakeStats,
    onDueReviewClick: () -> Unit,
    isDueFiltered: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "Mistake Pattern Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${stats.resolutionRatePercent}% Resolved",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Mini Stat Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    label = "Active",
                    value = "${stats.activeMistakesCount}",
                    emoji = "🔴",
                    color = StatusWeak,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Understood",
                    value = "${stats.understoodCount}",
                    emoji = "🟡",
                    color = StatusInProgress,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Mastered",
                    value = "${stats.masteredCount}",
                    emoji = "🟢",
                    color = StatusCompleted,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Due Today",
                    value = "${stats.reviewDueCount}",
                    emoji = "⏰",
                    color = if (stats.reviewDueCount > 0) StatusWeak else StatusCompleted,
                    onClick = onDueReviewClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Root Cause Insights banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Top Trap Area",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stats.mostVulnerableSubject,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (stats.sillyMistakesPercent > 0) {
                            CauseBadge("Silly slips", "${stats.sillyMistakesPercent}%", "🤦")
                        }
                        if (stats.formulaForgotPercent > 0) {
                            CauseBadge("Formulas", "${stats.formulaForgotPercent}%", "🧠")
                        }
                        if (stats.conceptGapPercent > 0) {
                            CauseBadge("Concept gaps", "${stats.conceptGapPercent}%", "🧩")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CauseBadge(label: String, pct: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$emoji $pct", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    emoji: String,
    color: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$emoji $value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun MistakeEmptyState(
    hasAnyMistake: Boolean,
    onAddClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (hasAnyMistake) "🔍" else "📓", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hasAnyMistake) "No Mistakes Matching Filter" else "Your Error Diary is Empty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (hasAnyMistake)
                "Try adjusting your search keywords, category filter, or subject filter."
            else
                "When practicing PYQs or attempting Mock Tests, log questions you solved wrong or took too long on. Master them with spaced repetition.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (hasAnyMistake) {
            OutlinedButton(onClick = onClearFilters) {
                Text("Clear All Filters")
            }
        } else {
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log First Mistake")
            }
        }
    }
}

@Composable
fun MistakeDrillDialog(
    mistakes: List<MistakeEntry>,
    onDismiss: () -> Unit,
    onMarkStatus: (MistakeEntry, MistakeResolutionStatus) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var isAnswerRevealed by remember { mutableStateOf(false) }

    val currentMistake = mistakes.getOrNull(currentIndex)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🧠 Active Recall Drill", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (mistakes.isNotEmpty()) {
                    Text(
                        "${currentIndex + 1} of ${mistakes.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            if (currentMistake == null || mistakes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("All Due Mistakes Reviewed!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Great job keeping your Error Diary clean and mastered.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category & Subject Tag
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(currentMistake.subjectName, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("${currentMistake.category.emoji} ${currentMistake.category.label}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Text(
                        text = currentMistake.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!isAnswerRevealed) {
                        Button(
                            onClick = { isAnswerRevealed = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reveal Answer & Concept")
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("✅ Correct Answer: ${currentMistake.correctAnswer}", fontWeight = FontWeight.Bold, color = StatusCompleted)
                                if (currentMistake.explanationOrKeyConcept.isNotBlank()) {
                                    Text("💡 ${currentMistake.explanationOrKeyConcept}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Text("How well did you recall this?", style = MaterialTheme.typography.labelMedium)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onMarkStatus(currentMistake, MistakeResolutionStatus.ACTIVE)
                                    if (currentIndex < mistakes.size - 1) {
                                        currentIndex++
                                        isAnswerRevealed = false
                                    } else onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Still Weak", fontSize = 11.sp, color = StatusWeak)
                            }

                            Button(
                                onClick = {
                                    onMarkStatus(currentMistake, MistakeResolutionStatus.MASTERED)
                                    if (currentIndex < mistakes.size - 1) {
                                        currentIndex++
                                        isAnswerRevealed = false
                                    } else onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted)
                            ) {
                                Text("Mastered! ⭐", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
