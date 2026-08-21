package com.example.ui.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Score
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BentoCard
import com.example.ui.components.StatMiniCard
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.BrandTerracotta
import com.example.ui.theme.StatusCompleted
import com.example.ui.viewmodel.MockStats
import com.example.ui.viewmodel.NavDestination

@Composable
fun AccuracyCard(
    mockStats: MockStats,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigate(NavDestination.MOCK_TESTS) }
            .testTag("analytics_mock_tests_bento"),
        shape = RoundedCornerShape(20.dp),
        accentColor = BrandForestGreen
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = BrandForestGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mock Test Performance & Ranks",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandForestGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Full Tracker →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandForestGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMiniCard(
                    title = "Avg Score",
                    value = String.format("%.1f", mockStats.averageScore),
                    subtitle = "Top: ${String.format("%.1f", mockStats.highestScore)}",
                    icon = Icons.Default.Score,
                    iconTint = BrandForestGreen,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Percentile",
                    value = "${String.format("%.1f", mockStats.averagePercentile)}%",
                    subtitle = "Best: ${String.format("%.1f", mockStats.bestPercentile)}%",
                    icon = Icons.Default.AutoGraph,
                    iconTint = BrandTerracotta,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Cutoff Rate",
                    value = "${mockStats.cutoffClearanceRate}%",
                    subtitle = "${mockStats.clearedCutoffCount}/${mockStats.totalMocksCount} Passed",
                    icon = Icons.Default.Verified,
                    iconTint = StatusCompleted,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
