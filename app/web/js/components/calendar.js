/* Calendar Screen Component */

import { state } from '../state.js';

export function renderCalendar() {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();
  const monthName = today.toLocaleString('default', { month: 'long' });

  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDayIndex = new Date(year, month, 1).getDay();

  return `
    <div class="calendar-view">
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">event</span> ${monthName} ${year} Calendar</span>
        </div>

        <div style="display: grid; grid-template-columns: repeat(7, 1fr); gap: 8px; text-align: center; margin-top: 16px;">
          ${['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(d => `
            <div style="font-weight: 700; font-size: 12px; color: var(--text-muted); padding: 8px;">${d}</div>
          `).join('')}

          ${Array(firstDayIndex).fill(0).map(() => `<div></div>`).join('')}

          ${Array(daysInMonth).fill(0).map((_, i) => {
            const dayNum = i + 1;
            const isToday = dayNum === today.getDate();
            return `
              <div style="background: ${isToday ? 'var(--primary-container)' : 'var(--surface-variant)'}; border: 1px solid ${isToday ? 'var(--primary)' : 'var(--border-color)'}; border-radius: var(--radius-md); padding: 12px; font-weight: 700; font-size: 14px; min-height: 60px; display: flex; flex-direction: column; justify-content: space-between;">
                <span>${dayNum}</span>
                ${isToday ? `<span style="font-size: 9px; color: var(--primary); font-weight: 800;">TODAY</span>` : ''}
              </div>
            `;
          }).join('')}
        </div>
      </div>
    </div>
  `;
}

export function bindCalendarEvents(container) {
  // Static calendar rendering
}
