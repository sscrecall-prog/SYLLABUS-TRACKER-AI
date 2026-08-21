package com.example.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AppDatabaseMigrations
import com.example.data.model.ChapterStatus
import com.example.data.model.ItemType
import com.example.data.model.SyllabusItem
import com.example.data.model.Subject
import com.example.data.repository.SyllabusRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SyllabusRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var syllabusRepository: SyllabusRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addMigrations(*AppDatabaseMigrations.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        syllabusRepository = SyllabusRepository(database.syllabusDao(), database.settingsDao())

        runBlocking {
            database.subjectDao().insertSubject(Subject(id = 1, name = "Math", code = "M", iconName = "m", colorHex = "#000"))
            database.subjectDao().insertSubject(Subject(id = 2, name = "English", code = "E", iconName = "e", colorHex = "#111"))
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetItemsForSubject_returnsFilteredItems() = runBlocking {
        val item1 = SyllabusItem(subjectId = 1, title = "Math Ch 1", itemType = ItemType.CHAPTER)
        val item2 = SyllabusItem(subjectId = 1, title = "Math Ch 2", itemType = ItemType.CHAPTER)
        val item3 = SyllabusItem(subjectId = 2, title = "Eng Ch 1", itemType = ItemType.CHAPTER)

        syllabusRepository.insertItem(item1)
        syllabusRepository.insertItem(item2)
        syllabusRepository.insertItem(item3)

        val mathItems = syllabusRepository.getItemsForSubject(1).first()

        assertEquals(2, mathItems.size)
        assertTrue(mathItems.all { it.subjectId == 1L })
    }

    @Test
    fun duplicateItem_duplicatesItemAndDirectChildren() = runBlocking {
        val parent = SyllabusItem(subjectId = 1, title = "Geometry", itemType = ItemType.SECTION)
        val parentId = syllabusRepository.insertItem(parent)

        val child = SyllabusItem(subjectId = 1, parentId = parentId, title = "Triangles", itemType = ItemType.CHAPTER)
        syllabusRepository.insertItem(child)

        val parentItemToDup = database.syllabusDao().getItemById(parentId)
        assertNotNull(parentItemToDup)

        syllabusRepository.duplicateItem(parentItemToDup!!)

        val allItems = syllabusRepository.allItems.first()
        // Original parent + original child + duplicated parent + duplicated child = 4 items
        assertEquals(4, allItems.size)
        assertTrue(allItems.any { it.title == "Geometry (Copy)" })
    }

    @Test
    fun updateChapterStatus_completed_setsCompletionAndNextRevision() = runBlocking {
        val chapter = SyllabusItem(
            subjectId = 1,
            title = "Percentage",
            itemType = ItemType.CHAPTER,
            status = ChapterStatus.NOT_STARTED,
            completionPercentage = 0
        )
        val id = syllabusRepository.insertItem(chapter)
        val item = database.syllabusDao().getItemById(id)
        assertNotNull(item)

        syllabusRepository.updateChapterStatus(item!!, ChapterStatus.COMPLETED)

        val updated = database.syllabusDao().getItemById(id)
        assertEquals(ChapterStatus.COMPLETED, updated?.status)
        assertEquals(100, updated?.completionPercentage)
        assertNotNull(updated?.lastStudiedTimestamp)
        assertNotNull(updated?.nextRevisionTimestamp)
    }

    @Test
    fun deleteItem_deletesItemAndChildrenCascade() = runBlocking {
        val section = SyllabusItem(subjectId = 1, title = "Sec 1", itemType = ItemType.SECTION)
        val sectionId = syllabusRepository.insertItem(section)

        val chap = SyllabusItem(subjectId = 1, parentId = sectionId, title = "Chap 1", itemType = ItemType.CHAPTER)
        syllabusRepository.insertItem(chap)

        val sectionItem = database.syllabusDao().getItemById(sectionId)
        assertNotNull(sectionItem)

        syllabusRepository.deleteItem(sectionItem!!)

        val remaining = syllabusRepository.allItems.first()
        assertTrue(remaining.isEmpty())
    }
}
