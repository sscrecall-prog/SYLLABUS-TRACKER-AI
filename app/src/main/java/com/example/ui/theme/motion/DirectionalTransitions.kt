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

    fun transitionSpec(
        direction: TransitionDirection = TransitionDirection.NEUTRAL,
        isReducedMotion: Boolean = false
    ): ContentTransform {
        if (isReducedMotion) {
            return fadeIn(animationSpec = tween(MotionTokens.DURATION_FAST, easing = LinearEasing)) togetherWith
                    fadeOut(animationSpec = tween(MotionTokens.DURATION_FAST, easing = LinearEasing))
        }

        val enterDuration = MotionTokens.DURATION_NORMAL
        val exitDuration = MotionTokens.DURATION_FAST

        return when (direction) {
            TransitionDirection.FORWARD -> {
                // Moving deeper into navigation / opening a screen (Slide leftwards)
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() },
                    animationSpec = tween(durationMillis = enterDuration, easing = MotionTokens.DecelerateEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = enterDuration, easing = MotionTokens.DecelerateEasing)
                )).togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> (-fullWidth * 0.15f).toInt() },
                        animationSpec = tween(durationMillis = exitDuration, easing = MotionTokens.StandardEasing)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = exitDuration, easing = MotionTokens.StandardEasing)
                    ) + scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(durationMillis = exitDuration, easing = MotionTokens.StandardEasing)
                    )
                )
            }

            TransitionDirection.BACKWARD -> {
                // Moving back / popping screen (Slide rightwards from depth)
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> (-fullWidth * 0.20f).toInt() },
                    animationSpec = tween(durationMillis = enterDuration, easing = MotionTokens.DecelerateEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = enterDuration, easing = MotionTokens.DecelerateEasing)
                ) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = tween(durationMillis = enterDuration, easing = MotionTokens.DecelerateEasing)
                )).togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                        animationSpec = tween(durationMillis = exitDuration, easing = MotionTokens.StandardEasing)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = exitDuration, easing = MotionTokens.StandardEasing)
                    )
                )
            }

            TransitionDirection.NEUTRAL -> {
                // Neutral tab cross-fade with subtle vertical settle
                (fadeIn(
                    animationSpec = tween(durationMillis = enterDuration, easing = MotionTokens.StandardEasing)
                ) + slideInVertically(
                    initialOffsetY = { (it * 0.05f).toInt() },
                    animationSpec = tween(durationMillis = enterDuration, easing = MotionTokens.StandardEasing)
                )).togetherWith(
                    fadeOut(
                        animationSpec = tween(durationMillis = exitDuration, easing = MotionTokens.StandardEasing)
                    ) + slideOutVertically(
                        targetOffsetY = { (-it * 0.03f).toInt() },
                        animationSpec = tween(durationMillis = exitDuration, easing = MotionTokens.StandardEasing)
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
