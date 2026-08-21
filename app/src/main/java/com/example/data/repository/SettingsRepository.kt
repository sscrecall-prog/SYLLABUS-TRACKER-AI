package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settingsDao: SettingsDao,
    private val achievementBadgeDao: AchievementBadgeDao,
    private val subjectDao: SubjectDao,
    private val syllabusDao: SyllabusDao,
    private val studySessionDao: StudySessionDao,
    private val studyPlanDao: StudyPlanDao,
    private val goalDao: GoalDao,
    private val mockTestDao: MockTestDao
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

        subjectDao.insertSubjects(PreloadData.defaultSubjects)
        syllabusDao.insertItems(PreloadData.createDefaultSyllabusItems())
        for (g in PreloadData.defaultGoals) goalDao.insertGoal(g)
        for (p in PreloadData.createSampleStudyPlans()) studyPlanDao.insertPlan(p)
        mockTestDao.insertMockTests(PreloadData.createSampleMockTests())
        settingsDao.insertOrUpdate(AppSettings())
    }

    // Export to JSON

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val subjectsList = subjectDao.getAllSubjects().first()
        val itemsList = syllabusDao.getAllItems().first()
        val goalsList = goalDao.getAllGoals().first()

        val subArray = JSONArray()
        for (s in subjectsList) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("code", s.code)
            obj.put("colorHex", s.colorHex)
            obj.put("iconName", s.iconName)
            obj.put("description", s.description)
            subArray.put(obj)
        }
        root.put("subjects", subArray)

        val itemArray = JSONArray()
        for (it in itemsList) {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("subjectId", it.subjectId)
            obj.put("parentId", it.parentId ?: JSONObject.NULL)
            obj.put("itemType", it.itemType.name)
            obj.put("title", it.title)
            obj.put("status", it.status.name)
            obj.put("completionPercentage", it.completionPercentage)
            obj.put("confidence", it.confidence)
            obj.put("priority", it.priority.name)
            obj.put("difficulty", it.difficulty.name)
            obj.put("notes", it.notes)
            obj.put("isImportant", it.isImportant)
            obj.put("isBookmarked", it.isBookmarked)
            obj.put("studyTimeMinutes", it.studyTimeMinutes)
            obj.put("revisionCount", it.revisionCount)
            obj.put("tags", it.tags)
            obj.put("pyqTotal", it.pyqTotal)
            obj.put("pyqAttempted", it.pyqAttempted)
            obj.put("pyqCorrect", it.pyqCorrect)
            itemArray.put(obj)
        }
        root.put("items", itemArray)
        root.put("exportDate", System.currentTimeMillis())
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
            sb.append("\"$subName\",\"${it.itemType.label}\",\"$cleanTitle\",\"${it.status.label}\",${it.completionPercentage},${it.confidence},\"${it.priority.label}\",\"${it.difficulty.label}\",${it.studyTimeMinutes},${it.revisionCount},\"${it.tags}\",${it.pyqAttempted},${it.pyqCorrect},\"$cleanNotes\"\n")
        }
        sb.toString()
    }

    // Import from JSON

    suspend fun importFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("subjects")) {
                val subArray = root.getJSONArray("subjects")
                for (i in 0 until subArray.length()) {
                    val obj = subArray.getJSONObject(i)
                    val s = Subject(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        code = obj.optString("code", ""),
                        colorHex = obj.optString("colorHex", "#2D4F1E"),
                        iconName = obj.optString("iconName", "School"),
                        description = obj.optString("description", "")
                    )
                    subjectDao.insertSubject(s)
                }
            }
            if (root.has("items")) {
                val itemArray = root.getJSONArray("items")
                for (i in 0 until itemArray.length()) {
                    val obj = itemArray.getJSONObject(i)
                    val it = SyllabusItem(
                        id = obj.optLong("id", 0L),
                        subjectId = obj.getLong("subjectId"),
                        parentId = if (obj.isNull("parentId")) null else obj.getLong("parentId"),
                        itemType = try { ItemType.valueOf(obj.getString("itemType")) } catch (e: Exception) { ItemType.CHAPTER },
                        title = obj.getString("title"),
                        status = try { ChapterStatus.valueOf(obj.getString("status")) } catch (e: Exception) { ChapterStatus.NOT_STARTED },
                        completionPercentage = obj.optInt("completionPercentage", 0),
                        confidence = obj.optInt("confidence", 3),
                        priority = try { Priority.valueOf(obj.getString("priority")) } catch (e: Exception) { Priority.MEDIUM },
                        difficulty = try { Difficulty.valueOf(obj.getString("difficulty")) } catch (e: Exception) { Difficulty.MEDIUM },
                        notes = obj.optString("notes", ""),
                        isImportant = obj.optBoolean("isImportant", false),
                        isBookmarked = obj.optBoolean("isBookmarked", false),
                        studyTimeMinutes = obj.optInt("studyTimeMinutes", 0),
                        revisionCount = obj.optInt("revisionCount", 0),
                        tags = obj.optString("tags", ""),
                        pyqTotal = obj.optInt("pyqTotal", 0),
                        pyqAttempted = obj.optInt("pyqAttempted", 0),
                        pyqCorrect = obj.optInt("pyqCorrect", 0)
                    )
                    syllabusDao.insertItem(it)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
