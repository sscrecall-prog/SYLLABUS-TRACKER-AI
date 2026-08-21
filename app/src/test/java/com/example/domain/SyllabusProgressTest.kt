package com.example.domain

import com.example.data.model.*
import com.example.data.repository.AnalyticsRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SyllabusProgressTest {

    private lateinit var analyticsRepository: AnalyticsRepository

    @Before
    fun setup() {
        analyticsRepository = AnalyticsRepository()
    }

    @Test
    fun calculateOverallStats_emptySyllabus_returnsZeroPercentageWithoutDivisionByZero() {
        val subjects = emptyList<Subject>()
        val items = emptyList<SyllabusItem>()
        val sessions = emptyList<StudySession>()

        val stats = analyticsRepository.calculateOverallStats(subjects, items, sessions)

        assertEquals(0, stats.totalChapters)
        assertEquals(0, stats.completedChapters)
        assertEquals(0, stats.completionPercentage)
        assertFalse(stats.completionPercentage.toFloat().isNaN())
        assertFalse(stats.completionPercentage.toFloat().isInfinite())
    }

    @Test
    fun calculateOverallStats_zeroCompletedTopics_returnsZeroPercent() {
        val subjects = listOf(Subject(id = 1, name = "Math", code = "M", iconName = "icon", colorHex = "#000"))
        val items = listOf(
            SyllabusItem(id = 10, subjectId = 1, title = "Ch 1", itemType = ItemType.CHAPTER, status = ChapterStatus.IN_PROGRESS),
            SyllabusItem(id = 11, subjectId = 1, title = "Ch 2", itemType = ItemType.CHAPTER, status = ChapterStatus.IN_PROGRESS)
        )

        val stats = analyticsRepository.calculateOverallStats(subjects, items, emptyList())

        assertEquals(2, stats.totalChapters)
        assertEquals(0, stats.completedChapters)
        assertEquals(2, stats.inProgressChapters)
        assertEquals(0, stats.completionPercentage)
    }

    @Test
    fun calculateOverallStats_oneCompletedTopicOutOfThree_calculatesCorrectPercentageAndRounding() {
        val subjects = listOf(Subject(id = 1, name = "Math", code = "M", iconName = "icon", colorHex = "#000"))
        val items = listOf(
            SyllabusItem(id = 10, subjectId = 1, title = "Ch 1", itemType = ItemType.CHAPTER, status = ChapterStatus.COMPLETED),
            SyllabusItem(id = 11, subjectId = 1, title = "Ch 2", itemType = ItemType.CHAPTER, status = ChapterStatus.NOT_STARTED),
            SyllabusItem(id = 12, subjectId = 1, title = "Ch 3", itemType = ItemType.CHAPTER, status = ChapterStatus.NOT_STARTED)
        )

        val stats = analyticsRepository.calculateOverallStats(subjects, items, emptyList())

        assertEquals(3, stats.totalChapters)
        assertEquals(1, stats.completedChapters)
        // 1 / 3 * 100 = 33.333% -> truncated to Int 33
        assertEquals(33, stats.completionPercentage)
    }

    @Test
    fun calculateOverallStats_fullyCompletedSyllabus_returnsOneHundredPercent() {
        val subjects = listOf(Subject(id = 1, name = "Math", code = "M", iconName = "icon", colorHex = "#000"))
        val items = listOf(
            SyllabusItem(id = 10, subjectId = 1, title = "Ch 1", itemType = ItemType.CHAPTER, status = ChapterStatus.COMPLETED),
            SyllabusItem(id = 11, subjectId = 1, title = "Ch 2", itemType = ItemType.CHAPTER, status = ChapterStatus.MASTERED)
        )

        val stats = analyticsRepository.calculateOverallStats(subjects, items, emptyList())

        assertEquals(2, stats.totalChapters)
        assertEquals(2, stats.completedChapters)
        assertEquals(100, stats.completionPercentage)
        assertEquals(1, stats.masteredChapters)
    }

    @Test
    fun calculateSubjectStats_multipleSubjectsAndNestedHierarchy_calculatesPerSubjectAccurately() {
        val sub1 = Subject(id = 1, name = "Math", code = "M", iconName = "icon", colorHex = "#000")
        val sub2 = Subject(id = 2, name = "English", code = "E", iconName = "icon", colorHex = "#111")
        val subjects = listOf(sub1, sub2)

        val items = listOf(
            // Sub 1 hierarchy
            SyllabusItem(id = 100, subjectId = 1, title = "Section 1", itemType = ItemType.SECTION),
            SyllabusItem(id = 101, subjectId = 1, parentId = 100, title = "Ch 1", itemType = ItemType.CHAPTER, status = ChapterStatus.COMPLETED, studyTimeMinutes = 60),
            SyllabusItem(id = 102, subjectId = 1, parentId = 100, title = "Ch 2", itemType = ItemType.CHAPTER, status = ChapterStatus.NOT_STARTED, studyTimeMinutes = 0),

            // Sub 2 hierarchy
            SyllabusItem(id = 200, subjectId = 2, title = "Ch 1", itemType = ItemType.CHAPTER, status = ChapterStatus.COMPLETED, studyTimeMinutes = 45)
        )

        val subjectStatsList = analyticsRepository.calculateSubjectStats(subjects, items)

        assertEquals(2, subjectStatsList.size)

        val mathStats = subjectStatsList.find { it.subject.id == 1L }
        assertNotNull(mathStats)
        assertEquals(1, mathStats?.totalSections)
        assertEquals(2, mathStats?.totalChapters)
        assertEquals(1, mathStats?.completedChapters)
        assertEquals(50, mathStats?.completionPercentage)
        assertEquals(60, mathStats?.totalStudyMinutes)

        val englishStats = subjectStatsList.find { it.subject.id == 2L }
        assertNotNull(englishStats)
        assertEquals(1, englishStats?.totalChapters)
        assertEquals(1, englishStats?.completedChapters)
        assertEquals(100, englishStats?.completionPercentage)
        assertEquals(45, englishStats?.totalStudyMinutes)
    }
}
