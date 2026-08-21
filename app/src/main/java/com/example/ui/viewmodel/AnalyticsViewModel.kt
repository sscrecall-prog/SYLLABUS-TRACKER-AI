package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.intelligence.*
import com.example.data.model.*
import com.example.data.repository.AnalyticsRepository
import kotlinx.coroutines.flow.*

class AnalyticsViewModel(application: Application) : BaseViewModel(application) {

    val examPaceStats: StateFlow<ExamPaceStats> = combine(
        appSettings,
        items,
        subjects,
        studySessions
    ) { settings, allItems, subs, sessions ->
        analyticsRepository.calculateExamPaceStats(settings, allItems, subs, sessions)
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ExamPaceStats()
    )

    val overallStats: StateFlow<OverallStats> = combine(
        subjects,
        items,
        studySessions
    ) { subs, allItems, sessions ->
        analyticsRepository.calculateOverallStats(subs, allItems, sessions)
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        OverallStats()
    )

    val selectedTimeRange = MutableStateFlow(TimeRange.LAST_7_DAYS)
    val selectedMetric = MutableStateFlow(TrendMetric.STUDY_HOURS)
    val selectedSubjectIdForDetail = MutableStateFlow<Long?>(null)
    val selectedAnalyticsWindow = MutableStateFlow(AnalyticsTimeWindow.DAYS_30)

    fun setTimeRange(range: TimeRange) { selectedTimeRange.value = range }
    fun setMetric(metric: TrendMetric) { selectedMetric.value = metric }
    fun setSelectedSubjectForDetail(id: Long?) { selectedSubjectIdForDetail.value = id }
    fun setAnalyticsWindow(window: AnalyticsTimeWindow) { selectedAnalyticsWindow.value = window }

    val subjectStatsList: StateFlow<List<SubjectStats>> = combine(
        subjects,
        items
    ) { subs, allItems ->
        analyticsRepository.calculateSubjectStats(subs, allItems)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mockStats: StateFlow<MockStats> = mockTests.map { tests ->
        analyticsRepository.calculateMockStats(tests)
    }.stateIn(viewModelScope, SharingStarted.Lazily, MockStats())

    @Suppress("UNCHECKED_CAST")
    val advancedAnalytics: StateFlow<AdvancedAnalyticsSnapshot?> = combine(
        items,
        subjects,
        mistakes,
        mockTests,
        studySessions,
        goals,
        appSettings
    ) { args: Array<Any?> ->
        val allItems = args[0] as List<SyllabusItem>
        val allSubs = args[1] as List<Subject>
        val allMistakes = args[2] as List<MistakeEntry>
        val allMocks = args[3] as List<MockTest>
        val allSessions = args[4] as List<StudySession>
        val allGoals = args[5] as List<Goal>
        val settings = args[6] as AppSettings

        val snap = AdaptivePlanningEngine.createIntelligenceSnapshot(
            topics = allItems,
            subjects = allSubs,
            mistakes = allMistakes,
            mockTests = allMocks,
            sessions = allSessions,
            settings = settings,
            goals = allGoals
        )
        snap.advancedAnalytics
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val uiState: StateFlow<AnalyticsUiState> = combine(
        overallStats,
        subjectStatsList,
        mockStats,
        examPaceStats,
        advancedAnalytics
    ) { overall, subStats, mStats, paceStats, advAnalytics ->
        val timeRange = selectedTimeRange.value
        val metric = selectedMetric.value
        val subjectId = selectedSubjectIdForDetail.value
        val win = selectedAnalyticsWindow.value
        val sessions = studySessions.value
        val mocks = mockTests.value

        val points = analyticsRepository.calculateTrendDataPoints(sessions, overall, timeRange, metric)

        AnalyticsUiState(
            overallStats = overall,
            subjectStats = subStats,
            studySessions = sessions,
            mockStats = mStats,
            examPaceStats = paceStats,
            mockTests = mocks,
            selectedTimeRange = timeRange,
            selectedMetric = metric,
            selectedSubjectIdForDetail = subjectId,
            trendDataPoints = points,
            advancedAnalytics = advAnalytics,
            selectedAnalyticsWindow = win
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, AnalyticsUiState())
}

