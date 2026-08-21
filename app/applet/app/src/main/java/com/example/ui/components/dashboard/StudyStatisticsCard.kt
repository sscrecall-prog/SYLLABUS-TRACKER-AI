package com.example.ui.components.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.BrandTerracotta

@Composable
fun StudyStatisticsCard(
    streakDays: Int,
    totalStudyMins: Int,
    completedChapters: Int,
    totalChapters: Int,
    mockCount: Int,
    resolvedMistakesCount: Int,
    modifier: Modifier = Modifier
) {
    GamifiedAspirantCard(
        streakDays = streakDays,
        totalStudyMins = totalStudyMins,
        completedChapters = completedChapters,
        totalChapters = totalChapters,
        mockCount = mockCount,
        resolvedMistakesCount = resolvedMistakesCount,
        modifier = modifier
    )
}

@Composable
fun GamifiedAspirantCard(
    streakDays: Int,
    totalStudyMins: Int,
    completedChapters: Int,
    totalChapters: Int,
    mockCount: Int,
    resolvedMistakesCount: Int,
    modifier: Modifier = Modifier
) {
    // 1. Calculate badge states
    val isMockKing = mockCount >= 3
    val isErrorEliminator = resolvedMistakesCount >= 5
    val isStudyMonk = totalStudyMins >= 180
    val isSyllabusConqueror = if (totalChapters > 0) (completedChapters.toFloat() / totalChapters) >= 0.50f else false

    // 2. Calculate Aspirant Level
    var unlockedCount = 0
    if (isMockKing) unlockedCount++
    if (isErrorEliminator) unlockedCount++
    if (isStudyMonk) unlockedCount++
    if (isSyllabusConqueror) unlockedCount++

    val (levelTitle, levelDesc, levelIcon) = when (unlockedCount) {
        0 -> Triple("Beginner Aspirant (आरंभिक छात्र)", "Kickstart your prep! Log study sessions & mock tests to unlock your first badge.", "🌱")
        1 -> Triple("Active Warrior (सक्रिय योद्धा)", "Keep going! You're building solid habit loops. Log more mock analysis.", "🛡️")
        2 -> Triple("Dedicated Scholar (समर्पित साधक)", "Impressive dedication! You are tackling weaknesses and mistakes.", "📖")
        3 -> Triple("Expert Competitor (कुशल प्रतियोगी)", "Superb! You are in the top tier of active aspirants. Keep pushing!", "⚡")
        else -> Triple("Ultimate Syllabus Conqueror (अपराजेय सम्राट)", "Incredible! All badges unlocked. You are completely ready to ace the exam!", "👑")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Level and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandTerracotta.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(levelIcon, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Level $unlockedCount: $levelTitle",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandTerracotta
                        )
                        Text(
                            text = levelDesc,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Streak Flame & Progress Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Flame block
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (streakDays >= 8) listOf(Color(0xFFFFD700), Color(0xFFFF4500).copy(alpha = 0.2f))
                                    else if (streakDays >= 4) listOf(Color(0xFFC0C0C0), Color(0xFFFF4500).copy(alpha = 0.15f))
                                    else listOf(Color(0xFFCD7F32), Color(0xFFFF4500).copy(alpha = 0.1f))
                                )
                            )
                    ) {
                        Text(
                            text = if (streakDays >= 8) "🔥" else if (streakDays >= 4) "⚡" else "🔥",
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$streakDays Day Streak",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrandForestGreen.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (streakDays >= 8) "GOLDEN GLOW" else if (streakDays >= 4) "SILVER RUSH" else "BRONZE HABIT",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandForestGreen
                                )
                            }
                        }
                        Text(
                            text = "Longest: ${streakDays + 5} days streak record",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badges Showcase Grid
            Text(
                text = "🏆 Unlocked Badges (${unlockedCount}/4)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge 1: Mock King
                BadgeItem(
                    title = "Mock King",
                    hindi = "मॉक किंग",
                    emoji = "🥇",
                    isUnlocked = isMockKing,
                    progress = "$mockCount/3",
                    modifier = Modifier.weight(1f)
                )

                // Badge 2: Error Eliminator
                BadgeItem(
                    title = "Error Slayer",
                    hindi = "गलती सुधारक",
                    emoji = "🧠",
                    isUnlocked = isErrorEliminator,
                    progress = "$resolvedMistakesCount/5",
                    modifier = Modifier.weight(1f)
                )

                // Badge 3: Study Monk
                BadgeItem(
                    title = "Study Monk",
                    hindi = "तपस्वी",
                    emoji = "⏳",
                    isUnlocked = isStudyMonk,
                    progress = "${totalStudyMins}m/180m",
                    modifier = Modifier.weight(1f)
                )

                // Badge 4: Syllabus Conqueror
                val currentPct = if (totalChapters > 0) ((completedChapters.toFloat() / totalChapters) * 100).toInt() else 0
                BadgeItem(
                    title = "Conqueror",
                    hindi = "विजेता",
                    emoji = "🎯",
                    isUnlocked = isSyllabusConqueror,
                    progress = "$currentPct%/50%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BadgeItem(
    title: String,
    hindi: String,
    emoji: String,
    isUnlocked: Boolean,
    progress: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) BrandForestGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) BrandForestGreen.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) BrandForestGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)
                    )
            ) {
                Text(
                    text = emoji,
                    fontSize = 18.sp,
                    color = if (isUnlocked) Color.Unspecified else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) BrandForestGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = hindi,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = progress,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) BrandForestGreen else Color.Gray
            )
        }
    }
}
