/* Mock Tests Screen Component */

import { state } from '../state.js';

export function renderMockTests() {
  const tests = state.mockTests;
  const totalCount = tests.length;
  
  let avgScore = 0;
  let highestScore = 0;
  let clearedCutoffCount = 0;
  let avgPercentile = 0;

  if (totalCount > 0) {
    const totalScore = tests.reduce((sum, t) => sum + (t.marksScored || 0), 0);
    avgScore = (totalScore / totalCount).toFixed(1);
    highestScore = Math.max(...tests.map(t => t.marksScored || 0));
    clearedCutoffCount = tests.filter(t => (t.marksScored || 0) >= (t.cutoffMarks || 0)).length;
    
    const totalPctile = tests.reduce((sum, t) => sum + (t.percentile || 0), 0);
    avgPercentile = (totalPctile / totalCount).toFixed(1);
  }

  const clearanceRate = totalCount > 0 ? Math.round((clearedCutoffCount / totalCount) * 100) : 0;

  return `
    <div class="mocktests-view">
      <!-- Performance Analytics Cards -->
      <div class="dashboard-grid">
        <div class="stats-card">
          <div class="stats-icon" style="background: rgba(59, 130, 246, 0.15); color: var(--status-in-progress);">
            <span class="material-symbols-outlined">quiz</span>
          </div>
          <div class="stats-info">
            <span class="stats-value">${totalCount}</span>
            <span class="stats-label">Mock Tests Attempted</span>
          </div>
        </div>

        <div class="stats-card">
          <div class="stats-icon" style="background: rgba(16, 185, 129, 0.15); color: var(--status-completed);">
            <span class="material-symbols-outlined">trending_up</span>
          </div>
          <div class="stats-info">
            <span class="stats-value" style="color: var(--status-completed);">${avgScore}</span>
            <span class="stats-label">Average Score (Best: ${highestScore})</span>
          </div>
        </div>

        <div class="stats-card">
          <div class="stats-icon" style="background: rgba(139, 92, 246, 0.15); color: var(--status-mastered);">
            <span class="material-symbols-outlined">military_tech</span>
          </div>
          <div class="stats-info">
            <span class="stats-value" style="color: var(--status-mastered);">${avgPercentile}%</span>
            <span class="stats-label">Avg Percentile (${clearanceRate}% Cutoff Cleared)</span>
          </div>
        </div>
      </div>

      <!-- Test Log -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">history</span> Mock Test History</span>
          <button class="btn btn-primary" id="add-mock-btn">
            <span class="material-symbols-outlined">add</span> Record Mock Test
          </button>
        </div>

        <div style="overflow-x: auto; margin-top: 16px;">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Test Name & Date</th>
                <th>Score Scored</th>
                <th>Cutoff</th>
                <th>Accuracy</th>
                <th>Percentile</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${tests.length === 0 ? `
                <tr>
                  <td colspan="6" style="text-align: center; padding: 30px; color: var(--text-muted);">
                    No mock tests recorded yet.
                  </td>
                </tr>
              ` : tests.map(t => {
                const cleared = (t.marksScored || 0) >= (t.cutoffMarks || 0);
                return `
                  <tr>
                    <td>
                      <strong style="display: block;">${t.testName}</strong>
                      <span style="font-size: 11px; color: var(--text-muted);">${t.testDateStr || 'Today'}</span>
                    </td>
                    <td><strong style="font-size: 15px; color: ${cleared ? 'var(--status-completed)' : 'var(--status-weak)'};">${t.marksScored} / ${t.totalMarks || 200}</strong></td>
                    <td>${t.cutoffMarks || 130}</td>
                    <td>${t.accuracy || 0}%</td>
                    <td><span class="badge badge-mastered">${t.percentile || 0}%ile</span></td>
                    <td>
                      <button class="btn btn-danger delete-mock-btn" data-id="${t.id}" style="padding: 4px 8px;">Delete</button>
                    </td>
                  </tr>
                `;
              }).join('')}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `;
}

export function bindMockTestsEvents(container, openModal) {
  const addBtn = container.querySelector('#add-mock-btn');
  if (addBtn) {
    addBtn.addEventListener('click', () => {
      const todayStr = new Date().toISOString().split('T')[0];
      openModal('Record Mock Test', `
        <form id="modal-add-mock-form">
          <div class="form-group">
            <label>Test Name</label>
            <input type="text" id="mock-name-input" class="form-control" placeholder="e.g. SSC CGL Full Test 05" required />
          </div>
          <div class="form-group">
            <label>Date</label>
            <input type="date" id="mock-date-input" class="form-control" value="${todayStr}" required />
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div class="form-group">
              <label>Marks Scored</label>
              <input type="number" step="0.5" id="mock-score-input" class="form-control" placeholder="145" required />
            </div>
            <div class="form-group">
              <label>Total Marks</label>
              <input type="number" id="mock-total-input" class="form-control" value="200" required />
            </div>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div class="form-group">
              <label>Cutoff Marks</label>
              <input type="number" id="mock-cutoff-input" class="form-control" value="135" />
            </div>
            <div class="form-group">
              <label>Percentile (%)</label>
              <input type="number" step="0.1" id="mock-pctile-input" class="form-control" placeholder="90.5" />
            </div>
          </div>
          <div class="form-group">
            <label>Accuracy (%)</label>
            <input type="number" step="0.1" id="mock-acc-input" class="form-control" placeholder="88.0" />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; margin-top: 12px;">Save Test</button>
        </form>
      `, (modalBody) => {
        const form = modalBody.querySelector('#modal-add-mock-form');
        form.addEventListener('submit', (e) => {
          e.preventDefault();
          state.addMockTest({
            testName: modalBody.querySelector('#mock-name-input').value,
            testDateStr: modalBody.querySelector('#mock-date-input').value,
            marksScored: parseFloat(modalBody.querySelector('#mock-score-input').value) || 0,
            totalMarks: parseFloat(modalBody.querySelector('#mock-total-input').value) || 200,
            cutoffMarks: parseFloat(modalBody.querySelector('#mock-cutoff-input').value) || 135,
            percentile: parseFloat(modalBody.querySelector('#mock-pctile-input').value) || 0,
            accuracy: parseFloat(modalBody.querySelector('#mock-acc-input').value) || 0
          });
        });
      });
    });
  }

  container.querySelectorAll('.delete-mock-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = parseInt(btn.dataset.id);
      if (confirm('Delete mock test entry?')) {
        state.deleteMockTest(id);
      }
    });
  });
}
