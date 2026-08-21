with open('app/src/main/java/com/example/data/repository/SyllabusRepository.kt', 'r') as f:
    content = f.read()

import re
methods = re.findall(r'^\s*(?:val|suspend fun|fun) ([a-zA-Z0-9_]+).*?(?:=|{)', content, re.MULTILINE)
for m in methods:
    print(m)
