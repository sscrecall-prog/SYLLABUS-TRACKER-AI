import re

# Read MainViewModel and remove selectedChapter
with open('app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt', 'r') as f:
    content = f.read()

# Extract selectedChapter block
match = re.search(r'\s*private val _selectedChapter = MutableStateFlow.*?fun selectChapter\(chapter: SyllabusItem\?\) \{\s*_selectedChapter\.value = chapter\s*\}', content, flags=re.DOTALL)
if match:
    chapter_block = match.group(0)
    content = content.replace(chapter_block, '')
    
    with open('app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt', 'w') as f:
        f.write(content)
        
    # Inject into SyllabusViewModel
    with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'r') as f:
        s_content = f.read()
        
    s_content = s_content.replace('class SyllabusViewModel(application: Application) : BaseViewModel(application) {', 'class SyllabusViewModel(application: Application) : BaseViewModel(application) {' + chapter_block)
    
    with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'w') as f:
        f.write(s_content)

