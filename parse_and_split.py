import os
import re
import json

with open('app/src/main/java/com/example/data/repository/SyllabusRepository.kt', 'r') as f:
    lines = f.readlines()

members = {}
current_member = []
brace_level = 0
member_name = None
inside_class = False

for line in lines:
    if "class SyllabusRepository" in line:
        inside_class = True
        continue
    
    if not inside_class:
        continue
        
    if "{" in line and "}" in line:
        pass # Handle on same line below
        
    # We need a robust brace counter that ignores strings
    def count_braces(s):
        # naive but usually works for kotlin if no braces in strings
        return s.count('{') - s.count('}')

    if brace_level == 1 or (brace_level == 0 and "}" not in line):
        # We are at class level
        # Look for method/property start
        match = re.match(r'^\s*(val|suspend\s+fun|fun)\s+([a-zA-Z0-9_]+)', line)
        if match:
            if current_member and member_name:
                members[member_name] = "".join(current_member)
            current_member = [line]
            member_name = match.group(2)
            brace_level += count_braces(line)
        else:
            if current_member:
                current_member.append(line)
                brace_level += count_braces(line)
    else:
        if current_member:
            current_member.append(line)
            brace_level += count_braces(line)
            
    if inside_class and brace_level == 0 and line.strip() == "}":
        if current_member and member_name:
            members[member_name] = "".join(current_member)
        break

# Manual grouping
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
                print(f"WARNING: Method {m} not found!")
                
        f.write("}\n")

print("Generated all repositories.")
