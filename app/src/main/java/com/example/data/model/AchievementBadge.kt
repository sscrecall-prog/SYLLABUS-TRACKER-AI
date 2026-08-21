package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BadgeCategory(val label: String, val iconName: String) {
    STREAK("Consistency & Streaks", "LocalFireDepartment"),
    TOPICS("Chapter Milestones", "CheckCircle"),
    SUBJECT("Subject Mastery", "EmojiEvents"),
    STUDY_TIME("Focus & Time", "Timer"),
    REVISION("Spaced Revision", "Update"),
    PYQ("PYQ & Confidence", "Psychology"),
    GOALS("Target Goals", "TrackChanges")
}

enum class BadgeTier(val label: String, val colorHex: String, val glowHex: String) {
    BRONZE("Bronze", "#CD7F32", "#FFE0B2"),
    SILVER("Silver", "#9E9E9E", "#ECEFF1"),
    GOLD("Gold", "#FFB300", "#FFF8E1"),
    PLATINUM("Platinum", "#00BCD4", "#E0F7FA"),
    DIAMOND("Diamond", "#7C4DFF", "#EDE7F6")
}

@Entity(tableName = "achievement_badges")
data class AchievementBadge(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: BadgeCategory,
    val iconEmoji: String,
    val tier: BadgeTier,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val currentProgress: Int = 0,
    val maxProgress: Int = 1,
    val rewardXp: Int = 50,
    val hintRequirement: String = ""
) {
    val progressPercentage: Int
        get() = if (maxProgress > 0) {
            ((currentProgress.toFloat() / maxProgress.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else if (isUnlocked) 100 else 0
}
