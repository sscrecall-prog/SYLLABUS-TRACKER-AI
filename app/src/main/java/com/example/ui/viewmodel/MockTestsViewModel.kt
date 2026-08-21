package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MockTestsViewModel(application: Application) : BaseViewModel(application) {

    val mockSearchQuery = MutableStateFlow("")
    val mockPlatformFilter = MutableStateFlow<String?>(null)
    val mockTypeFilter = MutableStateFlow<MockTestType?>(null)
    val selectedMockTest = MutableStateFlow<MockTest?>(null)

    val filteredMockTests: StateFlow<List<MockTest>> = combine(
        mockTests,
        mockSearchQuery,
        mockPlatformFilter,
        mockTypeFilter
    ) { tests, query, platform, type ->
        tests.filter { test ->
            val matchesQuery = query.isBlank() || 
                test.testName.contains(query, ignoreCase = true) ||
                test.testPlatform.contains(query, ignoreCase = true) ||
                test.weakAreasIdentified.contains(query, ignoreCase = true)
            
            val matchesPlatform = platform == null || platform == "All" || test.testPlatform.equals(platform, ignoreCase = true)
            val matchesType = type == null || test.testType == type
            matchesQuery && matchesPlatform && matchesType
        }.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mockStats: StateFlow<MockStats> = mockTests.map { tests ->
        analyticsRepository.calculateMockStats(tests)
    }.stateIn(viewModelScope, SharingStarted.Lazily, MockStats())

    val uiState: StateFlow<MockTestsUiState> = combine(
        mockTests,
        filteredMockTests,
        mockStats,
        selectedMockTest,
        combine(mockSearchQuery, mockPlatformFilter, mockTypeFilter) { q, p, t ->
            Triple(q, p, t)
        }
    ) { allTests, filtered, stats, selected, filterTuple ->
        val (query, platform, type) = filterTuple
        MockTestsUiState(
            allMockTests = allTests,
            filteredMockTests = filtered,
            mockStats = stats,
            searchQuery = query,
            platformFilter = platform,
            typeFilter = type,
            selectedMockTest = selected
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, MockTestsUiState())

    fun selectMockTest(test: MockTest?) {
        selectedMockTest.value = test
    }

    fun setMockSearchQuery(query: String) { mockSearchQuery.value = query }
    fun setMockPlatformFilter(platform: String?) { mockPlatformFilter.value = platform }
    fun setMockTypeFilter(type: MockTestType?) { mockTypeFilter.value = type }



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

    
}
