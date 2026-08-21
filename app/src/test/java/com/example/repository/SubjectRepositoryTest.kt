package com.example.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AppDatabaseMigrations
import com.example.data.model.Subject
import com.example.data.repository.SubjectRepository
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
class SubjectRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var subjectRepository: SubjectRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addMigrations(*AppDatabaseMigrations.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        subjectRepository = SubjectRepository(database.subjectDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQuerySubjects_returnsInsertedList() = runBlocking {
        val sub1 = Subject(name = "Mathematics", code = "MATH", iconName = "math", colorHex = "#123")
        val sub2 = Subject(name = "English Language", code = "ENG", iconName = "book", colorHex = "#456")

        subjectRepository.insertSubject(sub1)
        subjectRepository.insertSubject(sub2)

        val subjects = subjectRepository.allSubjects.first()

        assertEquals(2, subjects.size)
        assertTrue(subjects.any { it.name == "Mathematics" })
        assertTrue(subjects.any { it.name == "English Language" })
    }

    @Test
    fun updateSubject_modifiesExistingRecord() = runBlocking {
        val sub = Subject(name = "General Science", code = "GS", iconName = "science", colorHex = "#789")
        val id = subjectRepository.insertSubject(sub)

        val inserted = database.subjectDao().getSubjectById(id)
        assertNotNull(inserted)

        val updatedSubject = inserted!!.copy(name = "General Science & Tech")
        subjectRepository.updateSubject(updatedSubject)

        val retrieved = database.subjectDao().getSubjectById(id)
        assertEquals("General Science & Tech", retrieved?.name)
    }

    @Test
    fun deleteSubject_removesSubjectFromDatabase() = runBlocking {
        val sub = Subject(name = "Polity", code = "POL", iconName = "law", colorHex = "#000")
        val id = subjectRepository.insertSubject(sub)

        val inserted = database.subjectDao().getSubjectById(id)
        assertNotNull(inserted)

        subjectRepository.deleteSubject(inserted!!)

        val retrieved = database.subjectDao().getSubjectById(id)
        assertNull(retrieved)
    }

    @Test
    fun reorderSubjects_updatesOrderIndices() = runBlocking {
        val sub1 = Subject(id = 1, name = "A", code = "A", iconName = "i", colorHex = "#0", orderIndex = 5)
        val sub2 = Subject(id = 2, name = "B", code = "B", iconName = "i", colorHex = "#0", orderIndex = 10)

        val id1 = subjectRepository.insertSubject(sub1)
        val id2 = subjectRepository.insertSubject(sub2)

        val listToReorder = listOf(
            sub2.copy(id = id2),
            sub1.copy(id = id1)
        )

        subjectRepository.reorderSubjects(listToReorder)

        val subjects = subjectRepository.allSubjects.first()
        assertEquals(2, subjects.size)
        val retrievedB = subjects.find { it.id == id2 }
        val retrievedA = subjects.find { it.id == id1 }

        assertEquals(0, retrievedB?.orderIndex)
        assertEquals(1, retrievedA?.orderIndex)
    }
}
