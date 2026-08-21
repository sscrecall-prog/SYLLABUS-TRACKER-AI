package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "syllabus_items",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subjectId"]),
        Index(value = ["parentId"]),
        Index(value = ["status"])
    ]
)
data class SyllabusItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val parentId: Long? = null, // null for top-level Sections; or parent section/subsection/chapter ID
    val itemType: ItemType = ItemType.CHAPTER,
    val title: String,
    val orderIndex: Int = 0,
    
    // Status & Progress
    val status: ChapterStatus = ChapterStatus.NOT_STARTED,
    val completionPercentage: Int = 0, // 0 to 100
    val confidence: Int = 3, // 1 to 5
    val priority: Priority = Priority.MEDIUM,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    
    // Notes & Meta
    val notes: String = "",
    val isImportant: Boolean = false,
    val isBookmarked: Boolean = false,
    
    // Revision & Spaced Repetition
    val lastStudiedTimestamp: Long? = null,
    val nextRevisionTimestamp: Long? = null,
    val studyTimeMinutes: Int = 0,
    val revisionCount: Int = 0,
    val tags: String = "", // Comma-separated tags e.g. "#PYQ,#Formula"
    
    // PYQ / Question tracking
    val pyqTotal: Int = 0,
    val pyqAttempted: Int = 0,
    val pyqCorrect: Int = 0
) {
    val pyqAccuracy: Int
        get() = if (pyqAttempted > 0) ((pyqCorrect.toFloat() / pyqAttempted) * 100).toInt() else 0

    val isWeak: Boolean
        get() = status == ChapterStatus.WEAK || confidence <= 2 || (pyqAttempted >= 5 && pyqAccuracy < 60)

    val isRevisionDue: Boolean
        get() = status == ChapterStatus.REVISION_DUE || 
                (nextRevisionTimestamp != null && nextRevisionTimestamp <= System.currentTimeMillis())

    val tagList: List<String>
        get() = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
