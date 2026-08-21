/* Mock Tests Screen Component */

import { state } from '../state.js';

let platformFilter = 'ALL';
let typeFilter = 'ALL';
let searchQuery = '';

export function renderMockTests() {
  const tests = state.mockTests || [];
  
  // Calculate distinct platforms available in data
  const basePlatforms = ['Testbook', 'Oliveboard', 'PracticeMock', 'Gradeup', 'Unacademy'];
  const customPlatforms = Array.from(new Set(tests.map(t => t.testPlatform).filter(Boolean)))
    .filter(p => !basePlatforms.includes(p));
  const allPlatformsList = ['ALL', ...basePlatforms, ...customPlatforms];

  const allTypesList = ['ALL', 'Full Length', 'Sectional', 'Chapter Test', 'PYQ Paper'];

  // Filtered list
  const filteredTests = tests.filter(t => {
    const matchesPlatform = platformFilter === 'ALL' || (t.testPlatform || 'Testbook') === platformFilter;
    const matchesType = typeFilter === 'ALL' || (t.testType || 'Full Length') === typeFilter;
    const matchesSearch = !searchQuery || (t.testName || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
                          (t.testPlatform || '').toLowerCase().includes(searchQuery.toLowerCase());
    return matchesPlatform && matchesType && matchesSearch;
  });

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

      <!-- Test Platform & Type Filters -->
      <div class="card" style="margin-bottom: 16px;">
        <div style="display: flex; flex-direction: column; gap: 12px;">
          <!-- Search Bar -->
          <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
            <div style="position: relative; flex: 1; min-width: 200px;">
              <input type="text" id="mock-search-input" class="form-control" placeholder="Search mock test by name or platform..." value="${searchQuery}" style="padding-left: 36px;" />
              <span class="material-symbols-outlined" style="position: absolute; left: 10px; top: 50%; transform: translateY(-50%); font-size: 18px; color: var(--text-muted);">search</span>
            </div>
            <button class="btn btn-primary" id="add-mock-btn" style="white-space: nowrap;">
              <span class="material-symbols-outlined">add</span> Record Mock Test
            </button>
          </div>

          <!-- Platform Filter Chips -->
          <div>
            <span style="font-size: 11px; font-weight: 800; color: var(--text-muted); letter-spacing: 0.5px; text-transform: uppercase; display: block; margin-bottom: 6px;">MOCK PLATFORM</span>
            <div style="display: flex; gap: 8px; flex-wrap: wrap;">
              ${allPlatformsList.map(p => `
                <button class="btn ${platformFilter === p ? 'btn-primary' : 'btn-secondary'} platform-chip" data-platform="${p}" style="padding: 4px 12px; font-size: 12px; border-radius: 99px;">
                  ${p === 'ALL' ? '🌐 All Platforms' : p}
                </button>
              `).join('')}
            </div>
          </div>

          <!-- Type Filter Chips -->
          <div>
            <span style="font-size: 11px; font-weight: 800; color: var(--text-muted); letter-spacing: 0.5px; text-transform: uppercase; display: block; margin-bottom: 6px;">TEST TYPE</span>
            <div style="display: flex; gap: 8px; flex-wrap: wrap;">
              ${allTypesList.map(t => `
                <button class="btn ${typeFilter === t ? 'btn-primary' : 'btn-secondary'} type-chip" data-type="${t}" style="padding: 4px 12px; font-size: 12px; border-radius: 99px;">
                  ${t === 'ALL' ? '📋 All Types' : t}
                </button>
              `).join('')}
            </div>
          </div>
        </div>
      </div>

      <!-- Test Log -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">history</span> Mock Test History (${filteredTests.length})</span>
        </div>

        <div style="overflow-x: auto; margin-top: 8px;">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Test Name & Date</th>
                <th>Platform</th>
                <th>Type</th>
                <th>Score Scored</th>
                <th>Cutoff</th>
                <th>Accuracy</th>
                <th>Percentile</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${filteredTests.length === 0 ? `
                <tr>
                  <td colspan="8" style="text-align: center; padding: 30px; color: var(--text-muted);">
                    No mock tests found matching criteria.
                  </td>
                </tr>
              ` : filteredTests.map(t => {
                const cleared = (t.marksScored || 0) >= (t.cutoffMarks || 0);
                const platformName = t.testPlatform || 'Testbook';
                const testType = t.testType || 'Full Length';

                return `
                  <tr>
                    <td>
                      <strong style="display: block; font-size: 14px;">${t.testName}</strong>
                      <span style="font-size: 11px; color: var(--text-muted);">${t.testDateStr || 'Today'}</span>
                    </td>
                    <td>
                      <span class="badge" style="background: rgba(11, 102, 75, 0.15); color: var(--primary); font-weight: 800;">
                        ${platformName}
                      </span>
                    </td>
                    <td>
                      <span class="badge" style="background: rgba(59, 130, 246, 0.15); color: #2563eb; font-weight: 700;">
                        ${testType}
                      </span>
                    </td>
                    <td><strong style="font-size: 15px; color: ${cleared ? 'var(--status-completed)' : 'var(--status-weak)'};">${t.marksScored} / ${t.totalMarks || 200}</strong></td>
                    <td>${t.cutoffMarks || 135}</td>
                    <td>${t.accuracy || 0}%</td>
                    <td><span class="badge badge-mastered">${t.percentile || 0}%ile</span></td>
                    <td>
                      <button class="btn btn-danger delete-mock-btn" data-id="${t.id}" style="padding: 4px 8px; font-size: 12px;">Delete</button>
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
  // Search event
  const searchInput = container.querySelector('#mock-search-input');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      searchQuery = e.target.value;
      state.notify();
    });
  }

  // Platform chips
  container.querySelectorAll('.platform-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      platformFilter = chip.dataset.platform;
      state.notify();
    });
  });

  // Type chips
  container.querySelectorAll('.type-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      typeFilter = chip.dataset.type;
      state.notify();
    });
  });

  // Add mock test
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

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div class="form-group">
              <label>Mock Platform</label>
              <select id="mock-platform-select" class="form-control">
                <option value="Testbook">Testbook</option>
                <option value="Oliveboard">Oliveboard</option>
                <option value="PracticeMock">PracticeMock</option>
                <option value="Gradeup">Gradeup (BYJU'S)</option>
                <option value="Unacademy">Unacademy</option>
                <option value="Custom">Other / Custom</option>
              </select>
            </div>

            <div class="form-group">
              <label>Test Type</label>
              <select id="mock-type-select" class="form-control">
                <option value="Full Length">Full Length</option>
                <option value="Sectional">Sectional</option>
                <option value="Chapter Test">Chapter Test</option>
                <option value="PYQ Paper">PYQ Paper</option>
              </select>
            </div>
          </div>

          <div class="form-group" id="custom-platform-container" style="display: none;">
            <label>Custom Platform Name</label>
            <input type="text" id="mock-custom-platform-input" class="form-control" placeholder="e.g. RBE / SuperProfs" />
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

          <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; margin-top: 12px;">Save Mock Test</button>
        </form>
      `, (modalBody) => {
        const platformSelect = modalBody.querySelector('#mock-platform-select');
        const customContainer = modalBody.querySelector('#custom-platform-container');

        if (platformSelect && customContainer) {
          platformSelect.addEventListener('change', () => {
            if (platformSelect.value === 'Custom') {
              customContainer.style.display = 'block';
            } else {
              customContainer.style.display = 'none';
            }
          });
        }

        const form = modalBody.querySelector('#modal-add-mock-form');
        form.addEventListener('submit', (e) => {
          e.preventDefault();

          let selectedPlatform = platformSelect.value;
          if (selectedPlatform === 'Custom') {
            const customVal = modalBody.querySelector('#mock-custom-platform-input').value.trim();
            selectedPlatform = customVal || 'Custom';
          }

          const selectedType = modalBody.querySelector('#mock-type-select').value;

          state.addMockTest({
            testName: modalBody.querySelector('#mock-name-input').value,
            testPlatform: selectedPlatform,
            testType: selectedType,
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

  // Delete event
  container.querySelectorAll('.delete-mock-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = parseInt(btn.dataset.id);
      if (confirm('Delete mock test entry?')) {
        state.deleteMockTest(id);
      }
    });
  });
}
