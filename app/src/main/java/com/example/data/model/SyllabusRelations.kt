package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A Room relational POJO that represents a Sub-Section with all its nested Chapters.
 */
data class SubSectionWithChapters(
    @Embedded
    val subSection: SyllabusItem,

    @Relation(
        parentColumn = "id",
        entityColumn = "parentId",
        entity = SyllabusItem::class
    )
    val chapters: List<SyllabusItem>
)

/**
 * A Room relational POJO that represents a Section with all its nested Sub-Sections.
 */
data class SectionWithSubSections(
    @Embedded
    val section: SyllabusItem,

    @Relation(
        parentColumn = "id",
        entityColumn = "parentId",
        entity = SyllabusItem::class
    )
    val subSections: List<SubSectionWithChapters>
)

/**
 * A Room relational POJO representing the complete hierarchical structure of a Subject:
 * Subject -> Section -> Sub-Section -> Chapter.
 */
data class SubjectHierarchy(
    @Embedded
    val subject: Subject,

    @Relation(
        parentColumn = "id",
        entityColumn = "subjectId",
        entity = SyllabusItem::class
    )
    val sections: List<SectionWithSubSections>
)
