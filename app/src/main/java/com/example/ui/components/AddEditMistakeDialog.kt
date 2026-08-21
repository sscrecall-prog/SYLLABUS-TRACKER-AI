package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MistakeCategory
import com.example.data.model.MistakeEntry
import com.example.data.model.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMistakeDialog(
    subjects: List<Subject>,
    initialMistake: MistakeEntry? = null,
    onDismiss: () -> Unit,
    onSave: (
        questionText: String,
        yourWrongAnswer: String,
        correctAnswer: String,
        explanation: String,
        subjectId: Long,
        subjectName: String,
        chapterTitle: String,
        sourceMock: String,
        category: MistakeCategory,
        isStarred: Boolean,
        tags: String
    ) -> Unit
) {
    var questionText by remember { mutableStateOf(initialMistake?.questionText ?: "") }
    var yourWrongAnswer by remember { mutableStateOf(initialMistake?.yourWrongAnswer ?: "") }
    var correctAnswer by remember { mutableStateOf(initialMistake?.correctAnswer ?: "") }
    var explanation by remember { mutableStateOf(initialMistake?.explanationOrKeyConcept ?: "") }
    var selectedSubjectId by remember { mutableStateOf(initialMistake?.subjectId ?: subjects.firstOrNull()?.id ?: 1L) }
    var chapterTitle by remember { mutableStateOf(initialMistake?.chapterTitle ?: "") }
    var sourceMock by remember { mutableStateOf(initialMistake?.sourceMockOrBook ?: "") }
    var selectedCategory by remember { mutableStateOf(initialMistake?.category ?: MistakeCategory.SILLY_MISTAKE) }
    var isStarred by remember { mutableStateOf(initialMistake?.importanceStar ?: false) }
    var tags by remember { mutableStateOf(initialMistake?.tagsCsv ?: "") }

    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val currentSubject = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (initialMistake != null) "Edit Error Entry" else "Log Mock / PYQ Mistake",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { isStarred = !isStarred },
                        modifier = Modifier.testTag("dialog_toggle_star")
                    ) {
                        Icon(
                            imageVector = if (isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Star",
                            tint = if (isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Record questions you got wrong, miscalculated, or panicked on to never repeat them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subject Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currentSubject?.name ?: "Select Subject",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("dialog_subject_dropdown")
                    )
                    ExposedDropdownMenu(
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

                Spacer(modifier = Modifier.height(12.dp))

                // Error Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.emoji} ${selectedCategory.label}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mistake Root Cause / Reason") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("dialog_category_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        MistakeCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${cat.emoji} ${cat.label}", fontWeight = FontWeight.SemiBold)
                                        Text(cat.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedCategory = cat
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Question Text Field
                OutlinedTextField(
                    value = questionText,
                    onValueChange = {
                        questionText = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Question / Problem Statement *") },
                    placeholder = { Text("e.g., In triangle ABC, AD is median...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_question_input"),
                    minLines = 2,
                    maxLines = 5,
                    isError = isError,
                    supportingText = if (isError) { { Text("Question text is required", color = MaterialTheme.colorScheme.error) } } else null
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Wrong Answer (My Slip)
                OutlinedTextField(
                    value = yourWrongAnswer,
                    onValueChange = { yourWrongAnswer = it },
                    label = { Text("Your Wrong Answer / What went wrong?") },
                    placeholder = { Text("e.g., Marked option (B) 48 instead of 36 due to sign error") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_wrong_answer_input"),
                    minLines = 1,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Correct Answer
                OutlinedTextField(
                    value = correctAnswer,
                    onValueChange = { correctAnswer = it },
                    label = { Text("Correct Answer / Option") },
                    placeholder = { Text("e.g., Option (D) 36") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_correct_answer_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Solution / Explanation / Key Concept
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Key Formula / Concept / Explanation") },
                    placeholder = { Text("e.g., Apollonius Theorem: AB^2 + AC^2 = 2(AD^2 + BD^2)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_explanation_input"),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Source Mock / Book & Chapter Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sourceMock,
                        onValueChange = { sourceMock = it },
                        label = { Text("Source / Mock") },
                        placeholder = { Text("e.g. Mock #12") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = chapterTitle,
                        onValueChange = { chapterTitle = it },
                        label = { Text("Chapter Name") },
                        placeholder = { Text("e.g. Geometry") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tags
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    placeholder = { Text("e.g. Tier-1, Geometry, Formula Trap") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_cancel_btn")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (questionText.isBlank()) {
                                isError = true
                                return@Button
                            }
                            val subName = currentSubject?.name ?: "General"
                            onSave(
                                questionText,
                                yourWrongAnswer,
                                correctAnswer,
                                explanation,
                                selectedSubjectId,
                                subName,
                                chapterTitle,
                                sourceMock,
                                selectedCategory,
                                isStarred,
                                tags
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dialog_save_mistake_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (initialMistake != null) "Update Mistake" else "Save Mistake")
                    }
                }
            }
        }
    }
}
