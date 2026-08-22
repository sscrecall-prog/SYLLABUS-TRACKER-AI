package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.TimerViewModel
import com.example.ui.viewmodel.SyllabusViewModel
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    onNavigate: (NavDestination) -> Unit
) {
    val subjectViewModel: SubjectViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()
    val syllabusViewModel: SyllabusViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()

    val subjects by subjectViewModel.subjects.collectAsState()
    val uiState by syllabusViewModel.uiState.collectAsState()
    val allItems = uiState.allItems
    val selectedSubjectId by mainViewModel.selectedSubjectId.collectAsState()
    val searchQuery = uiState.filterCriteria.query
    val filteredItems = uiState.filteredItems
    val focusManager = LocalFocusManager.current

    // Determine current active subject
    val currentSubject = remember(subjects, selectedSubjectId) {
        subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()
    }

    // Dialog and Modal states
    var showSubjectEditDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }
    var showBulkAddDialog by remember { mutableStateOf(false) }
    var bulkAddParentSection by remember { mutableStateOf<SyllabusItem?>(null) }
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var newSectionTitle by remember { mutableStateOf("") }
    var expandedItems by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var filterStatusSelection by remember { mutableStateOf<ChapterStatus?>(null) }
    var isHierarchyMode by remember { mutableStateOf(false) }
    var isCompactMode by remember { mutableStateOf(false) }
    var showAddSubSectionDialog by remember { mutableStateOf(false) }
    var newSubSectionTitle by remember { mutableStateOf("") }
    var targetSectionForSubSection by remember { mutableStateOf<SyllabusItem?>(null) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var newChapterTitle by remember { mutableStateOf("") }
    var targetSubSectionForChapter by remember { mutableStateOf<SyllabusItem?>(null) }

    // Auto-expand top sections initially
    LaunchedEffect(currentSubject?.id, allItems) {
        if (currentSubject != null && expandedItems.isEmpty()) {
            val topSectionIds = allItems.filter { it.subjectId == currentSubject.id && it.itemType == ItemType.SECTION }.map { it.id }.toSet()
            expandedItems = topSectionIds
        }
    }

    val currentSubjectItems = remember(allItems, currentSubject?.id) {
        if (currentSubject == null) emptyList()
        else allItems.filter { it.subjectId == currentSubject.id }
    }

    val subjectColor = remember(currentSubject) {
        try {
            Color(android.graphics.Color.parseColor(currentSubject?.colorHex ?: "#2D4F1E"))
        } catch (e: Exception) {
            BrandForestGreen
        }
    }

    // Subject statistics
    val totalSections = currentSubjectItems.count { it.itemType == ItemType.SECTION }
    val chapters = currentSubjectItems.filter { it.itemType == ItemType.CHAPTER || it.itemType == ItemType.SUBTOPIC }
    val completedCount = chapters.count { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
    val inProgressCount = chapters.count { it.status == ChapterStatus.IN_PROGRESS || it.status == ChapterStatus.LEARNING }
    val weakCount = chapters.count { it.isWeak }
    val revDueCount = chapters.count { it.isRevisionDue }
    val completionPercent = if (chapters.isNotEmpty()) ((completedCount.toFloat() / chapters.size) * 100).toInt() else 0

    Column(modifier = Modifier.fillMaxSize()) {
        // Subject Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = subjects.indexOfFirst { it.id == currentSubject?.id }.coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            divider = {}
        ) {
            subjects.forEach { sub ->
                val isSelected = currentSubject?.id == sub.id
                val subCol = try { Color(android.graphics.Color.parseColor(sub.colorHex)) } catch (e: Exception) { BrandForestGreen }

                Tab(
                    selected = isSelected,
                    onClick = { mainViewModel.openSubjectDetail(sub.id) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(subCol)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = sub.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }

            // Tab for Adding Subject
            Tab(
                selected = false,
                onClick = {
                    editingSubject = null
                    showSubjectEditDialog = true
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Subject", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        // Toggle view mode: Subject Focus vs Full Hierarchy Tree
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isHierarchyMode,
                onClick = { isHierarchyMode = false },
                label = { Text("Subject Focus", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.AutoStories, null, modifier = Modifier.size(14.dp)) }
            )
            FilterChip(
                selected = isHierarchyMode,
                onClick = { isHierarchyMode = true },
                label = { Text("Full Hierarchy", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.AccountTree, null, modifier = Modifier.size(14.dp)) }
            )
            Spacer(modifier = Modifier.weight(1f))
            FilterChip(
                selected = isCompactMode,
                onClick = { isCompactMode = !isCompactMode },
                label = { Text("Compact", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.ViewCompact, null, modifier = Modifier.size(14.dp)) }
            )
        }

        if (isHierarchyMode) {
            SyllabusHierarchyScreen(
                onBackClick = { isHierarchyMode = false }
            )
        } else {
            // Search and Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { syllabusViewModel.searchQuery.value = it },
                placeholder = { Text("Search chapters, notes, tags...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = if (searchQuery.isNotBlank()) ElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotBlank() && filteredItems.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElectricBlue.copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = "${filteredItems.size}",
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
                                    syllabusViewModel.searchQuery.value = ""
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // All subjects overview button
            IconButton(
                onClick = { onNavigate(NavDestination.SUBJECTS) },
                modifier = Modifier.testTag("all_subjects_overview_button")
            ) {
                Icon(Icons.Default.GridView, contentDescription = "All Subjects List", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Subject edit/manage button
            if (currentSubject != null) {
                IconButton(
                    onClick = {
                        editingSubject = currentSubject
                        showSubjectEditDialog = true
                    }
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Edit Subject", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Filter chips row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = filterStatusSelection == null && !syllabusViewModel.filterOnlyWeak.value && !syllabusViewModel.filterOnlyRevisionDue.value,
                    onClick = {
                        filterStatusSelection = null
                        syllabusViewModel.filterStatus.value = null
                        syllabusViewModel.filterOnlyWeak.value = false
                        syllabusViewModel.filterOnlyRevisionDue.value = false
                    },
                    label = { Text("All", fontSize = 11.sp) }
                )
            }
            item {
                FilterChip(
                    selected = syllabusViewModel.filterOnlyRevisionDue.value,
                    onClick = {
                        syllabusViewModel.filterOnlyRevisionDue.value = !syllabusViewModel.filterOnlyRevisionDue.value
                        syllabusViewModel.filterOnlyWeak.value = false
                        filterStatusSelection = null
                    },
                    label = { Text("🟣 Revision Due ($revDueCount)", fontSize = 11.sp) }
                )
            }
            item {
                FilterChip(
                    selected = syllabusViewModel.filterOnlyWeak.value,
                    onClick = {
                        syllabusViewModel.filterOnlyWeak.value = !syllabusViewModel.filterOnlyWeak.value
                        syllabusViewModel.filterOnlyRevisionDue.value = false
                        filterStatusSelection = null
                    },
                    label = { Text("🔴 Weak ($weakCount)", fontSize = 11.sp) }
                )
            }
            item {
                FilterChip(
                    selected = filterStatusSelection == ChapterStatus.IN_PROGRESS,
                    onClick = {
                        filterStatusSelection = if (filterStatusSelection == ChapterStatus.IN_PROGRESS) null else ChapterStatus.IN_PROGRESS
                        syllabusViewModel.filterStatus.value = filterStatusSelection
                    },
                    label = { Text("🟡 In Progress ($inProgressCount)", fontSize = 11.sp) }
                )
            }
            item {
                FilterChip(
                    selected = filterStatusSelection == ChapterStatus.COMPLETED,
                    onClick = {
                        filterStatusSelection = if (filterStatusSelection == ChapterStatus.COMPLETED) null else ChapterStatus.COMPLETED
                        syllabusViewModel.filterStatus.value = filterStatusSelection
                    },
                    label = { Text("🟢 Completed ($completedCount)", fontSize = 11.sp) }
                )
            }
        }

        // Syllabus Content List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Subject Summary Banner Card
            if (currentSubject != null && searchQuery.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        accentColor = subjectColor
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentSubject.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (currentSubject.description.isNotBlank()) {
                                        Text(
                                            text = currentSubject.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(subjectColor)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$completionPercent%",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            LinearSyllabusBar(
                                progress = completionPercent / 100f,
                                height = 8.dp,
                                barColor = subjectColor
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("$totalSections Sections • ${chapters.size} Chapters", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("✅ $completedCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusCompleted)
                                    Text("⏳ $inProgressCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusInProgress)
                                    if (revDueCount > 0) Text("🟣 $revDueCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusRevisionDue)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons inside banner
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showAddSectionDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = subjectColor)
                                ) {
                                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Section", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        bulkAddParentSection = null
                                        showBulkAddDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bulk Add", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val allSectionIds = currentSubjectItems.map { it.id }.toSet()
                                        expandedItems = if (expandedItems.size >= allSectionIds.size / 2) emptySet() else allSectionIds
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (expandedItems.isNotEmpty()) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                                        contentDescription = "Expand/Collapse",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hierarchical Items List
            if (searchQuery.isNotBlank() || syllabusViewModel.filterOnlyWeak.value || syllabusViewModel.filterOnlyRevisionDue.value || filterStatusSelection != null) {
                // Flat filtered list
                val displayList = if (currentSubject != null && searchQuery.isEmpty()) {
                    filteredItems.filter { it.subjectId == currentSubject.id }
                } else filteredItems

                if (displayList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔍 No matching chapters found", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Try clearing the search or filter chips.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(displayList, key = { it.id }) { item ->
                        ChapterItemRow(
                            item = item,
                            depth = 0,
                            hasChildren = false,
                            isCompactMode = isCompactMode,
                            subjectColor = subjectColor,
                            isExpanded = false,
                            onClick = { syllabusViewModel.selectChapter(item) },
                            onStatusChange = { newStatus -> syllabusViewModel.updateChapterStatus(item, newStatus) },
                            onQuickAction = { action ->
                                handleItemAction(action, item, { sec -> bulkAddParentSection = sec; showBulkAddDialog = true }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)
                            }
                        )
                    }
                }
            } else if (currentSubject != null) {
                // Render tree: Top Sections -> Subsections -> Chapters
                val topSections = currentSubjectItems.filter { it.parentId == null }.sortedBy { it.orderIndex }

                if (topSections.isEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = subjectColor, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No sections in this subject yet", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Start building your syllabus by adding sections and chapters.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddSectionDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = subjectColor)
                                ) {
                                    Text("+ Add First Section")
                                }
                            }
                        }
                    }
                } else {
                    topSections.forEach { section ->
                        val isExpanded = expandedItems.contains(section.id)
                        val childSubsections = currentSubjectItems.filter { it.parentId == section.id }.sortedBy { it.orderIndex }
                        val hasChildren = childSubsections.isNotEmpty()

                        item(key = section.id) {
                            ChapterItemRow(
                                item = section,
                                depth = 0,
                                hasChildren = true,
                                isCompactMode = isCompactMode,
                                subjectColor = subjectColor,
                                isExpanded = isExpanded,
                                onToggleExpand = {
                                    expandedItems = if (isExpanded) expandedItems - section.id else expandedItems + section.id
                                },
                                onClick = {
                                    expandedItems = if (isExpanded) expandedItems - section.id else expandedItems + section.id
                                },
                                onStatusChange = { newStatus -> syllabusViewModel.updateChapterStatus(section, newStatus) },
                                onQuickAction = { action ->
                                    if (action == "add_subsection") {
                                        targetSectionForSubSection = section
                                        showAddSubSectionDialog = true
                                    } else {
                                        handleItemAction(action, section, { sec -> bulkAddParentSection = sec; showBulkAddDialog = true }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)
                                    }
                                }
                            )
                        }

                        if (isExpanded) {
                            childSubsections.forEach { subItem ->
                                val isSubExpanded = expandedItems.contains(subItem.id)
                                val subChildren = currentSubjectItems.filter { it.parentId == subItem.id }.sortedBy { it.orderIndex }
                                val subHasChildren = subChildren.isNotEmpty()

                                item(key = subItem.id) {
                                    ChapterItemRow(
                                        item = subItem,
                                        depth = 1,
                                        hasChildren = subHasChildren,
                                        isCompactMode = isCompactMode,
                                        subjectColor = subjectColor,
                                        isExpanded = isSubExpanded,
                                        onToggleExpand = {
                                            expandedItems = if (isSubExpanded) expandedItems - subItem.id else expandedItems + subItem.id
                                        },
                                        onClick = {
                                            if (subHasChildren) {
                                                expandedItems = if (isSubExpanded) expandedItems - subItem.id else expandedItems + subItem.id
                                            } else {
                                                syllabusViewModel.selectChapter(subItem)
                                            }
                                        },
                                        onStatusChange = { newStatus -> syllabusViewModel.updateChapterStatus(subItem, newStatus) },
                                        onQuickAction = { action ->
                                            if (action == "add_chapter") {
                                                targetSubSectionForChapter = subItem
                                                showAddChapterDialog = true
                                            } else {
                                                handleItemAction(action, subItem, { sec -> bulkAddParentSection = sec; showBulkAddDialog = true }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)
                                            }
                                        }
                                    )
                                }

                                if (isSubExpanded && subHasChildren) {
                                    subChildren.forEach { leafChapter ->
                                        item(key = leafChapter.id) {
                                            ChapterItemRow(
                                                item = leafChapter,
                                                depth = 2,
                                                hasChildren = false,
                                                isCompactMode = isCompactMode,
                                                subjectColor = subjectColor,
                                                isExpanded = false,
                                                onClick = { syllabusViewModel.selectChapter(leafChapter) },
                                                onStatusChange = { newStatus -> syllabusViewModel.updateChapterStatus(leafChapter, newStatus) },
                                                onQuickAction = { action ->
                                                    handleItemAction(action, leafChapter, { sec -> bulkAddParentSection = sec; showBulkAddDialog = true }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)
                                                }
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

    // Add Section Dialog
    if (showAddSectionDialog && currentSubject != null) {
        AlertDialog(
            onDismissRequest = { showAddSectionDialog = false },
            title = { Text("Add Section to ${currentSubject.name}") },
            text = {
                OutlinedTextField(
                    value = newSectionTitle,
                    onValueChange = { newSectionTitle = it },
                    label = { Text("Section Title (e.g. History / Grammar)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSectionTitle.isNotBlank()) {
                            syllabusViewModel.addItem(
                                subjectId = currentSubject.id,
                                parentId = null,
                                itemType = ItemType.SECTION,
                                title = newSectionTitle.trim()
                            )
                            newSectionTitle = ""
                            showAddSectionDialog = false
                        }
                    },
                    enabled = newSectionTitle.isNotBlank()
                ) {
                    Text("Add Section")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Sub-Section Dialog
    if (showAddSubSectionDialog && targetSectionForSubSection != null) {
        AlertDialog(
            onDismissRequest = { showAddSubSectionDialog = false },
            title = { Text("Add Sub-Section to ${targetSectionForSubSection?.title}") },
            text = {
                OutlinedTextField(
                    value = newSubSectionTitle,
                    onValueChange = { newSubSectionTitle = it },
                    label = { Text("Sub-Section Title (e.g. Ancient / Modern)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubSectionTitle.isNotBlank()) {
                            syllabusViewModel.addItem(
                                subjectId = targetSectionForSubSection!!.subjectId,
                                parentId = targetSectionForSubSection!!.id,
                                itemType = ItemType.SUBSECTION,
                                title = newSubSectionTitle.trim()
                            )
                            newSubSectionTitle = ""
                            showAddSubSectionDialog = false
                        }
                    },
                    enabled = newSubSectionTitle.isNotBlank()
                ) {
                    Text("Add Sub-Section")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubSectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Chapter Dialog
    if (showAddChapterDialog && targetSubSectionForChapter != null) {
        AlertDialog(
            onDismissRequest = { showAddChapterDialog = false },
            title = { Text("Add Chapter to ${targetSubSectionForChapter?.title}") },
            text = {
                OutlinedTextField(
                    value = newChapterTitle,
                    onValueChange = { newChapterTitle = it },
                    label = { Text("Chapter Title (e.g. Stone Age / Rigveda)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChapterTitle.isNotBlank()) {
                            syllabusViewModel.addItem(
                                subjectId = targetSubSectionForChapter!!.subjectId,
                                parentId = targetSubSectionForChapter!!.id,
                                itemType = ItemType.CHAPTER,
                                title = newChapterTitle.trim()
                            )
                            newChapterTitle = ""
                            showAddChapterDialog = false
                        }
                    },
                    enabled = newChapterTitle.isNotBlank()
                ) {
                    Text("Add Chapter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Bulk Add Dialog
    if (showBulkAddDialog && currentSubject != null) {
        BulkAddDialog(
            subject = currentSubject,
            parentSection = bulkAddParentSection,
            onDismiss = { showBulkAddDialog = false },
            onConfirmBulkAdd = { multiline ->
                syllabusViewModel.bulkAddChapters(
                    subjectId = currentSubject.id,
                    parentId = bulkAddParentSection?.id,
                    multilineText = multiline
                )
            }
        )
    }

    // Subject Edit Dialog
    if (showSubjectEditDialog) {
        SubjectEditDialog(
            subject = editingSubject,
            onDismiss = { showSubjectEditDialog = false },
            onSave = { updated ->
                if (editingSubject == null) {
                    subjectViewModel.addSubject(updated.name, updated.code, updated.iconName, updated.colorHex, updated.description)
                } else {
                    subjectViewModel.updateSubject(updated)
                }
            },
            onDelete = if (editingSubject != null) { { subjectViewModel.deleteSubject(it) } } else null
        )
    }
}

private fun handleItemAction(
    action: String,
    item: SyllabusItem,
    onOpenBulk: (SyllabusItem) -> Unit,
    syllabusViewModel: SyllabusViewModel,
    subjectViewModel: SubjectViewModel,
    timerViewModel: TimerViewModel,
    onNavigate: (NavDestination) -> Unit
) {
    when (action) {
        "edit" -> syllabusViewModel.selectChapter(item)
        "revise" -> syllabusViewModel.markChapterRevised(item)
        "duplicate" -> syllabusViewModel.duplicateItem(item)
        "delete" -> syllabusViewModel.deleteItem(item)
        "move_up" -> syllabusViewModel.moveItemUp(item)
        "move_down" -> syllabusViewModel.moveItemDown(item)
        "timer" -> {
            timerViewModel.setTimerTargetById(item.subjectId, item.id)
            onNavigate(NavDestination.TIMER)
        }
        "pyq" -> syllabusViewModel.selectChapter(item)
    }
}
