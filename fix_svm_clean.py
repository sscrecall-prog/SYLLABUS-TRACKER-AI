with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'r') as f:
    content = f.read()

# I will carefully remove the bad block I added
bad_block = """
    val filterStatus = MutableStateFlow<ChapterStatus?>(null)
    val filterPriority = MutableStateFlow<Priority?>(null)
    val filterDifficulty = MutableStateFlow<Difficulty?>(null)
    val filterOnlyWeak = MutableStateFlow(false)
    val filterOnlyRevisionDue = MutableStateFlow(false)

"""
content = content.replace(bad_block, "", 1)

with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'w') as f:
    f.write(content)
