import re

with open('app/src/main/java/com/example/ui/viewmodel/MistakeNotebookViewModel.kt', 'r') as f:
    content = f.read()

filters = """
    val mistakeSearchQuery = MutableStateFlow("")
    val mistakeFilterSubjectId = MutableStateFlow<Long?>(null)
    val mistakeFilterCategory = MutableStateFlow<MistakeCategory?>(null)
    val mistakeFilterStatus = MutableStateFlow<MistakeResolutionStatus?>(null)
    val mistakeFilterOnlyStarred = MutableStateFlow(false)
    val mistakeFilterOnlyReviewDue = MutableStateFlow(false)

    fun setMistakeSearchQuery(query: String) { mistakeSearchQuery.value = query }
    fun setMistakeFilterSubject(subId: Long?) { mistakeFilterSubjectId.value = subId }
    fun setMistakeFilterCategory(cat: MistakeCategory?) { mistakeFilterCategory.value = cat }
    fun setMistakeFilterStatus(status: MistakeResolutionStatus?) { mistakeFilterStatus.value = status }
    fun toggleMistakeFilterStarred() { mistakeFilterOnlyStarred.value = !mistakeFilterOnlyStarred.value }
    fun toggleMistakeFilterReviewDue() { mistakeFilterOnlyReviewDue.value = !mistakeFilterOnlyReviewDue.value }

    val filteredMistakes: StateFlow<List<MistakeEntry>> = combine(
        mistakes,
        mistakeSearchQuery,
        mistakeFilterSubjectId,
        mistakeFilterCategory,
        mistakeFilterStatus,
        mistakeFilterOnlyStarred,
        mistakeFilterOnlyReviewDue
    ) { allMistakes, query, subId, cat, status, starred, reviewDue ->
        allMistakes.filter { mistake ->
            val matchQuery = query.isBlank() || 
                mistake.questionText.contains(query, ignoreCase = true) ||
                mistake.yourWrongAnswer.contains(query, ignoreCase = true) ||
                mistake.explanationOrKeyConcept.contains(query, ignoreCase = true)
            
            val matchSub = subId == null || mistake.subjectId == subId
            val matchCat = cat == null || mistake.category == cat
            val matchStatus = status == null || mistake.resolutionStatus == status
            val matchStarred = !starred || mistake.importanceStar
            val matchReviewDue = !reviewDue || mistake.nextReviewTimestamp <= System.currentTimeMillis()

            matchQuery && matchSub && matchCat && matchStatus && matchStarred && matchReviewDue
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mistakeStats: StateFlow<MistakeStats> = mistakes.map { list ->
        val total = list.size
        val active = list.count { it.resolutionStatus == MistakeResolutionStatus.ACTIVE }
        val understood = list.count { it.resolutionStatus == MistakeResolutionStatus.UNDERSTOOD }
        val mastered = list.count { it.resolutionStatus == MistakeResolutionStatus.MASTERED }
        val reviewDue = list.count { it.nextReviewTimestamp <= System.currentTimeMillis() }
        val starred = list.count { it.importanceStar }
        
        val resRate = if (total > 0) (((understood + mastered).toFloat() / total) * 100).toInt() else 0
        
        val silly = list.count { it.category == MistakeCategory.SILLY_MISTAKE }
        val concept = list.count { it.category == MistakeCategory.CONCEPT_GAP }
        val formula = list.count { it.category == MistakeCategory.FORMULA_FORGOT }
        val calc = list.count { it.category == MistakeCategory.CALCULATION_ERROR }
        val panic = list.count { it.category == MistakeCategory.TIME_PANIC }
        
        val sillyPct = if (total > 0) ((silly.toFloat() / total) * 100).toInt() else 0
        val conceptPct = if (total > 0) ((concept.toFloat() / total) * 100).toInt() else 0
        val formulaPct = if (total > 0) ((formula.toFloat() / total) * 100).toInt() else 0
        val calcPct = if (total > 0) ((calc.toFloat() / total) * 100).toInt() else 0
        val panicPct = if (total > 0) ((panic.toFloat() / total) * 100).toInt() else 0
        
        val mostVulnerable = if (list.isNotEmpty()) {
            list.groupBy { it.subjectName }
                .maxByOrNull { it.value.size }?.key ?: "N/A"
        } else "N/A"
        
        MistakeStats(
            totalMistakesCount = total,
            activeMistakesCount = active,
            understoodCount = understood,
            masteredCount = mastered,
            reviewDueCount = reviewDue,
            starredCount = starred,
            resolutionRatePercent = resRate,
            sillyMistakesPercent = sillyPct,
            conceptGapPercent = conceptPct,
            formulaForgotPercent = formulaPct,
            calculationErrorPercent = calcPct,
            timePanicPercent = panicPct,
            mostVulnerableSubject = mostVulnerable
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, MistakeStats())
"""

content = content.replace("class MistakeNotebookViewModel(application: Application) : BaseViewModel(application) {", "class MistakeNotebookViewModel(application: Application) : BaseViewModel(application) {\n" + filters)

with open('app/src/main/java/com/example/ui/viewmodel/MistakeNotebookViewModel.kt', 'w') as f:
    f.write(content)
