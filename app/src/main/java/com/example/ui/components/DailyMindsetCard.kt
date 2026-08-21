package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AmbientSoundType
import com.example.ui.theme.*
import java.util.Calendar

data class StudyHack(
    val title: String,
    val description: String,
    val icon: String,
    val badge: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DailyMindsetCard(
    userName: String,
    currentAmbient: AmbientSoundType,
    isAmbientPlaying: Boolean,
    onSelectAmbient: (AmbientSoundType) -> Unit,
    onToggleAmbient: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Determine dynamic greeting based on system time
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val (greeting, greetingEmoji) = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good Morning" to "🌅"
            in 12..16 -> "Good Afternoon" to "☀️"
            in 17..20 -> "Good Evening" to "🌆"
            else -> "Good Night" to "🌌"
        }
    }

    val displayUsername = remember(userName) {
        userName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    // 2. Curated cognitive-science hacks carousel
    val hacks = remember {
        listOf(
            StudyHack(
                title = "Feynman Technique",
                description = "Try to explain your current topic (e.g. Stone Age) in extremely simple language, as if explaining to a 10-year-old. It instantly reveals gaps in your concept.",
                icon = "💡",
                badge = "ACTIVE RECALL"
            ),
            StudyHack(
                title = "Pomodoro Mastery",
                description = "Study intently for 25 minutes, then take a strict 5-minute offline break. This keeps your focus sharp and prevents burnout over long study sessions.",
                icon = "⏱️",
                badge = "TIME BOXING"
            ),
            StudyHack(
                title = "The Spaced Effect",
                description = "Revision is most effective when done just before you are about to forget. Revise sub-sections 1 day, 3 days, and 7 days after the first read.",
                icon = "🔁",
                badge = "SPACED REPETITION"
            ),
            StudyHack(
                title = "Zeigarnik Momentum",
                description = "Struggling with procrastination? Just open the syllabus and read for exactly 2 minutes. Once you start, your brain develops a natural urge to finish.",
                icon = "🚀",
                badge = "BEAT PROCRASTINATION"
            ),
            StudyHack(
                title = "Interleaved Practice",
                description = "Don't just study history all day. Mix in 30 minutes of math or reasoning in between. Switching subjects trains the brain to choose appropriate formulas/strategies.",
                icon = "🧠",
                badge = "COGNITIVE SPEED"
            ),
            StudyHack(
                title = "Error Diary Gold",
                description = "Make it a habit to log questions you got wrong in mock tests into the Digital Error Diary. Reviewing errors is 3x more effective than re-reading notes.",
                icon = "📓",
                badge = "MISTAKE DRILL"
            )
        )
    }

    var currentHackIndex by remember { mutableStateOf(0) }
    val hack = hacks[currentHackIndex]

    // Animation states
    var isFlipped by remember { mutableStateOf(false) }

    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                BrandForestGreen,
                Color(0xFF2E5B1C),
                Color(0xFF1B3313)
            )
        )
    }

    BentoCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        accentColor = BrandForestGreen,
        backgroundColor = Color.Transparent
    ) {
        // Linear gradient background with organic tone
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header: Greeting and dynamic user name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = greetingEmoji,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "$greeting, $displayUsername!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandWarmCream.copy(alpha = 0.9f)
                            )
                        }
                        Text(
                            text = "Your daily mindset catalyst is ready.",
                            fontSize = 11.sp,
                            color = BrandCreamLight.copy(alpha = 0.7f)
                        )
                    }

                    // Sparkle Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Mindset Hack",
                            tint = BrandWarmCream,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Interactive Carousel Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.22f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .clickable {
                            isFlipped = !isFlipped
                            currentHackIndex = (currentHackIndex + 1) % hacks.size
                        }
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = hack.icon, fontSize = 18.sp)
                                Text(
                                    text = hack.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandWarmCream
                                )
                            }

                            // Cognitive Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandTerracotta.copy(alpha = 0.85f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = hack.badge,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Hack Description with dynamic change transition
                        AnimatedContent(
                            targetState = hack.description,
                            transitionSpec = {
                                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) with
                                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
                            },
                            label = "hack_transition"
                        ) { descriptionText ->
                            Text(
                                text = descriptionText,
                                fontSize = 12.sp,
                                color = BrandWarmCream.copy(alpha = 0.85f),
                                lineHeight = 17.sp,
                                minLines = 3,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Hint indicator at the bottom
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tap for next cognitive hack 🔄",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandWarmCream.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Inline Ambient Sound Quick-Selector (For high-productivity study)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "🎧 Quick Study Vibe Ambience",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandWarmCream.copy(alpha = 0.85f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val ambientOptions = remember {
                            listOf(
                                AmbientSoundType.BROWN_NOISE to "🟫 Brown",
                                AmbientSoundType.PINK_RAIN to "🌧️ Rain",
                                AmbientSoundType.COZY_CAMPFIRE to "🪵 Cozy",
                                AmbientSoundType.BINAURAL_ALPHA to "🧠 Alpha"
                            )
                        }

                        for ((type, label) in ambientOptions) {
                            val isSelected = currentAmbient == type && isAmbientPlaying

                            Button(
                                onClick = {
                                    if (isSelected) {
                                        onToggleAmbient()
                                    } else {
                                        onSelectAmbient(type)
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) BrandTerracotta else Color.White.copy(alpha = 0.12f),
                                    contentColor = BrandWarmCream
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Playing",
                                            modifier = Modifier.size(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
