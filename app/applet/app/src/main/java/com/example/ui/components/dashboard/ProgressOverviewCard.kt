package com.example.ui.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientCard
import com.example.ui.components.ProgressRing
import com.example.ui.theme.*
import com.example.ui.viewmodel.OverallStats

@Composable
fun ProgressOverviewCard(
    overallStats: OverallStats,
    modifier: Modifier = Modifier
) {
    GradientCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_hero_bento"),
        shape = RoundedCornerShape(24.dp),
        colors = listOf(BrandForestGreen, Color(0xFF1B3313))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandTerracotta)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "BENTO SYLLABUS HUB",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🔥 ${overallStats.currentStreakDays}d Streak",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandWarmCream
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Syllabus Mastered",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandWarmCream
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${overallStats.completedChapters} of ${overallStats.totalChapters} chapters ready for exam",
                        fontSize = 13.sp,
                        color = BrandCreamDark
                    )
                }

                // Circular Dual Progress Ring
                ProgressRing(
                    progress = overallStats.completionPercentage / 100f,
                    size = 88.dp,
                    strokeWidth = 8.dp,
                    primaryColor = BrandWarmCream,
                    secondaryColor = BrandTerracotta,
                    backgroundColor = Color.White.copy(alpha = 0.18f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${overallStats.completionPercentage}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandWarmCream
                        )
                        Text(
                            text = "READY",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandCreamLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bento Micro-Metrics Bar inside Hero
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.28f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Active", fontSize = 10.sp, color = BrandCreamDark)
                    Text("${overallStats.inProgressChapters}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusInProgress)
                }
                VerticalDivider(modifier = Modifier.height(20.dp), color = Color.White.copy(alpha = 0.15f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Revision Due", fontSize = 10.sp, color = BrandCreamDark)
                    Text("${overallStats.revisionDueChapters}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusRevisionDue)
                }
                VerticalDivider(modifier = Modifier.height(20.dp), color = Color.White.copy(alpha = 0.15f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Weak Chapters", fontSize = 10.sp, color = BrandCreamDark)
                    Text("${overallStats.weakChapters}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusWeak)
                }
                VerticalDivider(modifier = Modifier.height(20.dp), color = Color.White.copy(alpha = 0.15f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Today's Study", fontSize = 10.sp, color = BrandCreamDark)
                    Text("${overallStats.todayStudyMinutes} mins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandWarmCream)
                }
            }
        }
    }
}
