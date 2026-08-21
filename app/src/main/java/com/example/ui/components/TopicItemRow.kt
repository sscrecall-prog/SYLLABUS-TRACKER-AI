package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onQuickAction: (String) -> Unit // "edit", "duplicate", "delete", "move_up", "move_down", "revise", "timer", "pyq"
) {
    var showMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    val isSection = item.itemType == ItemType.SECTION
    val isSubsection = item.itemType == ItemType.SUBSECTION
    val indentPadding = (depth * 14).dp

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onStatusChange(ChapterStatus.COMPLETED)
                    false // Return false so it springs back visually
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
            .padding(start = indentPadding, end = 0.dp, top = if (isCompactMode) 2.dp else 3.dp, bottom = if (isCompactMode) 2.dp else 3.dp)
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
                            .clip(RoundedCornerShape(if (isSection) 14.dp else 10.dp))
                            .background(StatusCompleted.copy(alpha = 0.2f))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Mark Revised", tint = StatusCompleted)
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = if (isSection) 2.dp else 1.dp)
                            .clip(RoundedCornerShape(if (isSection) 14.dp else 10.dp))
                            .background(BrandTerracotta.copy(alpha = 0.2f))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Toggle Bookmark", tint = BrandTerracotta)
                    }
                }
            },
            content = {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick() },
                    shape = RoundedCornerShape(if (isSection) 14.dp else 10.dp),
                    elevation = if (isSection) 2.dp else 1.dp,
                    accentColor = subjectColor ?: if (item.isWeak) StatusWeak else if (item.isRevisionDue) StatusRevisionDue else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = if (isCompactMode) 6.dp else if (isSection) 10.dp else 8.dp)
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (!isSection) {
                        // Quick status circle/emoji button
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(item.status.getColor().copy(alpha = 0.15f))
                                .clickable { showStatusMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item.status.iconEmoji, fontSize = 12.sp)
                            
                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false }
                            ) {
                                ChapterStatus.values().forEach { status ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(status.iconEmoji)
                                                Text(status.label, color = status.getColor(), fontWeight = FontWeight.Medium)
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
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Section",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title & Indicators
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.title,
                                fontSize = if (isSection) 15.sp else if (isSubsection) 14.sp else 13.sp,
                                fontWeight = if (isSection) FontWeight.Bold else if (isSubsection) FontWeight.SemiBold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (item.isImportant) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Important",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (item.isBookmarked) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Bookmarked",
                                    tint = BrandTerracotta,
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (item.pyqAttempted > 0) {
                                    Text(
                                        text = "PYQ: ${item.pyqAccuracy}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (item.pyqAccuracy >= 70) StatusCompleted else StatusWeak
                                    )
                                }
                            }
                        }
                    }

                    // Progress percentage or More Menu/Quick Actions
                    if (isSubsection) {
                        // For Sub-sections, show completion percentage and hide inline "+ Chapter" button
                        // This forces "Add Chapter" to be accessed exclusively via the 3-dot overflow menu
                        Text(
                            text = "${item.completionPercentage}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.completionPercentage == 100) StatusCompleted else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    } else if (!isSection) {
                        Text(
                            text = "${item.completionPercentage}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.completionPercentage == 100) StatusCompleted else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // For Sections, show completion percentage and hide "+ Sub-Section" button
                        // This forces "Add Sub-Section" to be accessed exclusively via the 3-dot overflow menu
                        Text(
                            text = "${item.completionPercentage}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.completionPercentage == 100) StatusCompleted else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More actions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isSection) {
                                DropdownMenuItem(
                                    text = { Text("Add Sub-Section") },
                                    leadingIcon = { Icon(Icons.Default.SnippetFolder, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    onClick = {
                                        onQuickAction("add_subsection")
                                        showMenu = false
                                    }
                                )
                            }
                            if (isSubsection) {
                                DropdownMenuItem(
                                    text = { Text("Add Chapter") },
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    onClick = {
                                        onQuickAction("add_chapter")
                                        showMenu = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("View & Edit Details") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    onQuickAction("edit")
                                    showMenu = false
                                }
                            )
                            if (!isSection && !isSubsection) {
                                DropdownMenuItem(
                                    text = { Text("Mark as Revised") },
                                    leadingIcon = { Icon(Icons.Default.Update, contentDescription = null, tint = StatusRevisionDue) },
                                    onClick = {
                                        onQuickAction("revise")
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Start Study Timer") },
                                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = BrandTerracotta) },
                                    onClick = {
                                        onQuickAction("timer")
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Track PYQs") },
                                    leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    onClick = {
                                        onQuickAction("pyq")
                                        showMenu = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    onQuickAction("duplicate")
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Move Up") },
                                leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) },
                                onClick = {
                                    onQuickAction("move_up")
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Move Down") },
                                leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
                                onClick = {
                                    onQuickAction("move_down")
                                    showMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    onQuickAction("delete")
                                    showMenu = false
                                }
                            )
                        }
                    }
                }

                // Mini completion progress bar
                if (!isSection && item.completionPercentage > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearSyllabusBar(
                        progress = item.completionPercentage / 100f,
                        height = 3.dp,
                        barColor = item.status.getColor()
                    )
                }
            }
        }
        }
        )
    }
}
