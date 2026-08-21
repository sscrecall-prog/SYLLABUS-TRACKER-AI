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

class MainViewModel(application: Application) : BaseViewModel(application) {
    private val _currentNav = MutableStateFlow(NavDestination.DASHBOARD)
    val currentNav: StateFlow<NavDestination> = _currentNav.asStateFlow()

    private val navBackStack = mutableListOf<NavDestination>(NavDestination.DASHBOARD)
    val transitionDirection = MutableStateFlow(TransitionDirection.NEUTRAL)

    fun navigateTo(destination: NavDestination, isBack: Boolean = false) {
        if (_currentNav.value == destination) return
        
        if (isBack) {
            transitionDirection.value = TransitionDirection.BACKWARD
        } else {
            transitionDirection.value = TransitionDirection.FORWARD
            if (navBackStack.isEmpty() || navBackStack.last() != _currentNav.value) {
                navBackStack.add(_currentNav.value)
            }
        }
        _currentNav.value = destination
    }

    fun navigateBack(): Boolean {
        if (navBackStack.isNotEmpty()) {
            val previous = navBackStack.removeAt(navBackStack.size - 1)
            transitionDirection.value = TransitionDirection.BACKWARD
            _currentNav.value = previous
            return true
        } else if (_currentNav.value != NavDestination.DASHBOARD) {
            transitionDirection.value = TransitionDirection.BACKWARD
            _currentNav.value = NavDestination.DASHBOARD
            return true
        }
        return false
    }

    fun canNavigateBack(): Boolean = navBackStack.isNotEmpty() || _currentNav.value != NavDestination.DASHBOARD

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId: StateFlow<Long?> = _selectedSubjectId.asStateFlow()

    fun openSubjectDetail(subjectId: Long) {
        _selectedSubjectId.value = subjectId
        navigateTo(NavDestination.SYLLABUS)
    }

    // Subject CRUD

    // Computed Overall Stats
}
