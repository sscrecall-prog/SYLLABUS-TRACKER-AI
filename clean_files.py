import re

def clean_file(filepath, to_remove):
    with open(filepath, 'r') as f:
        content = f.read()
    
    for r in to_remove:
        content = re.sub(r, '', content, flags=re.DOTALL)
        
    with open(filepath, 'w') as f:
        f.write(content)

# ProfileViewModel: remove all the syntax error bits. It's safe to just rewrite it.
with open('app/src/main/java/com/example/ui/viewmodel/ProfileViewModel.kt', 'w') as f:
    f.write("""package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.SyllabusRepository
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileViewModel(application: Application) : BaseViewModel(application) {
}
""")

clean_file('app/src/main/java/com/example/ui/viewmodel/SettingsViewModel.kt', [
    r'\s*val appSettings.*?\n'
])

clean_file('app/src/main/java/com/example/ui/viewmodel/SubjectViewModel.kt', [
    r'\s*val subjects.*?\n',
    r'\s*_selectedSubjectId\.value.*?\n'
])

clean_file('app/src/main/java/com/example/ui/viewmodel/SyllabusViewModel.kt', [
    r'\s*val items.*?\n',
    r'\s*val studySessions.*?\n'
])

