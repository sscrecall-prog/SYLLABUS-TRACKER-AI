import re

# 1. Fix WeakTopicsScreen
with open('app/src/main/java/com/example/ui/screens/WeakTopicsScreen.kt', 'r') as f:
    wt_content = f.read()

wt_content = wt_content.replace(
"""fun WeakChaptersScreen(
    onNavigate: (NavDestination) -> Unit
    val syllabusViewModel: SyllabusViewModel = viewModel()
    val subjectViewModel: SubjectViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()
) {""",
"""fun WeakChaptersScreen(
    onNavigate: (NavDestination) -> Unit,
    syllabusViewModel: SyllabusViewModel = viewModel(),
    subjectViewModel: SubjectViewModel = viewModel(),
    timerViewModel: TimerViewModel = viewModel()
) {""")

wt_content = wt_content.replace('syllabusViewModel.navigateTo(NavDestination.TIMER)', 'onNavigate(NavDestination.TIMER)')
wt_content = wt_content.replace('timerViewModel.setTimerTarget(sub, it)', 'timerViewModel.setTimerTarget(sub, it)')

with open('app/src/main/java/com/example/ui/screens/WeakTopicsScreen.kt', 'w') as f:
    f.write(wt_content)

# 2. Fix SyllabusScreen
with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'r') as f:
    s_content = f.read()

# Fix viewModel invocations
# e: file:///app/applet/app/src/main/java/com/example/ui/screens/SyllabusScreen.kt:453:64 Function invocation 'viewModel(...)' expected.
s_content = re.sub(
    r'onAction = \{ action, item -> handleItemAction\(action, item, \{ onOpenBulk\(it\) \}, syllabusViewModel, subjectViewModel, timerViewModel\) \}',
    r'onAction = { action, item -> handleItemAction(action, item, { onOpenBulk(it) }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate) }',
    s_content
)
s_content = re.sub(
    r'onAction = \{ action, item -> handleItemAction\(action, item, \{\}, syllabusViewModel, subjectViewModel, timerViewModel\) \}',
    r'onAction = { action, item -> handleItemAction(action, item, {}, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate) }',
    s_content
)

s_content = s_content.replace(
"""private fun handleItemAction(
    action: String,
    item: SyllabusItem,
    onOpenBulk: (SyllabusItem) -> Unit,
    syllabusViewModel: SyllabusViewModel,
    subjectViewModel: SubjectViewModel,
    timerViewModel: TimerViewModel
) {""",
"""private fun handleItemAction(
    action: String,
    item: SyllabusItem,
    onOpenBulk: (SyllabusItem) -> Unit,
    syllabusViewModel: SyllabusViewModel,
    subjectViewModel: SubjectViewModel,
    timerViewModel: TimerViewModel,
    onNavigate: (NavDestination) -> Unit
) {""")

s_content = s_content.replace('syllabusViewModel.navigateTo(NavDestination.TIMER)', 'onNavigate(NavDestination.TIMER)')

with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'w') as f:
    f.write(s_content)


# 3. Add missing properties to SyllabusViewModel
with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'r') as f:
    svm_content = f.read()

filters = """
    val filterStatus = MutableStateFlow<ChapterStatus?>(null)
    val filterPriority = MutableStateFlow<Priority?>(null)
    val filterDifficulty = MutableStateFlow<Difficulty?>(null)
    val filterOnlyWeak = MutableStateFlow(false)
    val filterOnlyRevisionDue = MutableStateFlow(false)

    private val _selectedChapter = MutableStateFlow<SyllabusItem?>(null)
    val selectedChapter = _selectedChapter.asStateFlow()

    fun selectChapter(item: SyllabusItem?) {
        _selectedChapter.value = item
    }
"""

svm_content = svm_content.replace('class SyllabusViewModel(application: Application) : BaseViewModel(application) {', 'class SyllabusViewModel(application: Application) : BaseViewModel(application) {\n' + filters)

with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'w') as f:
    f.write(svm_content)

