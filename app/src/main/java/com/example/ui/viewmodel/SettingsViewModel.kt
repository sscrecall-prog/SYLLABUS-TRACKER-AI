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
            settingsRepository.updateSettings(current.copy(dailyTargetMinutes = dailyMins, weeklyTargetMinutes = weeklyMins))
            showSnackbar("Study targets updated")
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
