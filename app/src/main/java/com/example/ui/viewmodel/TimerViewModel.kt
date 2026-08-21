package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.util.AmbientSoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : BaseViewModel(application) {
    val timerMode = MutableStateFlow(TimerMode.POMODORO)
    val timerRemainingSeconds = MutableStateFlow(25 * 60L)
    val timerTotalDurationSeconds = MutableStateFlow(25 * 60L)
    val isTimerRunning = MutableStateFlow(false)
    val timerSubject = MutableStateFlow<Subject?>(null)
    val timerChapter = MutableStateFlow<SyllabusItem?>(null)
    val pomodoroCyclesCompleted = MutableStateFlow(0)

    // Timestamp-based architecture state
    private var startTimestampMillis: Long = 0L
    private var accumulatedElapsedSeconds: Long = 0L
    private var timerJob: Job? = null

    // Ambient Sound Manager State Flows
    val ambientSound: StateFlow<AmbientSoundType> = AmbientSoundManager.currentSound
    val ambientVolume: StateFlow<Float> = AmbientSoundManager.volume
    val isAmbientPlaying: StateFlow<Boolean> = AmbientSoundManager.isAudioActive
    val ambientAutoPlayWithTimer: StateFlow<Boolean> = AmbientSoundManager.autoPlayWithTimer

    private fun computeCurrentElapsedSeconds(): Long {
        return if (isTimerRunning.value && startTimestampMillis > 0L) {
            val currentLegElapsed = (System.currentTimeMillis() - startTimestampMillis) / 1000L
            (accumulatedElapsedSeconds + currentLegElapsed).coerceAtLeast(0L)
        } else {
            accumulatedElapsedSeconds
        }
    }

    private fun updateRemainingTime() {
        val totalElapsed = computeCurrentElapsedSeconds()
        if (timerMode.value == TimerMode.STOPWATCH) {
            timerRemainingSeconds.value = totalElapsed
        } else {
            val duration = timerTotalDurationSeconds.value
            val remaining = (duration - totalElapsed).coerceAtLeast(0L)
            timerRemainingSeconds.value = remaining

            if (remaining <= 0L && isTimerRunning.value) {
                onTimerFinished()
            }
        }
    }

    fun setTimerTarget(subject: Subject?, chapter: SyllabusItem? = null) {
        timerSubject.value = subject
        timerChapter.value = chapter
    }

    fun setTimerTargetById(subjectId: Long?, chapterId: Long? = null) {
        val sub = if (subjectId != null) subjects.value.find { it.id == subjectId } else null
        val top = if (chapterId != null) items.value.find { it.id == chapterId } else null
        timerSubject.value = sub
        timerChapter.value = top
    }

    fun setTimerPreset(durationMinutes: Int, mode: TimerMode = TimerMode.POMODORO) {
        timerMode.value = mode
        setTimerDuration(durationMinutes)
    }

    fun startTimer() {
        if (isTimerRunning.value) return
        startTimestampMillis = System.currentTimeMillis()
        isTimerRunning.value = true

        if (AmbientSoundManager.autoPlayWithTimer.value && AmbientSoundManager.currentSound.value != AmbientSoundType.NONE) {
            AmbientSoundManager.start()
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isTimerRunning.value) {
                updateRemainingTime()
                delay(200)
            }
        }
    }

    fun pauseTimer() {
        if (isTimerRunning.value) {
            accumulatedElapsedSeconds = computeCurrentElapsedSeconds()
            startTimestampMillis = 0L
            isTimerRunning.value = false
            if (AmbientSoundManager.autoPlayWithTimer.value) {
                AmbientSoundManager.pause()
            }
        }
        timerJob?.cancel()
        updateRemainingTime()
    }

    fun resetTimer() {
        pauseTimer()
        accumulatedElapsedSeconds = 0L
        startTimestampMillis = 0L
        val duration = when (timerMode.value) {
            TimerMode.POMODORO -> appSettings.value.pomodoroWorkMinutes * 60L
            TimerMode.CUSTOM -> timerTotalDurationSeconds.value
            TimerMode.STOPWATCH -> 0L
        }
        timerTotalDurationSeconds.value = duration
        timerRemainingSeconds.value = duration
    }

    private fun onTimerFinished() {
        pauseTimer()
        val duration = timerTotalDurationSeconds.value
        logCompletedTimerSession(duration)
        pomodoroCyclesCompleted.value += 1
        showSnackbar("🎉 Pomodoro Session complete! Time for a short break.")
        resetTimer()
    }

    // Ambient Focus Audio Controls
    fun selectAmbientSound(sound: AmbientSoundType) {
        AmbientSoundManager.selectSound(sound)
    }

    fun setAmbientVolume(volume: Float) {
        AmbientSoundManager.setVolume(volume)
    }

    fun toggleAmbientPlayPause() {
        AmbientSoundManager.togglePlayPause()
    }

    fun toggleAmbientAutoPlayWithTimer(enabled: Boolean) {
        AmbientSoundManager.setAutoPlayWithTimer(enabled)
    }

    override fun onCleared() {
        super.onCleared()
        AmbientSoundManager.stop()
    }

    fun setTimerMode(mode: TimerMode) {
        timerMode.value = mode
        resetTimer()
    }

    fun setTimerDuration(minutes: Int) {
        pauseTimer()
        val duration = minutes * 60L
        timerTotalDurationSeconds.value = duration
        accumulatedElapsedSeconds = 0L
        startTimestampMillis = 0L
        timerRemainingSeconds.value = duration
    }

    fun selectTimerSubject(subject: Subject?) {
        timerSubject.value = subject
    }

    fun selectTimerChapter(chapter: SyllabusItem?) {
        timerChapter.value = chapter
    }

    fun finishAndSaveTimerSession() {
        finishAndLogTimer()
    }

    fun finishAndLogTimer() {
        val elapsedSecs = computeCurrentElapsedSeconds()
        pauseTimer()

        if (elapsedSecs >= 60) {
            viewModelScope.launch {
                val sub = timerSubject.value ?: subjects.value.firstOrNull()
                val subId = sub?.id ?: 1L
                val subName = sub?.name ?: "General Study"
                val top = timerChapter.value
                studySessionRepository.logStudySession(
                    subjectId = subId,
                    subjectName = subName,
                    chapterId = top?.id,
                    chapterTitle = top?.title ?: "Free Study Session",
                    durationSeconds = elapsedSecs,
                    mode = timerMode.value
                )
                resetTimer()
                showSnackbar("Logged ${(elapsedSecs / 60)} minutes of study!")
            }
        } else {
            resetTimer()
        }
    }

    private fun logCompletedTimerSession(durationSecs: Long) {
        viewModelScope.launch {
            val sub = timerSubject.value ?: subjects.value.firstOrNull()
            val subId = sub?.id ?: 1L
            val subName = sub?.name ?: "General Study"
            val top = timerChapter.value
            studySessionRepository.logStudySession(
                subjectId = subId,
                subjectName = subName,
                chapterId = top?.id,
                chapterTitle = top?.title ?: "Pomodoro Session",
                durationSeconds = durationSecs,
                mode = timerMode.value
            )
        }
    }
}
