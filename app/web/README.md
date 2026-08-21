# Syllabus Tracker - Responsive Web Version

This directory contains the standalone, responsive web application for the **Syllabus Tracker**. It runs independently from the Android app while replicating all features, navigation screens, and user experience.

## Features Included in Web Version
- **Dashboard**: Target exam countdown, overall syllabus progress, subject progress cards, and quick action grid.
- **Syllabus Hierarchy**: Subject management, section & chapter trees, chapter detail modal with confidence ratings and PYQ tracking.
- **Digital Error Diary**: Question mistake logger with categories (Calculation, Concept Gap, Silly Mistake, etc.) and resolution tracking.
- **Mock Test Tracker**: Score entry, percentile trends, and cutoff clearance stats.
- **Spaced Repetition Revision Manager**: 1d, 3d, 7d, 14d, 30d interval scheduler with 1-click revision actions.
- **Study Planner & Calendar**: Task scheduling and interactive monthly study calendar.
- **Focus Study Timer**: Interactive Pomodoro timer & Stopwatch mode with session recorder.
- **Analytics**: Subject-wise PYQ accuracy breakdown and exam required daily pace velocity.
- **Weak Topics Manager**: Filtered view of low-confidence topics.
- **Profile & Gamified Badges**: Achievement unlock tracking and aspirant profile.
- **Data Backup & Restore**: Full JSON export and import capabilities for data management.

## File Structure
- `index.html`: Main HTML single page application structure.
- `css/styles.css`: Material 3 inspired responsive CSS with Light & Dark themes.
- `js/data.js`: Initial sample competitive exam data and LocalStorage repository.
- `js/state.js`: Centralized reactive state bus and event system.
- `js/app.js`: Main router, controller, and global modal bindings.
- `js/components/`: Modular screen view renderers.
