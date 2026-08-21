package com.example.data.intelligence

import com.example.data.model.MistakeCategory

// -----------------------------------------------------------------------------
// Sprint 3: Performance Snapshot & Topic-Mock Models
// -----------------------------------------------------------------------------

/**
 * Reusable point-in-time Performance Snapshot for a topic.
 * Allows deterministic comparison of BEFORE vs AFTER study interventions.
 */
data class PerformanceSnapshot(
    val timestamp: Long,
    val topicId: Long,
    val topicTitle: String,
    val masteryScore: Double, // 0.0 to 100.0
    val pyqAccuracy: Double, // 0.0 to 100.0, or -1.0 if NO_DATA
    val confidence: Double, // 20.0 to 100.0
    val mistakeCount: Int,
    val activeMistakeCount: Int,
    val revisionCount: Int,
    val completion: Double, // 0.0 to 100.0
    val relevantStudyTimeSeconds: Long = 0L
)

/**
 * Aggregated performance metrics of a Mock Test mapped to a specific syllabus topic.
 */
data class MockTopicPerformance(
    val topicId: Long?,
    val topicTitle: String,
    val subjectId: Long?,
    val questionsCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val accuracy: Double, // 0.0 to 100.0
    val mockTestId: Long,
    val mockTestName: String,
    val timestamp: Long
)

// -----------------------------------------------------------------------------
// Sprint 3: Recurring Mistake Engine Models
// -----------------------------------------------------------------------------

enum class RecurringMistakeLevel(val label: String, val hexColor: String) {
    ISOLATED("Isolated", "#10B981"),
    REPEATED("Repeated", "#F59E0B"),
    RECURRING("Recurring Weakness", "#EF4444");

    companion object {
        fun fromCount(count: Int): RecurringMistakeLevel {
            return when {
                count <= IntelligenceConfig.isolatedMistakeMaxOccurrences -> ISOLATED
                count <= IntelligenceConfig.repeatedMistakeMaxOccurrences -> REPEATED
                else -> RECURRING
            }
        }
    }
}

data class RecurringMistakeGroup(
    val topicId: Long?,
    val topicTitle: String,
    val subjectId: Long?,
    val subjectName: String,
    val totalOccurrences: Int,
    val recentOccurrences: Int,
    val categories: List<MistakeCategory>,
    val repeatedCategories: Map<MistakeCategory, Int>,
    val primaryCategory: MistakeCategory?,
    val lastOccurrence: Long,
    val recurrenceScore: Double, // 0.0 to 100.0
    val level: RecurringMistakeLevel,
    val recommendation: String = ""
)

// -----------------------------------------------------------------------------
// Sprint 3: Performance Trend Engine Models
// -----------------------------------------------------------------------------

enum class TrendDirection(val label: String, val arrow: String, val hexColor: String) {
    IMPROVING("Improving", "↗", "#10B981"),
    STABLE("Stable", "→", "#3B82F6"),
    DECLINING("Declining", "↘", "#EF4444"),
    INSUFFICIENT_DATA("Insufficient Data", "–", "#9CA3AF")
}

enum class TrendWindow(val days: Int, val label: String) {
    DAYS_7(7, "7-Day Trend"),
    DAYS_15(15, "15-Day Trend"),
    DAYS_30(30, "30-Day Trend")
}

data class TrendMetric(
    val metricName: String,
    val currentValue: Double,
    val previousValue: Double,
    val absoluteChange: Double,
    val percentageChange: Double,
    val direction: TrendDirection,
    val hasSufficientData: Boolean,
    val formattedDisplay: String = ""
)

data class PerformanceTrendResult(
    val window: TrendWindow,
    val masteryTrend: TrendMetric,
    val pyqAccuracyTrend: TrendMetric,
    val mockScoreTrend: TrendMetric,
    val mistakesTrend: TrendMetric,
    val weakTopicsTrend: TrendMetric,
    val studyTimeTrend: TrendMetric,
    val summary: String = ""
)

// -----------------------------------------------------------------------------
// Sprint 3: Topic Improvement Analysis Models
// -----------------------------------------------------------------------------

enum class ImprovementOutcome(val label: String, val hexColor: String) {
    SIGNIFICANT_IMPROVEMENT("Significant Improvement", "#10B981"),
    IMPROVED("Improved", "#34D399"),
    STABLE("Stable", "#3B82F6"),
    DECLINED("Declined", "#EF4444"),
    INSUFFICIENT_DATA("Insufficient Data", "#9CA3AF")
}

data class TopicImprovementResult(
    val topicId: Long,
    val topicTitle: String,
    val beforeSnapshot: PerformanceSnapshot?,
    val afterSnapshot: PerformanceSnapshot,
    val masteryDelta: Double,
    val pyqDelta: Double,
    val mistakeDelta: Int,
    val confidenceDelta: Double,
    val improvementScore: Double, // 0.0 to 100.0 (normalized composite delta)
    val outcome: ImprovementOutcome,
    val summary: String
)

// -----------------------------------------------------------------------------
// Sprint 3: Study Effectiveness Engine Models
// -----------------------------------------------------------------------------

enum class EffectivenessLevel(val label: String, val hexColor: String) {
    LOW("Low", "#EF4444"),
    MODERATE("Moderate", "#F59E0B"),
    GOOD("Good", "#3B82F6"),
    HIGH("High", "#10B981"),
    INSUFFICIENT_DATA("Insufficient Data", "#9CA3AF");

    companion object {
        fun fromScore(score: Double): EffectivenessLevel {
            val clamped = score.coerceIn(0.0, 100.0)
            return when {
                clamped <= IntelligenceConfig.EFFECTIVENESS_LOW_MAX -> LOW
                clamped <= IntelligenceConfig.EFFECTIVENESS_MODERATE_MAX -> MODERATE
                clamped <= IntelligenceConfig.EFFECTIVENESS_GOOD_MAX -> GOOD
                else -> HIGH
            }
        }
    }
}

data class StudyEffectivenessResult(
    val topicId: Long,
    val topicTitle: String,
    val score: Double, // 0.0 to 100.0 or -1.0 if insufficient data
    val level: EffectivenessLevel,
    val studyTimeMinutes: Int,
    val revisionCount: Int,
    val pyqAttempts: Int,
    val evaluatedOutcomes: List<String>,
    val diagnosisText: String,
    val hasSufficientData: Boolean
)

// -----------------------------------------------------------------------------
// Sprint 3: Retention Validation Models
// -----------------------------------------------------------------------------

enum class RetentionState(val label: String, val hexColor: String) {
    UNKNOWN("Unknown / No Follow-Up", "#9CA3AF"),
    WEAK("Weak Retention", "#EF4444"),
    MODERATE("Moderate Retention", "#F59E0B"),
    STRONG("Strong Retention", "#10B981")
}

data class RetentionValidationResult(
    val topicId: Long,
    val topicTitle: String,
    val initialAccuracy: Double,
    val followUpAccuracy: Double,
    val daysBetweenChecks: Double,
    val state: RetentionState,
    val explanation: String,
    val hasSufficientData: Boolean
)

// -----------------------------------------------------------------------------
// Sprint 3: Weekly Intelligence Report Models
// -----------------------------------------------------------------------------

data class WeeklyReportMetric(
    val label: String,
    val before: Double,
    val after: Double,
    val delta: Double,
    val unit: String = ""
)

data class WeeklyPerformanceReport(
    val generatedTimestamp: Long,
    val hasSufficientData: Boolean,
    val masteryMetric: WeeklyReportMetric,
    val pyqAccuracyMetric: WeeklyReportMetric,
    val mockScoreMetric: WeeklyReportMetric,
    val mistakesMetric: WeeklyReportMetric,
    val weakTopicsMetric: WeeklyReportMetric,
    val totalStudyTimeMinutes: Int,
    val biggestImprovementTopic: String?,
    val biggestImprovementDelta: Double,
    val biggestDeclineTopic: String?,
    val biggestDeclineDelta: Double,
    val topRecurringMistakeTopic: String?,
    val topRecurringMistakeCategory: String?,
    val retentionWarnings: List<String>,
    val overallEffectivenessScore: Double,
    val overallEffectivenessLevel: EffectivenessLevel,
    val headlineSummary: String,
    val actionableTakeaways: List<String>
)

// -----------------------------------------------------------------------------
// Sprint 3: Adaptive Recommendation Feedback Model
// -----------------------------------------------------------------------------

data class PerformanceRecommendation(
    val topicId: Long?,
    val topicTitle: String,
    val actionableAdvice: String,
    val reason: String,
    val strategicCategory: String
)

/**
 * Aggregated Sprint 3 feedback summary for a single topic.
 */
data class TopicFeedbackSummary(
    val topicId: Long,
    val topicTitle: String,
    val currentMastery: Double,
    val previousMastery: Double?,
    val masteryDelta: Double,
    val pyqTrend: TrendDirection,
    val mistakeTrend: TrendDirection,
    val retention: RetentionValidationResult,
    val studyEffectiveness: StudyEffectivenessResult,
    val recurringMistake: RecurringMistakeGroup?,
    val recommendation: PerformanceRecommendation
)
