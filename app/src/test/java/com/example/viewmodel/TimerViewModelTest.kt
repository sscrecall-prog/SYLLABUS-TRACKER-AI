package com.example.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ItemType
import com.example.data.model.Subject
import com.example.data.model.SyllabusItem
import com.example.data.model.TimerMode
import com.example.ui.viewmodel.TimerViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TimerViewModelTest {

    private lateinit var app: Application
    private lateinit var timerViewModel: TimerViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        timerViewModel = TimerViewModel(app)
    }

    @Test
    fun initialState_hasDefaultPomodoroSettings() {
        assertEquals(TimerMode.POMODORO, timerViewModel.timerMode.value)
        assertEquals(25 * 60L, timerViewModel.timerRemainingSeconds.value)
        assertEquals(25 * 60L, timerViewModel.timerTotalDurationSeconds.value)
        assertFalse(timerViewModel.isTimerRunning.value)
        assertNull(timerViewModel.timerSubject.value)
        assertNull(timerViewModel.timerChapter.value)
        assertEquals(0, timerViewModel.pomodoroCyclesCompleted.value)
    }

    @Test
    fun setTimerPreset_updatesModeAndDuration() {
        timerViewModel.setTimerPreset(45, TimerMode.CUSTOM)

        assertEquals(TimerMode.CUSTOM, timerViewModel.timerMode.value)
        assertEquals(45 * 60L, timerViewModel.timerTotalDurationSeconds.value)
        assertEquals(45 * 60L, timerViewModel.timerRemainingSeconds.value)
        assertFalse(timerViewModel.isTimerRunning.value)
    }

    @Test
    fun setTimerTarget_assignsSubjectAndChapter() {
        val sub = Subject(id = 5, name = "History", code = "HIST", iconName = "book", colorHex = "#555")
        val chapter = SyllabusItem(id = 20, subjectId = 5, title = "Mughal Empire", itemType = ItemType.CHAPTER)

        timerViewModel.setTimerTarget(sub, chapter)

        assertEquals(sub, timerViewModel.timerSubject.value)
        assertEquals(chapter, timerViewModel.timerChapter.value)
    }

    @Test
    fun startAndPauseTimer_updatesRunningState() {
        assertFalse(timerViewModel.isTimerRunning.value)

        timerViewModel.startTimer()
        assertTrue(timerViewModel.isTimerRunning.value)

        timerViewModel.pauseTimer()
        assertFalse(timerViewModel.isTimerRunning.value)
    }

    @Test
    fun resetTimer_resetsRemainingTimeToTotalDuration() {
        timerViewModel.setTimerPreset(30, TimerMode.CUSTOM)
        timerViewModel.startTimer()
        timerViewModel.pauseTimer()

        timerViewModel.resetTimer()

        assertFalse(timerViewModel.isTimerRunning.value)
        assertEquals(30 * 60L, timerViewModel.timerRemainingSeconds.value)
    }

    @Test
    fun setTimerMode_stopwatchMode_resetsDurationToZero() {
        timerViewModel.setTimerMode(TimerMode.STOPWATCH)

        assertEquals(TimerMode.STOPWATCH, timerViewModel.timerMode.value)
        assertEquals(0L, timerViewModel.timerTotalDurationSeconds.value)
        assertEquals(0L, timerViewModel.timerRemainingSeconds.value)
    }
}
