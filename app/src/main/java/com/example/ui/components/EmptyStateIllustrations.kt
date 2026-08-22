package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Minimalist, high-fidelity Vector & Canvas Illustration for Dashboard Empty State
 */
@Composable
fun MinimalStudyEmptyStateIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "studyIllustrationMotion")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "studyFloat"
    )
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "studyPulse"
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f + floatOffset)

            // 1. Ambient Background Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = pulseGlow * 0.35f),
                        SoftMint.copy(alpha = pulseGlow * 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = center
            )

            // 2. Subtle Outer Target Orbit Ring (Dashed)
            val orbitRadius = w * 0.38f
            drawCircle(
                color = if (isDark) ElectricBlue.copy(alpha = 0.22f) else ElectricBlueDark.copy(alpha = 0.25f),
                radius = orbitRadius,
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )
            )

            // Small orbit nodes
            val nodeAngle = 45f * (Math.PI / 180f).toFloat()
            val nodeX = center.x + orbitRadius * cos(nodeAngle)
            val nodeY = center.y + orbitRadius * sin(nodeAngle)
            drawCircle(
                color = SoftMint,
                radius = 4.dp.toPx(),
                center = Offset(nodeX, nodeY)
            )

            val nodeAngle2 = 220f * (Math.PI / 180f).toFloat()
            val node2X = center.x + orbitRadius * cos(nodeAngle2)
            val node2Y = center.y + orbitRadius * sin(nodeAngle2)
            drawCircle(
                color = ElectricBlue,
                radius = 3.5.dp.toPx(),
                center = Offset(node2X, node2Y)
            )

            // 3. Base Shadow / Plinth
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        (if (isDark) Color.Black else Color(0xFFCBD5E1)).copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(w / 2f, h * 0.82f),
                    radius = w * 0.35f
                ),
                topLeft = Offset(w * 0.18f, h * 0.76f),
                size = Size(w * 0.64f, h * 0.12f)
            )

            // 4. Stylized Minimal Book / Binder
            val bookWidth = w * 0.52f
            val bookHeight = h * 0.36f
            val bookLeft = center.x - bookWidth / 2f
            val bookTop = center.y - bookHeight / 2f + 4f

            // Book Back Cover Accent
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        if (isDark) DarkSurfaceElevated else Color(0xFFE2E8F0),
                        if (isDark) DarkSurface else Color(0xFFCBD5E1)
                    )
                ),
                topLeft = Offset(bookLeft + 6f, bookTop + 8f),
                size = Size(bookWidth, bookHeight),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Fill
            )

            // Book Main Spine / Cover
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        if (isDark) Color(0xFF1E2A3A) else Color(0xFFF8FAFC),
                        if (isDark) Color(0xFF15202E) else Color(0xFFEEF2F6)
                    ),
                    start = Offset(bookLeft, bookTop),
                    end = Offset(bookLeft + bookWidth, bookTop + bookHeight)
                ),
                topLeft = Offset(bookLeft, bookTop),
                size = Size(bookWidth, bookHeight),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Fill
            )

            // Book Outline Border
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.7f),
                        SoftMint.copy(alpha = 0.5f)
                    )
                ),
                topLeft = Offset(bookLeft, bookTop),
                size = Size(bookWidth, bookHeight),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Stylized Horizontal Content Lines
            val lineMarginX = bookWidth * 0.18f
            val lineStartX = bookLeft + lineMarginX
            val lineEndX = bookLeft + bookWidth - lineMarginX

            // Line 1 (Accent Header)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(ElectricBlue, SoftMint),
                    startX = lineStartX,
                    endX = lineStartX + bookWidth * 0.38f
                ),
                start = Offset(lineStartX, bookTop + bookHeight * 0.30f),
                end = Offset(lineStartX + bookWidth * 0.38f, bookTop + bookHeight * 0.30f),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Line 2 (Body)
            drawLine(
                color = if (isDark) Color(0xFF334155) else Color(0xFF94A3B8),
                start = Offset(lineStartX, bookTop + bookHeight * 0.50f),
                end = Offset(lineEndX, bookTop + bookHeight * 0.50f),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Line 3 (Body)
            drawLine(
                color = if (isDark) Color(0xFF334155).copy(alpha = 0.7f) else Color(0xFF94A3B8).copy(alpha = 0.7f),
                start = Offset(lineStartX, bookTop + bookHeight * 0.68f),
                end = Offset(lineStartX + bookWidth * 0.45f, bookTop + bookHeight * 0.68f),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Floating Sparkle / Star (Top Right)
            val starCenter = Offset(center.x + bookWidth * 0.46f, center.y - bookHeight * 0.50f)
            drawSparkle(starCenter, size = 12.dp.toPx(), color = SoftMint)

            // Floating Sparkle 2 (Bottom Left)
            val starCenter2 = Offset(center.x - bookWidth * 0.44f, center.y + bookHeight * 0.40f)
            drawSparkle(starCenter2, size = 8.dp.toPx(), color = ElectricBlue)
        }
    }
}

/**
 * Minimalist, high-fidelity Vector & Canvas Illustration for Analytics Empty State
 */
@Composable
fun MinimalAnalyticsEmptyStateIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "analyticsIllustrationMotion")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "analyticsWave"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "analyticsGlow"
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)

            // 1. Ambient Circular Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SoftMint.copy(alpha = glowPulse * 0.35f),
                        ElectricBlue.copy(alpha = glowPulse * 0.20f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = center
            )

            // 2. Chart Grid Background Frame
            val gridLeft = w * 0.16f
            val gridTop = h * 0.20f
            val gridRight = w * 0.84f
            val gridBottom = h * 0.78f

            // Dashed Grid Lines (Horizontal)
            for (i in 1..3) {
                val lineY = gridTop + (gridBottom - gridTop) * (i / 4f)
                drawLine(
                    color = if (isDark) Color(0x2A64748B) else Color(0x3394A3B8),
                    start = Offset(gridLeft, lineY),
                    end = Offset(gridRight, lineY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            // 3. Futuristic Minimalist Bar Columns (Translucent gradient)
            val barCount = 4
            val barSpacing = (gridRight - gridLeft) / barCount
            val barWidth = 14.dp.toPx()
            val barHeights = listOf(0.35f, 0.65f, 0.45f, 0.85f)

            for (i in 0 until barCount) {
                val barX = gridLeft + (i + 0.5f) * barSpacing - barWidth / 2f
                val barH = (gridBottom - gridTop) * barHeights[i]
                val barY = gridBottom - barH

                // Column Gradient
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.85f),
                            ElectricBlue.copy(alpha = 0.15f)
                        ),
                        startY = barY,
                        endY = gridBottom
                    ),
                    topLeft = Offset(barX, barY),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }

            // 4. Smooth Spline / Trend Curve overlay with Glowing Nodes
            val p1 = Offset(gridLeft + 0.5f * barSpacing, gridBottom - (gridBottom - gridTop) * 0.35f)
            val p2 = Offset(gridLeft + 1.5f * barSpacing, gridBottom - (gridBottom - gridTop) * 0.65f)
            val p3 = Offset(gridLeft + 2.5f * barSpacing, gridBottom - (gridBottom - gridTop) * 0.45f)
            val p4 = Offset(gridLeft + 3.5f * barSpacing, gridBottom - (gridBottom - gridTop) * 0.85f)

            val curvePath = Path().apply {
                moveTo(p1.x, p1.y)
                // Cubic Bezier interpolation
                cubicTo(
                    p1.x + (p2.x - p1.x) / 2f, p1.y,
                    p1.x + (p2.x - p1.x) / 2f, p2.y,
                    p2.x, p2.y
                )
                cubicTo(
                    p2.x + (p3.x - p2.x) / 2f, p2.y,
                    p2.x + (p3.x - p2.x) / 2f, p3.y,
                    p3.x, p3.y
                )
                cubicTo(
                    p3.x + (p4.x - p3.x) / 2f, p3.y,
                    p3.x + (p4.x - p3.x) / 2f, p4.y,
                    p4.x, p4.y
                )
            }

            // Glow under the curve
            drawPath(
                path = curvePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(ElectricBlue, SoftMint),
                    startX = gridLeft,
                    endX = gridRight
                ),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Interactive Node Dots on Curve
            listOf(p1, p2, p3, p4).forEachIndexed { index, pt ->
                // Outer ring
                drawCircle(
                    color = if (isDark) DarkSurface else Color.White,
                    radius = 5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = if (index % 2 == 0) ElectricBlue else SoftMint,
                    radius = 3.5.dp.toPx(),
                    center = pt
                )
            }

            // Floating Sparkles
            drawSparkle(Offset(w * 0.85f, h * 0.18f), size = 11.dp.toPx(), color = SoftMint)
            drawSparkle(Offset(w * 0.15f, h * 0.32f), size = 8.dp.toPx(), color = ElectricBlue)
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, size: Float, color: Color) {
    val half = size / 2f
    val path = Path().apply {
        moveTo(center.x, center.y - half)
        quadraticBezierTo(center.x, center.y, center.x + half, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y + half)
        quadraticBezierTo(center.x, center.y, center.x - half, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y - half)
        close()
    }
    drawPath(path = path, color = color)
}

/**
 * Polished Empty State Card for Dashboard
 */
@Composable
fun DashboardEmptyStateCard(
    onLoadSampleSyllabus: () -> Unit,
    onAddSubject: () -> Unit,
    onSetExamTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val colorScheme = MaterialTheme.colorScheme

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_empty_state_card"),
        shape = RoundedCornerShape(24.dp),
        accentColor = ElectricBlue
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimal Illustration
            MinimalStudyEmptyStateIllustration(
                size = 140.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Command Center Ready",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.onSurface,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "No subjects or chapters found. Load standard competitive exam syllabus or create your custom study plan to begin tracking.",
                fontSize = 12.5.sp,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onLoadSampleSyllabus,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("empty_state_load_sample_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = Color(0xFF071B2B)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Load SSC CGL 2026 Syllabus",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddSubject,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("empty_state_add_subject_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDark) DarkGlassBorder else colorScheme.outlineVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Subject", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onSetExamTarget,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("empty_state_set_target_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDark) DarkGlassBorder else colorScheme.outlineVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Set Target", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Polished Empty State Card for Analytics
 */
@Composable
fun AnalyticsEmptyStateCard(
    onStartTimer: () -> Unit,
    onLogMockTest: () -> Unit,
    onBrowseSyllabus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val colorScheme = MaterialTheme.colorScheme

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("analytics_empty_state_card"),
        shape = RoundedCornerShape(24.dp),
        accentColor = SoftMint
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimal Illustration
            MinimalAnalyticsEmptyStateIllustration(
                size = 140.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Intelligence Awaiting First Session",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.onSurface,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Log study timer sessions or record mock tests to generate syllabus velocity curves, retention forecasts, and subject vulnerability heatmaps.",
                fontSize = 12.5.sp,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartTimer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("analytics_empty_start_timer_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftMintDark,
                        contentColor = Color(0xFF062310)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Focus Timer Session",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onLogMockTest,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("analytics_empty_mock_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDark) DarkGlassBorder else colorScheme.outlineVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Log Mock Test", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onBrowseSyllabus,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("analytics_empty_syllabus_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDark) DarkGlassBorder else colorScheme.outlineVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "View Syllabus", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
