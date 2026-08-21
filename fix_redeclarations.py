import os
import glob

# Models to extract and remove from other files
models = """
enum class NavDestination(val label: String, val iconName: String) {
    DASHBOARD("Home", "Dashboard"),
    SUBJECTS("Subjects", "School"),
    SYLLABUS("Syllabus", "AutoStories"),
    MISTAKES("Mistake Notebook", "BookmarkRemove"),
    REVISION("Revision", "Update"),
    PLANNER("Planner", "CalendarMonth"),
    MOCK_TESTS("Mock Tests", "Quiz"),
    ANALYTICS("Analytics", "Analytics"),
    WEAK_TOPICS("Weak", "ReportProblem"),
    GOALS("Goals", "TrackChanges"),
    TIMER("Timer", "Timer"),
    CALENDAR("Calendar", "Event"),
    PROFILE("Profile", "MilitaryTech"),
    SETTINGS("Settings", "Settings")
}

data class SubjectStats(
    val subject: Subject,
    val totalSections: Int,
    val totalChapters: Int,
    val completedChapters: Int,
    val inProgressChapters: Int,
    val notStartedChapters: Int,
    val weakChapters: Int,
    val revisionDueChapters: Int,
    val completionPercentage: Int,
    val totalStudyMinutes: Int,
    val averageConfidence: Float,
    val pyqAttempted: Int,
    val pyqCorrect: Int,
    val pyqAccuracy: Int
)

data class MockStats(
    val totalMocksCount: Int = 0,
    val averageScore: Float = 0f,
    val highestScore: Float = 0f,
    val latestScore: Float = 0f,
    val averagePercentile: Float = 0f,
    val bestPercentile: Float = 0f,
    val averageAccuracy: Float = 0f,
    val clearedCutoffCount: Int = 0,
    val cutoffClearanceRate: Int = 0,
    val averageQuantScore: Float = 0f,
    val averageEnglishScore: Float = 0f,
    val averageReasoningScore: Float = 0f,
    val averageGsScore: Float = 0f,
    val averageTimeTakenMinutes: Int = 0,
    val marksPerMinute: Float = 0f,
    val scoreProgression: List<Pair<String, Float>> = emptyList()
)

data class OverallStats(
    val totalSubjects: Int,
    val totalSections: Int,
    val totalChapters: Int,
    val completedChapters: Int,
    val inProgressChapters: Int,
    val notStartedChapters: Int,
    val weakChapters: Int,
    val revisionDueChapters: Int,
    val masteredChapters: Int,
    val completionPercentage: Int,
    val totalStudyMinutes: Int,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val todayStudyMinutes: Int
)

data class FilterCriteria(
    val query: String = "",
    val subjectId: Long? = null,
    val status: ChapterStatus? = null,
    val difficulty: Difficulty? = null,
    val priority: Priority? = null,
    val onlyWeak: Boolean = false,
    val onlyRevisionDue: Boolean = false
)
"""

with open('app/src/main/java/com/example/ui/viewmodel/AppModels.kt', 'w') as f:
    f.write("package com.example.ui.viewmodel\n")
    f.write("import com.example.data.model.*\n")
    f.write(models)

# Now remove these from all viewmodels
for filepath in glob.glob('app/src/main/java/com/example/ui/viewmodel/*.kt'):
    if 'AppModels.kt' in filepath: continue
    
    with open(filepath, 'r') as f:
        content = f.read()
    
    # We can just remove the lines using string splitting because the prefix was exactly the same
    # But it's easier to find "enum class NavDestination" and delete until "class XViewModel"
    import re
    
    content = re.sub(r'enum class NavDestination.*?data class FilterCriteria.*?\)[\r\n]+', '', content, flags=re.DOTALL)
    
    with open(filepath, 'w') as f:
        f.write(content)

