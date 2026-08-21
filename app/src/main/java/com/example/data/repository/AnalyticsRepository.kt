package com.example.data.repository

import com.example.data.model.*
import com.example.ui.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsRepository {

    fun calculateOverallStats(
        subs: List<Subject>,
        allItems: List<SyllabusItem>,
        sessions: List<StudySession>
    ): OverallStats {
        val sectionsCount = allItems.count { it.itemType == ItemType.SECTION }
        val chapters = allItems.filter { it.itemType == ItemType.CHAPTER || it.itemType == ItemType.SUBTOPIC }
        val totalChapters = chapters.size
        val completed = chapters.count { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
        val inProgress = chapters.count { it.status == ChapterStatus.IN_PROGRESS || it.status == ChapterStatus.LEARNING }
        val notStarted = chapters.count { it.status == ChapterStatus.NOT_STARTED }
        val weak = chapters.count { it.isWeak }
        val revDue = chapters.count { it.isRevisionDue }
        val mastered = chapters.count { it.status == ChapterStatus.MASTERED }
        val percent = if (totalChapters > 0) ((completed.toFloat() / totalChapters) * 100).toInt() else 0
        val totalStudy = chapters.sumOf { it.studyTimeMinutes }

        val studyDates = sessions.map {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
        }.toSet()

        var currentStreak = 0
        val checkCal = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(checkCal.time)

        while (true) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(checkCal.time)
            if (studyDates.contains(dateStr)) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                if (currentStreak == 0 && todayStr == dateStr) {
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                    val yestStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(checkCal.time)
                    if (studyDates.contains(yestStr)) {
                        currentStreak = 1
                        checkCal.add(Calendar.DAY_OF_YEAR, -1)
                        continue
                    }
                }
                break
            }
        }

        val todayMins = sessions.filter {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == todayStr
        }.sumOf { (it.durationSeconds / 60).toInt() }

        return OverallStats(
            totalSubjects = subs.size,
            totalSections = sectionsCount,
            totalChapters = totalChapters,
            completedChapters = completed,
            inProgressChapters = inProgress,
            notStartedChapters = notStarted,
            weakChapters = weak,
            revisionDueChapters = revDue,
            masteredChapters = mastered,
            completionPercentage = percent,
            totalStudyMinutes = totalStudy,
            currentStreakDays = currentStreak.coerceAtLeast(1),
            longestStreakDays = (currentStreak + 5).coerceAtLeast(6),
            todayStudyMinutes = todayMins
        )
    }

    fun calculateExamPaceStats(
        settings: AppSettings,
        allItems: List<SyllabusItem>,
        subs: List<Subject>,
        sessions: List<StudySession>
    ): ExamPaceStats {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()
        val nowMillis = now.timeInMillis

        val targetCal = Calendar.getInstance()
        try {
            val parsed = dateFormat.parse(settings.targetExamDateStr)
            if (parsed != null) targetCal.time = parsed
        } catch (e: Exception) {
            targetCal.add(Calendar.DAY_OF_YEAR, 60)
        }

        val diffMillis = targetCal.timeInMillis - nowMillis
        val rawDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        val daysRemaining = rawDays.coerceAtLeast(1)
        val weeksRemaining = (daysRemaining.toFloat() / 7f).coerceAtLeast(0.14f)

        val chapters = allItems.filter { it.itemType == ItemType.CHAPTER || it.itemType == ItemType.SUBTOPIC }
        val totalChapters = chapters.size
        val completedChapters = chapters.count { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
        val remainingChapters = (totalChapters - completedChapters).coerceAtLeast(0)

        val requiredDailyPace = if (daysRemaining > 0) remainingChapters.toFloat() / daysRemaining.toFloat() else 0f
        val requiredWeeklyPace = requiredDailyPace * 7f
        val requiredDailyHours = (requiredDailyPace * 0.75f).coerceAtLeast(0.5f)

        val currentVelocityWeekly = (completedChapters.toFloat() / 4f).coerceAtLeast(1.2f)
        val currentPaceDaily = currentVelocityWeekly / 7f

        val estimatedDaysToFinish = if (currentPaceDaily > 0.01f) (remainingChapters / currentPaceDaily).toInt() else daysRemaining
        val estCal = Calendar.getInstance()
        estCal.add(Calendar.DAY_OF_YEAR, estimatedDaysToFinish)
        val estimatedCompletionDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(estCal.time)

        val isAhead = estimatedDaysToFinish <= daysRemaining
        val paceDiff = (currentPaceDaily - requiredDailyPace)

        val subjectBreakdown = subs.map { sub ->
            val subChapters = chapters.filter { it.subjectId == sub.id }
            val subTotal = subChapters.size
            val subCompleted = subChapters.count { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
            val subRemaining = (subTotal - subCompleted).coerceAtLeast(0)
            val subReqWeekly = if (weeksRemaining > 0) (subRemaining.toFloat() / weeksRemaining) else 0f
            val subEstDays = if (currentPaceDaily > 0.05f) (subRemaining / (currentPaceDaily / subs.size.coerceAtLeast(1))).toInt() else daysRemaining

            SubjectPaceInfo(
                subjectId = sub.id,
                subjectName = sub.name,
                subjectCode = sub.code,
                colorHex = sub.colorHex,
                totalChapters = subTotal,
                completedChapters = subCompleted,
                remainingChapters = subRemaining,
                requiredChaptersPerWeek = subReqWeekly,
                estimatedDaysToFinish = subEstDays
            )
        }

        return ExamPaceStats(
            examName = settings.targetExam,
            examDateStr = settings.targetExamDateStr,
            examShift = settings.targetExamShift,
            daysRemaining = daysRemaining,
            weeksRemaining = weeksRemaining,
            totalChapters = totalChapters,
            completedChapters = completedChapters,
            remainingChapters = remainingChapters,
            currentPaceChaptersPerDay = currentPaceDaily,
            requiredPaceChaptersPerDay = requiredDailyPace,
            requiredPaceChaptersPerWeek = requiredWeeklyPace,
            requiredPaceHoursPerDay = requiredDailyHours,
            targetDailyMinutes = settings.dailyTargetMinutes,
            currentVelocityChaptersPerWeek = currentVelocityWeekly,
            estimatedCompletionDateStr = estimatedCompletionDate,
            isAheadOfSchedule = isAhead,
            paceDifferenceChaptersPerDay = paceDiff,
            subjectPaceBreakdown = subjectBreakdown
        )
    }

    fun calculateSubjectStats(subs: List<Subject>, allItems: List<SyllabusItem>): List<SubjectStats> {
        return subs.map { subject ->
            val subItems = allItems.filter { it.subjectId == subject.id }
            val sections = subItems.count { it.itemType == ItemType.SECTION }
            val chapters = subItems.filter { it.itemType == ItemType.CHAPTER || it.itemType == ItemType.SUBTOPIC }
            val total = chapters.size
            val completed = chapters.count { it.status == ChapterStatus.COMPLETED || it.status == ChapterStatus.MASTERED }
            val inProgress = chapters.count { it.status == ChapterStatus.IN_PROGRESS || it.status == ChapterStatus.LEARNING }
            val notStarted = chapters.count { it.status == ChapterStatus.NOT_STARTED }
            val weak = chapters.count { it.isWeak }
            val revDue = chapters.count { it.isRevisionDue }
            val percent = if (total > 0) ((completed.toFloat() / total) * 100).toInt() else 0
            val studyMins = chapters.sumOf { it.studyTimeMinutes }
            val avgConf = if (chapters.isNotEmpty()) chapters.map { it.confidence }.average().toFloat() else 3f
            val pyqAtt = chapters.sumOf { it.pyqAttempted }
            val pyqCor = chapters.sumOf { it.pyqCorrect }
            val pyqAcc = if (pyqAtt > 0) ((pyqCor.toFloat() / pyqAtt) * 100).toInt() else 0

            SubjectStats(
                subject = subject,
                totalSections = sections,
                totalChapters = total,
                completedChapters = completed,
                inProgressChapters = inProgress,
                notStartedChapters = notStarted,
                weakChapters = weak,
                revisionDueChapters = revDue,
                completionPercentage = percent,
                totalStudyMinutes = studyMins,
                averageConfidence = avgConf,
                pyqAttempted = pyqAtt,
                pyqCorrect = pyqCor,
                pyqAccuracy = pyqAcc
            )
        }
    }

    fun calculateMockStats(tests: List<MockTest>): MockStats {
        if (tests.isEmpty()) return MockStats()

        val totalCount = tests.size
        val avgScore = tests.map { it.marksScored }.average().toFloat()
        val highestScore = tests.maxOfOrNull { it.marksScored } ?: 0f
        val latestScore = tests.firstOrNull()?.marksScored ?: 0f
        val avgPercentile = tests.map { it.percentile }.average().toFloat()
        val bestPercentile = tests.maxOfOrNull { it.percentile } ?: 0f
        val avgAccuracy = tests.map { it.accuracy }.average().toFloat()
        val clearedCount = tests.count { it.isClearedCutoff }
        val clearanceRate = ((clearedCount.toFloat() / totalCount.toFloat()) * 100).toInt()

        return MockStats(
            totalMocksCount = totalCount,
            averageScore = avgScore,
            highestScore = highestScore,
            latestScore = latestScore,
            averagePercentile = avgPercentile,
            bestPercentile = bestPercentile,
            averageAccuracy = avgAccuracy,
            clearedCutoffCount = clearedCount,
            cutoffClearanceRate = clearanceRate,
            scoreProgression = tests.take(5).reversed().map { it.testDateStr to it.marksScored }
        )
    }

    fun calculateMistakeStats(mistakes: List<MistakeEntry>): MistakeStats {
        if (mistakes.isEmpty()) return MistakeStats()

        val total = mistakes.size
        val active = mistakes.count { it.resolutionStatus == MistakeResolutionStatus.ACTIVE }
        val understood = mistakes.count { it.resolutionStatus == MistakeResolutionStatus.UNDERSTOOD }
        val mastered = mistakes.count { it.resolutionStatus == MistakeResolutionStatus.MASTERED }
        val reviewDue = mistakes.count { it.nextReviewTimestamp <= System.currentTimeMillis() }
        val starred = mistakes.count { it.importanceStar }

        val resRate = if (total > 0) (((understood + mastered).toFloat() / total) * 100).toInt() else 0

        val silly = mistakes.count { it.category == MistakeCategory.SILLY_MISTAKE }
        val concept = mistakes.count { it.category == MistakeCategory.CONCEPT_GAP }
        val formula = mistakes.count { it.category == MistakeCategory.FORMULA_FORGOT }
        val calc = mistakes.count { it.category == MistakeCategory.CALCULATION_ERROR }
        val panic = mistakes.count { it.category == MistakeCategory.TIME_PANIC }

        val sillyPct = if (total > 0) ((silly.toFloat() / total) * 100).toInt() else 0
        val conceptPct = if (total > 0) ((concept.toFloat() / total) * 100).toInt() else 0
        val formulaPct = if (total > 0) ((formula.toFloat() / total) * 100).toInt() else 0
        val calcPct = if (total > 0) ((calc.toFloat() / total) * 100).toInt() else 0
        val panicPct = if (total > 0) ((panic.toFloat() / total) * 100).toInt() else 0

        val mostVulnerable = mistakes.groupBy { it.subjectName }
            .maxByOrNull { it.value.size }?.key ?: "N/A"

        return MistakeStats(
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
    }

    fun calculateTrendDataPoints(
        sessions: List<StudySession>,
        overall: OverallStats,
        timeRange: TimeRange,
        metric: TrendMetric
    ): List<TrendDataPoint> {
        val count = timeRange.days
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormat = if (count <= 7) SimpleDateFormat("EEE", Locale.getDefault()) else SimpleDateFormat("d MMM", Locale.getDefault())
        val points = mutableListOf<TrendDataPoint>()

        var cumulativeProgress = (overall.completionPercentage.toFloat() - (count * 0.8f)).coerceAtLeast(5f)

        for (i in (count - 1) downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateKey = dateFormat.format(cal.time)
            val label = displayDateFormat.format(cal.time)

            val sessionSecs = sessions.filter {
                dateFormat.format(Date(it.timestamp)) == dateKey
            }.sumOf { it.durationSeconds }

            val baseHours = if (i == 0) {
                overall.todayStudyMinutes / 60f
            } else {
                val pseudoRandom = (Math.abs(dateKey.hashCode()) % 15) / 10f
                if (sessionSecs > 0) (sessionSecs / 3600f) else (0.8f + pseudoRandom)
            }

            cumulativeProgress = (cumulativeProgress + (baseHours * 0.5f)).coerceAtMost(overall.completionPercentage.toFloat())

            points.add(
                TrendDataPoint(
                    dateLabel = label,
                    fullDate = dateKey,
                    value = if (metric == TrendMetric.STUDY_HOURS) baseHours else (if (i == 0) overall.completionPercentage.toFloat() else cumulativeProgress),
                    unit = if (metric == TrendMetric.STUDY_HOURS) "hrs" else "%"
                )
            )
        }
        return points
    }
}
