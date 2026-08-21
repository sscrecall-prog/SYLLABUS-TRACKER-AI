package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.di.AppContainer
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    protected val container = AppContainer.getInstance(application)

    protected val database get() = container.database
    protected val subjectRepository get() = container.subjectRepository
    protected val syllabusRepository get() = container.syllabusRepository
    protected val revisionRepository get() = container.revisionRepository
    protected val studySessionRepository get() = container.studySessionRepository
    protected val plannerRepository get() = container.plannerRepository
    protected val goalRepository get() = container.goalRepository
    protected val mockTestRepository get() = container.mockTestRepository
    protected val mistakeRepository get() = container.mistakeRepository
    protected val settingsRepository get() = container.settingsRepository
    protected val analyticsRepository get() = container.analyticsRepository

    val subjects: StateFlow<List<Subject>> = subjectRepository.allSubjects.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val items: StateFlow<List<SyllabusItem>> = syllabusRepository.allItems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val studySessions: StateFlow<List<StudySession>> = studySessionRepository.allStudySessions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val goals: StateFlow<List<Goal>> = goalRepository.allGoals.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val appSettings: StateFlow<AppSettings> = settingsRepository.appSettings.stateIn(viewModelScope, SharingStarted.Lazily, AppSettings())
    val allBadges: StateFlow<List<AchievementBadge>> = settingsRepository.allBadges.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val unlockedBadges: StateFlow<List<AchievementBadge>> = settingsRepository.unlockedBadges.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val mockTests: StateFlow<List<MockTest>> = mockTestRepository.allMockTests.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val mistakes: StateFlow<List<MistakeEntry>> = mistakeRepository.allMistakes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

