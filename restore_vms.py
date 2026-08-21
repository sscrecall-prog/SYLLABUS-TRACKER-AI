import re

with open('/tmp/SyllabusViewModel_backup.kt', 'r') as f:
    backup_content = f.read()

# MockTestsViewModel
mock_vm = """package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MockTestsViewModel(application: Application) : BaseViewModel(application) {

    private val _selectedMockTestId = MutableStateFlow<Long?>(null)
    val selectedMockTestId = _selectedMockTestId.asStateFlow()

    fun addMockTest(mockTest: MockTest) {
        viewModelScope.launch {
            mockTestRepository.insertMockTest(mockTest)
            showSnackbar("Mock Test added")
        }
    }

    fun updateMockTest(mockTest: MockTest) {
        viewModelScope.launch {
            mockTestRepository.updateMockTest(mockTest)
            showSnackbar("Mock Test updated")
        }
    }

    fun deleteMockTest(mockTest: MockTest) {
        viewModelScope.launch {
            mockTestRepository.deleteMockTest(mockTest)
            showSnackbar("Mock Test deleted")
        }
    }

    fun openMockTestDetail(id: Long) {
        _selectedMockTestId.value = id
    }

    fun closeMockTestDetail() {
        _selectedMockTestId.value = null
    }
}
"""

with open('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', 'w') as f:
    f.write(mock_vm)

# MistakeNotebookViewModel
mistake_vm = """package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MistakeNotebookViewModel(application: Application) : BaseViewModel(application) {
    fun addMistake(mistake: MistakeEntry) {
        viewModelScope.launch {
            mistakeRepository.insertMistake(mistake)
            showSnackbar("Mistake logged")
        }
    }

    fun updateMistake(mistake: MistakeEntry) {
        viewModelScope.launch {
            mistakeRepository.updateMistake(mistake)
        }
    }

    fun markMistakeReviewed(mistake: MistakeEntry, status: MistakeResolutionStatus) {
        viewModelScope.launch {
            mistakeRepository.markMistakeReviewed(mistake, status)
            showSnackbar("Marked as ${status.name}")
        }
    }

    fun toggleMistakeStar(mistake: MistakeEntry) {
        viewModelScope.launch {
            mistakeRepository.toggleMistakeStar(mistake)
        }
    }

    fun deleteMistake(mistake: MistakeEntry) {
        viewModelScope.launch {
            mistakeRepository.deleteMistake(mistake)
            showSnackbar("Mistake deleted")
        }
    }
}
"""
with open('app/src/main/java/com/example/ui/viewmodel/MistakeNotebookViewModel.kt', 'w') as f:
    f.write(mistake_vm)

# GoalsViewModel
goals_vm = """package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GoalsViewModel(application: Application) : BaseViewModel(application) {
    fun addGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.insertGoal(goal)
            showSnackbar("Goal added")
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goal)
            showSnackbar("Goal deleted")
        }
    }
}
"""
with open('app/src/main/java/com/example/ui/viewmodel/GoalsViewModel.kt', 'w') as f:
    f.write(goals_vm)

