/* Planner Screen Component */

import { state } from '../state.js';

export function renderPlanner() {
  const plans = state.plans;
  const completedPlans = plans.filter(p => p.isCompleted).length;

  return `
    <div class="planner-view">
      <!-- Header Summary Card -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">calendar_month</span> Study Schedule & Planner</span>
          <button class="btn btn-primary" id="add-plan-btn">
            <span class="material-symbols-outlined">add</span> Schedule Task
          </button>
        </div>
        <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Organize daily study time slots, topics, and duration goals.</p>
      </div>

      <!-- Plan Tasks List -->
      <div class="card">
        <div style="display: flex; flex-direction: column; gap: 12px;">
          ${plans.length === 0 ? `
            <div style="text-align: center; padding: 40px; color: var(--text-muted);">
              <span class="material-symbols-outlined" style="font-size: 48px; margin-bottom: 8px;">event_available</span>
              <p style="font-weight: 600;">No planned study tasks for today.</p>
            </div>
          ` : plans.map(p => `
            <div class="card" style="margin-bottom: 0; background: var(--surface-variant); display: flex; justify-content: space-between; align-items: center; gap: 12px; ${p.isCompleted ? 'opacity: 0.6;' : ''}">
              <div style="display: flex; align-items: center; gap: 12px;">
                <input type="checkbox" class="toggle-plan-check" data-id="${p.id}" ${p.isCompleted ? 'checked' : ''} style="width: 20px; height: 20px; cursor: pointer;" />
                <div>
                  <div style="font-weight: 700; font-size: 15px; ${p.isCompleted ? 'text-decoration: line-through;' : ''}">${p.chapterTitle || 'Study Session'}</div>
                  <div style="font-size: 12px; color: var(--text-muted);">
                    Subject: <strong>${p.subjectName || 'General'}</strong> &bull; Time: <strong>${p.timeSlotStr || 'Anytime'}</strong> (${p.durationMinutes || 60} mins)
                  </div>
                </div>
              </div>
              <button class="btn btn-danger delete-plan-btn" data-id="${p.id}" style="padding: 4px 8px;">Delete</button>
            </div>
          `).join('')}
        </div>
      </div>
    </div>
  `;
}

export function bindPlannerEvents(container, openModal) {
  const addBtn = container.querySelector('#add-plan-btn');
  if (addBtn) {
    addBtn.addEventListener('click', () => {
      const todayStr = new Date().toISOString().split('T')[0];
      openModal('Schedule Study Task', `
        <form id="modal-add-plan-form">
          <div class="form-group">
            <label>Subject</label>
            <select id="plan-sub-select" class="form-control">
              ${state.subjects.map(s => `<option value="${s.id}">${s.name}</option>`).join('')}
            </select>
          </div>
          <div class="form-group">
            <label>Chapter / Topic Title</label>
            <input type="text" id="plan-title-input" class="form-control" placeholder="e.g. Percentage Basics" required />
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div class="form-group">
              <label>Date</label>
              <input type="date" id="plan-date-input" class="form-control" value="${todayStr}" required />
            </div>
            <div class="form-group">
              <label>Time Slot</label>
              <input type="text" id="plan-time-input" class="form-control" placeholder="e.g. 09:00 AM - 10:30 AM" />
            </div>
          </div>
          <div class="form-group">
            <label>Planned Duration (Minutes)</label>
            <input type="number" id="plan-dur-input" class="form-control" value="60" min="15" step="15" required />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; margin-top: 12px;">Save Task</button>
        </form>
      `, (modalBody) => {
        const form = modalBody.querySelector('#modal-add-plan-form');
        form.addEventListener('submit', (e) => {
          e.preventDefault();
          const subId = parseInt(modalBody.querySelector('#plan-sub-select').value);
          const sub = state.subjects.find(s => s.id === subId);
          state.addPlan({
            dateStr: modalBody.querySelector('#plan-date-input').value,
            timeSlotStr: modalBody.querySelector('#plan-time-input').value,
            subjectId: subId,
            subjectName: sub ? sub.name : 'General',
            chapterTitle: modalBody.querySelector('#plan-title-input').value,
            durationMinutes: parseInt(modalBody.querySelector('#plan-dur-input').value) || 60
          });
        });
      });
    });
  }

  container.querySelectorAll('.toggle-plan-check').forEach(chk => {
    chk.addEventListener('change', () => {
      const id = parseInt(chk.dataset.id);
      state.togglePlanCompleted(id);
    });
  });

  container.querySelectorAll('.delete-plan-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = parseInt(btn.dataset.id);
      state.deletePlan(id);
    });
  });
}
