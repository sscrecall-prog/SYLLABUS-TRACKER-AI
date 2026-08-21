package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AchievementBadge
import com.example.data.model.BadgeCategory
import com.example.ui.components.BadgeCard
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.theme.motion.MotionTokens
import com.example.ui.theme.motion.motionCardEntry
import com.example.ui.theme.motion.motionPress
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.ProfileViewModel
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    onNavigate: (com.example.ui.viewmodel.NavDestination) -> Unit
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val appSettings by settingsViewModel.appSettings.collectAsState()
    val overallStats by analyticsViewModel.overallStats.collectAsState()
    val allBadges by profileViewModel.allBadges.collectAsState()

    var selectedCategory by remember { mutableStateOf<BadgeCategory?>(null) }
    var filterUnlockedOnly by remember { mutableStateOf(false) }

    var selectedBadgeForDetail by remember { mutableStateOf<AchievementBadge?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Calculate XP & Level
    val totalXp = remember(allBadges) {
        allBadges.filter { it.isUnlocked }.sumOf { it.rewardXp }
    }
    val unlockedCount = remember(allBadges) {
        allBadges.count { it.isUnlocked }
    }
    val totalBadgesCount = remember(allBadges) {
        allBadges.size
    }

    // Level formula: Level = (XP / 250) + 1
    val currentLevel = (totalXp / 250) + 1
    val xpInCurrentLevel = totalXp % 250
    val xpNeededForNextLevel = 250
    val levelProgress = (xpInCurrentLevel.toFloat() / xpNeededForNextLevel.toFloat()).coerceIn(0f, 1f)

    val levelTitle = when {
        currentLevel >= 8 -> "Grandmaster Scholar 👑"
        currentLevel >= 6 -> "Elite Conqueror ⚔️"
        currentLevel >= 4 -> "Senior Aspirant 🌟"
        currentLevel >= 2 -> "Disciplined Scholar 📚"
        else -> "Eager Initiate 🌱"
    }

    val filteredBadges = remember(allBadges, selectedCategory, filterUnlockedOnly) {
        allBadges.filter { badge ->
            (selectedCategory == null || badge.category == selectedCategory) &&
            (!filterUnlockedOnly || badge.isUnlocked)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Header & Level Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(0),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BrandForestGreen.copy(alpha = 0.15f))
                                .border(2.dp, BrandForestGreen, CircleShape)
                                .clickable { showEditProfileDialog = true }
                                .testTag("profile_avatar_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = appSettings.userAvatarEmoji,
                                fontSize = 32.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = appSettings.userName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                IconButton(
                                    onClick = { showEditProfileDialog = true },
                                    modifier = Modifier.size(32.dp).testTag("edit_profile_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Target: ${appSettings.targetExam}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrandTerracotta.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = levelTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTerracotta
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // XP Progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level $currentLevel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$totalXp XP Total ($xpInCurrentLevel/$xpNeededForNextLevel to Lv ${currentLevel + 1})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { levelProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                }
            }
        }

        // 2. Study Consistency & Milestone Stats Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(1),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Badges unlocked
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🏆", fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$unlockedCount / $totalBadgesCount",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Badges",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Active Streak
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔥", fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${overallStats.currentStreakDays} Days",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTerracotta
                        )
                        Text(
                            text = "Streak",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Study Hours
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "⏱️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        val hours = overallStats.totalStudyMinutes / 60
                        val mins = overallStats.totalStudyMinutes % 60
                        Text(
                            text = "${hours}h ${mins}m",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Focus Time",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Syllabus %
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📈", fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${overallStats.completionPercentage}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandForestGreen
                        )
                        Text(
                            text = "Mastered",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Category Filter Tabs
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionCardEntry(2)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎖️ Achievements & Badges",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = filterUnlockedOnly,
                            onClick = { filterUnlockedOnly = !filterUnlockedOnly },
                            label = { Text(if (filterUnlockedOnly) "Unlocked only" else "All state", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (filterUnlockedOnly) Icons.Default.Check else Icons.Default.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.testTag("filter_unlocked_chip")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All Categories (${allBadges.size})", fontSize = 12.sp) },
                            modifier = Modifier.testTag("category_all_chip")
                        )
                    }

                    items(BadgeCategory.values()) { cat ->
                        val count = allBadges.count { it.category == cat }
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.label} ($count)", fontSize = 12.sp) },
                            modifier = Modifier.testTag("category_${cat.name}_chip")
                        )
                    }
                }
            }
        }

        // 4. Badges Grid
        item {
            if (filteredBadges.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No badges found matching this filter.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Display in 2-column grid pairs
                val pairs = filteredBadges.chunked(2)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pairs.forEachIndexed { rowIndex, pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .motionCardEntry(index = rowIndex + 3),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BadgeCard(
                                badge = pair[0],
                                onClick = { selectedBadgeForDetail = pair[0] },
                                modifier = Modifier.weight(1f)
                            )

                            if (pair.size > 1) {
                                BadgeCard(
                                    badge = pair[1],
                                    onClick = { selectedBadgeForDetail = pair[1] },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    // Badge Details Modal Dialog
    selectedBadgeForDetail?.let { badge ->
        val tierColor = try {
            Color(android.graphics.Color.parseColor(badge.tier.colorHex))
        } catch (e: Exception) {
            BrandForestGreen
        }

        Dialog(onDismissRequest = { selectedBadgeForDetail = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("badge_detail_modal"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(tierColor.copy(alpha = 0.8f), tierColor.copy(alpha = 0.2f))
                    ),
                    width = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(tierColor.copy(alpha = 0.18f))
                            .border(2.dp, tierColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = badge.iconEmoji, fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = badge.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(tierColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${badge.tier.label.uppercase()} TIER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tierColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandTerracotta.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+${badge.rewardXp} XP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTerracotta
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = badge.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress or Unlock Timestamp
                    if (badge.isUnlocked) {
                        val unlockDate = badge.unlockedAt?.let {
                            SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(it))
                        } ?: "Achieved"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(tierColor.copy(alpha = 0.1f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = tierColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Unlocked Successfully",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = tierColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = unlockDate,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Current Milestone Progress",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${badge.currentProgress}/${badge.maxProgress} (${badge.progressPercentage}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { badge.progressPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = tierColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Requirement: ${badge.hintRequirement}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { selectedBadgeForDetail = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .motionPress(onClick = { selectedBadgeForDetail = null })
                            .testTag("close_badge_detail_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Edit Profile Modal
    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(appSettings.userName) }
        var examInput by remember { mutableStateOf(appSettings.targetExam) }
        var avatarInput by remember { mutableStateOf(appSettings.userAvatarEmoji) }

        val avatarOptions = listOf("🎓", "🏆", "🚀", "⚡", "📚", "🧠", "👑", "🎯", "🔥", "💎")

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_dialog"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Edit Aspirant Profile",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Select Avatar Icon",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(avatarOptions) { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (avatarInput == emoji) BrandForestGreen.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = if (avatarInput == emoji) 2.dp else 1.dp,
                                        color = if (avatarInput == emoji) BrandForestGreen else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { avatarInput = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 22.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name / Alias") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = examInput,
                        onValueChange = { examInput = it },
                        label = { Text("Target Exam (e.g. SSC CGL 2026)") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_exam_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditProfileDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                settingsViewModel.updateUserProfile(
                                    name = nameInput.ifBlank { "Aspirant" },
                                    targetExam = examInput.ifBlank { "Competitive Exam" },
                                    avatar = avatarInput
                                )
                                showEditProfileDialog = false
                            },
                            modifier = Modifier.weight(1f).testTag("save_profile_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
