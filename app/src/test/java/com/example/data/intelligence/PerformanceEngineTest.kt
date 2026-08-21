package com.example.data.intelligence

import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class PerformanceEngineTest {

    private val sampleSubject = Subject(
        id = 101L,
        name = "Quantitative Aptitude",
        code = "MATH",
        description = "Core Mathematics",
        colorHex = "#3B82F6"
    )

    private val sampleTopic = SyllabusItem(
        id = 1L,
        subjectId = 101L,
        title = "Percentages & Profit Loss",
        itemType = ItemType.CHAPTER,
        status = ChapterStatus.IN_PROGRESS,
        completionPercentage = 60,
        confidence = 3,
        priority = Priority.HIGH,
        pyqTotal = 50,
        pyqAttempted = 20,
        pyqCorrect = 14,
        revisionCount = 2,
        lastStudiedTimestamp = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000L),
        nextRevisionTimestamp = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000L)
    )

    @Test
    fun testMockTestToTopicMapping() {
        val mock = MockTest(
            id = 501L,
            testName = "Percentages Chapter Test",
            testType = MockTestType.TOPIC_TEST,
            testDateStr = "2026-08-20",
            totalQuestions = 30,
            attemptedQuestions = 25,
            correctQuestions = 20,
            incorrectQuestions = 5,
            totalMarks = 60f,
            marksScored = 40f,
            accuracy = 80f,
            weakAreasIdentified = "Percentages & Profit Loss discount calculations",
            analysisNotes = "Needs speed improvement in multi-step percentages"
        )

        val results = PerformanceFeedbackEngine.mapMockToTopics(
            mockTests = listOf(mock),
            topics = listOf(sampleTopic),
            subjects = listOf(sampleSubject)
        )

        assertEquals(1, results.size)
        assertEquals(1L, results[0].topicId)
        assertEquals("Percentages & Profit Loss", results[0].topicTitle)
        assertEquals(80.0, results[0].accuracy, 0.01)
    }

    @Test
    fun testDetectRecurringMistakesLevels() {
        val currentTime = System.currentTimeMillis()
        val m1 = MistakeEntry(
            id = 1L,
            subjectId = 101L,
            subjectName = "Quantitative Aptitude",
            chapterTitle = "Percentages & Profit Loss",
            questionText = "Q1",
            correctAnswer = "20%",
            yourWrongAnswer = "25%",
            category = MistakeCategory.CONCEPT_GAP,
            resolutionStatus = MistakeResolutionStatus.ACTIVE,
            createdTimestamp = currentTime - 10000L
        )
        val m2 = m1.copy(id = 2L, category = MistakeCategory.CONCEPT_GAP, createdTimestamp = currentTime - 20000L)
        val m3 = m1.copy(id = 3L, category = MistakeCategory.CALCULATION_ERROR, createdTimestamp = currentTime - 30000L)
        val m4 = m1.copy(id = 4L, category = MistakeCategory.CONCEPT_GAP, createdTimestamp = currentTime - 40000L)

        // 1. Isolated
        val isolatedGroup = PerformanceFeedbackEngine.detectRecurringMistakes(
            mistakes = listOf(m1),
            topics = listOf(sampleTopic),
            subjects = listOf(sampleSubject),
            currentTime = currentTime
        )
        assertEquals(1, isolatedGroup.size)
        assertEquals(RecurringMistakeLevel.ISOLATED, isolatedGroup[0].level)

        // 2. Repeated (3 mistakes)
        val repeatedGroup = PerformanceFeedbackEngine.detectRecurringMistakes(
            mistakes = listOf(m1, m2, m3),
            topics = listOf(sampleTopic),
            subjects = listOf(sampleSubject),
            currentTime = currentTime
        )
        assertEquals(1, repeatedGroup.size)
        assertEquals(RecurringMistakeLevel.REPEATED, repeatedGroup[0].level)

        // 3. Recurring (4 mistakes)
        val recurringGroup = PerformanceFeedbackEngine.detectRecurringMistakes(
            mistakes = listOf(m1, m2, m3, m4),
            topics = listOf(sampleTopic),
            subjects = listOf(sampleSubject),
            currentTime = currentTime
        )
        assertEquals(1, recurringGroup.size)
        assertEquals(RecurringMistakeLevel.RECURRING, recurringGroup[0].level)
        assertEquals(MistakeCategory.CONCEPT_GAP, recurringGroup[0].primaryCategory)
        assertTrue(recurringGroup[0].recommendation.contains("theory", ignoreCase = true) || recurringGroup[0].recommendation.contains("concept", ignoreCase = true))
    }

    @Test
    fun testPerformanceTrends7_15_30Days() {
        val currSnap = PerformanceSnapshot(
            timestamp = System.currentTimeMillis(),
            topicId = 1L,
            topicTitle = "Percentages",
            masteryScore = 75.0,
            pyqAccuracy = 80.0,
            confidence = 80.0,
            mistakeCount = 2,
            activeMistakeCount = 1,
            revisionCount = 3,
            completion = 80.0
        )

        val prevSnap = PerformanceSnapshot(
            timestamp = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000L),
            topicId = 1L,
            topicTitle = "Percentages",
            masteryScore = 35.0, // weak topic (<40)
            pyqAccuracy = 50.0,
            confidence = 60.0,
            mistakeCount = 4,
            activeMistakeCount = 3,
            revisionCount = 1,
            completion = 60.0
        )

        val trend = PerformanceFeedbackEngine.calculatePerformanceTrend(
            window = TrendWindow.DAYS_7,
            currentSnapshots = listOf(currSnap),
            previousSnapshots = listOf(prevSnap)
        )

        assertEquals(TrendDirection.IMPROVING, trend.masteryTrend.direction)
        assertEquals(40.0, trend.masteryTrend.absoluteChange, 0.01)
        assertEquals(TrendDirection.IMPROVING, trend.pyqAccuracyTrend.direction)
        assertEquals(30.0, trend.pyqAccuracyTrend.absoluteChange, 0.01)
        assertEquals(TrendDirection.IMPROVING, trend.weakTopicsTrend.direction)
    }

    @Test
    fun testTopicImprovementAnalysis() {
        val before = PerformanceSnapshot(
            timestamp = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000L),
            topicId = 1L,
            topicTitle = "Percentages",
            masteryScore = 45.0,
            pyqAccuracy = 50.0,
            confidence = 40.0,
            mistakeCount = 5,
            activeMistakeCount = 4,
            revisionCount = 0,
            completion = 50.0
        )

        val after = PerformanceSnapshot(
            timestamp = System.currentTimeMillis(),
            topicId = 1L,
            topicTitle = "Percentages",
            masteryScore = 70.0,
            pyqAccuracy = 80.0,
            confidence = 80.0,
            mistakeCount = 2,
            activeMistakeCount = 1,
            revisionCount = 2,
            completion = 80.0
        )

        val improvement = PerformanceFeedbackEngine.analyzeTopicImprovement(1L, "Percentages", before, after)
        assertEquals(25.0, improvement.masteryDelta, 0.01)
        assertEquals(30.0, improvement.pyqDelta, 0.01)
        assertEquals(3, improvement.mistakeDelta)
        assertEquals(ImprovementOutcome.SIGNIFICANT_IMPROVEMENT, improvement.outcome)
    }

    @Test
    fun testStudyEffectivenessCalculation() {
        val before = PerformanceSnapshot(
            timestamp = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000L),
            topicId = 1L,
            topicTitle = "Percentages",
            masteryScore = 50.0,
            pyqAccuracy = 55.0,
            confidence = 60.0,
            mistakeCount = 3,
            activeMistakeCount = 2,
            revisionCount = 1,
            completion = 60.0
        )

        // Case A: High Effectiveness
        val afterHigh = before.copy(
            masteryScore = 75.0,
            pyqAccuracy = 85.0,
            activeMistakeCount = 0
        )
        val effHigh = PerformanceFeedbackEngine.calculateStudyEffectiveness(
            topicId = 1L,
            topicTitle = "Percentages",
            before = before,
            after = afterHigh,
            studyTimeMinutes = 60,
            revisionCount = 1,
            pyqAttempts = 20,
            retentionState = RetentionState.STRONG
        )
        assertTrue(effHigh.score >= 70.0)
        assertEquals(EffectivenessLevel.HIGH, effHigh.level)

        // Case B: Low Effectiveness (High Study Time, No Gain)
        val afterLow = before.copy(
            masteryScore = 50.5,
            pyqAccuracy = 55.0,
            activeMistakeCount = 2
        )
        val effLow = PerformanceFeedbackEngine.calculateStudyEffectiveness(
            topicId = 1L,
            topicTitle = "Percentages",
            before = before,
            after = afterLow,
            studyTimeMinutes = 180,
            revisionCount = 1,
            pyqAttempts = 0,
            retentionState = RetentionState.WEAK
        )
        assertEquals(EffectivenessLevel.LOW, effLow.level)
        assertTrue(effLow.diagnosisText.contains("not improve", ignoreCase = true) || effLow.diagnosisText.contains("approach", ignoreCase = true))
    }

    @Test
    fun testRetentionStrengthValidation() {
        val topicFresh = sampleTopic.copy(
            revisionCount = 2,
            lastStudiedTimestamp = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000L),
            nextRevisionTimestamp = System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000L),
            pyqAttempted = 30,
            pyqCorrect = 27 // 90%
        )

        val retentionFresh = PerformanceFeedbackEngine.calculateRetentionStrength(topicFresh, emptyList())
        assertEquals(RetentionState.STRONG, retentionFresh.state)

        val topicDecayed = sampleTopic.copy(
            revisionCount = 0,
            lastStudiedTimestamp = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000L),
            nextRevisionTimestamp = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000L),
            pyqAttempted = 20,
            pyqCorrect = 8 // 40%
        )
        val retentionDecayed = PerformanceFeedbackEngine.calculateRetentionStrength(topicDecayed, emptyList())
        assertEquals(RetentionState.WEAK, retentionDecayed.state)
    }

    @Test
    fun testActionablePerformanceRecommendations() {
        val baseIntel = CoreIntelligenceEngine.calculateTopicIntelligence(sampleTopic)

        // Case 1: Low PYQ + repeated concept mistakes
        val lowPyqTopic = sampleTopic.copy(pyqAttempted = 20, pyqCorrect = 8)
        val lowIntel = CoreIntelligenceEngine.calculateTopicIntelligence(lowPyqTopic)
        val recurringMistake = RecurringMistakeGroup(
            topicId = 1L,
            topicTitle = "Percentages",
            subjectId = 101L,
            subjectName = "Math",
            totalOccurrences = 4,
            recentOccurrences = 2,
            categories = listOf(MistakeCategory.CONCEPT_GAP),
            repeatedCategories = mapOf(MistakeCategory.CONCEPT_GAP to 3),
            primaryCategory = MistakeCategory.CONCEPT_GAP,
            lastOccurrence = System.currentTimeMillis(),
            recurrenceScore = 80.0,
            level = RecurringMistakeLevel.RECURRING
        )

        val rec1 = PerformanceFeedbackEngine.generatePerformanceRecommendation(
            topic = lowPyqTopic,
            intel = lowIntel,
            recurringMistake = recurringMistake
        )
        assertEquals("Prioritize concept review followed by targeted PYQs.", rec1.actionableAdvice)

        // Case 2: High PYQ + weak retention
        val highPyqTopic = sampleTopic.copy(pyqAttempted = 30, pyqCorrect = 27)
        val highIntel = CoreIntelligenceEngine.calculateTopicIntelligence(highPyqTopic)
        val weakRetention = RetentionValidationResult(
            topicId = 1L,
            topicTitle = "Percentages",
            initialAccuracy = 90.0,
            followUpAccuracy = 50.0,
            daysBetweenChecks = 10.0,
            state = RetentionState.WEAK,
            explanation = "Decayed",
            hasSufficientData = true
        )

        val rec2 = PerformanceFeedbackEngine.generatePerformanceRecommendation(
            topic = highPyqTopic,
            intel = highIntel,
            retention = weakRetention
        )
        assertEquals("Reduce passive study and increase spaced retrieval.", rec2.actionableAdvice)

        // Case 3: Mastered + Strong Retention
        val masteredTopic = sampleTopic.copy(completionPercentage = 100, pyqAttempted = 50, pyqCorrect = 48, confidence = 5, revisionCount = 3)
        val masteredIntel = CoreIntelligenceEngine.calculateTopicIntelligence(masteredTopic)
        val strongRetention = RetentionValidationResult(
            topicId = 1L,
            topicTitle = "Percentages",
            initialAccuracy = 96.0,
            followUpAccuracy = 94.0,
            daysBetweenChecks = 14.0,
            state = RetentionState.STRONG,
            explanation = "Strong",
            hasSufficientData = true
        )

        val rec3 = PerformanceFeedbackEngine.generatePerformanceRecommendation(
            topic = masteredTopic,
            intel = masteredIntel,
            retention = strongRetention
        )
        assertEquals("Keep this topic on maintenance revision.", rec3.actionableAdvice)
    }

    @Test
    fun testFeedbackLoopEnhancesTopicIntelligence() {
        val baseIntel = CoreIntelligenceEngine.calculateTopicIntelligence(sampleTopic)
        val recurringMistake = RecurringMistakeGroup(
            topicId = 1L,
            topicTitle = "Percentages",
            subjectId = 101L,
            subjectName = "Math",
            totalOccurrences = 4,
            recentOccurrences = 2,
            categories = listOf(MistakeCategory.CONCEPT_GAP),
            repeatedCategories = mapOf(MistakeCategory.CONCEPT_GAP to 3),
            primaryCategory = MistakeCategory.CONCEPT_GAP,
            lastOccurrence = System.currentTimeMillis(),
            recurrenceScore = 80.0,
            level = RecurringMistakeLevel.RECURRING
        )
        val weakRetention = RetentionValidationResult(
            topicId = 1L,
            topicTitle = "Percentages",
            initialAccuracy = 80.0,
            followUpAccuracy = 50.0,
            daysBetweenChecks = 10.0,
            state = RetentionState.WEAK,
            explanation = "Decayed",
            hasSufficientData = true
        )

        val enhanced = PerformanceFeedbackEngine.enhanceTopicIntelligenceWithFeedback(
            baseIntel = baseIntel,
            recurringMistake = recurringMistake,
            retention = weakRetention,
            effectiveness = null
        )

        assertTrue("Weakness score should increase with recurring mistakes & weak retention", enhanced.weaknessScore > baseIntel.weaknessScore)
        assertTrue("Priority score should increase with recurring mistakes & weak retention", enhanced.priorityScore > baseIntel.priorityScore)
    }

    @Test
    fun testWeeklyPerformanceReportGeneration() {
        val sessions = listOf(
            StudySession(
                id = 1L,
                subjectId = 101L,
                subjectName = "Math",
                chapterId = 1L,
                chapterTitle = "Percentages & Profit Loss",
                durationSeconds = 3600L,
                timestamp = System.currentTimeMillis() - 100000L,
                notes = "Practiced PYQs"
            )
        )

        val intelMap = mapOf(1L to CoreIntelligenceEngine.calculateTopicIntelligence(sampleTopic))

        val report = PerformanceFeedbackEngine.generateWeeklyPerformanceReport(
            topics = listOf(sampleTopic),
            currentIntelMap = intelMap,
            studySessions = sessions
        )

        assertTrue(report.hasSufficientData)
        assertEquals(60, report.totalStudyTimeMinutes)
        assertNotNull(report.headlineSummary)
        assertTrue(report.actionableTakeaways.isNotEmpty())
    }
}
