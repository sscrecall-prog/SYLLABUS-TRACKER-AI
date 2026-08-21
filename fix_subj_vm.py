import re

with open('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('if (\n            }\n        }\n    }\n    // Syllabus Item CRUD', '}\n    }\n')

with open('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', 'w') as f:
    f.write(content)

