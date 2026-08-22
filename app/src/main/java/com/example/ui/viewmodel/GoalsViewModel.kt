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

    fun updateDailyTargetHours(hours: Float) {
        viewModelScope.launch {
            val current = appSettings.value
            val newDailyMinutes = (hours * 60).toInt().coerceAtLeast(15)
            settingsRepository.updateSettings(current.copy(dailyTargetMinutes = newDailyMinutes))
            showSnackbar("Daily study goal set to ${if (hours % 1f == 0f) hours.toInt().toString() else String.format(java.util.Locale.getDefault(), "%.1f", hours)} hours!")
        }
    }

    fun updateDailyTargetMinutes(minutes: Int) {
        viewModelScope.launch {
            val current = appSettings.value
            val validMins = minutes.coerceAtLeast(15)
            settingsRepository.updateSettings(current.copy(dailyTargetMinutes = validMins))
            showSnackbar("Daily study goal updated!")
        }
    }

    fun updateWeeklyTargetHours(hours: Int) {
        viewModelScope.launch {
            val current = appSettings.value
            val newWeeklyMinutes = hours * 60
            settingsRepository.updateSettings(current.copy(weeklyTargetMinutes = newWeeklyMinutes))
            showSnackbar("Weekly study goal set to ${hours} hours!")
        }
    }
}
