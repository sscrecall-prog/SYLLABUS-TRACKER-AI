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
import com.example.data.repository.RevisionRepository
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
class RevisionRepositoryTest {

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
            database.subjectDao().insertSubject(Subject(id = 1, name = "Math", code = "M", iconName = "m", colorHex = "#000"))
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun markChapterRevised_incrementsRevisionCountAndUpdatesTimestamps() = runBlocking {
        val chapter = SyllabusItem(
            subjectId = 1,
            title = "Number System",
            itemType = ItemType.CHAPTER,
            revisionCount = 0
        )
        val id = database.syllabusDao().insertItem(chapter)
        val item = database.syllabusDao().getItemById(id)
        assertNotNull(item)

        revisionRepository.markChapterRevised(item!!)

        val revised = database.syllabusDao().getItemById(id)
        assertNotNull(revised)
        assertEquals(1, revised?.revisionCount)
        assertEquals(ChapterStatus.COMPLETED, revised?.status)
        assertNotNull(revised?.nextRevisionTimestamp)
        assertNotNull(revised?.lastStudiedTimestamp)
    }

    @Test
    fun scheduleCustomRevision_updatesNextRevisionTimestamp() = runBlocking {
        val chapter = SyllabusItem(
            subjectId = 1,
            title = "LCM & HCF",
            itemType = ItemType.CHAPTER
        )
        val id = database.syllabusDao().insertItem(chapter)
        val item = database.syllabusDao().getItemById(id)
        assertNotNull(item)

        val targetTime = System.currentTimeMillis() + 86400000L * 5 // 5 days from now
        revisionRepository.scheduleCustomRevision(item!!, targetTime)

        val updated = database.syllabusDao().getItemById(id)
        assertEquals(targetTime, updated?.nextRevisionTimestamp)
    }
}
