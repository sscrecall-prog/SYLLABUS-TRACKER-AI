package com.example.ui.components.mocktests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockTest
import com.example.ui.theme.*

@Composable
fun MockTestCard(
    mockTest: MockTest,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCleared = mockTest.marksScored >= mockTest.cutoffMarks
    val diff = mockTest.marksScored - mockTest.cutoffMarks

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("mock_card_${mockTest.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isCleared) BrandForestGreen.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Platform Chip, Type Badge, and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandForestGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mockTest.testPlatform,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandForestGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mockTest.testType.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCleared) StatusCompleted.copy(alpha = 0.15f) else StatusWeak.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isCleared) "CUTOFF CLEARED ✓" else "BELOW CUTOFF ✗",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCleared) StatusCompleted else StatusWeak
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mockTest.testName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Attempted on ${mockTest.dateAttemptedStr}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Score & Stats 3-Column Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score
                Column {
                    Text(
                        text = "SCORE / TOTAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${mockTest.marksScored.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCleared) BrandForestGreen else StatusWeak
                        )
                        Text(
                            text = " / ${mockTest.totalMarks.toInt()}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    val diffStr = if (diff >= 0) "+${String.format("%.1f", diff)} Cutoff" else "${String.format("%.1f", diff)} Cutoff"
                    Text(
                        text = diffStr,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (diff >= 0) StatusCompleted else StatusWeak
                    )
                }

                // Percentile
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PERCENTILE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${String.format("%.1f", mockTest.percentile)}%",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandTerracotta
                    )
                    if (mockTest.rank > 0 && mockTest.totalStudents > 0) {
                        Text(
                            text = "Rank #${mockTest.rank}/${(mockTest.totalStudents/1000)}k",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Accuracy
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ACCURACY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${String.format("%.1f", mockTest.accuracy)}%",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mockTest.accuracy >= 85) StatusCompleted else if (mockTest.accuracy >= 70) StatusInProgress else StatusWeak
                    )
                    Text(
                        text = "${mockTest.correctQuestions}C / ${mockTest.incorrectQuestions}W / ${mockTest.totalQuestions - mockTest.attemptedQuestions}L",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sectional Breakdown Pills if available
            if (mockTest.mathTotal > 0 || mockTest.englishTotal > 0 || mockTest.reasoningTotal > 0 || mockTest.gsTotal > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (mockTest.mathTotal > 0) {
                        SectionScorePill(
                            label = "Quant",
                            score = mockTest.mathScore,
                            total = mockTest.mathTotal,
                            color = Color(0xFFE27D60),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (mockTest.reasoningTotal > 0) {
                        SectionScorePill(
                            label = "Reas",
                            score = mockTest.reasoningScore,
                            total = mockTest.reasoningTotal,
                            color = Color(0xFF8E24AA),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (mockTest.englishTotal > 0) {
                        SectionScorePill(
                            label = "Eng",
                            score = mockTest.englishScore,
                            total = mockTest.englishTotal,
                            color = Color(0xFF3F51B5),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (mockTest.gsTotal > 0) {
                        SectionScorePill(
                            label = "GS",
                            score = mockTest.gsScore,
                            total = mockTest.gsTotal,
                            color = Color(0xFF2D4F1E),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Weak areas tag if provided
            if (mockTest.weakAreasIdentified.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusWeak,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Weak Areas: ${mockTest.weakAreasIdentified}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Actions Footer
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deep Analysis", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusWeak, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun SectionScorePill(
    label: String,
    score: Float,
    total: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "${score.toInt()}/${total.toInt()}",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
