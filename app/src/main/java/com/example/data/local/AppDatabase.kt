package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Subject::class,
        SyllabusItem::class,
        StudySession::class,
        StudyPlan::class,
        Goal::class,
        AppSettings::class,
        AchievementBadge::class,
        MockTest::class,
        MistakeEntry::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun syllabusDao(): SyllabusDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun goalDao(): GoalDao
    abstract fun settingsDao(): SettingsDao
    abstract fun achievementBadgeDao(): AchievementBadgeDao
    abstract fun mockTestDao(): MockTestDao
    abstract fun mistakeDao(): MistakeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "syllabus_tracker_database"
                )
                .addMigrations(*AppDatabaseMigrations.ALL_MIGRATIONS)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        // Ensure default badges exist on upgrade or fresh run
                        database.achievementBadgeDao().insertBadges(PreloadData.defaultBadges)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            database.subjectDao().insertSubjects(PreloadData.defaultSubjects)
            database.syllabusDao().insertItems(PreloadData.createDefaultSyllabusItems())
            for (goal in PreloadData.defaultGoals) {
                database.goalDao().insertGoal(goal)
            }
            for (plan in PreloadData.createSampleStudyPlans()) {
                database.studyPlanDao().insertPlan(plan)
            }
            database.settingsDao().insertOrUpdate(AppSettings())
            database.achievementBadgeDao().insertBadges(PreloadData.defaultBadges)
            
            // Add initial study session samples
            val now = System.currentTimeMillis()
            val oneDay = 24 * 60 * 60 * 1000L
            val sampleSessions = listOf(
                StudySession(
                    subjectId = 4,
                    subjectName = "Maths",
                    chapterTitle = "Percentage & Conversions",
                    durationSeconds = 3600,
                    timestamp = now - 2 * 3600 * 1000L,
                    mode = TimerMode.POMODORO,
                    notes = "Finished exercise 1 to 4"
                ),
                StudySession(
                    subjectId = 2,
                    subjectName = "English",
                    chapterTitle = "Idioms & Phrases",
                    durationSeconds = 2700,
                    timestamp = now - 5 * 3600 * 1000L,
                    mode = TimerMode.POMODORO,
                    notes = "Revised 50 flashcards"
                ),
                StudySession(
                    subjectId = 1,
                    subjectName = "GS — General Studies",
                    chapterTitle = "Indus Valley Civilization",
                    durationSeconds = 4800,
                    timestamp = now - oneDay,
                    mode = TimerMode.STOPWATCH,
                    notes = "Major sites and artifacts mapped"
                ),
                StudySession(
                    subjectId = 3,
                    subjectName = "Reasoning",
                    chapterTitle = "Syllogisms",
                    durationSeconds = 3000,
                    timestamp = now - 2 * oneDay,
                    mode = TimerMode.POMODORO,
                    notes = "Venn diagrams 3-statement cases"
                )
            )
            for (session in sampleSessions) {
                database.studySessionDao().insertSession(session)
            }
            database.mockTestDao().insertMockTests(PreloadData.createSampleMockTests())
            database.mistakeDao().insertMistakes(PreloadData.createSampleMistakes())
        }
    }
}
