package com.example.domain

import com.example.data.model.MockTest
import com.example.data.repository.AnalyticsRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MockTestStatisticsTest {

    private lateinit var analyticsRepository: AnalyticsRepository

    @Before
    fun setup() {
        analyticsRepository = AnalyticsRepository()
    }

    @Test
    fun calculateMockStats_emptyList_returnsDefaultStats() {
        val stats = analyticsRepository.calculateMockStats(emptyList())

        assertEquals(0, stats.totalMocksCount)
        assertEquals(0f, stats.averageScore, 0.001f)
        assertEquals(0f, stats.highestScore, 0.001f)
        assertEquals(0f, stats.latestScore, 0.001f)
        assertEquals(0f, stats.averagePercentile, 0.001f)
        assertEquals(0f, stats.bestPercentile, 0.001f)
        assertEquals(0, stats.cutoffClearanceRate)
    }

    @Test
    fun calculateMockStats_singleMockTest_calculatesExactMetrics() {
        val mock = MockTest(
            id = 1,
            testName = "Mock 01",
            testDateStr = "2026-08-01",
            totalMarks = 200f,
            marksScored = 150f,
            totalQuestions = 100,
            attemptedQuestions = 90,
            correctQuestions = 80,
            incorrectQuestions = 10,
            cutoffMarks = 130f,
            percentile = 92.5f,
            accuracy = 88.8f
        )

        val stats = analyticsRepository.calculateMockStats(listOf(mock))

        assertEquals(1, stats.totalMocksCount)
        assertEquals(150f, stats.averageScore, 0.001f)
        assertEquals(150f, stats.highestScore, 0.001f)
        assertEquals(150f, stats.latestScore, 0.001f)
        assertEquals(92.5f, stats.averagePercentile, 0.001f)
        assertEquals(92.5f, stats.bestPercentile, 0.001f)
        assertEquals(88.8f, stats.averageAccuracy, 0.001f)
        assertEquals(1, stats.clearedCutoffCount)
        assertEquals(100, stats.cutoffClearanceRate)
    }

    @Test
    fun calculateMockStats_multipleMockTests_calculatesAveragesAndClearanceRate() {
        val mocks = listOf(
            MockTest(
                id = 1, testName = "Mock 01", testDateStr = "2026-08-01",
                marksScored = 160f, cutoffMarks = 140f, percentile = 95f, accuracy = 90f
            ),
            MockTest(
                id = 2, testName = "Mock 02", testDateStr = "2026-08-10",
                marksScored = 120f, cutoffMarks = 140f, percentile = 75f, accuracy = 70f
            )
        )

        val stats = analyticsRepository.calculateMockStats(mocks)

        assertEquals(2, stats.totalMocksCount)
        // Average score: (160 + 120) / 2 = 140
        assertEquals(140f, stats.averageScore, 0.001f)
        assertEquals(160f, stats.highestScore, 0.001f)
        assertEquals(160f, stats.latestScore, 0.001f)
        // Average percentile: (95 + 75) / 2 = 85
        assertEquals(85f, stats.averagePercentile, 0.001f)
        assertEquals(95f, stats.bestPercentile, 0.001f)
        // Average accuracy: (90 + 70) / 2 = 80
        assertEquals(80f, stats.averageAccuracy, 0.001f)
        // 1 out of 2 cleared cutoff -> 50%
        assertEquals(1, stats.clearedCutoffCount)
        assertEquals(50, stats.cutoffClearanceRate)
    }
}
