/* Mistakes Screen Component (Digital Error Diary) */

import { state } from '../state.js';

export function renderMistakes() {
  const mistakes = state.mistakes;
  const activeCount = mistakes.filter(m => m.resolutionStatus === 'ACTIVE').length;
  const understoodCount = mistakes.filter(m => m.resolutionStatus === 'UNDERSTOOD').length;
  const masteredCount = mistakes.filter(m => m.resolutionStatus === 'MASTERED').length;
  
  const totalCount = mistakes.length;
  const resolutionRate = totalCount > 0 ? Math.round(((understoodCount + masteredCount) / totalCount) * 100) : 0;

  return `
    <div class="mistakes-view">
      <!-- Error Diary Header Metrics -->
      <div class="dashboard-grid">
        <div class="stats-card">
          <div class="stats-icon" style="background: rgba(239, 68, 68, 0.15); color: var(--status-weak);">
            <span class="material-symbols-outlined">bookmark_remove</span>
          </div>
          <div class="stats-info">
            <span class="stats-value">${totalCount}</span>
            <span class="stats-label">Total Error Entries</span>
          </div>
        </div>

        <div class="stats-card">
          <div class="stats-icon" style="background: rgba(245, 158, 11, 0.15); color: var(--status-revision);">
            <span class="material-symbols-outlined">pending</span>
          </div>
          <div class="stats-info">
            <span class="stats-value" style="color: var(--status-revision);">${activeCount}</span>
            <span class="stats-label">Active / Pending Review</span>
          </div>
        </div>

        <div class="stats-card">
          <div class="stats-icon" style="background: rgba(16, 185, 129, 0.15); color: var(--status-completed);">
            <span class="material-symbols-outlined">task_alt</span>
          </div>
          <div class="stats-info">
            <span class="stats-value" style="color: var(--status-completed);">${resolutionRate}%</span>
            <span class="stats-label">Resolution Rate</span>
          </div>
        </div>
      </div>

      <!-- Log New Mistake Card Header -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">edit_note</span> Error Log Entries</span>
          <button class="btn btn-primary" id="add-mistake-btn">
            <span class="material-symbols-outlined">add</span> Log New Error
          </button>
        </div>

        <!-- Mistake List -->
        <div style="display: flex; flex-direction: column; gap: 12px; margin-top: 16px;">
          ${mistakes.length === 0 ? `
            <div style="text-align: center; padding: 40px; color: var(--text-muted);">
              <span class="material-symbols-outlined" style="font-size: 48px; margin-bottom: 8px;">verified</span>
              <p style="font-weight: 600;">No mistakes recorded yet. Clean streak!</p>
            </div>
          ` : mistakes.map(m => `
            <div class="card" style="margin-bottom: 0; background: var(--surface-variant);">
              <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; margin-bottom: 8px;">
                <div style="font-weight: 700; font-size: 15px;">${m.questionText}</div>
                <span class="badge ${m.resolutionStatus === 'ACTIVE' ? 'badge-weak' : m.resolutionStatus === 'UNDERSTOOD' ? 'badge-in-progress' : 'badge-completed'}">${m.resolutionStatus}</span>
              </div>
              <div style="font-size: 13px; color: var(--text-muted); margin-bottom: 8px;">
                <strong>${m.subjectName || 'General'}</strong> &bull; Category: <span style="color: var(--status-revision); font-weight: 600;">${m.category}</span>
              </div>
              ${m.explanation ? `<div style="background: var(--surface-color); padding: 10px; border-radius: var(--radius-sm); font-size: 13px; margin-bottom: 8px;">💡 ${m.explanation}</div>` : ''}
              <div style="display: flex; gap: 8px; justify-content: flex-end;">
                ${m.resolutionStatus === 'ACTIVE' ? `
                  <button class="btn btn-secondary mark-mistake-status-btn" data-id="${m.id}" data-status="UNDERSTOOD">Mark Understood</button>
                  <button class="btn btn-primary mark-mistake-status-btn" data-id="${m.id}" data-status="MASTERED">Mark Mastered</button>
                ` : ''}
                <button class="btn btn-danger delete-mistake-btn" data-id="${m.id}">Delete</button>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    </div>
  `;
}

export function bindMistakesEvents(container, openModal) {
  const addBtn = container.querySelector('#add-mistake-btn');
  if (addBtn) {
    addBtn.addEventListener('click', () => {
      openModal('Log Question Error', `
        <form id="modal-add-mistake-form">
          <div class="form-group">
            <label>Question Text / Description</label>

            <textarea id="mistake-question-input" class="form-control" rows="3" placeholder="Paste or type question snippet..." required></textarea>
          </div>
          <div class="form-group">
            <label>Subject</label>
            <select id="mistake-sub-select" class="form-control">
              ${state.subjects.map(s => `<option value="${s.id}">${s.name}</option>`).join('')}
            </select>
          </div>
          <div class="form-group">
            <label>Error Category</label>
            <select id="mistake-cat-select" class="form-control">
              <option value="CALCULATION_ERROR">CALCULATION ERROR</option>
              <option value="CONCEPT_GAP">CONCEPT GAP</option>
              <option value="SILLY_MISTAKE">SILLY MISTAKE</option>
              <option value="TIME_MANAGEMENT">TIME MANAGEMENT</option>
              <option value="FORMULA_FORGOT">FORMULA FORGOT</option>
              <option value="READING_MISTAKE">READING MISTAKE</option>
            </select>
          </div>
          <div class="form-group">
            <label>Explanation / Correct Concept</label>
            <textarea id="mistake-exp-input" class="form-control" rows="2" placeholder="Why was it wrong and what is the correct formula/concept?"></textarea>
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; margin-top: 12px;">Log Error</button>
        </form>
      `, (modalBody) => {
        const form = modalBody.querySelector('#modal-add-mistake-form');
        form.addEventListener('submit', (e) => {
          e.preventDefault();
          const subId = parseInt(modalBody.querySelector('#mistake-sub-select').value);
          const sub = state.subjects.find(s => s.id === subId);
          state.addMistake({
            questionText: modalBody.querySelector('#mistake-question-input').value,
            subjectId: subId,
            subjectName: sub ? sub.name : 'General',
            category: modalBody.querySelector('#mistake-cat-select').value,
            explanation: modalBody.querySelector('#mistake-exp-input').value
          });
        });
      });
    });
  }

  container.querySelectorAll('.mark-mistake-status-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = parseInt(btn.dataset.id);
      const status = btn.dataset.status;
      state.updateMistake(id, { resolutionStatus: status });
    });
  });

  container.querySelectorAll('.delete-mistake-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = parseInt(btn.dataset.id);
      if (confirm('Delete mistake entry?')) {
        state.deleteMistake(id);
      }
    });
  });
}
