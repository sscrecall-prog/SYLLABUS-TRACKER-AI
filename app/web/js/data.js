/* Default Sample Data & LocalStorage Repository Helper */

export const STORAGE_KEYS = {
  SETTINGS: 'syllabustracker_settings',
  SUBJECTS: 'syllabustracker_subjects',
  SYLLABUS_ITEMS: 'syllabustracker_items',
  STUDY_SESSIONS: 'syllabustracker_sessions',
  MISTAKES: 'syllabustracker_mistakes',
  MOCK_TESTS: 'syllabustracker_mock_tests',
  STUDY_PLANS: 'syllabustracker_plans',
  GOALS: 'syllabustracker_goals',
  BADGES: 'syllabustracker_badges'
};

export const DEFAULT_SETTINGS = {
  themeMode: 'SYSTEM',
  userAvatarEmoji: '🎯',
  dailyTargetMinutes: 180,
  targetExam: 'SSC CGL 2026',
  targetExamDateStr: '2026-10-01',
  reducedMotion: false,
  revisionIntervalsCsv: '1,3,7,14,30'
};

export const DEFAULT_SUBJECTS = [
  { id: 1, name: 'Quantitative Aptitude', code: 'MATHS', iconName: 'calculate', colorHex: '#3b82f6', orderIndex: 0 },
  { id: 2, name: 'General Intelligence & Reasoning', code: 'REASONING', iconName: 'psychology', colorHex: '#8b5cf6', orderIndex: 1 },
  { id: 3, name: 'English Language & Comprehension', code: 'ENGLISH', iconName: 'menu_book', colorHex: '#10b981', orderIndex: 2 },
  { id: 4, name: 'General Awareness', code: 'GS', iconName: 'public', colorHex: '#f59e0b', orderIndex: 3 },
  { id: 5, name: 'Computer Knowledge', code: 'COMPUTER', iconName: 'computer', colorHex: '#ec4899', orderIndex: 4 }
];

export const DEFAULT_SYLLABUS_ITEMS = [
  // Math
  { id: 101, subjectId: 1, parentId: null, title: 'Number System & Arithmetic', itemType: 'SECTION' },
  { id: 102, subjectId: 1, parentId: 101, title: 'Percentages & Ratio', itemType: 'CHAPTER', status: 'COMPLETED', priority: 'HIGH', difficulty: 'MEDIUM', confidence: 5, completionPercentage: 100, studyTimeMinutes: 240, pyqAttempted: 80, pyqCorrect: 72, revisionCount: 3, nextRevisionTimestamp: Date.now() + 86400000 },
  { id: 103, subjectId: 1, parentId: 101, title: 'Profit, Loss & Discount', itemType: 'CHAPTER', status: 'IN_PROGRESS', priority: 'HIGH', difficulty: 'HARD', confidence: 3, completionPercentage: 60, studyTimeMinutes: 150, pyqAttempted: 50, pyqCorrect: 38, revisionCount: 1, nextRevisionTimestamp: Date.now() - 3600000 },
  { id: 104, subjectId: 1, parentId: 101, title: 'Time, Distance & Work', itemType: 'CHAPTER', status: 'NOT_STARTED', priority: 'MEDIUM', difficulty: 'HARD', confidence: 2, completionPercentage: 0, studyTimeMinutes: 0, pyqAttempted: 0, pyqCorrect: 0, revisionCount: 0 },
  
  // Reasoning
  { id: 201, subjectId: 2, parentId: null, title: 'Verbal Reasoning', itemType: 'SECTION' },
  { id: 202, subjectId: 2, parentId: 201, title: 'Syllogisms & Logic', itemType: 'CHAPTER', status: 'COMPLETED', priority: 'HIGH', difficulty: 'MEDIUM', confidence: 4, completionPercentage: 100, studyTimeMinutes: 180, pyqAttempted: 60, pyqCorrect: 54, revisionCount: 2 },
  { id: 203, subjectId: 2, parentId: 201, title: 'Blood Relations & Coding', itemType: 'CHAPTER', status: 'COMPLETED', priority: 'MEDIUM', difficulty: 'EASY', confidence: 5, completionPercentage: 100, studyTimeMinutes: 120, pyqAttempted: 40, pyqCorrect: 39, revisionCount: 4 },

  // English
  { id: 301, subjectId: 3, parentId: null, title: 'Grammar & Vocabulary', itemType: 'SECTION' },
  { id: 302, subjectId: 3, parentId: 301, title: 'Error Spotting & Cloze Test', itemType: 'CHAPTER', status: 'WEAK', priority: 'HIGH', difficulty: 'HARD', confidence: 2, completionPercentage: 40, studyTimeMinutes: 110, pyqAttempted: 45, pyqCorrect: 25, revisionCount: 0, nextRevisionTimestamp: Date.now() - 7200000 },
  { id: 303, subjectId: 3, parentId: 301, title: 'Idioms & One Word Substitution', itemType: 'CHAPTER', status: 'COMPLETED', priority: 'MEDIUM', difficulty: 'EASY', confidence: 5, completionPercentage: 100, studyTimeMinutes: 160, pyqAttempted: 100, pyqCorrect: 92, revisionCount: 5 }
];

export const DEFAULT_MISTAKES = [
  { id: 1, questionText: 'In a circle of radius 10cm, length of chord is 12cm. Distance from center?', subjectId: 1, subjectName: 'Quantitative Aptitude', category: 'CALCULATION_ERROR', resolutionStatus: 'UNDERSTOOD', explanation: 'Used Pythagoras: 10^2 - 6^2 = 64 => sqrt is 8. Made silly subtraction mistake earlier.', timestamp: Date.now() - 86400000 },
  { id: 2, questionText: 'Identify the grammatically correct sentence involving "Hardly had I arrived when..."', subjectId: 3, subjectName: 'English Language', category: 'CONCEPT_GAP', resolutionStatus: 'ACTIVE', explanation: 'Remember inverted syntax: "Hardly had + Subject + V3... when". Do not use "than".', timestamp: Date.now() - 172800000 }
];

export const DEFAULT_MOCK_TESTS = [
  { id: 1, testName: 'SSC CGL Full Length Mock 01', testDateStr: new Date().toISOString().split('T')[0], totalMarks: 200, marksScored: 148.5, totalQuestions: 100, attemptedQuestions: 88, correctQuestions: 78, incorrectQuestions: 10, cutoffMarks: 135, percentile: 92.4, accuracy: 88.6 },
  { id: 2, testName: 'SSC CGL Tier I Live Test', testDateStr: new Date(Date.now() - 604800000).toISOString().split('T')[0], totalMarks: 200, marksScored: 136, totalQuestions: 100, attemptedQuestions: 82, correctQuestions: 72, incorrectQuestions: 10, cutoffMarks: 130, percentile: 86.1, accuracy: 87.8 }
];

export const DEFAULT_GOALS = [
  { id: 1, title: 'Complete Quantitative Aptitude Syllabus', targetDateStr: '2026-09-15', subjectId: 1, subjectName: 'Quantitative Aptitude', targetChaptersCount: 15, targetStudyHours: 80, isCompleted: false },
  { id: 2, title: 'Master 500 High-Frequency English Idioms', targetDateStr: '2026-09-01', subjectId: 3, subjectName: 'English Language', targetChaptersCount: 5, targetStudyHours: 30, isCompleted: false }
];

export const DEFAULT_BADGES = [
  { id: 'streak_3', title: 'Streak Master (3 Days)', description: 'Maintained a 3-day study streak', isUnlocked: true, unlockedDateStr: '2026-08-18' },
  { id: 'first_mock', title: 'Mock Champion', description: 'Recorded first full length mock test', isUnlocked: true, unlockedDateStr: '2026-08-20' },
  { id: 'master_topic', title: 'Syllabus Conqueror', description: 'Mastered 5 topics with spaced revision', isUnlocked: false },
  { id: 'error_crusher', title: 'Error Crusher', description: 'Resolved 5 entries in the digital error diary', isUnlocked: false }
];

/* Storage Loader Helpers */
export function loadFromStorage(key, defaultValue) {
  try {
    const json = localStorage.getItem(key);
    return json ? JSON.parse(json) : defaultValue;
  } catch (e) {
    console.error(`Error loading key ${key}:`, e);
    return defaultValue;
  }
}

export function saveToStorage(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (e) {
    console.error(`Error saving key ${key}:`, e);
  }
}
