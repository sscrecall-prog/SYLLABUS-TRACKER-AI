package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MockTestType(val label: String, val hexColor: String) {
    FULL_LENGTH("Full Length", "#2D4F1E"),
    SECTIONAL("Sectional", "#3F51B5"),
    TOPIC_TEST("Chapter Test", "#8E24AA"),
    PREVIOUS_YEAR("PYQ Paper", "#E27D60")
}

@Entity(tableName = "mock_tests")
data class MockTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testName: String,
    val testType: MockTestType = MockTestType.FULL_LENGTH,
    val testPlatform: String = "Testbook", // Testbook, Oliveboard, Gradeup, PracticeMock, Unacademy, Custom
    val testDateStr: String, // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis(),
    val totalMarks: Float = 200f,
    val marksScored: Float = 0f,
    val totalQuestions: Int = 100,
    val attemptedQuestions: Int = 0,
    val correctQuestions: Int = 0,
    val incorrectQuestions: Int = 0,
    val accuracy: Float = if (attemptedQuestions > 0) (correctQuestions.toFloat() / attemptedQuestions.toFloat()) * 100f else 0f,
    val percentile: Float = 0f,
    val rank: Int = 0,
    val totalStudents: Int = 0,
    val cutoffMarks: Float = 135f,
    val timeTakenMinutes: Int = 60,
    val mathScore: Float = 0f,
    val mathTotal: Float = 50f,
    val englishScore: Float = 0f,
    val englishTotal: Float = 50f,
    val reasoningScore: Float = 0f,
    val reasoningTotal: Float = 50f,
    val gsScore: Float = 0f,
    val gsTotal: Float = 50f,
    val weakAreasIdentified: String = "",
    val analysisNotes: String = "",
    val isClearedCutoff: Boolean = marksScored >= cutoffMarks
)
