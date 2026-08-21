import re

with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'r') as f:
    content = f.read()

# I will replace all handleItemAction calls in SyllabusScreen.kt to pass the proper viewModels
content = re.sub(
    r'handleItemAction\(action, item, viewModel, onOpenBulk = \{ sec ->\n\s*bulkAddParentSection = sec\n\s*showBulkAddDialog = true\n\s*\}\)',
    r'handleItemAction(action, item, { sec -> bulkAddParentSection = sec; showBulkAddDialog = true }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)',
    content
)

content = re.sub(
    r'handleItemAction\(action, item, viewModel, onOpenBulk = \{ bulkAddParentSection = it; showBulkAddDialog = true \}\)',
    r'handleItemAction(action, item, { bulkAddParentSection = it; showBulkAddDialog = true }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)',
    content
)

content = re.sub(
    r'handleItemAction\(action, item, viewModel, onOpenBulk = \{ \}\)',
    r'handleItemAction(action, item, {}, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)',
    content
)

content = content.replace("handleItemAction(action, item, viewModel", "handleItemAction(action, item, {}, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate")

content = content.replace("mainViewModel.openSubjectDetail", "mainViewModel.openSubjectDetail")
content = content.replace("syllabusViewModel.openSubjectDetail", "mainViewModel.openSubjectDetail")
content = content.replace("syllabusViewModel.selectChapter", "syllabusViewModel.selectChapter")

with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'w') as f:
    f.write(content)
