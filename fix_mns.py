import re

with open('app/src/main/java/com/example/ui/screens/MistakeNotebookScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("mistakeNotebookViewModel.addMistake(", "mistakeNotebookViewModel.addMistake(MistakeEntry(")
content = content.replace("tagsCsv = tags\n                )", "tagsCsv = tags\n                ))")
content = content.replace("setMistakeFilterSubject", "setMistakeFilterSubjectId")

with open('app/src/main/java/com/example/ui/screens/MistakeNotebookScreen.kt', 'w') as f:
    f.write(content)
