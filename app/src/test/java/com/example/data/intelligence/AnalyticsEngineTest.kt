package com.example.data.intelligence

import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class AnalyticsEngineTest {

    private val now = System.currentTimeMillis()

    private val mockSubjects = listOf(
        Subject(id = 1L, name = "Quantitative Aptitude", code = "QA", colorHex = "#10B981"),
        Subject(id = 2L, name = "General Science", code = "GS", colorHex = "#3B82F6"),
        Subject(id = 3L, name = "English", code = "ENG", colorHex = "#F59E0B")
    )

    private val mockTopics = listOf(
        SyllabusItem(
            id = 101L,
            subjectId = 1L,
            title = "Percentages & Ratio",
            itemType = ItemType.CHAPTER,
            status = ChapterStatus.MASTERED,
            completionPercentage = 100,
            pyqAttempted = 50,
            pyqCorrect = 46,
            revisionCount = 3,
            confidence = 5,
            lastStudiedTimestamp = now - (2L * 24 * 3600 * 1000)
        ),
        SyllabusItem(
            id = 102L,
            subjectId = 1L,
            title = "Number Systems",
            itemType = ItemType.CHAPTER,
            status = ChapterStatus.IN_PROGRESS,
            completionPercentage = 70,
            pyqAttempted = 25,
            pyqCorrect = 18,
            revisionCount = 1,
            confidence = 3,
            lastStudiedTimestamp = now - (5L * 24 * 3600 * 1000)
        ),
        SyllabusItem(
            id = 103L,
            subjectId = 2L,
            title = "Optics & Light",
            itemType = ItemType.CHAPTER,
            status = ChapterStatus.COMPLETED,
            completionPercentage = 100,
            pyqAttempted = 30,
            pyqCorrect = 25,
            revisionCount = 2,
            confidence = 4,
            lastStudiedTimestamp = now - (3L * 24 * 3600 * 1000)
        ),
        SyllabusItem(
            id = 104L,
            subjectId = 3L,
            title = "Reading Comprehension",
            itemType = ItemType.CHAPTER,
            status = ChapterStatus.NOT_STARTED,
            completionPercentage = 0,
            pyqAttempted = 0,
            pyqCorrect = 0,
            revisionCount = 0,
            confidence = 2
        )
    )

    private val sampleComponents = MasteryComponents(
        pyqAccuracy = 90.0,
        confidence = 90.0,
        revisionStrength = 90.0,
        completion = 100.0,
        mistakeControl = 100.0,
        retention = 90.0
    )

    private val mockTopicIntels = mapOf(
        101L to TopicIntelligence(
            topicId = 101L,
            topicTitle = "Percentages & Ratio",
            subjectId = 1L,
            masteryScore = 92.0,
            masteryLevel = MasteryLevel.MASTERED,
            pyq = PYQPerformance(50, 46, 4, 92.0, PYQStatus.EXCELLENT),
            confidence = ConfidenceInfo(5, 100.0),
            mistakes = MistakeControlResult(100.0, 0, 0, 0, 0),
            revision = RevisionStrengthResult(100.0, 3, false, 2.0),
            weaknessScore = 12.0,
            priorityScore = 20.0,
            status = ChapterStatus.MASTERED,
            isMasteredCriteriaMet = true,
            masteryComponents = sampleComponents
        ),
        102L to TopicIntelligence(
            topicId = 102L,
            topicTitle = "Number Systems",
            subjectId = 1L,
            masteryScore = 64.0,
            masteryLevel = MasteryLevel.STRONG,
            pyq = PYQPerformance(25, 18, 7, 72.0, PYQStatus.GOOD),
            confidence = ConfidenceInfo(3, 60.0),
            mistakes = MistakeControlResult(70.0, 2, 2, 0, 1),
            revision = RevisionStrengthResult(60.0, 1, false, 5.0),
            weaknessScore = 48.0,
            priorityScore = 65.0,
            status = ChapterStatus.IN_PROGRESS,
            isMasteredCriteriaMet = false,
            masteryComponents = sampleComponents
        ),
        103L to TopicIntelligence(
            topicId = 103L,
            topicTitle = "Optics & Light",
            subjectId = 2L,
            masteryScore = 82.0,
            masteryLevel = MasteryLevel.MASTERED,
            pyq = PYQPerformance(30, 25, 5, 83.3, PYQStatus.GOOD),
            confidence = ConfidenceInfo(4, 80.0),
            mistakes = MistakeControlResult(90.0, 1, 0, 0, 0),
            revision = RevisionStrengthResult(85.0, 2, false, 3.0),
            weaknessScore = 22.0,
            priorityScore = 30.0,
            status = ChapterStatus.COMPLETED,
            isMasteredCriteriaMet = true,
            masteryComponents = sampleComponents
        ),
        104L to TopicIntelligence(
            topicId = 104L,
            topicTitle = "Reading Comprehension",
            subjectId = 3L,
            masteryScore = 20.0,
            masteryLevel = MasteryLevel.WEAK,
            pyq = PYQPerformance(0, 0, 0, 0.0, PYQStatus.NO_DATA),
            confidence = ConfidenceInfo(2, 40.0),
            mistakes = MistakeControlResult(100.0, 0, 0, 0, 0),
            revision = RevisionStrengthResult(0.0, 0, false, null),
            weaknessScore = 75.0,
            priorityScore = 80.0,
            status = ChapterStatus.NOT_STARTED,
            isMasteredCriteriaMet = false,
            masteryComponents = sampleComponents
        )
    )

    private val mockMistakes = listOf(
        MistakeEntry(
            id = 1L,
            subjectId = 1L,
            subjectName = "Quantitative Aptitude",
            chapterTitle = "Number Systems",
            questionText = "Prime factors question",
            category = MistakeCategory.CONCEPT_GAP,
            resolutionStatus = MistakeResolutionStatus.ACTIVE,
            createdTimestamp = now - (2L * 24 * 3600 * 1000)
        ),
        MistakeEntry(
            id = 2L,
            subjectId = 2L,
            subjectName = "General Science",
            chapterTitle = "Optics & Light",
            questionText = "Focal length formula calculation",
            category = MistakeCategory.CALCULATION_ERROR,
            resolutionStatus = MistakeResolutionStatus.MASTERED,
            createdTimestamp = now - (10L * 24 * 3600 * 1000)
        )
    )

    private val mockTests = listOf(
        MockTest(
            id = 1L,
            testName = "All India Mock 1",
            marksScored = 145f,
            totalMarks = 200f,
            accuracy = 82f,
            percentile = 91f,
            timestamp = now - (4L * 24 * 3600 * 1000),
            testDateStr = "2026-08-17"
        )
    )

    private val mockSessions = listOf(
        StudySession(
            id = 1L,
            subjectId = 1L,
            subjectName = "Quantitative Aptitude",
            chapterId = 101L,
            chapterTitle = "Percentages & Ratio",
            durationSeconds = 1800, // 30m
            mode = TimerMode.POMODORO,
            notes = "PYQ drill",
            timestamp = now - (1L * 24 * 3600 * 1000)
        ),
        StudySession(
            id = 2L,
            subjectId = 2L,
            subjectName = "General Science",
            chapterId = 103L,
            chapterTitle = "Optics & Light",
            durationSeconds = 2400, // 40m
            mode = TimerMode.STOPWATCH,
            notes = "Revision session",
            timestamp = now - (2L * 24 * 3600 * 1000)
        ),
        StudySession(
            id = 3L,
            subjectId = 1L,
            subjectName = "Quantitative Aptitude",
            chapterId = 102L,
            chapterTitle = "Number Systems",
            durationSeconds = 1500, // 25m
            mode = TimerMode.POMODORO,
            notes = "Mistake review",
            timestamp = now - (3L * 24 * 3600 * 1000)
        )
    )

    @Test
    fun testLongTermAnalyticsCalculations() {
        val result30D = AnalyticsEngine.calculateLongTermAnalytics(
            window = AnalyticsTimeWindow.DAYS_30,
            topics = mockTopics,
            subjects = mockSubjects,
            topicIntelMap = mockTopicIntels,
            mockTests = mockTests,
            mistakes = mockMistakes,
            studySessions = mockSessions,
            readinessResult = null,
            currentTime = now
        )

        assertTrue(result30D.hasSufficientData)
        assertEquals(AnalyticsTimeWindow.DAYS_30, result30D.window)
        assertTrue(result30D.studyTimeHours.currentValue > 0.0)
        assertTrue(result30D.topicsCompleted.currentValue >= 2.0)
        assertTrue(result30D.topicsMastered.currentValue >= 2.0)
        assertTrue(result30D.pyqAccuracy.currentValue > 80.0)
        assertTrue(result30D.summaryInsight.isNotBlank())
    }

    @Test
    fun testMasteryGrowthCalculations() {
        val growth = AnalyticsEngine.calculateMasteryGrowth(
            window = AnalyticsTimeWindow.DAYS_30,
            topics = mockTopics,
            subjects = mockSubjects,
            topicIntelMap = mockTopicIntels,
            studySessions = mockSessions,
            currentTime = now
        )

        assertTrue(growth.hasSufficientData)
        assertEquals(4, growth.totalTopicsCount)
        assertEquals(2, growth.masteredTopicsCount)
        assertTrue(growth.currentMastery > growth.startingMastery)
        assertTrue(growth.absoluteGrowth > 0.0)
        assertNotNull(growth.fastestImprovingSubject)
    }

    @Test
    fun testSubjectComparisonsRanking() {
        val comparison = AnalyticsEngine.calculateSubjectComparisons(
            subjects = mockSubjects,
            topics = mockTopics,
            topicIntelMap = mockTopicIntels,
            mockTests = mockTests,
            mistakes = mockMistakes
        )

        assertEquals(3, comparison.rankings.size)
        assertNotNull(comparison.topSubject)
        assertEquals(1, comparison.rankings.first().rank)

        // Verify rankings are descending by composite score
        val scores = comparison.rankings.map { it.compositeScore }
        assertEquals(scores.sortedDescending(), scores)

        // QA has high mastery and PYQ accuracy
        val qa = comparison.rankings.find { it.subjectCode == "QA" }
        assertNotNull(qa)
        assertTrue(qa!!.compositeScore > 50.0)
    }

    @Test
    fun testStudyConsistencyCalculations() {
        val consistency = AnalyticsEngine.calculateStudyConsistency(
            window = AnalyticsTimeWindow.DAYS_30,
            studySessions = mockSessions,
            goals = emptyList(),
            meaningfulThresholdMinutes = 15,
            currentTime = now
        )

        assertEquals(30, consistency.totalDaysInWindow)
        assertEquals(3, consistency.studyDays)
        assertEquals(27, consistency.missedDays)
        assertEquals(3, consistency.completedSessions)
        assertTrue(consistency.averageDailyStudyMinutes >= 25)
        assertTrue(consistency.feedbackMessage.isNotBlank())
    }

    @Test
    fun testQualityAdjustedStudyTime() {
        val quality = AnalyticsEngine.calculateQualityAdjustedStudyTime(
            window = AnalyticsTimeWindow.DAYS_30,
            studySessions = mockSessions,
            mockTests = mockTests,
            currentTime = now
        )

        assertTrue(quality.totalRawMinutes > 0)
        assertTrue(quality.productiveMinutes > 0)
        assertTrue(quality.qualityAdjustedMinutes >= quality.totalRawMinutes)
        assertTrue(quality.qualityMultiplierAvg >= 1.0)
        assertEquals(6, quality.breakdowns.size)
    }

    @Test
    fun testProductivityPatterns() {
        val patterns = AnalyticsEngine.calculateProductivityPatterns(
            studySessions = mockSessions,
            mockTests = mockTests,
            currentTime = now
        )

        assertTrue(patterns.hasSufficientData)
        assertNotNull(patterns.bestStudyDay)
        assertNotNull(patterns.bestTimeOfDaySlot)
        assertEquals(7, patterns.dayPerformances.size)
    }

    @Test
    fun testMeaningfulAchievementsEvaluation() {
        val achievements = AnalyticsEngine.evaluateAchievements(
            topics = mockTopics,
            subjects = mockSubjects,
            topicIntelMap = mockTopicIntels,
            mockTests = mockTests,
            mistakes = mockMistakes,
            studySessions = mockSessions,
            goals = emptyList(),
            currentTime = now
        )

        assertTrue(achievements.isNotEmpty())
        val firstMastered = achievements.find { it.id == MeaningfulAchievementId.FIRST_MASTERED_TOPIC }
        assertNotNull(firstMastered)
        assertTrue(firstMastered!!.isUnlocked)

        val firstRev = achievements.find { it.id == MeaningfulAchievementId.FIRST_REVISION_COMPLETED }
        assertNotNull(firstRev)
        assertTrue(firstRev!!.isUnlocked)
    }

    @Test
    fun testMilestonesDistinction() {
        val milestones = AnalyticsEngine.evaluateMilestones(
            topics = mockTopics,
            topicIntelMap = mockTopicIntels
        )

        assertTrue(milestones.isNotEmpty())
        val coverage25 = milestones.find { it.id == "cov_25" }
        val mastery25 = milestones.find { it.id == "mast_25" }

        assertNotNull(coverage25)
        assertNotNull(mastery25)
        assertTrue(coverage25!!.isAchieved)
        assertTrue(mastery25!!.isAchieved)

        val next = AnalyticsEngine.recommendNextMilestone(milestones)
        assertNotNull(next)
        assertFalse(next!!.isAchieved)
    }

    @Test
    fun testPersonalRecordsEvaluation() {
        val records = AnalyticsEngine.evaluatePersonalRecords(
            mockTests = mockTests,
            topics = mockTopics,
            studySessions = mockSessions,
            mistakes = mockMistakes
        )

        assertNotNull(records.highestMockScore)
        assertEquals(145.0, records.highestMockScore!!.rawValue, 0.1)

        assertNotNull(records.highestPyqAccuracy)
        assertTrue(records.highestPyqAccuracy!!.rawValue > 80.0)

        assertTrue(records.totalRecordsCount >= 3)
    }

    @Test
    fun testMeaningfulStreaksAndCompassionateRecovery() {
        val (studyStreak, revStreak) = AnalyticsEngine.calculateMeaningfulStreaks(
            studySessions = mockSessions,
            topics = mockTopics,
            meaningfulMinutesThreshold = 15,
            currentTime = now
        )

        assertTrue(studyStreak.meaningfulMinutesRequired == 15)
        assertTrue(studyStreak.recoveryMessage.isNotBlank())
        assertTrue(revStreak.longestStreakDays >= 1)
    }

    @Test
    fun testMonthlyReviewGeneration() {
        val achievements = AnalyticsEngine.evaluateAchievements(
            topics = mockTopics,
            subjects = mockSubjects,
            topicIntelMap = mockTopicIntels,
            mockTests = mockTests,
            mistakes = mockMistakes,
            studySessions = mockSessions,
            goals = emptyList(),
            currentTime = now
        )

        val personalRecords = AnalyticsEngine.evaluatePersonalRecords(
            mockTests = mockTests,
            topics = mockTopics,
            studySessions = mockSessions,
            mistakes = mockMistakes
        )

        val review = AnalyticsEngine.generateMonthlyReview(
            topics = mockTopics,
            subjects = mockSubjects,
            topicIntelMap = mockTopicIntels,
            mockTests = mockTests,
            mistakes = mockMistakes,
            studySessions = mockSessions,
            achievements = achievements,
            personalRecords = personalRecords,
            currentTime = now
        )

        assertNotNull(review)
        assertTrue(review!!.hasSufficientData)
        assertTrue(review.activeStudyDays > 0)
        assertTrue(review.keyNextMonthDirectives.isNotEmpty())
        assertTrue(review.overallMonthNarrative.isNotBlank())
    }

    @Test
    fun testAdvancedAnalyticsSnapshotCreation() {
        val snapshot = AnalyticsEngine.createAdvancedAnalyticsSnapshot(
            topics = mockTopics,
            subjects = mockSubjects,
            topicIntelMap = mockTopicIntels,
            mockTests = mockTests,
            mistakes = mockMistakes,
            studySessions = mockSessions,
            goals = emptyList(),
            readinessResult = null,
            currentTime = now
        )

        assertNotNull(snapshot.longTerm30D)
        assertNotNull(snapshot.masteryGrowth)
        assertNotNull(snapshot.subjectComparisons)
        assertNotNull(snapshot.consistency)
        assertNotNull(snapshot.qualityStudyTime)
        assertNotNull(snapshot.productivityPatterns)
        assertTrue(snapshot.achievements.isNotEmpty())
        assertTrue(snapshot.milestones.isNotEmpty())
        assertNotNull(snapshot.personalRecords)
        assertNotNull(snapshot.studyStreak)
        assertNotNull(snapshot.monthlyReview)
    }
}
