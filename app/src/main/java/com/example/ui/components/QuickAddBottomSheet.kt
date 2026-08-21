package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class QuickAddTab(val label: String) {
    CHAPTER("Chapter"),
    SECTION("Section"),
    SUBJECT("Subject"),
    STUDY_PLAN("Study Plan"),
    GOAL("Goal")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    subjects: List<Subject>,
    items: List<SyllabusItem>,
    preselectedSubjectId: Long? = null,
    onDismiss: () -> Unit,
    onAddChapter: (Long, Long?, ItemType, String, Priority, Difficulty, String) -> Unit,
    onAddSubject: (String, String, String, String, String) -> Unit,
    onAddStudyPlan: (String, String, Long, String, String, Int, String) -> Unit,
    onAddGoal: (String, String, Long?, String, Int, Float) -> Unit
) {
    var selectedTab by remember { mutableStateOf(QuickAddTab.CHAPTER) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val safeDismiss = {
        keyboardController?.hide()
        focusManager.clearFocus()
        onDismiss()
    }

    // Chapter / Section form state
    var selectedSubjectId by remember {
        mutableStateOf(preselectedSubjectId ?: subjects.firstOrNull()?.id ?: 1L)
    }
    var selectedParentId by remember { mutableStateOf<Long?>(null) }
    var itemTitle by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var tags by remember { mutableStateOf("") }

    // Subject form state
    var subjectName by remember { mutableStateOf("") }
    var subjectCode by remember { mutableStateOf("") }
    var subjectColorHex by remember { mutableStateOf("#2D4F1E") }
    var subjectIconName by remember { mutableStateOf("School") }
    var subjectDesc by remember { mutableStateOf("") }

    // Study Plan form state
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var planTime by remember { mutableStateOf("09:00") }
    var planChapterTitle by remember { mutableStateOf("") }
    var plannedMinutes by remember { mutableIntStateOf(60) }
    var planNotes by remember { mutableStateOf("") }

    // Goal form state
    var goalTitle by remember { mutableStateOf("") }
    var goalTargetDate by remember { mutableStateOf("2026-09-30") }
    var goalTargetChapters by remember { mutableIntStateOf(20) }
    var goalTargetHours by remember { mutableFloatStateOf(40f) }

    val currentSubject = remember(selectedSubjectId, subjects) {
        subjects.find { it.id == selectedSubjectId }
    }

    val subjectSections = remember(selectedSubjectId, items) {
        items.filter { it.subjectId == selectedSubjectId && (it.itemType == ItemType.SECTION || it.itemType == ItemType.SUBSECTION) }
    }

    ModalBottomSheet(
        onDismissRequest = safeDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Quick Add",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                QuickAddTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                QuickAddTab.CHAPTER, QuickAddTab.SECTION -> {
                    // Subject Selector
                    Text("Select Subject", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    var subMenuExpanded by remember { mutableStateOf(false) }
                    val currentSubject = subjects.find { it.id == selectedSubjectId }
                    Box {
                        OutlinedButton(
                            onClick = { subMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(currentSubject?.name ?: "Select Subject")
                        }
                        DropdownMenu(expanded = subMenuExpanded, onDismissRequest = { subMenuExpanded = false }) {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        selectedSubjectId = s.id
                                        selectedParentId = null
                                        subMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedTab == QuickAddTab.CHAPTER && subjectSections.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Parent Section (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        var parentMenuExpanded by remember { mutableStateOf(false) }
                        val currentParent = subjectSections.find { it.id == selectedParentId }
                        Box {
                            OutlinedButton(
                                onClick = { parentMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(currentParent?.title ?: "Top-Level (None)")
                            }
                            DropdownMenu(expanded = parentMenuExpanded, onDismissRequest = { parentMenuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("None (Direct under Subject)") },
                                    onClick = {
                                        selectedParentId = null
                                        parentMenuExpanded = false
                                    }
                                )
                                subjectSections.forEach { sec ->
                                    DropdownMenuItem(
                                        text = { Text("${sec.itemType.label}: ${sec.title}") },
                                        onClick = {
                                            selectedParentId = sec.id
                                            parentMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = itemTitle,
                        onValueChange = { itemTitle = it },
                        label = { Text(if (selectedTab == QuickAddTab.CHAPTER) "Chapter Name" else "Section Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (selectedTab == QuickAddTab.CHAPTER) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("Tags (e.g. #PYQ, #Important)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (itemTitle.isNotBlank()) {
                                val type = if (selectedTab == QuickAddTab.SECTION) ItemType.SECTION else ItemType.CHAPTER
                                onAddChapter(selectedSubjectId, selectedParentId, type, itemTitle.trim(), priority, difficulty, tags)
                                safeDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        enabled = itemTitle.isNotBlank()
                    ) {
                        Text("Add ${selectedTab.label}")
                    }
                }

                QuickAddTab.SUBJECT -> {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject Name (e.g. History / Maths)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("Short Code (e.g. HIST)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = subjectDesc,
                        onValueChange = { subjectDesc = it },
                        label = { Text("Description / Syllabus Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Theme Color", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    val colorOptions = listOf("#2D4F1E", "#3F51B5", "#8E24AA", "#E27D60", "#00897B", "#D81B60", "#FB8C00", "#4A4A4A")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.forEach { hex ->
                            val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Green }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        if (subjectColorHex == hex) 3.dp else 0.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape
                                    )
                                    .clickable { subjectColorHex = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (subjectName.isNotBlank()) {
                                onAddSubject(subjectName.trim(), subjectCode.trim(), subjectIconName, subjectColorHex, subjectDesc.trim())
                                safeDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        enabled = subjectName.isNotBlank()
                    ) {
                        Text("Create Subject")
                    }
                }

                QuickAddTab.STUDY_PLAN -> {
                    // Select Subject
                    Text("Subject", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    var subMenuExpanded by remember { mutableStateOf(false) }
                    val currentSubject = subjects.find { it.id == selectedSubjectId }
                    Box {
                        OutlinedButton(
                            onClick = { subMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(currentSubject?.name ?: "Select Subject")
                        }
                        DropdownMenu(expanded = subMenuExpanded, onDismissRequest = { subMenuExpanded = false }) {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        selectedSubjectId = s.id
                                        subMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = planChapterTitle,
                        onValueChange = { planChapterTitle = it },
                        label = { Text("Chapter / Study Task") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = planTime,
                            onValueChange = { planTime = it },
                            label = { Text("Start Time (e.g. 08:00)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = plannedMinutes.toString(),
                            onValueChange = { plannedMinutes = it.toIntOrNull() ?: 60 },
                            label = { Text("Planned Minutes") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = planNotes,
                        onValueChange = { planNotes = it },
                        label = { Text("Goal or Specific Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (planChapterTitle.isNotBlank()) {
                                onAddStudyPlan(
                                    today,
                                    planTime,
                                    selectedSubjectId,
                                    currentSubject?.name ?: "General",
                                    planChapterTitle.trim(),
                                    plannedMinutes,
                                    planNotes.trim()
                                )
                                safeDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        enabled = planChapterTitle.isNotBlank()
                    ) {
                        Text("Add to Daily Plan")
                    }
                }

                QuickAddTab.GOAL -> {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Goal Title (e.g. Complete Polity in 15 Days)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = goalTargetDate,
                        onValueChange = { goalTargetDate = it },
                        label = { Text("Target Deadline (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = goalTargetChapters.toString(),
                            onValueChange = { goalTargetChapters = it.toIntOrNull() ?: 10 },
                            label = { Text("Target Chapters") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = goalTargetHours.toString(),
                            onValueChange = { goalTargetHours = it.toFloatOrNull() ?: 20f },
                            label = { Text("Target Hours") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (goalTitle.isNotBlank()) {
                                onAddGoal(
                                    goalTitle.trim(),
                                    goalTargetDate,
                                    selectedSubjectId,
                                    currentSubject?.name ?: "All Subjects",
                                    goalTargetChapters,
                                    goalTargetHours
                                )
                                safeDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        enabled = goalTitle.isNotBlank()
                    ) {
                        Text("Set Goal")
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
