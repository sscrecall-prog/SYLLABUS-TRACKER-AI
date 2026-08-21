import re

with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("val syllabusViewModel: MainViewModel = viewModel()", "val mainViewModel: MainViewModel = viewModel()")

content = content.replace("syllabusViewModel.selectedSubjectId", "mainViewModel.selectedSubjectId")
# Check if there are other uses of mainViewModel that were using syllabusViewModel incorrectly

with open('app/src/main/java/com/example/ui/screens/SyllabusScreen.kt', 'w') as f:
    f.write(content)
