import re

with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("private val _snackbarMessage = MutableSharedFlow<String>()", "private val _snackbarMessage = MutableStateFlow<String?>(null)")
content = content.replace("val snackbarMessage = _snackbarMessage.asSharedFlow()", "val snackbarMessage = _snackbarMessage.asStateFlow()")
content = content.replace("fun showSnackbar(message: String) {\n        viewModelScope.launch {\n            _snackbarMessage.emit(message)\n        }\n    }", "fun showSnackbar(message: String) {\n        _snackbarMessage.value = message\n    }\n    fun clearSnackbar() {\n        _snackbarMessage.value = null\n    }")

with open('app/src/main/java/com/example/ui/viewmodel/BaseViewModel.kt', 'w') as f:
    f.write(content)
