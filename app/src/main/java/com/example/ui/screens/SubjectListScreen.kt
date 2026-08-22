package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavDestination
import com.example.ui.viewmodel.SubjectStats
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.TimerViewModel

enum class SubjectSortOption(val label: String) {
    DEFAULT("Default"),
    COMPLETION_DESC("Highest Progress"),
    COMPLETION_ASC("Lowest Progress"),
    NAME("Alphabetical"),
    TOPIC_COUNT("Most Chapters"),
    WEAK_FIRST("Needs Focus")
}

/**
 * Screen displaying the list of subjects from Room database in a LazyColumn
 * with comprehensive syllabus completion progress indicators, statistics,
 * and quick navigation actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    onNavigate: (NavDestination) -> Unit,
    onOpenSubject: (Long) -> Unit
) {
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val subjectViewModel: SubjectViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()

    val subjects by subjectViewModel.subjects.collectAsState()
    val subjectStatsList by subjectViewModel.subjectStatsList.collectAsState()
    val overallStats by analyticsViewModel.overallStats.collectAsState()

    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f
    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(SubjectSortOption.DEFAULT) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

    // Filter and Sort Subjects
    val filteredSubjectStats = remember(subjectStatsList, searchQuery, selectedSort) {
        var list = if (searchQuery.isBlank()) {
            subjectStatsList
        } else {
            val q = searchQuery.trim().lowercase()
            subjectStatsList.filter {
                it.subject.name.lowercase().contains(q) ||
                it.subject.code.lowercase().contains(q) ||
                it.subject.description.lowercase().contains(q)
            }
        }

        when (selectedSort) {
            SubjectSortOption.DEFAULT -> list
            SubjectSortOption.COMPLETION_DESC -> list.sortedByDescending { it.completionPercentage }
            SubjectSortOption.COMPLETION_ASC -> list.sortedBy { it.completionPercentage }
            SubjectSortOption.NAME -> list.sortedBy { it.subject.name.lowercase() }
            SubjectSortOption.TOPIC_COUNT -> list.sortedByDescending { it.totalChapters }
            SubjectSortOption.WEAK_FIRST -> list.sortedByDescending { it.weakChapters }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("subject_list_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. HEADER & OVERALL PROGRESS CARD
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    accentColor = ElectricBlue
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SYLLABUS PROGRESS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElectricBlue,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Subjects & Modules",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }

                            // Add subject button
                            FilledTonalButton(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = ElectricBlue.copy(alpha = 0.18f),
                                    contentColor = ElectricBlue
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("add_subject_top_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Subject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Aggregate syllabus progress indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${overallStats.completedChapters} of ${overallStats.totalChapters} chapters completed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${overallStats.completionPercentage}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SoftMint
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Large gradient progress bar
                        LinearSyllabusBar(
                            progress = overallStats.completionPercentage / 100f,
                            height = 9.dp,
                            barColor = ElectricBlue,
                            secondaryColor = SoftMint,
                            backgroundColor = if (isDark) Color(0xFF1E2634) else colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Stat Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiniMetricChip(
                                label = "Subjects",
                                value = "${subjects.size}",
                                icon = Icons.Default.School,
                                tint = ElectricBlue,
                                modifier = Modifier.weight(1f)
                            )
                            MiniMetricChip(
                                label = "Active",
                                value = "${overallStats.inProgressChapters}",
                                icon = Icons.Default.TrendingUp,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                            MiniMetricChip(
                                label = "Revision",
                                value = "${overallStats.revisionDueChapters}",
                                icon = Icons.Default.Update,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.weight(1f)
                            )
                            MiniMetricChip(
                                label = "Weak",
                                value = "${overallStats.weakChapters}",
                                icon = Icons.Default.ReportProblem,
                                tint = AlertRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. SEARCH & FILTER CONTROLS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search subjects by name, code, or description...", fontSize = 13.sp, color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = if (searchQuery.isNotBlank()) ElectricBlue else colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQuery.isNotBlank() && filteredSubjectStats.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ElectricBlue.copy(alpha = 0.15f),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = "${filteredSubjectStats.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricBlue,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            focusManager.clearFocus()
                                        }
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = if (isDark) DarkSurface else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            focusedContainerColor = if (isDark) DarkSurface else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedBorderColor = if (isDark) DarkGlassBorder else colorScheme.outlineVariant,
                            focusedBorderColor = ElectricBlue,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sort Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SubjectSortOption.values()) { sortOpt ->
                            val isSelected = selectedSort == sortOpt
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSort = sortOpt },
                                label = {
                                    Text(
                                        text = sortOpt.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = ElectricBlue,
                                    containerColor = if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    labelColor = colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) ElectricBlue else if (isDark) DarkGlassBorder else colorScheme.outlineVariant,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // 3. SUBJECTS SECTION HEADER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subjects in Database (${filteredSubjectStats.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
            }

            // 4. SUBJECT LIST ITEMS
            if (filteredSubjectStats.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No subjects match '$searchQuery'" else "No subjects added yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Try clearing search filters." else "Create your first syllabus subject to start tracking.",
                                fontSize = 13.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color(0xFF071B2B))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add New Subject", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(
                    items = filteredSubjectStats,
                    key = { it.subject.id }
                ) { stats ->
                    SubjectProgressCard(
                        stats = stats,
                        onOpen = {
                            onOpenSubject(stats.subject.id)
                            onNavigate(NavDestination.SYLLABUS)
                        },
                        onStartTimer = {
                            timerViewModel.setTimerTargetById(stats.subject.id, null)
                            onNavigate(NavDestination.TIMER)
                        },
                        onEdit = { editingSubject = stats.subject },
                        onDelete = { subjectToDelete = stats.subject }
                    )
                }
            }
        }

        // FloatingActionButton to Add New Subject
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Subject") },
            text = { Text("New Subject", fontWeight = FontWeight.Bold) },
            containerColor = ElectricBlue,
            contentColor = Color(0xFF071B2B),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_subject_fab")
        )
    }

    // Add Subject Dialog
    if (showAddDialog) {
        SubjectEditDialog(
            subject = null,
            onDismiss = { showAddDialog = false },
            onSave = { newSub ->
                subjectViewModel.addSubject(newSub)
                showAddDialog = false
            }
        )
    }

    // Edit Subject Dialog
    if (editingSubject != null) {
        SubjectEditDialog(
            subject = editingSubject,
            onDismiss = { editingSubject = null },
            onSave = { updated ->
                subjectViewModel.updateSubject(updated)
                editingSubject = null
            },
            onDelete = { toDel ->
                subjectToDelete = toDel
                editingSubject = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (subjectToDelete != null) {
        val sub = subjectToDelete!!
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            containerColor = if (isDark) DarkSurface else colorScheme.surface,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed) },
            title = { Text("Delete '${sub.name}'?", color = colorScheme.onSurface) },
            text = { Text("This will permanently remove this subject and all its syllabus chapters, sections, and progress.", color = colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        subjectViewModel.deleteSubject(sub)
                        subjectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text("Cancel", color = colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

/**
 * Individual Subject Item Card with detailed syllabus completion progress indicators,
 * badges, chapter counts, and action buttons.
 */
@Composable
fun SubjectProgressCard(
    stats: SubjectStats,
    onOpen: () -> Unit,
    onStartTimer: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val subject = stats.subject
    val subjectColor = remember(subject.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(subject.colorHex))
        } catch (e: Exception) {
            ElectricBlue
        }
    }

    var showMenu by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.id}"),
        shape = RoundedCornerShape(22.dp),
        accentColor = subjectColor,
        onClick = onOpen
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Subject Icon, Name, Code, and Options Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(subjectColor.copy(alpha = 0.18f))
                        .border(1.dp, subjectColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForName(subject.iconName),
                        contentDescription = null,
                        tint = subjectColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Subject Title and Code
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = subject.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (subject.code.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(subjectColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = subject.code,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = subjectColor
                                )
                            }
                        }
                    }

                    if (subject.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subject.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Options Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Subject options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open Syllabus", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { Icon(Icons.Default.AutoStories, contentDescription = null, tint = ElectricBlue) },
                            onClick = {
                                showMenu = false
                                onOpen()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Study Timer", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = ElectricBlue) },
                            onClick = {
                                showMenu = false
                                onStartTimer()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Details", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        DropdownMenuItem(
                            text = { Text("Delete Subject", color = AlertRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AlertRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PROGRESS INDICATOR SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Syllabus Completion",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${stats.completedChapters}/${stats.totalChapters} chapters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.completionPercentage}%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = subjectColor,
                        modifier = Modifier.testTag("subject_progress_percent_${subject.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Animated Linear Progress Bar
            LinearSyllabusBar(
                progress = stats.completionPercentage / 100f,
                height = 7.dp,
                barColor = subjectColor,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.testTag("subject_progress_bar_${subject.id}")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TOPIC BREAKDOWN PILLS & METRICS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusPill(
                    count = stats.completedChapters,
                    label = "Done",
                    color = SoftMint,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    count = stats.inProgressChapters,
                    label = "Active",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    count = stats.notStartedChapters,
                    label = "Left",
                    color = Color(0xFF64748B),
                    modifier = Modifier.weight(1f)
                )
                if (stats.weakChapters > 0) {
                    StatusPill(
                        count = stats.weakChapters,
                        label = "Weak",
                        color = AlertRed,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (stats.revisionDueChapters > 0) {
                    StatusPill(
                        count = stats.revisionDueChapters,
                        label = "Due",
                        color = Color(0xFFA78BFA),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ACTION BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Confidence & Study Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (stats.averageConfidence > 0) {
                        ConfidenceStars(confidence = stats.averageConfidence.toInt().coerceIn(1, 5))
                    }
                    if (stats.totalStudyMinutes > 0) {
                        Text(
                            text = "•  ⏱ ${stats.totalStudyMinutes / 60}h ${stats.totalStudyMinutes % 60}m",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(
                        onClick = onStartTimer,
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            contentColor = subjectColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Study timer",
                            tint = subjectColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Button(
                        onClick = onOpen,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = subjectColor, contentColor = Color(0xFF071B2B)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("View Syllabus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun MiniMetricChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Text(text = label, fontSize = 9.sp, color = colorScheme.onSurfaceVariant)
        }
    }
}

private fun getIconForName(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "MenuBook" -> Icons.Default.MenuBook
        "Calculate" -> Icons.Default.Calculate
        "Psychology" -> Icons.Default.Psychology
        "Translate" -> Icons.Default.Translate
        "History" -> Icons.Default.History
        "Science" -> Icons.Default.Science
        "Public" -> Icons.Default.Public
        "Gavel" -> Icons.Default.Gavel
        "Computer" -> Icons.Default.Computer
        "AutoStories" -> Icons.Default.AutoStories
        else -> Icons.Default.School
    }
}
