/* Goals Screen Component */

import { state } from '../state.js';

export function renderGoals() {
  const goals = state.goals;

  return `
    <div class="goals-view">
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">flag</span> Target Goals & Milestone Tracker</span>
          <button class="btn btn-primary" id="add-goal-btn">
            <span class="material-symbols-outlined">add</span> Create Goal
          </button>
        </div>
      </div>

      <div class="card">
        <div style="display: flex; flex-direction: column; gap: 12px;">
          ${goals.length === 0 ? `
            <div style="text-align: center; padding: 40px; color: var(--text-muted);">
              <span class="material-symbols-outlined" style="font-size: 48px; margin-bottom: 8px;">flag</span>
              <p style="font-weight: 600;">No milestone goals set yet.</p>
            </div>
          ` : goals.map(g => `
            <div class="card" style="margin-bottom: 0; background: var(--surface-variant);">
              <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; margin-bottom: 8px;">
                <h3 style="font-size: 16px; font-weight: 700;">${g.title}</h3>
                <button class="btn btn-danger delete-goal-btn" data-id="${g.id}" style="padding: 4px 8px;">Delete</button>
              </div>
              <div style="font-size: 13px; color: var(--text-muted); margin-bottom: 8px;">
                Subject: <strong>${g.subjectName || 'General'}</strong> &bull; Target Date: <strong>${g.targetDateStr}</strong>
              </div>
              <div style="font-size: 12px; color: var(--text-muted);">
                Target Chapters: ${g.targetChaptersCount || 10} | Target Study Hours: ${g.targetStudyHours || 50} hrs
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    </div>
  `;
}

export function bindGoalsEvents(container, openModal) {
  const addBtn = container.querySelector('#add-goal-btn');
  if (addBtn) {
    addBtn.addEventListener('click', () => {
      openModal('Create Goal', `
        <form id="modal-add-goal-form">
          <div class="form-group">
            <label>Goal Title</label>
            <input type="text" id="goal-title-input" class="form-control" placeholder="e.g. Master English Vocabulary" required />
          </div>
          <div class="form-group">
            <label>Subject</label>
            <select id="goal-sub-select" class="form-control">
              ${state.subjects.map(s => `<option value="${s.id}">${s.name}</option>`).join('')}
            </select>
          </div>
          <div class="form-group">
            <label>Target Completion Date</label>
            <input type="date" id="goal-date-input" class="form-control" value="2026-09-30" required />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; margin-top: 12px;">Create Goal</button>
        </form>
      `, (modalBody) => {
        const form = modalBody.querySelector('#modal-add-goal-form');
        form.addEventListener('submit', (e) => {
          e.preventDefault();
          const subId = parseInt(modalBody.querySelector('#goal-sub-select').value);
          const sub = state.subjects.find(s => s.id === subId);
          state.addGoal({
            title: modalBody.querySelector('#goal-title-input').value,
            subjectId: subId,
            subjectName: sub ? sub.name : 'General',
            targetDateStr: modalBody.querySelector('#goal-date-input').value
          });
        });
      });
    });
  }

  container.querySelectorAll('.delete-goal-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = parseInt(btn.dataset.id);
      state.deleteGoal(id);
    });
  });
}
