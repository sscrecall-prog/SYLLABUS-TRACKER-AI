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

class SettingsViewModel(application: Application) : BaseViewModel(application) {    fun updateUserProfile(name: String, targetExam: String, avatar: String) {
        viewModelScope.launch {
            val current = appSettings.value
            settingsRepository.updateSettings(
                current.copy(
                    userName = name,
                    targetExam = targetExam,
                    userAvatarEmoji = avatar
                )
            )
            showSnackbar("Profile updated!")
        }
    }

    fun updateExamTarget(examName: String, targetDateStr: String, examShift: String) {
        viewModelScope.launch {
            val current = appSettings.value
            settingsRepository.updateSettings(
                current.copy(
                    targetExam = examName,
                    targetExamDateStr = targetDateStr,
                    targetExamShift = examShift
                )
            )
            showSnackbar("Exam countdown & pace targets updated!")
        }
    }

    fun updateReducedMotion(enabled: Boolean) {
        viewModelScope.launch {
            val current = appSettings.value
            settingsRepository.updateSettings(current.copy(reducedMotion = enabled))
            showSnackbar(if (enabled) "Reduced Motion enabled" else "Full motion animations enabled")
        }
    }

    // Settings & Import/Export
    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            val current = appSettings.value
            settingsRepository.updateSettings(current.copy(themeMode = mode))
        }
    }

    fun updateRevisionIntervals(intervalsCsv: String) {
        viewModelScope.launch {
            val current = appSettings.value
            settingsRepository.updateSettings(current.copy(revisionIntervalsCsv = intervalsCsv))
            showSnackbar("Revision cycle updated")
        }
    }

    fun updateStudyTargets(dailyMins: Int, weeklyMins: Int) {
        viewModelScope.launch {
            val current = appSettings.value
            val validDaily = dailyMins.coerceAtLeast(15)
            val validWeekly = weeklyMins.coerceAtLeast(60)
            settingsRepository.updateSettings(current.copy(dailyTargetMinutes = validDaily, weeklyTargetMinutes = validWeekly))
            showSnackbar("Study targets updated")
        }
    }

    fun updateDailyTargetHours(hours: Float) {
        viewModelScope.launch {
            val current = appSettings.value
            val newDailyMinutes = (hours * 60).toInt().coerceAtLeast(15)
            settingsRepository.updateSettings(current.copy(dailyTargetMinutes = newDailyMinutes))
            showSnackbar("Daily study goal updated to ${if (hours % 1f == 0f) hours.toInt().toString() else String.format(java.util.Locale.getDefault(), "%.1f", hours)} hours!")
        }
    }

    fun updateWeeklyTargetHours(hours: Int) {
        viewModelScope.launch {
            val current = appSettings.value
            val newWeeklyMinutes = hours * 60
            settingsRepository.updateSettings(current.copy(weeklyTargetMinutes = newWeeklyMinutes))
            showSnackbar("Weekly study goal updated to ${hours} hours!")
        }
    }

    // Mock Test Operations
    fun resetData() {
        viewModelScope.launch {
            settingsRepository.resetToSampleData()
            showSnackbar("Reset to standard sample syllabus")
        }
    }

    suspend fun getExportJson(): String = settingsRepository.exportToJson()

    suspend fun getExportCsv(): String = settingsRepository.exportToCsv()

    suspend fun importData(jsonContent: String): Boolean = settingsRepository.importFromJson(jsonContent)

}
