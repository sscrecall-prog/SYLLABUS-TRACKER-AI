import re

with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'r') as f:
    lines = f.readlines()

class_start = -1
for i, line in enumerate(lines):
    if line.startswith("class SyllabusViewModel"):
        class_start = i
        break

prefix = lines[:class_start+1]
body = lines[class_start+1:]

# parse members
members = []
current_member = []
brace_level = 1
member_name = None

for line in body:
    if brace_level == 1:
        match = re.match(r'^\s*(?:private\s+)?(?:suspend\s+)?(?:val|var|fun)\s+([a-zA-Z0-9_]+)', line)
        if match:
            if current_member:
                members.append((member_name, "".join(current_member)))
            current_member = [line]
            member_name = match.group(1)
        elif line.strip() == "init {":
            if current_member:
                members.append((member_name, "".join(current_member)))
            current_member = [line]
            member_name = "init"
        elif current_member:
            current_member.append(line)
    else:
        if current_member:
            current_member.append(line)
            
    brace_level += line.count('{')
    brace_level -= line.count('}')
    
    if brace_level == 0:
        if current_member:
            members.append((member_name, "".join(current_member)))
        break

# Now we have all members! Let's print them out to verify
for name, code in members:
    print(f"Name: {name}, Length: {len(code.splitlines())}")

