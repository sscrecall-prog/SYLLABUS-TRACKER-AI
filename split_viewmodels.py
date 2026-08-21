import re
import os

with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'r') as f:
    content = f.read()

# Just print the methods and properties to help us map them.
# We will match lines with `val `, `var `, `fun ` inside the class.
lines = content.split('\n')
inside_class = False
brace_level = 0
members = []

for line in lines:
    if "class SyllabusViewModel" in line:
        inside_class = True
    
    if inside_class:
        if '{' in line:
            brace_level += line.count('{')
        if '}' in line:
            brace_level -= line.count('}')
            
        if brace_level == 1:
            match = re.match(r'^\s*(private )?(val|var|fun)\s+([a-zA-Z0-9_]+)', line)
            if match:
                members.append(match.group(3))

for i, member in enumerate(members):
    print(f"{i}: {member}")
