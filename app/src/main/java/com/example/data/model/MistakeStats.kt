package com.example.data.model

data class MistakeStats(
    val totalMistakesCount: Int = 0,
    val activeMistakesCount: Int = 0,
    val understoodCount: Int = 0,
    val masteredCount: Int = 0,
    val reviewDueCount: Int = 0,
    val starredCount: Int = 0,
    val resolutionRatePercent: Int = 0,
    val sillyMistakesPercent: Int = 0,
    val conceptGapPercent: Int = 0,
    val formulaForgotPercent: Int = 0,
    val calculationErrorPercent: Int = 0,
    val timePanicPercent: Int = 0,
    val mostVulnerableSubject: String = "Maths"
)
