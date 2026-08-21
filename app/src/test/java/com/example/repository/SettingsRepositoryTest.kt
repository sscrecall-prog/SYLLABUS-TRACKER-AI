package com.example.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.repository.SettingsRepository
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
class SettingsRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: SettingsRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = SettingsRepository(
            settingsDao = database.settingsDao(),
            achievementBadgeDao = database.achievementBadgeDao(),
            subjectDao = database.subjectDao(),
            syllabusDao = database.syllabusDao(),
            studySessionDao = database.studySessionDao(),
            studyPlanDao = database.studyPlanDao(),
            goalDao = database.goalDao(),
            mockTestDao = database.mockTestDao(),
            mistakeDao = database.mistakeDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun resetToSampleDataPopulatesAllEntities() = runBlocking {
        repository.resetToSampleData()

        val subjects = database.subjectDao().getAllSubjects().first()
        val items = database.syllabusDao().getAllItems().first()
        val goals = database.goalDao().getAllGoals().first()
        val mocks = database.mockTestDao().getAllMockTests().first()
        val mistakes = database.mistakeDao().getAllMistakes().first()
        val badges = database.achievementBadgeDao().getAllBadges().first()

        assertTrue(subjects.isNotEmpty())
        assertTrue(items.isNotEmpty())
        assertTrue(goals.isNotEmpty())
        assertTrue(mocks.isNotEmpty())
        assertTrue(mistakes.isNotEmpty())
        assertTrue(badges.isNotEmpty())
    }

    @Test
    fun fullBackupAndRestoreMaintainsDataIntegrity() = runBlocking {
        repository.resetToSampleData()

        val jsonBackup = repository.exportToJson()
        assertNotNull(jsonBackup)
        assertTrue(jsonBackup.contains("\"subjects\""))
        assertTrue(jsonBackup.contains("\"items\""))
        assertTrue(jsonBackup.contains("\"mistakes\""))
        assertTrue(jsonBackup.contains("\"mockTests\""))

        // Clear all data
        database.subjectDao().deleteAllSubjects()
        database.syllabusDao().deleteAllItems()
        database.mistakeDao().deleteAllMistakes()
        database.mockTestDao().deleteAllMockTests()

        assertEquals(0, database.subjectDao().getAllSubjects().first().size)
        assertEquals(0, database.syllabusDao().getAllItems().first().size)

        // Restore from JSON
        val restoreSuccess = repository.importFromJson(jsonBackup)
        assertTrue(restoreSuccess)

        val restoredSubjects = database.subjectDao().getAllSubjects().first()
        val restoredItems = database.syllabusDao().getAllItems().first()
        val restoredMistakes = database.mistakeDao().getAllMistakes().first()
        val restoredMocks = database.mockTestDao().getAllMockTests().first()

        assertTrue(restoredSubjects.isNotEmpty())
        assertTrue(restoredItems.isNotEmpty())
        assertTrue(restoredMistakes.isNotEmpty())
        assertTrue(restoredMocks.isNotEmpty())
    }

    @Test
    fun csvExportProducesValidHeaderAndRows() = runBlocking {
        repository.resetToSampleData()
        val csv = repository.exportToCsv()
        assertNotNull(csv)
        assertTrue(csv.startsWith("Subject,Type,Title,Status,Completion %,Confidence"))
        assertTrue(csv.lines().size > 10)
    }

    @Test
    fun malformedJsonImportFailsGracefullyWithoutCrashing() = runBlocking {
        val malformedJson = "{ invalid json: true, broken... "
        val success = repository.importFromJson(malformedJson)
        assertFalse(success)
    }
}
