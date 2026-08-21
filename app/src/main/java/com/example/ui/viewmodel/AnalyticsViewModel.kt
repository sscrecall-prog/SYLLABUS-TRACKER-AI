package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
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

    fun setTimeRange(range: TimeRange) { selectedTimeRange.value = range }
    fun setMetric(metric: TrendMetric) { selectedMetric.value = metric }
    fun setSelectedSubjectForDetail(id: Long?) { selectedSubjectIdForDetail.value = id }

    val subjectStatsList: StateFlow<List<SubjectStats>> = combine(
        subjects,
        items
    ) { subs, allItems ->
        analyticsRepository.calculateSubjectStats(subs, allItems)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mockStats: StateFlow<MockStats> = mockTests.map { tests ->
        analyticsRepository.calculateMockStats(tests)
    }.stateIn(viewModelScope, SharingStarted.Lazily, MockStats())

    val uiState: StateFlow<AnalyticsUiState> = combine(
        overallStats,
        subjectStatsList,
        mockStats,
        examPaceStats,
        combine(
            combine(selectedTimeRange, selectedMetric, selectedSubjectIdForDetail) { r, m, s ->
                Triple(r, m, s)
            },
            studySessions,
            mockTests
        ) { sel, sessions, mocks ->
            Triple(sel, sessions, mocks)
        }
    ) { overall, subStats, mStats, paceStats, innerTuple ->
        val (sel, sessions, mocks) = innerTuple
        val (timeRange, metric, subjectId) = sel

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
            trendDataPoints = points
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, AnalyticsUiState())
}
