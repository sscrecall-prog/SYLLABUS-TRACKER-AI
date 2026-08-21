package com.example.ui.components.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyPlan
import com.example.ui.components.BentoCard
import com.example.ui.theme.StatusCompleted
import com.example.ui.viewmodel.NavDestination

@Composable
fun TodayPlanCard(
    todayPlans: List<StudyPlan>,
    onTogglePlanCompleted: (StudyPlan) -> Unit,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    if (todayPlans.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📅 Today's Study Schedule",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { onNavigate(NavDestination.PLANNER) }) {
                Text("Open Planner →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        todayPlans.forEach { plan ->
            BentoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTogglePlanCompleted(plan) },
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = plan.isCompleted,
                        onCheckedChange = { onTogglePlanCompleted(plan) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = plan.chapterTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (plan.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${plan.timeStr} • ${plan.subjectName} • ${plan.plannedMinutes} mins",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (plan.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = StatusCompleted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
