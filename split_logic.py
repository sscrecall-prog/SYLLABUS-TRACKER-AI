import re
import os

mappings = {
    'TimerViewModel': ['timerMode', 'timerRemainingSeconds', 'timerTotalDurationSeconds', 'isTimerRunning', 'timerSubject', 'timerChapter', 'pomodoroCyclesCompleted', 'timerJob', 'ambientSound', 'ambientVolume', 'isAmbientPlaying', 'ambientAutoPlayWithTimer', 'setTimerTarget', 'setTimerPreset', 'startTimer', 'pauseTimer', 'resetTimer', 'selectAmbientSound', 'setAmbientVolume', 'toggleAmbientPlayPause', 'toggleAmbientAutoPlayWithTimer', 'setTimerMode', 'setTimerDuration', 'selectTimerSubject', 'selectTimerChapter', 'finishAndSaveTimerSession', 'finishAndLogTimer', 'logCompletedTimerSession'],
    'MockTestsViewModel': ['mockTests', 'mockSearchQuery', 'mockPlatformFilter', 'mockTypeFilter', 'selectedMockTest', 'filteredMockTests', 'mockStats', 'addMockTest', 'updateMockTest', 'deleteMockTest', 'selectMockTest', 'setMockPlatformFilter', 'setMockTypeFilter', 'setMockSearchQuery'],
    'MistakeNotebookViewModel': ['mistakes', 'selectedMistake', 'mistakeFilterSubjectId', 'mistakeFilterCategory', 'mistakeFilterStatus', 'mistakeSearchQuery', 'mistakeFilterOnlyStarred', 'mistakeFilterOnlyReviewDue', 'mistakeStats', 'subId', 'cat', 'status', 'query', 'onlyStarred', 'onlyDue', 'filterPart1', 'mistakeFilterFlow', 'filteredMistakes', 'addMistake', 'updateMistake', 'deleteMistake', 'markMistakeReviewed', 'toggleMistakeStar', 'selectMistake', 'setMistakeFilterSubject', 'setMistakeFilterCategory', 'setMistakeFilterStatus', 'setMistakeSearchQuery', 'toggleMistakeFilterStarred', 'toggleMistakeFilterReviewDue'],
    'GoalsViewModel': ['goals', 'addGoal', 'updateGoal', 'toggleGoalCompleted', 'toggleGoalCompletion', 'deleteGoal'],
    'PlannerViewModel': ['todayDateStr', 'todayPlans', 'allPlans', 'addStudyPlan', 'togglePlanCompleted', 'togglePlanCompletion', 'deletePlan', 'deleteStudyPlan'],
    'ProfileViewModel': ['allBadges', 'unlockedBadges', 'newlyUnlockedBadge', 'evaluateAchievements', 'clearNewlyUnlockedBadge'],
    'SettingsViewModel': ['appSettings', 'updateUserProfile', 'updateExamTarget', 'updateReducedMotion', 'updateThemeMode', 'updateRevisionIntervals', 'updateStudyTargets', 'resetData', 'getExportJson', 'getExportCsv', 'importData'],
    'AnalyticsViewModel': ['examPaceStats', 'overallStats'],
    'SubjectViewModel': ['subjects', 'subjectStatsList', 'allSubjectHierarchies', 'addSubject', 'updateSubject', 'deleteSubject'],
    'SyllabusViewModel': ['items', 'studySessions', 'searchQuery', 'filterSubjectId', 'filterStatus', 'filterDifficulty', 'filterPriority', 'filterOnlyWeak', 'filterOnlyRevisionDue', 'filterCriteria', 'filteredItems', 'addItem', 'addSection', 'addChapter', 'bulkAddChapters', 'duplicateItem', 'moveItemUp', 'moveItemDown', 'updateItem', 'updateChapterStatus', 'markChapterRevised', 'completeRevision', 'markChapterStrong', 'updateChapterConfidence', 'updatePyqStats', 'toggleBookmark', 'toggleImportant', 'deleteItem', 'autoTagWeakSyllabusChapters'],
    'MainViewModel': ['_currentNav', 'currentNav', 'navBackStack', 'transitionDirection', 'navigateTo', 'navigateBack', 'canNavigateBack', '_selectedSubjectId', 'selectedSubjectId', 'openSubjectDetail', '_selectedChapter', 'selectedChapter', 'selectChapter', 'snackbarMessage', 'showSnackbar', 'clearSnackbar']
}

with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'r') as f:
    lines = f.readlines()

class_start = -1
for i, line in enumerate(lines):
    if line.startswith("class SyllabusViewModel"):
        class_start = i
        break

prefix = lines[:class_start]
body = lines[class_start+1:]

members = {}
current_member = []
brace_level = 1
member_name = None

for line in body:
    if brace_level == 1:
        match = re.match(r'^\s*(?:private\s+)?(?:suspend\s+)?(?:val|var|fun)\s+([a-zA-Z0-9_]+)', line)
        if match:
            if current_member:
                members[member_name] = current_member
            current_member = [line]
            member_name = match.group(1)
        elif line.strip() == "init {":
            if current_member:
                members[member_name] = current_member
            current_member = [line]
            member_name = "init"
        elif current_member:
            current_member.append(line)
    else:
        if current_member:
            current_member.append(line)
            
    brace_level += line.count('{')
    brace_level -= line.count('}')
    
    if brace_level == 0:
        if current_member:
            members[member_name] = current_member
        break

# Write BaseViewModel.kt
with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'w') as f:
    f.write("package com.example.ui.viewmodel\n\n")
    f.write("import android.app.Application\n")
    f.write("import androidx.lifecycle.AndroidViewModel\n")
    f.write("import androidx.lifecycle.viewModelScope\n")
    f.write("import com.example.data.local.AppDatabase\n")
    f.write("import com.example.data.repository.SyllabusRepository\n")
    f.write("import com.example.data.model.*\n")
    f.write("import kotlinx.coroutines.flow.*\n")
    f.write("import kotlinx.coroutines.launch\n")
    f.write("import kotlinx.coroutines.Dispatchers\n")
    f.write("import java.util.Calendar\n")
    f.write("import java.util.Date\n")
    f.write("import java.text.SimpleDateFormat\n")
    f.write("import java.util.Locale\n")
    f.write("\n")
    f.write("open class BaseViewModel(application: Application) : AndroidViewModel(application) {\n")
    f.write("    protected val repository: SyllabusRepository\n")
    f.writelines(members.get('init', []))
    f.write("}\n")

imports = "".join(prefix)

# Write other ViewModels
for vm_name, mapped_members in mappings.items():
    with open(f'app/src/main/java/com/example/ui/viewmodel/{vm_name}.kt', 'w') as f:
        f.write(imports)
        f.write(f"\nclass {vm_name}(application: Application) : BaseViewModel(application) {{\n")
        
        for m in mapped_members:
            if m in members:
                f.writelines(members[m])
            else:
                print(f"Warning: Member {m} not found in SyllabusViewModel")
        
        f.write("}\n")

# Make sure all members were mapped, if not print them
all_mapped = [item for sublist in mappings.values() for item in sublist]
for name in members.keys():
    if name != 'init' and name not in all_mapped:
        print(f"Unmapped member: {name}")

