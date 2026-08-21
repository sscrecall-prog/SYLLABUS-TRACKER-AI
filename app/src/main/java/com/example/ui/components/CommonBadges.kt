package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: ChapterStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val bg = status.getColor().copy(alpha = 0.15f)
    val textCol = status.getColor()
    val mod = modifier
        .clip(RoundedCornerShape(8.dp))
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .background(bg)
        .border(1.dp, status.getColor().copy(alpha = 0.35f), RoundedCornerShape(8.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp)

    Row(
        modifier = mod,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = status.iconEmoji, fontSize = 11.sp)
        Text(
            text = status.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textCol
        )
    }
}

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val color = when (priority) {
        Priority.LOW -> Color(0xFF4CAF50)
        Priority.MEDIUM -> Color(0xFFFF9800)
        Priority.HIGH -> Color(0xFFFF5722)
        Priority.URGENT -> Color(0xFFD32F2F)
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.8.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = priority.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun DifficultyBadge(difficulty: Difficulty, modifier: Modifier = Modifier) {
    val color = when (difficulty) {
        Difficulty.EASY -> Color(0xFF43A047)
        Difficulty.MEDIUM -> Color(0xFFFB8C00)
        Difficulty.HARD -> Color(0xFFE53935)
    }
    Text(
        text = difficulty.label,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.8.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = color
    )
}

@Composable
fun ConfidenceStars(
    confidence: Int,
    modifier: Modifier = Modifier,
    onRatingChanged: ((Int) -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 1..5) {
            val filled = i <= confidence
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star $i",
                tint = if (filled) Color(0xFFFFB300) else Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(16.dp)
                    .then(if (onRatingChanged != null) Modifier.clickable { onRatingChanged(i) } else Modifier)
            )
        }
    }
}

@Composable
fun TagChip(
    tag: String,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (tag.startsWith("#")) tag else "#$tag",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
