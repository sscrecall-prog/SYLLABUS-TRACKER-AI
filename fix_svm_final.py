import re

with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'r') as f:
    content = f.read()

# Remove the duplicate _selectedChapter
duplicate_str = """
    private val _selectedChapter = MutableStateFlow<SyllabusItem?>(null)
    val selectedChapter = _selectedChapter.asStateFlow()

    fun selectChapter(item: SyllabusItem?) {
        _selectedChapter.value = item
    }
"""
content = content.replace(duplicate_str, '', 1)

# Fix the Triple destructuring
content = content.replace(
    "} { base, (prio, weak, rev) ->",
    "} { base, triple ->"
)
content = content.replace(
    "base.copy(priority = prio, onlyWeak = weak, onlyRevisionDue = rev)",
    "base.copy(priority = triple.first, onlyWeak = triple.second, onlyRevisionDue = triple.third)"
)

with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'w') as f:
    f.write(content)
