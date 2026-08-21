package com.example.ui.components.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientCard
import com.example.ui.components.StatMiniCard
import com.example.ui.theme.BrandCreamDark
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.BrandTerracotta
import com.example.ui.theme.BrandWarmCream
import com.example.ui.theme.StatusCompleted
import com.example.ui.viewmodel.OverallStats

@Composable
fun StreakCard(
    overallStats: OverallStats,
    modifier: Modifier = Modifier
) {
    GradientCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("analytics_hero_card"),
        shape = RoundedCornerShape(22.dp),
        colors = listOf(BrandForestGreen, Color(0xFF162D10))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📈 Performance & Velocity",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandWarmCream
                    )
                    Text(
                        text = "Deep data insights, trends & syllabus completion trajectory",
                        fontSize = 12.sp,
                        color = BrandCreamDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMiniCard(
                    title = "Coverage",
                    value = "${overallStats.completionPercentage}%",
                    subtitle = "${overallStats.completedChapters}/${overallStats.totalChapters} Chapters",
                    icon = Icons.Default.CheckCircle,
                    iconTint = StatusCompleted,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Velocity",
                    value = "${String.format("%.1f", (overallStats.completedChapters.toFloat() / 7).coerceAtLeast(1.2f))}",
                    subtitle = "Chapters / week",
                    icon = Icons.Default.Speed,
                    iconTint = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Study Time",
                    value = "${overallStats.totalStudyMinutes / 60}h ${overallStats.totalStudyMinutes % 60}m",
                    subtitle = "Streak: ${overallStats.currentStreakDays}d 🔥",
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = BrandTerracotta,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
