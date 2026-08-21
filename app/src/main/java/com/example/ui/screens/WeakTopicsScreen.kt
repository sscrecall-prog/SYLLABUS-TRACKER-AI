package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterStatus
import com.example.ui.components.ConfidenceStars
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.TimerViewModel
import com.example.ui.viewmodel.SyllabusViewModel
import com.example.ui.viewmodel.MainViewModel

@Composable
fun WeakChaptersScreen(
    onNavigate: (NavDestination) -> Unit,
    syllabusViewModel: SyllabusViewModel = viewModel(),
    subjectViewModel: SubjectViewModel = viewModel(),
    timerViewModel: TimerViewModel = viewModel()
) {
    val weakChapters by syllabusViewModel.weakChapters.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()

    val subjectsMap = remember(subjects) { subjects.associateBy { it.id } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        item {
            GradientCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = listOf(Color(0xFFC62828), Color(0xFF8E0000))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🚨 Weak Chapters Review",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandWarmCream
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Chapters with low confidence or PYQ accuracy < 60%",
                                fontSize = 12.sp,
                                color = BrandCreamDark
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${weakChapters.size}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandWarmCream
                            )
                        }
                    }
                }
            }
        }

        if (weakChapters.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = StatusCompleted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Weak Chapters Found! 🌟", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All your studied chapters are above confidence threshold. Keep up the high retention!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(weakChapters, key = { it.id }) { chapter ->
                val sub = subjectsMap[chapter.subjectId]
                val subColor = try { Color(android.graphics.Color.parseColor(sub?.colorHex ?: "#2D4F1E")) } catch (e: Exception) { BrandForestGreen }

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syllabusViewModel.selectChapter(chapter) },
                    shape = RoundedCornerShape(14.dp),
                    elevation = 2.dp,
                    accentColor = StatusWeak
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(subColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sub?.name ?: "Subject",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = subColor
                                )
                            }
                            DifficultyBadge(difficulty = chapter.difficulty)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = chapter.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Confidence: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    ConfidenceStars(confidence = chapter.confidence)
                                }
                                if (chapter.pyqAttempted > 0) {
                                    Text(
                                        text = "PYQ Accuracy: ${chapter.pyqAccuracy}% (${chapter.pyqCorrect}/${chapter.pyqAttempted})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StatusWeak
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        timerViewModel.setTimerTargetById(sub?.id, chapter.id)
                                        onNavigate(NavDestination.TIMER)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Study", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { syllabusViewModel.markChapterStrong(chapter) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Mark Strong", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
