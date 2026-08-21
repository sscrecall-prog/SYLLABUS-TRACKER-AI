package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.map

class MistakeRepository(
    private val mistakeDao: MistakeDao
) {
    val allMistakes: Flow<List<MistakeEntry>> = mistakeDao.getAllMistakes()

    suspend fun insertMistake(mistake: MistakeEntry): Long = withContext(Dispatchers.IO) {
        mistakeDao.insertMistake(mistake)
    }


    suspend fun updateMistake(mistake: MistakeEntry) = withContext(Dispatchers.IO) {
        mistakeDao.updateMistake(mistake)
    }


    suspend fun deleteMistake(mistake: MistakeEntry) = withContext(Dispatchers.IO) {
        mistakeDao.deleteMistake(mistake)
    }


    suspend fun markMistakeReviewed(mistake: MistakeEntry, newStatus: MistakeResolutionStatus) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val nextReviewDays = when (newStatus) {
            MistakeResolutionStatus.ACTIVE -> 1L // Review again tomorrow
            MistakeResolutionStatus.UNDERSTOOD -> 4L // Review in 4 days
            MistakeResolutionStatus.MASTERED -> 14L // Mastered, check in 2 weeks or archive
        }
        val nextTimestamp = now + (nextReviewDays * 24 * 60 * 60 * 1000L)
        val updated = mistake.copy(
            resolutionStatus = newStatus,
            reviewCount = mistake.reviewCount + 1,
            lastReviewedTimestamp = now,
            nextReviewTimestamp = nextTimestamp
        )
        mistakeDao.updateMistake(updated)
    }


    suspend fun toggleMistakeStar(mistake: MistakeEntry) = withContext(Dispatchers.IO) {
        mistakeDao.updateMistake(mistake.copy(importanceStar = !mistake.importanceStar))
    }

    // Settings

}
