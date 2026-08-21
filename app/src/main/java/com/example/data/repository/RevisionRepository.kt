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

class RevisionRepository(
    private val syllabusDao: SyllabusDao,
    private val settingsDao: SettingsDao
) {
    suspend fun markChapterRevised(chapter: SyllabusItem) = withContext(Dispatchers.IO) {
        val settings = settingsDao.getSettingsDirect() ?: AppSettings()
        val intervals = settings.revisionIntervals
        val currentRevCount = chapter.revisionCount + 1
        
        val intervalIndex = (currentRevCount - 1).coerceAtMost(intervals.size - 1)
        val daysToAdd = intervals[intervalIndex]
        val now = System.currentTimeMillis()
        val nextDate = now + (daysToAdd.toLong() * 24 * 60 * 60 * 1000L)

        val updated = chapter.copy(
            revisionCount = currentRevCount,
            lastStudiedTimestamp = now,
            nextRevisionTimestamp = nextDate,
            status = if (currentRevCount >= 5) ChapterStatus.MASTERED else ChapterStatus.COMPLETED,
            completionPercentage = 100
        )
        syllabusDao.updateItem(updated)
    }


    suspend fun scheduleCustomRevision(chapter: SyllabusItem, targetTimestamp: Long) = withContext(Dispatchers.IO) {
        syllabusDao.updateItem(
            chapter.copy(
                nextRevisionTimestamp = targetTimestamp,
                status = if (targetTimestamp <= System.currentTimeMillis()) ChapterStatus.REVISION_DUE else chapter.status
            )
        )
    }


}
