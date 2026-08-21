import re

with open('app/src/main/java/com/example/ui/viewmodel/ProfileViewModel.kt', 'r') as f:
    content = f.read()

# Extract newlyUnlockedBadge
match = re.search(r'\s*val newlyUnlockedBadge = MutableStateFlow<AchievementBadge\?>\(null\)\n', content)
if match:
    badge_prop = match.group(0)
    content = content.replace(badge_prop, '')
    
    with open('app/src/main/java/com/example/ui/viewmodel/ProfileViewModel.kt', 'w') as f:
        f.write(content)
        
    with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'r') as f:
        base = f.read()
    
    base = base.replace('}', badge_prop + '\n}')
    
    with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'w') as f:
        f.write(base)
