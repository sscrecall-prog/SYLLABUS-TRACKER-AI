/* Settings Screen Component */

import { state } from '../state.js';

export function renderSettings() {
  const s = state.settings;

  return `
    <div class="settings-view" style="max-width: 700px; margin: 0 auto;">
      <!-- Preferences Card -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">settings</span> Preferences & Exam Settings</span>
        </div>

        <form id="settings-form">
          <div class="form-group">
            <label>Target Exam Name</label>
            <input type="text" id="set-exam-name" class="form-control" value="${s.targetExam || 'SSC CGL 2026'}" required />
          </div>

          <div class="form-group">
            <label>Target Exam Date</label>
            <input type="date" id="set-exam-date" class="form-control" value="${s.targetExamDateStr || '2026-10-01'}" required />
          </div>

          <div class="form-group">
            <label>Daily Study Target (Minutes)</label>
            <input type="number" id="set-daily-target" class="form-control" value="${s.dailyTargetMinutes || 180}" min="30" step="15" required />
          </div>

          <div class="form-group">
            <label>Spaced Repetition Intervals (Days CSV)</label>
            <input type="text" id="set-intervals" class="form-control" value="${s.revisionIntervalsCsv || '1,3,7,14,30'}" required />
          </div>

          <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; margin-top: 12px;">
            Save Preferences
          </button>
        </form>
      </div>

      <!-- Backup & Data Transfer Card -->
      <div class="card">
        <div class="card-header">
          <span class="card-title"><span class="material-symbols-outlined">cloud_download</span> Backup & Data Management</span>
        </div>
        <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 16px;">Export your complete syllabus, error diary, and progress data as a JSON file, or restore from a backup.</p>

        <div style="display: flex; gap: 12px; flex-wrap: wrap;">
          <button class="btn btn-primary" id="export-json-btn">
            <span class="material-symbols-outlined">download</span> Export Backup (JSON)
          </button>
          <label class="btn btn-secondary" style="cursor: pointer;">
            <span class="material-symbols-outlined">upload</span> Import Backup (JSON)
            <input type="file" id="import-json-file" accept=".json" style="display: none;" />
          </label>
          <button class="btn btn-danger" id="reset-data-btn">
            <span class="material-symbols-outlined">restart_alt</span> Reset to Defaults
          </button>
        </div>
      </div>
    </div>
  `;
}

export function bindSettingsEvents(container) {
  const form = container.querySelector('#settings-form');
  if (form) {
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      state.updateSettings({
        targetExam: container.querySelector('#set-exam-name').value,
        targetExamDateStr: container.querySelector('#set-exam-date').value,
        dailyTargetMinutes: parseInt(container.querySelector('#set-daily-target').value) || 180,
        revisionIntervalsCsv: container.querySelector('#set-intervals').value
      });
      alert('Settings saved!');
    });
  }

  const exportBtn = container.querySelector('#export-json-btn');
  if (exportBtn) {
    exportBtn.addEventListener('click', () => {
      const json = state.exportBackupJSON();
      const blob = new Blob([json], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `SyllabusTracker_Backup_${new Date().toISOString().split('T')[0]}.json`;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  const importFile = container.querySelector('#import-json-file');
  if (importFile) {
    importFile.addEventListener('change', (e) => {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = (evt) => {
        const success = state.importBackupJSON(evt.target.result);
        if (success) {
          alert('Data imported successfully!');
        } else {
          alert('Failed to import backup file. Invalid JSON structure.');
        }
      };
      reader.readAsText(file);
    });
  }

  const resetBtn = container.querySelector('#reset-data-btn');
  if (resetBtn) {
    resetBtn.addEventListener('click', () => {
      if (confirm('Are you sure you want to reset all data back to sample defaults? This action cannot be undone.')) {
        state.resetToDefault();
      }
    });
  }
}
