package com.example.ui.components.mocktests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MistakeCategory
import com.example.data.model.MockTest
import com.example.ui.theme.*

@Composable
fun MockTestDetails(
    mockTest: MockTest,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddMistake: (questionText: String, wrongAns: String, correctAns: String, exp: String, category: MistakeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    MockTestDetailDialog(
        mockTest = mockTest,
        onDismiss = onDismiss,
        onEdit = onEdit,
        onDelete = onDelete,
        onAddMistake = onAddMistake
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestDetailDialog(
    mockTest: MockTest,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddMistake: (questionText: String, wrongAns: String, correctAns: String, exp: String, category: MistakeCategory) -> Unit
) {
    val isCleared = mockTest.marksScored >= mockTest.cutoffMarks
    val diff = mockTest.marksScored - mockTest.cutoffMarks

    var questionText by remember { mutableStateOf("") }
    var yourWrongAns by remember { mutableStateOf("") }
    var correctAns by remember { mutableStateOf("") }
    var explanationText by remember { mutableStateOf("") }
    var mistakeCategory by remember { mutableStateOf(MistakeCategory.SILLY_ERROR) }
    var showQuickMistakeInput by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrandForestGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = mockTest.testPlatform,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandForestGreen
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mockTest.testDateStr,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mockTest.testName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Performance Hero in Details
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        if (isCleared) listOf(BrandForestGreen, BrandForestGreenLight)
                                        else listOf(Color(0xFFB71C1C), Color(0xFFE53935))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "TOTAL SCORE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "${mockTest.marksScored} / ${mockTest.totalMarks.toInt()}",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (isCleared) "✅ Qualified (+${String.format("%.1f", diff)} above cutoff)" else "❌ Missed Cutoff (${String.format("%.1f", diff)} below)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandWarmCream
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${String.format("%.1f", mockTest.percentile)}%",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrandWarmCream
                                        )
                                        Text(
                                            text = "Percentile",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                        if (mockTest.rank > 0) {
                                            Text(
                                                text = "AIR #${mockTest.rank} / ${mockTest.totalStudents}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Question Analytics breakdown
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Attempt & Accuracy Summary", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Attempted", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${mockTest.attemptedQuestions}/${mockTest.totalQuestions}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Correct", fontSize = 11.sp, color = StatusCompleted)
                                        Text("${mockTest.correctQuestions} (${String.format("%.1f", mockTest.accuracy)}%)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusCompleted)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Incorrect", fontSize = 11.sp, color = StatusWeak)
                                        Text("${mockTest.incorrectQuestions}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusWeak)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Speed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        val marksPerMin = if (mockTest.timeTakenMinutes > 0) mockTest.marksScored / mockTest.timeTakenMinutes else 0f
                                        Text("${String.format("%.2f", marksPerMin)} m/m", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Sectional breakdown if available
                    if (mockTest.mathTotal > 0 || mockTest.reasoningTotal > 0 || mockTest.englishTotal > 0 || mockTest.gsTotal > 0) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Sectional Marks Breakdown", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (mockTest.mathTotal > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("📐 Quantitative Aptitude", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Text("${mockTest.mathScore.toInt()} / ${mockTest.mathTotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (mockTest.reasoningTotal > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("🧠 Reasoning Ability", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Text("${mockTest.reasoningScore.toInt()} / ${mockTest.reasoningTotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (mockTest.englishTotal > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("📖 English Comprehension", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Text("${mockTest.englishScore.toInt()} / ${mockTest.englishTotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (mockTest.gsTotal > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("🏛️ General Awareness", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Text("${mockTest.gsScore.toInt()} / ${mockTest.gsTotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Weak areas & Notes
                    if (mockTest.weakAreasIdentified.isNotBlank() || mockTest.analysisNotes.isNotBlank()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    if (mockTest.weakAreasIdentified.isNotBlank()) {
                                        Text("⚠️ Weak Areas Identified", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusWeak)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(mockTest.weakAreasIdentified, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                    if (mockTest.analysisNotes.isNotBlank()) {
                                        Text("📝 Post-Test Strategy & Notes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(mockTest.analysisNotes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // Log Quick Mistakes to Notebook
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandForestGreen.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandForestGreen.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showQuickMistakeInput = !showQuickMistakeInput },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = BrandForestGreen, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Direct Error Logging to Notebook",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandForestGreen
                                        )
                                    }
                                    IconButton(
                                        onClick = { showQuickMistakeInput = !showQuickMistakeInput },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showQuickMistakeInput) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle Input",
                                            tint = BrandForestGreen
                                        )
                                    }
                                }

                                if (showQuickMistakeInput) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = questionText,
                                        onValueChange = { questionText = it },
                                        label = { Text("Wrong Question / Missed Concept", fontSize = 12.sp) },
                                        placeholder = { Text("e.g. Geometry tangent formula / Grammatical error in parallel structures...") },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("quick_question_field")
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = yourWrongAns,
                                            onValueChange = { yourWrongAns = it },
                                            label = { Text("Your Wrong Ans", fontSize = 11.sp) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = correctAns,
                                            onValueChange = { correctAns = it },
                                            label = { Text("Correct Ans", fontSize = 11.sp) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = explanationText,
                                        onValueChange = { explanationText = it },
                                        label = { Text("Post-Test Explanation / Core Concept", fontSize = 12.sp) },
                                        placeholder = { Text("Write formulas or learning trick...") },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Mistake Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(MistakeCategory.values()) { cat ->
                                            val isSelected = mistakeCategory == cat
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { mistakeCategory = cat },
                                                label = { Text(cat.label, fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = BrandTerracotta,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            if (questionText.isNotBlank()) {
                                                onAddMistake(questionText, yourWrongAns, correctAns, explanationText, mistakeCategory)
                                                questionText = ""
                                                yourWrongAns = ""
                                                correctAns = ""
                                                explanationText = ""
                                                showQuickMistakeInput = false
                                            }
                                        },
                                        enabled = questionText.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandForestGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Save to Mistake Notebook 📓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = StatusWeak)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                    Row {
                        OutlinedButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandForestGreen)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
