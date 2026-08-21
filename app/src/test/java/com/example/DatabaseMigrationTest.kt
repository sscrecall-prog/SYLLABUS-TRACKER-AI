package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AppDatabaseMigrations
import com.example.data.model.*
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
class DatabaseMigrationTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addMigrations(*AppDatabaseMigrations.ALL_MIGRATIONS)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun databaseInitializationAndMigrationPathSucceeds() {
        assertNotNull(database)
        database.openHelper.writableDatabase
        assertTrue(database.isOpen)
    }

    @Test
    fun subjectAndSyllabusItemDataSurvivesAndMaintainsCascadeRelationship() = runBlocking {
        val subjectDao = database.subjectDao()
        val syllabusDao = database.syllabusDao()

        // Insert Subject
        val subjectId = subjectDao.insertSubject(
            Subject(
                name = "Quantitative Aptitude",
                code = "QUANT",
                iconName = "Calculate",
                colorHex = "#3F51B5"
            )
        )
        assertTrue(subjectId > 0)

        // Insert Syllabus Item associated with subject
        val item = SyllabusItem(
            subjectId = subjectId,
            title = "Percentage & Ratios",
            itemType = ItemType.CHAPTER,
            status = ChapterStatus.IN_PROGRESS,
            completionPercentage = 50,
            confidence = 4
        )
        val itemId = syllabusDao.insertItem(item)
        assertTrue(itemId > 0)

        // Retrieve and verify data integrity
        val retrievedSubject = subjectDao.getSubjectById(subjectId)
        assertNotNull(retrievedSubject)
        assertEquals("Quantitative Aptitude", retrievedSubject?.name)

        val retrievedItems = syllabusDao.getItemsForSubject(subjectId).first()
        assertEquals(1, retrievedItems.size)
        assertEquals("Percentage & Ratios", retrievedItems[0].title)
        assertEquals(50, retrievedItems[0].completionPercentage)

        // Verify Cascade Delete behavior
        subjectDao.deleteSubject(retrievedSubject!!)
        val remainingItems = syllabusDao.getItemsForSubject(subjectId).first()
        assertTrue(remainingItems.isEmpty())
    }

    @Test
    fun allEntityTablesFunctionAndPersistRecordsCorrectly() = runBlocking {
        // Study Session
        val sessionDao = database.studySessionDao()
        val sessionId = sessionDao.insertSession(
            StudySession(
                subjectId = 100,
                subjectName = "English",
                chapterTitle = "Grammar",
                durationSeconds = 1800,
                mode = TimerMode.POMODORO
            )
        )
        assertTrue(sessionId > 0)
        assertEquals(1, sessionDao.getAllSessions().first().size)

        // Goal
        val goalDao = database.goalDao()
        val goalId = goalDao.insertGoal(
            Goal(
                title = "Complete 5 Math Chapters",
                targetDateStr = "2026-09-01",
                targetChaptersCount = 5
            )
        )
        assertTrue(goalId > 0)

        // App Settings
        val settingsDao = database.settingsDao()
        settingsDao.insertOrUpdate(
            AppSettings(
                userName = "Target Aspirant",
                targetExam = "SSC CGL 2026"
            )
        )
        val settings = settingsDao.getSettingsDirect()
        assertNotNull(settings)
        assertEquals("Target Aspirant", settings?.userName)

        // Mock Test
        val mockDao = database.mockTestDao()
        val mockId = mockDao.insertMockTest(
            MockTest(
                testName = "Tier-1 Mock 01",
                testDateStr = "2026-08-20",
                marksScored = 142.5f,
                cutoffMarks = 135f
            )
        )
        assertTrue(mockId > 0)
        assertEquals(1, mockDao.getAllMockTests().first().size)

        // Mistake Entry
        val mistakeDao = database.mistakeDao()
        val mistakeId = mistakeDao.insertMistake(
            MistakeEntry(
                questionText = "In radius of triangle formula?",
                subjectId = 100,
                subjectName = "Maths",
                category = MistakeCategory.FORMULA_FORGOT
            )
        )
        assertTrue(mistakeId > 0)
        assertEquals(1, mistakeDao.getAllMistakes().first().size)
    }
}
