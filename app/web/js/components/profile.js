/* Profile Screen Component */

import { state } from '../state.js';

export function renderProfile() {
  const badges = state.badges;
  const unlockedCount = badges.filter(b => b.isUnlocked).length;

  return `
    <div class="profile-view">
      <!-- User Profile Header Card -->
      <div class="card" style="text-align: center; padding: 32px;">
        <div style="font-size: 64px; margin-bottom: 8px;">${state.settings.userAvatarEmoji || '🎯'}</div>
        <h2 style="font-size: 22px; font-weight: 800;">Aspirant Profile</h2>
        <p style="font-size: 13px; color: var(--text-muted); margin-top: 2px;">Targeting: ${state.settings.targetExam || 'SSC CGL 2026'}</p>
        <div style="display: flex; gap: 12px; justify-content: center; margin-top: 16px;">
          <span class="streak-badge">🔥 Streak Active</span>
          <span class="badge badge-mastered">${unlockedCount} Badges Unlocked</span>
        </div>
      </div>

      <!-- Badges Showcase -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">military_tech</span> Achievement Badges</span>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; margin-top: 16px;">
          ${badges.map(b => `
            <div style="background: var(--surface-variant); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px; text-align: center; ${!b.isUnlocked ? 'opacity: 0.5;' : ''}">
              <div style="font-size: 32px; margin-bottom: 8px;">${b.isUnlocked ? '🏆' : '🔒'}</div>
              <strong style="display: block; font-size: 14px; margin-bottom: 4px;">${b.title}</strong>
              <span style="font-size: 12px; color: var(--text-muted); display: block;">${b.description}</span>
            </div>
          `).join('')}
        </div>
      </div>
    </div>
  `;
}

export function bindProfileEvents(container) {
  // Static rendering
}
