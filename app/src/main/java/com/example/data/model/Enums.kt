package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class ItemType(val label: String) {
    SECTION("Section"),
    SUBSECTION("Sub Section"),
    CHAPTER("Chapter"),
    SUBTOPIC("Subchapter")
}

enum class ChapterStatus(
    val label: String,
    val iconEmoji: String,
    val hexColor: String
) {
    NOT_STARTED("Not Started", "⚪", "#9E9E9E"),
    LEARNING("Learning", "🔵", "#29B6F6"),
    IN_PROGRESS("In Progress", "🟡", "#FFA726"),
    COMPLETED("Completed", "🟢", "#66BB6A"),
    REVISION_DUE("Revision Due", "🟣", "#AB47BC"),
    WEAK("Weak", "🔴", "#EF5350"),
    MASTERED("Mastered", "⭐", "#FFD54F");

    fun getColor(): Color {
        return when (this) {
            NOT_STARTED -> StatusNotStarted
            LEARNING -> StatusLearning
            IN_PROGRESS -> StatusInProgress
            COMPLETED -> StatusCompleted
            REVISION_DUE -> StatusRevisionDue
            WEAK -> StatusWeak
            MASTERED -> StatusMastered
        }
    }
}

enum class Priority(val label: String, val hexColor: String) {
    LOW("Low", "#81C784"),
    MEDIUM("Medium", "#FFB74D"),
    HIGH("High", "#FF7043"),
    URGENT("Urgent", "#E53935")
}

enum class Difficulty(val label: String, val hexColor: String) {
    EASY("Easy", "#66BB6A"),
    MEDIUM("Medium", "#FFA726"),
    HARD("Hard", "#EF5350")
}

enum class TimerMode(val label: String) {
    POMODORO("Pomodoro"),
    STOPWATCH("Stopwatch"),
    CUSTOM("Custom Timer")
}

enum class AppThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}
