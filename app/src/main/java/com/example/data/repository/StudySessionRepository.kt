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

class StudySessionRepository(
    private val studySessionDao: StudySessionDao,
    private val syllabusDao: SyllabusDao
) {
    val allStudySessions: Flow<List<StudySession>> = studySessionDao.getAllSessions()

    suspend fun logStudySession(
        subjectId: Long,
        subjectName: String,
        chapterId: Long?,
        chapterTitle: String,
        durationSeconds: Long,
        mode: TimerMode,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val session = StudySession(
            subjectId = subjectId,
            subjectName = subjectName,
            chapterId = chapterId,
            chapterTitle = chapterTitle,
            durationSeconds = durationSeconds,
            mode = mode,
            notes = notes
        )
        studySessionDao.insertSession(session)

        // If linked to chapter, update study time
        if (chapterId != null) {
            val chapter = syllabusDao.getItemById(chapterId)
            if (chapter != null) {
                val addedMinutes = (durationSeconds / 60).toInt().coerceAtLeast(1)
                syllabusDao.updateItem(
                    chapter.copy(
                        studyTimeMinutes = chapter.studyTimeMinutes + addedMinutes,
                        lastStudiedTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // Study Planner CRUD

}
