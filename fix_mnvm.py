import re

with open('app/src/main/java/com/example/ui/viewmodel/MistakeNotebookViewModel.kt', 'r') as f:
    content = f.read()

filters_replacement = """
    data class MistakeFilters(
        val query: String = "",
        val subjectId: Long? = null,
        val category: MistakeCategory? = null,
        val status: MistakeResolutionStatus? = null,
        val onlyStarred: Boolean = false,
        val onlyReviewDue: Boolean = false
    )

    private val _filters = MutableStateFlow(MistakeFilters())
    val mistakeSearchQuery = _filters.map { it.query }.stateIn(viewModelScope, SharingStarted.Lazily, "")
    val mistakeFilterSubjectId = _filters.map { it.subjectId }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val mistakeFilterCategory = _filters.map { it.category }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val mistakeFilterStatus = _filters.map { it.status }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val mistakeFilterOnlyStarred = _filters.map { it.onlyStarred }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val mistakeFilterOnlyReviewDue = _filters.map { it.onlyReviewDue }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setMistakeSearchQuery(query: String) { _filters.value = _filters.value.copy(query = query) }
    fun setMistakeFilterSubjectId(subId: Long?) { _filters.value = _filters.value.copy(subjectId = subId) }
    fun setMistakeFilterCategory(cat: MistakeCategory?) { _filters.value = _filters.value.copy(category = cat) }
    fun setMistakeFilterStatus(status: MistakeResolutionStatus?) { _filters.value = _filters.value.copy(status = status) }
    fun toggleMistakeFilterStarred() { _filters.value = _filters.value.copy(onlyStarred = !_filters.value.onlyStarred) }
    fun toggleMistakeFilterReviewDue() { _filters.value = _filters.value.copy(onlyReviewDue = !_filters.value.onlyReviewDue) }

    val filteredMistakes: StateFlow<List<MistakeEntry>> = combine(
        mistakes,
        _filters
    ) { allMistakes, f ->
        allMistakes.filter { mistake ->
            val matchQuery = f.query.isBlank() || 
                mistake.questionText.contains(f.query, ignoreCase = true) ||
                mistake.yourWrongAnswer.contains(f.query, ignoreCase = true) ||
                mistake.explanationOrKeyConcept.contains(f.query, ignoreCase = true)
            
            val matchSub = f.subjectId == null || mistake.subjectId == f.subjectId
            val matchCat = f.category == null || mistake.category == f.category
            val matchStatus = f.status == null || mistake.resolutionStatus == f.status
            val matchStarred = !f.onlyStarred || mistake.importanceStar
            val matchReviewDue = !f.onlyReviewDue || mistake.nextReviewTimestamp <= System.currentTimeMillis()

            matchQuery && matchSub && matchCat && matchStatus && matchStarred && matchReviewDue
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
"""

# replace everything from val mistakeSearchQuery up to stateIn(..., emptyList())
pattern = re.compile(r'    val mistakeSearchQuery = MutableStateFlow\(""\).*?\.stateIn\(viewModelScope, SharingStarted\.Lazily, emptyList\(\)\)', re.DOTALL)
content = pattern.sub(filters_replacement.strip(), content)

with open('app/src/main/java/com/example/ui/viewmodel/MistakeNotebookViewModel.kt', 'w') as f:
    f.write(content)
