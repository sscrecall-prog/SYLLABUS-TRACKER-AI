package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.ChapterStatus
import com.example.ui.theme.*

/**
 * Premium Dark Glass Surface Card (AI Command Center Design System)
 * Features 22-26dp rounded corners, subtle translucent border, ambient depth shadow,
 * and optional cyan/mint/alert accent glow.
 */
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    accentColor: Color? = null,
    backgroundColor: Color? = null,
    glowEffect: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f

    val defaultBg = if (isDark) DarkSurface else colorScheme.surface
    val bg = backgroundColor ?: defaultBg

    val baseBorderColor = if (isDark) DarkGlassBorder else LightGlassBorder
    val borderCol = if (accentColor != null) {
        accentColor.copy(alpha = if (isDark) 0.35f else 0.28f)
    } else {
        baseBorderColor
    }

    val shadowColor = if (accentColor != null && glowEffect) {
        accentColor.copy(alpha = 0.2f)
    } else if (isDark) {
        Color.Black.copy(alpha = 0.55f)
    } else {
        Color(0x140F172A)
    }

    val baseModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .clip(shape)
        .background(bg)
        .border(borderWidth, borderCol, shape)

    val finalModifier = if (onClick != null) {
        baseModifier.clickable { onClick() }
    } else {
        baseModifier
    }

    Box(modifier = finalModifier) {
        content()
    }
}

/**
 * Premium Glass Card with subtle inner specular highlight & cyan ambient glow
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    accentColor: Color? = null,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val bg = backgroundColor ?: if (isDark) DarkGlassCard else MaterialTheme.colorScheme.surface

    BentoCard(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        borderWidth = borderWidth,
        accentColor = accentColor,
        backgroundColor = bg,
        onClick = onClick,
        content = content
    )
}

/**
 * Ambient Atmospheric Background Modifier with subtle top-right / top-left cyan glow
 */
fun Modifier.ambientCommandCenterBackground(isDark: Boolean = true): Modifier = this.drawBehind {
    if (isDark) {
        // Deep base
        drawRect(DarkBg)
        // Subtle atmospheric cyan glow at top center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ElectricBlue.copy(alpha = 0.09f),
                    ElectricBlue.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.8f, size.height * 0.05f),
                radius = size.width * 0.7f
            )
        )
        // Subtle soft mint ambient lighting in bottom-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    SoftMint.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.1f, size.height * 0.6f),
                radius = size.width * 0.5f
            )
        )
    }
}

/**
 * Futuristic Holographic Gradient Card for Hero Statistics / Active Timer
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    colors: List<Color>? = null,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val resolvedColors = colors ?: if (isDark) {
        listOf(Color(0xFF162436), Color(0xFF121B2A))
    } else {
        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    }
    val resolvedBorder = borderColor ?: if (isDark) {
        ElectricBlue.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }
    val shadowColor = if (isDark) ElectricBlue.copy(alpha = 0.25f) else Color(0x180F172A)

    Box(
        modifier = modifier
            .shadow(if (isDark) 8.dp else 4.dp, shape = shape, spotColor = shadowColor)
            .clip(shape)
            .background(Brush.linearGradient(colors = resolvedColors))
            .border(1.dp, resolvedBorder, shape)
    ) {
        content()
    }
}

/**
 * Stat Mini Card for Quick Metric Highlights
 */
@Composable
fun StatMiniCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: @Composable () -> Unit,
    accentColor: Color = ElectricBlue,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier,
        accentColor = accentColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                icon()
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
