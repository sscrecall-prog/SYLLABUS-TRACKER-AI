/* Main Application Controller & Router */

import { state } from './state.js';
import { renderDashboard, bindDashboardEvents } from './components/dashboard.js';
import { renderSyllabus, bindSyllabusEvents } from './components/syllabus.js';
import { renderMistakes, bindMistakesEvents } from './components/mistakes.js';
import { renderMockTests, bindMockTestsEvents } from './components/mocktests.js';
import { renderRevision, bindRevisionEvents } from './components/revision.js';
import { renderPlanner, bindPlannerEvents } from './components/planner.js';
import { renderTimer, bindTimerEvents } from './components/timer.js';
import { renderAnalytics, bindAnalyticsEvents } from './components/analytics.js';
import { renderWeak, bindWeakEvents } from './components/weak.js';
import { renderGoals, bindGoalsEvents } from './components/goals.js';
import { renderProfile, bindProfileEvents } from './components/profile.js';
import { renderSettings, bindSettingsEvents } from './components/settings.js';
import { renderCalendar, bindCalendarEvents } from './components/calendar.js';

const NAV_ITEMS = [
  { id: 'dashboard', label: 'Home', icon: 'dashboard' },
  { id: 'syllabus', label: 'Syllabus', icon: 'auto_stories' },
  { id: 'mistakes', label: 'Error Diary', icon: 'bookmark_remove' },
  { id: 'mocktests', label: 'Mock Tests', icon: 'quiz' },
  { id: 'revision', label: 'Revision', icon: 'update' },
  { id: 'planner', label: 'Planner', icon: 'calendar_month' },
  { id: 'analytics', label: 'Analytics', icon: 'analytics' },
  { id: 'weak', label: 'Weak Topics', icon: 'report_problem' },
  { id: 'goals', label: 'Goals', icon: 'flag' },
  { id: 'timer', label: 'Timer', icon: 'timer' },
  { id: 'calendar', label: 'Calendar', icon: 'event' },
  { id: 'profile', label: 'Profile', icon: 'military_tech' },
  { id: 'settings', label: 'Settings', icon: 'settings' }
];

document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  renderNavigation();
  renderCurrentScreen();

  // Subscribe to state updates for automatic re-rendering
  state.subscribe(() => {
    renderNavigation();
    renderCurrentScreen();
  });

  // Global Event Binds
  bindHeaderAndNavEvents();
  bindFabAndModal();
});

/* Theme Handling */
function initTheme() {
  const currentTheme = localStorage.getItem('web_theme') || 'warm';
  setTheme(currentTheme);
}

function setTheme(theme) {
  const validThemes = ['light', 'warm', 'dark'];
  const newTheme = validThemes.includes(theme) ? theme : 'warm';
  document.documentElement.setAttribute('data-theme', newTheme);
  localStorage.setItem('web_theme', newTheme);

  document.querySelectorAll('.theme-swatch').forEach(swatch => {
    if (swatch.dataset.themeVal === newTheme) {
      swatch.classList.add('active');
    } else {
      swatch.classList.remove('active');
    }
  });

  const icon = document.getElementById('theme-icon');
  if (icon) {
    if (newTheme === 'dark') icon.textContent = 'dark_mode';
    else if (newTheme === 'warm') icon.textContent = 'palette';
    else icon.textContent = 'light_mode';
  }
}

function toggleTheme() {
  const current = document.documentElement.getAttribute('data-theme') || 'warm';
  const cycle = { 'dark': 'light', 'light': 'warm', 'warm': 'dark' };
  setTheme(cycle[current] || 'warm');
}

/* Sidebar & Bottom Nav Renderer */
function renderNavigation() {
  const sidebarNav = document.getElementById('sidebar-nav');
  const bottomNav = document.getElementById('bottom-nav');

  if (sidebarNav) {
    sidebarNav.innerHTML = NAV_ITEMS.map(item => `
      <button class="nav-item ${state.currentNav === item.id ? 'active' : ''}" data-nav="${item.id}">
        <span class="material-symbols-outlined">${item.icon}</span>
        <span>${item.label}</span>
      </button>
    `).join('');

    sidebarNav.querySelectorAll('.nav-item').forEach(btn => {
      btn.addEventListener('click', () => {
        state.setNav(btn.dataset.nav);
        document.getElementById('sidebar').classList.remove('mobile-open');
      });
    });
  }

  if (bottomNav) {
    // Mobile bottom nav top 5 destinations
    const mobileItems = NAV_ITEMS.slice(0, 5);
    bottomNav.innerHTML = mobileItems.map(item => `
      <button class="bottom-nav-item ${state.currentNav === item.id ? 'active' : ''}" data-nav="${item.id}">
        <span class="material-symbols-outlined">${item.icon}</span>
        <span>${item.label}</span>
      </button>
    `).join('');

    bottomNav.querySelectorAll('.bottom-nav-item').forEach(btn => {
      btn.addEventListener('click', () => {
        state.setNav(btn.dataset.nav);
      });
    });
  }
}

/* Screen Switcher */
function renderCurrentScreen() {
  const contentArea = document.getElementById('content-area');
  const pageTitle = document.getElementById('page-title');
  const pageSubtitle = document.getElementById('page-subtitle');

  const navObj = NAV_ITEMS.find(n => n.id === state.currentNav) || NAV_ITEMS[0];
  if (pageTitle) pageTitle.textContent = navObj.label;
  if (pageSubtitle) pageSubtitle.textContent = state.settings.targetExam || 'SSC CGL 2026';

  let html = '';
  switch (state.currentNav) {
    case 'dashboard':
      html = renderDashboard();
      contentArea.innerHTML = html;
      bindDashboardEvents(contentArea);
      break;
    case 'syllabus':
      html = renderSyllabus();
      contentArea.innerHTML = html;
      bindSyllabusEvents(contentArea, openModal);
      break;
    case 'mistakes':
      html = renderMistakes();
      contentArea.innerHTML = html;
      bindMistakesEvents(contentArea, openModal);
      break;
    case 'mocktests':
      html = renderMockTests();
      contentArea.innerHTML = html;
      bindMockTestsEvents(contentArea, openModal);
      break;
    case 'revision':
      html = renderRevision();
      contentArea.innerHTML = html;
      bindRevisionEvents(contentArea);
      break;
    case 'planner':
      html = renderPlanner();
      contentArea.innerHTML = html;
      bindPlannerEvents(contentArea, openModal);
      break;
    case 'timer':
      html = renderTimer();
      contentArea.innerHTML = html;
      bindTimerEvents(contentArea);
      break;
    case 'analytics':
      html = renderAnalytics();
      contentArea.innerHTML = html;
      bindAnalyticsEvents(contentArea);
      break;
    case 'weak':
      html = renderWeak();
      contentArea.innerHTML = html;
      bindWeakEvents(contentArea);
      break;
    case 'goals':
      html = renderGoals();
      contentArea.innerHTML = html;
      bindGoalsEvents(contentArea, openModal);
      break;
    case 'profile':
      html = renderProfile();
      contentArea.innerHTML = html;
      bindProfileEvents(contentArea);
      break;
    case 'settings':
      html = renderSettings();
      contentArea.innerHTML = html;
      bindSettingsEvents(contentArea);
      break;
    case 'calendar':
      html = renderCalendar();
      contentArea.innerHTML = html;
      bindCalendarEvents(contentArea);
      break;
    default:
      html = renderDashboard();
      contentArea.innerHTML = html;
      bindDashboardEvents(contentArea);
      break;
  }
}

/* Modal Management System */
export function openModal(title, bodyHTML, onBind) {
  const backdrop = document.getElementById('modal-backdrop');
  const modalTitle = document.getElementById('modal-title');
  const modalBody = document.getElementById('modal-body');

  modalTitle.textContent = title;
  modalBody.innerHTML = bodyHTML;

  if (onBind) onBind(modalBody);

  backdrop.classList.add('active');
}

export function closeModal() {
  const backdrop = document.getElementById('modal-backdrop');
  backdrop.classList.remove('active');
}

/* Event Handler Binding */
function bindHeaderAndNavEvents() {
  const themeToggle = document.getElementById('theme-toggle');
  if (themeToggle) themeToggle.addEventListener('click', toggleTheme);

  const themeSwatches = document.getElementById('theme-swatches');
  if (themeSwatches) {
    themeSwatches.querySelectorAll('.theme-swatch').forEach(swatch => {
      swatch.addEventListener('click', () => {
        setTheme(swatch.dataset.themeVal);
      });
    });
  }

  const mobileMenuBtn = document.getElementById('mobile-menu-btn');
  const sidebar = document.getElementById('sidebar');
  if (mobileMenuBtn && sidebar) {
    mobileMenuBtn.addEventListener('click', () => {
      sidebar.classList.toggle('mobile-open');
    });
  }

  const topTimerBtn = document.getElementById('top-timer-btn');
  if (topTimerBtn) topTimerBtn.addEventListener('click', () => state.setNav('timer'));

  const topProfileBtn = document.getElementById('top-profile-btn');
  if (topProfileBtn) topProfileBtn.addEventListener('click', () => state.setNav('profile'));

  const modalCloseBtn = document.getElementById('modal-close-btn');
  if (modalCloseBtn) modalCloseBtn.addEventListener('click', closeModal);

  const backdrop = document.getElementById('modal-backdrop');
  if (backdrop) {
    backdrop.addEventListener('click', (e) => {
      if (e.target === backdrop) closeModal();
    });
  }
}

function bindFabAndModal() {
  const fab = document.getElementById('global-fab');
  if (fab) {
    fab.addEventListener('click', () => {
      openModal('Quick Add Entry', `
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
          <button class="btn btn-secondary quick-fab-action" data-type="CHAPTER" style="flex-direction: column; padding: 20px;">
            <span class="material-symbols-outlined" style="font-size: 32px; color: var(--primary);">auto_stories</span>
            <span>New Chapter</span>
          </button>
          <button class="btn btn-secondary quick-fab-action" data-type="MISTAKE" style="flex-direction: column; padding: 20px;">
            <span class="material-symbols-outlined" style="font-size: 32px; color: var(--status-weak);">bookmark_remove</span>
            <span>Log Error</span>
          </button>
          <button class="btn btn-secondary quick-fab-action" data-type="MOCK" style="flex-direction: column; padding: 20px;">
            <span class="material-symbols-outlined" style="font-size: 32px; color: var(--primary);">quiz</span>
            <span>Mock Test</span>
          </button>
          <button class="btn btn-secondary quick-fab-action" data-type="PLAN" style="flex-direction: column; padding: 20px;">
            <span class="material-symbols-outlined" style="font-size: 32px; color: var(--status-in-progress);">calendar_month</span>
            <span>Study Plan</span>
          </button>
        </div>
      `, (modalBody) => {
        modalBody.querySelectorAll('.quick-fab-action').forEach(btn => {
          btn.addEventListener('click', () => {
            const type = btn.dataset.type;
            closeModal();
            if (type === 'CHAPTER') state.setNav('syllabus');
            else if (type === 'MISTAKE') state.setNav('mistakes');
            else if (type === 'MOCK') state.setNav('mocktests');
            else if (type === 'PLAN') state.setNav('planner');
          });
        });
      });
    });
  }
}
