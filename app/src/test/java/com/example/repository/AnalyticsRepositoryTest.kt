package com.example.repository

import com.example.data.model.*
import com.example.data.repository.AnalyticsRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AnalyticsRepositoryTest {

    private lateinit var analyticsRepository: AnalyticsRepository

    @Before
    fun setup() {
        analyticsRepository = AnalyticsRepository()
    }

    @Test
    fun calculateOverallStats_aggregatesCountsCorrectly() {
        val subjects = listOf(
            Subject(id = 1, name = "Math", code = "M", iconName = "m", colorHex = "#000")
        )
        val items = listOf(
            SyllabusItem(id = 10, subjectId = 1, title = "Sec 1", itemType = ItemType.SECTION),
            SyllabusItem(id = 11, subjectId = 1, parentId = 10, title = "Chap 1", itemType = ItemType.CHAPTER, status = ChapterStatus.COMPLETED),
            SyllabusItem(id = 12, subjectId = 1, parentId = 10, title = "Chap 2", itemType = ItemType.CHAPTER, status = ChapterStatus.IN_PROGRESS, confidence = 2)
        )

        val stats = analyticsRepository.calculateOverallStats(subjects, items, emptyList())

        assertEquals(1, stats.totalSubjects)
        assertEquals(1, stats.totalSections)
        assertEquals(2, stats.totalChapters)
        assertEquals(1, stats.completedChapters)
        assertEquals(1, stats.inProgressChapters)
        assertEquals(1, stats.weakChapters)
        assertEquals(50, stats.completionPercentage)
    }

    @Test
    fun calculateSubjectStats_aggregatesPYQAccuracyCorrectly() {
        val subjects = listOf(
            Subject(id = 1, name = "Quantitative Aptitude", code = "QA", iconName = "q", colorHex = "#111")
        )
        val items = listOf(
            SyllabusItem(
                id = 10, subjectId = 1, title = "Profit and Loss", itemType = ItemType.CHAPTER,
                pyqAttempted = 50, pyqCorrect = 40
            ),
            SyllabusItem(
                id = 11, subjectId = 1, title = "Simple Interest", itemType = ItemType.CHAPTER,
                pyqAttempted = 50, pyqCorrect = 30
            )
        )

        val subjectStats = analyticsRepository.calculateSubjectStats(subjects, items)

        assertEquals(1, subjectStats.size)
        val qaStats = subjectStats[0]
        assertEquals(100, qaStats.pyqAttempted)
        assertEquals(70, qaStats.pyqCorrect)
        // 70 / 100 * 100 = 70% accuracy
        assertEquals(70, qaStats.pyqAccuracy)
    }
}
