package com.example.ui.viewmodel

import com.example.data.model.*

enum class NavDestination(val label: String, val iconName: String) {
    DASHBOARD("Home", "Dashboard"),
    SUBJECTS("Subjects", "School"),
    SYLLABUS("Syllabus", "AutoStories"),
    MISTAKES("Mistake Notebook", "BookmarkRemove"),
    REVISION("Revision", "Update"),
    PLANNER("Planner", "CalendarMonth"),
    MOCK_TESTS("Mock Tests", "Quiz"),
    ANALYTICS("Analytics", "Analytics"),
    WEAK_TOPICS("Weak", "ReportProblem"),
    GOALS("Goals", "TrackChanges"),
    TIMER("Timer", "Timer"),
    CALENDAR("Calendar", "Event"),
    PROFILE("Profile", "MilitaryTech"),
    SETTINGS("Settings", "Settings")
}

data class SubjectStats(
    val subject: Subject,
    val totalSections: Int,
    val totalChapters: Int,
    val completedChapters: Int,
    val inProgressChapters: Int,
    val notStartedChapters: Int,
    val weakChapters: Int,
    val revisionDueChapters: Int,
    val completionPercentage: Int,
    val totalStudyMinutes: Int,
    val averageConfidence: Float,
    val pyqAttempted: Int,
    val pyqCorrect: Int,
    val pyqAccuracy: Int
)

data class MockStats(
    val totalMocksCount: Int = 0,
    val averageScore: Float = 0f,
    val highestScore: Float = 0f,
    val latestScore: Float = 0f,
    val averagePercentile: Float = 0f,
    val bestPercentile: Float = 0f,
    val averageAccuracy: Float = 0f,
    val clearedCutoffCount: Int = 0,
    val cutoffClearanceRate: Int = 0,
    val averageQuantScore: Float = 0f,
    val averageEnglishScore: Float = 0f,
    val averageReasoningScore: Float = 0f,
    val averageGsScore: Float = 0f,
    val averageTimeTakenMinutes: Int = 0,
    val marksPerMinute: Float = 0f,
    val scoreProgression: List<Pair<String, Float>> = emptyList()
)

data class OverallStats(
    val totalSubjects: Int = 0,
    val totalSections: Int = 0,
    val totalChapters: Int = 0,
    val completedChapters: Int = 0,
    val inProgressChapters: Int = 0,
    val notStartedChapters: Int = 0,
    val weakChapters: Int = 0,
    val revisionDueChapters: Int = 0,
    val masteredChapters: Int = 0,
    val completionPercentage: Int = 0,
    val totalStudyMinutes: Int = 0,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val todayStudyMinutes: Int = 0
)

data class FilterCriteria(
    val query: String = "",
    val subjectId: Long? = null,
    val status: ChapterStatus? = null,
    val difficulty: Difficulty? = null,
    val priority: Priority? = null,
    val onlyWeak: Boolean = false,
    val onlyRevisionDue: Boolean = false
)

enum class TrendMetric {
    STUDY_HOURS,
    SYLLABUS_PROGRESS
}

enum class TimeRange(val label: String, val days: Int) {
    LAST_7_DAYS("7 Days", 7),
    LAST_14_DAYS("14 Days", 14),
    LAST_30_DAYS("30 Days", 30)
}

data class TrendDataPoint(
    val dateLabel: String,
    val fullDate: String,
    val value: Float,
    val unit: String
)

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val overallStats: OverallStats = OverallStats(),
    val subjectStats: List<SubjectStats> = emptyList(),
    val studySessions: List<StudySession> = emptyList(),
    val mockStats: MockStats = MockStats(),
    val examPaceStats: ExamPaceStats = ExamPaceStats(),
    val mockTests: List<MockTest> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.LAST_7_DAYS,
    val selectedMetric: TrendMetric = TrendMetric.STUDY_HOURS,
    val selectedSubjectIdForDetail: Long? = null,
    val trendDataPoints: List<TrendDataPoint> = emptyList(),
    val advancedAnalytics: com.example.data.intelligence.AdvancedAnalyticsSnapshot? = null,
    val selectedAnalyticsWindow: com.example.data.intelligence.AnalyticsTimeWindow = com.example.data.intelligence.AnalyticsTimeWindow.DAYS_30
)

data class MockTestsUiState(
    val isLoading: Boolean = false,
    val allMockTests: List<MockTest> = emptyList(),
    val filteredMockTests: List<MockTest> = emptyList(),
    val mockStats: MockStats = MockStats(),
    val searchQuery: String = "",
    val platformFilter: String? = null,
    val typeFilter: MockTestType? = null,
    val selectedMockTest: MockTest? = null
)

data class RevisionState(
    val overdueList: List<SyllabusItem> = emptyList(),
    val dueTodayList: List<SyllabusItem> = emptyList(),
    val upcomingList: List<SyllabusItem> = emptyList(),
    val recentlyRevisedList: List<SyllabusItem> = emptyList(),
    val weakList: List<SyllabusItem> = emptyList()
)

data class MistakeNotebookUiState(
    val isLoading: Boolean = false,
    val allMistakes: List<MistakeEntry> = emptyList(),
    val filteredMistakes: List<MistakeEntry> = emptyList(),
    val mistakeStats: MistakeStats = MistakeStats(),
    val query: String = "",
    val subjectId: Long? = null,
    val category: MistakeCategory? = null,
    val status: MistakeResolutionStatus? = null,
    val onlyStarred: Boolean = false,
    val onlyReviewDue: Boolean = false,
    val selectedMistake: MistakeEntry? = null
)

data class SyllabusUiState(
    val isLoading: Boolean = false,
    val allItems: List<SyllabusItem> = emptyList(),
    val filteredItems: List<SyllabusItem> = emptyList(),
    val selectedChapter: SyllabusItem? = null,
    val filterCriteria: FilterCriteria = FilterCriteria()
)

data class PlannerUiState(
    val isLoading: Boolean = false,
    val todayDateStr: String = "",
    val todayPlans: List<StudyPlan> = emptyList(),
    val allPlans: List<StudyPlan> = emptyList(),
    val selectedDateStr: String = ""
)

data class SubjectUiState(
    val isLoading: Boolean = false,
    val subjects: List<Subject> = emptyList(),
    val subjectStatsList: List<SubjectStats> = emptyList(),
    val allSubjectHierarchies: List<SubjectHierarchy> = emptyList(),
    val selectedSubjectId: Long? = null
)

