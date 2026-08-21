package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA5D6A7),
    onPrimary = Color(0xFF0F260C),
    primaryContainer = Color(0xFF23441B),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = BrandTerracottaLight,
    onSecondary = Color(0xFF3E170C),
    secondaryContainer = Color(0xFF5D2817),
    onSecondaryContainer = BrandCreamLight,
    tertiary = Color(0xFFFFD580),
    onTertiary = Color(0xFF332B1A),
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF222E22),
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BrandForestGreen,
    onPrimary = BrandWarmCream,
    primaryContainer = Color(0xFFD6E8D0),
    onPrimaryContainer = BrandForestGreenDark,
    secondary = BrandTerracotta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF3B0900),
    tertiary = Color(0xFF6E5E3D),
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFEDE8DD),
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder
)

private val WarmCreamColorScheme = lightColorScheme(
    primary = BrandForestGreen,
    onPrimary = BrandCreamLight,
    primaryContainer = Color(0xFFE2D1B8),
    onPrimaryContainer = BrandForestGreenDark,
    secondary = BrandTerracotta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2D3C8),
    onSecondaryContainer = Color(0xFF3B0900),
    tertiary = Color(0xFF8A6233),
    onTertiary = Color.White,
    background = WarmCreamBg,
    onBackground = WarmCreamTextPrimary,
    surface = WarmCreamSurface,
    onSurface = WarmCreamTextPrimary,
    surfaceVariant = WarmCreamSurfaceContainer,
    onSurfaceVariant = WarmCreamTextSecondary,
    outline = WarmCreamGlassBorder
)

@Composable
fun SyllabusTrackerTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.WARM_CREAM -> WarmCreamColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    SyllabusTrackerTheme(
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        content = content
    )
}
