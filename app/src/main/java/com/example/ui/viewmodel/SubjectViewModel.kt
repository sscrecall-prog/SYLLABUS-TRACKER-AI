package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.SyllabusRepository
import com.example.util.AmbientSoundManager
import com.example.ui.theme.motion.TransitionDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SubjectViewModel(application: Application) : BaseViewModel(application) {    val subjectStatsList: StateFlow<List<SubjectStats>> = combine(
        subjects,
        items
    ) { subs, allItems ->
        subs.map { subject ->
            val subItems = allItems.filter { it.subjectId == subject.id }
            val sections = subItems.count { it.itemType == ItemType.SECTION }
            val chapters = subItems.filter { it.itemType == ItemType.CHAPTER || it.itemType == ItemType.SUBTOPIC }
            val total = chapters.size
            val completed = chapters.count { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
            val inProgress = chapters.count { it.status == ChapterStatus.IN_PROGRESS || it.status == ChapterStatus.LEARNING }
            val notStarted = chapters.count { it.status == ChapterStatus.NOT_STARTED }
            val weak = chapters.count { it.isWeak }
            val revDue = chapters.count { it.isRevisionDue }
            val percent = if (total > 0) ((completed.toFloat() / total) * 100).toInt() else 0
            val studyMins = chapters.sumOf { it.studyTimeMinutes }
            val avgConf = if (chapters.isNotEmpty()) chapters.map { it.confidence }.average().toFloat() else 3f
            val pyqAtt = chapters.sumOf { it.pyqAttempted }
            val pyqCor = chapters.sumOf { it.pyqCorrect }
            val pyqAcc = if (pyqAtt > 0) ((pyqCor.toFloat() / pyqAtt) * 100).toInt() else 0

            SubjectStats(
                subject = subject,
                totalSections = sections,
                totalChapters = total,
                completedChapters = completed,
                inProgressChapters = inProgress,
                notStartedChapters = notStarted,
                weakChapters = weak,
                revisionDueChapters = revDue,
                completionPercentage = percent,
                totalStudyMinutes = studyMins,
                averageConfidence = avgConf,
                pyqAttempted = pyqAtt,
                pyqCorrect = pyqCor,
                pyqAccuracy = pyqAcc
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Combined criteria flow
    val allSubjectHierarchies: StateFlow<List<SubjectHierarchy>> = syllabusRepository.allSubjectHierarchies.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState: StateFlow<SubjectUiState> = combine(
        subjects,
        subjectStatsList,
        allSubjectHierarchies
    ) { subs, stats, hierarchies ->
        SubjectUiState(
            subjects = subs,
            subjectStatsList = stats,
            allSubjectHierarchies = hierarchies
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, SubjectUiState())

    fun addSubject(name: String, code: String = "", iconName: String = "School", colorHex: String = "#2E7D32", description: String = "") {
        val maxOrder = subjects.value.maxOfOrNull { it.orderIndex } ?: 0
        val sub = Subject(
            name = name,
            code = code,
            iconName = iconName,
            colorHex = colorHex,
            orderIndex = maxOrder + 1,
            description = description
        )
        viewModelScope.launch { subjectRepository.insertSubject(sub) }
    }


    fun addSubject(subject: Subject) {
        viewModelScope.launch {
            val maxOrder = subjects.value.maxOfOrNull { it.orderIndex } ?: 0
            subjectRepository.insertSubject(subject.copy(orderIndex = maxOrder + 1))
            showSnackbar("Subject added")
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            subjectRepository.updateSubject(subject)
            showSnackbar("Subject updated")
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            subjectRepository.deleteSubject(subject)
            showSnackbar("Subject removed")
            }
    }
}
