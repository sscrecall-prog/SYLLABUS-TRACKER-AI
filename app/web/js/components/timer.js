/* Timer Screen Component (Pomodoro & Focus Timer) */

import { state } from '../state.js';

export function renderTimer() {
  const timer = state.timer;
  const mins = Math.floor(timer.remainingSeconds / 60).toString().padStart(2, '0');
  const secs = (timer.remainingSeconds % 60).toString().padStart(2, '0');

  return `
    <div class="timer-view" style="max-width: 600px; margin: 0 auto; text-align: center;">
      <!-- Timer Presets -->
      <div style="display: flex; gap: 8px; justify-content: center; margin-bottom: 24px; flex-wrap: wrap;">
        <button class="btn ${timer.mode === 'POMODORO' ? 'btn-primary' : 'btn-secondary'} timer-preset-btn" data-mins="25" data-mode="POMODORO">
          🎯 Focus (25m)
        </button>
        <button class="btn ${timer.mode === 'SHORT_BREAK' ? 'btn-primary' : 'btn-secondary'} timer-preset-btn" data-mins="5" data-mode="SHORT_BREAK">
          ☕ Short Break (5m)
        </button>
        <button class="btn ${timer.mode === 'LONG_BREAK' ? 'btn-primary' : 'btn-secondary'} timer-preset-btn" data-mins="15" data-mode="LONG_BREAK">
          🌴 Long Break (15m)
        </button>
        <button class="btn ${timer.mode === 'STOPWATCH' ? 'btn-primary' : 'btn-secondary'} timer-preset-btn" data-mins="0" data-mode="STOPWATCH">
          ⏱️ Stopwatch
        </button>
      </div>

      <!-- Main Timer Dial Display -->
      <div class="card" style="padding: 40px; background: radial-gradient(circle, var(--surface-variant) 0%, var(--surface-color) 100%); border-radius: var(--radius-lg); border: 2px solid var(--primary);">
        <div style="font-size: 72px; font-weight: 800; font-family: monospace; letter-spacing: -2px; color: var(--text-main); line-height: 1;">
          ${mins}:${secs}
        </div>
        <div style="font-size: 14px; font-weight: 700; color: var(--primary); margin-top: 12px; text-transform: uppercase;">
          ${timer.mode.replace('_', ' ')} MODE
        </div>

        <!-- Controls -->
        <div style="display: flex; gap: 16px; justify-content: center; margin-top: 32px;">
          <button class="btn btn-primary" id="timer-toggle-btn" style="padding: 14px 32px; font-size: 16px;">
            <span class="material-symbols-outlined">${timer.isRunning ? 'pause' : 'play_arrow'}</span>
            ${timer.isRunning ? 'Pause' : 'Start Focus'}
          </button>
          <button class="btn btn-secondary" id="timer-reset-btn" style="padding: 14px 20px;">
            <span class="material-symbols-outlined">replay</span> Reset
          </button>
        </div>
      </div>
    </div>
  `;
}

export function bindTimerEvents(container) {
  container.querySelectorAll('.timer-preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const mins = parseInt(btn.dataset.mins);
      const mode = btn.dataset.mode;
      if (state.timer.intervalId) clearInterval(state.timer.intervalId);
      
      state.timer.isRunning = false;
      state.timer.mode = mode;
      state.timer.remainingSeconds = mins * 60;
      state.timer.totalDurationSeconds = mins * 60;
      state.notify();
    });
  });

  const toggleBtn = container.querySelector('#timer-toggle-btn');
  if (toggleBtn) {
    toggleBtn.addEventListener('click', () => {
      if (state.timer.isRunning) {
        clearInterval(state.timer.intervalId);
        state.timer.isRunning = false;
        state.notify();
      } else {
        state.timer.isRunning = true;
        state.timer.intervalId = setInterval(() => {
          if (state.timer.mode === 'STOPWATCH') {
            state.timer.remainingSeconds++;
          } else {
            if (state.timer.remainingSeconds > 0) {
              state.timer.remainingSeconds--;
            } else {
              clearInterval(state.timer.intervalId);
              state.timer.isRunning = false;
              alert('Timer Complete! Great study session.');
            }
          }
          state.notify();
        }, 1000);
        state.notify();
      }
    });
  }

  const resetBtn = container.querySelector('#timer-reset-btn');
  if (resetBtn) {
    resetBtn.addEventListener('click', () => {
      if (state.timer.intervalId) clearInterval(state.timer.intervalId);
      state.timer.isRunning = false;
      state.timer.remainingSeconds = state.timer.totalDurationSeconds;
      state.notify();
    });
  }
}
