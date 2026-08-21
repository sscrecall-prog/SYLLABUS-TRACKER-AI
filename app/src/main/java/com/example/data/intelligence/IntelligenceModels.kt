package com.example.data.intelligence

import com.example.data.model.ChapterStatus

// -----------------------------------------------------------------------------
// Sprint 1 Models
// -----------------------------------------------------------------------------

enum class MasteryLevel(val label: String, val minScore: Double, val maxScore: Double) {
    WEAK("Weak", 0.0, IntelligenceConfig.WEAK_MAX_SCORE),
    LEARNING("Learning", 40.0, IntelligenceConfig.LEARNING_MAX_SCORE),
    STRONG("Strong", 60.0, IntelligenceConfig.STRONG_MAX_SCORE),
    MASTERED("Mastered", 80.0, 100.0);

    companion object {
        fun fromScore(score: Double): MasteryLevel {
            val clamped = score.coerceIn(0.0, 100.0)
            return when {
                clamped < 40.0 -> WEAK
                clamped < 60.0 -> LEARNING
                clamped < 80.0 -> STRONG
                else -> MASTERED
            }
        }
    }
}

enum class PYQStatus {
    NO_DATA,
    POOR,
    AVERAGE,
    GOOD,
    EXCELLENT
}

data class PYQPerformance(
    val attempted: Int,
    val correct: Int,
    val incorrect: Int,
    val accuracy: Double, // 0.0 to 100.0
    val status: PYQStatus
)

data class ConfidenceInfo(
    val value: Int, // 1 to 5
    val normalized: Double // 20.0 to 100.0
)

data class MistakeControlResult(
    val score: Double, // 0.0 to 100.0
    val totalMistakes: Int,
    val activeMistakes: Int,
    val repeatedMistakes: Int,
    val conceptGaps: Int
)

data class RevisionStrengthResult(
    val score: Double, // 0.0 to 100.0
    val revisionCount: Int,
    val overdue: Boolean,
    val daysSinceRevision: Double?
)

data class MasteryComponents(
    val pyqAccuracy: Double,
    val confidence: Double,
    val revisionStrength: Double,
    val completion: Double,
    val mistakeControl: Double,
    val retention: Double
)

data class MasteryResult(
    val score: Double, // 0.0 to 100.0
    val level: MasteryLevel,
    val components: MasteryComponents
)

data class TopicIntelligence(
    val topicId: Long,
    val topicTitle: String,
    val subjectId: Long = 0,
    val subjectName: String = "",
    val masteryScore: Double,
    val masteryLevel: MasteryLevel,
    val pyq: PYQPerformance,
    val confidence: ConfidenceInfo,
    val mistakes: MistakeControlResult,
    val revision: RevisionStrengthResult,
    val weaknessScore: Double,
    val priorityScore: Double,
    val status: ChapterStatus,
    val isMasteredCriteriaMet: Boolean,
    val masteryComponents: MasteryComponents,
    val isMaintenanceOnly: Boolean = false,
    val whyExplanation: String = ""
)

// -----------------------------------------------------------------------------
// Sprint 2 Models
// -----------------------------------------------------------------------------

enum class ReadinessLevel(val label: String, val minScore: Double, val maxScore: Double) {
    CRITICAL("Critical", 0.0, IntelligenceConfig.READINESS_CRITICAL_MAX),
    WEAK("Weak", 40.0, IntelligenceConfig.READINESS_WEAK_MAX),
    PREPARING("Preparing", 60.0, IntelligenceConfig.READINESS_PREPARING_MAX),
    STRONG("Strong", 75.0, IntelligenceConfig.READINESS_STRONG_MAX),
    EXAM_READY("Exam Ready", 90.0, 100.0);

    companion object {
        fun fromScore(score: Double): ReadinessLevel {
            val clamped = score.coerceIn(0.0, 100.0)
            return when {
                clamped < 40.0 -> CRITICAL
                clamped < 60.0 -> WEAK
                clamped < 75.0 -> PREPARING
                clamped < 90.0 -> STRONG
                else -> EXAM_READY
            }
        }
    }
}

data class ReadinessComponents(
    val syllabusCoverage: Double,
    val mastery: Double,
    val pyqPerformance: Double,
    val revisionCoverage: Double,
    val mistakeControl: Double,
    val mockPerformance: Double
)

data class ExamReadinessResult(
    val score: Double, // 0.0 to 100.0
    val level: ReadinessLevel,
    val components: ReadinessComponents,
    val confidence: Double, // 0.0 to 100.0 (data reliability)
    val warnings: List<String>,
    val disclaimer: String = "Exam Ready reflects internal preparation condition and is not a guarantee of selection."
)

enum class LastDaysExamMode(val label: String, val description: String, val focusAreas: String) {
    FOUNDATION(">60 Days", "Foundation & Systematic Learning", "Learn + Practice"),
    ACCELERATION("30–60 Days", "Core Coverage & Spaced Review", "Learn + PYQ + Revision"),
    INTENSIVE_REVISION("7–30 Days", "Targeted Weakness & High Yield", "PYQ + Mock + Weakness"),
    FINAL_CRUNCH("<7 Days", "Rapid Recall & Error Prevention", "Revision + Mock + Mistake Review")
}

enum class PlanActionType(val label: String, val iconName: String) {
    WEAK_TOPIC("Weak Topic", "PriorityHigh"),
    REVISION("Revision", "Refresh"),
    PYQ_PRACTICE("PYQ Drill", "Quiz"),
    MISTAKE_REVIEW("Mistake Review", "BugReport"),
    MOCK_TEST("Mock Test", "Assessment"),
    CONCEPT_REVIEW("Concept Gap", "MenuBook"),
    MAINTENANCE("Maintenance", "CheckCircle")
}

data class PlanActionItem(
    val id: String,
    val topicId: Long?,
    val topicTitle: String,
    val subjectId: Long?,
    val subjectName: String,
    val actionType: PlanActionType,
    val estimatedMinutes: Int,
    val priority: Double,
    val reason: String,
    val isCompleted: Boolean = false
)

data class TodaysPlanResult(
    val totalMinutes: Int,
    val availableMinutes: Int,
    val items: List<PlanActionItem>,
    val lastDaysMode: LastDaysExamMode,
    val notes: String = ""
)

enum class PaceStatus(val label: String) {
    AHEAD("Ahead of Schedule"),
    ON_TRACK("On Track"),
    BEHIND("Behind Schedule"),
    CRITICAL("Critical Delay")
}

data class RecoveryRecommendation(
    val isNeeded: Boolean,
    val isRealistic: Boolean,
    val additionalMinutesPerDay: Int,
    val additionalTopicsPerDay: Double,
    val recommendationText: String,
    val strategicFocus: String
)

data class ExamPaceResult(
    val daysRemaining: Int,
    val completedPercentage: Double,
    val expectedPercentage: Double,
    val currentDailyPace: Double,
    val requiredDailyPace: Double,
    val paceDifference: Double,
    val estimatedCompletionDateStr: String,
    val targetCompletionDateStr: String,
    val status: PaceStatus,
    val recovery: RecoveryRecommendation
)

enum class SubjectHealthStatus(val label: String) {
    EXCELLENT("Healthy & Mastered"),
    GOOD("Good Progress"),
    NEEDS_ATTENTION("Needs Attention"),
    CRITICAL("Critical Gaps")
}

data class SubjectHealthResult(
    val subjectId: Long,
    val subjectName: String,
    val subjectCode: String,
    val colorHex: String,
    val coveragePercentage: Double,
    val averageMasteryScore: Double,
    val pyqAccuracy: Double,
    val revisionCoverage: Double,
    val mistakeControlScore: Double,
    val status: SubjectHealthStatus,
    val totalChapters: Int,
    val completedChapters: Int,
    val weakChapters: Int,
    val revisionDueChapters: Int
)

data class IntelligenceSnapshot(
    val readiness: ExamReadinessResult,
    val todaysPlan: TodaysPlanResult,
    val pace: ExamPaceResult,
    val subjectHealthList: List<SubjectHealthResult>,
    val topWeakTopics: List<TopicIntelligence>,
    val masteredTopics: List<TopicIntelligence>,
    val maintenanceTopics: List<TopicIntelligence>,
    val allTopicIntelligence: Map<Long, TopicIntelligence>,
    val lastDaysMode: LastDaysExamMode
)
