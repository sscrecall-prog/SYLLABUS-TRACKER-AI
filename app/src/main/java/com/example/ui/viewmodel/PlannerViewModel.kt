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

class PlannerViewModel(application: Application) : BaseViewModel(application) {
    val todayDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayPlans: StateFlow<List<StudyPlan>> = plannerRepository.getPlansForDate(todayDateStr).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allPlans: StateFlow<List<StudyPlan>> = plannerRepository.allPlans.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState: StateFlow<PlannerUiState> = combine(
        todayPlans,
        allPlans
    ) { today, all ->
        PlannerUiState(
            todayDateStr = todayDateStr,
            todayPlans = today,
            allPlans = all
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, PlannerUiState())

    // Mock Test State & Filters
    fun addStudyPlan(
        dateStr: String,
        timeStr: String,
        subjectId: Long,
        subjectName: String,
        chapterTitle: String,
        plannedMinutes: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val plan = StudyPlan(
                dateStr = dateStr,
                timeStr = timeStr,
                subjectId = subjectId,
                subjectName = subjectName,
                chapterTitle = chapterTitle,
                plannedMinutes = plannedMinutes,
                goalNotes = notes
            )
            plannerRepository.insertPlan(plan)
            showSnackbar("Scheduled study session")
        }
    }

    fun togglePlanCompleted(plan: StudyPlan) {
        togglePlanCompletion(plan)
    }

    fun togglePlanCompletion(plan: StudyPlan) {
        viewModelScope.launch {
            val isNowCompleted = !plan.isCompleted
            val actual = if (isNowCompleted && plan.actualMinutes == 0) plan.plannedMinutes else plan.actualMinutes
            val updated = plan.copy(isCompleted = isNowCompleted, actualMinutes = actual)
            plannerRepository.updatePlan(updated)
            if (isNowCompleted) {
                studySessionRepository.logStudySession(
                    subjectId = plan.subjectId,
                    subjectName = plan.subjectName,
                    chapterId = null,
                    chapterTitle = plan.chapterTitle,
                    durationSeconds = (actual * 60).toLong(),
                    mode = TimerMode.POMODORO,
                    notes = plan.goalNotes
                )
                showSnackbar("Study session completed & logged!")
            }
        }
    }

    fun deletePlan(plan: StudyPlan) {
        deleteStudyPlan(plan)
    }

    fun deleteStudyPlan(plan: StudyPlan) {
        viewModelScope.launch {
            plannerRepository.deletePlan(plan)
            showSnackbar("Session removed")
        }
    }

    // Timer Implementation
}
