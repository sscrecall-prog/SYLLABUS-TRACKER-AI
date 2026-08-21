package com.example.domain

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AppDatabaseMigrations
import com.example.data.model.*
import com.example.data.repository.RevisionRepository
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
class RevisionTest {

    private lateinit var database: AppDatabase
    private lateinit var revisionRepository: RevisionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addMigrations(*AppDatabaseMigrations.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        revisionRepository = RevisionRepository(database.syllabusDao(), database.settingsDao())
        
        runBlocking {
            database.subjectDao().insertSubject(
                Subject(id = 10, name = "Math", code = "M", iconName = "icon", colorHex = "#000")
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun markChapterRevised_firstRevision_calculatesIntervalAndProgression() = runBlocking {
        val syllabusDao = database.syllabusDao()
        val settingsDao = database.settingsDao()

        // Default revision intervals in AppSettings: [1, 3, 7, 14, 30]
        val appSettings = AppSettings(revisionIntervalsCsv = "1,3,7,14,30")
        settingsDao.insertOrUpdate(appSettings)

        val chapter = SyllabusItem(
            id = 1,
            subjectId = 10,
            title = "Percentage",
            itemType = ItemType.CHAPTER,
            status = ChapterStatus.NOT_STARTED,
            revisionCount = 0
        )
        val insertedId = syllabusDao.insertItem(chapter)
        val insertedItem = chapter.copy(id = insertedId)

        val beforeTime = System.currentTimeMillis()
        revisionRepository.markChapterRevised(insertedItem)
        val afterTime = System.currentTimeMillis()

        val updatedItem = syllabusDao.getItemById(insertedId)
        assertNotNull(updatedItem)
        assertEquals(1, updatedItem?.revisionCount)
        assertEquals(100, updatedItem?.completionPercentage)
        assertEquals(ChapterStatus.COMPLETED, updatedItem?.status)

        // Verify last studied and next revision timestamp
        val lastStudied = updatedItem?.lastStudiedTimestamp ?: 0L
        val nextRev = updatedItem?.nextRevisionTimestamp ?: 0L

        assertTrue(lastStudied in beforeTime..afterTime)
        // Interval index 0 -> 1 day = 86400000 ms
        val expectedMinNext = lastStudied + 1 * 24 * 60 * 60 * 1000L
        assertEquals(expectedMinNext, nextRev)
    }

    @Test
    fun markChapterRevised_fifthRevision_marksChapterMastered() = runBlocking {
        val syllabusDao = database.syllabusDao()
        val settingsDao = database.settingsDao()
        settingsDao.insertOrUpdate(AppSettings(revisionIntervalsCsv = "1,3,7,14,30"))

        val chapter = SyllabusItem(
            id = 2,
            subjectId = 10,
            title = "Algebra",
            itemType = ItemType.CHAPTER,
            status = ChapterStatus.COMPLETED,
            revisionCount = 4
        )
        val insertedId = syllabusDao.insertItem(chapter)

        revisionRepository.markChapterRevised(chapter.copy(id = insertedId))

        val updatedItem = syllabusDao.getItemById(insertedId)
        assertNotNull(updatedItem)
        assertEquals(5, updatedItem?.revisionCount)
        assertEquals(ChapterStatus.MASTERED, updatedItem?.status)
    }

    @Test
    fun scheduleCustomRevision_updatesTargetTimestampAndStatus() = runBlocking {
        val syllabusDao = database.syllabusDao()

        val chapter = SyllabusItem(
            id = 3,
            subjectId = 10,
            title = "Geometry",
            itemType = ItemType.CHAPTER,
            status = ChapterStatus.COMPLETED
        )
        val insertedId = syllabusDao.insertItem(chapter)
        val targetTimestamp = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L

        revisionRepository.scheduleCustomRevision(chapter.copy(id = insertedId), targetTimestamp)

        val updatedItem = syllabusDao.getItemById(insertedId)
        assertNotNull(updatedItem)
        assertEquals(targetTimestamp, updatedItem?.nextRevisionTimestamp)
    }
}
