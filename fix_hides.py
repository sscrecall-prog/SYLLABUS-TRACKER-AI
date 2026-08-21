import glob
import re

hidden = ['goals', 'mistakes', 'mockTests', 'allBadges', 'unlockedBadges', 'appSettings', 'subjects', 'items', 'studySessions', 'snackbarMessage', 'showSnackbar', 'clearSnackbar']

for filepath in glob.glob('app/src/main/java/com/example/ui/viewmodel/*.kt'):
    if 'BaseViewModel' in filepath: continue
    
    with open(filepath, 'r') as f:
        content = f.read()
    
    for h in hidden:
        # e.g. val items = repository.allItems.stateIn...
        content = re.sub(rf'\s*val {h} =.*?\n', '\n', content)
        # for functions
        if h == 'showSnackbar':
            content = re.sub(r'\s*fun showSnackbar\(.*?\)\s*\{.*?\}\n', '\n', content, flags=re.DOTALL)
        if h == 'clearSnackbar':
            content = re.sub(r'\s*fun clearSnackbar\(\)\s*\{.*?\}\n', '\n', content, flags=re.DOTALL)
            
    with open(filepath, 'w') as f:
        f.write(content)

