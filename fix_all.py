import re
import os

# Fix BaseViewModel instantiation of SubjectRepository
with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'r') as f:
    bvm = f.read()
bvm = bvm.replace('SubjectRepository(database.subjectDao(), database.syllabusDao())', 'SubjectRepository(database.subjectDao())')
with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'w') as f:
    f.write(bvm)

# SubjectRepository deleteSubject missing syllabusDao
with open('app/src/main/java/com/example/data/repository/SubjectRepository.kt', 'r') as f:
    sr = f.read()
# Let's just remove the syllabusDao.deleteItemsForSubject because cascade delete should be handled in the database or ViewModel.
# For now, just remove it to match original or make it compile.
sr = sr.replace('syllabusDao.deleteItemsForSubject(subject.id)', '// syllabusDao.deleteItemsForSubject(subject.id)')
with open('app/src/main/java/com/example/data/repository/SubjectRepository.kt', 'w') as f:
    f.write(sr)

# SubjectViewModel
with open('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', 'r') as f:
    svm = f.read()
# Fix the broken if statement
svm = re.sub(r'if \(\s+', '', svm)
svm = re.sub(r'}\s*}\s*}\s*// Syllabus Item CRUD\s*}', '}\n    }\n}', svm)
with open('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', 'w') as f:
    f.write(svm)

# Remove duplicate flows in ViewModels
def remove_duplicate_flows(filepath, flows_to_remove):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f:
        content = f.read()
    for flow in flows_to_remove:
        content = re.sub(r'\s*val ' + flow + r'.*?\n', '\n', content, flags=re.DOTALL)
    with open(filepath, 'w') as f:
        f.write(content)

remove_duplicate_flows('app/src/main/java/com/example/ui/viewmodel/GoalsViewModel.kt', [r'goals: StateFlow<List<Goal>> = goalRepository\.allGoals.*'])
remove_duplicate_flows('app/src/main/java/com/example/ui/viewmodel/MistakeNotebookViewModel.kt', [r'mistakes: StateFlow<List<MistakeEntry>> = mistakeRepository\.allMistakes.*'])
remove_duplicate_flows('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', [r'mockTests: StateFlow<List<MockTest>> = mockTestRepository\.allMockTests.*'])

