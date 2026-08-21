import re

with open('app/src/main/java/com/example/ui/screens/MockTestsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("mistakeNotebookViewModel.addMistake(", "mistakeNotebookViewModel.addMistake(MistakeEntry(")
content = content.replace("category = cat\n                )", "category = cat\n                ))")

with open('app/src/main/java/com/example/ui/screens/MockTestsScreen.kt', 'w') as f:
    f.write(content)
