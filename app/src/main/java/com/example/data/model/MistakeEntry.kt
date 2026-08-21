package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MistakeCategory(
    val label: String,
    val emoji: String,
    val colorHex: String,
    val description: String
) {
    SILLY_MISTAKE("Silly Mistake", "🤦", "#FF7043", "Read question wrong or marked wrong option in hurry"),
    CONCEPT_GAP("Concept Gap", "🧩", "#AB47BC", "Did not understand the underlying concept or theorem"),
    FORMULA_FORGOT("Formula Forgot", "🧠", "#FFA726", "Forgot specific identity, shortcut formula or rule"),
    CALCULATION_ERROR("Calculation Error", "🔢", "#EF5350", "Addition, division, sign (+/-) or unit conversion slip"),
    TIME_PANIC("Time Panic / Rush", "⏰", "#EC407A", "Ran out of time in mock test and guessed under pressure"),
    NEW_PATTERN("New / Unseen Pattern", "💡", "#42A5F5", "Brand new question variation not seen in past year papers"),
    VOCAB_CONFUSION("Vocab / Grammar Rule", "📖", "#26A69A", "Grammar rule exception, idiom meaning or spelling trap")
}

enum class MistakeResolutionStatus(
    val label: String,
    val emoji: String,
    val colorHex: String
) {
    ACTIVE("Active / Needs Review", "🔴", "#EF5350"),
    UNDERSTOOD("Concept Understood", "🟡", "#FFA726"),
    MASTERED("Mastered & Resolved", "🟢", "#66BB6A")
}

@Entity(tableName = "mistake_entries")
data class MistakeEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionText: String,
    val yourWrongAnswer: String = "",
    val correctAnswer: String = "",
    val explanationOrKeyConcept: String = "",
    val subjectId: Long,
    val subjectName: String,
    val chapterTitle: String = "",
    val sourceMockOrBook: String = "", // e.g. "Testbook Live Mock #14" or "Pinnacle PYQ"
    val category: MistakeCategory = MistakeCategory.SILLY_MISTAKE,
    val resolutionStatus: MistakeResolutionStatus = MistakeResolutionStatus.ACTIVE,
    val reviewCount: Int = 0,
    val lastReviewedTimestamp: Long = 0L,
    val nextReviewTimestamp: Long = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L), // 3 days default
    val importanceStar: Boolean = false,
    val tagsCsv: String = "", // e.g. "Tier-2, Geometry, Circle"
    val createdTimestamp: Long = System.currentTimeMillis()
) {
    val isReviewDue: Boolean
        get() = resolutionStatus != MistakeResolutionStatus.MASTERED && 
                nextReviewTimestamp <= System.currentTimeMillis()

    val formattedCreatedDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(createdTimestamp))

    val formattedNextReviewDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(nextReviewTimestamp))
}
