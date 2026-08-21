package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Standard Bento Grid Card Container
 * Features modern rounded corners (20dp default), hairline border, soft ambient shadow,
 * and adaptive color scheme that strictly follows MaterialTheme.colorScheme.
 */
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 2.dp,
    borderWidth: Dp = 1.dp,
    accentColor: Color? = null,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f
    val defaultBg = colorScheme.surface
    val bg = backgroundColor ?: defaultBg

    val defaultBorder = if (isDark) DarkGlassBorder else LightGlassBorder
    val borderCol = if (accentColor != null) {
        accentColor.copy(alpha = if (isDark) 0.38f else 0.22f)
    } else {
        defaultBorder
    }

    val baseModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.5f) else BrandForestGreenDark.copy(alpha = 0.06f),
            spotColor = if (isDark) Color.Black.copy(alpha = 0.5f) else BrandForestGreenDark.copy(alpha = 0.1f)
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
 * Glass Card backwards compatibility wrapper mapped to Bento styling
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 2.dp,
    borderWidth: Dp = 1.dp,
    accentColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    BentoCard(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        borderWidth = borderWidth,
        accentColor = accentColor,
        content = content
    )
}

/**
 * Bento Gradient Card for Hero Sections
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    colors: List<Color> = listOf(BrandForestGreen, BrandForestGreenDark),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(6.dp, shape = shape, spotColor = colors.first().copy(alpha = 0.25f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = colors
                )
            )
    ) {
        content()
    }
}
