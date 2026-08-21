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

class SyllabusRepository(
    private val syllabusDao: SyllabusDao,
    private val settingsDao: SettingsDao
) {
    val allItems: Flow<List<SyllabusItem>> = syllabusDao.getAllItems()

    val allSubjectHierarchies: Flow<List<SubjectHierarchy>> = syllabusDao.getAllSubjectHierarchies()


    fun getItemsForSubject(subjectId: Long): Flow<List<SyllabusItem>> =
        syllabusDao.getItemsForSubject(subjectId)


    fun searchItems(query: String): Flow<List<SyllabusItem>> =
        syllabusDao.searchItems(query)

    // Subject CRUD

    suspend fun insertItem(item: SyllabusItem): Long = withContext(Dispatchers.IO) {
        syllabusDao.insertItem(item)
    }


    suspend fun insertItems(items: List<SyllabusItem>): List<Long> = withContext(Dispatchers.IO) {
        syllabusDao.insertItems(items)
    }


    suspend fun updateItem(item: SyllabusItem) = withContext(Dispatchers.IO) {
        syllabusDao.updateItem(item)
    }


    suspend fun deleteItem(item: SyllabusItem) = withContext(Dispatchers.IO) {
        syllabusDao.deleteItemAndChildren(item.id)
    }


    suspend fun duplicateItem(item: SyllabusItem) = withContext(Dispatchers.IO) {
        val newItem = item.copy(
            id = 0,
            title = "${item.title} (Copy)",
            orderIndex = item.orderIndex + 1
        )
        val newId = syllabusDao.insertItem(newItem)

        // Duplicate direct children if any
        val children = syllabusDao.getItemsByParent(item.id).first()
        for (child in children) {
            val newChild = child.copy(
                id = 0,
                parentId = newId
            )
            syllabusDao.insertItem(newChild)
        }
    }


    suspend fun moveItem(item: SyllabusItem, newParentId: Long?) = withContext(Dispatchers.IO) {
        syllabusDao.updateItem(item.copy(parentId = newParentId))
    }


    suspend fun reorderItems(items: List<SyllabusItem>) = withContext(Dispatchers.IO) {
        val updated = items.mapIndexed { index, item -> item.copy(orderIndex = index) }
        syllabusDao.updateItems(updated)
    }

    // Spaced Repetition Logic

    suspend fun updateChapterStatus(chapter: SyllabusItem, newStatus: ChapterStatus) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val settings = settingsDao.getSettingsDirect() ?: AppSettings()
        val intervals = settings.revisionIntervals

        val updated = when (newStatus) {
            ChapterStatus.COMPLETED -> {
                val nextRev = if (chapter.nextRevisionTimestamp == null || chapter.nextRevisionTimestamp <= now) {
                    now + (intervals.firstOrNull() ?: 1) * 24L * 60 * 60 * 1000L
                } else chapter.nextRevisionTimestamp
                chapter.copy(
                    status = newStatus,
                    completionPercentage = 100,
                    lastStudiedTimestamp = now,
                    nextRevisionTimestamp = nextRev
                )
            }
            ChapterStatus.MASTERED -> {
                chapter.copy(status = newStatus, completionPercentage = 100, confidence = 5)
            }
            ChapterStatus.NOT_STARTED -> {
                chapter.copy(status = newStatus, completionPercentage = 0)
            }
            ChapterStatus.WEAK -> {
                chapter.copy(status = newStatus, confidence = chapter.confidence.coerceAtMost(2))
            }
            ChapterStatus.REVISION_DUE -> {
                chapter.copy(status = newStatus, nextRevisionTimestamp = now)
            }
            else -> {
                chapter.copy(status = newStatus)
            }
        }
        syllabusDao.updateItem(updated)
    }

    // Study Sessions & Timer

}
