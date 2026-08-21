/* Analytics Screen Component */

import { state } from '../state.js';

export function renderAnalytics() {
  const subjects = state.subjects;
  
  return `
    <div class="analytics-view">
      <!-- Subject-wise Study & PYQ Performance -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">analytics</span> Subject-wise PYQ Accuracy</span>
        </div>

        <div style="display: flex; flex-direction: column; gap: 16px; margin-top: 16px;">
          ${subjects.map(s => {
            const subItems = state.items.filter(i => i.subjectId === s.id && i.itemType === 'CHAPTER');
            const totalAtt = subItems.reduce((sum, i) => sum + (i.pyqAttempted || 0), 0);
            const totalCorr = subItems.reduce((sum, i) => sum + (i.pyqCorrect || 0), 0);
            const accuracy = totalAtt > 0 ? Math.round((totalCorr / totalAtt) * 100) : 0;

            return `
              <div>
                <div style="display: flex; justify-content: space-between; font-size: 14px; font-weight: 700; margin-bottom: 4px;">
                  <span style="color: ${s.colorHex};">${s.name}</span>
                  <span>${totalCorr} / ${totalAtt} Correct (${accuracy}%)</span>
                </div>
                <div class="progress-bar-container" style="height: 10px;">
                  <div class="progress-bar-fill" style="width: ${accuracy}%; background-color: ${s.colorHex};"></div>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      </div>

      <!-- Velocity & Pace -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">speed</span> Exam Velocity & Required Pace</span>
        </div>
        <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 12px;">Based on target exam date (${state.settings.targetExamDateStr || '2026-10-01'}):</p>
        <div class="dashboard-grid" style="margin-bottom: 0;">
          <div style="background: var(--surface-variant); padding: 16px; border-radius: var(--radius-md);">
            <span style="font-size: 11px; color: var(--text-muted); font-weight: 700;">REQUIRED DAILY PACE</span>
            <div style="font-size: 20px; font-weight: 800; color: var(--primary);">0.8 Chapters / Day</div>
          </div>
          <div style="background: var(--surface-variant); padding: 16px; border-radius: var(--radius-md);">
            <span style="font-size: 11px; color: var(--text-muted); font-weight: 700;">ACTUAL CURRENT VELOCITY</span>
            <div style="font-size: 20px; font-weight: 800; color: var(--status-completed);">1.2 Chapters / Day</div>
          </div>
        </div>
      </div>
    </div>
  `;
}

export function bindAnalyticsEvents(container) {
  // Static visual rendering
}
