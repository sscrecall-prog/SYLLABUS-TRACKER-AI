package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Data representation of a bottom navigation item.
 */
data class BottomNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val badgeCount: Int? = null,
    val badgeColor: Color? = null,
    val accentColor: Color? = null,
    val isMoreMenu: Boolean = false,
    val testTag: String = "bottom_nav_$id",
    val contentDescription: String? = null,
    val extraData: Any? = null
)

val CenterFabElectricBlue = ElectricBlue
val CenterFabElectricBlueDark = ElectricBlueDark
val CenterFabCyanGlow = Color(0x666EC2FD)

/**
 * Custom Shape for the Center-Scooped Bottom Navigation Cradle Bar.
 * Creates a smooth organic dipped cradle cutout in the top center for the circular FAB.
 */
class CenterNotchedCradleShape(
    private val cornerRadius: Dp = 26.dp,
    private val cradleRadius: Dp = 36.dp,
    private val cradleMargin: Dp = 6.dp,
    private val roundedCornerNotch: Dp = 14.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerPx = with(density) { cornerRadius.toPx() }
        val cradleRadiusPx = with(density) { cradleRadius.toPx() }
        val marginPx = with(density) { cradleMargin.toPx() }
        val rcnPx = with(density) { roundedCornerNotch.toPx() }

        val path = Path().apply {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val totalRadius = cradleRadiusPx + marginPx
            val cradleLeft = cx - totalRadius
            val cradleRight = cx + totalRadius
            val cradleDepth = totalRadius * 0.72f

            moveTo(0f, cornerPx)

            // 1. Top-Left Rounded Corner
            arcTo(
                rect = Rect(0f, 0f, cornerPx * 2, cornerPx * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // 2. Line along top edge up to cradle entry
            lineTo(cradleLeft - rcnPx, 0f)

            // 3. Smooth organic scoop dip into the cradle (Cubic Bezier)
            cubicTo(
                x1 = cradleLeft,
                y1 = 0f,
                x2 = cradleLeft + marginPx * 1.5f,
                y2 = cradleDepth,
                x3 = cx,
                y3 = cradleDepth
            )
            cubicTo(
                x1 = cradleRight - marginPx * 1.5f,
                y1 = cradleDepth,
                x2 = cradleRight,
                y2 = 0f,
                x3 = cradleRight + rcnPx,
                y3 = 0f
            )

            // 4. Line along top edge to top-right corner
            lineTo(w - cornerPx, 0f)

            // 5. Top-Right Rounded Corner
            arcTo(
                rect = Rect(w - cornerPx * 2, 0f, w, cornerPx * 2),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // 6. Right edge
            lineTo(w, h - cornerPx)

            // 7. Bottom-Right Rounded Corner
            arcTo(
                rect = Rect(w - cornerPx * 2, h - cornerPx * 2, w, h),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // 8. Bottom edge
            lineTo(cornerPx, h)

            // 9. Bottom-Left Rounded Corner
            arcTo(
                rect = Rect(0f, h - cornerPx * 2, cornerPx * 2, h),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Premium Command Center Bottom Navigation Bar.
 * - Floating dark glass cradle bar with smooth circular notch cutout.
 * - Glowing Electric Blue Center FAB with spring physics.
 * - Left tabs: Home, Subject.
 * - Right tabs: Planner, Profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedBottomNavigation(
    items: List<BottomNavItem>,
    activeItemId: String,
    onItemSelected: (BottomNavItem) -> Unit,
    onFabClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    maxVisibleItems: Int = 5,
    accentColor: Color = ElectricBlue
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f

    var showMoreSheet by remember { mutableStateOf(false) }

    // Primary 4 Tabs around the Center FAB
    val homeItem = remember(items) {
        items.find { it.id == "dest_dashboard" } ?: BottomNavItem(
            id = "dest_dashboard",
            label = "Home",
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
            testTag = "bottom_nav_home"
        )
    }

    val subjectItem = remember(items) {
        items.find { it.id == "dest_subjects" } ?: BottomNavItem(
            id = "dest_subjects",
            label = "Subject",
            icon = Icons.Outlined.Search,
            selectedIcon = Icons.Filled.Search,
            testTag = "bottom_nav_explore"
        )
    }

    val plannerItem = remember(items) {
        items.find { it.id == "dest_planner" } ?: BottomNavItem(
            id = "dest_planner",
            label = "Planner",
            icon = Icons.Outlined.Notifications,
            selectedIcon = Icons.Filled.Notifications,
            testTag = "bottom_nav_inbox"
        )
    }

    val profileItem = remember(items) {
        items.find { it.id == "dest_profile" } ?: BottomNavItem(
            id = "dest_profile",
            label = "Profile",
            icon = Icons.Outlined.AccountCircle,
            selectedIcon = Icons.Filled.AccountCircle,
            testTag = "bottom_nav_profile"
        )
    }

    val otherItems = remember(items) {
        items.filter { it.id != "dest_dashboard" && it.id != "dest_subjects" && it.id != "dest_planner" && it.id != "dest_profile" }
    }

    val cradleShape = remember { CenterNotchedCradleShape() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("animated_bottom_nav_container"),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. FLOATING CRADLE BAR (Dark Glass Surface)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = cradleShape,
                    ambientColor = if (isDark) Color.Black.copy(alpha = 0.8f) else Color(0x336EC2FD),
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.85f) else Color(0x406EC2FD)
                ),
            shape = cradleShape,
            color = if (isDark) Color(0xFF191D26).copy(alpha = 0.95f) else Color.White,
            border = BorderStroke(
                width = 1.dp,
                color = if (isDark) Color(0x2EFFFFFF) else Color(0xFFE2E8F0)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT PAIR: Home & Subject
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CenterFabNavItem(
                        item = homeItem.copy(label = "Home"),
                        isSelected = activeItemId == homeItem.id,
                        activeColor = ElectricBlue,
                        onClick = { onItemSelected(homeItem) }
                    )

                    CenterFabNavItem(
                        item = subjectItem.copy(label = "Subject"),
                        isSelected = activeItemId == subjectItem.id,
                        activeColor = ElectricBlue,
                        onClick = { onItemSelected(subjectItem) }
                    )
                }

                // CENTER SPACER (Breathing room for scooped cradle)
                Spacer(modifier = Modifier.width(64.dp))

                // RIGHT PAIR: Planner & Profile
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CenterFabNavItem(
                        item = plannerItem.copy(label = "Planner"),
                        isSelected = activeItemId == plannerItem.id,
                        activeColor = ElectricBlue,
                        onClick = { onItemSelected(plannerItem) }
                    )

                    CenterFabNavItem(
                        item = profileItem.copy(label = "Profile"),
                        isSelected = activeItemId == profileItem.id,
                        activeColor = ElectricBlue,
                        onClick = { onItemSelected(profileItem) }
                    )
                }
            }
        }

        // 2. CENTER PROMINENT FLOATING CIRCULAR FAB
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        ) {
            CenterProminentFab(
                onClick = onFabClick
            )
        }
    }

    // 3. OVERFLOW "MORE" BOTTOM SHEET (If invoked)
    if (showMoreSheet && otherItems.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            containerColor = if (isDark) DarkSurface else colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Tools & Modules",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    IconButton(onClick = { showMoreSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(otherItems) { item ->
                        val isSelected = item.id == activeItemId
                        val itemColor = item.accentColor ?: ElectricBlue

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp)
                                .clickable {
                                    showMoreSheet = false
                                    onItemSelected(item)
                                }
                                .testTag("more_sheet_item_${item.id}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) itemColor.copy(alpha = 0.16f) else if (isDark) DarkSurfaceElevated else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = if (isSelected) BorderStroke(1.2.dp, itemColor) else BorderStroke(1.dp, if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) (item.selectedIcon ?: item.icon) else item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) itemColor else colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) itemColor else colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Center Prominent Floating FAB with Cyan Glow and Rotating Spring Feedback
 */
@Composable
private fun CenterProminentFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "FabScaleMotion"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "FabRotationMotion"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 12.dp,
        animationSpec = tween(150),
        label = "FabElevation"
    )

    Box(
        modifier = modifier
            .size(62.dp)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                ambientColor = CenterFabCyanGlow,
                spotColor = ElectricBlueDark
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        ElectricBlue,
                        ElectricBlueDark
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                contentDescription = "Quick Action Plus Button"
            }
            .testTag("center_prominent_fab"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color(0xFF071B2B), // Deep high contrast dark icon
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
        )
    }
}

/**
 * Individual Navigation Item with active cyan glow & pill dot
 */
@Composable
private fun CenterFabNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else if (isSelected) 1.14f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "NavItemScale"
    )

    val tintColor by animateColorAsState(
        targetValue = if (isSelected) ElectricBlue else Color(0xFF64748B),
        animationSpec = tween(200),
        label = "NavItemColor"
    )

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .semantics {
                role = Role.Tab
                selected = isSelected
                contentDescription = item.contentDescription ?: item.label
            }
            .testTag(item.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
            }
        ) {
            val displayIcon = if (isSelected) (item.selectedIcon ?: item.icon) else item.icon

            Icon(
                imageVector = displayIcon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(23.dp)
            )

            if (item.badgeCount != null && item.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-3).dp)
                        .clip(CircleShape)
                        .background(item.badgeColor ?: AlertRed)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${item.badgeCount}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = tintColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun getSubjectIcon(iconName: String): ImageVector {
    return when (iconName) {
        "MenuBook" -> Icons.Default.MenuBook
        "Calculate" -> Icons.Default.Calculate
        "Psychology" -> Icons.Default.Psychology
        "Translate" -> Icons.Default.Translate
        "History" -> Icons.Default.History
        "Science" -> Icons.Default.Science
        "Public" -> Icons.Default.Public
        "Gavel" -> Icons.Default.Gavel
        "Computer" -> Icons.Default.Computer
        "AutoStories" -> Icons.Default.AutoStories
        else -> Icons.Default.School
    }
}
