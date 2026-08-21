/* Revision Screen Component */

import { state } from '../state.js';

export function renderRevision() {
  const chapters = state.items.filter(i => i.itemType === 'CHAPTER');
  const now = Date.now();

  const dueToday = chapters.filter(c => c.nextRevisionTimestamp && c.nextRevisionTimestamp <= now);
  const upcoming = chapters.filter(c => c.nextRevisionTimestamp && c.nextRevisionTimestamp > now);

  return `
    <div class="revision-view">
      <!-- Header Summary Card -->
      <div class="card" style="background: linear-gradient(135deg, var(--surface-color) 0%, rgba(245, 158, 11, 0.15) 100%);">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
          <div>
            <h2 style="font-size: 20px; font-weight: 800; display: flex; align-items: center; gap: 8px;">
              <span class="material-symbols-outlined" style="color: var(--status-revision);">update</span> Spaced Repetition Revision
            </h2>
            <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Smart interval algorithm: 1d &rarr; 3d &rarr; 7d &rarr; 14d &rarr; 30d (5th revision marks Mastered)</p>
          </div>
          <div style="background: rgba(0,0,0,0.25); padding: 8px 16px; border-radius: var(--radius-md);">
            <span style="font-size: 22px; font-weight: 800; color: var(--status-revision);">${dueToday.length}</span>
            <span style="font-size: 11px; display: block; color: var(--text-muted); font-weight: 700;">DUE TODAY</span>
          </div>
        </div>
      </div>

      <!-- Revision Queue: Due Today -->
      <div class="card">
        <div class="card-header">
          <span class="card-title" style="color: var(--status-revision);"><span class="material-symbols-outlined">notification_important</span> Due For Revision Now (${dueToday.length})</span>
        </div>

        <div style="display: flex; flex-direction: column; gap: 12px; margin-top: 12px;">
          ${dueToday.length === 0 ? `
            <div style="text-align: center; padding: 30px; color: var(--text-muted);">
              <span class="material-symbols-outlined" style="font-size: 40px; margin-bottom: 8px; color: var(--status-completed);">task_alt</span>
              <p style="font-weight: 600;">All caught up! No revisions pending for today.</p>
            </div>
          ` : dueToday.map(c => {
            const sub = state.subjects.find(s => s.id === c.subjectId);
            return `
              <div class="card" style="margin-bottom: 0; background: var(--surface-variant); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                <div>
                  <div style="font-weight: 700; font-size: 15px;">${c.title}</div>
                  <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">
                    Subject: <strong style="color: ${sub ? sub.colorHex : 'var(--primary)'};">${sub ? sub.name : 'General'}</strong> &bull; Rev Count: <strong>${c.revisionCount || 0}/5</strong>
                  </div>
                </div>
                <button class="btn btn-primary mark-revised-btn" data-id="${c.id}">
                  <span class="material-symbols-outlined">check_circle</span> Mark Revised
                </button>
              </div>
            `;
          }).join('')}
        </div>
      </div>

      <!-- Upcoming Scheduled Revisions -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">event</span> Upcoming Scheduled Revisions (${upcoming.length})</span>
        </div>

        <div style="display: flex; flex-direction: column; gap: 10px; margin-top: 12px;">
          ${upcoming.length === 0 ? `
            <p style="color: var(--text-muted); font-size: 13px;">No upcoming revisions scheduled yet. Complete topics in Syllabus view to queue them up!</p>
          ` : upcoming.map(c => {
            const sub = state.subjects.find(s => s.id === c.subjectId);
            const dateStr = new Date(c.nextRevisionTimestamp).toLocaleDateString();
            return `
              <div style="background: var(--surface-variant); padding: 10px 14px; border-radius: var(--radius-md); display: flex; justify-content: space-between; align-items: center;">
                <div>
                  <span style="font-weight: 700; font-size: 14px;">${c.title}</span>
                  <span style="font-size: 11px; color: var(--text-muted); display: block;">${sub ? sub.name : 'General'}</span>
                </div>
                <span style="font-size: 12px; font-weight: 700; color: var(--primary);">Scheduled: ${dateStr}</span>
              </div>
            `;
          }).join('')}
        </div>
      </div>
    </div>
  `;
}

export function bindRevisionEvents(container) {
  container.querySelectorAll('.mark-revised-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = parseInt(btn.dataset.id);
      state.markChapterRevised(id);
    });
  });
}
