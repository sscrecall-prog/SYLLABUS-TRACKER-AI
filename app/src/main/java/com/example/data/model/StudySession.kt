package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val chapterId: Long? = null,
    val chapterTitle: String = "",
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: TimerMode = TimerMode.POMODORO,
    val notes: String = ""
)
