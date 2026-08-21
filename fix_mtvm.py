import re

with open('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("val gkTests = tests.filter { it.gkTotal > 0 }", "val gsTests = tests.filter { it.gsTotal > 0 }")
content = content.replace("val avgGk = if (gkTests.isNotEmpty()) gkTests.map { it.gkScore }.average().toFloat() else 0f", "val avgGs = if (gsTests.isNotEmpty()) gsTests.map { it.gsScore }.average().toFloat() else 0f")

replacement = """
            MockStats(
                totalMocksCount = totalCount,
                averageScore = avgScore,
                highestScore = highestScore,
                latestScore = latestScore,
                averagePercentile = avgPercentile,
                bestPercentile = bestPercentile,
                averageAccuracy = avgAccuracy,
                clearedCutoffCount = clearedCount,
                cutoffClearanceRate = clearanceRate,
                averageQuantScore = avgQuant,
                averageEnglishScore = avgEng,
                averageReasoningScore = avgReas,
                averageGsScore = avgGs,
                scoreProgression = tests.take(5).reversed().map { it.testDateStr to it.marksScored }
            )
"""
content = re.sub(r'MockStats\(\s*totalTestsAttempted.*?recentTrend = .*?\)', replacement.strip(), content, flags=re.DOTALL)

content = content.replace("fun openMockTestDetail(id: Long) {\n        _selectedMockTestId.value = id\n    }\n\n    fun closeMockTestDetail() {\n        _selectedMockTestId.value = null\n    }", "")

with open('app/src/main/java/com/example/ui/viewmodel/MockTestsViewModel.kt', 'w') as f:
    f.write(content)
