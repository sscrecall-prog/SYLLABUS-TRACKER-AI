package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// SYLLABUS TRACKER - AI COMMAND CENTER THEME
// ==========================================

// Primary Palette
val ElectricBlue = Color(0xFF6EC2FD)
val ElectricBlueDark = Color(0xFF389DF2)
val ElectricBlueGlow = Color(0x406EC2FD)
val ElectricCyanLight = Color(0xFFE0F4FF)

val SoftMint = Color(0xFFBEFFCC)
val SoftMintDark = Color(0xFF5EE88A)
val SoftMintGlow = Color(0x40BEFFCC)

val AlertRed = Color(0xFFEF4648)
val AlertRedDark = Color(0xFFD42D30)
val AlertRedGlow = Color(0x40EF4648)

// Dark Theme Surfaces (Deep, Premium Obsidian & Glass)
val DarkBg = Color(0xFF15171D)
val DarkSurface = Color(0xFF1B1E26)
val DarkSurfaceElevated = Color(0xFF222733)
val DarkSurfaceContainer = Color(0xFF1F2430)
val DarkGlassCard = Color(0xFF191D26)
val DarkGlassCardBorder = Color(0x1FFFFFFF)
val DarkGlassBorder = Color(0x1FFFFFFF)

// Dark Theme Typography
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Light Theme / Day Mode
val LightBg = Color(0xFFF4F7FB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceContainer = Color(0xFFE8EEF5)
val LightGlassCard = Color(0xFFFFFFFF)
val LightGlassBorder = Color(0x1F15171D)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF334155) // Slate 700 - high contrast (> 7.5:1)
val LightTextMuted = Color(0xFF475569)     // Slate 600 - comfortable contrast (> 5.0:1 on off-white)

// Warm Cream Mode (Maintained for theme switching compatibility)
val WarmCreamBg = Color(0xFFF7F4EE)
val WarmCreamSurface = Color(0xFFFFFDF9)
val WarmCreamSurfaceContainer = Color(0xFFECE5D8)
val WarmCreamGlassCard = Color(0xFFFFFDF9)
val WarmCreamGlassBorder = Color(0x2615171D)
val WarmCreamTextPrimary = Color(0xFF1E293B)
val WarmCreamTextSecondary = Color(0xFF334155)
val WarmCreamTextMuted = Color(0xFF475569)

// Backwards compatibility aliases for existing codebase
val BrandForestGreen = ElectricBlue
val BrandForestGreenLight = ElectricCyanLight
val BrandForestGreenDark = ElectricBlueDark
val BrandMoss = Color(0xFF4FA8E8)

val BrandTerracotta = AlertRed
val BrandTerracottaLight = Color(0xFFFF7A7C)
val BrandTerracottaDark = AlertRedDark

val BrandWarmCream = SoftMint
val BrandCreamLight = Color(0xFFE6FFE9)
val BrandCreamDark = Color(0xFFA6F7B8)
val BrandSand = Color(0xFFD6F5DE)

val BrandCharcoal = DarkSurface
val BrandCharcoalLight = DarkSurfaceElevated
val BrandCharcoalDark = DarkBg

// Status Colors - Futuristic High-Contrast Mapping
val WarningOrange = Color(0xFFF97316)
val AmberGold = Color(0xFFF59E0B)
val StatusNotStarted = Color(0xFF64748B)
val StatusLearning = ElectricBlue
val StatusInProgress = AmberGold
val StatusCompleted = SoftMint
val StatusRevisionDue = Color(0xFFA78BFA) // Violet Lavender
val StatusWeak = AlertRed
val StatusMastered = Color(0xFF34D399) // Emerald Mint

// Subject Accent Colors (Futuristic Holographic Spectrum)
val SubjectGS = Color(0xFF6EC2FD)       // Electric Cyan / Blue
val SubjectEnglish = Color(0xFFA78BFA)  // Electric Lavender / Indigo
val SubjectReasoning = Color(0xFFF472B6)// Holographic Pink
val SubjectMaths = Color(0xFF38BDF8)    // Sky Blue
val SubjectComputer = Color(0xFF34D399) // Emerald Mint
val SubjectCustom1 = Color(0xFFFB923C)  // Bright Coral
val SubjectCustom2 = Color(0xFF818CF8)  // Periwinkle Blue
val SubjectCustom3 = Color(0xFFFBBF24)  // Golden Amber

val CenterFabPurple = Color(0xFF6EC2FD)
