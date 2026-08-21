import re

with open('app/src/main/java/com/example/data/repository/SyllabusRepository.kt', 'r') as f:
    text = f.read()

# Just print the whole file with line numbers to see it
for i, line in enumerate(text.splitlines()):
    print(f"{i}: {line}")
