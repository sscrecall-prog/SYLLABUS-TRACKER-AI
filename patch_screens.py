import os
import re
import glob

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

prop_to_vm = {}
for vm, props in mappings.items():
    for p in props:
        prop_to_vm[p] = vm

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Find which viewmodels are used
    used_vms = set()
    for prop, vm in prop_to_vm.items():
        if re.search(r'\bviewModel\.' + prop + r'\b', content):
            used_vms.add(vm)
            content = re.sub(r'\bviewModel\.' + prop + r'\b', vm[0].lower() + vm[1:] + '.' + prop, content)
            
    if not used_vms:
        return # Skip if no viewmodel used

    # If SyllabusViewModel is no longer used, remove its import. 
    # But wait, we should add imports for the used ViewModels!
    # And add androidx.lifecycle.viewmodel.compose.viewModel
    
    import_block = "import androidx.lifecycle.viewmodel.compose.viewModel\n"
    for vm in used_vms:
        import_block += f"import com.example.ui.viewmodel.{vm}\n"
    
    # We can inject imports near the top
    content = re.sub(r'(import com\.example\.ui\.viewmodel\.SyllabusViewModel\n)', import_block, content)

    # Now we need to remove `viewModel: SyllabusViewModel` from the signature
    content = re.sub(r'viewModel:\s*SyllabusViewModel,?\s*', '', content)
    # also for MainScreen
    content = re.sub(r'viewModel\s*=\s*viewModel,?\s*', '', content)
    # also for cases without commas
    content = re.sub(r'viewModel:\s*SyllabusViewModel', '', content)
    
    # Now we need to inject the viewmodel instantiations at the start of the composable
    # We find the composable signature end `{`
    # It's safer to just inject it after `@Composable\nfun [^{]+\{`
    
    def inject_vms(match):
        vms_code = "\n"
        for vm in used_vms:
            var_name = vm[0].lower() + vm[1:]
            vms_code += f"    val {var_name}: {vm} = viewModel()\n"
        return match.group(0) + vms_code
    
    # Find the main composable function in the file
    # Usually it matches the filename
    screen_name = os.path.basename(filepath).replace('.kt', '')
    content = re.sub(rf'(@Composable\s+fun\s+{screen_name}[^{{]*{{)', inject_vms, content)
    
    with open(filepath, 'w') as f:
        f.write(content)

for filepath in glob.glob('app/src/main/java/com/example/ui/screens/*.kt'):
    process_file(filepath)

