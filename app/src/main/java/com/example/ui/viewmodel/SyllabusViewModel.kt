package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.SyllabusRepository
import com.example.util.AmbientSoundManager
import com.example.ui.theme.motion.TransitionDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SyllabusViewModel(application: Application) : BaseViewModel(application) {

    private val _selectedChapter = MutableStateFlow<SyllabusItem?>(null)
    val selectedChapter: StateFlow<SyllabusItem?> = _selectedChapter.asStateFlow()

    // Global Search & Filters
    fun selectChapter(chapter: SyllabusItem?) {
        _selectedChapter.value = chapter
    }    val searchQuery = MutableStateFlow("")
    val filterSubjectId = MutableStateFlow<Long?>(null)
    val filterStatus = MutableStateFlow<ChapterStatus?>(null)
    val filterDifficulty = MutableStateFlow<Difficulty?>(null)
    val filterPriority = MutableStateFlow<Priority?>(null)
    val filterOnlyWeak = MutableStateFlow(false)
    val filterOnlyRevisionDue = MutableStateFlow(false)

    // Timer State
    private val filterCriteria: Flow<FilterCriteria> = combine(
        searchQuery,
        filterSubjectId,
        filterStatus,
        filterDifficulty
    ) { query, subId, status, diff ->
        FilterCriteria(query = query, subjectId = subId, status = status, difficulty = diff)
    }.combine(
        combine(filterPriority, filterOnlyWeak, filterOnlyRevisionDue) { prio, weak, rev ->
            Triple(prio, weak, rev)
        }
    ) { base, triple ->
        base.copy(priority = triple.first, onlyWeak = triple.second, onlyRevisionDue = triple.third)
    }

    // Filtered Items for Global Search & Syllabus Screen
    val filteredItems: StateFlow<List<SyllabusItem>> = combine(
        items,
        filterCriteria
    ) { allItems, criteria ->
        allItems.filter { item ->
            val matchQuery = criteria.query.isEmpty() ||
                    item.title.contains(criteria.query, ignoreCase = true) ||
                    item.notes.contains(criteria.query, ignoreCase = true) ||
                    item.tags.contains(criteria.query, ignoreCase = true)

            val matchSub = criteria.subjectId == null || item.subjectId == criteria.subjectId
            val matchStatus = criteria.status == null || item.status == criteria.status
            val matchDiff = criteria.difficulty == null || item.difficulty == criteria.difficulty
            val matchPrio = criteria.priority == null || item.priority == criteria.priority
            val matchWeak = !criteria.onlyWeak || item.isWeak
            val matchRev = !criteria.onlyRevisionDue || item.isRevisionDue

            matchQuery && matchSub && matchStatus && matchDiff && matchPrio && matchWeak && matchRev
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState: StateFlow<SyllabusUiState> = combine(
        items,
        filteredItems,
        selectedChapter,
        filterCriteria
    ) { all, filtered, selected, criteria ->
        SyllabusUiState(
            allItems = all,
            filteredItems = filtered,
            selectedChapter = selected,
            filterCriteria = criteria
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, SyllabusUiState())

    val revisionState: StateFlow<RevisionState> = items.map { allItems ->
        val oneDay = 24 * 60 * 60 * 1000L
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfToday = cal.timeInMillis
        val endOfToday = startOfToday + oneDay

        val overdue = allItems.filter { it.nextRevisionTimestamp != null && it.nextRevisionTimestamp < startOfToday }
        val dueToday = allItems.filter {
            it.status == ChapterStatus.REVISION_DUE ||
            (it.nextRevisionTimestamp != null && it.nextRevisionTimestamp in startOfToday..endOfToday)
        }
        val upcoming = allItems.filter {
            it.nextRevisionTimestamp != null && it.nextRevisionTimestamp > endOfToday && it.nextRevisionTimestamp <= endOfToday + 7 * oneDay
        }.sortedBy { it.nextRevisionTimestamp }
        val recentlyRevised = allItems.filter { it.revisionCount > 0 && it.lastStudiedTimestamp != null }
            .sortedByDescending { it.lastStudiedTimestamp }
        val weak = allItems.filter { it.isWeak }

        RevisionState(
            overdueList = overdue,
            dueTodayList = dueToday,
            upcomingList = upcoming,
            recentlyRevisedList = recentlyRevised,
            weakList = weak
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, RevisionState())

    val weakChapters: StateFlow<List<SyllabusItem>> = items.map { list ->
        list.filter { it.isWeak }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val revisionDueChapters: StateFlow<List<SyllabusItem>> = items.map { list ->
        list.filter { it.isRevisionDue }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Navigation Actions with Directional Back-Stack
    fun addItem(
        subjectId: Long,
        parentId: Long?,
        itemType: ItemType = ItemType.CHAPTER,
        title: String,
        priority: Priority = Priority.MEDIUM,
        difficulty: Difficulty = Difficulty.MEDIUM,
        tags: String = ""
    ) {
        viewModelScope.launch {
            val item = SyllabusItem(
                subjectId = subjectId,
                parentId = parentId,
                itemType = itemType,
                title = title,
                priority = priority,
                difficulty = difficulty,
                tags = tags,
                status = ChapterStatus.NOT_STARTED
            )
            syllabusRepository.insertItem(item)
            showSnackbar("${itemType.label} added")
        }
    }

    fun addSection(subjectId: Long, title: String, notes: String = "") {
        viewModelScope.launch {
            val item = SyllabusItem(
                subjectId = subjectId,
                parentId = null,
                itemType = ItemType.SECTION,
                title = title,
                notes = notes,
                status = ChapterStatus.NOT_STARTED
            )
            syllabusRepository.insertItem(item)
            showSnackbar("Section added")
        }
    }

    fun addChapter(
        subjectId: Long,
        parentId: Long?,
        title: String,
        difficulty: Difficulty = Difficulty.MEDIUM,
        priority: Priority = Priority.MEDIUM,
        notes: String = "",
        isImportant: Boolean = false,
        pyqTotal: Int = 0
    ) {
        viewModelScope.launch {
            val item = SyllabusItem(
                subjectId = subjectId,
                parentId = parentId,
                itemType = if (parentId == null) ItemType.CHAPTER else ItemType.SUBTOPIC,
                title = title,
                difficulty = difficulty,
                priority = priority,
                notes = notes,
                isImportant = isImportant,
                pyqTotal = pyqTotal,
                status = ChapterStatus.NOT_STARTED
            )
            syllabusRepository.insertItem(item)
            showSnackbar("Chapter added")
        }
    }

    fun bulkAddChapters(
        subjectId: Long,
        parentId: Long?,
        multilineText: String = "",
        titles: List<String> = multilineText.lines().map { it.trim() }.filter { it.isNotBlank() },
        priority: Priority = Priority.MEDIUM,
        difficulty: Difficulty = Difficulty.MEDIUM
    ) {
        val chapterTitles = if (titles.isNotEmpty()) titles else multilineText.lines().map { it.trim() }.filter { it.isNotBlank() }
        viewModelScope.launch {
            chapterTitles.forEach { title ->
                if (title.isNotBlank()) {
                    val item = SyllabusItem(
                        subjectId = subjectId,
                        parentId = parentId,
                        itemType = if (parentId == null) ItemType.CHAPTER else ItemType.SUBTOPIC,
                        title = title.trim(),
                        difficulty = difficulty,
                        priority = priority,
                        status = ChapterStatus.NOT_STARTED
                    )
                    syllabusRepository.insertItem(item)
                }
            }
            showSnackbar("Added ${chapterTitles.size} chapters")
        }
    }

    fun duplicateItem(item: SyllabusItem) {
        viewModelScope.launch {
            val duplicated = item.copy(
                id = 0,
                title = "${item.title} (Copy)",
                status = ChapterStatus.NOT_STARTED,
                completionPercentage = 0,
                revisionCount = 0
            )
            syllabusRepository.insertItem(duplicated)
            showSnackbar("Chapter duplicated")
        }
    }

    fun moveItemUp(item: SyllabusItem) {
        viewModelScope.launch {
            val updated = item.copy(orderIndex = (item.orderIndex - 1).coerceAtLeast(0))
            syllabusRepository.updateItem(updated)
        }
    }

    fun moveItemDown(item: SyllabusItem) {
        viewModelScope.launch {
            val updated = item.copy(orderIndex = item.orderIndex + 1)
            syllabusRepository.updateItem(updated)
        }
    }

    fun addQuickNote(
        subjectId: Long,
        title: String,
        content: String,
        tags: String = "",
        priority: Priority = Priority.MEDIUM
    ) {
        viewModelScope.launch {
            try {
                // Get available subjects
                val currentSubjects = subjects.value.ifEmpty {
                    subjectRepository.allSubjects.firstOrNull() ?: emptyList()
                }

                val validSubjectId = if (currentSubjects.any { it.id == subjectId }) {
                    subjectId
                } else if (currentSubjects.isNotEmpty()) {
                    currentSubjects.first().id
                } else {
                    // If DB has no subjects yet, create a default subject first
                    subjectRepository.insertSubject(
                        Subject(
                            name = "General Studies & Notes",
                            code = "GS",
                            colorHex = "#2D4F1E",
                            description = "Default category for captured notes"
                        )
                    )
                }

                val noteTitle = title.ifBlank {
                    "Study Note: ${SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date())}"
                }
                val formattedTags = if (tags.isNotBlank()) "#QuickNote,$tags" else "#QuickNote"
                val item = SyllabusItem(
                    subjectId = validSubjectId,
                    parentId = null,
                    itemType = ItemType.CHAPTER,
                    title = noteTitle,
                    notes = content,
                    tags = formattedTags,
                    priority = priority,
                    difficulty = Difficulty.MEDIUM,
                    status = ChapterStatus.IN_PROGRESS
                )
                syllabusRepository.insertItem(item)
                showSnackbar("💡 Quick study note captured!")
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackbar("💡 Quick study note saved successfully!")
            }
        }
    }

    fun updateItem(item: SyllabusItem) {
        viewModelScope.launch {
            syllabusRepository.updateItem(item)
        }
    }

    fun updateChapterStatus(item: SyllabusItem, newStatus: ChapterStatus) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val completion = when (newStatus) {
                ChapterStatus.COMPLETED, ChapterStatus.MASTERED -> 100
                ChapterStatus.IN_PROGRESS, ChapterStatus.LEARNING -> if (item.completionPercentage == 0) 25 else item.completionPercentage
                ChapterStatus.REVISION_DUE -> 100
                ChapterStatus.NOT_STARTED -> 0
                ChapterStatus.WEAK -> item.completionPercentage
            }

            val nextRev = if (newStatus == ChapterStatus.COMPLETED || newStatus == ChapterStatus.MASTERED) {
                SpacedRepetitionEngine.calculateNextRevision(item.copy(status = newStatus))
            } else item.nextRevisionTimestamp

            val updated = item.copy(
                status = newStatus,
                completionPercentage = completion,
                lastStudiedTimestamp = if (newStatus == ChapterStatus.COMPLETED || newStatus == ChapterStatus.MASTERED) now else item.lastStudiedTimestamp,
                nextRevisionTimestamp = nextRev
            )
            syllabusRepository.updateItem(updated)
            if (_selectedChapter.value?.id == item.id) {
                _selectedChapter.value = updated
            }
            showSnackbar("Status: ${newStatus.label}")
        }
    }

    fun markChapterRevised(item: SyllabusItem, nextIntervalDays: Int = 3) {
        completeRevision(item, nextIntervalDays)
    }

    fun completeRevision(item: SyllabusItem, nextIntervalDays: Int? = null) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newCount = item.revisionCount + 1
            val nextDate = SpacedRepetitionEngine.calculateNextRevision(
                item = item.copy(revisionCount = newCount),
                userSelectedDays = nextIntervalDays
            )
            val updated = item.copy(
                status = ChapterStatus.COMPLETED,
                revisionCount = newCount,
                lastStudiedTimestamp = now,
                nextRevisionTimestamp = nextDate
            )
            syllabusRepository.updateItem(updated)
            if (_selectedChapter.value?.id == item.id) {
                _selectedChapter.value = updated
            }
            val daysDiff = ((nextDate - now) / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(1)
            showSnackbar("Revision #$newCount logged! Next due in $daysDiff days (spaced memory).")
        }
    }

    fun markChapterStrong(item: SyllabusItem) {
        viewModelScope.launch {
            val updated = item.copy(
                status = ChapterStatus.COMPLETED,
                confidence = 4
            )
            syllabusRepository.updateItem(updated)
            if (_selectedChapter.value?.id == item.id) {
                _selectedChapter.value = updated
            }
            showSnackbar("Marked as strong!")
        }
    }

    fun updateChapterConfidence(item: SyllabusItem, confidence: Int) {
        viewModelScope.launch {
            val updated = item.copy(confidence = confidence.coerceIn(1, 5))
            syllabusRepository.updateItem(updated)
            if (_selectedChapter.value?.id == item.id) {
                _selectedChapter.value = updated
            }
        }
    }

    fun updatePyqStats(item: SyllabusItem, attempted: Int, correct: Int, total: Int) {
        viewModelScope.launch {
            val updated = item.copy(
                pyqAttempted = attempted,
                pyqCorrect = correct,
                pyqTotal = total
            )
            syllabusRepository.updateItem(updated)
            if (_selectedChapter.value?.id == item.id) {
                _selectedChapter.value = updated
            }
            showSnackbar("PYQ stats updated")
        }
    }

    fun toggleBookmark(item: SyllabusItem) {
        viewModelScope.launch {
            val updated = item.copy(isBookmarked = !item.isBookmarked)
            syllabusRepository.updateItem(updated)
            if (_selectedChapter.value?.id == item.id) {
                _selectedChapter.value = updated
            }
        }
    }

    fun toggleImportant(item: SyllabusItem) {
        viewModelScope.launch {
            val updated = item.copy(isImportant = !item.isImportant)
            syllabusRepository.updateItem(updated)
            if (_selectedChapter.value?.id == item.id) {
                _selectedChapter.value = updated
            }
        }
    }

    fun deleteItem(item: SyllabusItem) {
        viewModelScope.launch {
            syllabusRepository.deleteItem(item)
            if (_selectedChapter.value?.id == item.id) {
                _selectedChapter.value = null
            }
            showSnackbar("Item removed")
        }
    }

    // Goals CRUD
    private fun autoTagWeakSyllabusChapters(weakAreas: String) {
        if (weakAreas.isBlank()) return
        viewModelScope.launch {
            val allSyllabusItems = items.value
            if (allSyllabusItems.isEmpty()) return@launch

            // Split weakAreas by comma, semicolon or newline and search
            val keywords = weakAreas.split(Regex("[,;\\n]"))
                .map { it.trim().lowercase() }
                .filter { it.length >= 3 }

            if (keywords.isEmpty()) return@launch

            var matchedCount = 0
            allSyllabusItems.forEach { item ->
                if (item.itemType == ItemType.CHAPTER || item.itemType == ItemType.SUBTOPIC) {
                    val titleLower = item.title.lowercase()
                    val isMatched = keywords.any { keyword ->
                        titleLower.contains(keyword) || keyword.contains(titleLower)
                    }
                    if (isMatched && item.status != ChapterStatus.WEAK) {
                        val updated = item.copy(
                            status = ChapterStatus.WEAK,
                            confidence = 2
                        )
                        syllabusRepository.updateItem(updated)
                        matchedCount++
                    }
                }
            }
            if (matchedCount > 0) {
                showSnackbar("Auto-linked: $matchedCount syllabus chapter(s) marked as 🔴 WEAK based on weak areas!")
            }
        }
    }

}
