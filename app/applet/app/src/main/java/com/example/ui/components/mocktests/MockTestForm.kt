package com.example.ui.components.mocktests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MockTest
import com.example.data.model.MockTestType
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.BrandTerracotta
import com.example.ui.theme.BrandWarmCream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestForm(
    initialMock: MockTest?,
    onDismiss: () -> Unit,
    onSave: (MockTest) -> Unit,
    modifier: Modifier = Modifier
) {
    AddEditMockTestDialog(
        initialMock = initialMock,
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMockTestDialog(
    initialMock: MockTest?,
    onDismiss: () -> Unit,
    onSave: (MockTest) -> Unit
) {
    val isEdit = initialMock != null

    val todayStr = remember {
        val sdf = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault())
        sdf.format(Date())
    }

    var testName by remember { mutableStateOf(initialMock?.testName ?: "") }
    var testPlatform by remember { mutableStateOf(initialMock?.testPlatform ?: "Testbook") }
    var customPlatformInput by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(initialMock != null && initialMock.testPlatform !in listOf("Testbook", "Oliveboard", "PracticeMock", "Gradeup", "Unacademy")) }
    var testType by remember { mutableStateOf(initialMock?.testType ?: MockTestType.FULL) }
    var testDateStr by remember { mutableStateOf(initialMock?.dateAttemptedStr ?: todayStr) }

    var marksScoredStr by remember { mutableStateOf(initialMock?.marksScored?.takeIf { it > 0 }?.toString() ?: "") }
    var totalMarksStr by remember { mutableStateOf(initialMock?.totalMarks?.toString() ?: "200") }
    var cutoffMarksStr by remember { mutableStateOf(initialMock?.cutoffMarks?.takeIf { it > 0 }?.toString() ?: "135") }
    var timeTakenStr by remember { mutableStateOf(initialMock?.timeTakenMinutes?.toString() ?: "60") }
    var totalQuestionsStr by remember { mutableStateOf(initialMock?.totalQuestions?.toString() ?: "100") }
    var attemptedStr by remember { mutableStateOf(initialMock?.attemptedQuestions?.toString() ?: "") }
    var correctStr by remember { mutableStateOf(initialMock?.correctQuestions?.toString() ?: "") }
    var incorrectStr by remember { mutableStateOf(initialMock?.incorrectQuestions?.toString() ?: "") }
    var percentileStr by remember { mutableStateOf(initialMock?.percentile?.toString() ?: "") }
    var rankStr by remember { mutableStateOf(initialMock?.rank?.takeIf { it > 0 }?.toString() ?: "") }
    var totalStudentsStr by remember { mutableStateOf(initialMock?.totalStudents?.takeIf { it > 0 }?.toString() ?: "") }

    // Sectional
    var mathScoreStr by remember { mutableStateOf(initialMock?.mathScore?.takeIf { it > 0 }?.toString() ?: "") }
    var reasScoreStr by remember { mutableStateOf(initialMock?.reasoningScore?.takeIf { it > 0 }?.toString() ?: "") }
    var engScoreStr by remember { mutableStateOf(initialMock?.englishScore?.takeIf { it > 0 }?.toString() ?: "") }
    var gsScoreStr by remember { mutableStateOf(initialMock?.gsScore?.takeIf { it > 0 }?.toString() ?: "") }

    var weakAreas by remember { mutableStateOf(initialMock?.weakAreasIdentified ?: "") }
    var analysisNotes by remember { mutableStateOf(initialMock?.analysisNotes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isEdit) "Edit Mock Test" else "Log Mock Test",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Form Fields
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Test Name
                    item {
                        OutlinedTextField(
                            value = testName,
                            onValueChange = { testName = it },
                            label = { Text("Mock Test Title / Name *") },
                            placeholder = { Text("e.g., SSC CGL Tier 1 Live Mock #14") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_mock_title")
                        )
                    }

                    // Platform Selection
                    item {
                        Text("Test Platform", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val defaultPlats = listOf("Testbook", "Oliveboard", "PracticeMock", "Gradeup", "Unacademy")
                            items(defaultPlats) { plat ->
                                FilterChip(
                                    selected = !showCustomInput && testPlatform.equals(plat, ignoreCase = true),
                                    onClick = {
                                        showCustomInput = false
                                        testPlatform = plat
                                    },
                                    label = { Text(plat, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandForestGreen,
                                        selectedLabelColor = BrandWarmCream
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = showCustomInput,
                                    onClick = {
                                        showCustomInput = true
                                        testPlatform = customPlatformInput.ifBlank { "Custom" }
                                    },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Text(if (customPlatformInput.isNotBlank()) customPlatformInput else "Other / Custom", fontSize = 12.sp)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandTerracotta,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        if (showCustomInput) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customPlatformInput,
                                onValueChange = {
                                    customPlatformInput = it
                                    testPlatform = it.ifBlank { "Custom" }
                                },
                                label = { Text("Enter Custom Platform Name") },
                                placeholder = { Text("e.g. Pinnacle, Exampur, RBE, Neon") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_custom_platform")
                            )
                        }
                    }

                    // Test Type
                    item {
                        Text("Test Type", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(MockTestType.values()) { type ->
                                FilterChip(
                                    selected = testType == type,
                                    onClick = { testType = type },
                                    label = { Text(type.label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandTerracotta,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Date & Time Spent
                    item {
                        val context = LocalContext.current
                        val calendar = Calendar.getInstance()
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            sdf.parse(testDateStr)?.let {
                                calendar.time = it
                            }
                        } catch (e: Exception) {}

                        val datePickerDialog = remember {
                            android.app.DatePickerDialog(
                                context,
                                { _, selectedYear, selectedMonth, selectedDay ->
                                    testDateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = testDateStr,
                                    onValueChange = { testDateStr = it },
                                    label = { Text("Date (YYYY-MM-DD)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { datePickerDialog.show() }) {
                                            Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { datePickerDialog.show() }
                                )
                            }

                            OutlinedTextField(
                                value = timeTakenStr,
                                onValueChange = { timeTakenStr = it },
                                label = { Text("Time (Minutes)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Marks Scored & Total Marks & Cutoff
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = marksScoredStr,
                                onValueChange = { marksScoredStr = it },
                                label = { Text("Marks Scored *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_mock_score")
                            )

                            OutlinedTextField(
                                value = totalMarksStr,
                                onValueChange = { totalMarksStr = it },
                                label = { Text("Total Marks") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = cutoffMarksStr,
                                onValueChange = { cutoffMarksStr = it },
                                label = { Text("Cutoff Marks") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Question Breakdown
                    item {
                        Text("Question Breakdown (Optional)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = totalQuestionsStr,
                                onValueChange = { totalQuestionsStr = it },
                                label = { Text("Total Qs") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = attemptedStr,
                                onValueChange = { attemptedStr = it },
                                label = { Text("Attempted") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = correctStr,
                                onValueChange = { correctStr = it },
                                label = { Text("Correct ✓") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = incorrectStr,
                                onValueChange = { incorrectStr = it },
                                label = { Text("Wrong ✗") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Percentile & Rank
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = percentileStr,
                                onValueChange = { percentileStr = it },
                                label = { Text("Percentile %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = rankStr,
                                onValueChange = { rankStr = it },
                                label = { Text("Rank (e.g. 450)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = totalStudentsStr,
                                onValueChange = { totalStudentsStr = it },
                                label = { Text("Total Test Takers") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Sectional Scores Breakdown
                    item {
                        Text("Sectional Marks (Optional)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = mathScoreStr,
                                onValueChange = { mathScoreStr = it },
                                label = { Text("📐 Quant") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = reasScoreStr,
                                onValueChange = { reasScoreStr = it },
                                label = { Text("🧠 Reas") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = engScoreStr,
                                onValueChange = { engScoreStr = it },
                                label = { Text("📖 Eng") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = gsScoreStr,
                                onValueChange = { gsScoreStr = it },
                                label = { Text("🏛️ GS") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Weak Areas Identified
                    item {
                        OutlinedTextField(
                            value = weakAreas,
                            onValueChange = { weakAreas = it },
                            label = { Text("Weak Chapters / Silly Mistakes Identified") },
                            placeholder = { Text("e.g. Trigonometry Height & Distance, Polity Articles") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Analysis & Learning Notes
                    item {
                        OutlinedTextField(
                            value = analysisNotes,
                            onValueChange = { analysisNotes = it },
                            label = { Text("Post-Test Analysis Notes & Strategy") },
                            placeholder = { Text("e.g. Solved Quant in 22 mins, English RC was easy, need to avoid guessing in GS.") },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val scored = marksScoredStr.toFloatOrNull() ?: 0f
                            val total = totalMarksStr.toFloatOrNull() ?: 200f
                            val cutoff = cutoffMarksStr.toFloatOrNull() ?: 135f
                            val attempted = attemptedStr.toIntOrNull() ?: 0
                            val correct = correctStr.toIntOrNull() ?: 0
                            val incorrect = incorrectStr.toIntOrNull() ?: 0
                            val totalQ = totalQuestionsStr.toIntOrNull() ?: 100
                            val accuracy = if (attempted > 0) (correct.toFloat() / attempted.toFloat()) * 100f else 0f
                            val percentile = percentileStr.toFloatOrNull() ?: 0f
                            val rank = rankStr.toIntOrNull() ?: 0
                            val students = totalStudentsStr.toIntOrNull() ?: 0
                            val timeTaken = timeTakenStr.toIntOrNull() ?: 60
                            val math = mathScoreStr.toFloatOrNull() ?: 0f
                            val reas = reasScoreStr.toFloatOrNull() ?: 0f
                            val eng = engScoreStr.toFloatOrNull() ?: 0f
                            val gs = gsScoreStr.toFloatOrNull() ?: 0f

                            val newMock = (initialMock ?: MockTest(
                                testName = if (testName.isNotBlank()) testName else "Mock Test #${System.currentTimeMillis() % 1000}",
                                testDateStr = testDateStr
                            )).copy(
                                testName = if (testName.isNotBlank()) testName else "Mock Test",
                                testPlatform = testPlatform,
                                testType = testType,
                                testDateStr = testDateStr,
                                totalMarks = total,
                                marksScored = scored,
                                cutoffMarks = cutoff,
                                timeTakenMinutes = timeTaken,
                                totalQuestions = totalQ,
                                attemptedQuestions = attempted,
                                correctQuestions = correct,
                                incorrectQuestions = incorrect,
                                accuracy = accuracy,
                                percentile = percentile,
                                rank = rank,
                                totalStudents = students,
                                mathScore = math,
                                mathTotal = if (math > 0) 50f else 0f,
                                reasoningScore = reas,
                                reasoningTotal = if (reas > 0) 50f else 0f,
                                englishScore = eng,
                                englishTotal = if (eng > 0) 50f else 0f,
                                gsScore = gs,
                                gsTotal = if (gs > 0) 50f else 0f,
                                weakAreasIdentified = weakAreas,
                                analysisNotes = analysisNotes,
                                isClearedCutoff = scored >= cutoff
                            )
                            onSave(newMock)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandForestGreen),
                        modifier = Modifier.testTag("save_mock_button")
                    ) {
                        Text(if (isEdit) "Update Mock" else "Save Mock")
                    }
                }
            }
        }
    }
}
