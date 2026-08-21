/* Reactive State Management Bus */

import {
  STORAGE_KEYS,
  DEFAULT_SETTINGS,
  DEFAULT_SUBJECTS,
  DEFAULT_SYLLABUS_ITEMS,
  DEFAULT_MISTAKES,
  DEFAULT_MOCK_TESTS,
  DEFAULT_GOALS,
  DEFAULT_BADGES,
  loadFromStorage,
  saveToStorage
} from './data.js';

class AppState {
  constructor() {
    this.currentNav = 'dashboard';
    this.selectedSubjectId = null;
    this.listeners = new Set();

    // Load state from local storage or defaults
    this.settings = loadFromStorage(STORAGE_KEYS.SETTINGS, DEFAULT_SETTINGS);
    this.subjects = loadFromStorage(STORAGE_KEYS.SUBJECTS, DEFAULT_SUBJECTS);
    this.items = loadFromStorage(STORAGE_KEYS.SYLLABUS_ITEMS, DEFAULT_SYLLABUS_ITEMS);
    this.sessions = loadFromStorage(STORAGE_KEYS.STUDY_SESSIONS, []);
    this.mistakes = loadFromStorage(STORAGE_KEYS.MISTAKES, DEFAULT_MISTAKES);
    this.mockTests = loadFromStorage(STORAGE_KEYS.MOCK_TESTS, DEFAULT_MOCK_TESTS);
    this.plans = loadFromStorage(STORAGE_KEYS.STUDY_PLANS, []);
    this.goals = loadFromStorage(STORAGE_KEYS.GOALS, DEFAULT_GOALS);
    this.badges = loadFromStorage(STORAGE_KEYS.BADGES, DEFAULT_BADGES);

    // Active Timer state
    this.timer = {
      isRunning: false,
      mode: 'POMODORO', // 'POMODORO', 'SHORT_BREAK', 'LONG_BREAK', 'STOPWATCH'
      remainingSeconds: 25 * 60,
      totalDurationSeconds: 25 * 60,
      subjectId: null,
      chapterId: null,
      intervalId: null
    };
  }

  subscribe(callback) {
    this.listeners.add(callback);
    return () => this.listeners.delete(callback);
  }

  notify() {
    this.listeners.forEach(cb => cb(this));
  }

  setNav(nav, subjectId = null) {
    this.currentNav = nav;
    if (subjectId !== null) this.selectedSubjectId = subjectId;
    this.notify();
  }

  updateSettings(newSettings) {
    this.settings = { ...this.settings, ...newSettings };
    saveToStorage(STORAGE_KEYS.SETTINGS, this.settings);
    this.notify();
  }

  // Subjects
  addSubject(subject) {
    const newSub = {
      id: Date.now(),
      orderIndex: this.subjects.length,
      ...subject
    };
    this.subjects.push(newSub);
    saveToStorage(STORAGE_KEYS.SUBJECTS, this.subjects);
    this.notify();
    return newSub;
  }

  updateSubject(id, updated) {
    const index = this.subjects.findIndex(s => s.id === id);
    if (index !== -1) {
      this.subjects[index] = { ...this.subjects[index], ...updated };
      saveToStorage(STORAGE_KEYS.SUBJECTS, this.subjects);
      this.notify();
    }
  }

  deleteSubject(id) {
    this.subjects = this.subjects.filter(s => s.id !== id);
    this.items = this.items.filter(i => i.subjectId !== id);
    saveToStorage(STORAGE_KEYS.SUBJECTS, this.subjects);
    saveToStorage(STORAGE_KEYS.SYLLABUS_ITEMS, this.items);
    this.notify();
  }

  // Syllabus Items
  addItem(item) {
    const newItem = {
      id: Date.now(),
      status: 'NOT_STARTED',
      priority: 'MEDIUM',
      difficulty: 'MEDIUM',
      confidence: 3,
      completionPercentage: 0,
      studyTimeMinutes: 0,
      pyqAttempted: 0,
      pyqCorrect: 0,
      revisionCount: 0,
      ...item
    };
    this.items.push(newItem);
    saveToStorage(STORAGE_KEYS.SYLLABUS_ITEMS, this.items);
    this.notify();
    return newItem;
  }

  updateItem(id, updated) {
    const index = this.items.findIndex(i => i.id === id);
    if (index !== -1) {
      this.items[index] = { ...this.items[index], ...updated };
      saveToStorage(STORAGE_KEYS.SYLLABUS_ITEMS, this.items);
      this.notify();
    }
  }

  deleteItem(id) {
    // Delete item and child items cascade
    const idsToDelete = new Set([id]);
    let added = true;
    while (added) {
      added = false;
      this.items.forEach(i => {
        if (i.parentId && idsToDelete.has(i.parentId) && !idsToDelete.has(i.id)) {
          idsToDelete.add(i.id);
          added = true;
        }
      });
    }
    this.items = this.items.filter(i => !idsToDelete.has(i.id));
    saveToStorage(STORAGE_KEYS.SYLLABUS_ITEMS, this.items);
    this.notify();
  }

  // Revision logic
  markChapterRevised(id) {
    const item = this.items.find(i => i.id === id);
    if (!item) return;

    const intervals = (this.settings.revisionIntervalsCsv || '1,3,7,14,30')
      .split(',')
      .map(x => parseInt(x.trim()) || 1);

    const nextRevCount = (item.revisionCount || 0) + 1;
    let nextStatus = item.status === 'WEAK' ? 'IN_PROGRESS' : item.status;
    if (nextRevCount >= 5) {
      nextStatus = 'MASTERED';
    } else if (item.status === 'NOT_STARTED' || item.status === 'WEAK') {
      nextStatus = 'COMPLETED';
    }

    const intervalDays = intervals[Math.min(nextRevCount - 1, intervals.length - 1)];
    const nextTimestamp = Date.now() + intervalDays * 86400000;

    this.updateItem(id, {
      revisionCount: nextRevCount,
      status: nextStatus,
      lastStudiedTimestamp: Date.now(),
      nextRevisionTimestamp: nextTimestamp
    });
  }

  // Mistakes
  addMistake(mistake) {
    const newMistake = {
      id: Date.now(),
      resolutionStatus: 'ACTIVE',
      timestamp: Date.now(),
      ...mistake
    };
    this.mistakes.unshift(newMistake);
    saveToStorage(STORAGE_KEYS.MISTAKES, this.mistakes);
    this.notify();
  }

  updateMistake(id, updated) {
    const idx = this.mistakes.findIndex(m => m.id === id);
    if (idx !== -1) {
      this.mistakes[idx] = { ...this.mistakes[idx], ...updated };
      saveToStorage(STORAGE_KEYS.MISTAKES, this.mistakes);
      this.notify();
    }
  }

  deleteMistake(id) {
    this.mistakes = this.mistakes.filter(m => m.id !== id);
    saveToStorage(STORAGE_KEYS.MISTAKES, this.mistakes);
    this.notify();
  }

  // Mock Tests
  addMockTest(test) {
    const newTest = {
      id: Date.now(),
      ...test
    };
    this.mockTests.unshift(newTest);
    saveToStorage(STORAGE_KEYS.MOCK_TESTS, this.mockTests);
    this.notify();
  }

  deleteMockTest(id) {
    this.mockTests = this.mockTests.filter(m => m.id !== id);
    saveToStorage(STORAGE_KEYS.MOCK_TESTS, this.mockTests);
    this.notify();
  }

  // Study Plans
  addPlan(plan) {
    const newPlan = {
      id: Date.now(),
      isCompleted: false,
      ...plan
    };
    this.plans.unshift(newPlan);
    saveToStorage(STORAGE_KEYS.STUDY_PLANS, this.plans);
    this.notify();
  }

  togglePlanCompleted(id) {
    const idx = this.plans.findIndex(p => p.id === id);
    if (idx !== -1) {
      this.plans[idx].isCompleted = !this.plans[idx].isCompleted;
      saveToStorage(STORAGE_KEYS.STUDY_PLANS, this.plans);
      this.notify();
    }
  }

  deletePlan(id) {
    this.plans = this.plans.filter(p => p.id !== id);
    saveToStorage(STORAGE_KEYS.STUDY_PLANS, this.plans);
    this.notify();
  }

  // Goals
  addGoal(goal) {
    const newGoal = {
      id: Date.now(),
      isCompleted: false,
      ...goal
    };
    this.goals.push(newGoal);
    saveToStorage(STORAGE_KEYS.GOALS, this.goals);
    this.notify();
  }

  deleteGoal(id) {
    this.goals = this.goals.filter(g => g.id !== id);
    saveToStorage(STORAGE_KEYS.GOALS, this.goals);
    this.notify();
  }

  // Backup & Restore
  exportBackupJSON() {
    const backup = {
      settings: this.settings,
      subjects: this.subjects,
      items: this.items,
      sessions: this.sessions,
      mistakes: this.mistakes,
      mockTests: this.mockTests,
      plans: this.plans,
      goals: this.goals,
      badges: this.badges,
      exportDate: new Date().toISOString()
    };
    return JSON.stringify(backup, null, 2);
  }

  importBackupJSON(jsonStr) {
    try {
      const parsed = JSON.parse(jsonStr);
      if (parsed.settings) this.settings = parsed.settings;
      if (parsed.subjects) this.subjects = parsed.subjects;
      if (parsed.items) this.items = parsed.items;
      if (parsed.sessions) this.sessions = parsed.sessions;
      if (parsed.mistakes) this.mistakes = parsed.mistakes;
      if (parsed.mockTests) this.mockTests = parsed.mockTests;
      if (parsed.plans) this.plans = parsed.plans;
      if (parsed.goals) this.goals = parsed.goals;
      if (parsed.badges) this.badges = parsed.badges;

      saveToStorage(STORAGE_KEYS.SETTINGS, this.settings);
      saveToStorage(STORAGE_KEYS.SUBJECTS, this.subjects);
      saveToStorage(STORAGE_KEYS.SYLLABUS_ITEMS, this.items);
      saveToStorage(STORAGE_KEYS.STUDY_SESSIONS, this.sessions);
      saveToStorage(STORAGE_KEYS.MISTAKES, this.mistakes);
      saveToStorage(STORAGE_KEYS.MOCK_TESTS, this.mockTests);
      saveToStorage(STORAGE_KEYS.STUDY_PLANS, this.plans);
      saveToStorage(STORAGE_KEYS.GOALS, this.goals);
      saveToStorage(STORAGE_KEYS.BADGES, this.badges);

      this.notify();
      return true;
    } catch (e) {
      console.error('Import backup failed:', e);
      return false;
    }
  }

  resetToDefault() {
    this.settings = DEFAULT_SETTINGS;
    this.subjects = DEFAULT_SUBJECTS;
    this.items = DEFAULT_SYLLABUS_ITEMS;
    this.sessions = [];
    this.mistakes = DEFAULT_MISTAKES;
    this.mockTests = DEFAULT_MOCK_TESTS;
    this.plans = [];
    this.goals = DEFAULT_GOALS;
    this.badges = DEFAULT_BADGES;

    localStorage.clear();
    saveToStorage(STORAGE_KEYS.SETTINGS, this.settings);
    saveToStorage(STORAGE_KEYS.SUBJECTS, this.subjects);
    saveToStorage(STORAGE_KEYS.SYLLABUS_ITEMS, this.items);
    saveToStorage(STORAGE_KEYS.MISTAKES, this.mistakes);
    saveToStorage(STORAGE_KEYS.MOCK_TESTS, this.mockTests);
    saveToStorage(STORAGE_KEYS.GOALS, this.goals);
    saveToStorage(STORAGE_KEYS.BADGES, this.badges);

    this.notify();
  }
}

export const state = new AppState();
