import os
import re

with open('app/src/main/java/com/example/data/repository/SyllabusRepository.kt', 'r') as f:
    lines = f.readlines()

members = {}
current_member = []
member_name = None

inside_class = False

for line in lines:
    if line.startswith("class SyllabusRepository"):
        inside_class = True
        continue
    
    if not inside_class:
        continue
        
    if line.strip() == "}" and line.startswith("}"):
        if current_member and member_name:
            members[member_name] = "".join(current_member)
        break
        
    # Check if this line starts a new member. It starts with exactly 4 spaces (or maybe 1 tab).
    # Actually, look for match at start of string (ignoring whitespace).
    match = re.match(r'^ {4}(val|suspend\s+fun|fun)\s+([a-zA-Z0-9_]+)', line)
    if match:
        if current_member and member_name:
            members[member_name] = "".join(current_member)
        current_member = [line]
        member_name = match.group(2)
    else:
        if current_member:
            current_member.append(line)

# Handle the last member if file ended abruptly without } at column 0
if current_member and member_name and member_name not in members:
    members[member_name] = "".join(current_member)

groups = {
    "SubjectRepository": {
        "daos": ["private val subjectDao: SubjectDao"],
        "methods": ["allSubjects", "insertSubject", "updateSubject", "deleteSubject", "reorderSubjects"]
    },
    "SyllabusRepository": {
        "daos": ["private val syllabusDao: SyllabusDao", "private val settingsDao: SettingsDao"],
        "methods": ["allItems", "allSubjectHierarchies", "getItemsForSubject", "searchItems", "insertItem", "insertItems", "updateItem", "deleteItem", "duplicateItem", "moveItem", "reorderItems", "updateChapterStatus"]
    },
    "RevisionRepository": {
        "daos": ["private val syllabusDao: SyllabusDao", "private val settingsDao: SettingsDao"],
        "methods": ["markChapterRevised", "scheduleCustomRevision"]
    },
    "StudySessionRepository": {
        "daos": ["private val studySessionDao: StudySessionDao", "private val syllabusDao: SyllabusDao"],
        "methods": ["allStudySessions", "logStudySession"]
    },
    "PlannerRepository": {
        "daos": ["private val studyPlanDao: StudyPlanDao"],
        "methods": ["allPlans", "getPlansForDate", "insertPlan", "updatePlan", "deletePlan"]
    },
    "GoalRepository": {
        "daos": ["private val goalDao: GoalDao"],
        "methods": ["allGoals", "insertGoal", "updateGoal", "deleteGoal"]
    },
    "MockTestRepository": {
        "daos": ["private val mockTestDao: MockTestDao"],
        "methods": ["allMockTests", "insertMockTest", "updateMockTest", "deleteMockTest", "getMockTestById"]
    },
    "MistakeRepository": {
        "daos": ["private val mistakeDao: MistakeDao"],
        "methods": ["allMistakes", "insertMistake", "updateMistake", "deleteMistake", "markMistakeReviewed", "toggleMistakeStar"]
    },
    "SettingsRepository": {
        "daos": [
            "private val settingsDao: SettingsDao", 
            "private val achievementBadgeDao: AchievementBadgeDao",
            "private val subjectDao: SubjectDao",
            "private val syllabusDao: SyllabusDao",
            "private val studySessionDao: StudySessionDao",
            "private val studyPlanDao: StudyPlanDao",
            "private val goalDao: GoalDao",
            "private val mockTestDao: MockTestDao"
        ],
        "methods": ["appSettings", "allBadges", "unlockedBadges", "updateBadge", "insertBadges", "updateSettings", "resetToSampleData", "exportToJson", "exportToCsv", "importFromJson"]
    },
    "AnalyticsRepository": {
        "daos": [],
        "methods": []
    }
}

os.makedirs('app/src/main/java/com/example/data/repository', exist_ok=True)

imports = """package com.example.data.repository

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

"""

for repo_name, config in groups.items():
    with open(f'app/src/main/java/com/example/data/repository/{repo_name}.kt', 'w') as f:
        f.write(imports)
        
        dao_str = ",\n    ".join(config["daos"])
        f.write(f"class {repo_name}(\n    {dao_str}\n) {{\n")
        
        for m in config["methods"]:
            if m in members:
                f.write(members[m])
                f.write("\n")
            else:
                print(f"WARNING: Method {m} not found in parsed members!")
                
        f.write("}\n")

print("Regenerated all repositories correctly.")
