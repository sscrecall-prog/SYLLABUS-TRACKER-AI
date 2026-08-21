with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('AppDatabase.getDatabase(application)', 'AppDatabase.getDatabase(application, viewModelScope)')

with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'w') as f:
    f.write(content)
