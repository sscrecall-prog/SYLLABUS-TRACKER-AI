package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Professional Landing Page & Showcase Screen for Exam AI Studio.
 * Features hero banners, live feature interactive demos, trust metrics,
 * topper testimonials, tier comparison, and expandable FAQs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var activeFeatureTab by remember { mutableStateOf(0) }
    var expandedFaqIndex by remember { mutableStateOf<Int?>(0) }

    // Live Demo Widget State
    var demoTimerSeconds by remember { mutableStateOf(25 * 60) }
    var isDemoTimerRunning by remember { mutableStateOf(false) }
    var demoSampleNoteSaved by remember { mutableStateOf(false) }

    LaunchedEffect(isDemoTimerRunning) {
        if (isDemoTimerRunning) {
            while (isDemoTimerRunning && demoTimerSeconds > 0) {
                delay(1000)
                demoTimerSeconds--
            }
            if (demoTimerSeconds == 0) {
                isDemoTimerRunning = false
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = if (isDark) DarkSurface.copy(alpha = 0.92f) else colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(ElectricBlue, SoftMintDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = Color(0xFF071B2B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "EXAM AI STUDIO",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.onSurface,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Precision Exam OS",
                                fontSize = 10.sp,
                                color = ElectricBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = onGetStarted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = Color(0xFF071B2B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("landing_top_launch_btn")
                    ) {
                        Text(
                            text = "Launch App",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        },
        containerColor = colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // ----------------------------------------------------
            // 1. HERO BANNER SECTION
            // ----------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = if (isDark) listOf(
                                Color(0xFF0B192C),
                                Color(0xFF102238),
                                colorScheme.background
                            ) else listOf(
                                ElectricBlue.copy(alpha = 0.12f),
                                SoftMint.copy(alpha = 0.15f),
                                colorScheme.background
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Badge Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isDark) DarkSurfaceElevated else ElectricBlue.copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                ElectricBlue.copy(alpha = 0.5f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NEXT-GEN EXAM PREPARATION OS 2.5",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricBlue,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Headline
                    Text(
                        text = "Master Your Exam Syllabus\nwith AI-Driven Precision",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp,
                        color = colorScheme.onSurface,
                        modifier = Modifier.testTag("landing_hero_title")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subheading
                    Text(
                        text = "The ultimate study workspace for competitive exam aspirants. Track micro-syllabus depth, run Pomodoro focus sessions, capture instant study thoughts, and analyze mock tests in real time.",
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center,
                        color = colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // CTA Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onGetStarted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = Color(0xFF071B2B)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(50.dp)
                                .testTag("landing_get_started_primary_btn")
                        ) {
                            Text(
                                text = "Get Started Free",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(1100)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(50.dp)
                                .testTag("landing_demo_secondary_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Demo",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Trust Stats Ticker Bar
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) DarkSurfaceElevated else colorScheme.surface,
                        border = BorderStroke(1.dp, if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrustStatItem("100K+", "Aspirants", ElectricBlue)
                            Divider(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp),
                                color = colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                            TrustStatItem("98.4%", "Accuracy", SoftMintDark)
                            Divider(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp),
                                color = colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                            TrustStatItem("10M+", "Focus Mins", StatusRevisionDue)
                            Divider(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp),
                                color = colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                            TrustStatItem("4.9 ★", "Top Rated", StatusMastered)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ----------------------------------------------------
            // 2. CORE FEATURE SHOWCASE MATRIX
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                SectionHeaderPill(
                    icon = Icons.Default.Category,
                    label = "POWERFUL FEATURE SUITE"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Everything You Need to Top Your Exam",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Built specifically for serious aspirants targeting top ranks.",
                    fontSize = 12.5.sp,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feature Category Tabs
                val featureTabs = listOf(
                    "🎯 Micro-Syllabus",
                    "⏱️ Pomodoro Focus",
                    "💡 Quick Thoughts",
                    "📓 Mistake Log",
                    "📊 AI Analytics"
                )

                ScrollableTabRow(
                    selectedTabIndex = activeFeatureTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    indicator = {},
                    divider = {}
                ) {
                    featureTabs.forEachIndexed { idx, label ->
                        val isSelected = activeFeatureTab == idx
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) ElectricBlue else if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { activeFeatureTab = idx }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF071B2B) else colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Feature Card Display
                AnimatedContent(
                    targetState = activeFeatureTab,
                    label = "featureCardAnim"
                ) { tab ->
                    when (tab) {
                        0 -> FeatureHighlightCard(
                            icon = Icons.Default.AutoStories,
                            title = "Hierarchical Micro-Syllabus Breakdown",
                            description = "Break down complex subjects into Chapters, Sections, Subchapters, and Topics. Track status with 7 precision stages (Not Started, Learning, In Progress, Completed, Revision Due, Weak, Mastered).",
                            tag = "Syllabus Depth",
                            accentColor = ElectricBlue,
                            isDark = isDark,
                            bullets = listOf(
                                "Multi-level topic nesting & confidence tracking",
                                "Bulk subject importer & preloaded syllabus templates",
                                "Exam weightage & priority indicators (Low to Urgent)"
                            )
                        )
                        1 -> FeatureHighlightCard(
                            icon = Icons.Default.HourglassTop,
                            title = "Pomodoro Focus Timer & Soundscapes",
                            description = "Train deep concentration with built-in 25m, 15m, and 45m Pomodoro cycles. Ambient soundscapes (Rain, Cafe, Brown Noise, Lo-Fi) boost focus and automatically credit minutes to daily targets.",
                            tag = "Focus & Flow",
                            accentColor = SoftMintDark,
                            isDark = isDark,
                            bullets = listOf(
                                "Automatic daily & weekly study target credit",
                                "Integrated ambient audio generator & rain noise",
                                "1-tap session completion & streak tracking"
                            )
                        )
                        2 -> FeatureHighlightCard(
                            icon = Icons.Default.Lightbulb,
                            title = "Instant 'Quick Thoughts' Capture",
                            description = "Never lose a formula shortcut or mnemonic again! Tap the Quick Note button from anywhere to instantly log notes, tag key concepts (#PYQ, #Mnemonic, #Formula), and link to subjects.",
                            tag = "Rapid Recall",
                            accentColor = Color(0xFFF59E0B),
                            isDark = isDark,
                            bullets = listOf(
                                "Instant floating modal & sheet access",
                                "Tag chips for fast categorization & retrieval",
                                "Direct subject linking and Room DB persistence"
                            )
                        )
                        3 -> FeatureHighlightCard(
                            icon = Icons.Default.BookmarkRemove,
                            title = "Smart Mistake Notebook & Spaced Repetition",
                            description = "Log PYQ and Mock Test mistakes with root-cause analysis (Silly mistake, Conceptual gap, Time pressure). The built-in Spaced Repetition engine schedules optimal review intervals.",
                            tag = "Error Correction",
                            accentColor = StatusWeak,
                            isDark = isDark,
                            bullets = listOf(
                                "Root-cause mistake categorization",
                                "SM-2 based Spaced Repetition review decks",
                                "Confidence-based auto-scheduling"
                            )
                        )
                        else -> FeatureHighlightCard(
                            icon = Icons.Default.Analytics,
                            title = "AI-Driven Mock Analytics & Pace Engine",
                            description = "Get instant percentile trends, subject-wise score splits, marks-per-minute speed ratios, and exam countdown pace calculations to know exactly when you'll complete your syllabus.",
                            tag = "Rank Predictor",
                            accentColor = StatusRevisionDue,
                            isDark = isDark,
                            bullets = listOf(
                                "Full length & sectional mock score progression",
                                "Speed vs Accuracy balance matrix",
                                "Daily required study pace calculator"
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ----------------------------------------------------
            // 3. INTERACTIVE LIVE DEMO WIDGET
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                SectionHeaderPill(
                    icon = Icons.Default.TouchApp,
                    label = "INTERACTIVE DEMO"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Try the Focus & Note Widgets Live",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Test out the real-time Pomodoro timer and quick thought capture right here.",
                    fontSize = 12.5.sp,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    accentColor = ElectricBlue
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️ Focus Timer Demo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isDemoTimerRunning) "RUNNING" else "READY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElectricBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Timer Display
                        val mins = demoTimerSeconds / 60
                        val secs = demoTimerSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { isDemoTimerRunning = !isDemoTimerRunning },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDemoTimerRunning) StatusWeak else ElectricBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isDemoTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isDemoTimerRunning) "Pause" else "Start Demo", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    isDemoTimerRunning = false
                                    demoTimerSeconds = 25 * 60
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reset")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Note Capture Demo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 Quick Thought Capture Demo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            if (demoSampleNoteSaved) {
                                Text("Saved! ✅", fontSize = 11.sp, color = SoftMintDark, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("#Mnemonic", "#Formula", "#PYQKey", "#Doubt").forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElectricBlue.copy(alpha = 0.15f))
                                        .clickable { demoSampleNoteSaved = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(tag, fontSize = 10.5.sp, color = ElectricBlue, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ----------------------------------------------------
            // 4. TOPPER TESTIMONIALS / SUCCESS STORIES
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                SectionHeaderPill(
                    icon = Icons.Default.EmojiEvents,
                    label = "TOPPER TESTIMONIALS"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Trusted by Top Rankers",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Hear from candidates who transformed their exam prep using Exam AI Studio.",
                    fontSize = 12.5.sp,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                TestimonialCard(
                    name = "Ananya Sharma",
                    rank = "AIR 14 • SSC CGL 2025",
                    quote = "The Micro-Syllabus hierarchy and Pomodoro target auto-crediting gave me 100% clarity on my weak chapters before the Tier 2 exam!",
                    avatarBg = ElectricBlue,
                    isDark = isDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                TestimonialCard(
                    name = "Rohan Verma",
                    rank = "AIR 42 • UPSC CSE 2025",
                    quote = "The Quick Thought modal is a game changer for revision! Whenever I found a crucial GS article or mnemonic, I saved it in 3 seconds.",
                    avatarBg = SoftMintDark,
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ----------------------------------------------------
            // 5. FEATURE COMPARISON MATRIX (FREE vs RANKER PRO)
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                SectionHeaderPill(
                    icon = Icons.Default.Verified,
                    label = "FEATURE MATRIX"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Free vs Ranker PRO Workspace",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) DarkSurfaceElevated else colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ComparisonRow("Unlimited Micro-Syllabus Nesting", true, true)
                        ComparisonRow("Pomodoro Focus & Soundscapes", true, true)
                        ComparisonRow("Instant Quick Thought Capture", true, true)
                        ComparisonRow("Spaced Repetition SM-2 Engine", true, true)
                        ComparisonRow("Full Length Mock & Pace Analytics", false, true)
                        ComparisonRow("Offline Room DB Local Persistence", true, true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ----------------------------------------------------
            // 6. FREQUENTLY ASKED QUESTIONS (FAQ ACCORDION)
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                SectionHeaderPill(
                    icon = Icons.Default.HelpOutline,
                    label = "FREQUENTLY ASKED QUESTIONS"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Got Questions? We've Got Answers",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                val faqs = listOf(
                    "Is my study data stored securely offline?" to "Yes! All your subjects, syllabus items, notes, and focus timer logs are stored locally using Room SQLite database on your device.",
                    "How does the Pomodoro timer update my daily targets?" to "When you finish a focus session, the timer automatically calculates your study minutes and credits them directly to your Daily & Weekly Goal progress bars.",
                    "Can I capture quick notes from anywhere in the app?" to "Yes, simply tap the floating Quick Add FAB or the lightbulb icon in the top header bar to launch the Quick Thought modal anytime.",
                    "Can I track multiple competitive exams at once?" to "Absolutely! You can create custom subjects, syllabus trees, and mock test logs for SSC, UPSC, Banking, GATE, CAT, and state exams."
                )

                faqs.forEachIndexed { idx, (question, answer) ->
                    val isExpanded = expandedFaqIndex == idx
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) DarkSurfaceElevated else colorScheme.surface,
                        border = BorderStroke(1.dp, if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clickable { expandedFaqIndex = if (isExpanded) null else idx }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = question,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = ElectricBlue
                                )
                            }
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = answer,
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ----------------------------------------------------
            // 7. FINAL HIGH-IMPACT CTA BANNER
            // ----------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0F172A),
                                Color(0xFF1E293B),
                                ElectricBlue.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ready to Boost Your Exam Rank?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Join thousands of aspirants using Exam AI Studio to conquer their syllabus with confidence.",
                        fontSize = 12.5.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onGetStarted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = Color(0xFF071B2B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp)
                            .testTag("landing_final_cta_btn")
                    ) {
                        Text(
                            text = "Start Studying Now 🚀",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeaderPill(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricBlue,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ElectricBlue,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun TrustStatItem(value: String, label: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = accentColor
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeatureHighlightCard(
    icon: ImageVector,
    title: String,
    description: String,
    tag: String,
    accentColor: Color,
    isDark: Boolean,
    bullets: List<String>
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) DarkSurfaceElevated else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tag,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            bullets.forEach { b ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = b,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun TestimonialCard(
    name: String,
    rank: String,
    quote: String,
    avatarBg: Color,
    isDark: Boolean
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) DarkSurfaceElevated else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isDark) DarkGlassBorder else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF071B2B)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = name,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = rank,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarBg
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$quote\"",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun ComparisonRow(feature: String, free: Boolean, pro: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Icon(
                imageVector = if (free) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (free) SoftMintDark else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
            Icon(
                imageVector = if (pro) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
