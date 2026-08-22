package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.Subject
import com.example.ui.theme.*

/**
 * Quick Note Modal to instantly capture text-based study thoughts,
 * mnemonics, formula tricks, and rapid exam reminders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNoteModal(
    subjects: List<Subject>,
    preselectedSubjectId: Long? = null,
    onDismiss: () -> Unit,
    onSaveNote: (subjectId: Long, title: String, content: String, tags: String, priority: Priority) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val colorScheme = MaterialTheme.colorScheme

    var selectedSubjectId by remember {
        mutableStateOf(preselectedSubjectId ?: subjects.firstOrNull()?.id ?: 1L)
    }

    LaunchedEffect(subjects, preselectedSubjectId) {
        if (preselectedSubjectId != null && subjects.any { it.id == preselectedSubjectId }) {
            selectedSubjectId = preselectedSubjectId
        } else if (subjects.isNotEmpty() && subjects.none { it.id == selectedSubjectId }) {
            selectedSubjectId = subjects.first().id
        }
    }

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var activeTags by remember { mutableStateOf(setOf<String>()) }
    var customTagInput by remember { mutableStateOf("") }

    val presetTags = listOf("#KeyPoint", "#Formula", "#Mnemonic", "#ExamTrick", "#PYQ", "#Doubt", "#Concept")

    val currentSubject = remember(selectedSubjectId, subjects) {
        subjects.find { it.id == selectedSubjectId }
    }

    var subjectMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("quick_note_modal"),
        containerColor = if (isDark) DarkSurface else colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Capture Study Thought",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Instantly jot down key insights, mnemonics & exam notes",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subject Dropdown
                Text(
                    text = "Subject Category",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { subjectMenuExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_note_subject_picker"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentSubject?.name ?: "Select Subject",
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        subjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.name) },
                                onClick = {
                                    selectedSubjectId = sub.id
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Note / Thought Title
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    label = { Text("Topic / Title (e.g. Fundamental Rights Article 32)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_note_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Main Thought / Content Area
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Study Thoughts & Insights") },
                    placeholder = {
                        Text(
                            "• Shortcut formulas or rules\n• Mnemonics & recall anchors\n• Crucial exam exceptions or doubts...",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_note_content_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Priority Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Importance:", fontSize = 11.5.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Priority.values().forEach { p ->
                            val isSelected = selectedPriority == p
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) {
                                            when (p) {
                                                Priority.URGENT, Priority.HIGH -> StatusWeak.copy(alpha = 0.25f)
                                                Priority.MEDIUM -> ElectricBlue.copy(alpha = 0.25f)
                                                Priority.LOW -> SoftMint.copy(alpha = 0.25f)
                                            }
                                        } else Color.Transparent
                                    )
                                    .clickable { selectedPriority = p }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        when (p) {
                                            Priority.URGENT, Priority.HIGH -> StatusWeak
                                            Priority.MEDIUM -> ElectricBlue
                                            Priority.LOW -> SoftMintDark
                                        }
                                    } else colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Quick Tags
                Column {
                    Text(
                        text = "Quick Categorization Tags",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetTags.take(4).forEach { tag ->
                            val isSelected = activeTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    activeTags = if (isSelected) activeTags - tag else activeTags + tag
                                },
                                label = { Text(tag, fontSize = 10.5.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetTags.drop(4).forEach { tag ->
                            val isSelected = activeTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    activeTags = if (isSelected) activeTags - tag else activeTags + tag
                                },
                                label = { Text(tag, fontSize = 10.5.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (noteContent.isNotBlank() || noteTitle.isNotBlank()) {
                        val tagsFormatted = activeTags.joinToString(",")
                        onSaveNote(
                            selectedSubjectId,
                            noteTitle.trim(),
                            noteContent.trim(),
                            tagsFormatted,
                            selectedPriority
                        )
                        onDismiss()
                    }
                },
                enabled = noteContent.isNotBlank() || noteTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = Color(0xFF071B2B)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_quick_note_btn")
            ) {
                Text("Save Thought", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colorScheme.onSurfaceVariant)
            }
        }
    )
}
