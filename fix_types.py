import re

with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('val subjects = repository.allSubjects.stateIn', 'val subjects: StateFlow<List<Subject>> = repository.allSubjects.stateIn')
content = content.replace('val items = repository.allItems.stateIn', 'val items: StateFlow<List<SyllabusItem>> = repository.allItems.stateIn')
content = content.replace('val studySessions = repository.allStudySessions.stateIn', 'val studySessions: StateFlow<List<StudySession>> = repository.allStudySessions.stateIn')
content = content.replace('val goals = repository.allGoals.stateIn', 'val goals: StateFlow<List<Goal>> = repository.allGoals.stateIn')
content = content.replace('val appSettings = repository.appSettings.stateIn', 'val appSettings: StateFlow<AppSettings> = repository.appSettings.stateIn')
content = content.replace('val allBadges = repository.allBadges.stateIn', 'val allBadges: StateFlow<List<AchievementBadge>> = repository.allBadges.stateIn')
content = content.replace('val unlockedBadges = allBadges.map { list -> list.filter { it.isUnlocked } }.stateIn', 'val unlockedBadges: StateFlow<List<AchievementBadge>> = allBadges.map { list -> list.filter { it.isUnlocked } }.stateIn')
content = content.replace('val mockTests = repository.allMockTests.stateIn', 'val mockTests: StateFlow<List<MockTest>> = repository.allMockTests.stateIn')
content = content.replace('val mistakes = repository.allMistakes.stateIn', 'val mistakes: StateFlow<List<MistakeEntry>> = repository.allMistakes.stateIn')

with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'w') as f:
    f.write(content)
