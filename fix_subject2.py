with open('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('repository.insertSubject(sub)', 'viewModelScope.launch { repository.insertSubject(sub) }')

with open('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', 'w') as f:
    f.write(content)
