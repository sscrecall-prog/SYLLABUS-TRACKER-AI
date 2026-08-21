import re

with open('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', 'r') as f:
    content = f.read()

filters = """
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
        if (tests.isEmpty()) {
            MockStats()
        } else {
            val totalCount = tests.size
            val avgScore = tests.map { it.marksScored }.average().toFloat()
            val highestScore = tests.maxOfOrNull { it.marksScored } ?: 0f
            val latestScore = tests.firstOrNull()?.marksScored ?: 0f
            
            val avgPercentile = tests.map { it.percentile }.average().toFloat()
            val bestPercentile = tests.maxOfOrNull { it.percentile } ?: 0f
            
            val avgAccuracy = tests.map { it.accuracy }.average().toFloat()
            
            val clearedCount = tests.count { it.isClearedCutoff }
            val clearanceRate = ((clearedCount.toFloat() / totalCount.toFloat()) * 100).toInt()
            
            val quantTests = tests.filter { it.mathTotal > 0 }
            val avgQuant = if (quantTests.isNotEmpty()) quantTests.map { it.mathScore }.average().toFloat() else 0f
            
            val engTests = tests.filter { it.englishTotal > 0 }
            val avgEng = if (engTests.isNotEmpty()) engTests.map { it.englishScore }.average().toFloat() else 0f
            
            val reasTests = tests.filter { it.reasoningTotal > 0 }
            val avgReas = if (reasTests.isNotEmpty()) reasTests.map { it.reasoningScore }.average().toFloat() else 0f
            
            val gkTests = tests.filter { it.gkTotal > 0 }
            val avgGk = if (gkTests.isNotEmpty()) gkTests.map { it.gkScore }.average().toFloat() else 0f

            MockStats(
                totalTestsAttempted = totalCount,
                averageScore = avgScore,
                highestScore = highestScore,
                latestScore = latestScore,
                averagePercentile = avgPercentile,
                highestPercentile = bestPercentile,
                averageAccuracy = avgAccuracy,
                cutoffsClearedCount = clearedCount,
                cutoffsClearanceRate = clearanceRate,
                averageMathScore = avgQuant,
                averageEnglishScore = avgEng,
                averageReasoningScore = avgReas,
                averageGkScore = avgGk,
                recentTrend = if (tests.size >= 2) (tests[0].marksScored - tests[1].marksScored) else 0f
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, MockStats())

    fun selectMockTest(test: MockTest?) {
        selectedMockTest.value = test
    }

    fun setMockSearchQuery(query: String) { mockSearchQuery.value = query }
    fun setMockPlatformFilter(platform: String?) { mockPlatformFilter.value = platform }
    fun setMockTypeFilter(type: MockTestType?) { mockTypeFilter.value = type }

"""

# replace selectedMockTest stuff that might already be there
duplicate = """
    private val _selectedMockTestId = MutableStateFlow<Long?>(null)
    val selectedMockTestId = _selectedMockTestId.asStateFlow()
"""
content = content.replace(duplicate, "")
content = content.replace("class MockTestsViewModel(application: Application) : BaseViewModel(application) {", "class MockTestsViewModel(application: Application) : BaseViewModel(application) {\n" + filters)

with open('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', 'w') as f:
    f.write(content)
