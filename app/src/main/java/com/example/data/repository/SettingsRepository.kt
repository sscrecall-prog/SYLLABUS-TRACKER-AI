package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class SettingsRepository(
    private val settingsDao: SettingsDao,
    private val achievementBadgeDao: AchievementBadgeDao,
    private val subjectDao: SubjectDao,
    private val syllabusDao: SyllabusDao,
    private val studySessionDao: StudySessionDao,
    private val studyPlanDao: StudyPlanDao,
    private val goalDao: GoalDao,
    private val mockTestDao: MockTestDao,
    private val mistakeDao: MistakeDao
) {
    val appSettings: Flow<AppSettings> = settingsDao.getSettings().map { it ?: AppSettings() }

    val allBadges: Flow<List<AchievementBadge>> = achievementBadgeDao.getAllBadges()

    val unlockedBadges: Flow<List<AchievementBadge>> = achievementBadgeDao.getUnlockedBadges()

    suspend fun updateBadge(badge: AchievementBadge) = withContext(Dispatchers.IO) {
        achievementBadgeDao.updateBadge(badge)
    }

    suspend fun insertBadges(badges: List<AchievementBadge>) = withContext(Dispatchers.IO) {
        achievementBadgeDao.insertBadges(badges)
    }

    suspend fun updateSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdate(settings)
    }

    // Reset Data
    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        syllabusDao.deleteAllItems()
        subjectDao.deleteAllSubjects()
        studySessionDao.deleteAllSessions()
        studyPlanDao.deleteAllPlans()
        goalDao.deleteAllGoals()
        mockTestDao.deleteAllMockTests()
        mistakeDao.deleteAllMistakes()
        achievementBadgeDao.deleteAllBadges()

        subjectDao.insertSubjects(PreloadData.defaultSubjects)
        syllabusDao.insertItems(PreloadData.createDefaultSyllabusItems())
        for (g in PreloadData.defaultGoals) goalDao.insertGoal(g)
        for (p in PreloadData.createSampleStudyPlans()) studyPlanDao.insertPlan(p)
        mockTestDao.insertMockTests(PreloadData.createSampleMockTests())
        mistakeDao.insertMistakes(PreloadData.createSampleMistakes())
        achievementBadgeDao.insertBadges(PreloadData.defaultBadges)
        settingsDao.insertOrUpdate(AppSettings())
    }

    // Export to JSON
    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val subjectsList = subjectDao.getAllSubjects().first()
        val itemsList = syllabusDao.getAllItems().first()
        val goalsList = goalDao.getAllGoals().first()
        val mockTestsList = mockTestDao.getAllMockTests().first()
        val mistakesList = mistakeDao.getAllMistakes().first()
        val sessionsList = studySessionDao.getAllSessions().first()
        val plansList = studyPlanDao.getAllPlans().first()
        val badgesList = achievementBadgeDao.getAllBadges().first()
        val currentSettings = settingsDao.getSettings().first() ?: AppSettings()

        // Export AppSettings
        val settingsObj = JSONObject().apply {
            put("id", currentSettings.id)
            put("themeMode", currentSettings.themeMode.name)
            put("targetExam", currentSettings.targetExam)
            put("targetExamShift", currentSettings.targetExamShift)
            put("targetExamDateStr", currentSettings.targetExamDateStr)
            put("dailyTargetMinutes", currentSettings.dailyTargetMinutes)
            put("weeklyTargetMinutes", currentSettings.weeklyTargetMinutes)
            put("revisionIntervalsCsv", currentSettings.revisionIntervalsCsv)
            put("pomodoroWorkMinutes", currentSettings.pomodoroWorkMinutes)
            put("pomodoroShortBreakMinutes", currentSettings.pomodoroShortBreakMinutes)
            put("pomodoroLongBreakMinutes", currentSettings.pomodoroLongBreakMinutes)
            put("userName", currentSettings.userName)
            put("userAvatarEmoji", currentSettings.userAvatarEmoji)
            put("reducedMotion", currentSettings.reducedMotion)
        }
        root.put("settings", settingsObj)

        // Export Subjects
        val subArray = JSONArray()
        for (s in subjectsList) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("code", s.code)
                put("colorHex", s.colorHex)
                put("iconName", s.iconName)
                put("orderIndex", s.orderIndex)
                put("description", s.description)
            }
            subArray.put(obj)
        }
        root.put("subjects", subArray)

        // Export Syllabus Items
        val itemArray = JSONArray()
        for (it in itemsList) {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("subjectId", it.subjectId)
                put("parentId", it.parentId ?: JSONObject.NULL)
                put("itemType", it.itemType.name)
                put("title", it.title)
                put("orderIndex", it.orderIndex)
                put("status", it.status.name)
                put("completionPercentage", it.completionPercentage)
                put("confidence", it.confidence)
                put("priority", it.priority.name)
                put("difficulty", it.difficulty.name)
                put("lastStudiedTimestamp", it.lastStudiedTimestamp ?: JSONObject.NULL)
                put("nextRevisionTimestamp", it.nextRevisionTimestamp ?: JSONObject.NULL)
                put("notes", it.notes)
                put("isImportant", it.isImportant)
                put("isBookmarked", it.isBookmarked)
                put("studyTimeMinutes", it.studyTimeMinutes)
                put("revisionCount", it.revisionCount)
                put("tags", it.tags)
                put("pyqTotal", it.pyqTotal)
                put("pyqAttempted", it.pyqAttempted)
                put("pyqCorrect", it.pyqCorrect)
            }
            itemArray.put(obj)
        }
        root.put("items", itemArray)

        // Export Mistakes
        val mistakeArray = JSONArray()
        for (m in mistakesList) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("questionText", m.questionText)
                put("yourWrongAnswer", m.yourWrongAnswer)
                put("correctAnswer", m.correctAnswer)
                put("explanationOrKeyConcept", m.explanationOrKeyConcept)
                put("subjectId", m.subjectId)
                put("subjectName", m.subjectName)
                put("chapterTitle", m.chapterTitle)
                put("sourceMockOrBook", m.sourceMockOrBook)
                put("category", m.category.name)
                put("resolutionStatus", m.resolutionStatus.name)
                put("importanceStar", m.importanceStar)
                put("tagsCsv", m.tagsCsv)
                put("createdTimestamp", m.createdTimestamp)
                put("lastReviewedTimestamp", m.lastReviewedTimestamp)
                put("nextReviewTimestamp", m.nextReviewTimestamp)
                put("reviewCount", m.reviewCount)
            }
            mistakeArray.put(obj)
        }
        root.put("mistakes", mistakeArray)

        // Export Mock Tests
        val mockArray = JSONArray()
        for (mk in mockTestsList) {
            val obj = JSONObject().apply {
                put("id", mk.id)
                put("testName", mk.testName)
                put("testType", mk.testType.name)
                put("testPlatform", mk.testPlatform)
                put("testDateStr", mk.testDateStr)
                put("timestamp", mk.timestamp)
                put("totalMarks", mk.totalMarks.toDouble())
                put("marksScored", mk.marksScored.toDouble())
                put("totalQuestions", mk.totalQuestions)
                put("attemptedQuestions", mk.attemptedQuestions)
                put("correctQuestions", mk.correctQuestions)
                put("incorrectQuestions", mk.incorrectQuestions)
                put("accuracy", mk.accuracy.toDouble())
                put("percentile", mk.percentile.toDouble())
                put("rank", mk.rank)
                put("totalStudents", mk.totalStudents)
                put("cutoffMarks", mk.cutoffMarks.toDouble())
                put("timeTakenMinutes", mk.timeTakenMinutes)
                put("mathScore", mk.mathScore.toDouble())
                put("mathTotal", mk.mathTotal.toDouble())
                put("englishScore", mk.englishScore.toDouble())
                put("englishTotal", mk.englishTotal.toDouble())
                put("reasoningScore", mk.reasoningScore.toDouble())
                put("reasoningTotal", mk.reasoningTotal.toDouble())
                put("gsScore", mk.gsScore.toDouble())
                put("gsTotal", mk.gsTotal.toDouble())
                put("weakAreasIdentified", mk.weakAreasIdentified)
                put("analysisNotes", mk.analysisNotes)
                put("isClearedCutoff", mk.isClearedCutoff)
            }
            mockArray.put(obj)
        }
        root.put("mockTests", mockArray)

        // Export Study Sessions
        val sessionArray = JSONArray()
        for (ss in sessionsList) {
            val obj = JSONObject().apply {
                put("id", ss.id)
                put("subjectId", ss.subjectId)
                put("subjectName", ss.subjectName)
                put("chapterId", ss.chapterId ?: JSONObject.NULL)
                put("chapterTitle", ss.chapterTitle)
                put("durationSeconds", ss.durationSeconds)
                put("mode", ss.mode.name)
                put("notes", ss.notes)
                put("timestamp", ss.timestamp)
            }
            sessionArray.put(obj)
        }
        root.put("studySessions", sessionArray)

        // Export Study Plans
        val planArray = JSONArray()
        for (p in plansList) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("dateStr", p.dateStr)
                put("timeStr", p.timeStr)
                put("subjectId", p.subjectId)
                put("subjectName", p.subjectName)
                put("chapterTitle", p.chapterTitle)
                put("plannedMinutes", p.plannedMinutes)
                put("actualMinutes", p.actualMinutes)
                put("isCompleted", p.isCompleted)
                put("goalNotes", p.goalNotes)
            }
            planArray.put(obj)
        }
        root.put("studyPlans", planArray)

        // Export Goals
        val goalArray = JSONArray()
        for (g in goalsList) {
            val obj = JSONObject().apply {
                put("id", g.id)
                put("title", g.title)
                put("targetDateStr", g.targetDateStr)
                put("subjectId", g.subjectId ?: JSONObject.NULL)
                put("subjectName", g.subjectName)
                put("targetChaptersCount", g.targetChaptersCount)
                put("completedChaptersCount", g.completedChaptersCount)
                put("targetStudyHours", g.targetStudyHours.toDouble())
                put("isCompleted", g.isCompleted)
            }
            goalArray.put(obj)
        }
        root.put("goals", goalArray)

        // Export Badges
        val badgeArray = JSONArray()
        for (b in badgesList) {
            val obj = JSONObject().apply {
                put("id", b.id)
                put("title", b.title)
                put("description", b.description)
                put("category", b.category.name)
                put("iconEmoji", b.iconEmoji)
                put("tier", b.tier.name)
                put("isUnlocked", b.isUnlocked)
                put("unlockedAt", b.unlockedAt ?: JSONObject.NULL)
                put("currentProgress", b.currentProgress)
                put("maxProgress", b.maxProgress)
                put("rewardXp", b.rewardXp)
                put("hintRequirement", b.hintRequirement)
            }
            badgeArray.put(obj)
        }
        root.put("badges", badgeArray)

        root.put("exportDate", System.currentTimeMillis())
        root.put("backupVersion", 1)
        root.put("version", "1.0")

        root.toString(2)
    }

    // Export to CSV
    suspend fun exportToCsv(): String = withContext(Dispatchers.IO) {
        val subjects = subjectDao.getAllSubjects().first().associateBy { it.id }
        val items = syllabusDao.getAllItems().first()
        val sb = StringBuilder()
        sb.append("Subject,Type,Title,Status,Completion %,Confidence,Priority,Difficulty,Study Time (mins),Revisions,Tags,PYQ Attempted,PYQ Correct,Notes\n")
        for (it in items) {
            val subName = subjects[it.subjectId]?.name ?: "Unknown"
            val cleanNotes = it.notes.replace("\"", "\"\"").replace("\n", " ")
            val cleanTitle = it.title.replace("\"", "\"\"")
            val cleanTags = it.tags.replace("\"", "\"\"")
            sb.append("\"$subName\",\"${it.itemType.label}\",\"$cleanTitle\",\"${it.status.label}\",${it.completionPercentage},${it.confidence},\"${it.priority.label}\",\"${it.difficulty.label}\",${it.studyTimeMinutes},${it.revisionCount},\"$cleanTags\",${it.pyqAttempted},${it.pyqCorrect},\"$cleanNotes\"\n")
        }
        sb.toString()
    }

    // Import from JSON with robust validation and sanitization
    suspend fun importFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            // 1. Settings
            if (root.has("settings")) {
                val sObj = root.getJSONObject("settings")
                val s = AppSettings(
                    id = sObj.optInt("id", 1),
                    themeMode = runCatching { AppThemeMode.valueOf(sObj.optString("themeMode", AppThemeMode.SYSTEM.name)) }.getOrDefault(AppThemeMode.SYSTEM),
                    targetExam = sObj.optString("targetExam", "SSC CGL 2026"),
                    targetExamShift = sObj.optString("targetExamShift", "Tier-1 / Prelims"),
                    targetExamDateStr = sObj.optString("targetExamDateStr", "2026-10-01"),
                    dailyTargetMinutes = sObj.optInt("dailyTargetMinutes", 240).coerceIn(15, 1440),
                    weeklyTargetMinutes = sObj.optInt("weeklyTargetMinutes", 1440).coerceIn(60, 10080),
                    revisionIntervalsCsv = sObj.optString("revisionIntervalsCsv", "1,3,7,21,45,90"),
                    pomodoroWorkMinutes = sObj.optInt("pomodoroWorkMinutes", 25).coerceIn(5, 120),
                    pomodoroShortBreakMinutes = sObj.optInt("pomodoroShortBreakMinutes", 5).coerceIn(1, 60),
                    pomodoroLongBreakMinutes = sObj.optInt("pomodoroLongBreakMinutes", 15).coerceIn(5, 60),
                    userName = sObj.optString("userName", "Aspirant"),
                    userAvatarEmoji = sObj.optString("userAvatarEmoji", "🎓"),
                    reducedMotion = sObj.optBoolean("reducedMotion", false)
                )
                settingsDao.insertOrUpdate(s)
            }

            // 2. Subjects
            if (root.has("subjects")) {
                val subArray = root.getJSONArray("subjects")
                val subs = mutableListOf<Subject>()
                for (i in 0 until subArray.length()) {
                    val obj = subArray.getJSONObject(i)
                    subs.add(
                        Subject(
                            id = obj.optLong("id", (i + 1).toLong()),
                            name = obj.optString("name", "Subject ${i + 1}"),
                            code = obj.optString("code", "SUB"),
                            colorHex = obj.optString("colorHex", "#2D4F1E"),
                            iconName = obj.optString("iconName", "School"),
                            orderIndex = obj.optInt("orderIndex", i),
                            description = obj.optString("description", "")
                        )
                    )
                }
                if (subs.isNotEmpty()) {
                    subjectDao.insertSubjects(subs)
                }
            }

            // 3. Syllabus Items
            if (root.has("items")) {
                val itemArray = root.getJSONArray("items")
                val items = mutableListOf<SyllabusItem>()
                for (i in 0 until itemArray.length()) {
                    val obj = itemArray.getJSONObject(i)
                    val it = SyllabusItem(
                        id = obj.optLong("id", (i + 1).toLong()),
                        subjectId = obj.optLong("subjectId", 1L),
                        parentId = if (obj.isNull("parentId")) null else obj.optLong("parentId"),
                        itemType = runCatching { ItemType.valueOf(obj.optString("itemType", "CHAPTER")) }.getOrDefault(ItemType.CHAPTER),
                        title = obj.optString("title", "Topic ${i + 1}"),
                        orderIndex = obj.optInt("orderIndex", i),
                        status = runCatching { ChapterStatus.valueOf(obj.optString("status", "NOT_STARTED")) }.getOrDefault(ChapterStatus.NOT_STARTED),
                        completionPercentage = obj.optInt("completionPercentage", 0).coerceIn(0, 100),
                        confidence = obj.optInt("confidence", 3).coerceIn(1, 5),
                        priority = runCatching { Priority.valueOf(obj.optString("priority", "MEDIUM")) }.getOrDefault(Priority.MEDIUM),
                        difficulty = runCatching { Difficulty.valueOf(obj.optString("difficulty", "MEDIUM")) }.getOrDefault(Difficulty.MEDIUM),
                        lastStudiedTimestamp = if (obj.isNull("lastStudiedTimestamp")) null else obj.optLong("lastStudiedTimestamp").takeIf { it > 0 },
                        nextRevisionTimestamp = if (obj.isNull("nextRevisionTimestamp")) null else obj.optLong("nextRevisionTimestamp").takeIf { it > 0 },
                        notes = obj.optString("notes", ""),
                        isImportant = obj.optBoolean("isImportant", false),
                        isBookmarked = obj.optBoolean("isBookmarked", false),
                        studyTimeMinutes = obj.optInt("studyTimeMinutes", 0).coerceAtLeast(0),
                        revisionCount = obj.optInt("revisionCount", 0).coerceAtLeast(0),
                        tags = obj.optString("tags", ""),
                        pyqTotal = obj.optInt("pyqTotal", 0).coerceAtLeast(0),
                        pyqAttempted = obj.optInt("pyqAttempted", 0).coerceAtLeast(0),
                        pyqCorrect = obj.optInt("pyqCorrect", 0).coerceAtLeast(0)
                    )
                    items.add(it)
                }
                if (items.isNotEmpty()) {
                    syllabusDao.insertItems(items)
                }
            }

            // 4. Mistakes
            if (root.has("mistakes")) {
                val mistakeArray = root.getJSONArray("mistakes")
                val mistakes = mutableListOf<MistakeEntry>()
                for (i in 0 until mistakeArray.length()) {
                    val obj = mistakeArray.getJSONObject(i)
                    mistakes.add(
                        MistakeEntry(
                            id = obj.optLong("id", (i + 1).toLong()),
                            questionText = obj.optString("questionText", ""),
                            yourWrongAnswer = obj.optString("yourWrongAnswer", ""),
                            correctAnswer = obj.optString("correctAnswer", ""),
                            explanationOrKeyConcept = obj.optString("explanationOrKeyConcept", ""),
                            subjectId = obj.optLong("subjectId", 1L),
                            subjectName = obj.optString("subjectName", "General"),
                            chapterTitle = obj.optString("chapterTitle", "Topic"),
                            sourceMockOrBook = obj.optString("sourceMockOrBook", ""),
                            category = runCatching { MistakeCategory.valueOf(obj.optString("category", "SILLY_MISTAKE")) }.getOrDefault(MistakeCategory.SILLY_MISTAKE),
                            resolutionStatus = runCatching { MistakeResolutionStatus.valueOf(obj.optString("resolutionStatus", "ACTIVE")) }.getOrDefault(MistakeResolutionStatus.ACTIVE),
                            importanceStar = obj.optBoolean("importanceStar", false),
                            tagsCsv = obj.optString("tagsCsv", ""),
                            createdTimestamp = obj.optLong("createdTimestamp", System.currentTimeMillis()),
                            lastReviewedTimestamp = obj.optLong("lastReviewedTimestamp", 0L),
                            nextReviewTimestamp = obj.optLong("nextReviewTimestamp", System.currentTimeMillis()),
                            reviewCount = obj.optInt("reviewCount", 0).coerceAtLeast(0)
                        )
                    )
                }
                if (mistakes.isNotEmpty()) {
                    mistakeDao.insertMistakes(mistakes)
                }
            }

            // 5. Mock Tests
            if (root.has("mockTests")) {
                val mockArray = root.getJSONArray("mockTests")
                val mocks = mutableListOf<MockTest>()
                for (i in 0 until mockArray.length()) {
                    val obj = mockArray.getJSONObject(i)
                    mocks.add(
                        MockTest(
                            id = obj.optLong("id", (i + 1).toLong()),
                            testName = obj.optString("testName", "Mock Test ${i + 1}"),
                            testType = runCatching { MockTestType.valueOf(obj.optString("testType", "FULL_LENGTH")) }.getOrDefault(MockTestType.FULL_LENGTH),
                            testPlatform = obj.optString("testPlatform", "General"),
                            testDateStr = obj.optString("testDateStr", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            totalMarks = obj.optDouble("totalMarks", 200.0).toFloat(),
                            marksScored = obj.optDouble("marksScored", 0.0).toFloat(),
                            totalQuestions = obj.optInt("totalQuestions", 100),
                            attemptedQuestions = obj.optInt("attemptedQuestions", 0),
                            correctQuestions = obj.optInt("correctQuestions", 0),
                            incorrectQuestions = obj.optInt("incorrectQuestions", 0),
                            accuracy = obj.optDouble("accuracy", 0.0).toFloat().coerceIn(0f, 100f),
                            percentile = obj.optDouble("percentile", 0.0).toFloat().coerceIn(0f, 100f),
                            rank = obj.optInt("rank", 0),
                            totalStudents = obj.optInt("totalStudents", 0),
                            cutoffMarks = obj.optDouble("cutoffMarks", 0.0).toFloat(),
                            timeTakenMinutes = obj.optInt("timeTakenMinutes", 60),
                            mathScore = obj.optDouble("mathScore", 0.0).toFloat(),
                            mathTotal = obj.optDouble("mathTotal", 50.0).toFloat(),
                            englishScore = obj.optDouble("englishScore", 0.0).toFloat(),
                            englishTotal = obj.optDouble("englishTotal", 50.0).toFloat(),
                            reasoningScore = obj.optDouble("reasoningScore", 0.0).toFloat(),
                            reasoningTotal = obj.optDouble("reasoningTotal", 50.0).toFloat(),
                            gsScore = obj.optDouble("gsScore", 0.0).toFloat(),
                            gsTotal = obj.optDouble("gsTotal", 50.0).toFloat(),
                            weakAreasIdentified = obj.optString("weakAreasIdentified", ""),
                            analysisNotes = obj.optString("analysisNotes", ""),
                            isClearedCutoff = obj.optBoolean("isClearedCutoff", false)
                        )
                    )
                }
                if (mocks.isNotEmpty()) {
                    mockTestDao.insertMockTests(mocks)
                }
            }

            // 6. Study Sessions
            if (root.has("studySessions")) {
                val sessionArray = root.getJSONArray("studySessions")
                val sessions = mutableListOf<StudySession>()
                for (i in 0 until sessionArray.length()) {
                    val obj = sessionArray.getJSONObject(i)
                    sessions.add(
                        StudySession(
                            id = obj.optLong("id", (i + 1).toLong()),
                            subjectId = obj.optLong("subjectId", 1L),
                            subjectName = obj.optString("subjectName", "General"),
                            chapterId = if (obj.isNull("chapterId")) null else obj.optLong("chapterId"),
                            chapterTitle = obj.optString("chapterTitle", ""),
                            durationSeconds = obj.optLong("durationSeconds", 1500L).coerceAtLeast(0L),
                            mode = runCatching { TimerMode.valueOf(obj.optString("mode", "POMODORO")) }.getOrDefault(TimerMode.POMODORO),
                            notes = obj.optString("notes", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                if (sessions.isNotEmpty()) {
                    studySessionDao.insertSessions(sessions)
                }
            }

            // 7. Study Plans
            if (root.has("studyPlans")) {
                val planArray = root.getJSONArray("studyPlans")
                for (i in 0 until planArray.length()) {
                    val obj = planArray.getJSONObject(i)
                    studyPlanDao.insertPlan(
                        StudyPlan(
                            id = obj.optLong("id", (i + 1).toLong()),
                            dateStr = obj.optString("dateStr", ""),
                            timeStr = obj.optString("timeStr", "09:00"),
                            subjectId = obj.optLong("subjectId", 1L),
                            subjectName = obj.optString("subjectName", "Subject"),
                            chapterTitle = obj.optString("chapterTitle", "Topic"),
                            plannedMinutes = obj.optInt("plannedMinutes", 45).coerceAtLeast(0),
                            actualMinutes = obj.optInt("actualMinutes", 0).coerceAtLeast(0),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            goalNotes = obj.optString("goalNotes", "")
                        )
                    )
                }
            }

            // 8. Goals
            if (root.has("goals")) {
                val goalArray = root.getJSONArray("goals")
                for (i in 0 until goalArray.length()) {
                    val obj = goalArray.getJSONObject(i)
                    goalDao.insertGoal(
                        Goal(
                            id = obj.optLong("id", (i + 1).toLong()),
                            title = obj.optString("title", "Goal ${i + 1}"),
                            targetDateStr = obj.optString("targetDateStr", ""),
                            subjectId = if (obj.isNull("subjectId")) null else obj.optLong("subjectId"),
                            subjectName = obj.optString("subjectName", "General"),
                            targetChaptersCount = obj.optInt("targetChaptersCount", 10),
                            completedChaptersCount = obj.optInt("completedChaptersCount", 0),
                            targetStudyHours = obj.optDouble("targetStudyHours", 20.0).toFloat().coerceAtLeast(0f),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
            }

            // 9. Badges
            if (root.has("badges")) {
                val badgeArray = root.getJSONArray("badges")
                val badges = mutableListOf<AchievementBadge>()
                for (i in 0 until badgeArray.length()) {
                    val obj = badgeArray.getJSONObject(i)
                    badges.add(
                        AchievementBadge(
                            id = obj.optString("id", "badge_$i"),
                            title = obj.optString("title", "Achievement"),
                            description = obj.optString("description", ""),
                            category = runCatching { BadgeCategory.valueOf(obj.optString("category", "STREAK")) }.getOrDefault(BadgeCategory.STREAK),
                            iconEmoji = obj.optString("iconEmoji", "🏆"),
                            tier = runCatching { BadgeTier.valueOf(obj.optString("tier", "BRONZE")) }.getOrDefault(BadgeTier.BRONZE),
                            isUnlocked = obj.optBoolean("isUnlocked", false),
                            unlockedAt = if (obj.isNull("unlockedAt")) null else obj.optLong("unlockedAt"),
                            currentProgress = obj.optInt("currentProgress", 0),
                            maxProgress = obj.optInt("maxProgress", 1),
                            rewardXp = obj.optInt("rewardXp", 50),
                            hintRequirement = obj.optString("hintRequirement", "")
                        )
                    )
                }
                if (badges.isNotEmpty()) {
                    achievementBadgeDao.insertBadges(badges)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
