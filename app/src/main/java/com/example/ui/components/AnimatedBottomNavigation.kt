package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

/**
 * Premium Glassmorphism Animated Bottom Navigation Component.
 *
 * Features:
 * - A single morphing floating background pill that glides smoothly behind the active item.
 * - Smooth spring-like easing (~380ms) without excessive bounce.
 * - Active item naturally expands to reveal its label while inactive items stay compact.
 * - Active icon scales subtly (1.0 -> 1.08 -> 1.0) and settles cleanly.
 * - GPU-friendly transform and opacity transitions.
 * - Safe area padding, backdrop blur styling, and responsive item budget with "More" sheet.
 * - Dynamically supports any user-created custom subjects and color themes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedBottomNavigation(
    items: List<BottomNavItem>,
    activeItemId: String,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
    maxVisibleItems: Int = 5,
    containerShape: RoundedCornerShape = RoundedCornerShape(26.dp),
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f

    var showMoreSheet by remember { mutableStateOf(false) }

    // Split items into visible bar items and overflow "More" items
    val (visibleItems, overflowItems) = remember(items, maxVisibleItems, activeItemId) {
        if (items.size <= maxVisibleItems) {
            items to emptyList()
        } else {
            val primaryCount = maxVisibleItems - 1
            val primaryList = items.take(primaryCount).toMutableList()
            val overflowList = items.drop(primaryCount)

            // If active item is in overflow, we temporarily promote it to primary visible slot
            val activeInOverflow = overflowList.find { it.id == activeItemId }
            if (activeInOverflow != null) {
                val replaced = primaryList.removeAt(primaryList.lastIndex)
                primaryList.add(activeInOverflow)
                val newOverflow = (overflowList.filter { it.id != activeItemId } + listOf(replaced))
                (primaryList + listOf(
                    BottomNavItem(
                        id = "more_menu",
                        label = "More",
                        icon = Icons.Outlined.GridView,
                        selectedIcon = Icons.Filled.GridView,
                        isMoreMenu = true,
                        testTag = "bottom_nav_more"
                    )
                )) to newOverflow
            } else {
                (primaryList + listOf(
                    BottomNavItem(
                        id = "more_menu",
                        label = "More",
                        icon = Icons.Outlined.GridView,
                        selectedIcon = Icons.Filled.GridView,
                        isMoreMenu = true,
                        testTag = "bottom_nav_more"
                    )
                )) to overflowList
            }
        }
    }

    // Geometry Tracking for the morphing active pill
    val itemBoundsMap = remember { mutableStateMapOf<String, Rect>() }

    // Active item properties
    val activeItem = visibleItems.find { it.id == activeItemId } ?: visibleItems.firstOrNull()
    val activeAccent = activeItem?.accentColor ?: accentColor

    // Target geometry for active pill
    val targetRect = activeItem?.let { itemBoundsMap[it.id] } ?: Rect.Zero
    val targetOffsetX = targetRect.left
    val targetWidth = targetRect.width
    val hasMeasured = targetRect.width > 0f

    // Smooth Spring Morphing Animation for the Single Floating Pill
    val animatedPillOffset by animateFloatAsState(
        targetValue = if (hasMeasured) targetOffsetX else 0f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 380f
        ),
        label = "PillOffsetMotion"
    )

    val animatedPillWidth by animateFloatAsState(
        targetValue = if (hasMeasured) targetWidth else 0f,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = 420f
        ),
        label = "PillWidthMotion"
    )

    val animatedPillAlpha by animateFloatAsState(
        targetValue = if (hasMeasured) 1f else 0f,
        animationSpec = tween(250),
        label = "PillAlpha"
    )

    // Outer Floating Glass Bar Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .testTag("animated_bottom_nav_container")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = containerShape,
                    ambientColor = if (isDark) Color.Black.copy(alpha = 0.6f) else BrandForestGreenDark.copy(alpha = 0.12f),
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.6f) else BrandForestGreenDark.copy(alpha = 0.18f)
                ),
            shape = containerShape,
            color = if (isDark) {
                DarkSurface.copy(alpha = 0.92f)
            } else {
                LightSurface.copy(alpha = 0.94f)
            },
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(DarkGlassBorder.copy(alpha = 0.6f), DarkGlassBorder.copy(alpha = 0.2f))
                    } else {
                        listOf(LightGlassBorder.copy(alpha = 0.9f), LightGlassBorder.copy(alpha = 0.4f))
                    }
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                // 1. SINGLE MORPHING FLOATING ACTIVE PILL
                if (hasMeasured && animatedPillWidth > 0f) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = animatedPillOffset
                                alpha = animatedPillAlpha
                            }
                            .width(with(density) { animatedPillWidth.toDp() })
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                activeAccent.copy(alpha = if (isDark) 0.22f else 0.15f)
                            )
                            .border(
                                width = 1.2.dp,
                                color = activeAccent.copy(alpha = if (isDark) 0.55f else 0.35f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    )
                }

                // 2. INTERACTIVE ITEMS ROW
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    visibleItems.forEach { item ->
                        val isSelected = item.id == activeItemId

                        Box(
                            modifier = Modifier
                                .weight(if (isSelected) 1.35f else 1.0f)
                                .fillMaxHeight()
                                .onGloballyPositioned { coordinates ->
                                    itemBoundsMap[item.id] = coordinates.boundsInParent()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedNavItemCell(
                                item = item,
                                isSelected = isSelected,
                                activeColor = activeAccent,
                                onClick = {
                                    if (item.isMoreMenu) {
                                        showMoreSheet = true
                                    } else {
                                        onItemSelected(item)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 3. OVERFLOW "MORE" BOTTOM SHEET
    if (showMoreSheet && overflowItems.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
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
                        text = "More Sections & Subjects",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { showMoreSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(overflowItems) { item ->
                        val isSelected = item.id == activeItemId
                        val itemColor = item.accentColor ?: MaterialTheme.colorScheme.primary

                        BentoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("more_sheet_item_${item.id}"),
                            shape = RoundedCornerShape(16.dp),
                            accentColor = if (isSelected) itemColor else null,
                            backgroundColor = if (isSelected) itemColor.copy(alpha = 0.12f) else null,
                            onClick = {
                                showMoreSheet = false
                                onItemSelected(item)
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box {
                                    Icon(
                                        imageVector = if (isSelected) (item.selectedIcon ?: item.icon) else item.icon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    if (item.badgeCount != null && item.badgeCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 6.dp, y = (-4).dp)
                                                .clip(CircleShape)
                                                .background(item.badgeColor ?: StatusWeak)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${item.badgeCount}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurface,
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
 * Individual Navigation Item Cell inside the Animated Bottom Navigation.
 */
@Composable
private fun AnimatedNavItemCell(
    item: BottomNavItem,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isTapped by remember { mutableStateOf(false) }

    // Subtle scale feedback when tapped (1.0 -> 1.08 -> 1.0)
    val iconScale by animateFloatAsState(
        targetValue = if (isTapped) 1.12f else if (isSelected) 1.04f else 0.94f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = 500f
        ),
        label = "IconScale"
    )

    val iconAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.68f,
        animationSpec = tween(220),
        label = "IconAlpha"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "TextColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    coroutineScope.launch {
                        isTapped = true
                        delay(120)
                        isTapped = false
                    }
                    onClick()
                }
            )
            .semantics {
                role = Role.Tab
                selected = isSelected
                contentDescription = item.contentDescription ?: item.label
            }
            .testTag(item.testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // ICON & BADGE
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                    alpha = iconAlpha
                }
            ) {
                val displayIcon = if (isSelected) (item.selectedIcon ?: item.icon) else item.icon

                Icon(
                    imageVector = displayIcon,
                    contentDescription = null,
                    tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )

                if (item.badgeCount != null && item.badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(item.badgeColor ?: StatusWeak)
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

            // EXPANDING LABEL ON ACTIVE STATE
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(animationSpec = tween(260, delayMillis = 40, easing = FastOutSlowInEasing)) +
                        expandHorizontally(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f),
                            expandFrom = Alignment.Start
                        ),
                exit = fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                        shrinkHorizontally(
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            shrinkTowards = Alignment.Start
                        )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Helper to map subject icon names to ImageVectors
 */
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

