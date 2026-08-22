package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientCard

// Theme Accents matching the infographic
val UiPrimary = Color(0xFF5E5CE6) // Purple / Indigo for UI
val UiLight = Color(0xFFEDE9FE)
val UiDark = Color(0xFF3730A3)

val UxPrimary = Color(0xFF059669) // Emerald / Green for UX
val UxLight = Color(0xFFD1FAE5)
val UxDark = Color(0xFF065F46)

val VsAccent = Color(0xFF1E293B) // Dark slate for VS badge

enum class UiUxViewTab(val label: String, val icon: ImageVector) {
    COMPARE("Comparison Matrix", Icons.Default.CompareArrows),
    LIVE_LAB("Interactive Lab", Icons.Default.Science),
    EXAMPLE("Side by Side Example", Icons.Default.Splitscreen),
    QUIZ("Concept Quiz", Icons.Default.Quiz)
}

data class ComparisonItem(
    val id: String,
    val uiTitle: String,
    val uiDesc: String,
    val uiIcon: ImageVector,
    val uxTitle: String,
    val uxDesc: String,
    val uxIcon: ImageVector
)

data class QuizQuestion(
    val question: String,
    val isUi: Boolean, // true = UI, false = UX
    val explanation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiVsUxScreen(
    onNavigateBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(UiUxViewTab.COMPARE) }
    var interactiveLabMode by remember { mutableStateOf("UI") } // "UI" or "UX"
    var activeFlowStep by remember { mutableIntStateOf(0) }

    // Quiz State
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswerIsUi by remember { mutableStateOf<Boolean?>(null) }
    var quizScore by remember { mutableIntStateOf(0) }
    var showQuizFinished by remember { mutableStateOf(false) }

    val comparisonItems = remember {
        listOf(
            ComparisonItem(
                id = "1",
                uiTitle = "Visual Design",
                uiDesc = "Deals with colors, typography, icons, spacing, and layouts.",
                uiIcon = Icons.Default.Palette,
                uxTitle = "User Research",
                uxDesc = "Understands users, their needs, problems, and goals.",
                uxIcon = Icons.Default.People
            ),
            ComparisonItem(
                id = "2",
                uiTitle = "What Users See",
                uiDesc = "Everything users see and interact with on the screen.",
                uiIcon = Icons.Default.Visibility,
                uxTitle = "Journey Focused",
                uxDesc = "Focuses on the entire end-to-end journey and experience.",
                uxIcon = Icons.Default.AltRoute
            ),
            ComparisonItem(
                id = "3",
                uiTitle = "Screen Based",
                uiDesc = "Works on screens, buttons, menus, and visual UI elements.",
                uiIcon = Icons.Default.PhoneAndroid,
                uxTitle = "Structure & Flow",
                uxDesc = "Creates wireframes, user flows, and information architecture.",
                uxIcon = Icons.Default.AccountTree
            ),
            ComparisonItem(
                id = "4",
                uiTitle = "Consistency",
                uiDesc = "Maintains visual consistency and design systems across the product.",
                uiIcon = Icons.Default.DashboardCustomize,
                uxTitle = "Problem Solving",
                uxDesc = "Finds user friction points and creates frictionless solutions.",
                uxIcon = Icons.Default.Lightbulb
            ),
            ComparisonItem(
                id = "5",
                uiTitle = "Tools Used",
                uiDesc = "Figma, Sketch, Adobe XD, Photoshop, Illustrator",
                uiIcon = Icons.Default.Draw,
                uxTitle = "Tools Used",
                uxDesc = "Figma, Miro, Balsamiq, UsabilityHub, Hotjar",
                uxIcon = Icons.Default.Handyman
            ),
            ComparisonItem(
                id = "6",
                uiTitle = "Primary Goal",
                uiDesc = "Make the product visually appealing, intuitive, and delightful.",
                uiIcon = Icons.Default.AutoAwesome,
                uxTitle = "Primary Goal",
                uxDesc = "Make the product useful, usable, frictionless, and valuable.",
                uxIcon = Icons.Default.CheckCircle
            )
        )
    }

    val quizQuestions = remember {
        listOf(
            QuizQuestion(
                question = "Conducting 1-on-1 interviews to find out why users abandon the checkout cart is...",
                isUi = false,
                explanation = "UX is user research and identifying friction in the user journey."
            ),
            QuizQuestion(
                question = "Choosing font hierarchy, drop shadows, and border radiuses for primary CTA buttons is...",
                isUi = true,
                explanation = "UI deals with typography, visual styling, colors, and aesthetics."
            ),
            QuizQuestion(
                question = "Mapping out the step-by-step wireframe flow from Home ➔ Search ➔ Booking is...",
                isUi = false,
                explanation = "UX structures information architecture and user journey flows."
            ),
            QuizQuestion(
                question = "Creating high-fidelity vector icons and illustrations in Illustrator is...",
                isUi = true,
                explanation = "UI creates visual assets, iconography, and graphic elements."
            ),
            QuizQuestion(
                question = "Measuring task completion time and conducting heat-map usability tests is...",
                isUi = false,
                explanation = "UX tests usability, efficiency, and value of the interaction."
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("ui_vs_ux_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // 1. MASTER BANNER HEADER: UI vs UX
        item {
            GradientCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ui_vs_ux_master_banner"),
                shape = RoundedCornerShape(24.dp),
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = UiPrimary.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, UiPrimary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = UiPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("UI • User Interface", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA5B4FC))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = UxPrimary.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, UxPrimary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, tint = UxPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("UX • User Experience", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6EE7B7))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Typography Title: UI vs UX
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "UI",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = UiPrimary,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "vs",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "UX",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = UxPrimary,
                            letterSpacing = (-1).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Understand the difference. Design better.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Golden Mantra Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UI is how it ",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "LOOKS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFA5B4FC)
                            )
                            Text(
                                text = ".  UX is how it ",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "WORKS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF6EE7B7)
                            )
                            Text(
                                text = ".",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // 2. DUAL PILLARS HERO CARDS (UI Left, UX Right)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // UI Pillar Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ui_pillar_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = UiLight.copy(alpha = 0.35f)),
                    border = BorderStroke(1.5.dp, UiPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(UiPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Draw, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("UI", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UiDark)
                                Text("User Interface", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "• Focuses on look & feel",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Designs visual product parts",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // UX Pillar Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ux_pillar_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = UxLight.copy(alpha = 0.35f)),
                    border = BorderStroke(1.5.dp, UxPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(UxPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("UX", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UxDark)
                                Text("User Experience", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "• Focuses on experience & usability",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Designs overall user journey",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. SEGMENTED TABS (Compare, Live Lab, Example, Quiz)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(UiUxViewTab.values()) { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        leadingIcon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // TAB 1: COMPARISON MATRIX
        if (selectedTab == UiUxViewTab.COMPARE) {
            item {
                Text(
                    text = "📊 Key Differences Breakdown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(comparisonItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("comparison_item_${item.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // UI Side
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(UiPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.uiIcon, contentDescription = null, tint = UiPrimary, modifier = Modifier.size(16.dp))
                                }
                                Column {
                                    Text(item.uiTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UiDark)
                                    Text(item.uiDesc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // Center VS Divider
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("VS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // UX Side
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(UxPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.uxIcon, contentDescription = null, tint = UxPrimary, modifier = Modifier.size(16.dp))
                                }
                                Column {
                                    Text(item.uxTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UxDark)
                                    Text(item.uxDesc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: INTERACTIVE LIVE LAB (Toggle between UI & UX Lens)
        if (selectedTab == UiUxViewTab.LIVE_LAB) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔬 Interactive Lens: Switch Perspective",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "See how the same Travel Booking screen is perceived through UI vs UX lenses.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (interactiveLabMode == "UI") UiPrimary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { interactiveLabMode = "UI" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = if (interactiveLabMode == "UI") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "UI Lens (Looks)",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (interactiveLabMode == "UI") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (interactiveLabMode == "UX") UxPrimary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { interactiveLabMode = "UX" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AltRoute,
                                        contentDescription = null,
                                        tint = if (interactiveLabMode == "UX") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "UX Lens (Works)",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (interactiveLabMode == "UX") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Viewport Mockup
                        if (interactiveLabMode == "UI") {
                            // UI VIEW: Beautiful visual screen
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(2.dp, UiPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Explore Places", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                                        Icon(Icons.Default.Notifications, contentDescription = null, tint = UiPrimary)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Search Bar UI
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        shadowElevation = 3.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Search, contentDescription = null, tint = UiPrimary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Search destinations...", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Destination Card UI
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🏔️ Swiss Alps", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Interlaken, Switzerland", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                    Text("$250 / night", fontSize = 11.5.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                                                }

                                                Button(
                                                    onClick = {},
                                                    colors = ButtonDefaults.buttonColors(containerColor = UiPrimary),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text("Book Now", fontSize = 11.5.sp)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "🎨 UI Focus: Vibrant gradients, soft shadows, rounded corners, brand typography, and micro-interactions.",
                                        fontSize = 11.sp,
                                        color = UiDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            // UX VIEW: Wireframe User Flow & Information Architecture
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(2.dp, UxPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "🗺️ Wireframe Journey & Friction Test",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = UxDark
                                    )
                                    Text(
                                        text = "Tap steps to trace user mental models & conversion funnel.",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    val steps = listOf(
                                        "1. User Intent (Search)" to "Goal: Find peaceful mountains within $300 budget.",
                                        "2. Filter & Comparison" to "UX Decision: Put price & reviews upfront to reduce cognitive load.",
                                        "3. Details & Trust" to "UX Decision: Clear cancellation policy & 1-tap amenities checklist.",
                                        "4. 2-Step Checkout" to "UX Goal: Frictionless booking in under 45 seconds."
                                    )

                                    steps.forEachIndexed { index, (stepTitle, stepDetail) ->
                                        val isActive = activeFlowStep == index
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isActive) UxLight else Color.White,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isActive) UxPrimary else Color.LightGray.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { activeFlowStep = index }
                                                .padding(vertical = 3.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isActive) UxPrimary else Color.Gray),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("${index + 1}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(stepTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isActive) UxDark else Color.DarkGray)
                                                    Text(stepDetail, fontSize = 10.5.sp, color = if (isActive) UxDark.copy(alpha = 0.8f) else Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: SIDE BY SIDE EXAMPLE (From the Infographic)
        if (selectedTab == UiUxViewTab.EXAMPLE) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🖼️ Side by Side Real World Roles",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // UI Designer Role
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = UiLight,
                                    border = BorderStroke(1.dp, UiPrimary)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("UI Designer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UiDark)
                                        Text("Designs this screen", fontSize = 10.sp, color = UiDark.copy(alpha = 0.7f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val uiChecklist = listOf(
                                    "Chooses colors, fonts and icons",
                                    "Designs buttons, cards and components",
                                    "Makes it visually appealing",
                                    "Ensures consistency in design"
                                )
                                uiChecklist.forEach {
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = UiPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }

                            // UX Designer Role
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = UxLight,
                                    border = BorderStroke(1.dp, UxPrimary)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("UX Designer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UxDark)
                                        Text("Plans this experience", fontSize = 10.sp, color = UxDark.copy(alpha = 0.7f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val uxChecklist = listOf(
                                    "Researches users and their needs",
                                    "Creates user flow and wireframes",
                                    "Organizes information and content",
                                    "Improves usability and experience"
                                )
                                uxChecklist.forEach {
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = UxPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 4: CONCEPT QUIZ
        if (selectedTab == UiUxViewTab.QUIZ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 UI or UX? Flashcard Challenge",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Score: $quizScore/${quizQuestions.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!showQuizFinished) {
                            val question = quizQuestions[currentQuestionIndex]

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Question ${currentQuestionIndex + 1} of ${quizQuestions.size}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = question.question,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Choice Buttons: UI vs UX
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (selectedAnswerIsUi == null) {
                                            selectedAnswerIsUi = true
                                            if (question.isUi) quizScore++
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedAnswerIsUi != null) {
                                            if (question.isUi) Color(0xFF10B981) else Color(0xFFEF4444)
                                        } else UiPrimary
                                    )
                                ) {
                                    Text("It's UI (Visual)", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        if (selectedAnswerIsUi == null) {
                                            selectedAnswerIsUi = false
                                            if (!question.isUi) quizScore++
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedAnswerIsUi != null) {
                                            if (!question.isUi) Color(0xFF10B981) else Color(0xFFEF4444)
                                        } else UxPrimary
                                    )
                                ) {
                                    Text("It's UX (Flow)", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (selectedAnswerIsUi != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedAnswerIsUi == question.isUi) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                    border = BorderStroke(1.dp, if (selectedAnswerIsUi == question.isUi) Color(0xFF10B981) else Color(0xFFEF4444))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (selectedAnswerIsUi == question.isUi) "✅ Correct!" else "❌ Incorrect",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (selectedAnswerIsUi == question.isUi) Color(0xFF065F46) else Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = question.explanation,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (currentQuestionIndex < quizQuestions.size - 1) {
                                            currentQuestionIndex++
                                            selectedAnswerIsUi = null
                                        } else {
                                            showQuizFinished = true
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (currentQuestionIndex < quizQuestions.size - 1) "Next Question" else "See Results")
                                }
                            }
                        } else {
                            // Quiz Completed
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎉 Quiz Complete!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("You scored $quizScore out of ${quizQuestions.size}", fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        currentQuestionIndex = 0
                                        quizScore = 0
                                        selectedAnswerIsUi = null
                                        showQuizFinished = false
                                    }
                                ) {
                                    Text("Retake Quiz")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. IN SHORT & SYNERGY PUZZLE CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ui_ux_synergy_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "In Short",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "UI makes it Beautiful. 🎨\nUX makes it Meaningful. 💡",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "🧩 SYNERGY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Best products come when UI and UX work together in harmony.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. PRO TIP FOOTER BANNER
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1B4B),
                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ui_ux_pro_tip_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PRO TIP FOR DEVELOPERS & DESIGNERS",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA5B4FC),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Learn both UI and UX to become a complete designer. Design with beauty. Build with purpose.",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
