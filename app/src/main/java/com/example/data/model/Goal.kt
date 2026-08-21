package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateStr: String, // YYYY-MM-DD
    val subjectId: Long? = null,
    val subjectName: String = "All Subjects",
    val targetChaptersCount: Int = 0,
    val completedChaptersCount: Int = 0,
    val targetStudyHours: Float = 0f,
    val isCompleted: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
