import re

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'val newlyUnlockedBadge by profileViewModel\.newlyUnlockedBadge\.collectAsState\(\)', '', content)
content = re.sub(r'// Milestone Unlock Celebration Dialog.*?\}\n            \}\n        \}\n    \}', '        }\n    }', content, flags=re.DOTALL)

# Also fix the GoalsViewModel.addGoal arguments
goal_replacement = """onAddGoal = { title, dateStr, subId, sName, chapters, hours ->
                        goalsViewModel.addGoal(com.example.data.model.Goal(title = title, targetDateStr = dateStr, subjectId = subId, subjectName = sName, targetChaptersCount = chapters, targetStudyHours = hours))
                    }"""

content = re.sub(r'onAddGoal = \{ title, dateStr, subId, sName, chapters, hours ->\s*goalsViewModel\.addGoal\(title, dateStr, subId, sName, chapters, hours\)\s*\}', goal_replacement, content)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
