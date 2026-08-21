package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromItemType(value: ItemType): String = value.name

    @TypeConverter
    fun toItemType(value: String): ItemType = try {
        ItemType.valueOf(value)
    } catch (e: Exception) {
        ItemType.CHAPTER
    }

    @TypeConverter
    fun fromChapterStatus(value: ChapterStatus): String = value.name

    @TypeConverter
    fun toChapterStatus(value: String): ChapterStatus = try {
        ChapterStatus.valueOf(value)
    } catch (e: Exception) {
        ChapterStatus.NOT_STARTED
    }

    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority = try {
        Priority.valueOf(value)
    } catch (e: Exception) {
        Priority.MEDIUM
    }

    @TypeConverter
    fun fromDifficulty(value: Difficulty): String = value.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty = try {
        Difficulty.valueOf(value)
    } catch (e: Exception) {
        Difficulty.MEDIUM
    }

    @TypeConverter
    fun fromTimerMode(value: TimerMode): String = value.name

    @TypeConverter
    fun toTimerMode(value: String): TimerMode = try {
        TimerMode.valueOf(value)
    } catch (e: Exception) {
        TimerMode.POMODORO
    }

    @TypeConverter
    fun fromAppThemeMode(value: AppThemeMode): String = value.name

    @TypeConverter
    fun toAppThemeMode(value: String): AppThemeMode = try {
        AppThemeMode.valueOf(value)
    } catch (e: Exception) {
        AppThemeMode.SYSTEM
    }

    @TypeConverter
    fun fromBadgeCategory(value: BadgeCategory): String = value.name

    @TypeConverter
    fun toBadgeCategory(value: String): BadgeCategory = try {
        BadgeCategory.valueOf(value)
    } catch (e: Exception) {
        BadgeCategory.TOPICS
    }

    @TypeConverter
    fun fromBadgeTier(value: BadgeTier): String = value.name

    @TypeConverter
    fun toBadgeTier(value: String): BadgeTier = try {
        BadgeTier.valueOf(value)
    } catch (e: Exception) {
        BadgeTier.BRONZE
    }

    @TypeConverter
    fun fromMockTestType(value: MockTestType): String = value.name

    @TypeConverter
    fun toMockTestType(value: String): MockTestType = try {
        MockTestType.valueOf(value)
    } catch (e: Exception) {
        MockTestType.FULL_LENGTH
    }

    @TypeConverter
    fun fromMistakeCategory(value: MistakeCategory): String = value.name

    @TypeConverter
    fun toMistakeCategory(value: String): MistakeCategory = try {
        MistakeCategory.valueOf(value)
    } catch (e: Exception) {
        MistakeCategory.SILLY_MISTAKE
    }

    @TypeConverter
    fun fromMistakeResolutionStatus(value: MistakeResolutionStatus): String = value.name

    @TypeConverter
    fun toMistakeResolutionStatus(value: String): MistakeResolutionStatus = try {
        MistakeResolutionStatus.valueOf(value)
    } catch (e: Exception) {
        MistakeResolutionStatus.ACTIVE
    }
}

