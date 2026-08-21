package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val revisionIntervalsCsv: String = "1,3,7,21,45,90",
    val dailyTargetMinutes: Int = 240, // 4 hours
    val weeklyTargetMinutes: Int = 1440, // 24 hours
    val pomodoroWorkMinutes: Int = 25,
    val pomodoroShortBreakMinutes: Int = 5,
    val pomodoroLongBreakMinutes: Int = 15,
    val defaultPriority: Priority = Priority.MEDIUM,
    val defaultDifficulty: Difficulty = Difficulty.MEDIUM,
    val userName: String = "Aspirant",
    val targetExam: String = "SSC CGL 2026",
    val targetExamDateStr: String = "2026-09-15",
    val targetExamShift: String = "Tier-1 / Prelims",
    val userAvatarEmoji: String = "🎓",
    val reducedMotion: Boolean = false
) {
    val revisionIntervals: List<Int>
        get() = revisionIntervalsCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .ifEmpty { listOf(1, 3, 7, 21, 45, 90) }
}
