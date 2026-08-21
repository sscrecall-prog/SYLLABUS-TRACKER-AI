package com.example.data.intelligence

import com.example.data.model.ChapterStatus
import com.example.data.model.MistakeCategory
import com.example.data.model.Subject

// -----------------------------------------------------------------------------
// SPRINT 5: ADVANCED ANALYTICS ENUMS & WINDOWS
// -----------------------------------------------------------------------------

enum class AnalyticsTimeWindow(val days: Int, val label: String) {
    DAYS_7(7, "7 Days"),
    DAYS_15(15, "15 Days"),
    DAYS_30(30, "30 Days"),
    DAYS_90(90, "90 Days"),
    ALL_TIME(3650, "All Time")
}

enum class SubjectHealthTier(val label: String, val hexColor: String) {
    EXCELLENT("Strong & Mastered", "#10B981"),
    GOOD("Good Progress", "#3B82F6"),
    NEEDS_ATTENTION("Needs Attention", "#F59E0B"),
    CRITICAL("Critical Gaps", "#EF4444")
}

// -----------------------------------------------------------------------------
// SPRINT 5: LONG-TERM PERFORMANCE METRICS
// -----------------------------------------------------------------------------

data class LongTermMetricTrend(
    val metricKey: String,
    val label: String,
    val currentValue: Double,
    val previousValue: Double,
    val absoluteChange: Double,
    val percentageChange: Double,
    val direction: TrendDirection,
    val hasSufficientData: Boolean,
    val formattedDisplay: String,
    val unit: String = ""
)

data class LongTermAnalyticsResult(
    val window: AnalyticsTimeWindow,
    val studyTimeHours: LongTermMetricTrend,
    val topicsCompleted: LongTermMetricTrend,
    val topicsMastered: LongTermMetricTrend,
    val pyqAccuracy: LongTermMetricTrend,
    val mockPerformance: LongTermMetricTrend,
    val activeMistakes: LongTermMetricTrend,
    val revisionCompletion: LongTermMetricTrend,
    val masteryGrowth: LongTermMetricTrend,
    val examReadiness: LongTermMetricTrend,
    val hasSufficientData: Boolean,
    val summaryInsight: String
)

// -----------------------------------------------------------------------------
// SPRINT 5: MASTERY GROWTH ANALYTICS
// -----------------------------------------------------------------------------

data class SubjectMasteryDelta(
    val subjectId: Long,
    val subjectName: String,
    val colorHex: String,
    val startMastery: Double,
    val currentMastery: Double,
    val delta: Double
)

data class TopicMasteryDelta(
    val topicId: Long,
    val topicTitle: String,
    val subjectName: String,
    val startMastery: Double,
    val currentMastery: Double,
    val delta: Double
)

data class MasteryGrowthResult(
    val window: AnalyticsTimeWindow,
    val startingMastery: Double,
    val currentMastery: Double,
    val absoluteGrowth: Double,
    val growthRatePointsPerWeek: Double,
    val masteredTopicsCount: Int,
    val totalTopicsCount: Int,
    val newlyMasteredTopicsCount: Int,
    val weakenedTopicsCount: Int,
    val fastestImprovingSubject: SubjectMasteryDelta?,
    val slowestImprovingSubject: SubjectMasteryDelta?,
    val mostImprovedTopic: TopicMasteryDelta?,
    val mostDeclinedTopic: TopicMasteryDelta?,
    val hasSufficientData: Boolean,
    val summaryNote: String
)

// -----------------------------------------------------------------------------
// SPRINT 5: SUBJECT COMPARISON & RANKINGS
// -----------------------------------------------------------------------------

data class SubjectRankItem(
    val rank: Int,
    val subjectId: Long,
    val subjectName: String,
    val subjectCode: String,
    val colorHex: String,
    val masteryScore: Double, // 0.0 to 100.0
    val pyqAccuracy: Double, // 0.0 to 100.0 or -1.0
    val mockScore: Double, // 0.0 to 100.0 or -1.0
    val revisionScore: Double, // 0.0 to 100.0
    val activeMistakes: Int,
    val totalChapters: Int,
    val completedChapters: Int,
    val masteredChapters: Int,
    val healthTier: SubjectHealthTier,
    val compositeScore: Double // Clearly documented 0-100 score
)

data class SubjectComparisonResult(
    val rankings: List<SubjectRankItem>,
    val topSubject: SubjectRankItem?,
    val attentionSubject: SubjectRankItem?,
    val formulaExplanation: String = "Composite Score = 35% Mastery + 25% PYQ + 20% Mock + 10% Revision + 10% Mistake Control"
)

// -----------------------------------------------------------------------------
// SPRINT 5: STUDY CONSISTENCY ENGINE
// -----------------------------------------------------------------------------

data class StudyConsistencyResult(
    val window: AnalyticsTimeWindow,
    val totalDaysInWindow: Int,
    val studyDays: Int,
    val missedDays: Int,
    val plannedSessions: Int,
    val completedSessions: Int,
    val sessionCompletionRatio: Double, // 0.0 to 100.0
    val consistencyPercentage: Double, // 0.0 to 100.0 (composite of day adherence & session completion)
    val averageDailyStudyMinutes: Int,
    val weeklyConsistencyPercentage: Double,
    val monthlyConsistencyPercentage: Double,
    val adherenceGrade: String, // e.g. "Excellent", "Consistent", "Moderate", "Sporadic"
    val feedbackMessage: String
)

// -----------------------------------------------------------------------------
// SPRINT 5: QUALITY-ADJUSTED STUDY TIME
// -----------------------------------------------------------------------------

enum class StudyActivityCategory(val label: String, val multiplier: Double, val iconName: String) {
    ACTIVE_STUDY("Active Core Study", 1.0, "Timer"),
    PYQ_PRACTICE("PYQ Problem Solving", 1.15, "Quiz"),
    MOCK_TEST("Mock Test & Analysis", 1.25, "Assessment"),
    REVISION("Spaced Active Retrieval", 1.10, "Update"),
    MISTAKE_REVIEW("Mistake Remediation", 1.20, "BugReport"),
    PASSIVE_READING("General / Notes Reading", 0.80, "MenuBook")
}

data class ActivityTimeBreakdown(
    val category: StudyActivityCategory,
    val rawMinutes: Int,
    val percentageOfTotal: Double,
    val qualityAdjustedMinutes: Int
)

data class QualityStudyTimeResult(
    val window: AnalyticsTimeWindow,
    val totalRawMinutes: Int,
    val productiveMinutes: Int, // Active + PYQ + Mock + Revision + Mistake
    val qualityAdjustedMinutes: Int,
    val breakdowns: List<ActivityTimeBreakdown>,
    val qualityMultiplierAvg: Double,
    val methodologyNote: String = "Quality adjustment weights active recall, test practice, and error analysis higher (1.1x–1.25x) than passive reading (0.8x)."
)

// -----------------------------------------------------------------------------
// SPRINT 5: PRODUCTIVITY PATTERN ANALYSIS
// -----------------------------------------------------------------------------

enum class TimeOfDaySlot(val label: String, val timeRangeDisplay: String) {
    EARLY_MORNING("Early Morning", "5:00 AM – 8:00 AM"),
    MORNING("Morning", "8:00 AM – 12:00 PM"),
    AFTERNOON("Afternoon", "12:00 PM – 5:00 PM"),
    EVENING("Evening", "5:00 PM – 9:00 PM"),
    NIGHT("Night", "9:00 PM – 1:00 AM"),
    LATE_NIGHT("Late Night", "1:00 AM – 5:00 AM")
}

data class DayOfWeekPerformance(
    val dayName: String, // Monday, Tuesday, etc.
    val totalStudyMinutes: Int,
    val sessionsCount: Int,
    val avgMasteryOrAccuracy: Double
)

data class ProductivityPatternsResult(
    val hasSufficientData: Boolean,
    val bestStudyDay: String?, // e.g. "Tuesday"
    val bestTimeOfDaySlot: TimeOfDaySlot?,
    val bestSubjectByVelocity: String?,
    val peakEfficiencySlotDisplay: String?,
    val dayPerformances: List<DayOfWeekPerformance>,
    val takeawayMessage: String
)

// -----------------------------------------------------------------------------
// SPRINT 5: MEANINGFUL ACHIEVEMENTS & GAMIFICATION
// -----------------------------------------------------------------------------

enum class MeaningfulAchievementId(
    val title: String,
    val description: String,
    val categoryLabel: String,
    val emoji: String,
    val xpValue: Int
) {
    // Mastery
    FIRST_MASTERED_TOPIC("First Mastered Topic", "Achieve 80+ mastery on your first syllabus chapter", "Mastery", "⭐", 100),
    FIVE_MASTERED_TOPICS("Mastery Apprentice", "Attain full mastery across 5 syllabus topics", "Mastery", "🌟", 250),
    TEN_MASTERED_TOPICS("Mastery Scholar", "Attain full mastery across 10 syllabus topics", "Mastery", "🏆", 500),
    TWENTY_FIVE_MASTERED_TOPICS("Mastery Grandmaster", "Attain full mastery across 25 syllabus topics", "Mastery", "👑", 1000),
    SUBJECT_MASTERED("Subject Dominator", "Achieve 100% mastery across an entire subject", "Mastery", "🥇", 750),

    // Revision
    FIRST_REVISION_COMPLETED("Active Recall Initiate", "Complete your first spaced revision cycle on time", "Revision", "🔄", 75),
    REVISION_STREAK("Spaced Retrieval Discipline", "Complete scheduled revisions on 3 consecutive days", "Revision", "⚡", 150),
    TEN_SUCCESSFUL_REVISIONS("Retention Architect", "Successfully complete 10 spaced revision reviews", "Revision", "🧠", 300),
    ALL_DUE_REVISIONS_COMPLETED("Clean Slate", "Clear all overdue revision chapters in a single day", "Revision", "✨", 200),

    // Performance
    PYQ_80_PERCENT("PYQ Proficiency", "Achieve 80%+ accuracy on 20+ attempted PYQs", "Performance", "🎯", 150),
    PYQ_90_PERCENT("PYQ Precision Master", "Achieve 90%+ accuracy on 30+ attempted PYQs", "Performance", "🏹", 300),
    PERSONAL_BEST("New High Ground", "Establish a new personal best score in mock tests or PYQs", "Performance", "📈", 200),
    MOCK_SCORE_IMPROVEMENT("Mock Breakthrough", "Improve mock test score by 10+ marks over previous test", "Performance", "🚀", 250),

    // Mistakes
    FIRST_ERROR_CORRECTED("Mistake Tamed", "Mark your first recorded mistake as resolved", "Mistake Control", "🛡️", 50),
    RECURRING_ERROR_RESOLVED("Root-Cause Crusher", "Eliminate a recurring weakness mistake category", "Mistake Control", "⚔️", 200),
    MISTAKE_RATE_REDUCED("Precision Surge", "Reduce total active mistakes by 25% or more", "Mistake Control", "📉", 250),

    // Consistency
    SEVEN_ACTIVE_DAYS("Weekly Consistency", "Log meaningful study activity on 7 distinct days", "Consistency", "📅", 150),
    FOUR_WEEKS_CONSISTENT("Month of Steel", "Maintain 4 consecutive weeks of regular study", "Consistency", "🔥", 400),
    MONTHLY_GOAL_COMPLETED("Goal Finisher", "Successfully complete a scheduled monthly target goal", "Consistency", "🏁", 300)
}

data class MeaningfulAchievement(
    val id: MeaningfulAchievementId,
    val title: String,
    val description: String,
    val category: String,
    val emoji: String,
    val xpValue: Int,
    val isUnlocked: Boolean,
    val unlockedTimestamp: Long?,
    val currentProgress: Int,
    val maxProgress: Int,
    val progressPercentage: Int,
    val conditionNote: String
)

// -----------------------------------------------------------------------------
// SPRINT 5: MEANINGFUL PROGRESS MILESTONES
// -----------------------------------------------------------------------------

enum class MilestoneCategory {
    SYLLABUS_COVERAGE,
    SYLLABUS_MASTERY,
    REVISION_CYCLES,
    CONSISTENCY
}

data class MeaningfulMilestone(
    val id: String,
    val title: String,
    val category: MilestoneCategory,
    val targetThreshold: Double,
    val currentThreshold: Double,
    val isAchieved: Boolean,
    val progressPercentage: Int,
    val distinctionNote: String // Clearly explains Coverage vs Mastery
)

// -----------------------------------------------------------------------------
// SPRINT 5: PERSONAL RECORDS (PERSONAL BESTS)
// -----------------------------------------------------------------------------

data class PersonalBestRecord(
    val recordKey: String,
    val title: String,
    val valueFormatted: String,
    val rawValue: Double,
    val achievedDate: String,
    val contextDescription: String,
    val isNewThisWeek: Boolean = false
)

data class PersonalRecordsResult(
    val highestMockScore: PersonalBestRecord?,
    val highestPyqAccuracy: PersonalBestRecord?,
    val longestRevisionStreakDays: PersonalBestRecord?,
    val mostTopicsMasteredInAWeek: PersonalBestRecord?,
    val bestWeeklyMasteryGain: PersonalBestRecord?,
    val lowestMistakeRatio: PersonalBestRecord?,
    val highestMonthlyConsistency: PersonalBestRecord?,
    val totalRecordsCount: Int
)

// -----------------------------------------------------------------------------
// SPRINT 5: MEANINGFUL STREAKS & COMPASSIONATE RECOVERY
// -----------------------------------------------------------------------------

data class MeaningfulStreak(
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val lastActiveDateStr: String,
    val isMaintainedToday: Boolean,
    val missedYesterday: Boolean,
    val recoveryMessage: String,
    val meaningfulMinutesRequired: Int = 15,
    val todayMeaningfulMinutesLogged: Int
)

// -----------------------------------------------------------------------------
// SPRINT 5: MONTHLY REVIEW & OVERVIEW
// -----------------------------------------------------------------------------

data class MonthlyReviewResult(
    val monthLabel: String, // e.g. "August 2026"
    val hasSufficientData: Boolean,
    val totalStudyHours: Double,
    val activeStudyDays: Int,
    val totalDaysInMonth: Int,
    val masteryGrowthPoints: Double,
    val subjectRankings: List<SubjectRankItem>,
    val strongestSubject: SubjectRankItem?,
    val weakestSubject: SubjectRankItem?,
    val mostImprovedTopic: TopicMasteryDelta?,
    val recurringMistakesEliminated: Int,
    val mockScoreDelta: Double,
    val readinessChangeDelta: Double,
    val achievementsUnlockedCount: Int,
    val personalBestsSetCount: Int,
    val overallMonthNarrative: String,
    val keyNextMonthDirectives: List<String>
)

// -----------------------------------------------------------------------------
// SPRINT 5: AGGREGATED ADVANCED ANALYTICS SNAPSHOT
// -----------------------------------------------------------------------------

data class AdvancedAnalyticsSnapshot(
    val longTerm7D: LongTermAnalyticsResult,
    val longTerm15D: LongTermAnalyticsResult,
    val longTerm30D: LongTermAnalyticsResult,
    val longTerm90D: LongTermAnalyticsResult,
    val longTermAllTime: LongTermAnalyticsResult,
    val masteryGrowth: MasteryGrowthResult,
    val subjectComparisons: SubjectComparisonResult,
    val consistency: StudyConsistencyResult,
    val qualityStudyTime: QualityStudyTimeResult,
    val productivityPatterns: ProductivityPatternsResult,
    val achievements: List<MeaningfulAchievement>,
    val milestones: List<MeaningfulMilestone>,
    val nextMilestoneRecommendation: MeaningfulMilestone?,
    val personalRecords: PersonalRecordsResult,
    val studyStreak: MeaningfulStreak,
    val revisionStreak: MeaningfulStreak,
    val monthlyReview: MonthlyReviewResult?
)
