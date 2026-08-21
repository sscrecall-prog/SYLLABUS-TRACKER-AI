import re

with open('/tmp/SyllabusViewModel_backup.kt', 'r') as f:
    content = f.read()

# We need a clean MockTestsViewModel
# It should only have the Mock Test CRUD and state.

mock_vm = """package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MockTestsViewModel(application: Application) : BaseViewModel(application) {

    // Mock Test State
    private val _selectedMockTestId = MutableStateFlow<Long?>(null)
    val selectedMockTestId = _selectedMockTestId.asStateFlow()

    // Add/Edit Mock Test
    fun addMockTest(mockTest: MockTest) {
        viewModelScope.launch {
            mockTestRepository.insertMockTest(mockTest)
            showSnackbar("Mock Test added")
        }
    }

    fun updateMockTest(mockTest: MockTest) {
        viewModelScope.launch {
            mockTestRepository.updateMockTest(mockTest)
            showSnackbar("Mock Test updated")
        }
    }

    fun deleteMockTest(mockTest: MockTest) {
        viewModelScope.launch {
            mockTestRepository.deleteMockTest(mockTest)
            showSnackbar("Mock Test deleted")
        }
    }

    fun openMockTestDetail(id: Long) {
        _selectedMockTestId.value = id
    }

    fun closeMockTestDetail() {
        _selectedMockTestId.value = null
    }
}
"""

with open('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', 'w') as f:
    f.write(mock_vm)
    
