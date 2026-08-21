import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('private val viewModel: SyllabusViewModel by viewModels()', 'private val settingsViewModel: SettingsViewModel by viewModels()')
content = content.replace('viewModel.appSettings', 'settingsViewModel.appSettings')
content = content.replace('import com.example.ui.viewmodel.SyllabusViewModel', 'import com.example.ui.viewmodel.SettingsViewModel')
content = content.replace('MainScreen(viewModel = viewModel)', 'MainScreen()')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
