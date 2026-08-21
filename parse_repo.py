import re

with open('app/src/main/java/com/example/data/repository/SyllabusRepository.kt', 'r') as f:
    lines = f.readlines()

members = []
current_member = []
brace_level = 1
member_name = None
inside_class = False

for line in lines:
    if line.startswith("class SyllabusRepository"):
        inside_class = True
        continue
    
    if not inside_class:
        continue
        
    if brace_level == 1:
        match = re.match(r'^\s*(?:val|suspend\s+fun|fun)\s+([a-zA-Z0-9_]+)', line)
        if match:
            if current_member:
                members.append((member_name, "".join(current_member)))
            current_member = [line]
            member_name = match.group(1)
        elif current_member:
            current_member.append(line)
    else:
        if current_member:
            current_member.append(line)
            
    brace_level += line.count('{')
    brace_level -= line.count('}')
    
    if brace_level == 0 and inside_class:
        if current_member:
            members.append((member_name, "".join(current_member)))
        break

for name, body in members:
    print(f"--- {name} ---")

with open('parsed_repo.json', 'w') as f:
    import json
    json.dump({name: body for name, body in members}, f, indent=2)

