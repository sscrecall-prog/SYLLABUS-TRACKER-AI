package com.example.ui.theme.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Slide Direction for Predictive Back & Hierarchical Forward/Backward Transitions.
 */
enum class TransitionDirection {
    FORWARD,
    BACKWARD,
    NEUTRAL
}

/**
 * Enhanced Screen Transition Specification that supports:
 * 1. Standard Forward Push: Smooth Slide-In from Right/Bottom + Fade-in
 * 2. Back / Pop Transition: Smooth Slide-In from Left/Top + Fade-in with slight scale depth
 * 3. Reduced Motion: Pure cross-fade
 */
object DirectionalPageTransitions {
    const val TRANSITION_DURATION_MS = 300

    fun transitionSpec(
        direction: TransitionDirection = TransitionDirection.NEUTRAL,
        isReducedMotion: Boolean = false
    ): ContentTransform {
        val duration = TRANSITION_DURATION_MS // 300ms transition duration
        val smoothEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f) // Smooth deceleration

        if (isReducedMotion) {
            return fadeIn(animationSpec = tween(duration, easing = LinearEasing)) togetherWith
                    fadeOut(animationSpec = tween(duration, easing = LinearEasing))
        }

        return when (direction) {
            TransitionDirection.FORWARD -> {
                // Moving forward/deeper: Slide in horizontally from right + Fade in with 300ms duration
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth * 0.18f).toInt() },
                    animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                )).togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> (-fullWidth * 0.12f).toInt() },
                        animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                    )
                )
            }

            TransitionDirection.BACKWARD -> {
                // Moving backward/popping: Slide in horizontally from left + Fade in with 300ms duration
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> (-fullWidth * 0.18f).toInt() },
                    animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                )).togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> (fullWidth * 0.18f).toInt() },
                        animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                    )
                )
            }

            TransitionDirection.NEUTRAL -> {
                // Neutral tab change / cross-screen transition: Slide & Fade with 300ms duration
                (slideInVertically(
                    initialOffsetY = { (it * 0.05f).toInt() },
                    animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                )).togetherWith(
                    slideOutVertically(
                        targetOffsetY = { (-it * 0.03f).toInt() },
                        animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = duration, easing = smoothEasing)
                    )
                )
            }
        }
    }
}

/**
 * Direction-Aware Page AnimatedContent Wrapper supporting Back Transitions.
 */
@Composable
fun <T> DirectionalPageTransitionWrapper(
    targetState: T,
    direction: TransitionDirection,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "DirectionalPageTransition",
    content: @Composable AnimatedVisibilityScope.(T) -> Unit
) {
    val isReducedMotion = LocalReducedMotion.current

    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        contentAlignment = contentAlignment,
        transitionSpec = {
            DirectionalPageTransitions.transitionSpec(
                direction = direction,
                isReducedMotion = isReducedMotion
            )
        },
        label = label,
        content = content
    )
}
