package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.intelligence.*
import com.example.ui.theme.*
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailSheet(
    chapter: SyllabusItem,
    onDismiss: () -> Unit,
    onSave: (SyllabusItem) -> Unit,
    onDelete: (SyllabusItem) -> Unit,
    onMarkRevised: (SyllabusItem) -> Unit,
    onStartTimer: (SyllabusItem) -> Unit
) {
    var title by remember { mutableStateOf(chapter.title) }
    var status by remember { mutableStateOf(chapter.status) }
    var completionPercentage by remember { mutableFloatStateOf(chapter.completionPercentage.toFloat()) }
    var confidence by remember { mutableIntStateOf(chapter.confidence) }
    var priority by remember { mutableStateOf(chapter.priority) }
    var difficulty by remember { mutableStateOf(chapter.difficulty) }
    var notes by remember { mutableStateOf(chapter.notes) }
    var tags by remember { mutableStateOf(chapter.tags) }
    var isImportant by remember { mutableStateOf(chapter.isImportant) }
    var isBookmarked by remember { mutableStateOf(chapter.isBookmarked) }
    var studyTimeMinutes by remember { mutableIntStateOf(chapter.studyTimeMinutes) }
    
    // PYQ tracking
    var pyqTotal by remember { mutableIntStateOf(chapter.pyqTotal) }
    var pyqAttempted by remember { mutableIntStateOf(chapter.pyqAttempted) }
    var pyqCorrect by remember { mutableIntStateOf(chapter.pyqCorrect) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val shortDateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row: Type, Title, Pin & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chapter.itemType.label.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• Tracking & Meta",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = { isImportant = !isImportant }) {
                        Icon(
                            imageVector = if (isImportant) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Important",
                            tint = if (isImportant) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { isBookmarked = !isBookmarked }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) BrandTerracotta else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Chapter Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Chapter Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Topic Intelligence & Why This Topic Analysis
            val liveChapter = remember(chapter, title, status, completionPercentage, confidence, priority, pyqAttempted, pyqCorrect) {
                chapter.copy(
                    title = title,
                    status = status,
                    completionPercentage = completionPercentage.toInt(),
                    confidence = confidence,
                    priority = priority,
                    pyqAttempted = pyqAttempted,
                    pyqCorrect = pyqCorrect
                )
            }
            val intel = remember(liveChapter) {
                CoreIntelligenceEngine.calculateTopicIntelligence(liveChapter)
            }
            val whyExplanation = remember(liveChapter, intel) {
                AdaptivePlanningEngine.generateWhyExplanation(liveChapter, intel)
            }
            val isMaint = remember(liveChapter, intel) {
                AdaptivePlanningEngine.isMaintenanceOnlyTopic(liveChapter, intel)
            }

            val levelColor = when (intel.masteryLevel) {
                MasteryLevel.MASTERED -> Color(0xFF10B981)
                MasteryLevel.STRONG -> Color(0xFF3B82F6)
                MasteryLevel.LEARNING -> Color(0xFFF59E0B)
                MasteryLevel.WEAK -> Color(0xFFEF4444)
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Topic Intelligence",
                                tint = levelColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Topic Intelligence",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = levelColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, levelColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = intel.masteryLevel.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = levelColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Mastery Score
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Mastery", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${intel.masteryScore.roundToInt()}/100", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = levelColor)
                            }
                        }

                        // Priority Score
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Priority", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${intel.priorityScore.roundToInt()}/100", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Weakness Score
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Weakness", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${intel.weaknessScore.roundToInt()}/100", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (intel.weaknessScore > 50) Color(0xFFEF4444) else Color(0xFF10B981))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sprint 3: Performance Recommendation & Feedback
                    val retention = remember(liveChapter) {
                        PerformanceFeedbackEngine.calculateRetentionStrength(liveChapter, emptyList())
                    }
                    val recommendation = remember(liveChapter, intel, retention) {
                        PerformanceFeedbackEngine.generatePerformanceRecommendation(liveChapter, intel, null, retention, null)
                    }

                    // Engine Analysis & Performance Feedback
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💡 Engine Analysis: $whyExplanation",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 14.sp
                        )

                        // Sprint 3 Recommendation Callout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Actionable Advice: ${recommendation.actionableAdvice}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Reason: ${recommendation.reason}",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Retention status
                        if (retention.hasSufficientData) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Memory Retention: ${retention.state.label}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (retention.state) {
                                        RetentionState.STRONG -> Color(0xFF10B981)
                                        RetentionState.MODERATE -> Color(0xFFF59E0B)
                                        RetentionState.WEAK -> Color(0xFFEF4444)
                                        RetentionState.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        if (isMaint) {
                            Text(
                                text = "🛡️ Maintenance Mode: Core syllabus time should prioritize weak chapters; keep this fresh with spaced revision.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Selector Chips
            Text(
                text = "Status",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChapterStatus.values().take(4).forEach { s ->
                    val selected = status == s
                    FilterChip(
                        selected = selected,
                        onClick = {
                            status = s
                            if (s == ChapterStatus.COMPLETED || s == ChapterStatus.MASTERED) {
                                completionPercentage = 100f
                            }
                        },
                        label = { Text("${s.iconEmoji} ${s.label}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = s.getColor().copy(alpha = 0.2f),
                            selectedLabelColor = s.getColor()
                        )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChapterStatus.values().drop(4).forEach { s ->
                    val selected = status == s
                    FilterChip(
                        selected = selected,
                        onClick = {
                            status = s
                            if (s == ChapterStatus.MASTERED) completionPercentage = 100f
                        },
                        label = { Text("${s.iconEmoji} ${s.label}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = s.getColor().copy(alpha = 0.2f),
                            selectedLabelColor = s.getColor()
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Completion Percentage Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Completion Progress",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${completionPercentage.toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = completionPercentage,
                onValueChange = {
                    completionPercentage = it
                    if (it.toInt() == 100 && status == ChapterStatus.NOT_STARTED) {
                        status = ChapterStatus.COMPLETED
                    }
                },
                valueRange = 0f..100f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confidence Level (1-5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confidence Level",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ConfidenceStars(
                    confidence = confidence,
                    onRatingChanged = { confidence = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority & Difficulty Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Priority
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Priority",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    var expandedPrio by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedPrio = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(priority.label, fontSize = 12.sp)
                        }
                        DropdownMenu(expanded = expandedPrio, onDismissRequest = { expandedPrio = false }) {
                            Priority.values().forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.label) },
                                    onClick = {
                                        priority = p
                                        expandedPrio = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Difficulty
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Difficulty",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    var expandedDiff by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedDiff = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(difficulty.label, fontSize = 12.sp)
                        }
                        DropdownMenu(expanded = expandedDiff, onDismissRequest = { expandedDiff = false }) {
                            Difficulty.values().forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d.label) },
                                    onClick = {
                                        difficulty = d
                                        expandedDiff = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spaced Repetition Info & Fast Schedule
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Update,
                                contentDescription = null,
                                tint = StatusRevisionDue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Spaced Repetition",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Revisions: ${chapter.revisionCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusRevisionDue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Last Studied:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = chapter.lastStudiedTimestamp?.let { shortDateFormat.format(Date(it)) } ?: "Not yet",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column {
                            Text("Next Revision:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = chapter.nextRevisionTimestamp?.let { shortDateFormat.format(Date(it)) } ?: "None scheduled",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (chapter.isRevisionDue) StatusRevisionDue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column {
                            Text("Study Time:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${studyTimeMinutes} mins", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onMarkRevised(chapter) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRevisionDue)
                        ) {
                            Text("Mark Revised", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onStartTimer(chapter) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Study Timer", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PYQ Question Tracking Section
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PYQ / Question Practice",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val acc = if (pyqAttempted > 0) ((pyqCorrect.toFloat() / pyqAttempted) * 100).toInt() else 0
                        Text(
                            text = "Accuracy: $acc%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (acc >= 70) StatusCompleted else if (acc >= 50) StatusInProgress else StatusWeak
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pyqTotal.toString(),
                            onValueChange = { pyqTotal = it.toIntOrNull() ?: 0 },
                            label = { Text("Total PYQs", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = pyqAttempted.toString(),
                            onValueChange = { pyqAttempted = it.toIntOrNull() ?: 0 },
                            label = { Text("Attempted", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = pyqCorrect.toString(),
                            onValueChange = { pyqCorrect = it.toIntOrNull() ?: 0 },
                            label = { Text("Correct", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags Field
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma separated e.g. #PYQ, #Important, #Formula)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes Editor with Quick Formatting Actions
            Text(
                text = "Personal Study Notes",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AssistChip(
                    onClick = { notes = "$notes\n• " },
                    label = { Text("• Bullet", fontSize = 10.sp) }
                )
                AssistChip(
                    onClick = { notes = "$notes\n1. " },
                    label = { Text("1. List", fontSize = 10.sp) }
                )
                AssistChip(
                    onClick = { notes = "$notes\n★ KEY CONCEPT: " },
                    label = { Text("★ Key Concept", fontSize = 10.sp) }
                )
                AssistChip(
                    onClick = { notes = "$notes\n⚠️ COMMON TRICK: " },
                    label = { Text("⚠️ Trick", fontSize = 10.sp) }
                )
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Key formulas, shortcuts, conceptual summary...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }

                Button(
                    onClick = {
                        val updated = chapter.copy(
                            title = title.trim().ifEmpty { chapter.title },
                            status = status,
                            completionPercentage = completionPercentage.toInt(),
                            confidence = confidence,
                            priority = priority,
                            difficulty = difficulty,
                            notes = notes,
                            tags = tags,
                            isImportant = isImportant,
                            isBookmarked = isBookmarked,
                            pyqTotal = pyqTotal,
                            pyqAttempted = pyqAttempted,
                            pyqCorrect = pyqCorrect
                        )
                        onSave(updated)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Changes")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Chapter?") },
            text = { Text("Are you sure you want to delete '${chapter.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(chapter)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
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
