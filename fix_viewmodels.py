import re

for filepath in ['app/src/main/java/com/example/ui/viewmodel/GoalsViewModel.kt',
                 'app/src/main/java/com/example/ui/viewmodel/MistakeNotebookViewModel.kt',
                 'app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt']:
    with open(filepath, 'r') as f:
        content = f.read()
    if '}' not in content[-10:]:
        with open(filepath, 'w') as f:
            f.write(content + '\n}\n')

