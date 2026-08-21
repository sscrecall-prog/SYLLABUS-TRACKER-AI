package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AppContainer private constructor(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    val database: AppDatabase = AppDatabase.getDatabase(context.applicationContext, scope)

    val subjectRepository: SubjectRepository by lazy { SubjectRepository(database.subjectDao()) }
    val syllabusRepository: SyllabusRepository by lazy { SyllabusRepository(database.syllabusDao(), database.settingsDao()) }
    val revisionRepository: RevisionRepository by lazy { RevisionRepository(database.syllabusDao(), database.settingsDao()) }
    val studySessionRepository: StudySessionRepository by lazy { StudySessionRepository(database.studySessionDao(), database.syllabusDao()) }
    val plannerRepository: PlannerRepository by lazy { PlannerRepository(database.studyPlanDao()) }
    val goalRepository: GoalRepository by lazy { GoalRepository(database.goalDao()) }
    val mockTestRepository: MockTestRepository by lazy { MockTestRepository(database.mockTestDao()) }
    val mistakeRepository: MistakeRepository by lazy { MistakeRepository(database.mistakeDao()) }
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(
            database.settingsDao(), database.achievementBadgeDao(), database.subjectDao(),
            database.syllabusDao(), database.studySessionDao(), database.studyPlanDao(),
            database.goalDao(), database.mockTestDao()
        )
    }
    val analyticsRepository: AnalyticsRepository by lazy { AnalyticsRepository() }

    companion object {
        @Volatile
        private var INSTANCE: AppContainer? = null

        fun getInstance(context: Context): AppContainer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppContainer(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
