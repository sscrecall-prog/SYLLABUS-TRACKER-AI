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

class SubjectRepository(
    private val subjectDao: SubjectDao
) {
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()

    suspend fun insertSubject(subject: Subject): Long = withContext(Dispatchers.IO) {
        subjectDao.insertSubject(subject)
    }


    suspend fun updateSubject(subject: Subject) = withContext(Dispatchers.IO) {
        subjectDao.updateSubject(subject)
    }


    suspend fun deleteSubject(subject: Subject) = withContext(Dispatchers.IO) {
        // syllabusDao.deleteItemsForSubject(subject.id)
        subjectDao.deleteSubject(subject)
    }


    suspend fun reorderSubjects(orderedList: List<Subject>) = withContext(Dispatchers.IO) {
        orderedList.forEachIndexed { index, subject ->
            subjectDao.updateSubject(subject.copy(orderIndex = index))
        }
    }

    // Syllabus Item CRUD

}
