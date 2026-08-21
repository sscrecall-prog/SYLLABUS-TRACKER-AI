package com.example.data.model

data class ExamPaceStats(
    val examName: String = "",
    val examDateStr: String = "",
    val examShift: String = "",
    val daysRemaining: Int = 0,
    val weeksRemaining: Float = 0f,
    val totalChapters: Int = 0,
    val completedChapters: Int = 0,
    val remainingChapters: Int = 0,
    val currentPaceChaptersPerDay: Float = 0f,
    val requiredPaceChaptersPerDay: Float = 0f,
    val requiredPaceChaptersPerWeek: Float = 0f,
    val requiredPaceHoursPerDay: Float = 0f,
    val targetDailyMinutes: Int = 0,
    val currentVelocityChaptersPerWeek: Float = 0f,
    val estimatedCompletionDateStr: String = "",
    val isAheadOfSchedule: Boolean = true,
    val paceDifferenceChaptersPerDay: Float = 0f,
    val subjectPaceBreakdown: List<SubjectPaceInfo> = emptyList()
)

data class SubjectPaceInfo(
    val subjectId: Long,
    val subjectName: String,
    val subjectCode: String,
    val colorHex: String,
    val totalChapters: Int,
    val completedChapters: Int,
    val remainingChapters: Int,
    val requiredChaptersPerWeek: Float,
    val estimatedDaysToFinish: Int
)
