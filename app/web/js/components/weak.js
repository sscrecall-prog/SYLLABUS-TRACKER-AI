/* Weak Topics Screen Component */

import { state } from '../state.js';

export function renderWeak() {
  const weakChapters = state.items.filter(i => 
    i.itemType === 'CHAPTER' && (i.status === 'WEAK' || (i.confidence && i.confidence <= 2))
  );

  return `
    <div class="weak-view">
      <div class="card" style="border-left: 4px solid var(--status-weak);">
        <h2 style="font-size: 18px; font-weight: 800; color: var(--status-weak); display: flex; align-items: center; gap: 8px;">
          <span class="material-symbols-outlined">report_problem</span> Weak Topics & Low Confidence Focus
        </h2>
        <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Topics flagged with Low Confidence (&le;2 Stars) or WEAK status requiring priority revision.</p>
      </div>

      <div class="card">
        <div style="display: flex; flex-direction: column; gap: 12px;">
          ${weakChapters.length === 0 ? `
            <div style="text-align: center; padding: 40px; color: var(--text-muted);">
              <span class="material-symbols-outlined" style="font-size: 48px; margin-bottom: 8px; color: var(--status-completed);">verified</span>
              <p style="font-weight: 600;">No weak topics flagged! Keep up the great understanding.</p>
            </div>
          ` : weakChapters.map(c => {
            const sub = state.subjects.find(s => s.id === c.subjectId);
            return `
              <div class="card" style="margin-bottom: 0; background: var(--surface-variant); display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap;">
                <div>
                  <div style="font-weight: 700; font-size: 15px;">${c.title}</div>
                  <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">
                    Subject: <strong>${sub ? sub.name : 'General'}</strong> &bull; Confidence: <strong>${'⭐'.repeat(c.confidence || 2)}</strong>
                  </div>
                </div>
                <button class="btn btn-primary resolve-weak-btn" data-id="${c.id}">
                  <span class="material-symbols-outlined">upgrade</span> Mark Improved
                </button>
              </div>
            `;
          }).join('')}
        </div>
      </div>
    </div>
  `;
}

export function bindWeakEvents(container) {
  container.querySelectorAll('.resolve-weak-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = parseInt(btn.dataset.id);
      state.updateItem(id, { status: 'IN_PROGRESS', confidence: 4 });
    });
  });
}
