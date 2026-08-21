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
    <div class="dashboard-view" style="display: flex; flex-direction: column; gap: 20px;">
      <!-- Hero Emerald Banner Card (Matching Screenshot) -->
      <div class="card" style="background: #0b664b; color: #ffffff; border: none; border-radius: 20px; padding: 24px;">
        <div style="margin-bottom: 16px;">
          <span style="font-size: 13px; color: rgba(255,255,255,0.85); font-weight: 600;">Good afternoon,</span>
          <h2 style="font-size: 28px; font-weight: 800; margin-top: 2px; color: #ffffff;">${state.settings.userName || 'Sunny'} 👋</h2>
        </div>

        <div style="display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap;">
          <div style="background: rgba(255,255,255,0.12); padding: 10px 16px; border-radius: 12px; display: flex; align-items: center; gap: 10px;">
            <span style="font-size: 22px;">🔥</span>
            <div>
              <span style="font-size: 18px; font-weight: 800; display: block; line-height: 1.1;">1</span>
              <span style="font-size: 10px; font-weight: 700; opacity: 0.8; letter-spacing: 0.5px;">DAY STREAK</span>
            </div>
          </div>

          <div style="background: rgba(255,255,255,0.12); padding: 10px 16px; border-radius: 12px; display: flex; align-items: center; gap: 10px;">
            <span style="font-size: 22px;">🎯</span>
            <div>
              <span style="font-size: 18px; font-weight: 800; display: block; line-height: 1.1;">60%</span>
              <span style="font-size: 10px; font-weight: 700; opacity: 0.8; letter-spacing: 0.5px;">ACCURACY</span>
            </div>
          </div>
        </div>

        <!-- Milestone Progress Line -->
        <div style="margin-bottom: 18px;">
          <div style="display: flex; justify-content: space-between; font-size: 11px; font-weight: 700; opacity: 0.85; margin-bottom: 6px;">
            <span>1 days</span>
            <span>10-day milestone</span>
          </div>
          <div style="height: 6px; background: rgba(255,255,255,0.2); border-radius: 99px; overflow: hidden;">
            <div style="width: 10%; height: 100%; background: #f59e0b; border-radius: 99px;"></div>
          </div>
        </div>

        <!-- Focus Area Tag -->
        <div style="display: inline-flex; align-items: center; gap: 8px; background: rgba(255,255,255,0.15); padding: 8px 14px; border-radius: 10px; font-size: 13px; font-weight: 700;">
          <span>⚠️ Focus area:</span>
          <span style="color: #ffffff;">One Word Substitution</span>
        </div>
      </div>

      <!-- Today's Targets Section -->
      <div>
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
          <h3 style="font-size: 18px; font-weight: 800;">Today's Targets</h3>
          <span style="font-size: 13px; font-weight: 700; color: var(--text-muted);">0/4 done</span>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px;">
          <!-- Target 1 -->
          <div class="card target-card-item" style="background: var(--surface-color); border: 1px solid var(--border-color); border-radius: 16px; padding: 18px; margin: 0; cursor: pointer;">
            <div style="width: 36px; height: 36px; background: #0b664b; color: #fff; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-weight: 900; font-size: 16px; margin-bottom: 12px;">
              R
            </div>
            <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 4px;">
              <span style="font-weight: 800; font-size: 15px;">RC Practice</span>
              <span style="background: #fef3c7; color: #d97706; font-size: 10px; font-weight: 900; padding: 1px 6px; border-radius: 4px;">PRO</span>
            </div>
            <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px;">2 passages</p>
            <span style="font-size: 13px; font-weight: 800; color: #0b664b;">Start →</span>
          </div>

          <!-- Target 2 -->
          <div class="card target-card-item" style="background: var(--surface-color); border: 1px solid var(--border-color); border-radius: 16px; padding: 18px; margin: 0; cursor: pointer;">
            <div style="width: 36px; height: 36px; background: #10b981; color: #fff; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-weight: 900; font-size: 16px; margin-bottom: 12px;">
              D
            </div>
            <div style="font-weight: 800; font-size: 15px; margin-bottom: 4px;">Daily Vocab</div>
            <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px;">Learn today's 20 key words</p>
            <span style="font-size: 13px; font-weight: 800; color: #10b981;">Start →</span>
          </div>

          <!-- Target 3 -->
          <div class="card target-card-item" style="background: var(--surface-color); border: 1px solid var(--border-color); border-radius: 16px; padding: 18px; margin: 0; cursor: pointer;">
            <div style="width: 36px; height: 36px; background: #0f766e; color: #fff; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-weight: 900; font-size: 16px; margin-bottom: 12px;">
              D
            </div>
            <div style="font-weight: 800; font-size: 15px; margin-bottom: 4px;">Daily Grammar Rule</div>
            <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px;">1 Rule</p>
            <span style="font-size: 13px; font-weight: 800; color: #0f766e;">Start →</span>
          </div>

          <!-- Target 4 -->
          <div class="card target-card-item" style="background: var(--surface-color); border: 1px solid var(--border-color); border-radius: 16px; padding: 18px; margin: 0; cursor: pointer;">
            <div style="width: 36px; height: 36px; background: #2563eb; color: #fff; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-weight: 900; font-size: 16px; margin-bottom: 12px;">
              S
            </div>
            <div style="font-weight: 800; font-size: 15px; margin-bottom: 4px;">Spellings Test</div>
            <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px;">50 Spellings test</p>
            <span style="font-size: 13px; font-weight: 800; color: #2563eb;">Start →</span>
          </div>
        </div>
      </div>

      <!-- Intense Revision Section -->
      <div>
        <h3 style="font-size: 18px; font-weight: 800; margin-bottom: 12px;">Intense Revision</h3>
        <div class="card" style="background: #852222; color: #ffffff; border: none; border-radius: 20px; padding: 24px;">
          <span style="font-size: 11px; font-weight: 800; opacity: 0.8; letter-spacing: 1px; text-transform: uppercase; display: block; margin-bottom: 8px;">SSC CGL - TIER - 1</span>
          <h2 style="font-size: 20px; font-weight: 900; color: #ffffff; margin-bottom: 6px; letter-spacing: -0.2px; line-height: 1.3;">30 DAYS INTENSE REVISION TARGETS - CGL PRE 2026</h2>
          <p style="font-size: 13px; opacity: 0.85; margin-bottom: 20px;">30-Day Intensive</p>

          <div style="display: flex; justify-content: space-between; font-size: 11px; font-weight: 700; opacity: 0.85; margin-bottom: 6px;">
            <span>0/30 days</span>
            <span>0%</span>
          </div>
          <div style="height: 6px; background: rgba(255,255,255,0.2); border-radius: 99px; overflow: hidden; margin-bottom: 20px;">
            <div style="width: 0%; height: 100%; background: #ffffff; border-radius: 99px;"></div>
          </div>

          <button class="btn quick-action-btn" data-nav="revision" style="background: rgba(255,255,255,0.2); color: #ffffff; border: none; border-radius: 99px; padding: 10px 22px; font-weight: 800; font-size: 13px; cursor: pointer;">
            Start now →
          </button>
        </div>
      </div>

      <!-- Stats & Subjects Breakdown -->
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
