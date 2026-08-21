import re

with open('app/src/main/java/com/example/ui/viewmodel/ProfileViewModel.kt', 'r') as f:
    content = f.read()

# Extract evaluateAchievements from ProfileViewModel
match = re.search(r'^\s*private suspend fun evaluateAchievements\(.*?\n\s*\}\n', content, flags=re.MULTILINE | re.DOTALL)
if match:
    eval_block = match.group(0)
    content = content.replace(eval_block, '')
    
    with open('app/src/main/java/com/example/ui/viewmodel/ProfileViewModel.kt', 'w') as f:
        f.write(content)
        
    with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'r') as f:
        base = f.read()
    
    # insert before the closing brace
    base = base.replace('}', eval_block + '\n}')
    
    with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'w') as f:
        f.write(base)

