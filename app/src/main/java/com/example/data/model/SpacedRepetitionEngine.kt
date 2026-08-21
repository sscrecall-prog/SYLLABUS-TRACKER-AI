package com.example.data.model

import java.util.Calendar
import java.util.Date

object SpacedRepetitionEngine {
    /**
     * Calculates the optimal next revision date (timestamp) based on custom spacing algorithms:
     * - `status`: Current ChapterStatus (MASTERED, COMPLETED, WEAK, IN_PROGRESS, REVISION_DUE)
     * - `confidence`: Self-assessed confidence (1 to 5)
     * - `pyqAccuracy`: Accuracy on previous attempts (0 to 100)
     * - `revisionCount`: Number of times already revised
     * - `lastStudied`: Last studied timestamp
     *
     * Returns: Next optimal revision timestamp (Long)
     */
    fun calculateNextRevision(
        item: SyllabusItem,
        userSelectedDays: Int? = null
    ): Long {
        val now = System.currentTimeMillis()
        if (userSelectedDays != null) {
            return now + userSelectedDays * 24 * 60 * 60 * 1000L
        }

        // Base interval in days based on status and confidence
        val baseInterval = when (item.status) {
            ChapterStatus.MASTERED -> 15.0
            ChapterStatus.COMPLETED -> 7.0
            ChapterStatus.REVISION_DUE -> 3.0
            ChapterStatus.WEAK -> 1.0
            ChapterStatus.IN_PROGRESS, ChapterStatus.LEARNING -> 2.0
            ChapterStatus.NOT_STARTED -> 3.0
        }

        // Confidence multiplier (1 to 5)
        // confidence = 1: multiplier = 0.4 (need revision soon)
        // confidence = 3: multiplier = 1.0 (standard)
        // confidence = 5: multiplier = 2.2 (good retention)
        val confidenceMultiplier = when (item.confidence) {
            1 -> 0.4
            2 -> 0.7
            3 -> 1.0
            4 -> 1.5
            5 -> 2.2
            else -> 1.0
        }

        // PYQ accuracy multiplier
        // If accuracy is high (>85%), double the interval. If low (<50%), shrink it.
        val accuracyMultiplier = when {
            item.pyqAttempted == 0 -> 1.0
            item.pyqAccuracy >= 85 -> 1.8
            item.pyqAccuracy >= 70 -> 1.3
            item.pyqAccuracy >= 50 -> 1.0
            item.pyqAccuracy >= 30 -> 0.6
            else -> 0.4
        }

        // Revision count progression (the more you revise, the longer the intervals get)
        // e.g. interval = base * confidenceMultiplier * accuracyMultiplier * (1.6 ^ revisionCount)
        val factor = Math.pow(1.6, item.revisionCount.toDouble().coerceAtMost(5.0))

        val calculatedDays = (baseInterval * confidenceMultiplier * accuracyMultiplier * factor).coerceIn(1.0, 90.0)
        return now + (calculatedDays * 24 * 60 * 60 * 1000L).toLong()
    }
}
