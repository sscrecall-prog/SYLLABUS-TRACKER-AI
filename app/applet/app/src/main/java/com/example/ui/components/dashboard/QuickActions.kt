package com.example.ui.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.AmbientSoundType
import com.example.data.model.MockTest
import com.example.data.model.StudyPlan
import com.example.ui.components.BentoActionTile
import com.example.ui.components.BentoCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MistakeStats
import com.example.ui.viewmodel.NavDestination
import com.example.ui.viewmodel.OverallStats

@Composable
fun QuickActions(
    overallStats: OverallStats,
    todayPlans: List<StudyPlan>,
    mockTests: List<MockTest>,
    mistakeStats: MistakeStats,
    ambientSound: AmbientSoundType,
    isAmbientPlaying: Boolean,
    onToggleAmbientSound: () -> Unit,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoActionTile(
                title = "Spaced Revision",
                subtitle = "${overallStats.revisionDueChapters} chapters due",
                badgeText = if (overallStats.revisionDueChapters > 0) "DUE NOW" else null,
                icon = Icons.Default.Update,
                iconColor = StatusRevisionDue,
                modifier = Modifier
                    .weight(1f)
                    .testTag("bento_revision_tile"),
                onClick = { onNavigate(NavDestination.REVISION) }
            )
            BentoActionTile(
                title = "Daily Planner",
                subtitle = "${todayPlans.count { it.isCompleted }}/${todayPlans.size} tasks done",
                badgeText = "${todayPlans.size} TODAY",
                icon = Icons.Default.CalendarMonth,
                iconColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("bento_planner_tile"),
                onClick = { onNavigate(NavDestination.PLANNER) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoActionTile(
                title = "Pomodoro Timer",
                subtitle = "Focus & log sessions",
                badgeText = "25m / 50m",
                icon = Icons.Default.Timer,
                iconColor = BrandTerracotta,
                modifier = Modifier
                    .weight(1f)
                    .testTag("bento_timer_tile"),
                onClick = { onNavigate(NavDestination.TIMER) }
            )
            BentoActionTile(
                title = "Exam Readiness",
                subtitle = "${overallStats.masteredChapters} mastered",
                badgeText = "ANALYTICS",
                icon = Icons.Default.Analytics,
                iconColor = SubjectEnglish,
                modifier = Modifier
                    .weight(1f)
                    .testTag("bento_analytics_tile"),
                onClick = { onNavigate(NavDestination.ANALYTICS) }
            )
        }

        // Mock Test & Percentile Hub Tile
        BentoCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(NavDestination.MOCK_TESTS) }
                .testTag("dashboard_mock_test_hub_card"),
            shape = RoundedCornerShape(20.dp),
            accentColor = BrandForestGreen
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BrandForestGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = BrandForestGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mock Test & Percentiles",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrandTerracotta.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${mockTests.size} MOCKS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTerracotta
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val latest = mockTests.firstOrNull()
                        val subText = if (latest != null) {
                            "Latest: ${latest.marksScored}/${latest.totalMarks.toInt()} • ${String.format("%.1f", latest.percentile)}%ile"
                        } else {
                            "Track Testbook, Oliveboard scores & rank"
                        }
                        Text(
                            text = subText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandForestGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Track →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandForestGreen
                    )
                }
            }
        }

        // Digital Error Diary Tile
        BentoCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(NavDestination.MISTAKES) }
                .testTag("dashboard_mistake_notebook_card"),
            shape = RoundedCornerShape(20.dp),
            accentColor = if (mistakeStats.reviewDueCount > 0) StatusWeak else StatusInProgress
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (mistakeStats.reviewDueCount > 0) StatusWeak.copy(alpha = 0.15f)
                                else StatusInProgress.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkRemove,
                            contentDescription = null,
                            tint = if (mistakeStats.reviewDueCount > 0) StatusWeak else StatusInProgress,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Digital Error Diary",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (mistakeStats.reviewDueCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StatusWeak.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${mistakeStats.reviewDueCount} REVISE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusWeak
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val subText = if (mistakeStats.reviewDueCount > 0) {
                            "${mistakeStats.reviewDueCount} mistake reviews pending today"
                        } else {
                            "${mistakeStats.totalMistakesCount} errors logged • ${mistakeStats.resolutionRatePercent}% resolved"
                        }
                        Text(
                            text = subText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (mistakeStats.reviewDueCount > 0) StatusWeak.copy(alpha = 0.12f)
                            else StatusInProgress.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Drill →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mistakeStats.reviewDueCount > 0) StatusWeak else StatusInProgress
                    )
                }
            }
        }

        // Quick Ambient Focus Audio / White Noise Bar
        BentoCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(NavDestination.TIMER) }
                .testTag("dashboard_ambient_audio_quick_tile"),
            shape = RoundedCornerShape(20.dp),
            accentColor = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE) Color(0xFFAB47BC) else BrandTerracotta
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE)
                                    Color(0xFFAB47BC).copy(alpha = 0.15f)
                                else BrandTerracotta.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (ambientSound != AmbientSoundType.NONE) ambientSound.emoji else "🎧",
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Ambient White Noise",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE)
                                            Color(0xFFAB47BC).copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE) "PLAYING 🎵" else "OFFLINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE) Color(0xFFAB47BC) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (ambientSound != AmbientSoundType.NONE) "${ambientSound.title} • Tap to tune" else "Rain, Deep Brown Noise, Binaural Beats & Waves",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                FilledIconButton(
                    onClick = onToggleAmbientSound,
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE)
                            Color(0xFFAB47BC)
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE)
                            Icons.Default.VolumeUp
                        else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Ambient Sound",
                        tint = if (isAmbientPlaying && ambientSound != AmbientSoundType.NONE)
                            Color.White
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
