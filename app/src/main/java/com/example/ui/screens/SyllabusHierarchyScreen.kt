package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.SyllabusViewModel
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusHierarchyScreen(
    onBackClick: () -> Unit
) {
    val subjectViewModel: SubjectViewModel = viewModel()
    val syllabusViewModel: SyllabusViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()

    val hierarchies by subjectViewModel.allSubjectHierarchies.collectAsState()
    val allItems by syllabusViewModel.items.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    
    // Set of unique keys of expanded nodes: "subject_{id}", "section_{id}", "subsection_{id}"
    var expandedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Aggregate stats across all subjects
    val overallTotalChapters = remember(allItems) {
        allItems.count { it.itemType == ItemType.CHAPTER }
    }
    val overallCompletedChapters = remember(allItems) {
        allItems.count { it.itemType == ItemType.CHAPTER && (it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED) }
    }
    val overallInProgressChapters = remember(allItems) {
        allItems.count { it.itemType == ItemType.CHAPTER && (it.status == ChapterStatus.IN_PROGRESS || it.status == ChapterStatus.LEARNING) }
    }
    val overallRevisionDueChapters = remember(allItems) {
        allItems.count { it.itemType == ItemType.CHAPTER && it.isRevisionDue }
    }
    val overallCompletionPercent = if (overallTotalChapters > 0) {
        ((overallCompletedChapters.toFloat() / overallTotalChapters) * 100).toInt()
    } else 0

    // Auto-expand paths when searching to reveal matches
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            val matchingItems = allItems.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true) ||
                it.tags.contains(searchQuery, ignoreCase = true)
            }
            
            val keysToExpand = mutableSetOf<String>()
            matchingItems.forEach { item ->
                // If it's a chapter, expand its subsection and parent section and subject
                if (item.itemType == ItemType.CHAPTER) {
                    item.parentId?.let { subSecId ->
                        keysToExpand.add("subsection_$subSecId")
                        // Find parent section
                        val subSec = allItems.find { it.id == subSecId }
                        subSec?.parentId?.let { secId ->
                            keysToExpand.add("section_$secId")
                        }
                    }
                } else if (item.itemType == ItemType.SUBSECTION) {
                    keysToExpand.add("subsection_${item.id}")
                    item.parentId?.let { secId ->
                        keysToExpand.add("section_$secId")
                    }
                } else if (item.itemType == ItemType.SECTION) {
                    keysToExpand.add("section_${item.id}")
                }
                keysToExpand.add("subject_${item.subjectId}")
            }
            expandedKeys = expandedKeys + keysToExpand
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search & Control Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search through entire hierarchy...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))

            // Expand All / Collapse All Toggle
            OutlinedIconButton(
                onClick = {
                    if (expandedKeys.isEmpty()) {
                        val allKeys = mutableSetOf<String>()
                        hierarchies.forEach { h ->
                            allKeys.add("subject_${h.subject.id}")
                            h.sections.forEach { sWithSub ->
                                if (sWithSub.section.parentId == null) {
                                    allKeys.add("section_${sWithSub.section.id}")
                                }
                                sWithSub.subSections.forEach { subWithChap ->
                                    if (subWithChap.subSection.itemType == ItemType.SUBSECTION) {
                                        allKeys.add("subsection_${subWithChap.subSection.id}")
                                    }
                                }
                            }
                        }
                        expandedKeys = allKeys
                    } else {
                        expandedKeys = emptySet()
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (expandedKeys.isNotEmpty()) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                    contentDescription = "Expand/Collapse All"
                )
            }
        }

        // Master Summary Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            accentColor = BrandForestGreen
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Comprehensive Syllabus Tree",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Viewing all courses, sections & chapters",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandForestGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$overallCompletionPercent% Done",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandForestGreen
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                LinearSyllabusBar(
                    progress = overallCompletionPercent / 100f,
                    height = 6.dp,
                    barColor = BrandForestGreen
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${subjects.size} Subjects • $overallTotalChapters Chapters",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✅ $overallCompletedChapters", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusCompleted)
                        Text("⏳ $overallInProgressChapters", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusInProgress)
                        if (overallRevisionDueChapters > 0) {
                            Text("🟣 $overallRevisionDueChapters", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusRevisionDue)
                        }
                    }
                }
            }
        }

        // Tree View List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (hierarchies.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No subjects or chapters found. Create subjects first!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                hierarchies.forEach { hierarchy ->
                    val subject = hierarchy.subject
                    val subjectColor = try {
                        Color(android.graphics.Color.parseColor(subject.colorHex))
                    } catch (e: Exception) {
                        BrandForestGreen
                    }
                    val isSubjectExpanded = expandedKeys.contains("subject_${subject.id}")

                    // Filtered and sorted sections
                    val subjectSections = hierarchy.sections
                        .filter { it.section.parentId == null && it.section.itemType == ItemType.SECTION }
                        .sortedBy { it.section.orderIndex }

                    // Calculation of child totals for header display
                    val subTotalChapters = allItems.count { it.subjectId == subject.id && it.itemType == ItemType.CHAPTER }
                    val subCompletedChapters = allItems.count {
                        it.subjectId == subject.id && it.itemType == ItemType.CHAPTER && (it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED)
                    }
                    val subCompletionPercent = if (subTotalChapters > 0) {
                        ((subCompletedChapters.toFloat() / subTotalChapters) * 100).toInt()
                    } else 0

                    // Subject Level Card
                    item(key = "subject_${subject.id}") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val key = "subject_${subject.id}"
                                    expandedKeys = if (isSubjectExpanded) expandedKeys - key else expandedKeys + key
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = subjectColor.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(subjectColor.copy(alpha = 0.2f)),
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
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$subTotalChapters Chapters • $subCompletionPercent% Complete",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "🎯 $subCompletionPercent%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = subjectColor,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            val key = "subject_${subject.id}"
                                            expandedKeys = if (isSubjectExpanded) expandedKeys - key else expandedKeys + key
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSubjectExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isSubjectExpanded) "Collapse" else "Expand",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    var showSubjectMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(
                                            onClick = { showSubjectMenu = true },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Subject Menu",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showSubjectMenu,
                                            onDismissRequest = { showSubjectMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Open Focus View") },
                                                leadingIcon = { Icon(Icons.Default.AutoStories, contentDescription = null, tint = subjectColor) },
                                                onClick = {
                                                    showSubjectMenu = false
                                                    mainViewModel.openSubjectDetail(subject.id)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(if (isSubjectExpanded) "Collapse Subject" else "Expand Subject") },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = if (isSubjectExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    showSubjectMenu = false
                                                    val key = "subject_${subject.id}"
                                                    expandedKeys = if (isSubjectExpanded) expandedKeys - key else expandedKeys + key
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section, Sub-Section, Chapter Tree rendering
                    if (isSubjectExpanded) {
                        if (subjectSections.isEmpty()) {
                            item(key = "empty_subject_${subject.id}") {
                                PaddingRow(depth = 1) {
                                    Text(
                                        text = "No sections in this subject.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            subjectSections.forEach { sectionWithSubs ->
                                val section = sectionWithSubs.section
                                val isSectionExpanded = expandedKeys.contains("section_${section.id}")
                                val subSections = sectionWithSubs.subSections
                                    .filter { it.subSection.itemType == ItemType.SUBSECTION }
                                    .sortedBy { it.subSection.orderIndex }

                                item(key = "section_${section.id}") {
                                    PaddingRow(
                                        depth = 1,
                                        modifier = Modifier.clickable {
                                            val key = "section_${section.id}"
                                            expandedKeys = if (isSectionExpanded) expandedKeys - key else expandedKeys + key
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isSectionExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                                contentDescription = "Section",
                                                tint = subjectColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = section.title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.5.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = if (isSectionExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                if (isSectionExpanded) {
                                    if (subSections.isEmpty()) {
                                        item(key = "empty_section_${section.id}") {
                                            PaddingRow(depth = 2) {
                                                Text(
                                                    text = "No sub-sections in this section.",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else {
                                        subSections.forEach { subSectionWithChapters ->
                                            val subSection = subSectionWithChapters.subSection
                                            val isSubSectionExpanded = expandedKeys.contains("subsection_${subSection.id}")
                                            val chapters = subSectionWithChapters.chapters
                                                .sortedBy { it.orderIndex }

                                            item(key = "subsection_${subSection.id}") {
                                                PaddingRow(
                                                    depth = 2,
                                                    modifier = Modifier.clickable {
                                                        val key = "subsection_${subSection.id}"
                                                        expandedKeys = if (isSubSectionExpanded) expandedKeys - key else expandedKeys + key
                                                    }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.SnippetFolder,
                                                            contentDescription = "Sub-Section",
                                                            tint = subjectColor.copy(alpha = 0.7f),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = subSection.title,
                                                            fontWeight = FontWeight.Medium,
                                                            fontSize = 12.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Text(
                                                            text = "(${chapters.size})",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(end = 4.dp)
                                                        )
                                                        Icon(
                                                            imageVector = if (isSubSectionExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            if (isSubSectionExpanded) {
                                                if (chapters.isEmpty()) {
                                                    item(key = "empty_subsection_${subSection.id}") {
                                                        PaddingRow(depth = 3) {
                                                            Text(
                                                                text = "No chapters in this sub-section.",
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    // Render matching chapters
                                                    val filteredChapters = if (searchQuery.isNotBlank()) {
                                                        chapters.filter {
                                                            it.title.contains(searchQuery, ignoreCase = true) ||
                                                            it.notes.contains(searchQuery, ignoreCase = true) ||
                                                            it.tags.contains(searchQuery, ignoreCase = true)
                                                        }
                                                    } else chapters

                                                    items(filteredChapters, key = { "chapter_${it.id}" }) { chapter ->
                                                        PaddingRow(depth = 3) {
                                                            ChapterLeafItemRow(
                                                                chapter = chapter,
                                                                onClick = { syllabusViewModel.selectChapter(chapter) },
                                                                onStatusChange = { newStatus ->
                                                                    syllabusViewModel.updateChapterStatus(chapter, newStatus)
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
            }
        }
    }
}

@Composable
fun PaddingRow(
    depth: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val paddingStart = (depth * 14).dp
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = paddingStart, top = 4.dp, bottom = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
fun ChapterLeafItemRow(
    chapter: SyllabusItem,
    onClick: () -> Unit,
    onStatusChange: (ChapterStatus) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick status emoji changer
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(chapter.status.getColor().copy(alpha = 0.15f))
                .clickable { showStatusMenu = true },
            contentAlignment = Alignment.Center
        ) {
            Text(text = chapter.status.iconEmoji, fontSize = 12.sp)

            DropdownMenu(
                expanded = showStatusMenu,
                onDismissRequest = { showStatusMenu = false }
            ) {
                ChapterStatus.values().forEach { status ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(status.iconEmoji)
                                Text(status.label, color = status.getColor(), fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = {
                            onStatusChange(status)
                            showStatusMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chapter.title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (chapter.isImportant) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Important",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PriorityBadge(priority = chapter.priority)
                if (chapter.revisionCount > 0) {
                    Text(
                        text = "• Rev: ${chapter.revisionCount}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${chapter.completionPercentage}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (chapter.completionPercentage == 100) StatusCompleted else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
