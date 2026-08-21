with open('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', 'r') as f:
    content = f.read()

func = """
    fun addSubject(subject: Subject) {
        viewModelScope.launch {
            val maxOrder = subjects.value.maxOfOrNull { it.orderIndex } ?: 0
            val newId = repository.insertSubject(subject.copy(orderIndex = maxOrder + 1))
            showSnackbar("Subject added")
            // Can't openSubjectDetail easily because it's in MainViewModel, but we can just skip it or leave it
            // Or we just do nothing and let the user navigate manually or emit an event
        }
    }
"""

content = content.replace('addSubject(sub)', 'repository.insertSubject(sub)')

with open('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', 'w') as f:
    f.write(content)
