package com.example.data.local

import com.example.data.model.*

object PreloadData {
    val defaultSubjects = listOf(
        Subject(
            id = 1,
            name = "GS — General Studies",
            code = "GS",
            iconName = "School",
            colorHex = "#2D4F1E",
            orderIndex = 0,
            description = "History, Geography, Polity, Economics, General Science, Environment, Static GK"
        ),
        Subject(
            id = 2,
            name = "English",
            code = "ENG",
            iconName = "MenuBook",
            colorHex = "#3F51B5",
            orderIndex = 1,
            description = "Grammar, Vocabulary, Reading Comprehension, Error Detection, Cloze Test"
        ),
        Subject(
            id = 3,
            name = "Reasoning",
            code = "REAS",
            iconName = "Psychology",
            colorHex = "#8E24AA",
            orderIndex = 2,
            description = "Verbal Reasoning, Non-Verbal Reasoning, Analytical & Logical Reasoning"
        ),
        Subject(
            id = 4,
            name = "Maths",
            code = "MATH",
            iconName = "Calculate",
            colorHex = "#E27D60",
            orderIndex = 3,
            description = "Arithmetic, Advanced Maths, Algebra, Geometry, Trigonometry, Data Interpretation"
        ),
        Subject(
            id = 5,
            name = "Computer",
            code = "COMP",
            iconName = "Computer",
            colorHex = "#00897B",
            orderIndex = 4,
            description = "Fundamentals, Hardware, OS, Networking, MS Office, Cyber Security"
        )
    )

    fun createDefaultSyllabusItems(): List<SyllabusItem> {
        val items = mutableListOf<SyllabusItem>()
        var idCounter = 1L
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L

        // Helper to add tree
        fun addSection(
            subjectId: Long,
            title: String,
            subsections: List<Pair<String, List<String>>>
        ) {
            val sectionId = idCounter++
            items.add(
                SyllabusItem(
                    id = sectionId,
                    subjectId = subjectId,
                    parentId = null,
                    itemType = ItemType.SECTION,
                    title = title,
                    orderIndex = items.size
                )
            )

            for ((subTitle, chapters) in subsections) {
                val subId = idCounter++
                items.add(
                    SyllabusItem(
                        id = subId,
                        subjectId = subjectId,
                        parentId = sectionId,
                        itemType = ItemType.SUBSECTION,
                        title = subTitle,
                        orderIndex = items.size
                    )
                )

                for (chapterName in chapters) {
                    val chapterId = idCounter++
                    // Assign realistic initial statuses for high engagement
                    val isDemoDone = (chapterId % 4 == 0L)
                    val isDemoLearning = (chapterId % 5 == 0L)
                    val isDemoWeak = (chapterId % 9 == 0L)
                    val isDemoRevDue = (chapterId % 7 == 0L)
                    
                    val status = when {
                        isDemoRevDue -> ChapterStatus.REVISION_DUE
                        isDemoWeak -> ChapterStatus.WEAK
                        isDemoDone -> ChapterStatus.COMPLETED
                        isDemoLearning -> ChapterStatus.IN_PROGRESS
                        else -> ChapterStatus.NOT_STARTED
                    }

                    val completion = when (status) {
                        ChapterStatus.COMPLETED -> 100
                        ChapterStatus.MASTERED -> 100
                        ChapterStatus.REVISION_DUE -> 100
                        ChapterStatus.IN_PROGRESS -> 50
                        ChapterStatus.LEARNING -> 25
                        ChapterStatus.WEAK -> 30
                        ChapterStatus.NOT_STARTED -> 0
                    }

                    val confidence = when (status) {
                        ChapterStatus.COMPLETED, ChapterStatus.MASTERED -> 4
                        ChapterStatus.WEAK -> 2
                        ChapterStatus.IN_PROGRESS -> 3
                        else -> 3
                    }

                    items.add(
                        SyllabusItem(
                            id = chapterId,
                            subjectId = subjectId,
                            parentId = subId,
                            itemType = ItemType.CHAPTER,
                            title = chapterName,
                            orderIndex = items.size,
                            status = status,
                            completionPercentage = completion,
                            confidence = confidence,
                            priority = if (chapterId % 3 == 0L) Priority.HIGH else Priority.MEDIUM,
                            difficulty = if (chapterId % 4 == 0L) Difficulty.HARD else Difficulty.MEDIUM,
                            lastStudiedTimestamp = if (completion > 0) now - (chapterId % 10) * oneDay else null,
                            nextRevisionTimestamp = if (status == ChapterStatus.REVISION_DUE) now - oneDay else if (status == ChapterStatus.COMPLETED) now + 3 * oneDay else null,
                            studyTimeMinutes = if (completion > 0) (chapterId.toInt() % 5 + 1) * 35 else 0,
                            revisionCount = if (status == ChapterStatus.COMPLETED || status == ChapterStatus.REVISION_DUE) 2 else 0,
                            tags = if (chapterId % 2 == 0L) "#PYQ,#Important" else "#Concept",
                            pyqTotal = 50,
                            pyqAttempted = if (completion > 0) 30 else 0,
                            pyqCorrect = if (completion > 0) (if (status == ChapterStatus.WEAK) 12 else 24) else 0,
                            notes = "Key points, definitions, and previous year examination trends."
                        )
                    )
                }
            }
        }

        // GS Syllabus
        addSection(
            subjectId = 1,
            title = "History",
            subsections = listOf(
                "Ancient History" to listOf("Indus Valley Civilization", "Vedic Age & Early States", "Mauryan Empire & Ashoka", "Gupta Empire & Post-Gupta"),
                "Medieval History" to listOf("Delhi Sultanate Dynasties", "Mughal Empire & Architecture", "Vijayanagara & Bahmani Kingdoms", "Bhakti & Sufi Movements"),
                "Modern History" to listOf("Revolt of 1857", "Socio-Religious Reform Movements", "Indian National Congress & Freedom Struggle", "Gandhian Era & Mass Movements")
            )
        )
        addSection(
            subjectId = 1,
            title = "Geography",
            subsections = listOf(
                "Physical & Indian Geography" to listOf("Physiographic Divisions of India", "River Systems & Drainage", "Climate & Monsoons", "Soils & Natural Vegetation"),
                "Economic & World Geography" to listOf("Agriculture & Major Crops", "Minerals & Energy Resources", "Major World Climatic Regions", "Oceans, Currents & Tides")
            )
        )
        addSection(
            subjectId = 1,
            title = "Polity & Constitution",
            subsections = listOf(
                "Constitutional Framework" to listOf("Historical Background & Preamble", "Fundamental Rights & Duties", "Directive Principles of State Policy (DPSP)", "Amendment of Constitution"),
                "Union & State Executive" to listOf("President, Vice President & Prime Minister", "Parliament & Legislative Procedures", "Supreme Court & High Courts", "Constitutional & Non-Constitutional Bodies")
            )
        )
        addSection(
            subjectId = 1,
            title = "Economics",
            subsections = listOf(
                "Core Concepts" to listOf("National Income, GDP & GNP", "Inflation, Indices (CPI/WPI)", "Monetary Policy & RBI", "Fiscal Policy & Budget"),
                "Sectors & Schemes" to listOf("Banking & Financial Markets", "Taxation & GST Reforms", "Poverty, Unemployment & Flagship Schemes", "Foreign Trade & Balance of Payments")
            )
        )
        addSection(
            subjectId = 1,
            title = "General Science & Environment",
            subsections = listOf(
                "Science" to listOf("Physics: Mechanics, Optics, Units & Dimensions", "Chemistry: Acids, Bases, Salts, Periodic Table", "Biology: Human Digestive & Circulatory Systems", "Diseases, Vitamins & Nutrition"),
                "Environment & Ecology" to listOf("Ecosystems & Biodiversity Hotspots", "National Parks, Sanctuaries & Biosphere Reserves", "Climate Change & Conventions", "Pollution Control & Waste Management")
            )
        )

        // English Syllabus
        addSection(
            subjectId = 2,
            title = "Grammar",
            subsections = listOf(
                "Syntax & Rules" to listOf("Nouns, Pronouns & Articles", "Tenses & Conditional Sentences", "Subject-Verb Agreement", "Active & Passive Voice", "Direct & Indirect Speech", "Prepositions & Conjunctions")
            )
        )
        addSection(
            subjectId = 2,
            title = "Vocabulary",
            subsections = listOf(
                "Word Power" to listOf("Synonyms & Antonyms (High Frequency)", "Idioms & Phrases with Usage", "One Word Substitution", "Phrasal Verbs & Confusing Words", "Spelling Rules & Misspelt Words")
            )
        )
        addSection(
            subjectId = 2,
            title = "Comprehension & Verbal Ability",
            subsections = listOf(
                "Application" to listOf("Reading Comprehension Passages", "Cloze Test Techniques", "Sentence Improvement", "Para Jumbles & Sentence Rearrangement", "Spotting Errors in Long Sentences")
            )
        )

        // Reasoning Syllabus
        addSection(
            subjectId = 3,
            title = "Verbal Reasoning",
            subsections = listOf(
                "Core Chapters" to listOf("Analogy & Classification", "Number, Letter & Mixed Series", "Coding & Decoding", "Blood Relations (Family Tree)", "Direction & Distance", "Order & Ranking")
            )
        )
        addSection(
            subjectId = 3,
            title = "Logical & Analytical Reasoning",
            subsections = listOf(
                "Logic & Deduction" to listOf("Syllogisms (Venn Diagram Method)", "Venn Diagrams & Set Representation", "Statement & Assumptions", "Statement & Arguments", "Course of Action & Conclusions", "Seating Arrangement & Puzzles")
            )
        )
        addSection(
            subjectId = 3,
            title = "Non-Verbal Reasoning",
            subsections = listOf(
                "Spatial & Visual" to listOf("Mirror & Water Images", "Paper Folding & Cutting", "Embedded Figures", "Pattern Completion", "Dice, Cube & Counting of Figures")
            )
        )

        // Maths Syllabus
        addSection(
            subjectId = 4,
            title = "Arithmetic",
            subsections = listOf(
                "Commercial Maths" to listOf("Number System & Divisibility Rules", "LCM & HCF Problems", "Percentage & Conversions", "Profit, Loss & Discount", "Simple & Compound Interest", "Ratio, Proportion & Partnership", "Averages, Ages & Mixtures", "Time & Work, Pipes & Cisterns", "Time, Speed, Distance, Trains & Boats")
            )
        )
        addSection(
            subjectId = 4,
            title = "Advanced Mathematics",
            subsections = listOf(
                "Algebra & Geometry" to listOf("Algebra: Identities & Polynomials", "Linear & Quadratic Equations", "Geometry: Lines, Angles, Triangles", "Circles, Chords & Tangents", "Mensuration 2D (Area & Perimeter)", "Mensuration 3D (Surface Area & Volume)"),
                "Trigonometry & Statistics" to listOf("Trigonometric Ratios & Identities", "Heights and Distances", "Data Interpretation (Tables, Bar, Pie Charts)", "Elementary Statistics: Mean, Median, Mode")
            )
        )

        // Computer Syllabus
        addSection(
            subjectId = 5,
            title = "Computer Fundamentals",
            subsections = listOf(
                "Basics & Architecture" to listOf("Generations & Types of Computers", "CPU, ALU, Registers & Bus Architecture", "Memory: RAM, ROM, Cache & Storage Devices", "Input, Output & Peripheral Devices")
            )
        )
        addSection(
            subjectId = 5,
            title = "Software & Operating Systems",
            subsections = listOf(
                "OS & Applications" to listOf("System vs Application Software", "Windows OS & File Management", "Linux Basics & Shell Commands", "Process & Memory Management Concepts")
            )
        )
        addSection(
            subjectId = 5,
            title = "Networking & Cyber Security",
            subsections = listOf(
                "Internet & Security" to listOf("OSI & TCP/IP Model Layers", "Network Topologies & IP Addressing", "Web Protocols: HTTP, HTTPS, FTP, DNS", "Cyber Threats: Malware, Phishing, Ransomware", "Firewalls, Antivirus & Cryptography Basics")
            )
        )
        addSection(
            subjectId = 5,
            title = "Productivity Tools",
            subsections = listOf(
                "MS Office Suite" to listOf("MS Word: Formatting, Headers, Mail Merge", "MS Excel: Formulas, Charts & Pivot Tables", "MS PowerPoint: Transitions, Animations", "Essential Windows & Office Shortcut Keys")
            )
        )

        return items
    }

    val defaultGoals = listOf(
        Goal(
            id = 1,
            title = "Complete Polity in 15 Days",
            targetDateStr = "2026-09-10",
            subjectId = 1,
            subjectName = "GS — General Studies",
            targetChaptersCount = 12,
            completedChaptersCount = 5,
            targetStudyHours = 30f,
            isCompleted = false
        ),
        Goal(
            id = 2,
            title = "Finish Maths Arithmetic & PYQs",
            targetDateStr = "2026-09-30",
            subjectId = 4,
            subjectName = "Maths",
            targetChaptersCount = 18,
            completedChaptersCount = 8,
            targetStudyHours = 45f,
            isCompleted = false
        ),
        Goal(
            id = 3,
            title = "Master 500 Vocabulary & Idioms",
            targetDateStr = "2026-09-15",
            subjectId = 2,
            subjectName = "English",
            targetChaptersCount = 10,
            completedChaptersCount = 4,
            targetStudyHours = 20f,
            isCompleted = false
        )
    )

    fun createSampleStudyPlans(): List<StudyPlan> {
        val today = "2026-08-20"
        return listOf(
            StudyPlan(
                id = 1,
                dateStr = today,
                timeStr = "08:00",
                subjectId = 4,
                subjectName = "Maths",
                chapterTitle = "Percentage & Conversions",
                plannedMinutes = 60,
                actualMinutes = 60,
                isCompleted = true,
                goalNotes = "Solve 30 PYQs with timer"
            ),
            StudyPlan(
                id = 2,
                dateStr = today,
                timeStr = "10:00",
                subjectId = 2,
                subjectName = "English",
                chapterTitle = "Idioms & Phrases with Usage",
                plannedMinutes = 45,
                actualMinutes = 45,
                isCompleted = true,
                goalNotes = "Flashcard revision of 50 idioms"
            ),
            StudyPlan(
                id = 3,
                dateStr = today,
                timeStr = "14:00",
                subjectId = 1,
                subjectName = "GS — General Studies",
                chapterTitle = "Fundamental Rights & Duties",
                plannedMinutes = 90,
                actualMinutes = 0,
                isCompleted = false,
                goalNotes = "Cover Article 12 to Article 35"
            ),
            StudyPlan(
                id = 4,
                dateStr = today,
                timeStr = "18:00",
                subjectId = 3,
                subjectName = "Reasoning",
                chapterTitle = "Coding & Decoding",
                plannedMinutes = 45,
                actualMinutes = 0,
                isCompleted = false,
                goalNotes = "Pattern practice set 1 & 2"
            )
        )
    }

    val defaultBadges = listOf(
        // Streaks
        AchievementBadge(
            id = "streak_3",
            title = "Spark of Discipline",
            description = "Maintain an active study streak for 3 consecutive days.",
            category = BadgeCategory.STREAK,
            iconEmoji = "🔥",
            tier = BadgeTier.BRONZE,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 2,
            currentProgress = 3,
            maxProgress = 3,
            rewardXp = 50,
            hintRequirement = "Study on 3 different consecutive days"
        ),
        AchievementBadge(
            id = "streak_7",
            title = "Week of Fire",
            description = "Complete a solid 7-day study consistency streak.",
            category = BadgeCategory.STREAK,
            iconEmoji = "⚡",
            tier = BadgeTier.SILVER,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 4,
            maxProgress = 7,
            rewardXp = 150,
            hintRequirement = "Reach a 7-day streak"
        ),
        AchievementBadge(
            id = "streak_14",
            title = "Relentless Focus",
            description = "Sustain a 14-day study streak with unwavering dedication.",
            category = BadgeCategory.STREAK,
            iconEmoji = "🌟",
            tier = BadgeTier.GOLD,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 4,
            maxProgress = 14,
            rewardXp = 300,
            hintRequirement = "Reach a 14-day streak"
        ),
        AchievementBadge(
            id = "streak_30",
            title = "Iron Will Master",
            description = "Achieve a full 30-day unbroken study streak. True exam mastery!",
            category = BadgeCategory.STREAK,
            iconEmoji = "👑",
            tier = BadgeTier.PLATINUM,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 4,
            maxProgress = 30,
            rewardXp = 600,
            hintRequirement = "Reach a 30-day streak"
        ),

        // Chapter Milestones
        AchievementBadge(
            id = "chapters_first",
            title = "First Step Taken",
            description = "Mark your very first syllabus chapter as completed.",
            category = BadgeCategory.TOPICS,
            iconEmoji = "🌱",
            tier = BadgeTier.BRONZE,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 5,
            currentProgress = 1,
            maxProgress = 1,
            rewardXp = 30,
            hintRequirement = "Complete 1 chapter"
        ),
        AchievementBadge(
            id = "chapters_10",
            title = "Foundation Builder",
            description = "Complete 10 syllabus chapters across any subjects.",
            category = BadgeCategory.TOPICS,
            iconEmoji = "🧱",
            tier = BadgeTier.BRONZE,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 1,
            currentProgress = 10,
            maxProgress = 10,
            rewardXp = 100,
            hintRequirement = "Complete 10 chapters"
        ),
        AchievementBadge(
            id = "chapters_25",
            title = "Pace Setter",
            description = "Complete 25 syllabus chapters with solid preparation.",
            category = BadgeCategory.TOPICS,
            iconEmoji = "🏃",
            tier = BadgeTier.SILVER,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 14,
            maxProgress = 25,
            rewardXp = 200,
            hintRequirement = "Complete 25 chapters"
        ),
        AchievementBadge(
            id = "chapters_50",
            title = "Halfway Conqueror",
            description = "Complete 50 comprehensive chapters across the syllabus.",
            category = BadgeCategory.TOPICS,
            iconEmoji = "⚔️",
            tier = BadgeTier.GOLD,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 14,
            maxProgress = 50,
            rewardXp = 450,
            hintRequirement = "Complete 50 chapters"
        ),
        AchievementBadge(
            id = "chapters_100",
            title = "Centurion Scholar",
            description = "Master 100 syllabus chapters with deep conceptual clarity.",
            category = BadgeCategory.TOPICS,
            iconEmoji = "🏛️",
            tier = BadgeTier.PLATINUM,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 14,
            maxProgress = 100,
            rewardXp = 1000,
            hintRequirement = "Complete 100 chapters"
        ),

        // Subject Mastery
        AchievementBadge(
            id = "subject_master_1",
            title = "Subject Champion",
            description = "Complete 100% of all chapters in any core subject.",
            category = BadgeCategory.SUBJECT,
            iconEmoji = "🏆",
            tier = BadgeTier.GOLD,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 0,
            maxProgress = 1,
            rewardXp = 350,
            hintRequirement = "Complete all chapters in 1 subject"
        ),
        AchievementBadge(
            id = "all_subjects_master",
            title = "Grandmaster of Syllabus",
            description = "Achieve 100% completion across the entire exam syllabus.",
            category = BadgeCategory.SUBJECT,
            iconEmoji = "💎",
            tier = BadgeTier.DIAMOND,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 0,
            maxProgress = 1,
            rewardXp = 2000,
            hintRequirement = "100% syllabus mastery"
        ),

        // Focus & Time
        AchievementBadge(
            id = "time_first",
            title = "First Pomodoro",
            description = "Complete your first focused study timer session.",
            category = BadgeCategory.STUDY_TIME,
            iconEmoji = "⏱️",
            tier = BadgeTier.BRONZE,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 3,
            currentProgress = 1,
            maxProgress = 1,
            rewardXp = 40,
            hintRequirement = "Log 1 study session"
        ),
        AchievementBadge(
            id = "time_5_hours",
            title = "Deep Diver",
            description = "Log at least 5 total hours (300 minutes) of focused study.",
            category = BadgeCategory.STUDY_TIME,
            iconEmoji = "🌊",
            tier = BadgeTier.SILVER,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 1,
            currentProgress = 320,
            maxProgress = 300,
            rewardXp = 150,
            hintRequirement = "Log 5 hours of study"
        ),
        AchievementBadge(
            id = "time_20_hours",
            title = "Hourglass Veteran",
            description = "Accumulate 20 hours (1200 minutes) of logged study time.",
            category = BadgeCategory.STUDY_TIME,
            iconEmoji = "⏳",
            tier = BadgeTier.GOLD,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 320,
            maxProgress = 1200,
            rewardXp = 400,
            hintRequirement = "Log 20 hours of study"
        ),
        AchievementBadge(
            id = "time_50_hours",
            title = "Time Lord Scholar",
            description = "Dedicate 50 hours (3000 minutes) of deep preparation.",
            category = BadgeCategory.STUDY_TIME,
            iconEmoji = "🌌",
            tier = BadgeTier.PLATINUM,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 320,
            maxProgress = 3000,
            rewardXp = 900,
            hintRequirement = "Log 50 hours of study"
        ),

        // Spaced Revision
        AchievementBadge(
            id = "revision_first",
            title = "Active Recall Initiate",
            description = "Complete your first spaced repetition revision session.",
            category = BadgeCategory.REVISION,
            iconEmoji = "🔄",
            tier = BadgeTier.BRONZE,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 2,
            currentProgress = 1,
            maxProgress = 1,
            rewardXp = 40,
            hintRequirement = "Revise 1 chapter"
        ),
        AchievementBadge(
            id = "revision_10",
            title = "Memory Architect",
            description = "Successfully complete 10 spaced revisions on scheduled intervals.",
            category = BadgeCategory.REVISION,
            iconEmoji = "🧠",
            tier = BadgeTier.SILVER,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 4,
            maxProgress = 10,
            rewardXp = 200,
            hintRequirement = "Revise 10 chapters"
        ),
        AchievementBadge(
            id = "revision_30",
            title = "Spaced Memory Guru",
            description = "Complete 30 spaced revisions. Forget curve conquered!",
            category = BadgeCategory.REVISION,
            iconEmoji = "🪄",
            tier = BadgeTier.GOLD,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 4,
            maxProgress = 30,
            rewardXp = 500,
            hintRequirement = "Revise 30 chapters"
        ),

        // PYQ & Confidence
        AchievementBadge(
            id = "pyq_25",
            title = "Question Cracker",
            description = "Attempt 25 Previous Year Questions (PYQs) in chapters.",
            category = BadgeCategory.PYQ,
            iconEmoji = "📝",
            tier = BadgeTier.SILVER,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 1,
            currentProgress = 30,
            maxProgress = 25,
            rewardXp = 150,
            hintRequirement = "Solve 25 PYQs"
        ),
        AchievementBadge(
            id = "pyq_100",
            title = "PYQ Specialist",
            description = "Solve 100 Previous Year Questions across subjects.",
            category = BadgeCategory.PYQ,
            iconEmoji = "🎯",
            tier = BadgeTier.GOLD,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 30,
            maxProgress = 100,
            rewardXp = 500,
            hintRequirement = "Solve 100 PYQs"
        ),

        // Goals
        AchievementBadge(
            id = "goal_first",
            title = "Target Locked",
            description = "Create and complete your first custom study target goal.",
            category = BadgeCategory.GOALS,
            iconEmoji = "🎯",
            tier = BadgeTier.BRONZE,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 0,
            maxProgress = 1,
            rewardXp = 100,
            hintRequirement = "Complete 1 study goal"
        ),
        AchievementBadge(
            id = "goal_5",
            title = "Goal Crusher",
            description = "Accomplish 5 custom deadline study goals.",
            category = BadgeCategory.GOALS,
            iconEmoji = "🚀",
            tier = BadgeTier.GOLD,
            isUnlocked = false,
            unlockedAt = null,
            currentProgress = 0,
            maxProgress = 5,
            rewardXp = 450,
            hintRequirement = "Complete 5 study goals"
        )
    )

    fun createSampleMockTests(): List<MockTest> {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        return listOf(
            MockTest(
                id = 1,
                testName = "SSC CGL 2024 Tier 1 Full Mock #01",
                testType = MockTestType.FULL_LENGTH,
                testPlatform = "Testbook",
                testDateStr = "2026-08-10",
                timestamp = now - 10 * oneDay,
                totalMarks = 200f,
                marksScored = 128.5f,
                totalQuestions = 100,
                attemptedQuestions = 84,
                correctQuestions = 68,
                incorrectQuestions = 16,
                accuracy = 80.95f,
                percentile = 86.4f,
                rank = 2840,
                totalStudents = 20850,
                cutoffMarks = 135f,
                timeTakenMinutes = 58,
                mathScore = 36f,
                mathTotal = 50f,
                englishScore = 38f,
                englishTotal = 50f,
                reasoningScore = 42.5f,
                reasoningTotal = 50f,
                gsScore = 12f,
                gsTotal = 50f,
                weakAreasIdentified = "History Chronology, Trigonometry Identities",
                analysisNotes = "Math calculation was slow. Took 28 mins on Quant alone. GS negative marks hurt total.",
                isClearedCutoff = false
            ),
            MockTest(
                id = 2,
                testName = "SSC CGL 2024 Tier 1 Full Mock #02",
                testType = MockTestType.FULL_LENGTH,
                testPlatform = "Oliveboard",
                testDateStr = "2026-08-13",
                timestamp = now - 7 * oneDay,
                totalMarks = 200f,
                marksScored = 138.0f,
                totalQuestions = 100,
                attemptedQuestions = 86,
                correctQuestions = 73,
                incorrectQuestions = 13,
                accuracy = 84.88f,
                percentile = 91.2f,
                rank = 1420,
                totalStudents = 16100,
                cutoffMarks = 134f,
                timeTakenMinutes = 57,
                mathScore = 40f,
                mathTotal = 50f,
                englishScore = 41.5f,
                englishTotal = 50f,
                reasoningScore = 44f,
                reasoningTotal = 50f,
                gsScore = 12.5f,
                gsTotal = 50f,
                weakAreasIdentified = "Polity Articles, Time & Work",
                analysisNotes = "Cleared cutoff! English vocabulary was strong. GS needs systematic revision of Polity.",
                isClearedCutoff = true
            ),
            MockTest(
                id = 3,
                testName = "Quantitative Aptitude Sectional #04",
                testType = MockTestType.SECTIONAL,
                testPlatform = "PracticeMock",
                testDateStr = "2026-08-16",
                timestamp = now - 4 * oneDay,
                totalMarks = 50f,
                marksScored = 44.0f,
                totalQuestions = 25,
                attemptedQuestions = 24,
                correctQuestions = 22,
                incorrectQuestions = 2,
                accuracy = 91.67f,
                percentile = 96.8f,
                rank = 310,
                totalStudents = 9700,
                cutoffMarks = 35f,
                timeTakenMinutes = 20,
                mathScore = 44f,
                mathTotal = 50f,
                englishScore = 0f,
                englishTotal = 0f,
                reasoningScore = 0f,
                reasoningTotal = 0f,
                gsScore = 0f,
                gsTotal = 0f,
                weakAreasIdentified = "Geometry Circles & Tangents",
                analysisNotes = "Great speed! Completed in 20 minutes with 91.7% accuracy. Arithmetic was flawless.",
                isClearedCutoff = true
            ),
            MockTest(
                id = 4,
                testName = "SSC CGL 2024 Tier 1 All India Live Mock #05",
                testType = MockTestType.FULL_LENGTH,
                testPlatform = "Testbook",
                testDateStr = "2026-08-18",
                timestamp = now - 2 * oneDay,
                totalMarks = 200f,
                marksScored = 152.5f,
                totalQuestions = 100,
                attemptedQuestions = 91,
                correctQuestions = 80,
                incorrectQuestions = 11,
                accuracy = 87.91f,
                percentile = 96.4f,
                rank = 620,
                totalStudents = 17200,
                cutoffMarks = 136f,
                timeTakenMinutes = 56,
                mathScore = 45f,
                mathTotal = 50f,
                englishScore = 43.5f,
                englishTotal = 50f,
                reasoningScore = 46f,
                reasoningTotal = 50f,
                gsScore = 18f,
                gsTotal = 50f,
                weakAreasIdentified = "Current Affairs, Economics Inflation terms",
                analysisNotes = "Personal Best! Reasoning took only 14 mins. GS improved to 18 marks. Aiming for 160+ next.",
                isClearedCutoff = true
            ),
            MockTest(
                id = 5,
                testName = "SSC CGL 2023 Tier 1 Previous Year Paper (Shift 1)",
                testType = MockTestType.PREVIOUS_YEAR,
                testPlatform = "Testbook",
                testDateStr = "2026-08-20",
                timestamp = now,
                totalMarks = 200f,
                marksScored = 158.0f,
                totalQuestions = 100,
                attemptedQuestions = 93,
                correctQuestions = 83,
                incorrectQuestions = 10,
                accuracy = 89.25f,
                percentile = 97.9f,
                rank = 410,
                totalStudents = 19500,
                cutoffMarks = 138f,
                timeTakenMinutes = 55,
                mathScore = 47.5f,
                mathTotal = 50f,
                englishScore = 45f,
                englishTotal = 50f,
                reasoningScore = 47.5f,
                reasoningTotal = 50f,
                gsScore = 18f,
                gsTotal = 50f,
                weakAreasIdentified = "Static GK Dances, Medieval Dynasties",
                analysisNotes = "Excellent performance! Maths 24/25 correct, Reasoning 24/25 correct. English RC was fully accurate.",
                isClearedCutoff = true
            )
        )
    }

    fun createSampleMistakes(): List<MistakeEntry> {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        return listOf(
            MistakeEntry(
                id = 1,
                questionText = "If x + 1/x = 3, find the value of x^5 + 1/x^5.",
                yourWrongAnswer = "143 (Calculated (x^2 + 1/x^2)(x^3 + 1/x^3) = 7 * 18 = 126 and forgot subtracting (x + 1/x))",
                correctAnswer = "123",
                explanationOrKeyConcept = "Formula: (x^5 + 1/x^5) = (x^2 + 1/x^2)(x^3 + 1/x^3) - (x + 1/x). Since x^2+1/x^2 = 3^2 - 2 = 7 and x^3+1/x^3 = 3^3 - 3(3) = 18. Value = 7 * 18 - 3 = 126 - 3 = 123.",
                subjectId = 4,
                subjectName = "Maths",
                chapterTitle = "Algebraic Identities & Powers",
                sourceMockOrBook = "SSC CGL 2023 Tier-1 Shift 1",
                category = MistakeCategory.FORMULA_FORGOT,
                resolutionStatus = MistakeResolutionStatus.ACTIVE,
                importanceStar = true,
                tagsCsv = "Algebra, Identities, Powers, Tier-1",
                createdTimestamp = now - 2 * oneDay,
                nextReviewTimestamp = now - 1 * oneDay
            ),
            MistakeEntry(
                id = 2,
                questionText = "Choose the correctly spelt word / idiom meaning for 'To bury the hatchet'.",
                yourWrongAnswer = "To hide weapons secretly before war",
                correctAnswer = "To end a quarrel and make peace with someone",
                explanationOrKeyConcept = "Origin: Native American peacemaking ceremony where war axes (hatchets) were buried underground to signify peace treaty.",
                subjectId = 2,
                subjectName = "English",
                chapterTitle = "Idioms & Phrases",
                sourceMockOrBook = "Oliveboard Live Mock #05",
                category = MistakeCategory.VOCAB_CONFUSION,
                resolutionStatus = MistakeResolutionStatus.ACTIVE,
                importanceStar = false,
                tagsCsv = "Idioms, Vocabulary, English",
                createdTimestamp = now - 3 * oneDay,
                nextReviewTimestamp = now - 1000L
            ),
            MistakeEntry(
                id = 3,
                questionText = "Which Article of the Indian Constitution deals with the Election Commission of India?",
                yourWrongAnswer = "Article 326 (Confused with Universal Adult Suffrage)",
                correctAnswer = "Article 324",
                explanationOrKeyConcept = "Part XV (Elections): Article 324 = Superintendence, direction and control of elections in ECI. Article 326 = Elections to Lok Sabha & Vidhan Sabha on basis of Adult Suffrage.",
                subjectId = 1,
                subjectName = "GS — General Studies",
                chapterTitle = "Polity — Constitutional Bodies",
                sourceMockOrBook = "Testbook Live All India Mock #12",
                category = MistakeCategory.CONCEPT_GAP,
                resolutionStatus = MistakeResolutionStatus.ACTIVE,
                importanceStar = true,
                tagsCsv = "Polity, Constitution, Articles, ECI",
                createdTimestamp = now - 1 * oneDay,
                nextReviewTimestamp = now + 2 * oneDay
            ),
            MistakeEntry(
                id = 4,
                questionText = "Statements: All doors are keys. No key is lock. Some locks are windows.\nConclusions: I. Some windows are not keys. II. No door is lock.",
                yourWrongAnswer = "Only conclusion II follows (missed Conclusion I negative deduction)",
                correctAnswer = "Both Conclusions I and II follow",
                explanationOrKeyConcept = "Since some locks are windows, and no key is lock, the window portion that is lock can never be a key. Hence 'Some windows are not keys' is 100% true.",
                subjectId = 3,
                subjectName = "Reasoning",
                chapterTitle = "Syllogisms — Negative & Possibility cases",
                sourceMockOrBook = "Pinnacle Reasoning 6800+ PYQ",
                category = MistakeCategory.SILLY_MISTAKE,
                resolutionStatus = MistakeResolutionStatus.UNDERSTOOD,
                importanceStar = false,
                tagsCsv = "Reasoning, Syllogism, Deduction",
                createdTimestamp = now - 4 * oneDay,
                nextReviewTimestamp = now + 1 * oneDay
            )
        )
    }
}

