package com.example.data.intelligence

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object AdaptivePlanningEngine {

    /**
     * Determines Last-Days Exam Mode based on remaining days.
     */
    fun determineLastDaysExamMode(daysRemaining: Int): LastDaysExamMode {
        return when {
            daysRemaining > IntelligenceConfig.lastDaysFoundationThreshold -> LastDaysExamMode.FOUNDATION
            daysRemaining >= IntelligenceConfig.lastDaysAccelerationThreshold -> LastDaysExamMode.ACCELERATION
            daysRemaining >= IntelligenceConfig.lastDaysIntensiveThreshold -> LastDaysExamMode.INTENSIVE_REVISION
            else -> LastDaysExamMode.FINAL_CRUNCH
        }
    }

    /**
     * Generates a descriptive "Why this?" explanation for a topic.
     */
    fun generateWhyExplanation(
        topic: SyllabusItem,
        intel: TopicIntelligence
    ): String {
        val reasons = mutableListOf<String>()

        if (intel.revision.overdue || topic.isRevisionDue) {
            val overdueDays = intel.revision.daysSinceRevision?.toInt() ?: 0
            if (overdueDays > 0) {
                reasons.add("Revision overdue by $overdueDays days")
            } else {
                reasons.add("Scheduled revision is due")
            }
        }

        if (intel.mistakes.conceptGaps > 0) {
            reasons.add("${intel.mistakes.conceptGaps} unresolved concept gaps")
        } else if (intel.mistakes.activeMistakes > 0) {
            reasons.add("${intel.mistakes.activeMistakes} active mistakes detected")
        }

        if (intel.mistakes.repeatedMistakes > 0) {
            reasons.add("Repeated mistakes require review")
        }

        if (intel.pyq.status != PYQStatus.NO_DATA) {
            if (intel.pyq.accuracy < 50.0) {
                reasons.add("Low PYQ accuracy (${intel.pyq.accuracy.roundToInt()}%)")
            } else if (intel.pyq.accuracy < 70.0) {
                reasons.add("Below-target PYQ accuracy (${intel.pyq.accuracy.roundToInt()}%)")
            }
        } else if (topic.completionPercentage > 0 && intel.pyq.attempted == 0) {
            reasons.add("No PYQ practice recorded yet")
        }

        if (topic.priority == Priority.URGENT || topic.isImportant) {
            reasons.add("High exam weightage topic")
        }

        if (intel.confidence.value <= 2) {
            reasons.add("Low self-reported confidence (${intel.confidence.value}/5)")
        }

        if (intel.isMasteredCriteriaMet) {
            return "Mastered topic — on track, only maintenance revision needed."
        }

        if (reasons.isEmpty()) {
            return when {
                topic.completionPercentage == 0 -> "High-yield unstarted chapter to build foundation."
                topic.completionPercentage < 100 -> "In-progress chapter (${topic.completionPercentage}% complete)."
                else -> "Completed chapter awaiting strengthening drills."
            }
        }

        return reasons.joinToString(" • ")
    }

    /**
     * Identifies whether a topic qualifies as Maintenance Only.
     */
    fun isMaintenanceOnlyTopic(topic: SyllabusItem, intel: TopicIntelligence): Boolean {
        val isMastered = intel.isMasteredCriteriaMet || (
                intel.masteryScore >= 80.0 &&
                        (intel.pyq.status == PYQStatus.NO_DATA || intel.pyq.accuracy >= 80.0) &&
                        intel.confidence.value >= 4 &&
                        intel.mistakes.activeMistakes == 0
                )
        val isRevisionOnSchedule = !topic.isRevisionDue && !intel.revision.overdue
        return isMastered && isRevisionOnSchedule
    }

    /**
     * Calculates Exam Readiness (0-100) with dynamic weight re-normalization.
     */
    fun calculateExamReadiness(
        topics: List<SyllabusItem>,
        intelligenceList: List<TopicIntelligence>,
        mockTests: List<MockTest> = emptyList(),
        allMistakes: List<MistakeEntry> = emptyList(),
        daysRemaining: Int = 60
    ): ExamReadinessResult {
        val chapters = topics.filter { it.itemType == ItemType.CHAPTER || it.itemType == ItemType.SUBTOPIC }
        val totalChapters = chapters.size

        if (totalChapters == 0) {
            return ExamReadinessResult(
                score = 0.0,
                level = ReadinessLevel.CRITICAL,
                components = ReadinessComponents(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                confidence = 0.0,
                warnings = listOf("No syllabus topics found. Add topics to begin tracking.")
            )
        }

        // 1. Syllabus Coverage (0..100)
        val completedCount = chapters.count { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
        val syllabusCoverage = ((completedCount.toDouble() / totalChapters.toDouble()) * 100.0).coerceIn(0.0, 100.0)

        // 2. Average Mastery Score (0..100)
        val avgMastery = if (intelligenceList.isNotEmpty()) {
            intelligenceList.map { it.masteryScore }.average().coerceIn(0.0, 100.0)
        } else 0.0

        // 3. PYQ Performance
        val topicsWithPyq = intelligenceList.filter { it.pyq.status != PYQStatus.NO_DATA }
        val hasPyqData = topicsWithPyq.isNotEmpty()
        val pyqPerformance = if (hasPyqData) {
            topicsWithPyq.map { it.pyq.accuracy }.average().coerceIn(0.0, 100.0)
        } else 0.0

        // 4. Revision Coverage (completed topics that have been revised and are not overdue)
        val completedChapters = chapters.filter { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
        val revisedCount = completedChapters.count { it.revisionCount >= 1 && !it.isRevisionDue }
        val revisionCoverage = if (completedChapters.isNotEmpty()) {
            ((revisedCount.toDouble() / completedChapters.size.toDouble()) * 100.0).coerceIn(0.0, 100.0)
        } else 0.0

        // 5. Mistake Control Score (average across topics or resolution rate)
        val mistakeControlScore = if (intelligenceList.isNotEmpty()) {
            intelligenceList.map { it.mistakes.score }.average().coerceIn(0.0, 100.0)
        } else 100.0

        // 6. Mock Performance
        val hasMockData = mockTests.isNotEmpty()
        val mockPerformance = if (hasMockData) {
            val avgAcc = mockTests.map { it.accuracy.toDouble() }.average()
            val avgPercentile = mockTests.map { it.percentile.toDouble() }.average()
            ((avgAcc * 0.5) + (avgPercentile * 0.5)).coerceIn(0.0, 100.0)
        } else 0.0

        // Dynamic Weight Re-normalization
        var activeWeightSum = 0.0
        var weightedSum = 0.0

        // Syllabus Coverage always active
        weightedSum += syllabusCoverage * IntelligenceConfig.readinessSyllabusCoverageWeight
        activeWeightSum += IntelligenceConfig.readinessSyllabusCoverageWeight

        // Mastery always active
        weightedSum += avgMastery * IntelligenceConfig.readinessMasteryWeight
        activeWeightSum += IntelligenceConfig.readinessMasteryWeight

        // Revision Coverage always active
        weightedSum += revisionCoverage * IntelligenceConfig.readinessRevisionCoverageWeight
        activeWeightSum += IntelligenceConfig.readinessRevisionCoverageWeight

        // Mistake Control always active
        weightedSum += mistakeControlScore * IntelligenceConfig.readinessMistakeControlWeight
        activeWeightSum += IntelligenceConfig.readinessMistakeControlWeight

        // PYQ component
        if (hasPyqData) {
            weightedSum += pyqPerformance * IntelligenceConfig.readinessPyqWeight
            activeWeightSum += IntelligenceConfig.readinessPyqWeight
        }

        // Mock Test component
        if (hasMockData) {
            weightedSum += mockPerformance * IntelligenceConfig.readinessMockPerformanceWeight
            activeWeightSum += IntelligenceConfig.readinessMockPerformanceWeight
        }

        val finalScore = if (activeWeightSum > 0.0) {
            (weightedSum / activeWeightSum).coerceIn(0.0, 100.0)
        } else 0.0

        val level = ReadinessLevel.fromScore(finalScore)

        // Confidence calculation based on data reliability
        var dataPoints = 2.0 // syllabus + mastery always present
        if (hasPyqData) dataPoints += 1.5
        if (hasMockData) dataPoints += 1.5
        if (allMistakes.isNotEmpty()) dataPoints += 1.0
        val reliabilityConfidence = ((dataPoints / 6.0) * 100.0).coerceIn(20.0, 100.0)

        // Warnings generation
        val warnings = mutableListOf<String>()
        if (!hasMockData && daysRemaining <= 45) {
            warnings.add("No mock tests recorded yet. Practice full tests to validate exam tempo.")
        }
        val overdueCount = chapters.count { it.isRevisionDue }
        if (overdueCount > 0) {
            warnings.add("$overdueCount chapters have overdue revisions.")
        }
        val activeConceptGaps = allMistakes.count { it.category == MistakeCategory.CONCEPT_GAP && it.resolutionStatus == MistakeResolutionStatus.ACTIVE }
        if (activeConceptGaps > 0) {
            warnings.add("$activeConceptGaps unresolved concept gap mistakes in Mistake Notebook.")
        }
        if (syllabusCoverage < 50.0 && daysRemaining <= 30) {
            warnings.add("Syllabus coverage is critical (${syllabusCoverage.roundToInt()}%) for remaining $daysRemaining days.")
        }
        if (hasPyqData && pyqPerformance < 60.0) {
            warnings.add("Overall PYQ accuracy is ${pyqPerformance.roundToInt()}% — focus on high-yield question drills.")
        }

        return ExamReadinessResult(
            score = finalScore,
            level = level,
            components = ReadinessComponents(
                syllabusCoverage = syllabusCoverage,
                mastery = avgMastery,
                pyqPerformance = if (hasPyqData) pyqPerformance else -1.0,
                revisionCoverage = revisionCoverage,
                mistakeControl = mistakeControlScore,
                mockPerformance = if (hasMockData) mockPerformance else -1.0
            ),
            confidence = reliabilityConfidence,
            warnings = warnings
        )
    }

    /**
     * Calculates Exam Pace & Recovery Recommendations.
     */
    fun calculateExamPace(
        settings: AppSettings,
        topics: List<SyllabusItem>,
        sessions: List<StudySession> = emptyList(),
        currentTimestamp: Long = System.currentTimeMillis()
    ): ExamPaceResult {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val targetCal = Calendar.getInstance()
        var hasValidDate = false

        try {
            if (settings.targetExamDateStr.isNotBlank()) {
                val parsed = dateFormat.parse(settings.targetExamDateStr)
                if (parsed != null) {
                    targetCal.time = parsed
                    hasValidDate = true
                }
            }
        } catch (e: Exception) {
            hasValidDate = false
        }

        if (!hasValidDate) {
            targetCal.timeInMillis = currentTimestamp
            targetCal.add(Calendar.DAY_OF_YEAR, 60)
        }

        val diffMillis = targetCal.timeInMillis - currentTimestamp
        val rawDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        val daysRemaining = rawDays.coerceAtLeast(0)

        val chapters = topics.filter { it.itemType == ItemType.CHAPTER || it.itemType == ItemType.SUBTOPIC }
        val totalChapters = chapters.size
        val completedChapters = chapters.count { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
        val remainingChapters = (totalChapters - completedChapters).coerceAtLeast(0)

        val completedPercentage = if (totalChapters > 0) {
            ((completedChapters.toDouble() / totalChapters.toDouble()) * 100.0).coerceIn(0.0, 100.0)
        } else 100.0

        // Expected progress model: assuming a baseline 90-day cycle or timeline span
        val assumedTotalCycleDays = (daysRemaining + 30).coerceAtLeast(60)
        val elapsedDays = (assumedTotalCycleDays - daysRemaining).coerceAtLeast(1)
        val expectedPercentage = ((elapsedDays.toDouble() / assumedTotalCycleDays.toDouble()) * 100.0).coerceIn(0.0, 100.0)

        val requiredDailyPace = if (daysRemaining > 0) {
            (remainingChapters.toDouble() / daysRemaining.toDouble()).coerceAtLeast(0.0)
        } else {
            if (remainingChapters > 0) remainingChapters.toDouble() else 0.0
        }

        // Current velocity derived from completed chapters & sessions
        val recentSessions = sessions.filter { it.timestamp >= currentTimestamp - (14L * 24 * 60 * 60 * 1000L) }
        val sessionsPace = if (recentSessions.isNotEmpty()) {
            val totalMins = recentSessions.sumOf { (it.durationSeconds / 60).toDouble() }
            (totalMins / 14.0) / 45.0 // ~45 min per chapter equivalent
        } else 0.0

        val fallbackPace = if (elapsedDays > 0) (completedChapters.toDouble() / elapsedDays.toDouble()) else 0.0
        val currentDailyPace = if (sessionsPace > 0.1) (sessionsPace * 0.6 + fallbackPace * 0.4) else fallbackPace.coerceAtLeast(0.1)

        val paceDifference = currentDailyPace - requiredDailyPace

        // Determine Status
        val status = when {
            remainingChapters == 0 -> PaceStatus.AHEAD
            daysRemaining == 0 && remainingChapters > 0 -> PaceStatus.CRITICAL
            paceDifference >= 0.0 -> PaceStatus.AHEAD
            paceDifference >= -0.3 || (completedPercentage >= expectedPercentage - 5.0) -> PaceStatus.ON_TRACK
            paceDifference >= -0.8 || (completedPercentage >= expectedPercentage - 20.0) -> PaceStatus.BEHIND
            else -> PaceStatus.CRITICAL
        }

        // Estimated completion date
        val daysToFinish = if (currentDailyPace > 0.05) {
            (remainingChapters / currentDailyPace).toInt()
        } else daysRemaining + 30

        val estCal = Calendar.getInstance()
        estCal.timeInMillis = currentTimestamp
        estCal.add(Calendar.DAY_OF_YEAR, daysToFinish)
        val estimatedCompletionDateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(estCal.time)
        val targetCompletionDateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(targetCal.time)

        // Recovery Recommendation
        val isRecoveryNeeded = status == PaceStatus.BEHIND || status == PaceStatus.CRITICAL
        val deficitChaptersPerDay = (requiredDailyPace - currentDailyPace).coerceAtLeast(0.0)
        val additionalMinutesNeeded = (deficitChaptersPerDay * 40.0).roundToInt()

        // Realism check: if required pace > 5 chapters/day or extra study time > 240 mins/day, impossible full recovery
        val isRealistic = requiredDailyPace <= 4.5 && additionalMinutesNeeded <= 200 && daysRemaining >= 5

        val recommendationText = when {
            !isRecoveryNeeded -> "You are on track. Maintain current daily rhythm and spaced revisions."
            isRealistic -> "Increase daily study output by ~${String.format(Locale.US, "%.1f", deficitChaptersPerDay)} chapters/day (+${additionalMinutesNeeded.coerceAtLeast(20)} min/day) to regain target pace."
            else -> "Full syllabus completion is unrealistic in the remaining time. Switch to high-yield core chapters and intensive PYQ drills."
        }

        val strategicFocus = when {
            !isRecoveryNeeded -> "Maintain balance between new learning and active revisions."
            isRealistic -> "Add dedicated study blocks for high-priority uncompleted chapters."
            else -> "Triage syllabus: Focus exclusively on top 40% weightage topics and mock tests."
        }

        val recovery = RecoveryRecommendation(
            isNeeded = isRecoveryNeeded,
            isRealistic = isRealistic,
            additionalMinutesPerDay = if (isRecoveryNeeded) additionalMinutesNeeded else 0,
            additionalTopicsPerDay = if (isRecoveryNeeded) deficitChaptersPerDay else 0.0,
            recommendationText = recommendationText,
            strategicFocus = strategicFocus
        )

        return ExamPaceResult(
            daysRemaining = daysRemaining,
            completedPercentage = completedPercentage,
            expectedPercentage = expectedPercentage,
            currentDailyPace = currentDailyPace,
            requiredDailyPace = requiredDailyPace,
            paceDifference = paceDifference,
            estimatedCompletionDateStr = estimatedCompletionDateStr,
            targetCompletionDateStr = targetCompletionDateStr,
            status = status,
            recovery = recovery
        )
    }

    /**
     * Calculates Subject Health from real topic intelligence.
     */
    fun calculateSubjectHealth(
        subject: Subject,
        topics: List<SyllabusItem>,
        intelligenceMap: Map<Long, TopicIntelligence>
    ): SubjectHealthResult {
        val subTopics = topics.filter { it.subjectId == subject.id && (it.itemType == ItemType.CHAPTER || it.itemType == ItemType.SUBTOPIC) }
        val total = subTopics.size

        if (total == 0) {
            return SubjectHealthResult(
                subjectId = subject.id,
                subjectName = subject.name,
                subjectCode = subject.code,
                colorHex = subject.colorHex,
                coveragePercentage = 0.0,
                averageMasteryScore = 0.0,
                pyqAccuracy = 0.0,
                revisionCoverage = 0.0,
                mistakeControlScore = 100.0,
                status = SubjectHealthStatus.NEEDS_ATTENTION,
                totalChapters = 0,
                completedChapters = 0,
                weakChapters = 0,
                revisionDueChapters = 0
            )
        }

        val intelList = subTopics.mapNotNull { intelligenceMap[it.id] }
        val completedCount = subTopics.count { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
        val coverage = ((completedCount.toDouble() / total.toDouble()) * 100.0).coerceIn(0.0, 100.0)

        val avgMastery = if (intelList.isNotEmpty()) intelList.map { it.masteryScore }.average() else 0.0

        val pyqItems = intelList.filter { it.pyq.status != PYQStatus.NO_DATA }
        val pyqAcc = if (pyqItems.isNotEmpty()) pyqItems.map { it.pyq.accuracy }.average() else 0.0

        val completedTopics = subTopics.filter { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
        val revised = completedTopics.count { it.revisionCount >= 1 && !it.isRevisionDue }
        val revCoverage = if (completedTopics.isNotEmpty()) ((revised.toDouble() / completedTopics.size.toDouble()) * 100.0) else 0.0

        val mistakeCtrl = if (intelList.isNotEmpty()) intelList.map { it.mistakes.score }.average() else 100.0

        val weakCount = subTopics.count { it.isWeak || (intelligenceMap[it.id]?.masteryLevel == MasteryLevel.WEAK) }
        val revDueCount = subTopics.count { it.isRevisionDue }

        val status = when {
            avgMastery >= 75.0 && coverage >= 75.0 && weakCount == 0 -> SubjectHealthStatus.EXCELLENT
            avgMastery >= 60.0 && coverage >= 50.0 -> SubjectHealthStatus.GOOD
            avgMastery < 40.0 || (weakCount.toDouble() / total.toDouble()) > 0.4 -> SubjectHealthStatus.CRITICAL
            else -> SubjectHealthStatus.NEEDS_ATTENTION
        }

        return SubjectHealthResult(
            subjectId = subject.id,
            subjectName = subject.name,
            subjectCode = subject.code,
            colorHex = subject.colorHex,
            coveragePercentage = coverage,
            averageMasteryScore = avgMastery,
            pyqAccuracy = pyqAcc,
            revisionCoverage = revCoverage,
            mistakeControlScore = mistakeCtrl,
            status = status,
            totalChapters = total,
            completedChapters = completedCount,
            weakChapters = weakCount,
            revisionDueChapters = revDueCount
        )
    }

    /**
     * Generates Today's Adaptive Plan strictly respecting available study time.
     */
    fun generateTodaysPlan(
        topics: List<SyllabusItem>,
        intelligenceMap: Map<Long, TopicIntelligence>,
        subjectsMap: Map<Long, Subject>,
        allMistakes: List<MistakeEntry> = emptyList(),
        mockTests: List<MockTest> = emptyList(),
        availableMinutes: Int = 120,
        daysRemaining: Int = 60
    ): TodaysPlanResult {
        val safeBudget = availableMinutes.coerceIn(15, 600)
        val mode = determineLastDaysExamMode(daysRemaining)
        val chapters = topics.filter { it.itemType == ItemType.CHAPTER || it.itemType == ItemType.SUBTOPIC }

        if (chapters.isEmpty()) {
            return TodaysPlanResult(
                totalMinutes = 0,
                availableMinutes = safeBudget,
                items = emptyList(),
                lastDaysMode = mode,
                notes = "Add syllabus chapters to generate daily adaptive plans."
            )
        }

        // Candidate Pool Generation
        val candidateActions = mutableListOf<PlanActionItem>()

        for (topic in chapters) {
            val intel = intelligenceMap[topic.id] ?: continue
            val subName = subjectsMap[topic.subjectId]?.name ?: "General"
            val why = generateWhyExplanation(topic, intel)

            // 1. Overdue / Due Revisions
            if (topic.isRevisionDue || intel.revision.overdue) {
                val revPriority = (intel.priorityScore + 30.0).coerceAtMost(100.0)
                val duration = when {
                    safeBudget <= 45 -> 15
                    safeBudget <= 90 -> 20
                    else -> 25
                }
                candidateActions.add(
                    PlanActionItem(
                        id = "rev_${topic.id}",
                        topicId = topic.id,
                        topicTitle = topic.title,
                        subjectId = topic.subjectId,
                        subjectName = subName,
                        actionType = PlanActionType.REVISION,
                        estimatedMinutes = duration,
                        priority = revPriority,
                        reason = if (intel.revision.overdue) "Revision overdue • Memory retention decaying" else "Spaced revision due today"
                    )
                )
            }

            // 2. Unresolved Concept Gaps or Repeated Mistakes
            if (intel.mistakes.conceptGaps > 0 || intel.mistakes.repeatedMistakes > 0) {
                val mistakePriority = (intel.priorityScore + 25.0).coerceAtMost(100.0)
                val duration = when {
                    safeBudget <= 45 -> 15
                    safeBudget <= 90 -> 20
                    else -> 25
                }
                candidateActions.add(
                    PlanActionItem(
                        id = "mistake_${topic.id}",
                        topicId = topic.id,
                        topicTitle = topic.title,
                        subjectId = topic.subjectId,
                        subjectName = subName,
                        actionType = if (intel.mistakes.conceptGaps > 0) PlanActionType.CONCEPT_REVIEW else PlanActionType.MISTAKE_REVIEW,
                        estimatedMinutes = duration,
                        priority = mistakePriority,
                        reason = "${intel.mistakes.activeMistakes} mistakes recorded (${intel.mistakes.conceptGaps} concept gaps)"
                    )
                )
            }

            // 3. Critical Weak Topics
            if (intel.masteryLevel == MasteryLevel.WEAK || intel.weaknessScore >= 60.0) {
                val weakPriority = (intel.priorityScore + 22.0).coerceAtMost(100.0)
                val duration = when {
                    safeBudget <= 45 -> 20
                    safeBudget <= 90 -> 30
                    else -> 35
                }
                candidateActions.add(
                    PlanActionItem(
                        id = "weak_${topic.id}",
                        topicId = topic.id,
                        topicTitle = topic.title,
                        subjectId = topic.subjectId,
                        subjectName = subName,
                        actionType = PlanActionType.WEAK_TOPIC,
                        estimatedMinutes = duration,
                        priority = weakPriority,
                        reason = "Weak mastery (${intel.masteryScore.roundToInt()}/100) • High weakness score"
                    )
                )
            }

            // 4. PYQ Drills
            if (intel.pyq.status == PYQStatus.POOR || (intel.pyq.status == PYQStatus.NO_DATA && topic.completionPercentage >= 50)) {
                val pyqPriority = (intel.priorityScore + 18.0).coerceAtMost(100.0)
                val duration = when {
                    safeBudget <= 45 -> 15
                    safeBudget <= 90 -> 20
                    else -> 25
                }
                candidateActions.add(
                    PlanActionItem(
                        id = "pyq_${topic.id}",
                        topicId = topic.id,
                        topicTitle = topic.title,
                        subjectId = topic.subjectId,
                        subjectName = subName,
                        actionType = PlanActionType.PYQ_PRACTICE,
                        estimatedMinutes = duration,
                        priority = pyqPriority,
                        reason = if (intel.pyq.status == PYQStatus.NO_DATA) "No PYQ practice yet • Reinforce concepts" else "PYQ accuracy ${intel.pyq.accuracy.roundToInt()}% • Drill practice"
                    )
                )
            }

            // 5. In-Progress / High Priority Learn Topics (Skip if Final Crunch mode)
            if (mode != LastDaysExamMode.FINAL_CRUNCH && (topic.status == ChapterStatus.IN_PROGRESS || topic.status == ChapterStatus.NOT_STARTED)) {
                val learnPriority = intel.priorityScore
                val duration = when {
                    safeBudget <= 45 -> 20
                    safeBudget <= 90 -> 30
                    else -> 35
                }
                candidateActions.add(
                    PlanActionItem(
                        id = "learn_${topic.id}",
                        topicId = topic.id,
                        topicTitle = topic.title,
                        subjectId = topic.subjectId,
                        subjectName = subName,
                        actionType = PlanActionType.CONCEPT_REVIEW,
                        estimatedMinutes = duration,
                        priority = learnPriority,
                        reason = why
                    )
                )
            }

            // 6. Mastered Topics (Maintenance only)
            if (intel.isMasteredCriteriaMet && (topic.isRevisionDue || intel.revision.overdue)) {
                candidateActions.add(
                    PlanActionItem(
                        id = "maint_${topic.id}",
                        topicId = topic.id,
                        topicTitle = topic.title,
                        subjectId = topic.subjectId,
                        subjectName = subName,
                        actionType = PlanActionType.MAINTENANCE,
                        estimatedMinutes = 15,
                        priority = (intel.priorityScore * 0.5).coerceAtLeast(10.0),
                        reason = "Mastered topic • Quick maintenance review"
                    )
                )
            }
        }

        // Mode specific boost/filtering
        val adjustedActions = candidateActions.map { action ->
            var prio = action.priority
            when (mode) {
                LastDaysExamMode.FINAL_CRUNCH -> {
                    if (action.actionType == PlanActionType.REVISION || action.actionType == PlanActionType.MISTAKE_REVIEW) {
                        prio += 30.0
                    }
                }
                LastDaysExamMode.INTENSIVE_REVISION -> {
                    if (action.actionType == PlanActionType.PYQ_PRACTICE || action.actionType == PlanActionType.WEAK_TOPIC) {
                        prio += 20.0
                    }
                }
                LastDaysExamMode.ACCELERATION -> {
                    if (action.actionType == PlanActionType.PYQ_PRACTICE || action.actionType == PlanActionType.REVISION) {
                        prio += 15.0
                    }
                }
                LastDaysExamMode.FOUNDATION -> {
                    // balanced
                }
            }
            action.copy(priority = prio)
        }

        // Sort by priority descending
        val sortedCandidates = adjustedActions.sortedByDescending { it.priority }

        // Greedily pick actions fitting into safeBudget without exceeding it
        val selectedItems = mutableListOf<PlanActionItem>()
        var currentAllocated = 0
        val usedTopicActionKeys = mutableSetOf<String>()

        for (action in sortedCandidates) {
            val key = "${action.topicId}_${action.actionType}"
            if (usedTopicActionKeys.contains(key)) continue

            // Don't duplicate same topic more than once unless budget >= 150
            if (safeBudget < 150 && action.topicId != null && selectedItems.any { it.topicId == action.topicId }) {
                continue
            }

            if (currentAllocated + action.estimatedMinutes <= safeBudget) {
                selectedItems.add(action)
                currentAllocated += action.estimatedMinutes
                usedTopicActionKeys.add(key)
            } else {
                // If there is leftover time (e.g. 15 mins) and we can fit a scaled 15m review
                val remainingTime = safeBudget - currentAllocated
                if (remainingTime >= 15 && action.estimatedMinutes > remainingTime) {
                    val scaledAction = action.copy(
                        estimatedMinutes = remainingTime,
                        reason = "${action.reason} (Quick focus)"
                    )
                    selectedItems.add(scaledAction)
                    currentAllocated += remainingTime
                    usedTopicActionKeys.add(key)
                }
            }

            if (currentAllocated >= safeBudget) break
        }

        // Fallback if empty (e.g. all chapters mastered)
        if (selectedItems.isEmpty() && chapters.isNotEmpty()) {
            val topTopic = chapters.first()
            val intel = intelligenceMap[topTopic.id]
            val subName = subjectsMap[topTopic.subjectId]?.name ?: "General"
            selectedItems.add(
                PlanActionItem(
                    id = "fallback_${topTopic.id}",
                    topicId = topTopic.id,
                    topicTitle = topTopic.title,
                    subjectId = topTopic.subjectId,
                    subjectName = subName,
                    actionType = PlanActionType.REVISION,
                    estimatedMinutes = safeBudget.coerceAtMost(30),
                    priority = 50.0,
                    reason = "Daily study drill • Maintain retention"
                )
            )
            currentAllocated = safeBudget.coerceAtMost(30)
        }

        val planNote = when (mode) {
            LastDaysExamMode.FINAL_CRUNCH -> "⚡ Final 7-Day Sprint: Prioritizing high-yield revisions, error logs, and rapid recall."
            LastDaysExamMode.INTENSIVE_REVISION -> "🎯 Intensive Mode: Concentrating on weak chapters and PYQ speed drills."
            LastDaysExamMode.ACCELERATION -> "🚀 Acceleration Mode: Balancing new concept completion with regular spaced revision."
            LastDaysExamMode.FOUNDATION -> "📚 Foundation Mode: Steady systematic coverage and concept clarity."
        }

        return TodaysPlanResult(
            totalMinutes = currentAllocated,
            availableMinutes = safeBudget,
            items = selectedItems,
            lastDaysMode = mode,
            notes = planNote
        )
    }

    /**
     * Builds a single centralized Intelligence Snapshot.
     */
    fun createIntelligenceSnapshot(
        topics: List<SyllabusItem>,
        subjects: List<Subject>,
        mistakes: List<MistakeEntry>,
        mockTests: List<MockTest>,
        sessions: List<StudySession>,
        settings: AppSettings,
        availableBudgetMinutes: Int = IntelligenceConfig.defaultDailyBudgetMinutes,
        currentTime: Long = System.currentTimeMillis()
    ): IntelligenceSnapshot {
        val subjectsMap = subjects.associateBy { it.id }

        // 1. Topic Intelligence Map
        val intelMap = mutableMapOf<Long, TopicIntelligence>()
        for (topic in topics) {
            val baseIntel = CoreIntelligenceEngine.calculateTopicIntelligence(topic, mistakes, currentTime)
            val subName = subjectsMap[topic.subjectId]?.name ?: ""
            val isMaint = isMaintenanceOnlyTopic(topic, baseIntel)
            val why = generateWhyExplanation(topic, baseIntel)

            val fullIntel = baseIntel.copy(
                subjectId = topic.subjectId,
                subjectName = subName,
                isMaintenanceOnly = isMaint,
                whyExplanation = why
            )
            intelMap[topic.id] = fullIntel
        }

        val pace = calculateExamPace(settings, topics, sessions, currentTime)
        val mode = determineLastDaysExamMode(pace.daysRemaining)

        val readiness = calculateExamReadiness(
            topics = topics,
            intelligenceList = intelMap.values.toList(),
            mockTests = mockTests,
            allMistakes = mistakes,
            daysRemaining = pace.daysRemaining
        )

        val todaysPlan = generateTodaysPlan(
            topics = topics,
            intelligenceMap = intelMap,
            subjectsMap = subjectsMap,
            allMistakes = mistakes,
            mockTests = mockTests,
            availableMinutes = availableBudgetMinutes,
            daysRemaining = pace.daysRemaining
        )

        val subjectHealthList = subjects.map { sub ->
            calculateSubjectHealth(sub, topics, intelMap)
        }

        // Top Weak Topics (sorted by weakness descending)
        val topWeak = intelMap.values
            .filter { it.weaknessScore >= 40.0 || it.masteryLevel == MasteryLevel.WEAK }
            .sortedByDescending { it.weaknessScore }
            .take(5)

        // Mastered Topics
        val mastered = intelMap.values
            .filter { it.isMasteredCriteriaMet || it.masteryLevel == MasteryLevel.MASTERED }
            .sortedByDescending { it.masteryScore }

        // Maintenance Topics
        val maintenance = intelMap.values
            .filter { it.isMaintenanceOnly }
            .sortedByDescending { it.masteryScore }

        return IntelligenceSnapshot(
            readiness = readiness,
            todaysPlan = todaysPlan,
            pace = pace,
            subjectHealthList = subjectHealthList,
            topWeakTopics = topWeak,
            masteredTopics = mastered,
            maintenanceTopics = maintenance,
            allTopicIntelligence = intelMap,
            lastDaysMode = mode
        )
    }
}
