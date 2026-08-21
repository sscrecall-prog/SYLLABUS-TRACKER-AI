import re

with open('app/src/main/java/com/example/ui/screens/GoalsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("goalsViewModel.toggleGoalCompleted(goal)", "goalsViewModel.updateGoal(goal.copy(isCompleted = !goal.isCompleted))")

goal_add_bad = """goalsViewModel.addGoal(
                                title = goalTitle.trim(),
                                targetDateStr = goalTargetDate.trim(),
                                subjectId = selectedSubjectId,
                                subjectName = currentSub?.name ?: "All Subjects",
                                targetChapters = goalTargetChapters,
                                targetHours = goalTargetHours
                            )"""

goal_add_good = """goalsViewModel.addGoal(com.example.data.model.Goal(
                                title = goalTitle.trim(),
                                targetDateStr = goalTargetDate.trim(),
                                subjectId = selectedSubjectId,
                                subjectName = currentSub?.name ?: "All Subjects",
                                targetChaptersCount = goalTargetChapters,
                                targetStudyHours = goalTargetHours
                            ))"""

content = content.replace(goal_add_bad, goal_add_good)

with open('app/src/main/java/com/example/ui/screens/GoalsScreen.kt', 'w') as f:
    f.write(content)
