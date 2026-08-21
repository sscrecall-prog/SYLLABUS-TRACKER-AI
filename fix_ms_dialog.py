import re

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'\s*// Milestone Unlock Celebration Dialog.*?newlyUnlockedBadge\?\.let \{ badge ->.*?MilestoneUnlockDialog\(.*?\)\s*\}\s*\}', re.DOTALL)
content = pattern.sub('\n        }', content)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
