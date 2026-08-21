import re
import os

base_properties = """
    val subjects = repository.allSubjects.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val items = repository.allItems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val studySessions = repository.allStudySessions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val goals = repository.allGoals.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val appSettings = repository.appSettings.stateIn(viewModelScope, SharingStarted.Lazily, AppSettings())
    val allBadges = repository.allBadges.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val unlockedBadges = allBadges.map { list -> list.filter { it.isUnlocked } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val mockTests = repository.allMockTests.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val mistakes = repository.allMistakes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val snackbarMessage = MutableStateFlow<String?>(null)
    fun showSnackbar(message: String) {
        snackbarMessage.value = message
    }
    fun clearSnackbar() {
        snackbarMessage.value = null
    }
"""

with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'r') as f:
    content = f.read()

# insert before the closing brace
content = content.replace('}', base_properties + '\n}')

with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'w') as f:
    f.write(content)

