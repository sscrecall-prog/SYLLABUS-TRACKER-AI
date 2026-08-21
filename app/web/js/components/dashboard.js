/* Dashboard Screen Component */

import { state } from '../state.js';

export function renderDashboard() {
  const chapters = state.items.filter(i => i.itemType === 'CHAPTER');
  const totalChapters = chapters.length;
  const completedChapters = chapters.filter(c => c.status === 'COMPLETED' || c.status === 'MASTERED').length;
  const inProgressChapters = chapters.filter(c => c.status === 'IN_PROGRESS').length;
  const weakChapters = chapters.filter(c => c.status === 'WEAK' || (c.confidence && c.confidence <= 2)).length;
  const revisionDueChapters = chapters.filter(c => c.nextRevisionTimestamp && c.nextRevisionTimestamp <= Date.now()).length;
  
  const overallCompletion = totalChapters > 0 ? Math.round((completedChapters / totalChapters) * 100) : 0;

  // Days remaining for target exam
  let daysRemainingText = '120';
  if (state.settings.targetExamDateStr) {
    const examDate = new Date(state.settings.targetExamDateStr);
    const diff = Math.ceil((examDate - new Date()) / (1000 * 60 * 60 * 24));
    daysRemainingText = diff > 0 ? `${diff}` : '0';
  }

  return `
    <div class="dashboard-view">
      <!-- Target Exam Banner -->
      <div class="card" style="background: linear-gradient(135deg, var(--surface-color) 0%, var(--primary-container) 100%); border-color: var(--primary);">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
          <div>
            <span style="font-size: 11px; font-weight: 700; color: var(--accent-cream); letter-spacing: 0.5px; text-transform: uppercase;">TARGET EXAM</span>
            <h2 style="font-size: 22px; font-weight: 800; margin-top: 4px;">${state.settings.targetExam || 'SSC CGL 2026'}</h2>
            <p style="font-size: 13px; color: var(--text-muted); margin-top: 2px;">Daily Target: ${state.settings.dailyTargetMinutes || 180} Mins | Pace Velocity: Active</p>
          </div>
          <div style="display: flex; gap: 12px; align-items: center;">
            <div style="background: rgba(0,0,0,0.25); padding: 10px 16px; border-radius: var(--radius-md); text-align: center;">
              <span style="font-size: 22px; font-weight: 800; color: var(--accent-cream);">${daysRemainingText}</span>
              <span style="font-size: 10px; display: block; color: var(--text-muted); font-weight: 700;">DAYS LEFT</span>
            </div>
          </div>
        </div>
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

      <!-- Overall Syllabus Progress Bar -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">analytics</span> Syllabus Track</span>
          <span style="font-weight: 700; font-size: 14px; color: var(--primary);">${completedChapters} Completed (${overallCompletion}%)</span>
        </div>
        <div class="progress-bar-container" style="height: 12px;">
          <div class="progress-bar-fill" style="width: ${overallCompletion}%;"></div>
        </div>
      </div>

      <!-- Subject Breakdown Cards -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">school</span> Subjects Progress</span>
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

      <!-- Quick Action Grid -->
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
            <span class="material-symbols-outlined" style="color: var(--status-revision);">update</span> Revision
          </button>
        </div>
      </div>
    </div>
  `;
}

export function bindDashboardEvents(container) {
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
