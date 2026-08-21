import re

with open('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', 'r') as f:
    content = f.read()

overload = """
    fun addSubject(subject: Subject) {
        viewModelScope.launch {
            val maxOrder = subjects.value.maxOfOrNull { it.orderIndex } ?: 0
            subjectRepository.insertSubject(subject.copy(orderIndex = maxOrder + 1))
            showSnackbar("Subject added")
        }
    }
"""
content = content.replace("    fun updateSubject(subject: Subject) {", overload + "\n    fun updateSubject(subject: Subject) {")

with open('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', 'w') as f:
    f.write(content)
