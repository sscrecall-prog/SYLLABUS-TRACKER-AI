import os
import re

mapping = {
    "subjectRepository": ["allSubjects", "insertSubject", "updateSubject", "deleteSubject", "reorderSubjects"],
    "syllabusRepository": ["allItems", "allSubjectHierarchies", "getItemsForSubject", "searchItems", "insertItem", "insertItems", "updateItem", "deleteItem", "duplicateItem", "moveItem", "reorderItems", "updateChapterStatus"],
    "revisionRepository": ["markChapterRevised", "scheduleCustomRevision"],
    "studySessionRepository": ["allStudySessions", "logStudySession"],
    "plannerRepository": ["allPlans", "getPlansForDate", "insertPlan", "updatePlan", "deletePlan"],
    "goalRepository": ["allGoals", "insertGoal", "updateGoal", "deleteGoal"],
    "mockTestRepository": ["allMockTests", "insertMockTest", "updateMockTest", "deleteMockTest", "getMockTestById"],
    "mistakeRepository": ["allMistakes", "insertMistake", "updateMistake", "deleteMistake", "markMistakeReviewed", "toggleMistakeStar"],
    "settingsRepository": ["appSettings", "allBadges", "unlockedBadges", "updateBadge", "insertBadges", "updateSettings", "resetToSampleData", "exportToJson", "exportToCsv", "importFromJson"]
}

vm_dir = 'app/src/main/java/com/example/ui/viewmodel/'
for file in os.listdir(vm_dir):
    if not file.endswith('.kt'): continue
    filepath = os.path.join(vm_dir, file)
    with open(filepath, 'r') as f:
        content = f.read()
        
    for repo, methods in mapping.items():
        for method in methods:
            # Replace repository.method with repo.method
            content = re.sub(r'\brepository\.' + method + r'\b', f'{repo}.{method}', content)
            
    with open(filepath, 'w') as f:
        f.write(content)

print("Replaced all repository references.")
