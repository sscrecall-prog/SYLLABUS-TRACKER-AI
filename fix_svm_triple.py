import re

with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'\) \{ base, \(prio, weak, rev\) ->\n\s+base\.copy\(priority = triple\.first, onlyWeak = triple\.second, onlyRevisionDue = triple\.third\)',
    r') { base, triple ->\n        base.copy(priority = triple.first, onlyWeak = triple.second, onlyRevisionDue = triple.third)',
    content
)

with open('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', 'w') as f:
    f.write(content)
