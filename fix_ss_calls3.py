import re

with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'r') as f:
    content = f.read()

# I will just regex find all handleItemAction(action, item, callback) and add the viewmodels
content = re.sub(
    r'handleItemAction\(action, ([\w]+), \{ sec ->\s*bulkAddParentSection = sec\s*showBulkAddDialog = true\s*\}\)',
    r'handleItemAction(action, \1, { sec -> bulkAddParentSection = sec; showBulkAddDialog = true }, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)',
    content
)

content = re.sub(
    r'handleItemAction\(action, ([\w]+), \{ \}\)',
    r'handleItemAction(action, \1, {}, syllabusViewModel, subjectViewModel, timerViewModel, onNavigate)',
    content
)

with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'w') as f:
    f.write(content)
