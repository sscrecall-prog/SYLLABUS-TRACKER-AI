package com.example.data.intelligence

/**
 * Centralized Configuration for Core Intelligence & Adaptive Planning Engine
 * All weights, thresholds, and mastery criteria are configured here.
 */
object IntelligenceConfig {
    // -------------------------------------------------------------------------
    // Sprint 1: Mastery Weights (Sum = 1.0)
    // -------------------------------------------------------------------------
    var pyqAccuracyWeight: Double = 0.30
    var confidenceWeight: Double = 0.20
    var revisionStrengthWeight: Double = 0.20
    var completionWeight: Double = 0.15
    var mistakeControlWeight: Double = 0.10
    var retentionWeight: Double = 0.05

    // -------------------------------------------------------------------------
    // Sprint 1: Topic Priority Weights (Sum = 1.0)
    // -------------------------------------------------------------------------
    var priorityWeaknessWeight: Double = 0.35
    var priorityExamImportanceWeight: Double = 0.25
    var priorityRevisionUrgencyWeight: Double = 0.20
    var priorityMistakeFrequencyWeight: Double = 0.15
    var priorityRecencyWeight: Double = 0.05

    // -------------------------------------------------------------------------
    // Sprint 1: Mastery Score Thresholds
    // -------------------------------------------------------------------------
    const val WEAK_MAX_SCORE: Double = 39.99
    const val LEARNING_MAX_SCORE: Double = 59.99
    const val STRONG_MAX_SCORE: Double = 79.99
    // MASTERED: 80.0 to 100.0

    // Mastered Criteria Constraints
    var masteredMinCompletionPercentage: Int = 100
    var masteredMinPYQAccuracy: Double = 85.0
    var masteredMinConfidence: Int = 4
    var masteredMinRevisionCount: Int = 1

    // -------------------------------------------------------------------------
    // Sprint 2: Exam Readiness Weights (Standard Sum = 1.0)
    // -------------------------------------------------------------------------
    var readinessSyllabusCoverageWeight: Double = 0.15
    var readinessMasteryWeight: Double = 0.25
    var readinessPyqWeight: Double = 0.20
    var readinessRevisionCoverageWeight: Double = 0.15
    var readinessMistakeControlWeight: Double = 0.10
    var readinessMockPerformanceWeight: Double = 0.15

    // -------------------------------------------------------------------------
    // Sprint 2: Readiness Score Thresholds
    // -------------------------------------------------------------------------
    const val READINESS_CRITICAL_MAX: Double = 39.99
    const val READINESS_WEAK_MAX: Double = 59.99
    const val READINESS_PREPARING_MAX: Double = 74.99
    const val READINESS_STRONG_MAX: Double = 89.99
    // EXAM_READY: 90.0 to 100.0

    // -------------------------------------------------------------------------
    // Sprint 2: Last-Days Exam Mode Thresholds (in days)
    // -------------------------------------------------------------------------
    var lastDaysFoundationThreshold: Int = 60
    var lastDaysAccelerationThreshold: Int = 30
    var lastDaysIntensiveThreshold: Int = 7

    // -------------------------------------------------------------------------
    // Sprint 2: Daily Planner Settings
    // -------------------------------------------------------------------------
    var defaultDailyBudgetMinutes: Int = 120
    var minActionDurationMinutes: Int = 15
    var maxActionDurationMinutes: Int = 45
}
