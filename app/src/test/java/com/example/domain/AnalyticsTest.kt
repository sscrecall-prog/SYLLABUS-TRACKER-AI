package com.example.domain

import com.example.data.model.*
import com.example.data.repository.AnalyticsRepository
import com.example.ui.viewmodel.OverallStats
import com.example.ui.viewmodel.TimeRange
import com.example.ui.viewmodel.TrendMetric
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsTest {

    private lateinit var analyticsRepository: AnalyticsRepository

    @Before
    fun setup() {
        analyticsRepository = AnalyticsRepository()
    }

    @Test
    fun calculateOverallStats_emptyDataset_handlesGracefully() {
        val stats = analyticsRepository.calculateOverallStats(emptyList(), emptyList(), emptyList())

        assertEquals(0, stats.totalSubjects)
        assertEquals(0, stats.totalChapters)
        assertEquals(0, stats.totalStudyMinutes)
        assertEquals(0, stats.todayStudyMinutes)
        assertTrue(stats.currentStreakDays >= 1)
    }

    @Test
    fun calculateOverallStats_singleRecordDataset_calculatesStudyTimeAndStreak() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayMillis = System.currentTimeMillis()

        val subjects = listOf(Subject(id = 1, name = "Reasoning", code = "REAS", iconName = "icon", colorHex = "#123"))
        val items = listOf(
            SyllabusItem(id = 10, subjectId = 1, title = "Puzzles", itemType = ItemType.CHAPTER, status = ChapterStatus.IN_PROGRESS, studyTimeMinutes = 90)
        )
        val sessions = listOf(
            StudySession(id = 1, subjectId = 1, subjectName = "Reasoning", durationSeconds = 5400, timestamp = todayMillis)
        )

        val stats = analyticsRepository.calculateOverallStats(subjects, items, sessions)

        assertEquals(1, stats.totalSubjects)
        assertEquals(1, stats.totalChapters)
        assertEquals(90, stats.totalStudyMinutes)
        assertEquals(90, stats.todayStudyMinutes)
        assertEquals(1, stats.currentStreakDays)
    }

    @Test
    fun calculateExamPaceStats_multipleChaptersAndTargetDate_calculatesPaceAndVelocity() {
        val targetDateStr = "2026-10-01"
        val settings = AppSettings(
            targetExam = "SSC CGL 2026",
            targetExamDateStr = targetDateStr,
            dailyTargetMinutes = 180
        )
        val subjects = listOf(
            Subject(id = 1, name = "Math", code = "M", iconName = "i", colorHex = "#000"),
            Subject(id = 2, name = "English", code = "E", iconName = "i", colorHex = "#111")
        )
        val items = listOf(
            SyllabusItem(id = 10, subjectId = 1, title = "Ch 1", itemType = ItemType.CHAPTER, status = ChapterStatus.COMPLETED),
            SyllabusItem(id = 11, subjectId = 1, title = "Ch 2", itemType = ItemType.CHAPTER, status = ChapterStatus.COMPLETED),
            SyllabusItem(id = 12, subjectId = 1, title = "Ch 3", itemType = ItemType.CHAPTER, status = ChapterStatus.NOT_STARTED),
            SyllabusItem(id = 20, subjectId = 2, title = "Ch 1", itemType = ItemType.CHAPTER, status = ChapterStatus.NOT_STARTED)
        )

        val paceStats = analyticsRepository.calculateExamPaceStats(settings, items, subjects, emptyList())

        assertEquals("SSC CGL 2026", paceStats.examName)
        assertEquals(4, paceStats.totalChapters)
        assertEquals(2, paceStats.completedChapters)
        assertEquals(2, paceStats.remainingChapters)
        assertTrue(paceStats.daysRemaining > 0)
        assertTrue(paceStats.requiredPaceChaptersPerDay >= 0f)
        assertEquals(2, paceStats.subjectPaceBreakdown.size)
    }

    @Test
    fun calculateTrendDataPoints_validSessions_returnsRequestedDataPointsCount() {
        val sessions = listOf(
            StudySession(id = 1, subjectId = 1, subjectName = "General", durationSeconds = 3600, timestamp = System.currentTimeMillis())
        )
        val overall = OverallStats(
            totalSubjects = 1, totalSections = 0, totalChapters = 10,
            completedChapters = 5, inProgressChapters = 2, notStartedChapters = 3,
            weakChapters = 0, revisionDueChapters = 0, masteredChapters = 1,
            completionPercentage = 50, totalStudyMinutes = 300, currentStreakDays = 3,
            longestStreakDays = 8, todayStudyMinutes = 60
        )

        val trendPoints = analyticsRepository.calculateTrendDataPoints(sessions, overall, TimeRange.LAST_7_DAYS, TrendMetric.STUDY_HOURS)

        assertEquals(7, trendPoints.size)
        assertEquals("hrs", trendPoints[0].unit)
        assertTrue(trendPoints.last().value >= 0f)
    }

    @Test
    fun calculateMistakeStats_emptyAndNonEmpty_calculatesCategoryBreakdownAndAccuracy() {
        val emptyStats = analyticsRepository.calculateMistakeStats(emptyList())
        assertEquals(0, emptyStats.totalMistakesCount)
        assertEquals(0, emptyStats.resolutionRatePercent)

        val mistakes = listOf(
            MistakeEntry(id = 1, questionText = "Q1", subjectId = 1, subjectName = "Math", category = MistakeCategory.CALCULATION_ERROR, resolutionStatus = MistakeResolutionStatus.UNDERSTOOD),
            MistakeEntry(id = 2, questionText = "Q2", subjectId = 1, subjectName = "Math", category = MistakeCategory.SILLY_MISTAKE, resolutionStatus = MistakeResolutionStatus.MASTERED),
            MistakeEntry(id = 3, questionText = "Q3", subjectId = 2, subjectName = "English", category = MistakeCategory.CONCEPT_GAP, resolutionStatus = MistakeResolutionStatus.ACTIVE)
        )

        val stats = analyticsRepository.calculateMistakeStats(mistakes)

        assertEquals(3, stats.totalMistakesCount)
        assertEquals(1, stats.activeMistakesCount)
        assertEquals(1, stats.understoodCount)
        assertEquals(1, stats.masteredCount)
        // 2 resolved out of 3 total -> 66%
        assertEquals(66, stats.resolutionRatePercent)
        assertEquals("Math", stats.mostVulnerableSubject)
    }
}
