package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color(0xFF071B2B),
    primaryContainer = Color(0xFF143048),
    onPrimaryContainer = ElectricCyanLight,
    secondary = SoftMint,
    onSecondary = Color(0xFF042610),
    secondaryContainer = Color(0xFF11381E),
    onSecondaryContainer = SoftMint,
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF1C1338),
    tertiaryContainer = Color(0xFF2E244E),
    onTertiaryContainer = Color(0xFFDDD6FE),
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorder,
    error = AlertRed,
    onError = Color.White,
    errorContainer = Color(0xFF3B1214),
    onErrorContainer = Color(0xFFFFD7D8)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF047857),
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder,
    error = AlertRed,
    onError = Color.White
)

private val WarmCreamColorScheme = lightColorScheme(
    primary = ElectricBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF047857),
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color.White,
    background = WarmCreamBg,
    onBackground = WarmCreamTextPrimary,
    surface = WarmCreamSurface,
    onSurface = WarmCreamTextPrimary,
    surfaceVariant = WarmCreamSurfaceContainer,
    onSurfaceVariant = WarmCreamTextSecondary,
    outline = WarmCreamGlassBorder,
    error = AlertRed,
    onError = Color.White
)

/**
 * Animates all theme color properties smoothly across Light, Warm Cream, and Dark theme switches.
 */
@Composable
fun ColorScheme.animate(
    animationSpec: AnimationSpec<Color> = tween(durationMillis = 350, easing = FastOutSlowInEasing)
): ColorScheme {
    return this.copy(
        primary = animateColorAsState(primary, animationSpec, label = "theme_primary").value,
        onPrimary = animateColorAsState(onPrimary, animationSpec, label = "theme_onPrimary").value,
        primaryContainer = animateColorAsState(primaryContainer, animationSpec, label = "theme_primaryContainer").value,
        onPrimaryContainer = animateColorAsState(onPrimaryContainer, animationSpec, label = "theme_onPrimaryContainer").value,
        inversePrimary = animateColorAsState(inversePrimary, animationSpec, label = "theme_inversePrimary").value,
        secondary = animateColorAsState(secondary, animationSpec, label = "theme_secondary").value,
        onSecondary = animateColorAsState(onSecondary, animationSpec, label = "theme_onSecondary").value,
        secondaryContainer = animateColorAsState(secondaryContainer, animationSpec, label = "theme_secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(onSecondaryContainer, animationSpec, label = "theme_onSecondaryContainer").value,
        tertiary = animateColorAsState(tertiary, animationSpec, label = "theme_tertiary").value,
        onTertiary = animateColorAsState(onTertiary, animationSpec, label = "theme_onTertiary").value,
        tertiaryContainer = animateColorAsState(tertiaryContainer, animationSpec, label = "theme_tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(onTertiaryContainer, animationSpec, label = "theme_onTertiaryContainer").value,
        background = animateColorAsState(background, animationSpec, label = "theme_background").value,
        onBackground = animateColorAsState(onBackground, animationSpec, label = "theme_onBackground").value,
        surface = animateColorAsState(surface, animationSpec, label = "theme_surface").value,
        onSurface = animateColorAsState(onSurface, animationSpec, label = "theme_onSurface").value,
        surfaceVariant = animateColorAsState(surfaceVariant, animationSpec, label = "theme_surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(onSurfaceVariant, animationSpec, label = "theme_onSurfaceVariant").value,
        surfaceTint = animateColorAsState(surfaceTint, animationSpec, label = "theme_surfaceTint").value,
        inverseSurface = animateColorAsState(inverseSurface, animationSpec, label = "theme_inverseSurface").value,
        inverseOnSurface = animateColorAsState(inverseOnSurface, animationSpec, label = "theme_inverseOnSurface").value,
        error = animateColorAsState(error, animationSpec, label = "theme_error").value,
        onError = animateColorAsState(onError, animationSpec, label = "theme_onError").value,
        errorContainer = animateColorAsState(errorContainer, animationSpec, label = "theme_errorContainer").value,
        onErrorContainer = animateColorAsState(onErrorContainer, animationSpec, label = "theme_onErrorContainer").value,
        outline = animateColorAsState(outline, animationSpec, label = "theme_outline").value,
        outlineVariant = animateColorAsState(outlineVariant, animationSpec, label = "theme_outlineVariant").value,
        scrim = animateColorAsState(scrim, animationSpec, label = "theme_scrim").value
    )
}

@Composable
fun SyllabusTrackerTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK, // Default to dark command center theme
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val targetColorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.WARM_CREAM -> WarmCreamColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
    }

    // Apply smooth cross-fade animation across all theme color tokens
    val animatedColorScheme = targetColorScheme.animate()

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    SyllabusTrackerTheme(
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        content = content
    )
}

