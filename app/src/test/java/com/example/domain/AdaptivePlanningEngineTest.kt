package com.example.domain

import com.example.data.intelligence.*
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class AdaptivePlanningEngineTest {

    private val baseTimestamp = 1700000000000L

    @Test
    fun testDetermineLastDaysMode() {
        val mode7 = AdaptivePlanningEngine.determineLastDaysExamMode(5)
        assertEquals(LastDaysExamMode.FINAL_CRUNCH, mode7)

        val mode15 = AdaptivePlanningEngine.determineLastDaysExamMode(15)
        assertEquals(LastDaysExamMode.INTENSIVE_REVISION, mode15)

        val mode30 = AdaptivePlanningEngine.determineLastDaysExamMode(45)
        assertEquals(LastDaysExamMode.ACCELERATION, mode30)

        val mode60 = AdaptivePlanningEngine.determineLastDaysExamMode(75)
        assertEquals(LastDaysExamMode.FOUNDATION, mode60)
    }

    @Test
    fun testExamReadinessDeterministicCalculation() {
        val topic1 = SyllabusItem(
            id = 1L,
            subjectId = 101L,
            title = "Percentages & Profit Loss",
            status = ChapterStatus.COMPLETED,
            completionPercentage = 100,
            confidence = 5,
            revisionCount = 3,
            pyqAttempted = 50,
            pyqCorrect = 45, // 90% accuracy
            lastStudiedTimestamp = baseTimestamp,
            nextRevisionTimestamp = baseTimestamp + 86400000L * 5
        )

        val topic2 = SyllabusItem(
            id = 2L,
            subjectId = 101L,
            title = "Algebra & Quadratics",
            status = ChapterStatus.IN_PROGRESS,
            completionPercentage = 80,
            confidence = 4,
            revisionCount = 1,
            pyqAttempted = 20,
            pyqCorrect = 16, // 80% accuracy
            lastStudiedTimestamp = baseTimestamp,
            nextRevisionTimestamp = baseTimestamp + 86400000L * 3
        )

        val topics = listOf(topic1, topic2)
        val intelList = topics.map { CoreIntelligenceEngine.calculateTopicIntelligence(it, emptyList(), baseTimestamp) }
        val mockTests = listOf(
            MockTest(
                id = 1L,
                testName = "Tier 1 Full Mock 1",
                testDateStr = "2026-08-01",
                marksScored = 160f,
                totalMarks = 200f,
                percentile = 92.5f
            )
        )

        val readiness = AdaptivePlanningEngine.calculateExamReadiness(
            topics = topics,
            intelligenceList = intelList,
            mockTests = mockTests,
            allMistakes = emptyList(),
            daysRemaining = 60
        )

        assertTrue(readiness.score in 50.0..100.0)
        assertTrue(readiness.level == ReadinessLevel.PREPARING || readiness.level == ReadinessLevel.STRONG || readiness.level == ReadinessLevel.EXAM_READY)
        assertEquals(50.0, readiness.components.syllabusCoverage, 0.01)
        assertTrue(readiness.confidence >= 50.0)
    }

    @Test
    fun testExamReadinessWithNoMockOrPyqData() {
        val topic = SyllabusItem(
            id = 1L,
            subjectId = 101L,
            title = "Modern Indian History",
            status = ChapterStatus.IN_PROGRESS,
            completionPercentage = 50,
            confidence = 3,
            revisionCount = 0,
            pyqAttempted = 0,
            pyqCorrect = 0
        )

        val topics = listOf(topic)
        val intelList = topics.map { CoreIntelligenceEngine.calculateTopicIntelligence(it, emptyList(), baseTimestamp) }

        val readiness = AdaptivePlanningEngine.calculateExamReadiness(
            topics = topics,
            intelligenceList = intelList,
            mockTests = emptyList(),
            allMistakes = emptyList(),
            daysRemaining = 30
        )

        assertTrue(readiness.score in 0.0..70.0)
        assertTrue(readiness.warnings.any { it.contains("mock", ignoreCase = true) })
    }

    @Test
    fun testExamPaceCalculationAheadAndBehind() {
        val settings = AppSettings(
            targetExamDateStr = "2026-12-31"
        )

        val topics = (1..10).map { i ->
            SyllabusItem(
                id = i.toLong(),
                subjectId = 1L,
                title = "Chapter $i",
                status = if (i <= 8) ChapterStatus.COMPLETED else ChapterStatus.NOT_STARTED,
                completionPercentage = if (i <= 8) 100 else 0
            )
        }

        val pace = AdaptivePlanningEngine.calculateExamPace(
            settings = settings,
            topics = topics,
            sessions = emptyList(),
            currentTimestamp = baseTimestamp
        )

        assertNotNull(pace.status)
        assertTrue(pace.daysRemaining > 0)
        assertEquals(80.0, pace.completedPercentage, 0.01)
    }

    @Test
    fun testGenerateTodaysPlanTimeBudgeting() {
        val topic1 = SyllabusItem(
            id = 1L,
            subjectId = 101L,
            title = "Weak Trigonometry",
            confidence = 1,
            status = ChapterStatus.IN_PROGRESS,
            pyqAttempted = 10,
            pyqCorrect = 2
        )

        val topic2 = SyllabusItem(
            id = 2L,
            subjectId = 101L,
            title = "Overdue Geometry",
            status = ChapterStatus.IN_PROGRESS,
            nextRevisionTimestamp = baseTimestamp - 10000L
        )

        val topic3 = SyllabusItem(
            id = 3L,
            subjectId = 101L,
            title = "Mastered Arithmetic",
            status = ChapterStatus.COMPLETED,
            completionPercentage = 100,
            confidence = 5,
            revisionCount = 4,
            pyqAttempted = 50,
            pyqCorrect = 48
        )

        val topics = listOf(topic1, topic2, topic3)
        val intelMap = topics.associate { it.id to CoreIntelligenceEngine.calculateTopicIntelligence(it, emptyList(), baseTimestamp) }
        val subMap = mapOf(101L to Subject(id = 101L, name = "Quantitative Aptitude"))

        val plan60 = AdaptivePlanningEngine.generateTodaysPlan(
            topics = topics,
            intelligenceMap = intelMap,
            subjectsMap = subMap,
            allMistakes = emptyList(),
            mockTests = emptyList(),
            availableMinutes = 60,
            daysRemaining = 60
        )

        assertTrue(plan60.items.isNotEmpty())
        assertTrue(plan60.totalMinutes <= 60)
    }

    @Test
    fun testGenerateWhyExplanation() {
        val topic = SyllabusItem(
            id = 1L,
            subjectId = 1L,
            title = "Reasoning Blood Relations",
            confidence = 2,
            pyqAttempted = 20,
            pyqCorrect = 8 // 40% accuracy
        )

        val intel = CoreIntelligenceEngine.calculateTopicIntelligence(topic)
        val explanation = AdaptivePlanningEngine.generateWhyExplanation(topic, intel)

        assertTrue(explanation.isNotEmpty())
        assertTrue(explanation.contains("PYQ", ignoreCase = true) || explanation.contains("confidence", ignoreCase = true) || explanation.contains("accuracy", ignoreCase = true))
    }

    @Test
    fun testSubjectHealthCalculation() {
        val sub = Subject(id = 10L, name = "General English")
        val chapters = listOf(
            SyllabusItem(id = 1L, subjectId = 10L, title = "Tenses", status = ChapterStatus.COMPLETED, completionPercentage = 100, confidence = 5, pyqAttempted = 30, pyqCorrect = 28, revisionCount = 2),
            SyllabusItem(id = 2L, subjectId = 10L, title = "Prepositions", status = ChapterStatus.COMPLETED, completionPercentage = 100, confidence = 4, pyqAttempted = 30, pyqCorrect = 26, revisionCount = 2)
        )
        val intelMap = chapters.associate { it.id to CoreIntelligenceEngine.calculateTopicIntelligence(it, emptyList(), baseTimestamp) }

        val health = AdaptivePlanningEngine.calculateSubjectHealth(
            subject = sub,
            topics = chapters,
            intelligenceMap = intelMap
        )

        assertEquals(SubjectHealthStatus.EXCELLENT, health.status)
        assertEquals(100.0, health.coveragePercentage, 0.01)
        assertTrue(health.averageMasteryScore >= 75.0)
    }
}
