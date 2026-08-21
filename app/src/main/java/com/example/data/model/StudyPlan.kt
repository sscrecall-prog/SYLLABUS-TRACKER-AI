package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_plans")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateStr: String, // YYYY-MM-DD
    val timeStr: String = "09:00", // e.g. "08:00"
    val subjectId: Long,
    val subjectName: String,
    val chapterId: Long? = null,
    val chapterTitle: String = "",
    val plannedMinutes: Int = 60,
    val actualMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val goalNotes: String = ""
)
