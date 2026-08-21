package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.data.model.SyllabusItem

@Composable
fun BulkAddDialog(
    subject: Subject,
    parentSection: SyllabusItem?,
    onDismiss: () -> Unit,
    onConfirmBulkAdd: (String) -> Unit
) {
    var multilineInput by remember { mutableStateOf("") }
    val lineCount = remember(multilineInput) {
        multilineInput.lines().filter { it.trim().isNotEmpty() }.size
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bulk Add Chapters", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Adding to: ${subject.name}${if (parentSection != null) " → " + parentSection.title else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter or paste each chapter on a new line.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = multilineInput,
                    onValueChange = { multilineInput = it },
                    placeholder = {
                        Text(
                            "Chapter 1: Indus Valley Civilization\nChapter 2: Vedic Period\nChapter 3: Mauryan Empire\nChapter 4: Gupta Age",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$lineCount chapter(s) recognized",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (lineCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (multilineInput.isNotBlank()) {
                        onConfirmBulkAdd(multilineInput)
                        onDismiss()
                    }
                },
                enabled = lineCount > 0
            ) {
                Text("Add $lineCount Chapters")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
