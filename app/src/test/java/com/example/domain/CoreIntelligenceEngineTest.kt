package com.example.domain

import com.example.data.intelligence.*
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class CoreIntelligenceEngineTest {

    // -------------------------------------------------------------
    // 1. PYQ ENGINE TESTS
    // -------------------------------------------------------------

    @Test
    fun calculatePYQPerformance_zeroAttempts_returnsNoDataWithoutNaN() {
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "New Topic",
            pyqAttempted = 0,
            pyqCorrect = 0
        )

        val perf = CoreIntelligenceEngine.calculatePYQPerformance(topic)

        assertEquals(0, perf.attempted)
        assertEquals(0, perf.correct)
        assertEquals(0, perf.incorrect)
        assertEquals(0.0, perf.accuracy, 0.001)
        assertEquals(PYQStatus.NO_DATA, perf.status)
        assertFalse(perf.accuracy.isNaN())
        assertFalse(perf.accuracy.isInfinite())
    }

    @Test
    fun calculatePYQPerformance_tenOfTen_returnsOneHundredPercentExcellent() {
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Percentage",
            pyqAttempted = 10,
            pyqCorrect = 10
        )

        val perf = CoreIntelligenceEngine.calculatePYQPerformance(topic)

        assertEquals(10, perf.attempted)
        assertEquals(10, perf.correct)
        assertEquals(0, perf.incorrect)
        assertEquals(100.0, perf.accuracy, 0.001)
        assertEquals(PYQStatus.EXCELLENT, perf.status)
    }

    @Test
    fun calculatePYQPerformance_fiveOfTen_returnsFiftyPercentAverage() {
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Algebra",
            pyqAttempted = 10,
            pyqCorrect = 5
        )

        val perf = CoreIntelligenceEngine.calculatePYQPerformance(topic)

        assertEquals(10, perf.attempted)
        assertEquals(5, perf.correct)
        assertEquals(5, perf.incorrect)
        assertEquals(50.0, perf.accuracy, 0.001)
        assertEquals(PYQStatus.AVERAGE, perf.status)
    }

    @Test
    fun calculatePYQPerformance_zeroOfTen_returnsZeroPercentPoor() {
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Geometry",
            pyqAttempted = 10,
            pyqCorrect = 0
        )

        val perf = CoreIntelligenceEngine.calculatePYQPerformance(topic)

        assertEquals(10, perf.attempted)
        assertEquals(0, perf.correct)
        assertEquals(10, perf.incorrect)
        assertEquals(0.0, perf.accuracy, 0.001)
        assertEquals(PYQStatus.POOR, perf.status)
    }

    @Test
    fun calculatePYQPerformance_negativeOrInvalidValues_handlesGracefully() {
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Invalid Topic",
            pyqAttempted = -5,
            pyqCorrect = 100 // correct > attempted case
        )

        val perf = CoreIntelligenceEngine.calculatePYQPerformance(topic)

        assertEquals(0, perf.attempted)
        assertEquals(0, perf.correct)
        assertEquals(0.0, perf.accuracy, 0.001)
        assertEquals(PYQStatus.NO_DATA, perf.status)
    }

    // -------------------------------------------------------------
    // 2. CONFIDENCE ENGINE TESTS
    // -------------------------------------------------------------

    @Test
    fun calculateConfidenceInfo_validScales_normalizesCorrectly() {
        for (i in 1..5) {
            val topic = SyllabusItem(id = 1, subjectId = 10, title = "Topic", confidence = i)
            val conf = CoreIntelligenceEngine.calculateConfidenceInfo(topic)
            assertEquals(i, conf.value)
            assertEquals(i * 20.0, conf.normalized, 0.001)
        }
    }

    @Test
    fun calculateConfidenceInfo_outOfRange_defaultsToAverage() {
        val topicZero = SyllabusItem(id = 1, subjectId = 10, title = "Topic", confidence = 0)
        val confZero = CoreIntelligenceEngine.calculateConfidenceInfo(topicZero)
        assertEquals(3, confZero.value)
        assertEquals(60.0, confZero.normalized, 0.001)

        val topicSeven = SyllabusItem(id = 1, subjectId = 10, title = "Topic", confidence = 7)
        val confSeven = CoreIntelligenceEngine.calculateConfidenceInfo(topicSeven)
        assertEquals(3, confSeven.value)
        assertEquals(60.0, confSeven.normalized, 0.001)
    }

    // -------------------------------------------------------------
    // 3. MISTAKE CONTROL ENGINE TESTS
    // -------------------------------------------------------------

    @Test
    fun calculateMistakeControl_noMistakes_returnsOneHundredScore() {
        val topic = SyllabusItem(id = 1, subjectId = 10, title = "Trigonometry")
        val result = CoreIntelligenceEngine.calculateMistakeControl(topic, emptyList())

        assertEquals(100.0, result.score, 0.001)
        assertEquals(0, result.totalMistakes)
        assertEquals(0, result.activeMistakes)
        assertEquals(0, result.repeatedMistakes)
        assertEquals(0, result.conceptGaps)
    }

    @Test
    fun calculateMistakeControl_activeAndRepeatedConceptMistakes_appliesStrongPenalties() {
        val topic = SyllabusItem(id = 1, subjectId = 10, title = "Trigonometry")
        val mistakes = listOf(
            MistakeEntry(
                id = 101,
                questionText = "Q1",
                subjectId = 10,
                subjectName = "Math",
                chapterTitle = "Trigonometry",
                category = MistakeCategory.CONCEPT_GAP,
                resolutionStatus = MistakeResolutionStatus.ACTIVE,
                reviewCount = 2
            ),
            MistakeEntry(
                id = 102,
                questionText = "Q2",
                subjectId = 10,
                subjectName = "Math",
                chapterTitle = "Trigonometry",
                category = MistakeCategory.SILLY_MISTAKE,
                resolutionStatus = MistakeResolutionStatus.ACTIVE
            )
        )

        val result = CoreIntelligenceEngine.calculateMistakeControl(topic, mistakes)

        assertEquals(2, result.totalMistakes)
        assertEquals(2, result.activeMistakes)
        assertEquals(1, result.conceptGaps)
        assertEquals(1, result.repeatedMistakes)
        // Score: 100 - (2*15 [active] + 1*10 [repeated] + 1*10 [concept gap]) = 100 - 50 = 50.0
        assertEquals(50.0, result.score, 0.001)
    }

    @Test
    fun calculateMistakeControl_masteredMistakes_doesNotPenalizeScore() {
        val topic = SyllabusItem(id = 1, subjectId = 10, title = "History")
        val mistakes = listOf(
            MistakeEntry(
                id = 101,
                questionText = "Q1",
                subjectId = 10,
                subjectName = "History",
                chapterTitle = "History",
                category = MistakeCategory.CONCEPT_GAP,
                resolutionStatus = MistakeResolutionStatus.MASTERED
            )
        )

        val result = CoreIntelligenceEngine.calculateMistakeControl(topic, mistakes)

        assertEquals(1, result.totalMistakes)
        assertEquals(0, result.activeMistakes)
        assertEquals(0, result.conceptGaps)
        assertEquals(100.0, result.score, 0.001)
    }

    // -------------------------------------------------------------
    // 4. REVISION STRENGTH & ADAPTIVE INTERVAL TESTS
    // -------------------------------------------------------------

    @Test
    fun calculateRevisionStrength_newTopic_returnsZeroScore() {
        val topic = SyllabusItem(id = 1, subjectId = 10, title = "Ch 1", revisionCount = 0)
        val result = CoreIntelligenceEngine.calculateRevisionStrength(topic)

        assertEquals(0.0, result.score, 0.001)
        assertEquals(0, result.revisionCount)
        assertFalse(result.overdue)
        assertNull(result.daysSinceRevision)
    }

    @Test
    fun calculateRevisionStrength_overdueTopic_appliesOverduePenalty() {
        val now = System.currentTimeMillis()
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Ch 1",
            revisionCount = 2,
            lastStudiedTimestamp = now - 10 * 24 * 60 * 60 * 1000L,
            nextRevisionTimestamp = now - 2 * 24 * 60 * 60 * 1000L, // 2 days overdue
            status = ChapterStatus.REVISION_DUE
        )

        val result = CoreIntelligenceEngine.calculateRevisionStrength(topic, now)

        assertTrue(result.overdue)
        // 2 revisions base = 75.0, overdue penalty = -25.0 -> 50.0
        assertEquals(50.0, result.score, 0.001)
    }

    @Test
    fun calculateNextRevision_strongPerformance_expandsInterval() {
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Algebra",
            confidence = 5,
            pyqAttempted = 20,
            pyqCorrect = 19, // 95% accuracy
            revisionCount = 2
        )

        val now = System.currentTimeMillis()
        val nextRevTimestamp = SpacedRepetitionEngine.calculateNextRevision(topic)

        assertTrue(nextRevTimestamp > now)
        val daysAdded = (nextRevTimestamp - now).toDouble() / (1000 * 60 * 60 * 24)
        // Base 7 days * adaptive multiplier 2.5 * confidence 1.5 = ~26 days
        assertTrue("Expected expanded interval > 15 days, got $daysAdded", daysAdded >= 15.0)
    }

    @Test
    fun calculateNextRevision_weakPerformance_shortensInterval() {
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Geometry",
            confidence = 1,
            pyqAttempted = 20,
            pyqCorrect = 6, // 30% accuracy (<40% = very weak)
            revisionCount = 2
        )

        val now = System.currentTimeMillis()
        val nextRevTimestamp = SpacedRepetitionEngine.calculateNextRevision(topic)

        assertTrue(nextRevTimestamp >= now)
        val daysAdded = (nextRevTimestamp - now).toDouble() / (1000 * 60 * 60 * 24)
        // Very weak accuracy forces short interval ~ 1-2 days
        assertTrue("Expected shortened interval <= 3 days, got $daysAdded", daysAdded <= 3.0)
    }

    // -------------------------------------------------------------
    // 5. MASTERY SCORE & NORMALIZATION TESTS
    // -------------------------------------------------------------

    @Test
    fun calculateMasteryScore_zeroDataNewTopic_doesNotPenalizeForMissingPYQ() {
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Brand New Topic",
            completionPercentage = 0,
            confidence = 3,
            pyqAttempted = 0,
            revisionCount = 0
        )

        val result = CoreIntelligenceEngine.calculateMasteryScore(topic)

        assertFalse(result.score.isNaN())
        assertFalse(result.score.isInfinite())
        assertTrue(result.score >= 0.0 && result.score <= 100.0)
        assertEquals(MasteryLevel.WEAK, result.level)
        assertEquals(-1.0, result.components.pyqAccuracy, 0.001) // Indicates NO_DATA
    }

    @Test
    fun calculateMasteryScore_highCompletionAndAccuracy_returnsMasteredLevel() {
        val now = System.currentTimeMillis()
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Mastered Math",
            completionPercentage = 100,
            confidence = 5,
            pyqAttempted = 50,
            pyqCorrect = 48, // 96% accuracy
            revisionCount = 4,
            lastStudiedTimestamp = now - 86400000L
        )

        val result = CoreIntelligenceEngine.calculateMasteryScore(topic, emptyList(), now)

        assertTrue("Mastery score should be >= 80, got ${result.score}", result.score >= 80.0)
        assertEquals(MasteryLevel.MASTERED, result.level)
    }

    @Test
    fun calculateMasteryScore_lowPYQAccuracy_reducesMasteryScore() {
        val topicHighPyq = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Topic A",
            completionPercentage = 100,
            confidence = 4,
            pyqAttempted = 20,
            pyqCorrect = 18,
            revisionCount = 2
        )

        val topicLowPyq = SyllabusItem(
            id = 2,
            subjectId = 10,
            title = "Topic B",
            completionPercentage = 100,
            confidence = 4,
            pyqAttempted = 20,
            pyqCorrect = 6, // 30% accuracy
            revisionCount = 2
        )

        val resultHigh = CoreIntelligenceEngine.calculateMasteryScore(topicHighPyq)
        val resultLow = CoreIntelligenceEngine.calculateMasteryScore(topicLowPyq)

        assertTrue(resultHigh.score > resultLow.score)
    }

    // -------------------------------------------------------------
    // 6. MASTERED CRITERIA & STATUS TESTS
    // -------------------------------------------------------------

    @Test
    fun isTopicMastered_completedButUnresolvedMistakes_returnsFalseCompletedNotMastered() {
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Polity",
            completionPercentage = 100,
            confidence = 5,
            pyqAttempted = 20,
            pyqCorrect = 18,
            revisionCount = 2
        )

        val unresolvedMistake = listOf(
            MistakeEntry(
                id = 1,
                questionText = "Q",
                subjectId = 10,
                subjectName = "Polity",
                chapterTitle = "Polity",
                category = MistakeCategory.CONCEPT_GAP,
                resolutionStatus = MistakeResolutionStatus.ACTIVE
            )
        )

        val intel = CoreIntelligenceEngine.calculateTopicIntelligence(topic, unresolvedMistake)

        assertFalse(intel.isMasteredCriteriaMet)
        assertNotEquals(ChapterStatus.MASTERED, intel.status)
        assertEquals(ChapterStatus.COMPLETED, intel.status)
    }

    @Test
    fun isTopicMastered_allCriteriaSatisfied_returnsTrueAndMasteredStatus() {
        val topic = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Articles",
            completionPercentage = 100,
            confidence = 5,
            pyqAttempted = 30,
            pyqCorrect = 28, // 93% accuracy
            revisionCount = 3
        )

        val intel = CoreIntelligenceEngine.calculateTopicIntelligence(topic, emptyList())

        assertTrue(intel.isMasteredCriteriaMet)
        assertEquals(ChapterStatus.MASTERED, intel.status)
    }

    // -------------------------------------------------------------
    // 7. WEAKNESS & PRIORITY ENGINE TESTS
    // -------------------------------------------------------------

    @Test
    fun calculatePriorityScore_weakUrgentTopic_returnsHighPriorityScore() {
        val topicUrgentWeak = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Hard Math",
            priority = Priority.URGENT,
            isImportant = true,
            confidence = 1,
            pyqAttempted = 10,
            pyqCorrect = 2,
            completionPercentage = 20,
            status = ChapterStatus.WEAK
        )

        val mistakes = listOf(
            MistakeEntry(
                id = 1,
                questionText = "Q",
                subjectId = 10,
                subjectName = "Math",
                chapterTitle = "Hard Math",
                category = MistakeCategory.CONCEPT_GAP,
                resolutionStatus = MistakeResolutionStatus.ACTIVE,
                reviewCount = 3
            )
        )

        val intel = CoreIntelligenceEngine.calculateTopicIntelligence(topicUrgentWeak, mistakes)

        assertTrue("Expected weakness > 70, got ${intel.weaknessScore}", intel.weaknessScore >= 70.0)
        assertTrue("Expected priority > 70, got ${intel.priorityScore}", intel.priorityScore >= 70.0)
    }

    @Test
    fun calculatePriorityScore_masteredLowPriorityTopic_returnsLowPriorityScore() {
        val topicMasteredLow = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Easy Grammar",
            priority = Priority.LOW,
            confidence = 5,
            pyqAttempted = 50,
            pyqCorrect = 49,
            completionPercentage = 100,
            revisionCount = 4,
            status = ChapterStatus.MASTERED
        )

        val intel = CoreIntelligenceEngine.calculateTopicIntelligence(topicMasteredLow, emptyList())

        assertTrue("Expected low priority <= 30, got ${intel.priorityScore}", intel.priorityScore <= 30.0)
    }

    // -------------------------------------------------------------
    // 8. BACKWARD COMPATIBILITY & BOUNDS SAFETY
    // -------------------------------------------------------------

    @Test
    fun calculateTopicIntelligence_oldDataMissingFields_neverProducesNaNsOrOutOfBounds() {
        val legacyTopic = SyllabusItem(
            id = 999,
            subjectId = 10,
            title = "Legacy Topic"
        )

        val intel = CoreIntelligenceEngine.calculateTopicIntelligence(legacyTopic, emptyList())

        assertFalse(intel.masteryScore.isNaN())
        assertFalse(intel.masteryScore.isInfinite())
        assertFalse(intel.weaknessScore.isNaN())
        assertFalse(intel.weaknessScore.isInfinite())
        assertFalse(intel.priorityScore.isNaN())
        assertFalse(intel.priorityScore.isInfinite())

        assertTrue(intel.masteryScore in 0.0..100.0)
        assertTrue(intel.weaknessScore in 0.0..100.0)
        assertTrue(intel.priorityScore in 0.0..100.0)
    }
}
