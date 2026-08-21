/* Syllabus Screen Component */

import { state } from '../state.js';

export function renderSyllabus() {
  const selectedSubId = state.selectedSubjectId || (state.subjects[0] ? state.subjects[0].id : null);
  const currentSub = state.subjects.find(s => s.id === selectedSubId);
  
  const items = state.items.filter(i => i.subjectId === selectedSubId);
  const sections = items.filter(i => i.itemType === 'SECTION');

  return `
    <div class="syllabus-view">
      <!-- Subject Tabs Bar -->
      <div style="display: flex; gap: 8px; overflow-x: auto; padding-bottom: 12px; margin-bottom: 16px;">
        ${state.subjects.map(s => `
          <button class="btn ${s.id === selectedSubId ? 'btn-primary' : 'btn-secondary'} sub-tab-btn" data-subid="${s.id}" style="white-space: nowrap;">
            <span class="material-symbols-outlined" style="font-size: 18px;">${s.iconName || 'school'}</span>
            ${s.name}
          </button>
        `).join('')}
        <button class="btn btn-secondary" id="add-subject-btn" style="white-space: nowrap;">
          <span class="material-symbols-outlined">add</span> New Subject
        </button>
      </div>

      <!-- Selected Subject Header -->
      ${currentSub ? `
        <div class="card" style="border-left: 4px solid ${currentSub.colorHex};">
          <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
            <div>
              <h2 style="font-size: 20px; font-weight: 800; color: ${currentSub.colorHex};">${currentSub.name}</h2>
              <span style="font-size: 12px; color: var(--text-muted);">Subject Code: ${currentSub.code}</span>
            </div>
            <div style="display: flex; gap: 8px;">
              <button class="btn btn-primary" id="add-section-btn">
                <span class="material-symbols-outlined">add</span> Add Section / Chapter
              </button>
              <button class="btn btn-danger" id="delete-sub-btn" data-subid="${currentSub.id}">
                <span class="material-symbols-outlined">delete</span>
              </button>
            </div>
          </div>
        </div>
      ` : ''}

      <!-- Topic Hierarchy Tree -->
      <div class="topic-tree">
        ${sections.length === 0 ? `
          <div class="card" style="text-align: center; padding: 40px; color: var(--text-muted);">
            <span class="material-symbols-outlined" style="font-size: 48px; margin-bottom: 8px;">folder_off</span>
            <p style="font-weight: 600;">No sections or topics added for this subject yet.</p>
            <p style="font-size: 12px; margin-top: 4px;">Click "Add Section / Chapter" above to get started.</p>
          </div>
        ` : sections.map(sec => {
          const chapters = items.filter(i => i.parentId === sec.id && i.itemType === 'CHAPTER');
          return `
            <div class="card" style="margin-bottom: 16px;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; border-bottom: 1px solid var(--border-color); padding-bottom: 8px;">
                <h3 style="font-size: 16px; font-weight: 700; display: flex; align-items: center; gap: 8px;">
                  <span class="material-symbols-outlined" style="color: var(--primary);">folder</span> ${sec.title}
                </h3>
                <span style="font-size: 12px; color: var(--text-muted); font-weight: 600;">${chapters.length} Chapters</span>
              </div>

              <div style="display: flex; flex-direction: column; gap: 10px;">
                ${chapters.map(chap => {
                  const statusBadgeClass = `badge-${chap.status.toLowerCase().replace('_', '-')}`;
                  return `
                    <div class="chapter-item-row" data-chapid="${chap.id}" style="background: var(--surface-variant); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 12px 16px; display: flex; justify-content: space-between; align-items: center; gap: 12px; cursor: pointer;">
                      <div>
                        <div style="font-weight: 700; font-size: 14px; margin-bottom: 4px;">${chap.title}</div>
                        <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap;">
                          <span class="badge ${statusBadgeClass}">${chap.status.replace('_', ' ')}</span>
                          <span style="font-size: 11px; color: var(--text-muted);">Priority: <strong>${chap.priority}</strong></span>
                          <span style="font-size: 11px; color: var(--text-muted);">Difficulty: <strong>${chap.difficulty}</strong></span>
                          <span style="font-size: 11px; color: var(--text-muted);">Confidence: <strong>${'⭐'.repeat(chap.confidence || 3)}</strong></span>
                        </div>
                      </div>
                      <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="font-size: 12px; font-weight: 700; color: var(--primary);">${chap.completionPercentage || 0}%</span>
                        <span class="material-symbols-outlined" style="color: var(--text-muted);">chevron_right</span>
                      </div>
                    </div>
                  `;
                }).join('')}
              </div>
            </div>
          `;
        }).join('')}
      </div>
    </div>
  `;
}

export function bindSyllabusEvents(container, openModal) {
  container.querySelectorAll('.sub-tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const subId = parseInt(btn.dataset.subid);
      state.setNav('syllabus', subId);
    });
  });

  const addSubBtn = container.querySelector('#add-subject-btn');
  if (addSubBtn) {
    addSubBtn.addEventListener('click', () => {
      openModal('Add New Subject', `
        <form id="modal-add-subject-form">
          <div class="form-group">
            <label>Subject Name</label>
            <input type="text" id="sub-name-input" class="form-control" placeholder="e.g. General Science" required />
          </div>
          <div class="form-group">
            <label>Subject Code</label>
            <input type="text" id="sub-code-input" class="form-control" placeholder="e.g. GS" required />
          </div>
          <div class="form-group">
            <label>Color</label>
            <input type="color" id="sub-color-input" class="form-control" value="#10b981" style="height: 44px;" />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; margin-top: 12px;">Save Subject</button>
        </form>
      `, (modalBody) => {
        const form = modalBody.querySelector('#modal-add-subject-form');
        form.addEventListener('submit', (e) => {
          e.preventDefault();
          const name = modalBody.querySelector('#sub-name-input').value;
          const code = modalBody.querySelector('#sub-code-input').value;
          const colorHex = modalBody.querySelector('#sub-color-input').value;
          const newSub = state.addSubject({ name, code, colorHex, iconName: 'school' });
          state.setNav('syllabus', newSub.id);
        });
      });
    });
  }

  const addSecBtn = container.querySelector('#add-section-btn');
  if (addSecBtn) {
    addSecBtn.addEventListener('click', () => {
      const subId = state.selectedSubjectId || state.subjects[0]?.id;
      openModal('Add Section / Chapter', `
        <form id="modal-add-item-form">
          <div class="form-group">
            <label>Item Type</label>
            <select id="item-type-select" class="form-control">
              <option value="SECTION">Section / Unit</option>
              <option value="CHAPTER">Chapter / Topic</option>
            </select>
          </div>
          <div class="form-group" id="parent-sec-group" style="display: none;">
            <label>Parent Section</label>
            <select id="parent-sec-select" class="form-control">
              ${state.items.filter(i => i.subjectId === subId && i.itemType === 'SECTION').map(sec => `
                <option value="${sec.id}">${sec.title}</option>
              `).join('')}
            </select>
          </div>
          <div class="form-group">
            <label>Title</label>
            <input type="text" id="item-title-input" class="form-control" placeholder="Title" required />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; margin-top: 12px;">Add Item</button>
        </form>
      `, (modalBody) => {
        const typeSelect = modalBody.querySelector('#item-type-select');
        const parentGroup = modalBody.querySelector('#parent-sec-group');
        typeSelect.addEventListener('change', () => {
          parentGroup.style.display = typeSelect.value === 'CHAPTER' ? 'block' : 'none';
        });

        const form = modalBody.querySelector('#modal-add-item-form');
        form.addEventListener('submit', (e) => {
          e.preventDefault();
          const itemType = typeSelect.value;
          const title = modalBody.querySelector('#item-title-input').value;
          let parentId = null;
          if (itemType === 'CHAPTER') {
            parentId = parseInt(modalBody.querySelector('#parent-sec-select').value);
          }
          state.addItem({ subjectId: subId, parentId, title, itemType });
        });
      });
    });
  }

  container.querySelectorAll('.chapter-item-row').forEach(row => {
    row.addEventListener('click', () => {
      const chapId = parseInt(row.dataset.chapid);
      const chap = state.items.find(i => i.id === chapId);
      if (!chap) return;

      openModal(`Edit Chapter: ${chap.title}`, `
        <form id="modal-edit-chap-form">
          <div class="form-group">
            <label>Chapter Title</label>
            <input type="text" id="chap-title" class="form-control" value="${chap.title}" required />
          </div>
          <div class="form-group">
            <label>Status</label>
            <select id="chap-status" class="form-control">
              <option value="NOT_STARTED" ${chap.status === 'NOT_STARTED' ? 'selected' : ''}>NOT STARTED</option>
              <option value="IN_PROGRESS" ${chap.status === 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
              <option value="COMPLETED" ${chap.status === 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
              <option value="MASTERED" ${chap.status === 'MASTERED' ? 'selected' : ''}>MASTERED</option>
              <option value="WEAK" ${chap.status === 'WEAK' ? 'selected' : ''}>WEAK</option>
            </select>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div class="form-group">
              <label>Priority</label>
              <select id="chap-priority" class="form-control">
                <option value="HIGH" ${chap.priority === 'HIGH' ? 'selected' : ''}>HIGH</option>
                <option value="MEDIUM" ${chap.priority === 'MEDIUM' ? 'selected' : ''}>MEDIUM</option>
                <option value="LOW" ${chap.priority === 'LOW' ? 'selected' : ''}>LOW</option>
              </select>
            </div>
            <div class="form-group">
              <label>Difficulty</label>
              <select id="chap-difficulty" class="form-control">
                <option value="EASY" ${chap.difficulty === 'EASY' ? 'selected' : ''}>EASY</option>
                <option value="MEDIUM" ${chap.difficulty === 'MEDIUM' ? 'selected' : ''}>MEDIUM</option>
                <option value="HARD" ${chap.difficulty === 'HARD' ? 'selected' : ''}>HARD</option>
              </select>
            </div>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div class="form-group">
              <label>Completion %</label>
              <input type="number" id="chap-pct" class="form-control" min="0" max="100" value="${chap.completionPercentage || 0}" />
            </div>
            <div class="form-group">
              <label>Confidence (1-5 Stars)</label>
              <input type="number" id="chap-conf" class="form-control" min="1" max="5" value="${chap.confidence || 3}" />
            </div>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div class="form-group">
              <label>PYQs Attempted</label>
              <input type="number" id="chap-pyq-att" class="form-control" value="${chap.pyqAttempted || 0}" />
            </div>
            <div class="form-group">
              <label>PYQs Correct</label>
              <input type="number" id="chap-pyq-corr" class="form-control" value="${chap.pyqCorrect || 0}" />
            </div>
          </div>
          <div style="display: flex; justify-content: space-between; gap: 12px; margin-top: 16px;">
            <button type="button" class="btn btn-danger" id="chap-del-btn">Delete</button>
            <button type="submit" class="btn btn-primary">Save Changes</button>
          </div>
        </form>
      `, (modalBody) => {
        const form = modalBody.querySelector('#modal-edit-chap-form');
        form.addEventListener('submit', (e) => {
          e.preventDefault();
          state.updateItem(chap.id, {
            title: modalBody.querySelector('#chap-title').value,
            status: modalBody.querySelector('#chap-status').value,
            priority: modalBody.querySelector('#chap-priority').value,
            difficulty: modalBody.querySelector('#chap-difficulty').value,
            completionPercentage: parseInt(modalBody.querySelector('#chap-pct').value) || 0,
            confidence: parseInt(modalBody.querySelector('#chap-conf').value) || 3,
            pyqAttempted: parseInt(modalBody.querySelector('#chap-pyq-att').value) || 0,
            pyqCorrect: parseInt(modalBody.querySelector('#chap-pyq-corr').value) || 0
          });
        });

        const delBtn = modalBody.querySelector('#chap-del-btn');
        delBtn.addEventListener('click', () => {
          if (confirm('Delete this chapter?')) {
            state.deleteItem(chap.id);
          }
        });
      });
    });
  });

  const deleteSubBtn = container.querySelector('#delete-sub-btn');
  if (deleteSubBtn) {
    deleteSubBtn.addEventListener('click', () => {
      const subId = parseInt(deleteSubBtn.dataset.subid);
      if (confirm('Are you sure you want to delete this subject and all its topics?')) {
        state.deleteSubject(subId);
      }
    });
  }
}
