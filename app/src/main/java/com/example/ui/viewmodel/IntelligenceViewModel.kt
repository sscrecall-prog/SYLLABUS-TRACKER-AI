package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.intelligence.*
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IntelligenceViewModel(application: Application) : BaseViewModel(application) {

    private val _dailyBudgetMinutes = MutableStateFlow(IntelligenceConfig.defaultDailyBudgetMinutes)
    val dailyBudgetMinutes: StateFlow<Int> = _dailyBudgetMinutes.asStateFlow()

    private val _selectedTopicForDetail = MutableStateFlow<SyllabusItem?>(null)
    val selectedTopicForDetail: StateFlow<SyllabusItem?> = _selectedTopicForDetail.asStateFlow()

    fun setDailyBudgetMinutes(minutes: Int) {
        _dailyBudgetMinutes.value = minutes.coerceIn(15, 600)
    }

    fun selectTopicForDetail(topic: SyllabusItem?) {
        _selectedTopicForDetail.value = topic
    }

    @Suppress("UNCHECKED_CAST")
    val snapshot: StateFlow<IntelligenceSnapshot> = combine(
        items,
        subjects,
        mistakes,
        mockTests,
        studySessions,
        appSettings,
        _dailyBudgetMinutes,
        goals
    ) { args: Array<Any?> ->
        val allItems = args[0] as List<SyllabusItem>
        val allSubs = args[1] as List<Subject>
        val allMistakes = args[2] as List<MistakeEntry>
        val allMocks = args[3] as List<MockTest>
        val allSessions = args[4] as List<StudySession>
        val settings = args[5] as AppSettings
        val budget = args[6] as Int
        val allGoals = args[7] as List<Goal>

        val safeBudget = if (budget > 0) budget else IntelligenceConfig.defaultDailyBudgetMinutes
        AdaptivePlanningEngine.createIntelligenceSnapshot(
            topics = allItems,
            subjects = allSubs,
            mistakes = allMistakes,
            mockTests = allMocks,
            sessions = allSessions,
            settings = settings,
            availableBudgetMinutes = safeBudget,
            goals = allGoals
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        AdaptivePlanningEngine.createIntelligenceSnapshot(
            topics = emptyList(),
            subjects = emptyList(),
            mistakes = emptyList(),
            mockTests = emptyList(),
            sessions = emptyList(),
            settings = AppSettings()
        )
    )

    fun markPlanActionCompleted(actionItem: PlanActionItem) {
        viewModelScope.launch {
            if (actionItem.topicId != null) {
                val topic = items.value.find { it.id == actionItem.topicId }
                if (topic != null) {
                    when (actionItem.actionType) {
                        PlanActionType.REVISION, PlanActionType.MAINTENANCE -> {
                            val updated = topic.copy(
                                revisionCount = topic.revisionCount + 1,
                                status = if (topic.completionPercentage >= 100) ChapterStatus.COMPLETED else ChapterStatus.IN_PROGRESS,
                                lastStudiedTimestamp = System.currentTimeMillis()
                            )
                            syllabusRepository.updateItem(updated)
                            studySessionRepository.logStudySession(
                                subjectId = topic.subjectId,
                                subjectName = actionItem.subjectName,
                                chapterId = topic.id,
                                chapterTitle = topic.title,
                                durationSeconds = (actionItem.estimatedMinutes * 60).toLong(),
                                mode = TimerMode.POMODORO,
                                notes = "Adaptive Plan: ${actionItem.actionType.label} completed"
                            )
                            showSnackbar("Revision recorded for ${topic.title}")
                        }
                        PlanActionType.WEAK_TOPIC, PlanActionType.CONCEPT_REVIEW -> {
                            val updated = topic.copy(
                                status = ChapterStatus.IN_PROGRESS,
                                lastStudiedTimestamp = System.currentTimeMillis()
                            )
                            syllabusRepository.updateItem(updated)
                            studySessionRepository.logStudySession(
                                subjectId = topic.subjectId,
                                subjectName = actionItem.subjectName,
                                chapterId = topic.id,
                                chapterTitle = topic.title,
                                durationSeconds = (actionItem.estimatedMinutes * 60).toLong(),
                                mode = TimerMode.POMODORO,
                                notes = "Adaptive Plan: Focused study completed"
                            )
                            showSnackbar("Focus session completed for ${topic.title}")
                        }
                        else -> {
                            studySessionRepository.logStudySession(
                                subjectId = topic.subjectId,
                                subjectName = actionItem.subjectName,
                                chapterId = topic.id,
                                chapterTitle = topic.title,
                                durationSeconds = (actionItem.estimatedMinutes * 60).toLong(),
                                mode = TimerMode.POMODORO,
                                notes = "Adaptive Plan: ${actionItem.actionType.label} completed"
                            )
                            showSnackbar("Completed ${actionItem.actionType.label} for ${topic.title}")
                        }
                    }
                }
            } else {
                showSnackbar("Completed action: ${actionItem.actionType.label}")
            }
        }
    }
}
