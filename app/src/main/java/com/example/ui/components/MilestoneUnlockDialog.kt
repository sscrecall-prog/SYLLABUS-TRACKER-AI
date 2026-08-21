package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AchievementBadge
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.BrandTerracotta
import com.example.ui.theme.BrandWarmCream
import com.example.ui.theme.motion.MotionTokens
import com.example.ui.theme.motion.motionPress

@Composable
fun MilestoneUnlockDialog(
    badge: AchievementBadge,
    onDismiss: () -> Unit,
    onViewAllBadges: () -> Unit
) {
    val tierColor = try {
        Color(android.graphics.Color.parseColor(badge.tier.colorHex))
    } catch (e: Exception) {
        BrandForestGreen
    }

    var isAnimatedIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isAnimatedIn = true
    }

    val popScale by animateFloatAsState(
        targetValue = if (isAnimatedIn) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 380f
        ),
        label = "BadgeUnlockPopScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(popScale)
                .testTag("milestone_unlock_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(tierColor.copy(alpha = 0.9f), tierColor.copy(alpha = 0.2f))
                ),
                width = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(tierColor.copy(alpha = 0.25f), BrandTerracotta.copy(alpha = 0.25f))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✨ ACHIEVEMENT UNLOCKED! ✨",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Avatar with glowing halo
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(tierColor.copy(alpha = 0.35f), tierColor.copy(alpha = 0.05f))
                            )
                        )
                        .border(3.dp, tierColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.iconEmoji,
                        fontSize = 48.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = badge.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tier Tag & Reward XP
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tierColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${badge.tier.label.uppercase()} TIER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tierColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandTerracotta.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "+${badge.rewardXp} XP EARNED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTerracotta
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = badge.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .motionPress(onClick = onDismiss)
                            .testTag("dismiss_unlock_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Awesome")
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onViewAllBadges()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .motionPress(onClick = {
                                onDismiss()
                                onViewAllBadges()
                            })
                            .testTag("view_all_badges_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("View Badges", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
