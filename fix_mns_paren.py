with open('app/src/main/java/com/example/ui/screens/MistakeNotebookScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("tagsCsv = tags\n                    )\n                }\n                showAddEditDialog = false", "tagsCsv = tags\n                    ))\n                }\n                showAddEditDialog = false")

with open('app/src/main/java/com/example/ui/screens/MistakeNotebookScreen.kt', 'w') as f:
    f.write(content)
