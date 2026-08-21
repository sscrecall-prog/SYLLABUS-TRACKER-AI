/* Dashboard Screen Component */

import { state } from '../state.js';
import { ambientAudio } from '../ambient.js';

const MOTIVATIONAL_QUOTES = [
  { quote: "Success is the sum of small efforts, repeated day in and day out.", author: "Robert Collier" },
  { quote: "The secret of getting ahead is getting started.", author: "Mark Twain" },
  { quote: "Consistency is what transforms average into excellence.", author: "SSC Aspirant Mantra" },
  { quote: "Discipline is choosing between what you want now and what you want most.", author: "Abraham Lincoln" }
];

export function renderDashboard() {
  const chapters = state.items.filter(i => i.itemType === 'CHAPTER');
  const totalChapters = chapters.length;
  const completedChapters = chapters.filter(c => c.status === 'COMPLETED' || c.status === 'MASTERED').length;
  const inProgressChapters = chapters.filter(c => c.status === 'IN_PROGRESS').length;
  const weakChapters = chapters.filter(c => c.status === 'WEAK' || (c.confidence && c.confidence <= 2)).length;
  const revisionDueChapters = chapters.filter(c => c.nextRevisionTimestamp && c.nextRevisionTimestamp <= Date.now()).length;
  
  const overallCompletion = totalChapters > 0 ? Math.round((completedChapters / totalChapters) * 100) : 0;

  // Days remaining & pace calculation
  let daysRemaining = 120;
  if (state.settings.targetExamDateStr) {
    const examDate = new Date(state.settings.targetExamDateStr);
    const diff = Math.ceil((examDate - new Date()) / (1000 * 60 * 60 * 24));
    daysRemaining = diff > 0 ? diff : 0;
  }

  const dailyTargetHours = ((state.settings.dailyTargetMinutes || 240) / 60).toFixed(1);
  const randomQuote = MOTIVATIONAL_QUOTES[Math.floor(Math.random() * MOTIVATIONAL_QUOTES.length)];

  // Ambient sound state
  const currentSound = ambientAudio.currentType;
  const isPlaying = ambientAudio.isPlaying;
  const volumePct = Math.round(ambientAudio.volume * 100);

  return `
    <div class="dashboard-view" style="display: flex; flex-direction: column; gap: 20px;">
      <!-- Pace & Exam Countdown Hero Banner -->
      <div class="card" style="background: linear-gradient(135deg, var(--surface-color) 0%, var(--primary-container) 100%); border: 1.5px solid var(--primary-hover);">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 16px;">
          <div>
            <div style="display: flex; align-items: center; gap: 8px;">
              <span style="font-size: 11px; font-weight: 800; color: var(--primary); letter-spacing: 0.8px; text-transform: uppercase;">TARGET EXAM</span>
              <span class="badge" style="background: var(--primary); color: #fff; font-size: 10px; padding: 2px 8px;">Pace: Active</span>
            </div>
            <h2 style="font-size: 24px; font-weight: 800; margin-top: 4px;">${state.settings.targetExam || 'SSC CGL 2026'}</h2>
            <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">
              Daily Target: <strong>${dailyTargetHours} Hours/Day</strong> | Shift: ${state.settings.targetExamShift || 'Tier-1 / Prelims'}
            </p>
          </div>

          <div style="display: flex; gap: 12px; align-items: center;">
            <div style="background: var(--surface-color); border: 1px solid var(--border-color); padding: 12px 18px; border-radius: var(--radius-md); text-align: center; box-shadow: var(--shadow-sm);">
              <span style="font-size: 26px; font-weight: 900; color: var(--primary); font-family: monospace;">${daysRemaining}</span>
              <span style="font-size: 10px; display: block; color: var(--text-muted); font-weight: 800; letter-spacing: 0.5px;">DAYS LEFT</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Ambient Audio Focus Player Card (Exact Mirror of Android) -->
      <div class="card" style="border: 1px solid var(--border-color); background: var(--surface-color);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
          <div style="display: flex; align-items: center; gap: 10px;">
            <div style="width: 38px; height: 38px; border-radius: 10px; background: var(--primary-container); color: var(--primary); display: flex; align-items: center; justify-content: center; font-size: 20px;">
              🎧
            </div>
            <div>
              <span style="font-size: 10px; font-weight: 800; color: var(--primary); letter-spacing: 0.5px;">AMBIENT FOCUS AUDIO</span>
              <h4 style="font-size: 14px; font-weight: 700; margin: 0;" id="ambient-title-text">${currentSound === 'NONE' ? 'Sound Muted' : currentSound + ' Ambiance'}</h4>
            </div>
          </div>

          <button class="btn btn-primary" id="ambient-toggle-btn" style="border-radius: 99px; width: 40px; height: 40px; padding: 0; justify-content: center;">
            <span class="material-symbols-outlined">${isPlaying ? 'volume_up' : 'volume_off'}</span>
          </button>
        </div>

        <!-- Presets Row -->
        <div style="display: flex; gap: 8px; overflow-x: auto; padding-bottom: 8px; margin-bottom: 12px;">
          <button class="btn ${currentSound === 'RAIN' ? 'btn-primary' : 'btn-secondary'} ambient-preset-btn" data-preset="RAIN">🌧️ Rain</button>
          <button class="btn ${currentSound === 'OCEAN' ? 'btn-primary' : 'btn-secondary'} ambient-preset-btn" data-preset="OCEAN">🌊 Ocean Waves</button>
          <button class="btn ${currentSound === 'FOREST' ? 'btn-primary' : 'btn-secondary'} ambient-preset-btn" data-preset="FOREST">🌲 Forest</button>
          <button class="btn ${currentSound === 'WHITE_NOISE' ? 'btn-primary' : 'btn-secondary'} ambient-preset-btn" data-preset="WHITE_NOISE">📻 White Noise</button>
          <button class="btn ${currentSound === 'NONE' ? 'btn-primary' : 'btn-secondary'} ambient-preset-btn" data-preset="NONE">🔇 Mute</button>
        </div>

        <!-- Volume Slider -->
        <div style="display: flex; align-items: center; gap: 12px; background: var(--surface-variant); padding: 8px 14px; border-radius: var(--radius-md);">
          <span class="material-symbols-outlined" style="font-size: 18px; color: var(--text-muted);">volume_down</span>
          <input type="range" id="ambient-vol-slider" min="0" max="100" value="${volumePct}" style="flex: 1; accent-color: var(--primary);" />
          <span style="font-size: 11px; font-weight: 800; color: var(--text-muted); width: 32px;" id="ambient-vol-text">${volumePct}%</span>
        </div>
      </div>

      <!-- Mindset Quote Card -->
      <div class="card" style="background: var(--surface-variant); border-left: 4px solid var(--primary);">
        <span style="font-size: 11px; font-weight: 800; color: var(--primary); letter-spacing: 0.5px; text-transform: uppercase;">DAILY FOCUS MANTRA</span>
        <p style="font-size: 14px; font-style: italic; margin-top: 4px; color: var(--text-main); font-weight: 600;">"${randomQuote.quote}"</p>
        <span style="font-size: 11px; color: var(--text-muted); display: block; margin-top: 2px;">— ${randomQuote.author}</span>
      </div>

      <!-- Stats Grid -->
      <div class="dashboard-grid">
        <div class="stats-card">
          <div class="stats-icon" style="background: rgba(16, 185, 129, 0.15); color: var(--status-completed);">
            <span class="material-symbols-outlined">donut_large</span>
          </div>
          <div class="stats-info">
            <span class="stats-value" style="color: var(--status-completed);">${overallCompletion}%</span>
            <span class="stats-label">Overall Completion</span>
          </div>
        </div>

        <div class="stats-card">
          <div class="stats-icon" style="background: rgba(59, 130, 246, 0.15); color: var(--status-in-progress);">
            <span class="material-symbols-outlined">auto_stories</span>
          </div>
          <div class="stats-info">
            <span class="stats-value">${completedChapters} / ${totalChapters}</span>
            <span class="stats-label">Chapters Completed</span>
          </div>
        </div>

        <div class="stats-card">
          <div class="stats-icon" style="background: rgba(245, 158, 11, 0.15); color: var(--status-revision);">
            <span class="material-symbols-outlined">update</span>
          </div>
          <div class="stats-info">
            <span class="stats-value" style="color: var(--status-revision);">${revisionDueChapters}</span>
            <span class="stats-label">Revisions Due</span>
          </div>
        </div>

        <div class="stats-card">
          <div class="stats-icon" style="background: rgba(239, 68, 68, 0.15); color: var(--status-weak);">
            <span class="material-symbols-outlined">report_problem</span>
          </div>
          <div class="stats-info">
            <span class="stats-value" style="color: var(--status-weak);">${weakChapters}</span>
            <span class="stats-label">Weak Topics</span>
          </div>
        </div>
      </div>

      <!-- Syllabus Track Bar -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">analytics</span> Overall Syllabus Track</span>
          <span style="font-weight: 700; font-size: 14px; color: var(--primary);">${completedChapters} Completed (${overallCompletion}%)</span>
        </div>
        <div class="progress-bar-container" style="height: 12px;">
          <div class="progress-bar-fill" style="width: ${overallCompletion}%;"></div>
        </div>
      </div>

      <!-- Subject Breakdown Cards -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">school</span> Subjects Breakdown</span>
          <button class="btn btn-secondary" id="dash-all-subjects-btn">View All Subjects</button>
        </div>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px;">
          ${state.subjects.map(sub => {
            const subChapters = state.items.filter(i => i.subjectId === sub.id && i.itemType === 'CHAPTER');
            const subDone = subChapters.filter(c => c.status === 'COMPLETED' || c.status === 'MASTERED').length;
            const subPct = subChapters.length > 0 ? Math.round((subDone / subChapters.length) * 100) : 0;
            return `
              <div class="sub-card" data-subid="${sub.id}" style="background: var(--surface-variant); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px; cursor: pointer;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                  <span style="font-weight: 700; font-size: 14px; color: ${sub.colorHex};">${sub.name}</span>
                  <span class="badge" style="background: ${sub.colorHex}22; color: ${sub.colorHex};">${sub.code}</span>
                </div>
                <div style="font-size: 12px; color: var(--text-muted); margin-bottom: 8px;">${subDone} of ${subChapters.length} Chapters (${subPct}%)</div>
                <div class="progress-bar-container">
                  <div class="progress-bar-fill" style="width: ${subPct}%; background-color: ${sub.colorHex};"></div>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      </div>

      <!-- Quick Action Shortcuts Grid -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">bolt</span> Quick Features</span>
        </div>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px;">
          <button class="btn btn-secondary quick-action-btn" data-nav="timer" style="justify-content: center; padding: 14px;">
            <span class="material-symbols-outlined" style="color: var(--primary);">timer</span> Study Timer
          </button>
          <button class="btn btn-secondary quick-action-btn" data-nav="mistakes" style="justify-content: center; padding: 14px;">
            <span class="material-symbols-outlined" style="color: var(--status-weak);">bookmark_remove</span> Error Diary
          </button>
          <button class="btn btn-secondary quick-action-btn" data-nav="mocktests" style="justify-content: center; padding: 14px;">
            <span class="material-symbols-outlined" style="color: var(--primary);">quiz</span> Mock Tests
          </button>
          <button class="btn btn-secondary quick-action-btn" data-nav="planner" style="justify-content: center; padding: 14px;">
            <span class="material-symbols-outlined" style="color: var(--status-in-progress);">calendar_month</span> Study Planner
          </button>
          <button class="btn btn-secondary quick-action-btn" data-nav="revision" style="justify-content: center; padding: 14px;">
            <span class="material-symbols-outlined" style="color: var(--status-revision);">update</span> Revision Schedule
          </button>
        </div>
      </div>
    </div>
  `;
}

export function bindDashboardEvents(container) {
  // Ambient sound events
  const toggleBtn = container.querySelector('#ambient-toggle-btn');
  if (toggleBtn) {
    toggleBtn.addEventListener('click', () => {
      ambientAudio.togglePlayPause();
      state.notify();
    });
  }

  container.querySelectorAll('.ambient-preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const preset = btn.dataset.preset;
      ambientAudio.setSound(preset);
      if (preset !== 'NONE' && !ambientAudio.isPlaying) {
        ambientAudio.play(preset);
      }
      state.notify();
    });
  });

  const volSlider = container.querySelector('#ambient-vol-slider');
  if (volSlider) {
    volSlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value) / 100;
      ambientAudio.setVolume(val);
      const text = container.querySelector('#ambient-vol-text');
      if (text) text.textContent = `${e.target.value}%`;
    });
  }

  // Navigation events
  container.querySelectorAll('.sub-card').forEach(card => {
    card.addEventListener('click', () => {
      const subId = parseInt(card.dataset.subid);
      state.setNav('syllabus', subId);
    });
  });

  const allSubBtn = container.querySelector('#dash-all-subjects-btn');
  if (allSubBtn) {
    allSubBtn.addEventListener('click', () => state.setNav('syllabus'));
  }

  container.querySelectorAll('.quick-action-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const nav = btn.dataset.nav;
      state.setNav(nav);
    });
  });
}
