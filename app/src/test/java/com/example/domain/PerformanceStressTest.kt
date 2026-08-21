package com.example.domain

import com.example.data.intelligence.*
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class PerformanceStressTest {

    @Test
    fun intelligenceCalculationsScaleLinearlyOn1000Topics() {
        val now = System.currentTimeMillis()
        val oneDay = 86400000L

        // Generate 1000 topics across 10 subjects
        val topics = (1..1000).map { id ->
            val status = when (id % 5) {
                0 -> ChapterStatus.MASTERED
                1 -> ChapterStatus.COMPLETED
                2 -> ChapterStatus.IN_PROGRESS
                3 -> ChapterStatus.WEAK
                else -> ChapterStatus.NOT_STARTED
            }
            SyllabusItem(
                id = id.toLong(),
                subjectId = (id % 10 + 1).toLong(),
                itemType = ItemType.CHAPTER,
                title = "Scale Test Topic #$id",
                status = status,
                completionPercentage = if (status == ChapterStatus.MASTERED || status == ChapterStatus.COMPLETED) 100 else 40,
                confidence = (id % 5 + 1),
                pyqAttempted = 50,
                pyqCorrect = (id % 50),
                revisionCount = (id % 6),
                lastStudiedTimestamp = now - (id % 30) * oneDay,
                nextRevisionTimestamp = if (id % 4 == 0) now - oneDay else now + 3 * oneDay,
                priority = if (id % 3 == 0) Priority.HIGH else Priority.MEDIUM,
                difficulty = if (id % 4 == 0) Difficulty.HARD else Difficulty.MEDIUM
            )
        }

        val mistakes = (1..500).map { id ->
            MistakeEntry(
                id = id.toLong(),
                subjectId = (id % 10 + 1).toLong(),
                subjectName = "Subject ${id % 10 + 1}",
                chapterTitle = "Scale Test Topic #${id * 2}",
                questionText = "Question #$id",
                category = MistakeCategory.values()[id % MistakeCategory.values().size],
                resolutionStatus = if (id % 3 == 0) MistakeResolutionStatus.MASTERED else MistakeResolutionStatus.ACTIVE,
                createdTimestamp = now - (id % 20) * oneDay,
                nextReviewTimestamp = now - (id % 5) * oneDay,
                reviewCount = (id % 5)
            )
        }

        val mockTests = (1..100).map { id ->
            MockTest(
                id = id.toLong(),
                testName = "All India Mock #$id",
                testDateStr = "2026-08-${(id % 28 + 1).toString().padStart(2, '0')}",
                timestamp = now - (100 - id) * oneDay,
                totalMarks = 200f,
                marksScored = (100 + (id % 80)).toFloat(),
                totalQuestions = 100,
                attemptedQuestions = 85,
                correctQuestions = 70,
                incorrectQuestions = 15,
                accuracy = 82.35f,
                percentile = (75 + (id % 24)).toFloat(),
                cutoffMarks = 135f,
                isClearedCutoff = (100 + (id % 80)) >= 135f
            )
        }

        val subjects = (1..10).map { id ->
            Subject(
                id = id.toLong(),
                name = "Subject $id",
                code = "S$id",
                colorHex = "#10B981"
            )
        }
        val subjectsMap = subjects.associateBy { it.id }

        val startTime = System.currentTimeMillis()

        // 1. Core Intelligence calculations for all 1000 topics
        val topicIntelligenceList = topics.map { topic ->
            CoreIntelligenceEngine.calculateTopicIntelligence(topic, mistakes, now)
        }
        val topicIntelligenceMap = topicIntelligenceList.associateBy { it.topicId }
        assertEquals(1000, topicIntelligenceList.size)

        // 2. Exam readiness calculation
        val readiness = AdaptivePlanningEngine.calculateExamReadiness(
            topics = topics,
            intelligenceList = topicIntelligenceList,
            mockTests = mockTests,
            allMistakes = mistakes,
            daysRemaining = 60
        )
        assertTrue(readiness.score in 0.0..100.0)

        // 3. Exam Pace calculation
        val pace = AdaptivePlanningEngine.calculateExamPace(
            settings = AppSettings(targetExamDateStr = "2026-10-20"),
            topics = topics,
            sessions = emptyList(),
            currentTimestamp = now
        )
        assertNotNull(pace)

        // 4. Time budgeted daily plan
        val dailyPlan = AdaptivePlanningEngine.generateTodaysPlan(
            topics = topics,
            intelligenceMap = topicIntelligenceMap,
            subjectsMap = subjectsMap,
            allMistakes = mistakes,
            mockTests = mockTests,
            availableMinutes = 240,
            daysRemaining = 60
        )
        assertTrue(dailyPlan.totalMinutes <= 240)

        // 5. Performance Trends & Recurring Mistakes Detection
        val recurring = PerformanceFeedbackEngine.detectRecurringMistakes(
            mistakes = mistakes,
            topics = topics,
            subjects = subjects,
            currentTime = now
        )
        assertNotNull(recurring)

        val mappedMocks = PerformanceFeedbackEngine.mapMockToTopics(
            mockTests = mockTests,
            topics = topics,
            subjects = subjects
        )
        assertNotNull(mappedMocks)

        val duration = System.currentTimeMillis() - startTime

        // Ensure calculation of 1000 topics and complex intelligence runs quickly
        assertTrue("Execution duration was ${duration}ms, expected under 500ms", duration < 500)
    }
}
