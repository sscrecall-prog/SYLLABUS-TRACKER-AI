import re

with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'handleItemAction\(action, section, viewModel, onOpenBulk = \{ sec ->\n\s*bulkAddParentSection = sec\n\s*showBulkAddDialog = true\n\s*\}\)',
    r'handleItemAction(action, section, { sec -> bulkAddParentSection = sec; showBulkAddDialog = true }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)',
    content
)

content = re.sub(
    r'handleItemAction\(action, subsection, viewModel, onOpenBulk = \{ sec ->\n\s*bulkAddParentSection = sec\n\s*showBulkAddDialog = true\n\s*\}\)',
    r'handleItemAction(action, subsection, { sec -> bulkAddParentSection = sec; showBulkAddDialog = true }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)',
    content
)

content = re.sub(
    r'handleItemAction\(action, chapter, viewModel, onOpenBulk = \{ \}\)',
    r'handleItemAction(action, chapter, {}, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)',
    content
)

content = re.sub(
    r'handleItemAction\(action, chapter, viewModel, onOpenBulk = \{ sec ->\n\s*bulkAddParentSection = sec\n\s*showBulkAddDialog = true\n\s*\}\)',
    r'handleItemAction(action, chapter, { sec -> bulkAddParentSection = sec; showBulkAddDialog = true }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)',
    content
)


# Also ensure any other `viewModel,` are caught
content = content.replace("viewModel, onOpenBulk = { sec ->", "{ sec ->")
content = content.replace("viewModel, onOpenBulk = { }", "{}")


with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'w') as f:
    f.write(content)

