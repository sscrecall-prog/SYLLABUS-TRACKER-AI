with open('/tmp/SyllabusViewModel_backup.kt', 'r') as f:
    content = f.read()

import re
match = re.search(r'^\s*private fun autoTagWeakSyllabusChapters\(.*?\n\s*\}\n\s*\}\n', content, flags=re.MULTILINE | re.DOTALL)
if match:
    func = match.group(0)
    with open('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', 'r') as f:
        m_content = f.read()
    
    m_content = m_content.replace('}', func + '\n}')
    
    with open('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', 'w') as f:
        f.write(m_content)
