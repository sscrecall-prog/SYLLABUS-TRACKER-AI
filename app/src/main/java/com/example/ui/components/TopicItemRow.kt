package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterItemRow(
    item: SyllabusItem,
    depth: Int = 0,
    hasChildren: Boolean = false,
    isExpanded: Boolean = false,
    isCompactMode: Boolean = false,
    subjectColor: Color? = null,
    onToggleExpand: (() -> Unit)? = null,
    onClick: () -> Unit,
    onStatusChange: (ChapterStatus) -> Unit,
    onQuickAction: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f

    var showMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    val isSection = item.itemType == ItemType.SECTION
    val isSubsection = item.itemType == ItemType.SUBSECTION
    val indentPadding = (depth * 14).dp
    val isCompleted = item.status == ChapterStatus.COMPLETED || item.completionPercentage == 100

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onStatusChange(ChapterStatus.COMPLETED)
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onQuickAction("toggle_bookmark")
                    false
                }
                else -> false
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = indentPadding,
                end = 0.dp,
                top = if (isCompactMode) 2.dp else 3.dp,
                bottom = if (isCompactMode) 2.dp else 3.dp
            )
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = if (isSection) 2.dp else 1.dp)
                            .clip(RoundedCornerShape(if (isSection) 16.dp else 12.dp))
                            .background(SoftMint.copy(alpha = 0.2f))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Mark Revised", tint = SoftMint)
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = if (isSection) 2.dp else 1.dp)
                            .clip(RoundedCornerShape(if (isSection) 16.dp else 12.dp))
                            .background(ElectricBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Toggle Bookmark", tint = ElectricBlue)
                    }
                }
            },
            content = {
                val cardBorderColor by animateColorAsState(
                    targetValue = if (isCompleted) {
                        SoftMint.copy(alpha = 0.35f)
                    } else if (item.isWeak) {
                        AlertRed.copy(alpha = 0.4f)
                    } else if (item.isRevisionDue) {
                        Color(0xFFA78BFA).copy(alpha = 0.4f)
                    } else if (subjectColor != null) {
                        subjectColor.copy(alpha = 0.25f)
                    } else {
                        DarkGlassBorder
                    },
                    animationSpec = tween(300),
                    label = "borderGlow"
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick() },
                    shape = RoundedCornerShape(if (isSection) 18.dp else 14.dp),
                    elevation = if (isSection) 3.dp else 1.dp,
                    accentColor = cardBorderColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 14.dp,
                                vertical = if (isCompactMode) 8.dp else if (isSection) 12.dp else 10.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tree expand icon or Type indicator
                            if (hasChildren) {
                                IconButton(
                                    onClick = { onToggleExpand?.invoke() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else if (!isSection) {
                                // Status Circle with Soft Mint Glow on Complete
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCompleted) SoftMint.copy(alpha = 0.2f)
                                            else item.status.getColor().copy(alpha = 0.12f)
                                        )
                                        .clickable { showStatusMenu = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = SoftMint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(text = item.status.iconEmoji, fontSize = 12.sp)
                                    }

                                    DropdownMenu(
                                        expanded = showStatusMenu,
                                        onDismissRequest = { showStatusMenu = false },
                                        modifier = Modifier.background(if (isDark) DarkSurfaceElevated else colorScheme.surface)
                                    ) {
                                        ChapterStatus.values().forEach { status ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(status.iconEmoji)
                                                        Text(
                                                            status.label,
                                                            color = status.getColor(),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    onStatusChange(status)
                                                    showStatusMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElectricBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = "Section",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Title & Indicators
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.title,
                                        fontSize = if (isSection) 15.sp else if (isSubsection) 14.sp else 13.sp,
                                        fontWeight = if (isSection) FontWeight.Bold else if (isSubsection) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isCompleted) colorScheme.onSurface.copy(alpha = 0.5f) else colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (item.isImportant) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Important",
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    if (item.isBookmarked) {
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = "Bookmarked",
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                // Meta details for Chapters
                                if (!isSection && !isSubsection) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        StatusBadge(status = item.status)
                                        PriorityBadge(priority = item.priority)
                                        if (item.revisionCount > 0) {
                                            Text(
                                                text = "Rev: ${item.revisionCount}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                        if (item.pyqAttempted > 0) {
                                            Text(
                                                text = "PYQ: ${item.pyqAccuracy}%",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (item.pyqAccuracy >= 70) SoftMint else AlertRed
                                            )
                                        }
                                    }
                                }
                            }

                            // Progress percentage
                            val progressText = "${item.completionPercentage}%"
                            Text(
                                text = progressText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) SoftMint else Color(0xFF94A3B8),
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            // 3-Dots Quick Actions Menu
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More actions",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(if (isDark) DarkSurfaceElevated else colorScheme.surface)
                                ) {
                                    if (isSection) {
                                        DropdownMenuItem(
                                            text = { Text("Add Sub-Section", color = colorScheme.onSurface) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.SnippetFolder,
                                                    contentDescription = null,
                                                    tint = ElectricBlue
                                                )
                                            },
                                            onClick = {
                                                onQuickAction("add_subsection")
                                                showMenu = false
                                            }
                                        )
                                    }
                                    if (isSubsection) {
                                        DropdownMenuItem(
                                            text = { Text("Add Chapter", color = colorScheme.onSurface) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = ElectricBlue
                                                )
                                            },
                                            onClick = {
                                                onQuickAction("add_chapter")
                                                showMenu = false
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("View & Edit Details", color = colorScheme.onSurface) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            onQuickAction("edit")
                                            showMenu = false
                                        }
                                    )
                                    if (!isSection && !isSubsection) {
                                        DropdownMenuItem(
                                            text = { Text("Mark as Revised", color = colorScheme.onSurface) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Update,
                                                    contentDescription = null,
                                                    tint = Color(0xFFA78BFA)
                                                )
                                            },
                                            onClick = {
                                                onQuickAction("revise")
                                                showMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Start Study Timer", color = colorScheme.onSurface) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Timer,
                                                    contentDescription = null,
                                                    tint = ElectricBlue
                                                )
                                            },
                                            onClick = {
                                                onQuickAction("timer")
                                                showMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Track PYQs", color = colorScheme.onSurface) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Quiz,
                                                    contentDescription = null,
                                                    tint = SoftMint
                                                )
                                            },
                                            onClick = {
                                                onQuickAction("pyq")
                                                showMenu = false
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Duplicate", color = colorScheme.onSurface) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                tint = colorScheme.onSurfaceVariant
                                            )
                                        },
                                        onClick = {
                                            onQuickAction("duplicate")
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Move Up", color = colorScheme.onSurface) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = colorScheme.onSurfaceVariant
                                            )
                                        },
                                        onClick = {
                                            onQuickAction("move_up")
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Move Down", color = colorScheme.onSurface) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = colorScheme.onSurfaceVariant
                                            )
                                        },
                                        onClick = {
                                            onQuickAction("move_down")
                                            showMenu = false
                                        }
                                    )
                                    HorizontalDivider(color = if (isDark) DarkGlassBorder else colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = AlertRed) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = AlertRed
                                            )
                                        },
                                        onClick = {
                                            onQuickAction("delete")
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Glowing Linear Progress Bar for Chapter Progress
                        if (!isSection && item.completionPercentage > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearSyllabusBar(
                                progress = item.completionPercentage / 100f,
                                height = 3.dp,
                                barColor = if (isCompleted) SoftMint else ElectricBlue
                            )
                        }
                    }
                }
            }
        )
    }
}
