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

