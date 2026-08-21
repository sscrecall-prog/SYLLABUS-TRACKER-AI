package com.example.data.intelligence

import com.example.data.model.*
import kotlin.math.exp

object CoreIntelligenceEngine {

    /**
     * Calculates PYQ Performance for a given topic safely without NaN or Infinity.
     * Distinguishes NO_DATA from 0% Accuracy.
     */
    fun calculatePYQPerformance(topic: SyllabusItem): PYQPerformance {
        val attempted = topic.pyqAttempted.coerceAtLeast(0)
        val correct = topic.pyqCorrect.coerceIn(0, if (attempted > 0) attempted else 0)
        val incorrect = (attempted - correct).coerceAtLeast(0)

        if (attempted <= 0) {
            return PYQPerformance(
                attempted = 0,
                correct = 0,
                incorrect = 0,
                accuracy = 0.0,
                status = PYQStatus.NO_DATA
            )
        }

        val accuracy = ((correct.toDouble() / attempted.toDouble()) * 100.0).coerceIn(0.0, 100.0)
        val status = when {
            accuracy >= 85.0 -> PYQStatus.EXCELLENT
            accuracy >= 70.0 -> PYQStatus.GOOD
            accuracy >= 50.0 -> PYQStatus.AVERAGE
            else -> PYQStatus.POOR
        }

        return PYQPerformance(
            attempted = attempted,
            correct = correct,
            incorrect = incorrect,
            accuracy = accuracy,
            status = status
        )
    }

    /**
     * Calculates confidence scale normalized to 0-100.
     * Scale 1-5 maps to 20.0, 40.0, 60.0, 80.0, 100.0.
     * Default for uninitialized or out-of-range values is 3 (60.0).
     */
    fun calculateConfidenceInfo(topic: SyllabusItem): ConfidenceInfo {
        val raw = if (topic.confidence in 1..5) topic.confidence else 3
        val normalized = (raw * 20.0).coerceIn(0.0, 100.0)
        return ConfidenceInfo(value = raw, normalized = normalized)
    }

    /**
     * Finds mistakes related to a specific syllabus topic.
     */
    fun filterMistakesForTopic(topic: SyllabusItem, allMistakes: List<MistakeEntry>): List<MistakeEntry> {
        return allMistakes.filter { mistake ->
            mistake.subjectId == topic.subjectId &&
                    (mistake.chapterTitle.equals(topic.title, ignoreCase = true) ||
                            (mistake.chapterTitle.isNotEmpty() && topic.title.contains(mistake.chapterTitle, ignoreCase = true)) ||
                            (topic.title.isNotEmpty() && mistake.chapterTitle.contains(topic.title, ignoreCase = true)) ||
                            mistake.tagsCsv.contains(topic.title, ignoreCase = true))
        }
    }

    /**
     * Calculates normalized mistake control score (0-100).
     */
    fun calculateMistakeControl(
        topic: SyllabusItem,
        allMistakes: List<MistakeEntry>
    ): MistakeControlResult {
        val relevantMistakes = filterMistakesForTopic(topic, allMistakes)
        val total = relevantMistakes.size

        if (total == 0) {
            return MistakeControlResult(
                score = 100.0,
                totalMistakes = 0,
                activeMistakes = 0,
                repeatedMistakes = 0,
                conceptGaps = 0
            )
        }

        val active = relevantMistakes.count { it.resolutionStatus == MistakeResolutionStatus.ACTIVE }
        val understood = relevantMistakes.count { it.resolutionStatus == MistakeResolutionStatus.UNDERSTOOD }
        val repeated = relevantMistakes.count {
            it.reviewCount > 1 || it.tagsCsv.contains("repeated", ignoreCase = true)
        }
        val conceptGaps = relevantMistakes.count {
            it.category == MistakeCategory.CONCEPT_GAP && it.resolutionStatus != MistakeResolutionStatus.MASTERED
        }

        // Penalties
        val activePenalty = active * 15.0
        val understoodPenalty = understood * 5.0
        val repeatedPenalty = repeated * 10.0
        val conceptGapPenalty = conceptGaps * 10.0

        val totalPenalty = activePenalty + understoodPenalty + repeatedPenalty + conceptGapPenalty
        val score = (100.0 - totalPenalty).coerceIn(0.0, 100.0)

        return MistakeControlResult(
            score = score,
            totalMistakes = total,
            activeMistakes = active,
            repeatedMistakes = repeated,
            conceptGaps = conceptGaps
        )
    }

    /**
     * Calculates revision strength score (0-100).
     */
    fun calculateRevisionStrength(
        topic: SyllabusItem,
        currentTime: Long = System.currentTimeMillis()
    ): RevisionStrengthResult {
        val revCount = topic.revisionCount.coerceAtLeast(0)
        val overdue = topic.isRevisionDue

        val daysSince: Double? = if (topic.lastStudiedTimestamp != null && topic.lastStudiedTimestamp > 0) {
            ((currentTime - topic.lastStudiedTimestamp).toDouble() / (1000.0 * 60 * 60 * 24)).coerceAtLeast(0.0)
        } else null

        var baseScore = when (revCount) {
            0 -> 0.0
            1 -> 50.0
            2 -> 75.0
            3 -> 88.0
            else -> 100.0
        }

        if (overdue) {
            baseScore -= 25.0
        }

        if (daysSince != null) {
            if (daysSince <= 7.0 && revCount > 0) {
                baseScore += 10.0
            } else if (daysSince > 30.0) {
                baseScore -= 15.0
            }
        }

        val score = baseScore.coerceIn(0.0, 100.0)

        return RevisionStrengthResult(
            score = score,
            revisionCount = revCount,
            overdue = overdue,
            daysSinceRevision = daysSince
        )
    }

    /**
     * Calculates recency / memory retention score (0-100).
     */
    fun calculateRetentionScore(
        topic: SyllabusItem,
        currentTime: Long = System.currentTimeMillis()
    ): Double {
        if (topic.lastStudiedTimestamp == null || topic.lastStudiedTimestamp <= 0) {
            return 50.0 // neutral default for unstudied topics
        }

        val days = ((currentTime - topic.lastStudiedTimestamp).toDouble() / (1000.0 * 60 * 60 * 24)).coerceAtLeast(0.0)
        // Ebbinghaus forgetting curve approximation: R = exp(-0.03 * days)
        val retention = 100.0 * exp(-0.03 * days)
        return retention.coerceIn(0.0, 100.0)
    }

    /**
     * Calculates Centralized Mastery Score (0-100) and Level.
     * Safely handles missing data by normalizing active component weights.
     */
    fun calculateMasteryScore(
        topic: SyllabusItem,
        allMistakes: List<MistakeEntry> = emptyList(),
        currentTime: Long = System.currentTimeMillis()
    ): MasteryResult {
        val pyq = calculatePYQPerformance(topic)
        val conf = calculateConfidenceInfo(topic)
        val rev = calculateRevisionStrength(topic, currentTime)
        val comp = topic.completionPercentage.toDouble().coerceIn(0.0, 100.0)
        val mistakeCtrl = calculateMistakeControl(topic, allMistakes)
        val retention = calculateRetentionScore(topic, currentTime)

        val pyqHasData = pyq.status != PYQStatus.NO_DATA

        val pyqScoreVal = if (pyqHasData) pyq.accuracy else 0.0
        val confScoreVal = conf.normalized
        val revScoreVal = rev.score
        val compScoreVal = comp
        val mistakeScoreVal = mistakeCtrl.score
        val retentionScoreVal = retention

        val score = if (pyqHasData) {
            (pyqScoreVal * IntelligenceConfig.pyqAccuracyWeight) +
                    (confScoreVal * IntelligenceConfig.confidenceWeight) +
                    (revScoreVal * IntelligenceConfig.revisionStrengthWeight) +
                    (compScoreVal * IntelligenceConfig.completionWeight) +
                    (mistakeScoreVal * IntelligenceConfig.mistakeControlWeight) +
                    (retentionScoreVal * IntelligenceConfig.retentionWeight)
        } else {
            // Re-normalize remaining weights to sum to 1.0 (excluding PYQ 30% weight, sum = 0.70)
            val activeSumWeight = IntelligenceConfig.confidenceWeight +
                    IntelligenceConfig.revisionStrengthWeight +
                    IntelligenceConfig.completionWeight +
                    IntelligenceConfig.mistakeControlWeight +
                    IntelligenceConfig.retentionWeight

            val wConf = IntelligenceConfig.confidenceWeight / activeSumWeight
            val wRev = IntelligenceConfig.revisionStrengthWeight / activeSumWeight
            val wComp = IntelligenceConfig.completionWeight / activeSumWeight
            val wMistake = IntelligenceConfig.mistakeControlWeight / activeSumWeight
            val wRet = IntelligenceConfig.retentionWeight / activeSumWeight

            (confScoreVal * wConf) +
                    (revScoreVal * wRev) +
                    (compScoreVal * wComp) +
                    (mistakeScoreVal * wMistake) +
                    (retentionScoreVal * wRet)
        }

        val clampedScore = score.coerceIn(0.0, 100.0)
        val level = MasteryLevel.fromScore(clampedScore)

        val components = MasteryComponents(
            pyqAccuracy = if (pyqHasData) pyq.accuracy else -1.0, // -1 indicates NO_DATA
            confidence = confScoreVal,
            revisionStrength = revScoreVal,
            completion = compScoreVal,
            mistakeControl = mistakeScoreVal,
            retention = retentionScoreVal
        )

        return MasteryResult(
            score = clampedScore,
            level = level,
            components = components
        )
    }

    /**
     * Calculates Weakness Score (0-100).
     */
    fun calculateWeaknessScore(
        topic: SyllabusItem,
        masteryResult: MasteryResult,
        pyq: PYQPerformance,
        mistakesResult: MistakeControlResult
    ): Double {
        val baseWeakness = (100.0 - masteryResult.score).coerceIn(0.0, 100.0)

        var boost = 0.0
        if (mistakesResult.repeatedMistakes > 0) {
            boost += mistakesResult.repeatedMistakes * 15.0
        }
        if (mistakesResult.conceptGaps > 0) {
            boost += mistakesResult.conceptGaps * 15.0
        }
        if (pyq.status != PYQStatus.NO_DATA && pyq.accuracy < 50.0) {
            boost += 20.0
        }
        if (topic.confidence <= 2) {
            boost += 15.0
        }
        if (topic.isRevisionDue || topic.isWeak) {
            boost += 10.0
        }

        return (baseWeakness + boost).coerceIn(0.0, 100.0)
    }

    /**
     * Calculates Priority Score (0-100).
     */
    fun calculatePriorityScore(
        topic: SyllabusItem,
        weaknessScore: Double,
        mistakesResult: MistakeControlResult,
        revResult: RevisionStrengthResult,
        currentTime: Long = System.currentTimeMillis()
    ): Double {
        val weaknessComp = weaknessScore

        val importanceComp = when (topic.priority) {
            Priority.URGENT -> 100.0
            Priority.HIGH -> 80.0
            Priority.MEDIUM -> 50.0
            Priority.LOW -> 20.0
        } + (if (topic.isImportant) 15.0 else 0.0)

        val revisionUrgencyComp = when {
            revResult.overdue -> 100.0
            topic.isRevisionDue -> 75.0
            topic.isWeak || topic.status == ChapterStatus.WEAK -> 60.0
            else -> 0.0
        }

        val mistakeFrequencyComp = (mistakesResult.activeMistakes * 25.0 + mistakesResult.conceptGaps * 20.0).coerceIn(0.0, 100.0)

        val recencyComp = if (revResult.daysSinceRevision != null) {
            (revResult.daysSinceRevision * 3.0).coerceIn(0.0, 100.0)
        } else 50.0

        val priorityScore = (weaknessComp * IntelligenceConfig.priorityWeaknessWeight) +
                (importanceComp.coerceAtMost(100.0) * IntelligenceConfig.priorityExamImportanceWeight) +
                (revisionUrgencyComp * IntelligenceConfig.priorityRevisionUrgencyWeight) +
                (mistakeFrequencyComp * IntelligenceConfig.priorityMistakeFrequencyWeight) +
                (recencyComp * IntelligenceConfig.priorityRecencyWeight)

        return priorityScore.coerceIn(0.0, 100.0)
    }

    /**
     * Centralized test to determine if a topic is genuinely MASTERED.
     */
    fun isTopicMastered(
        topic: SyllabusItem,
        intelligence: TopicIntelligence
    ): Boolean {
        if (topic.completionPercentage < IntelligenceConfig.masteredMinCompletionPercentage) return false
        if (topic.confidence < IntelligenceConfig.masteredMinConfidence) return false
        if (topic.revisionCount < IntelligenceConfig.masteredMinRevisionCount) return false
        if (intelligence.mistakes.activeMistakes > 0) return false
        if (intelligence.mistakes.conceptGaps > 0) return false
        if (intelligence.pyq.status != PYQStatus.NO_DATA && intelligence.pyq.accuracy < IntelligenceConfig.masteredMinPYQAccuracy) return false

        return true
    }

    /**
     * Determines conceptual syllabus status without breaking existing enum values.
     */
    fun determineSyllabusStatus(
        topic: SyllabusItem,
        intelligence: TopicIntelligence
    ): ChapterStatus {
        if (intelligence.isMasteredCriteriaMet) {
            return ChapterStatus.MASTERED
        }
        if (topic.isRevisionDue || intelligence.revision.overdue) {
            return ChapterStatus.REVISION_DUE
        }
        if (intelligence.weaknessScore >= 65.0 || intelligence.masteryLevel == MasteryLevel.WEAK) {
            return ChapterStatus.WEAK
        }
        if (topic.completionPercentage >= 100) {
            return ChapterStatus.COMPLETED
        }
        if (topic.completionPercentage > 0) {
            return ChapterStatus.IN_PROGRESS
        }
        return ChapterStatus.NOT_STARTED
    }

    /**
     * Builds complete normalized Topic Intelligence object.
     */
    fun calculateTopicIntelligence(
        topic: SyllabusItem,
        allMistakes: List<MistakeEntry> = emptyList(),
        currentTime: Long = System.currentTimeMillis()
    ): TopicIntelligence {
        val pyq = calculatePYQPerformance(topic)
        val conf = calculateConfidenceInfo(topic)
        val mistakes = calculateMistakeControl(topic, allMistakes)
        val rev = calculateRevisionStrength(topic, currentTime)
        val mastery = calculateMasteryScore(topic, allMistakes, currentTime)
        val weakness = calculateWeaknessScore(topic, mastery, pyq, mistakes)
        val priority = calculatePriorityScore(topic, weakness, mistakes, rev, currentTime)

        val partialIntel = TopicIntelligence(
            topicId = topic.id,
            topicTitle = topic.title,
            masteryScore = mastery.score,
            masteryLevel = mastery.level,
            pyq = pyq,
            confidence = conf,
            mistakes = mistakes,
            revision = rev,
            weaknessScore = weakness,
            priorityScore = priority,
            status = topic.status,
            isMasteredCriteriaMet = false,
            masteryComponents = mastery.components
        )

        val masteredMet = isTopicMastered(topic, partialIntel)
        val conceptualStatus = determineSyllabusStatus(topic, partialIntel.copy(isMasteredCriteriaMet = masteredMet))

        return partialIntel.copy(
            status = conceptualStatus,
            isMasteredCriteriaMet = masteredMet
        )
    }
}
