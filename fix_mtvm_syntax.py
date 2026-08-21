import re

with open('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("            ) (tests[0].marksScored - tests[1].marksScored) else 0f\n            )", "            )")

with open('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', 'w') as f:
    f.write(content)
