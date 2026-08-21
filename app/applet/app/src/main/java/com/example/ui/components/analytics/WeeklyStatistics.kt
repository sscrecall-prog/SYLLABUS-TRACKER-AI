package com.example.ui.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudySession
import com.example.ui.components.BentoCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExamPaceStats
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeeklyStatistics(
    studySessions: List<StudySession>,
    examPaceStats: ExamPaceStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 28-day Study Consistency Heatmap Card
        BentoCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("heatmap_consistency_card"),
            shape = RoundedCornerShape(20.dp),
            accentColor = BrandTerracotta
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🗓️ 28-Day Consistency Matrix",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Visual study activity distribution across last 4 weeks",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                StudyConsistencyHeatmap(
                    studySessions = studySessions,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Heatmap Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Less", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        BrandForestGreen.copy(alpha = 0.35f),
                        BrandForestGreen.copy(alpha = 0.65f),
                        BrandForestGreen,
                        BrandTerracotta
                    ).forEach { col ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(col)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("More", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Pacing Projection Card
        BentoCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("velocity_forecast_card"),
            shape = RoundedCornerShape(20.dp),
            accentColor = if (examPaceStats.isAheadOfSchedule) StatusMastered else BrandTerracotta
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (examPaceStats.isAheadOfSchedule) StatusMastered.copy(alpha = 0.15f)
                                else BrandTerracotta.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (examPaceStats.isAheadOfSchedule) Icons.Default.EventAvailable else Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = if (examPaceStats.isAheadOfSchedule) StatusMastered else BrandTerracotta,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimated Completion Date",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (examPaceStats.isAheadOfSchedule) StatusCompleted.copy(alpha = 0.15f)
                                        else BrandTerracotta.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${examPaceStats.daysRemaining}d to ${examPaceStats.examName}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (examPaceStats.isAheadOfSchedule) StatusCompleted else BrandTerracotta
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = examPaceStats.estimatedCompletionDateStr,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (examPaceStats.isAheadOfSchedule) {
                        "🎉 Great pace! At your current velocity (${String.format("%.1f", examPaceStats.currentDailyVelocity)} ch/day), you will finish all subjects comfortably before target date."
                    } else {
                        "⚡ Target Alert: To finish 100% syllabus before ${examPaceStats.examName}, increase pace to ${String.format("%.1f", examPaceStats.requiredDailyVelocity)} chapters per day."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun StudyConsistencyHeatmap(
    studySessions: List<StudySession>,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val daysData = remember(studySessions) {
        val list = mutableListOf<Pair<String, Int>>()
        for (i in 27 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dStr = dateFormat.format(cal.time)
            val mins = studySessions.filter {
                dateFormat.format(Date(it.timestamp)) == dStr
            }.sumOf { (it.durationSeconds / 60).toInt() }
            list.add(dStr to mins)
        }
        list
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val weeks = daysData.chunked(7)
        weeks.forEach { weekDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { (dateStr, mins) ->
                    val color = when {
                        mins >= 90 -> BrandTerracotta
                        mins >= 45 -> BrandForestGreen
                        mins >= 20 -> BrandForestGreen.copy(alpha = 0.65f)
                        mins > 0 -> BrandForestGreen.copy(alpha = 0.35f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}
