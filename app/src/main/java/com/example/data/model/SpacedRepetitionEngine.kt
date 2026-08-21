package com.example.data.model

import com.example.data.intelligence.CoreIntelligenceEngine
import com.example.data.intelligence.PYQStatus

object SpacedRepetitionEngine {
    /**
     * Calculates the optimal next revision date (timestamp) based on custom spacing algorithms:
     * - Uses baseline 1-3-7-21 progression adjusted by performance signals
     * - High accuracy (>=85%) & high confidence (>=4) -> expands interval (e.g., 7 -> 21 days)
     * - Medium accuracy (60-84%) -> normal interval
     * - Weak accuracy (<60%) -> shortens interval (e.g., 7 -> 3 days)
     * - Very weak (<40%) or concept gaps -> immediate / 1-day revision
     * - `userSelectedDays`: Optional manual override in days
     */
    fun calculateNextRevision(
        item: SyllabusItem,
        allMistakes: List<MistakeEntry> = emptyList(),
        userSelectedDays: Int? = null
    ): Long {
        val now = System.currentTimeMillis()
        if (userSelectedDays != null && userSelectedDays > 0) {
            return now + userSelectedDays * 24 * 60 * 60 * 1000L
        }

        // Standard 1-3-7-14-21-30 baseline intervals based on revision count
        val baseIntervalDays = when (item.revisionCount) {
            0 -> 1.0
            1 -> 3.0
            2 -> 7.0
            3 -> 14.0
            4 -> 21.0
            else -> 30.0
        }

        val pyq = CoreIntelligenceEngine.calculatePYQPerformance(item)
        val mistakesCtrl = CoreIntelligenceEngine.calculateMistakeControl(item, allMistakes)
        val confInfo = CoreIntelligenceEngine.calculateConfidenceInfo(item)

        // Adaptive performance multiplier
        val adaptiveMultiplier = when {
            // Very weak performance (<40% accuracy OR unresolved concept gaps)
            (pyq.status != PYQStatus.NO_DATA && pyq.accuracy < 40.0) || mistakesCtrl.conceptGaps > 0 -> 0.33 // forces ~1 day
            // Weak performance (<60% accuracy OR confidence <= 2)
            (pyq.status != PYQStatus.NO_DATA && pyq.accuracy < 60.0) || confInfo.value <= 2 -> 0.5 // e.g. 7 -> 3.5 days
            // Strong performance (>=85% accuracy AND confidence >= 4)
            (pyq.status != PYQStatus.NO_DATA && pyq.accuracy >= 85.0) && confInfo.value >= 4 -> 2.5 // e.g. 7 -> 17.5-21 days
            // Good performance (70-84% accuracy)
            (pyq.status != PYQStatus.NO_DATA && pyq.accuracy >= 70.0) -> 1.3
            // Medium / Baseline
            else -> 1.0
        }

        val confidenceFactor = when (confInfo.value) {
            1 -> 0.6
            2 -> 0.8
            3 -> 1.0
            4 -> 1.25
            5 -> 1.5
            else -> 1.0
        }

        val finalIntervalDays = (baseIntervalDays * adaptiveMultiplier * confidenceFactor).coerceIn(1.0, 90.0)
        return now + (finalIntervalDays * 24 * 60 * 60 * 1000L).toLong()
    }
}
