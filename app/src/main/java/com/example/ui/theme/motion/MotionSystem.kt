package com.example.ui.theme.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Centralized Motion Tokens adhering to the calm, tactile, and responsive motion principles.
 * FAST (150-200ms) for micro-interactions & feedback
 * NORMAL (250-350ms) for lists, modals, cards
 * SMOOTH (350-450ms) for bottom sheets, navigation pill morphing
 * EMPHASIS (450-600ms) for milestone celebrations and progress fills
 */
object MotionTokens {
    const val DURATION_FAST = 180
    const val DURATION_NORMAL = 300
    const val DURATION_SMOOTH = 400
    const val DURATION_EMPHASIS = 550

    val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val DecelerateEasing = FastOutSlowInEasing
    val AccelerateEasing = FastOutLinearInEasing

    val SmoothSpringSpec = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 380f
    )

    val SnappySpringSpec = spring<Float>(
        dampingRatio = 0.90f,
        stiffness = 550f
    )

    val GentleSpringSpec = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = 300f
    )
}

/**
 * CompositionLocal for app-wide reduced motion preference.
 */
val LocalReducedMotion = compositionLocalOf { false }

/**
 * Tactical micro-interaction on press: scales gently (1.0 -> 0.97) and springs back cleanly.
 */
fun Modifier.motionPress(
    enabled: Boolean = true,
    scaleDownTarget: Float = 0.97f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val isReduced = LocalReducedMotion.current
    if (isReduced || !enabled) {
        if (onClick != null) {
            return@composed this.clickable(enabled = enabled, onClick = onClick)
        }
        return@composed this
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDownTarget else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.80f,
            stiffness = 500f
        ),
        label = "MotionPressScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = MotionTokens.DURATION_FAST),
        label = "MotionPressAlpha"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
            } else Modifier
        )
}

/**
 * Staggered Card Entrance Animation:
 * Smooth entry with slight vertical glide (translateY 8dp -> 0) and opacity (0 -> 1).
 */
fun Modifier.motionCardEntry(
    index: Int = 0,
    maxDelayMs: Int = 180,
    stepDelayMs: Int = 30
): Modifier = composed {
    val isReduced = LocalReducedMotion.current
    if (isReduced) return@composed this

    val delay = (index * stepDelayMs).coerceAtMost(maxDelayMs)
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = MotionTokens.DURATION_NORMAL,
            delayMillis = delay,
            easing = MotionTokens.DecelerateEasing
        ),
        label = "CardEntryAlpha"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 16f,
        animationSpec = tween(
            durationMillis = MotionTokens.DURATION_NORMAL,
            delayMillis = delay,
            easing = MotionTokens.DecelerateEasing
        ),
        label = "CardEntryOffsetY"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.97f,
        animationSpec = tween(
            durationMillis = MotionTokens.DURATION_NORMAL,
            delayMillis = delay,
            easing = MotionTokens.DecelerateEasing
        ),
        label = "CardEntryScale"
    )

    this.graphicsLayer {
        alpha = animatedAlpha
        translationY = animatedOffsetY
        scaleX = animatedScale
        scaleY = animatedScale
    }
}

/**
 * Calm Skeleton Loading Shimmer Modifier.
 * Provides a gentle, non-aggressive pulse to convey loading state.
 */
fun Modifier.motionSkeleton(
    isLoading: Boolean = true,
    shimmerColor: Color = Color.White.copy(alpha = 0.18f)
): Modifier = composed {
    if (!isLoading) return@composed this

    val isReduced = LocalReducedMotion.current
    if (isReduced) {
        return@composed this.alpha(0.6f)
    }

    val transition = rememberInfiniteTransition(label = "SkeletonShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SkeletonShimmerTranslate"
    )

    this.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                shimmerColor,
                Color.Transparent
            ),
            start = Offset(translateAnim - 300f, translateAnim - 300f),
            end = Offset(translateAnim, translateAnim)
        )
        drawRect(brush = brush)
    }
}

/**
 * Standard Page Transition Specification.
 * Smooth cross-fade with 8dp upward entry (opacity 0 -> 1, y 8 -> 0) and -6dp upward exit.
 */
object PageMotionTransitions {
    fun standardTransitionSpec(
        isReducedMotion: Boolean = false,
        initialOffsetYPx: Int = 22,
        targetOffsetYPx: Int = -16
    ): ContentTransform {
        return if (isReducedMotion) {
            fadeIn(animationSpec = tween(MotionTokens.DURATION_FAST, easing = LinearEasing)) togetherWith 
                    fadeOut(animationSpec = tween(MotionTokens.DURATION_FAST, easing = LinearEasing))
        } else {
            val enterSpec = tween<Float>(
                durationMillis = MotionTokens.DURATION_NORMAL,
                easing = MotionTokens.StandardEasing
            )
            val exitSpec = tween<Float>(
                durationMillis = MotionTokens.DURATION_FAST,
                easing = MotionTokens.StandardEasing
            )

            (fadeIn(animationSpec = enterSpec) + slideInVertically(
                animationSpec = tween(durationMillis = MotionTokens.DURATION_NORMAL, easing = MotionTokens.StandardEasing),
                initialOffsetY = { initialOffsetYPx }
            )).togetherWith(
                fadeOut(animationSpec = exitSpec) + slideOutVertically(
                    animationSpec = tween(durationMillis = MotionTokens.DURATION_FAST, easing = MotionTokens.StandardEasing),
                    targetOffsetY = { targetOffsetYPx }
                )
            )
        }
    }
}

/**
 * Standardized Composable wrapper that applies consistent page motion transitions
 * (opacity 0 -> 1, vertical translation y 8.dp -> 0) to major screen content changes using Compose's AnimatedContent.
 *
 * @param targetState The current state / destination representing the active screen.
 * @param modifier Modifier applied to the AnimatedContent container.
 * @param contentAlignment Content alignment inside the animated container.
 * @param label Descriptive label for animation inspections and tooling.
 * @param content Screen content composable builder with AnimatedVisibilityScope.
 */
@Composable
fun <T> StandardPageTransitionWrapper(
    targetState: T,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "StandardPageTransition",
    content: @Composable AnimatedVisibilityScope.(T) -> Unit
) {
    val isReducedMotion = LocalReducedMotion.current
    val density = LocalDensity.current
    val initialOffsetY8dp = remember(density) { with(density) { 8.dp.roundToPx() } }
    val targetOffsetY6dp = remember(density) { with(density) { (-6).dp.roundToPx() } }

    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        contentAlignment = contentAlignment,
        transitionSpec = {
            PageMotionTransitions.standardTransitionSpec(
                isReducedMotion = isReducedMotion,
                initialOffsetYPx = initialOffsetY8dp,
                targetOffsetYPx = targetOffsetY6dp
            )
        },
        label = label,
        content = content
    )
}

