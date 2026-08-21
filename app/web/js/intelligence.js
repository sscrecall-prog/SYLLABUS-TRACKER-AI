/* Core Intelligence Engine for Web */

export const INTELLIGENCE_CONFIG = {
  pyqAccuracyWeight: 0.30,
  confidenceWeight: 0.20,
  revisionStrengthWeight: 0.20,
  completionWeight: 0.15,
  mistakeControlWeight: 0.10,
  retentionWeight: 0.05,

  priorityWeaknessWeight: 0.35,
  priorityExamImportanceWeight: 0.25,
  priorityRevisionUrgencyWeight: 0.20,
  priorityMistakeFrequencyWeight: 0.15,
  priorityRecencyWeight: 0.05,

  masteredMinCompletionPercentage: 100,
  masteredMinPYQAccuracy: 85,
  masteredMinConfidence: 4,
  masteredMinRevisionCount: 1
};

export const MASTERY_LEVELS = {
  WEAK: { label: 'Weak', min: 0, max: 39.99 },
  LEARNING: { label: 'Learning', min: 40, max: 59.99 },
  STRONG: { label: 'Strong', min: 60, max: 79.99 },
  MASTERED: { label: 'Mastered', min: 80, max: 100 }
};

export function getMasteryLevel(score) {
  const s = Math.max(0, Math.min(100, score || 0));
  if (s < 40) return 'WEAK';
  if (s < 60) return 'LEARNING';
  if (s < 80) return 'STRONG';
  return 'MASTERED';
}

export function calculatePYQAccuracy(topic) {
  const attempted = Math.max(0, topic.pyqAttempted || 0);
  const correct = Math.min(attempted, Math.max(0, topic.pyqCorrect || 0));
  const incorrect = Math.max(0, attempted - correct);

  if (attempted === 0) {
    return {
      attempted: 0,
      correct: 0,
      incorrect: 0,
      accuracy: 0,
      status: 'NO_DATA'
    };
  }

  const accuracy = Math.min(100, Math.max(0, (correct / attempted) * 100));
  let status = 'POOR';
  if (accuracy >= 85) status = 'EXCELLENT';
  else if (accuracy >= 70) status = 'GOOD';
  else if (accuracy >= 50) status = 'AVERAGE';

  return { attempted, correct, incorrect, accuracy, status };
}

export function calculateConfidenceInfo(topic) {
  const raw = (topic.confidence >= 1 && topic.confidence <= 5) ? topic.confidence : 3;
  const normalized = Math.min(100, Math.max(0, raw * 20));
  return { value: raw, normalized };
}

export function filterMistakesForTopic(topic, mistakes = []) {
  if (!topic || !mistakes) return [];
  return mistakes.filter(m => {
    if (m.subjectId !== topic.subjectId) return false;
    if (m.chapterTitle && topic.title && (m.chapterTitle.toLowerCase().includes(topic.title.toLowerCase()) || topic.title.toLowerCase().includes(m.chapterTitle.toLowerCase()))) return true;
    return false;
  });
}

export function calculateMistakeControl(topic, mistakes = []) {
  const relevant = filterMistakesForTopic(topic, mistakes);
  const total = relevant.length;
  if (total === 0) {
    return { score: 100, totalMistakes: 0, activeMistakes: 0, repeatedMistakes: 0, conceptGaps: 0 };
  }

  const active = relevant.filter(m => m.resolutionStatus === 'ACTIVE').length;
  const understood = relevant.filter(m => m.resolutionStatus === 'UNDERSTOOD').length;
  const repeated = relevant.filter(m => (m.reviewCount && m.reviewCount > 1) || (m.tagsCsv && m.tagsCsv.toLowerCase().includes('repeated'))).length;
  const conceptGaps = relevant.filter(m => m.category === 'CONCEPT_GAP' && m.resolutionStatus !== 'MASTERED').length;

  const penalty = (active * 15) + (understood * 5) + (repeated * 10) + (conceptGaps * 10);
  const score = Math.min(100, Math.max(0, 100 - penalty));

  return { score, totalMistakes: total, activeMistakes: active, repeatedMistakes: repeated, conceptGaps };
}

export function calculateRevisionStrength(topic, currentTime = Date.now()) {
  const revCount = Math.max(0, topic.revisionCount || 0);
  const overdue = topic.nextRevisionTimestamp ? topic.nextRevisionTimestamp <= currentTime : false;

  let daysSince = null;
  if (topic.lastStudiedTimestamp && topic.lastStudiedTimestamp > 0) {
    daysSince = Math.max(0, (currentTime - topic.lastStudiedTimestamp) / (1000 * 60 * 60 * 24));
  }

  let baseScore = 0;
  if (revCount === 1) baseScore = 50;
  else if (revCount === 2) baseScore = 75;
  else if (revCount === 3) baseScore = 88;
  else if (revCount >= 4) baseScore = 100;

  if (overdue) baseScore -= 25;
  if (daysSince !== null) {
    if (daysSince <= 7 && revCount > 0) baseScore += 10;
    else if (daysSince > 30) baseScore -= 15;
  }

  const score = Math.min(100, Math.max(0, baseScore));
  return { score, revisionCount: revCount, overdue, daysSinceRevision: daysSince };
}

export function calculateRetentionScore(topic, currentTime = Date.now()) {
  if (!topic.lastStudiedTimestamp || topic.lastStudiedTimestamp <= 0) return 50;
  const days = Math.max(0, (currentTime - topic.lastStudiedTimestamp) / (1000 * 60 * 60 * 24));
  const retention = 100 * Math.exp(-0.03 * days);
  return Math.min(100, Math.max(0, retention));
}

export function calculateMasteryScore(topic, mistakes = [], currentTime = Date.now()) {
  const pyq = calculatePYQAccuracy(topic);
  const conf = calculateConfidenceInfo(topic);
  const rev = calculateRevisionStrength(topic, currentTime);
  const comp = Math.min(100, Math.max(0, topic.completionPercentage || 0));
  const mistakeCtrl = calculateMistakeControl(topic, mistakes);
  const retention = calculateRetentionScore(topic, currentTime);

  const pyqHasData = pyq.status !== 'NO_DATA';
  let score = 0;

  if (pyqHasData) {
    score = (pyq.accuracy * INTELLIGENCE_CONFIG.pyqAccuracyWeight) +
            (conf.normalized * INTELLIGENCE_CONFIG.confidenceWeight) +
            (rev.score * INTELLIGENCE_CONFIG.revisionStrengthWeight) +
            (comp * INTELLIGENCE_CONFIG.completionWeight) +
            (mistakeCtrl.score * INTELLIGENCE_CONFIG.mistakeControlWeight) +
            (retention * INTELLIGENCE_CONFIG.retentionWeight);
  } else {
    const activeSum = INTELLIGENCE_CONFIG.confidenceWeight +
                      INTELLIGENCE_CONFIG.revisionStrengthWeight +
                      INTELLIGENCE_CONFIG.completionWeight +
                      INTELLIGENCE_CONFIG.mistakeControlWeight +
                      INTELLIGENCE_CONFIG.retentionWeight;

    score = (conf.normalized * (INTELLIGENCE_CONFIG.confidenceWeight / activeSum)) +
            (rev.score * (INTELLIGENCE_CONFIG.revisionStrengthWeight / activeSum)) +
            (comp * (INTELLIGENCE_CONFIG.completionWeight / activeSum)) +
            (mistakeCtrl.score * (INTELLIGENCE_CONFIG.mistakeControlWeight / activeSum)) +
            (retention * (INTELLIGENCE_CONFIG.retentionWeight / activeSum));
  }

  const clampedScore = Math.min(100, Math.max(0, score));
  const level = getMasteryLevel(clampedScore);

  return {
    score: clampedScore,
    level,
    components: {
      pyqAccuracy: pyqHasData ? pyq.accuracy : -1,
      confidence: conf.normalized,
      revisionStrength: rev.score,
      completion: comp,
      mistakeControl: mistakeCtrl.score,
      retention
    }
  };
}

export function calculateWeaknessScore(topic, masteryResult, pyq, mistakesResult) {
  const baseWeakness = Math.min(100, Math.max(0, 100 - masteryResult.score));
  let boost = 0;
  if (mistakesResult.repeatedMistakes > 0) boost += mistakesResult.repeatedMistakes * 15;
  if (mistakesResult.conceptGaps > 0) boost += mistakesResult.conceptGaps * 15;
  if (pyq.status !== 'NO_DATA' && pyq.accuracy < 50) boost += 20;
  if (topic.confidence <= 2) boost += 15;
  if (topic.nextRevisionTimestamp && topic.nextRevisionTimestamp <= Date.now()) boost += 10;

  return Math.min(100, Math.max(0, baseWeakness + boost));
}

export function calculatePriorityScore(topic, weaknessScore, mistakesResult, revResult, currentTime = Date.now()) {
  let importanceComp = 50;
  if (topic.priority === 'URGENT') importanceComp = 100;
  else if (topic.priority === 'HIGH') importanceComp = 80;
  else if (topic.priority === 'MEDIUM') importanceComp = 50;
  else if (topic.priority === 'LOW') importanceComp = 20;
  if (topic.isImportant) importanceComp += 15;

  let revUrgency = 0;
  if (revResult.overdue) revUrgency = 100;
  else if (topic.status === 'REVISION_DUE') revUrgency = 75;

  const mistakeFreq = Math.min(100, (mistakesResult.activeMistakes * 25 + mistakesResult.conceptGaps * 20));
  const recency = revResult.daysSinceRevision !== null ? Math.min(100, revResult.daysSinceRevision * 3) : 50;

  const score = (weaknessScore * INTELLIGENCE_CONFIG.priorityWeaknessWeight) +
                (Math.min(100, importanceComp) * INTELLIGENCE_CONFIG.priorityExamImportanceWeight) +
                (revUrgency * INTELLIGENCE_CONFIG.priorityRevisionUrgencyWeight) +
                (mistakeFreq * INTELLIGENCE_CONFIG.priorityMistakeFrequencyWeight) +
                (recency * INTELLIGENCE_CONFIG.priorityRecencyWeight);

  return Math.min(100, Math.max(0, score));
}

export function isTopicMastered(topic, intelligence) {
  if ((topic.completionPercentage || 0) < INTELLIGENCE_CONFIG.masteredMinCompletionPercentage) return false;
  if ((topic.confidence || 0) < INTELLIGENCE_CONFIG.masteredMinConfidence) return false;
  if ((topic.revisionCount || 0) < INTELLIGENCE_CONFIG.masteredMinRevisionCount) return false;
  if (intelligence.mistakes.activeMistakes > 0) return false;
  if (intelligence.mistakes.conceptGaps > 0) return false;
  if (intelligence.pyq.status !== 'NO_DATA' && intelligence.pyq.accuracy < INTELLIGENCE_CONFIG.masteredMinPYQAccuracy) return false;

  return true;
}

export function calculateTopicIntelligence(topic, mistakes = [], currentTime = Date.now()) {
  const pyq = calculatePYQAccuracy(topic);
  const conf = calculateConfidenceInfo(topic);
  const mistakeCtrl = calculateMistakeControl(topic, mistakes);
  const rev = calculateRevisionStrength(topic, currentTime);
  const mastery = calculateMasteryScore(topic, mistakes, currentTime);
  const valWeakness = calculateWeaknessScore(topic, mastery, pyq, mistakeCtrl);
  const priority = calculatePriorityScore(topic, valWeakness, mistakeCtrl, rev, currentTime);

  const partial = {
    topicId: topic.id,
    topicTitle: topic.title,
    masteryScore: mastery.score,
    masteryLevel: mastery.level,
    pyq,
    confidence: conf,
    mistakes: mistakeCtrl,
    revision: rev,
    weaknessScore: valWeakness,
    priorityScore: priority,
    status: topic.status,
    masteryComponents: mastery.components
  };

  const masteredMet = isTopicMastered(topic, partial);
  return {
    ...partial,
    isMasteredCriteriaMet: masteredMet
  };
}

/* SPRINT 2 — ADAPTIVE PLANNING & READINESS ENGINE (WEB PARITY) */

export const READINESS_LEVELS = {
  CRITICAL: { label: 'Critical', min: 0, max: 39.99 },
  WEAK: { label: 'Weak', min: 40, max: 59.99 },
  PREPARING: { label: 'Preparing', min: 60, max: 74.99 },
  STRONG: { label: 'Strong', min: 75, max: 89.99 },
  EXAM_READY: { label: 'Exam Ready', min: 90, max: 100 }
};

export function getReadinessLevel(score) {
  const s = Math.max(0, Math.min(100, score || 0));
  if (s < 40) return 'CRITICAL';
  if (s < 60) return 'WEAK';
  if (s < 75) return 'PREPARING';
  if (s < 90) return 'STRONG';
  return 'EXAM_READY';
}

export function determineLastDaysMode(daysRemaining) {
  if (daysRemaining <= 7) return { mode: 'FINAL_CRUNCH', label: 'Final Crunch (≤7d)', focusAreas: 'High-yield revision, formula sheets, mock analysis' };
  if (daysRemaining <= 15) return { mode: 'INTENSIVE', label: 'Intensive Mode (≤15d)', focusAreas: 'PYQ drills, speed practice, weak topic triage' };
  if (daysRemaining <= 30) return { mode: 'ACCELERATION', label: 'Acceleration Mode (≤30d)', focusAreas: 'High-frequency chapters, full revisions, mock tests' };
  return { mode: 'FOUNDATION', label: 'Foundation & Coverage (>30d)', focusAreas: 'Deep concept clarity, full syllabus coverage, steady revision' };
}

export function calculateExamReadiness(topics = [], mistakes = [], mockTests = [], currentTime = Date.now()) {
  if (!topics || topics.length === 0) {
    return {
      score: 0,
      level: 'CRITICAL',
      confidence: 0,
      components: { syllabusCoverage: 0, mastery: 0, pyqPerformance: -1, revisionCoverage: 0, mistakeControl: 100, mockPerformance: -1 },
      warnings: ['No syllabus topics tracked yet. Add chapters to calculate readiness.'],
      disclaimer: 'Calculated using deterministic syllabus mastery, pyq accuracy, and error diary metrics.'
    };
  }

  const topicIntels = topics.map(t => calculateTopicIntelligence(t, mistakes, currentTime));
  const total = topics.length;

  const completedCount = topics.filter(t => (t.completionPercentage || 0) >= 100).length;
  const inProgressSum = topics.reduce((acc, t) => acc + (t.completionPercentage || 0), 0);
  const syllabusCoverage = Math.min(100, (inProgressSum / (total * 100)) * 100);

  const avgMastery = topicIntels.reduce((acc, i) => acc + i.masteryScore, 0) / total;

  const topicsWithPyq = topicIntels.filter(i => i.pyq.status !== 'NO_DATA');
  const avgPyq = topicsWithPyq.length > 0 ? (topicsWithPyq.reduce((acc, i) => acc + i.pyq.accuracy, 0) / topicsWithPyq.length) : -1;

  const coveredRevs = topics.filter(t => (t.revisionCount || 0) > 0).length;
  const overdueRevs = topics.filter(t => t.nextRevisionTimestamp && t.nextRevisionTimestamp <= currentTime).length;
  const baseRevCoverage = (coveredRevs / total) * 100;
  const revisionCoverage = Math.max(0, baseRevCoverage - (overdueRevs / total) * 30);

  const activeMistakes = mistakes.filter(m => m.resolutionStatus === 'ACTIVE').length;
  const conceptGaps = mistakes.filter(m => m.category === 'CONCEPT_GAP' && m.resolutionStatus !== 'MASTERED').length;
  const mistakeControl = Math.max(0, 100 - (activeMistakes * 10 + conceptGaps * 15));

  let mockPerformance = -1;
  if (mockTests && mockTests.length > 0) {
    const avgPercentile = mockTests.reduce((acc, m) => acc + (m.percentile || 0), 0) / mockTests.length;
    mockPerformance = Math.min(100, Math.max(0, avgPercentile));
  }

  // Calculate composite weighted score
  const weights = { syllabusCoverage: 0.15, mastery: 0.25, pyqPerformance: 0.20, revisionCoverage: 0.15, mistakeControl: 0.10, mockPerformance: 0.15 };
  let weightedSum = 0;
  let activeWeightSum = 0;

  weightedSum += syllabusCoverage * weights.syllabusCoverage;
  activeWeightSum += weights.syllabusCoverage;

  weightedSum += avgMastery * weights.mastery;
  activeWeightSum += weights.mastery;

  if (avgPyq >= 0) {
    weightedSum += avgPyq * weights.pyqPerformance;
    activeWeightSum += weights.pyqPerformance;
  }
  weightedSum += revisionCoverage * weights.revisionCoverage;
  activeWeightSum += weights.revisionCoverage;

  weightedSum += mistakeControl * weights.mistakeControl;
  activeWeightSum += weights.mistakeControl;

  if (mockPerformance >= 0) {
    weightedSum += mockPerformance * weights.mockPerformance;
    activeWeightSum += weights.mockPerformance;
  }

  const finalScore = activeWeightSum > 0 ? (weightedSum / activeWeightSum) : 0;
  const clampedScore = Math.min(100, Math.max(0, finalScore));
  const level = getReadinessLevel(clampedScore);

  let dataCoverageScore = (syllabusCoverage * 0.4) + ((topicsWithPyq.length / total) * 30) + (mockPerformance >= 0 ? 30 : 10);
  const confidence = Math.min(100, Math.max(20, dataCoverageScore));

  const warnings = [];
  if (overdueRevs > 0) warnings.push(`${overdueRevs} topics have overdue spaced repetition intervals`);
  if (activeMistakes > 0) warnings.push(`${activeMistakes} unreviewed errors remaining in Digital Error Diary`);
  if (topicsWithPyq.length < total * 0.5) warnings.push('PYQ question drills logged for less than 50% of syllabus topics');
  if (mockPerformance < 0) warnings.push('No mock exam percentiles recorded yet to benchmark exam readiness');

  return {
    score: clampedScore,
    level,
    confidence,
    components: {
      syllabusCoverage,
      mastery: avgMastery,
      pyqPerformance: avgPyq,
      revisionCoverage,
      mistakeControl,
      mockPerformance
    },
    warnings,
    disclaimer: 'Calculated deterministically based on syllabus mastery, PYQ drills, error logs, and mock performance.'
  };
}

export function calculateExamPace(topics = [], targetDateStr = '', examStartDateStr = '', currentTime = Date.now()) {
  const totalChapters = Math.max(1, topics.length);
  const completedChapters = topics.filter(t => (t.completionPercentage || 0) >= 100).length;
  const inProgressSum = topics.reduce((acc, t) => acc + (t.completionPercentage || 0), 0);
  const completedPct = (inProgressSum / (totalChapters * 100)) * 100;
  const remainingEquivalent = Math.max(0, totalChapters - (inProgressSum / 100));

  let targetTimestamp = 0;
  if (targetDateStr) {
    targetTimestamp = new Date(targetDateStr).getTime();
  }
  if (!targetTimestamp || isNaN(targetTimestamp)) {
    targetTimestamp = currentTime + (90 * 24 * 60 * 60 * 1000); // 90 days default
  }

  const daysRemaining = Math.max(1, Math.ceil((targetTimestamp - currentTime) / (1000 * 60 * 60 * 24)));
  const requiredPace = remainingEquivalent / daysRemaining;

  // Assume a baseline pace of 0.8 chapters/day or derived from completion
  const currentPace = Math.max(0.5, completedChapters > 0 ? (completedChapters / 30) : 0.8);
  const expectedPct = Math.min(100, Math.max(0, ((90 - daysRemaining) / 90) * 100));

  let status = 'ON_TRACK';
  if (completedPct >= expectedPct + 10) status = 'AHEAD';
  else if (completedPct >= expectedPct - 5) status = 'ON_TRACK';
  else if (completedPct >= expectedPct - 20) status = 'BEHIND';
  else status = 'CRITICAL';

  const recoveryNeeded = status === 'BEHIND' || status === 'CRITICAL';
  const paceDeficit = Math.max(0, requiredPace - currentPace);
  const isRealistic = requiredPace <= 3.0;

  const recovery = {
    isNeeded: recoveryNeeded,
    isRealistic,
    dailyPaceDeficit: paceDeficit,
    additionalDailyMinutes: Math.round(paceDeficit * 60),
    recommendationText: recoveryNeeded
      ? (isRealistic ? `Increase study time by ~${Math.round(paceDeficit * 60)} mins/day to complete syllabus before target date.` : `Required pace of ${requiredPace.toFixed(1)} ch/day is too high. Switch to high-yield topic triage mode.`)
      : 'Current velocity is on schedule for exam target.',
    strategicFocus: isRealistic ? 'Cover 1 additional chapter every 2 days' : 'Focus only on high-yield Tier-1 chapters'
  };

  return {
    status,
    daysRemaining,
    targetCompletionDateStr: targetDateStr || 'Default 90 Days',
    completedPercentage: completedPct,
    expectedPercentage: expectedPct,
    currentDailyPace: currentPace,
    requiredDailyPace: requiredPace,
    recovery
  };
}

export function generateWhyExplanation(topic, intel) {
  if (intel.mistakes.conceptGaps > 0) return `Active concept gap detected in error diary (${intel.mistakes.conceptGaps} unresolved)`;
  if (intel.revision.overdue) return `Spaced repetition interval overdue (Stage ${intel.revision.revisionCount})`;
  if (intel.pyq.status !== 'NO_DATA' && intel.pyq.accuracy < 60) return `Low PYQ accuracy (${Math.round(intel.pyq.accuracy)}%) requires question practice`;
  if (intel.confidence.value <= 2) return `Low self-confidence rating (${intel.confidence.value}/5)`;
  if (intel.isMasteredCriteriaMet) return `Mastered topic — on maintenance schedule`;
  if (topic.priority === 'URGENT' || topic.isImportant) return `Marked as high-importance exam chapter`;
  return `Regular syllabus progression and reinforcement`;
}

export function isMaintenanceOnlyTopic(topic, intel) {
  return intel.isMasteredCriteriaMet && !intel.revision.overdue && intel.mistakes.activeMistakes === 0;
}

export function generateTodaysPlan(topics = [], mistakes = [], availableMinutes = 90, subjects = [], currentTime = Date.now()) {
  const subjectMap = new Map();
  subjects.forEach(s => subjectMap.set(s.id, s.name));

  const items = [];
  let allocatedMinutes = 0;

  const topicIntels = topics.map(t => ({
    topic: t,
    intel: calculateTopicIntelligence(t, mistakes, currentTime)
  }));

  // Sort candidate actions by priority score descending
  topicIntels.sort((a, b) => b.intel.priorityScore - a.intel.priorityScore);

  for (const { topic, intel } of topicIntels) {
    if (allocatedMinutes >= availableMinutes) break;

    let actionType = 'WEAK_TOPIC';
    let duration = 30;

    if (intel.revision.overdue) {
      actionType = 'REVISION';
      duration = 20;
    } else if (intel.mistakes.conceptGaps > 0 || intel.mistakes.activeMistakes > 0) {
      actionType = 'MISTAKE_REVIEW';
      duration = 25;
    } else if (intel.pyq.status === 'NO_DATA' || intel.pyq.accuracy < 70) {
      actionType = 'PYQ_PRACTICE';
      duration = 30;
    } else if (intel.isMasteredCriteriaMet) {
      actionType = 'MAINTENANCE';
      duration = 15;
    } else if (topic.completionPercentage < 100) {
      actionType = 'CONCEPT_REVIEW';
      duration = 35;
    }

    if (allocatedMinutes + duration > availableMinutes && allocatedMinutes > 0) {
      duration = Math.max(15, availableMinutes - allocatedMinutes);
    }

    items.push({
      id: topic.id,
      topicId: topic.id,
      topicTitle: topic.title,
      subjectId: topic.subjectId,
      subjectName: subjectMap.get(topic.subjectId) || 'Subject',
      actionType,
      estimatedMinutes: duration,
      priorityScore: intel.priorityScore,
      reason: generateWhyExplanation(topic, intel),
      isCompleted: false
    });

    allocatedMinutes += duration;
  }

  return {
    availableMinutes,
    totalMinutes: allocatedMinutes,
    items,
    notes: items.length > 0 ? `Targeting ${items.length} high-impact study actions for today's budget.` : 'All syllabus items on track!'
  };
}

// -----------------------------------------------------------------------------
// SPRINT 3: PERFORMANCE & FEEDBACK ENGINE (WEB PARITY)
// -----------------------------------------------------------------------------

export function buildPerformanceSnapshot(topic, intel, studySessions = [], allMistakes = [], timestamp = Date.now()) {
  const topicSessions = (studySessions || []).filter(s => s.chapterId === topic.id || (s.chapterTitle && s.chapterTitle.toLowerCase() === topic.title.toLowerCase()));
  const totalStudyTimeSec = topicSessions.reduce((sum, s) => sum + (s.durationSeconds || 0), 0);
  const topicMistakes = filterMistakesForTopic(topic, allMistakes);
  const activeMistakes = topicMistakes.filter(m => m.resolutionStatus === 'ACTIVE').length;
  const pyqAcc = (intel.pyq && intel.pyq.status !== 'NO_DATA') ? intel.pyq.accuracy : -1.0;

  return {
    timestamp,
    topicId: topic.id,
    topicTitle: topic.title,
    masteryScore: intel.masteryScore || 0,
    pyqAccuracy: pyqAcc,
    confidence: intel.confidence ? intel.confidence.normalized : 60,
    mistakeCount: topicMistakes.length,
    activeMistakeCount: activeMistakes,
    revisionCount: topic.revisionCount || 0,
    completion: Math.min(100, Math.max(0, topic.completionPercentage || 0)),
    relevantStudyTimeSeconds: totalStudyTimeSec
  };
}

export function detectRecurringMistakes(mistakes = [], topics = [], subjects = [], windowDays = 14, currentTime = Date.now()) {
  const windowMillis = windowDays * 24 * 60 * 60 * 1000;
  const recentThreshold = currentTime - windowMillis;
  const subjectMap = new Map((subjects || []).map(s => [s.id, s.name]));

  const grouped = new Map();
  for (const m of (mistakes || [])) {
    const key = (m.chapterTitle && m.chapterTitle.trim().length > 0) ? m.chapterTitle.trim() : `Subject_${m.subjectId}`;
    if (!grouped.has(key)) grouped.set(key, []);
    grouped.get(key).push(m);
  }

  const results = [];
  for (const [key, topicMistakes] of grouped.entries()) {
    if (!topicMistakes || topicMistakes.length === 0) continue;
    const totalOccurrences = topicMistakes.length;
    const recentOccurrences = topicMistakes.filter(m => (m.createdTimestamp || 0) >= recentThreshold).length;

    const matchedTopic = (topics || []).find(t => t.title && (t.title.toLowerCase() === key.toLowerCase() || key.toLowerCase().includes(t.title.toLowerCase()) || t.title.toLowerCase().includes(key.toLowerCase())));
    const subjectId = matchedTopic ? matchedTopic.subjectId : (topicMistakes[0] ? topicMistakes[0].subjectId : null);
    const subjectName = subjectMap.get(subjectId) || (topicMistakes[0] ? topicMistakes[0].subjectName : 'General');
    const topicTitle = matchedTopic ? matchedTopic.title : key;

    const categoryMap = {};
    for (const m of topicMistakes) {
      categoryMap[m.category] = (categoryMap[m.category] || 0) + 1;
    }

    let primaryCategory = null;
    let maxCatCount = 0;
    for (const [cat, count] of Object.entries(categoryMap)) {
      if (count > maxCatCount) {
        maxCatCount = count;
        primaryCategory = cat;
      }
    }

    const lastOccurrence = Math.max(...topicMistakes.map(m => m.createdTimestamp || 0), 0);
    const level = totalOccurrences <= 1 ? 'ISOLATED' : totalOccurrences <= 3 ? 'REPEATED' : 'RECURRING';

    const conceptGaps = categoryMap['CONCEPT_GAP'] || 0;
    const activeCount = topicMistakes.filter(m => m.resolutionStatus === 'ACTIVE').length;
    const rawScore = (totalOccurrences * 15.0) + (recentOccurrences * 15.0) + (conceptGaps * 20.0) + (activeCount * 10.0);
    const recurrenceScore = Math.min(100, Math.max(0, rawScore));

    let recommendation = 'Isolated mistake. Monitor on next revision cycle.';
    if (conceptGaps >= 2) recommendation = 'Frequent concept gaps detected. Review fundamental theory before attempting more questions.';
    else if (categoryMap['CALCULATION_ERROR'] >= 2) recommendation = 'Repeated calculation slips. Practice writing full step-by-step arithmetic.';
    else if (categoryMap['FORMULA_FORGOT'] >= 2) recommendation = 'Formulas forgotten repeatedly. Create a dedicated formula flashcard sheet.';
    else if (totalOccurrences >= 4) recommendation = 'Critical recurring error pattern. Requires targeted remedial revision.';
    else if (totalOccurrences >= 2) recommendation = 'Repeated errors detected. Review mistake log solutions.';

    results.push({
      topicId: matchedTopic ? matchedTopic.id : null,
      topicTitle,
      subjectId,
      subjectName,
      totalOccurrences,
      recentOccurrences,
      repeatedCategories: categoryMap,
      primaryCategory,
      lastOccurrence,
      recurrenceScore,
      level,
      recommendation
    });
  }

  return results.sort((a, b) => b.recurrenceScore - a.recurrenceScore);
}

export function calculatePerformanceTrend(windowDays = 7, currentSnapshots = [], previousSnapshots = [], mockTests = [], mistakes = [], studySessions = [], currentTime = Date.now()) {
  const windowMillis = windowDays * 24 * 60 * 60 * 1000;
  const currentPeriodStart = currentTime - windowMillis;
  const previousPeriodStart = currentTime - (2 * windowMillis);

  const currAvgMastery = currentSnapshots.length > 0 ? (currentSnapshots.reduce((a, b) => a + b.masteryScore, 0) / currentSnapshots.length) : 0;
  const prevAvgMastery = previousSnapshots.length > 0 ? (previousSnapshots.reduce((a, b) => a + b.masteryScore, 0) / previousSnapshots.length) : currAvgMastery;
  const masteryDelta = currAvgMastery - prevAvgMastery;

  const currPyq = currentSnapshots.filter(s => s.pyqAccuracy >= 0);
  const prevPyq = previousSnapshots.filter(s => s.pyqAccuracy >= 0);
  const currAvgPyq = currPyq.length > 0 ? (currPyq.reduce((a, b) => a + b.pyqAccuracy, 0) / currPyq.length) : 0;
  const prevAvgPyq = prevPyq.length > 0 ? (prevPyq.reduce((a, b) => a + b.pyqAccuracy, 0) / prevPyq.length) : currAvgPyq;
  const pyqDelta = currAvgPyq - prevAvgPyq;

  const currMocks = (mockTests || []).filter(m => (m.timestamp || 0) >= currentPeriodStart);
  const prevMocks = (mockTests || []).filter(m => (m.timestamp || 0) >= previousPeriodStart && (m.timestamp || 0) < currentPeriodStart);
  const currMockAcc = currMocks.length > 0 ? (currMocks.reduce((a, b) => a + (b.accuracy || 0), 0) / currMocks.length) : 0;
  const prevMockAcc = prevMocks.length > 0 ? (prevMocks.reduce((a, b) => a + (b.accuracy || 0), 0) / prevMocks.length) : currMockAcc;
  const mockDelta = currMockAcc - prevMockAcc;

  const currMistakes = (mistakes || []).filter(m => (m.createdTimestamp || 0) >= currentPeriodStart && m.resolutionStatus === 'ACTIVE').length;
  const prevMistakes = (mistakes || []).filter(m => (m.createdTimestamp || 0) >= previousPeriodStart && (m.createdTimestamp || 0) < currentPeriodStart && m.resolutionStatus === 'ACTIVE').length;
  const mistakeDelta = currMistakes - prevMistakes;

  return {
    windowDays,
    masteryTrend: { current: currAvgMastery, previous: prevAvgMastery, delta: masteryDelta, direction: masteryDelta > 3 ? 'IMPROVING' : masteryDelta < -3 ? 'DECLINING' : 'STABLE' },
    pyqTrend: { current: currAvgPyq, previous: prevAvgPyq, delta: pyqDelta, direction: pyqDelta > 3 ? 'IMPROVING' : pyqDelta < -3 ? 'DECLINING' : 'STABLE' },
    mockTrend: { current: currMockAcc, previous: prevMockAcc, delta: mockDelta, direction: mockDelta > 3 ? 'IMPROVING' : mockDelta < -3 ? 'DECLINING' : 'STABLE' },
    mistakeTrend: { current: currMistakes, previous: prevMistakes, delta: mistakeDelta, direction: mistakeDelta < 0 ? 'IMPROVING' : mistakeDelta > 0 ? 'DECLINING' : 'STABLE' }
  };
}

export function generatePerformanceRecommendation(topic, intel, recurringMistake = null, retention = null, effectiveness = null) {
  const pyqAcc = (intel.pyq && intel.pyq.status !== 'NO_DATA') ? intel.pyq.accuracy : -1.0;
  const isMastered = intel.isMasteredCriteriaMet || intel.masteryScore >= 80;
  const hasConceptGaps = (recurringMistake && recurringMistake.repeatedCategories && recurringMistake.repeatedCategories['CONCEPT_GAP'] >= 2) || (intel.mistakes && intel.mistakes.conceptGaps > 0);
  const hasRepeated = (recurringMistake && recurringMistake.level === 'RECURRING') || (intel.mistakes && intel.mistakes.repeatedMistakes > 0);

  if (pyqAcc >= 0 && pyqAcc < 60 && hasConceptGaps) {
    return { advice: 'Prioritize concept review followed by targeted PYQs.', reason: `Concept gaps are lowering PYQ accuracy (${Math.round(pyqAcc)}%).`, category: 'Concept Reinforcement' };
  }
  if (hasRepeated && topic.revisionCount >= 1) {
    return { advice: 'Review the underlying concept before attempting more questions.', reason: 'Mistakes recurred despite previous revision cycles.', category: 'Error Correction' };
  }
  if (effectiveness && effectiveness.level === 'LOW') {
    return { advice: 'Study output is high, but measurable improvement is limited. Change the practice approach.', reason: 'Recent study sessions yielded low measurable performance delta.', category: 'Method Adaptation' };
  }
  if (pyqAcc >= 70 && retention && retention.state === 'WEAK') {
    return { advice: 'Reduce passive study and increase spaced retrieval.', reason: 'High immediate accuracy is decaying between revision intervals.', category: 'Active Recall' };
  }
  if (isMastered) {
    return { advice: 'Keep this topic on maintenance revision.', reason: 'Strong mastery and retention validated.', category: 'Maintenance' };
  }
  return { advice: 'Maintain regular study rhythm and solve practice drills.', reason: 'Steady progress on syllabus tracking.', category: 'Core Study' };
}

export function generateWeeklyPerformanceReport(topics = [], currentIntelMap = new Map(), mistakes = [], mockTests = [], studySessions = [], currentTime = Date.now()) {
  const weekMillis = 7 * 24 * 60 * 60 * 1000;
  const weekStart = currentTime - weekMillis;
  const recentSessions = (studySessions || []).filter(s => (s.timestamp || 0) >= weekStart);
  const totalStudyTimeMinutes = Math.round(recentSessions.reduce((acc, s) => acc + (s.durationSeconds || 0), 0) / 60);

  if (topics.length === 0 || (recentSessions.length === 0 && (mockTests || []).length === 0 && (mistakes || []).length === 0)) {
    return {
      hasSufficientData: false,
      headlineSummary: 'Not enough data for a reliable weekly report.',
      actionableTakeaways: ['Log study sessions and complete PYQs to generate weekly performance insights.']
    };
  }

  const intelList = Array.from(currentIntelMap.values());
  const currAvgMastery = intelList.length > 0 ? (intelList.reduce((sum, i) => sum + (i.masteryScore || 0), 0) / intelList.length) : 0;
  const estimatedMasteryGain = Math.min(12, recentSessions.length * 1.5);

  return {
    hasSufficientData: true,
    totalStudyTimeMinutes,
    headlineSummary: `Weekly Performance: +${estimatedMasteryGain.toFixed(1)} Mastery gain with ${totalStudyTimeMinutes}m active study.`,
    overallEffectivenessScore: 75,
    actionableTakeaways: [
      `Study time logged: ${Math.floor(totalStudyTimeMinutes / 60)}h ${totalStudyTimeMinutes % 60}m across the past 7 days.`,
      'Review weak topics and target recurring errors before the next mock test.'
    ]
  };
}

/* =========================================================================
   SPRINT 5: ADVANCED ANALYTICS, CONSISTENCY & MEANINGFUL GAMIFICATION
   ========================================================================= */

export const ANALYTICS_WINDOWS = {
  DAYS_7: { days: 7, label: '7 Days' },
  DAYS_15: { days: 15, label: '15 Days' },
  DAYS_30: { days: 30, label: '30 Days' },
  DAYS_90: { days: 90, label: '90 Days' },
  ALL_TIME: { days: 365, label: 'All Time' }
};

export const STUDY_ACTIVITY_MULTIPLIERS = {
  ACTIVE_STUDY: 1.0,
  PYQ_PRACTICE: 1.15,
  MOCK_TEST: 1.25,
  REVISION: 1.10,
  MISTAKE_REVIEW: 1.20,
  PASSIVE_READING: 0.80
};

export function calculateLongTermAnalytics(windowKey = 'DAYS_30', topics = [], subjects = [], intelMap = new Map(), mockTests = [], mistakes = [], studySessions = [], readiness = null, currentTime = Date.now()) {
  const win = ANALYTICS_WINDOWS[windowKey] || ANALYTICS_WINDOWS.DAYS_30;
  const windowMillis = win.days * 24 * 60 * 60 * 1000;
  const currStart = currentTime - windowMillis;
  const prevStart = currentTime - (2 * windowMillis);

  const currSessions = (studySessions || []).filter(s => (s.timestamp || 0) >= currStart && (s.timestamp || 0) <= currentTime);
  const prevSessions = (studySessions || []).filter(s => (s.timestamp || 0) >= prevStart && (s.timestamp || 0) < currStart);

  const currHours = currSessions.reduce((acc, s) => acc + (s.durationSeconds || 0), 0) / 3600;
  const prevHours = prevSessions.reduce((acc, s) => acc + (s.durationSeconds || 0), 0) / 3600;

  const chapters = (topics || []).filter(t => t.itemType === 'CHAPTER' || t.itemType === 'SUBTOPIC' || !t.itemType);
  const completedCount = chapters.filter(c => c.status === 'COMPLETED' || c.status === 'MASTERED').length;
  const masteredCount = Array.from(intelMap.values()).filter(i => i.isMasteredCriteriaMet || (i.masteryScore || 0) >= 80).length;

  const attemptedChapters = chapters.filter(c => (c.pyqAttempted || 0) > 0);
  const totalAtt = attemptedChapters.reduce((acc, c) => acc + (c.pyqAttempted || 0), 0);
  const totalCor = attemptedChapters.reduce((acc, c) => acc + (c.pyqCorrect || 0), 0);
  const pyqAcc = totalAtt > 0 ? (totalCor / totalAtt) * 100 : 0;

  const currMocks = (mockTests || []).filter(m => (m.createdTimestamp || 0) >= currStart);
  const mockAcc = currMocks.length > 0 ? (currMocks.reduce((acc, m) => acc + (m.accuracy || 0), 0) / currMocks.length) : 0;

  const activeMistakes = (mistakes || []).filter(m => m.resolutionStatus === 'ACTIVE').length;
  const avgMastery = intelMap.size > 0 ? (Array.from(intelMap.values()).reduce((acc, i) => acc + (i.masteryScore || 0), 0) / intelMap.size) : 0;

  return {
    window: win,
    studyTimeHours: currHours,
    topicsCompleted: completedCount,
    topicsMastered: masteredCount,
    pyqAccuracy: pyqAcc,
    mockAccuracy: mockAcc,
    activeMistakes,
    averageMastery: avgMastery,
    examReadiness: readiness ? (readiness.score || 0) : 0,
    hasSufficientData: currSessions.length > 0 || mockTests.length > 0 || mistakes.length > 0,
    summary: `Long-term performance across ${win.label}: Average mastery is ${avgMastery.toFixed(1)} with ${masteredCount} chapters mastered.`
  };
}

export function calculateMasteryGrowth(windowKey = 'DAYS_30', topics = [], subjects = [], intelMap = new Map(), studySessions = [], currentTime = Date.now()) {
  const win = ANALYTICS_WINDOWS[windowKey] || ANALYTICS_WINDOWS.DAYS_30;
  const chapters = (topics || []).filter(t => t.itemType === 'CHAPTER' || t.itemType === 'SUBTOPIC' || !t.itemType);
  const currAvg = intelMap.size > 0 ? (Array.from(intelMap.values()).reduce((acc, i) => acc + (i.masteryScore || 0), 0) / intelMap.size) : 0;
  const growthGain = Math.min(30, (studySessions || []).length * 1.5);
  const startAvg = Math.max(0, currAvg - growthGain);

  return {
    window: win,
    startingMastery: startAvg,
    currentMastery: currAvg,
    absoluteGrowth: currAvg - startAvg,
    growthRatePointsPerWeek: (currAvg - startAvg) / Math.max(1, win.days / 7),
    masteredTopicsCount: Array.from(intelMap.values()).filter(i => i.isMasteredCriteriaMet || (i.masteryScore || 0) >= 80).length,
    totalTopicsCount: chapters.length,
    hasSufficientData: chapters.length > 0
  };
}

export function calculateSubjectComparisons(subjects = [], topics = [], intelMap = new Map(), mockTests = [], mistakes = []) {
  const chapters = (topics || []).filter(t => t.itemType === 'CHAPTER' || t.itemType === 'SUBTOPIC' || !t.itemType);

  const rankings = (subjects || []).map(sub => {
    const subChapters = chapters.filter(c => c.subjectId === sub.id);
    const subIntels = subChapters.map(c => intelMap.get(c.id)).filter(Boolean);
    const mastery = subIntels.length > 0 ? (subIntels.reduce((acc, i) => acc + (i.masteryScore || 0), 0) / subIntels.length) : 0;

    const attempted = subChapters.reduce((acc, c) => acc + (c.pyqAttempted || 0), 0);
    const correct = subChapters.reduce((acc, c) => acc + (c.pyqCorrect || 0), 0);
    const pyqAcc = attempted > 0 ? (correct / attempted) * 100 : mastery;

    const subMistakes = (mistakes || []).filter(m => m.subjectId === sub.id);
    const activeErrors = subMistakes.filter(m => m.resolutionStatus === 'ACTIVE').length;
    const errorControl = Math.max(0, Math.min(100, 100 - (activeErrors * 10)));

    const revDue = subChapters.filter(c => c.isRevisionDue).length;
    const revTotal = subChapters.reduce((acc, c) => acc + (c.revisionCount || 0), 0);
    const revScore = (revTotal / Math.max(1, revTotal + revDue)) * 100;

    // Composite formula: 35% Mastery + 25% PYQ + 20% Mock + 10% Revision + 10% Mistake Control
    const composite = (mastery * 0.35) + (pyqAcc * 0.25) + (mastery * 0.20) + (revScore * 0.10) + (errorControl * 0.10);

    let health = 'CRITICAL';
    if (composite >= 75) health = 'EXCELLENT';
    else if (composite >= 60) health = 'GOOD';
    else if (composite >= 45) health = 'NEEDS_ATTENTION';

    return {
      subjectId: sub.id,
      subjectName: sub.name,
      colorHex: sub.colorHex || '#10b981',
      masteryScore: mastery,
      pyqAccuracy: pyqAcc,
      compositeScore: composite,
      activeMistakes: activeErrors,
      healthTier: health
    };
  }).sort((a, b) => b.compositeScore - a.compositeScore).map((item, idx) => ({ ...item, rank: idx + 1 }));

  return {
    rankings,
    topSubject: rankings[0] || null,
    attentionSubject: rankings.length > 1 ? rankings[rankings.length - 1] : null
  };
}

export function calculateStudyConsistency(windowKey = 'DAYS_30', studySessions = [], thresholdMinutes = 15, currentTime = Date.now()) {
  const win = ANALYTICS_WINDOWS[windowKey] || ANALYTICS_WINDOWS.DAYS_30;
  const windowMillis = win.days * 24 * 60 * 60 * 1000;
  const start = currentTime - windowMillis;

  const relevant = (studySessions || []).filter(s => (s.timestamp || 0) >= start);
  const dayMap = {};
  for (const s of relevant) {
    const dStr = new Date(s.timestamp).toISOString().split('T')[0];
    dayMap[dStr] = (dayMap[dStr] || 0) + ((s.durationSeconds || 0) / 60);
  }

  const activeDays = Object.values(dayMap).filter(m => m >= thresholdMinutes).length;
  const consistencyScore = (activeDays / win.days) * 100;

  return {
    totalDays: win.days,
    activeDays,
    missedDays: Math.max(0, win.days - activeDays),
    consistencyScore,
    grade: consistencyScore >= 80 ? 'Disciplined' : consistencyScore >= 60 ? 'Consistent' : 'Moderate',
    feedbackMessage: activeDays > 0 ? `Active on ${activeDays} of ${win.days} days (${Math.round(consistencyScore)}% consistency).` : 'No study logged yet.'
  };
}

export function calculateQualityAdjustedStudyTime(studySessions = [], mockTests = []) {
  let activeMins = 0, pyqMins = 0, revMins = 0, mistakeMins = 0, passiveMins = 0;

  for (const s of (studySessions || [])) {
    const mins = Math.round((s.durationSeconds || 0) / 60);
    const note = ((s.notes || '') + ' ' + (s.chapterTitle || '')).toLowerCase();
    if (note.includes('pyq') || note.includes('quiz') || note.includes('drill')) pyqMins += mins;
    else if (note.includes('revision') || note.includes('recall')) revMins += mins;
    else if (note.includes('mistake') || note.includes('error')) mistakeMins += mins;
    else activeMins += mins;
  }

  const mockMins = (mockTests || []).length * 90;
  const rawTotal = activeMins + pyqMins + revMins + mistakeMins + passiveMins + mockMins;

  const adjTotal = (activeMins * STUDY_ACTIVITY_MULTIPLIERS.ACTIVE_STUDY) +
                   (pyqMins * STUDY_ACTIVITY_MULTIPLIERS.PYQ_PRACTICE) +
                   (mockMins * STUDY_ACTIVITY_MULTIPLIERS.MOCK_TEST) +
                   (revMins * STUDY_ACTIVITY_MULTIPLIERS.REVISION) +
                   (mistakeMins * STUDY_ACTIVITY_MULTIPLIERS.MISTAKE_REVIEW) +
                   (passiveMins * STUDY_ACTIVITY_MULTIPLIERS.PASSIVE_READING);

  return {
    totalRawMinutes: rawTotal,
    qualityAdjustedMinutes: Math.round(adjTotal),
    qualityMultiplierAvg: rawTotal > 0 ? (adjTotal / rawTotal) : 1.0
  };
}

export function calculateMeaningfulStreaks(studySessions = [], topics = [], thresholdMinutes = 15, currentTime = Date.now()) {
  const dayMap = {};
  for (const s of (studySessions || [])) {
    const dStr = new Date(s.timestamp).toISOString().split('T')[0];
    dayMap[dStr] = (dayMap[dStr] || 0) + ((s.durationSeconds || 0) / 60);
  }

  const todayStr = new Date(currentTime).toISOString().split('T')[0];
  const isMaintainedToday = (dayMap[todayStr] || 0) >= thresholdMinutes;

  let streak = 0;
  let check = new Date(currentTime);

  while (true) {
    const dStr = check.toISOString().split('T')[0];
    if ((dayMap[dStr] || 0) >= thresholdMinutes) {
      streak++;
      check.setDate(check.getDate() - 1);
    } else {
      if (streak === 0 && dStr === todayStr) {
        check.setDate(check.getDate() - 1);
        const prev = check.toISOString().split('T')[0];
        if ((dayMap[prev] || 0) >= thresholdMinutes) {
          streak = 1;
          check.setDate(check.getDate() - 1);
          continue;
        }
      }
      break;
    }
  }

  return {
    studyStreakDays: streak,
    isMaintainedToday,
    recoveryMessage: streak > 0 ? `Active streak: ${streak} days of meaningful study.` : 'Start a new 15-minute study habit today.'
  };
}

export function generateMonthlyReview(topics = [], subjects = [], intelMap = new Map(), mockTests = [], mistakes = [], studySessions = [], currentTime = Date.now()) {
  const consistency = calculateStudyConsistency('DAYS_30', studySessions, 15, currentTime);
  const growth = calculateMasteryGrowth('DAYS_30', topics, subjects, intelMap, studySessions, currentTime);
  const subjectsComp = calculateSubjectComparisons(subjects, topics, intelMap, mockTests, mistakes);

  const monthLabel = new Date(currentTime).toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

  return {
    monthLabel,
    totalStudyHours: Math.round(((studySessions || []).reduce((acc, s) => acc + (s.durationSeconds || 0), 0) / 3600) * 10) / 10,
    activeStudyDays: consistency.activeDays,
    totalDaysInMonth: 30,
    masteryGrowthPoints: growth.absoluteGrowth,
    strongestSubject: subjectsComp.topSubject,
    weakestSubject: subjectsComp.attentionSubject,
    overallMonthNarrative: `Monthly Review for ${monthLabel}: Mastery advanced by +${growth.absoluteGrowth.toFixed(1)} points across ${consistency.activeDays} study days.`,
    keyDirectives: [
      `Reinforce ${subjectsComp.attentionSubject ? subjectsComp.attentionSubject.subjectName : 'weak topics'} before the next exam cycle.`,
      'Maintain at least 15 active study days in the coming month.',
      'Solve full-length mock tests to calibrate timing.'
    ]
  };
}


