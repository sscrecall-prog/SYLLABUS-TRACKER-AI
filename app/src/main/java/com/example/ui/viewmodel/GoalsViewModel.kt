package com.example.ui.viewmodel

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
