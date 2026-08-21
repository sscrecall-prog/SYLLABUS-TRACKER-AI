package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject

@Composable
fun SubjectEditDialog(
    subject: Subject?, // null if adding new
    onDismiss: () -> Unit,
    onSave: (Subject) -> Unit,
    onDelete: ((Subject) -> Unit)? = null
) {
    var name by remember { mutableStateOf(subject?.name ?: "") }
    var code by remember { mutableStateOf(subject?.code ?: "") }
    var description by remember { mutableStateOf(subject?.description ?: "") }
    var colorHex by remember { mutableStateOf(subject?.colorHex ?: "#2D4F1E") }
    var iconName by remember { mutableStateOf(subject?.iconName ?: "School") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val colorOptions = listOf(
        "#2D4F1E", "#3F51B5", "#8E24AA", "#E27D60", "#00897B",
        "#D81B60", "#039BE5", "#FB8C00", "#5D4037", "#455A64"
    )

    val iconOptions = listOf(
        "School" to Icons.Default.School,
        "Calculate" to Icons.Default.Calculate,
        "MenuBook" to Icons.Default.MenuBook,
        "Psychology" to Icons.Default.Psychology,
        "Science" to Icons.Default.Science,
        "Translate" to Icons.Default.Translate,
        "History" to Icons.Default.History,
        "Public" to Icons.Default.Public,
        "Computer" to Icons.Default.Computer
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("subject_edit_dialog"),
        title = {
            Text(
                text = if (subject == null) "New Subject" else "Edit Subject",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name *") },
                    placeholder = { Text("e.g. Mathematics, Quantitative Aptitude") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Short Code (optional)") },
                    placeholder = { Text("e.g. MATH, QA, GS") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_code_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Modules summary") },
                    placeholder = { Text("e.g. Arithmetic, Algebra, Geometry") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_description_input"),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Icon", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(iconOptions) { (nameKey, iconVec) ->
                        val isSel = iconName == nameKey
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    if (isSel) 2.dp else 0.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { iconName = nameKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = nameKey,
                                tint = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Color Theme", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.take(5).forEach { hex ->
                        val col = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Green }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    if (colorHex == hex) 3.dp else 0.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.drop(5).forEach { hex ->
                        val col = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Green }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    if (colorHex == hex) 3.dp else 0.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val s = subject?.copy(
                            name = name.trim(),
                            code = code.trim(),
                            description = description.trim(),
                            colorHex = colorHex,
                            iconName = iconName
                        ) ?: Subject(
                            name = name.trim(),
                            code = code.trim(),
                            description = description.trim(),
                            colorHex = colorHex,
                            iconName = iconName
                        )
                        onSave(s)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_subject_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (subject != null && onDelete != null) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("delete_subject_button")
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_subject_button")
                ) {
                    Text("Cancel")
                }
            }
        }
    )

    if (showDeleteConfirm && subject != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Subject?") },
            text = { Text("Are you sure you want to delete '${subject.name}' and all its sections and chapters? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(subject)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
