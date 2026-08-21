import re

with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""private fun handleItemAction(
    action: String,
    item: SyllabusItem,
    onOpenBulk: (SyllabusItem) -> Unit
) {""",
"""private fun handleItemAction(
    action: String,
    item: SyllabusItem,
    onOpenBulk: (SyllabusItem) -> Unit,
    syllabusViewModel: SyllabusViewModel,
    subjectViewModel: SubjectViewModel,
    timerViewModel: TimerViewModel
) {"""
)

# And fix the calls to it:
content = re.sub(
    r'onAction = \{ action, item -> handleItemAction\(action, item, \{ onOpenBulk\(it\) \}\) \}',
    r'onAction = { action, item -> handleItemAction(action, item, { onOpenBulk(it) }, syllabusViewModel, subjectViewModel, timerViewModel) }',
    content
)
content = re.sub(
    r'onAction = \{ action, item -> handleItemAction\(action, item, \{\} \) \}',
    r'onAction = { action, item -> handleItemAction(action, item, {}, syllabusViewModel, subjectViewModel, timerViewModel) }',
    content
)

with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'w') as f:
    f.write(content)

