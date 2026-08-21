package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.theme.motion.*
import com.example.ui.viewmodel.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.MistakeNotebookViewModel
import com.example.ui.viewmodel.ProfileViewModel
import com.example.ui.viewmodel.SubjectViewModel
import com.example.ui.viewmodel.TimerViewModel
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.GoalsViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.SyllabusViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val mistakeNotebookViewModel: MistakeNotebookViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val subjectViewModel: SubjectViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val goalsViewModel: GoalsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val syllabusViewModel: SyllabusViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()
    val plannerViewModel: PlannerViewModel = viewModel()

    val currentNav by mainViewModel.currentNav.collectAsState()
    val transitionDirection by mainViewModel.transitionDirection.collectAsState()
    val selectedSubjectId by mainViewModel.selectedSubjectId.collectAsState()
    val selectedChapter by syllabusViewModel.selectedChapter.collectAsState()
    val overallStats by analyticsViewModel.overallStats.collectAsState()
    val mistakeStats by mistakeNotebookViewModel.mistakeStats.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()
    val items by syllabusViewModel.items.collectAsState()
    val appSettings by settingsViewModel.appSettings.collectAsState()
    val snackbarMsg by mainViewModel.snackbarMessage.collectAsState()
    
    val allBadges by profileViewModel.allBadges.collectAsState()

    var showQuickAddSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Automatically hide keyboard and clear focus when switching screens to avoid IME warning issues
    LaunchedEffect(currentNav) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    // Intercept hardware/gesture back press for smooth page back transitions
    BackHandler(enabled = mainViewModel.canNavigateBack()) {
        mainViewModel.navigateBack()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val unlockedBadgesCount = remember(allBadges) {
        allBadges.count { it.isUnlocked }
    }

    CompositionLocalProvider(LocalReducedMotion provides appSettings.reducedMotion) {
        // Build data-driven bottom navigation items: Home + dynamic subjects + Tools + Profile
        val navItems = remember(subjects, overallStats, unlockedBadgesCount, onSurfaceVariantColor) {
            val list = mutableListOf<BottomNavItem>()

            // 1. Home
            list.add(
                BottomNavItem(
                    id = "dest_dashboard",
                    label = "Home",
                    icon = Icons.Outlined.Dashboard,
                    selectedIcon = Icons.Filled.Dashboard,
                    accentColor = BrandForestGreen,
                    testTag = "bottom_nav_home"
                )
            )

            // 2. Data-driven Subject Sections (GS, English, Reasoning, Maths, Computer, Custom Subjects)
            subjects.forEach { subject ->
                val subColor = try {
                    Color(android.graphics.Color.parseColor(subject.colorHex))
                } catch (e: Exception) {
                    BrandForestGreen
                }
                list.add(
                    BottomNavItem(
                        id = "subject_${subject.id}",
                        label = if (subject.code.isNotBlank()) subject.code else subject.name.take(9),
                        icon = getSubjectIcon(subject.iconName),
                        selectedIcon = getSubjectIcon(subject.iconName),
                        accentColor = subColor,
                        extraData = subject,
                        testTag = "bottom_nav_subject_${subject.id}"
                    )
                )
            }

            // 3. Essential Tools & Features
            list.add(
                BottomNavItem(
                    id = "dest_mistakes",
                    label = "Error Diary",
                    icon = Icons.Outlined.BookmarkRemove,
                    selectedIcon = Icons.Filled.BookmarkRemove,
                    badgeCount = if (mistakeStats.reviewDueCount > 0) mistakeStats.reviewDueCount else null,
                    badgeColor = StatusWeak,
                    accentColor = StatusWeak,
                    testTag = "bottom_nav_mistakes"
                )
            )

            list.add(
                BottomNavItem(
                    id = "dest_mock_tests",
                    label = "Mock Tests",
                    icon = Icons.Outlined.Quiz,
                    selectedIcon = Icons.Filled.Quiz,
                    accentColor = BrandForestGreen,
                    testTag = "bottom_nav_mock_tests"
                )
            )

            list.add(
                BottomNavItem(
                    id = "dest_revision",
                    label = "Revision",
                    icon = Icons.Outlined.Update,
                    selectedIcon = Icons.Filled.Update,
                    badgeCount = if (overallStats.revisionDueChapters > 0) overallStats.revisionDueChapters else null,
                    badgeColor = StatusRevisionDue,
                    accentColor = StatusRevisionDue,
                    testTag = "bottom_nav_revision"
                )
            )

            list.add(
                BottomNavItem(
                    id = "dest_planner",
                    label = "Planner",
                    icon = Icons.Outlined.CalendarMonth,
                    selectedIcon = Icons.Filled.CalendarMonth,
                    accentColor = StatusInProgress,
                    testTag = "bottom_nav_planner"
                )
            )

            list.add(
                BottomNavItem(
                    id = "dest_analytics",
                    label = "Analytics",
                    icon = Icons.Outlined.Analytics,
                    selectedIcon = Icons.Filled.Analytics,
                    accentColor = StatusCompleted,
                    testTag = "bottom_nav_analytics"
                )
            )

            list.add(
                BottomNavItem(
                    id = "dest_profile",
                    label = "Profile",
                    icon = Icons.Outlined.MilitaryTech,
                    selectedIcon = Icons.Filled.MilitaryTech,
                    badgeCount = if (unlockedBadgesCount > 0) unlockedBadgesCount else null,
                    badgeColor = BrandTerracotta,
                    accentColor = BrandTerracotta,
                    testTag = "bottom_nav_profile"
                )
            )

            list.add(
                BottomNavItem(
                    id = "dest_subjects",
                    label = "All Subjects",
                    icon = Icons.Outlined.School,
                    selectedIcon = Icons.Filled.School,
                    accentColor = BrandForestGreen,
                    testTag = "bottom_nav_subjects_all"
                )
            )

            list.add(
                BottomNavItem(
                    id = "dest_weak",
                    label = "Weak",
                    icon = Icons.Outlined.ReportProblem,
                    selectedIcon = Icons.Filled.ReportProblem,
                    badgeCount = if (overallStats.weakChapters > 0) overallStats.weakChapters else null,
                    badgeColor = StatusWeak,
                    accentColor = StatusWeak,
                    testTag = "bottom_nav_weak"
                )
            )

            list.add(
                BottomNavItem(
                    id = "dest_goals",
                    label = "Goals",
                    icon = Icons.Outlined.Flag,
                    selectedIcon = Icons.Filled.Flag,
                    accentColor = BrandForestGreen,
                    testTag = "bottom_nav_goals"
                )
            )

            list.add(
                BottomNavItem(
                    id = "dest_settings",
                    label = "Settings",
                    icon = Icons.Outlined.Settings,
                    selectedIcon = Icons.Filled.Settings,
                    accentColor = onSurfaceVariantColor,
                    testTag = "bottom_nav_settings"
                )
            )

            list
        }

        // Determine active item id in navigation
        val activeNavId = when (currentNav) {
            NavDestination.DASHBOARD -> "dest_dashboard"
            NavDestination.SYLLABUS -> {
                if (selectedSubjectId != null) "subject_$selectedSubjectId"
                else if (subjects.isNotEmpty()) "subject_${subjects.first().id}"
                else "dest_syllabus"
            }
            NavDestination.MISTAKES -> "dest_mistakes"
            NavDestination.MOCK_TESTS -> "dest_mock_tests"
            NavDestination.SUBJECTS -> "dest_subjects"
            NavDestination.REVISION -> "dest_revision"
            NavDestination.PLANNER -> "dest_planner"
            NavDestination.ANALYTICS -> "dest_analytics"
            NavDestination.WEAK_TOPICS -> "dest_weak"
            NavDestination.GOALS -> "dest_goals"
            NavDestination.TIMER -> "dest_timer"
            NavDestination.CALENDAR -> "dest_calendar"
            NavDestination.PROFILE -> "dest_profile"
            NavDestination.SETTINGS -> "dest_settings"
        }

        LaunchedEffect(snackbarMsg) {
            if (snackbarMsg != null) {
                snackbarHostState.showSnackbar(snackbarMsg ?: "")
                mainViewModel.clearSnackbar()
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { mainViewModel.navigateTo(NavDestination.DASHBOARD) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BrandForestGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = BrandWarmCream,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Syllabus Tracker",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = currentNav.label.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        actions = {
                            // User Profile Avatar & Badges shortcut
                            IconButton(
                                onClick = { mainViewModel.navigateTo(NavDestination.PROFILE) },
                                modifier = Modifier
                                    .motionPress(onClick = { mainViewModel.navigateTo(NavDestination.PROFILE) })
                                    .testTag("top_bar_profile_button")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (currentNav == NavDestination.PROFILE) BrandForestGreen.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = appSettings.userAvatarEmoji, fontSize = 16.sp)
                                }
                            }

                            // Study streak badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandTerracotta.copy(alpha = 0.12f))
                                    .clickable { mainViewModel.navigateTo(NavDestination.PROFILE) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔥 ${overallStats.currentStreakDays}d",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTerracotta
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Timer action shortcut
                            IconButton(
                                onClick = { mainViewModel.navigateTo(NavDestination.TIMER) },
                                modifier = Modifier.testTag("timer_nav_button")
                            ) {
                                Icon(
                                    imageVector = if (currentNav == NavDestination.TIMER) Icons.Filled.Timer else Icons.Outlined.Timer,
                                    contentDescription = "Study Timer",
                                    tint = if (currentNav == NavDestination.TIMER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // More destinations menu (Weak, Goals, Calendar, Profile, Settings)
                            Box {
                                IconButton(onClick = { showMoreMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More features")
                                }

                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Digital Error Diary (${mistakeStats.totalMistakesCount})") },
                                        leadingIcon = { Icon(Icons.Default.BookmarkRemove, contentDescription = null, tint = StatusWeak) },
                                        onClick = {
                                            mainViewModel.navigateTo(NavDestination.MISTAKES)
                                            showMoreMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Mock Test Tracker") },
                                        leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, tint = BrandForestGreen) },
                                        onClick = {
                                            mainViewModel.navigateTo(NavDestination.MOCK_TESTS)
                                            showMoreMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Profile & Badges (${unlockedBadgesCount})") },
                                        leadingIcon = { Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = BrandTerracotta) },
                                        onClick = {
                                            mainViewModel.navigateTo(NavDestination.PROFILE)
                                            showMoreMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Subjects Overview") },
                                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            mainViewModel.navigateTo(NavDestination.SUBJECTS)
                                            showMoreMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Weak Chapters (${overallStats.weakChapters})") },
                                        leadingIcon = { Icon(Icons.Default.ReportProblem, contentDescription = null, tint = StatusWeak) },
                                        onClick = {
                                            mainViewModel.navigateTo(NavDestination.WEAK_TOPICS)
                                            showMoreMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Target Goals & Pace") },
                                        leadingIcon = { Icon(Icons.Default.TrackChanges, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            mainViewModel.navigateTo(NavDestination.GOALS)
                                            showMoreMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Monthly Calendar") },
                                        leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                                        onClick = {
                                            mainViewModel.navigateTo(NavDestination.CALENDAR)
                                            showMoreMenu = false
                                        }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Settings & Backup") },
                                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                        onClick = {
                                            mainViewModel.navigateTo(NavDestination.SETTINGS)
                                            showMoreMenu = false
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        val maxItems = if (maxWidth < 360.dp) 4 else if (maxWidth < 430.dp) 5 else 6
                        AnimatedBottomNavigation(
                            items = navItems,
                            activeItemId = activeNavId,
                            maxVisibleItems = maxItems,
                            onItemSelected = { item ->
                                when {
                                    item.id.startsWith("subject_") -> {
                                        val subId = item.id.removePrefix("subject_").toLongOrNull()
                                        if (subId != null) {
                                            mainViewModel.openSubjectDetail(subId)
                                        }
                                    }
                                    item.id == "dest_dashboard" -> mainViewModel.navigateTo(NavDestination.DASHBOARD)
                                    item.id == "dest_mistakes" -> mainViewModel.navigateTo(NavDestination.MISTAKES)
                                    item.id == "dest_mock_tests" -> mainViewModel.navigateTo(NavDestination.MOCK_TESTS)
                                    item.id == "dest_subjects" -> mainViewModel.navigateTo(NavDestination.SUBJECTS)
                                    item.id == "dest_revision" -> mainViewModel.navigateTo(NavDestination.REVISION)
                                    item.id == "dest_planner" -> mainViewModel.navigateTo(NavDestination.PLANNER)
                                    item.id == "dest_analytics" -> mainViewModel.navigateTo(NavDestination.ANALYTICS)
                                    item.id == "dest_profile" -> mainViewModel.navigateTo(NavDestination.PROFILE)
                                    item.id == "dest_weak" -> mainViewModel.navigateTo(NavDestination.WEAK_TOPICS)
                                    item.id == "dest_goals" -> mainViewModel.navigateTo(NavDestination.GOALS)
                                    item.id == "dest_timer" -> mainViewModel.navigateTo(NavDestination.TIMER)
                                    item.id == "dest_calendar" -> mainViewModel.navigateTo(NavDestination.CALENDAR)
                                    item.id == "dest_settings" -> mainViewModel.navigateTo(NavDestination.SETTINGS)
                                }
                            }
                        )
                    }
                },
                floatingActionButton = {
                    if (currentNav != NavDestination.SETTINGS && currentNav != NavDestination.TIMER && currentNav != NavDestination.SUBJECTS && currentNav != NavDestination.PROFILE && currentNav != NavDestination.MOCK_TESTS && currentNav != NavDestination.MISTAKES) {
                        FloatingActionButton(
                            onClick = { showQuickAddSheet = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .motionPress(onClick = { showQuickAddSheet = true })
                                .testTag("quick_add_fab")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Quick Add", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Navigation Rail for Tablet / Landscape screens
                    if (isWideScreen) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.width(80.dp)
                        ) {
                            val navList = listOf(
                                NavDestination.DASHBOARD,
                                NavDestination.MISTAKES,
                                NavDestination.MOCK_TESTS,
                                NavDestination.SUBJECTS,
                                NavDestination.SYLLABUS,
                                NavDestination.REVISION,
                                NavDestination.PLANNER,
                                NavDestination.ANALYTICS,
                                NavDestination.PROFILE,
                                NavDestination.WEAK_TOPICS,
                                NavDestination.GOALS,
                                NavDestination.TIMER,
                                NavDestination.CALENDAR,
                                NavDestination.SETTINGS
                            )

                            navList.forEach { destination ->
                                val isSelected = currentNav == destination
                                val icon = when (destination) {
                                    NavDestination.DASHBOARD -> Icons.Default.Dashboard
                                    NavDestination.MISTAKES -> Icons.Default.BookmarkRemove
                                    NavDestination.MOCK_TESTS -> Icons.Default.Quiz
                                    NavDestination.SUBJECTS -> Icons.Default.School
                                    NavDestination.SYLLABUS -> Icons.Default.AutoStories
                                    NavDestination.REVISION -> Icons.Default.Update
                                    NavDestination.PLANNER -> Icons.Default.CalendarMonth
                                    NavDestination.ANALYTICS -> Icons.Default.Analytics
                                    NavDestination.PROFILE -> Icons.Default.MilitaryTech
                                    NavDestination.WEAK_TOPICS -> Icons.Default.ReportProblem
                                    NavDestination.GOALS -> Icons.Default.TrackChanges
                                    NavDestination.TIMER -> Icons.Default.Timer
                                    NavDestination.CALENDAR -> Icons.Default.Event
                                    NavDestination.SETTINGS -> Icons.Default.Settings
                                }

                                NavigationRailItem(
                                    selected = isSelected,
                                    onClick = { mainViewModel.navigateTo(destination) },
                                    icon = { Icon(icon, contentDescription = destination.label) },
                                    label = { Text(destination.label, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    // Main Content View with Direction-Aware Page Transitions (Smooth Forward / Backward motion)
                    Box(modifier = Modifier.weight(1f)) {
                        DirectionalPageTransitionWrapper(
                            targetState = currentNav,
                            direction = transitionDirection,
                            label = "MainScreenTransition"
                        ) { destination ->
                            when (destination) {
                                NavDestination.DASHBOARD -> DashboardScreen(
                                    onNavigate = { mainViewModel.navigateTo(it) },
                                    onOpenSubject = { mainViewModel.openSubjectDetail(it) }
                                )
                                NavDestination.MISTAKES -> MistakeNotebookScreen(
                                    )
                                NavDestination.MOCK_TESTS -> MockTestsScreen(
                                    )
                                NavDestination.SUBJECTS -> SubjectListScreen(
                                    onNavigate = { mainViewModel.navigateTo(it) },
                                    onOpenSubject = { mainViewModel.openSubjectDetail(it) }
                                )
                                NavDestination.SYLLABUS -> SyllabusScreen(
                                    onNavigate = { mainViewModel.navigateTo(it) }
                                )
                                NavDestination.REVISION -> RevisionScreen(
                                    onNavigate = { mainViewModel.navigateTo(it) }
                                )
                                NavDestination.PLANNER -> PlannerScreen(
                                    onNavigate = { mainViewModel.navigateTo(it) }
                                )
                                NavDestination.ANALYTICS -> AnalyticsScreen(
                                    onNavigate = { mainViewModel.navigateTo(it) }
                                )
                                NavDestination.PROFILE -> ProfileScreen(
                                    onNavigate = { mainViewModel.navigateTo(it) }
                                )
                                NavDestination.WEAK_TOPICS -> WeakChaptersScreen(
                                    onNavigate = { mainViewModel.navigateTo(it) }
                                )
                                NavDestination.GOALS -> GoalsScreen(
                                    onNavigate = { mainViewModel.navigateTo(it) }
                                )
                                NavDestination.TIMER -> TimerScreen()
                                NavDestination.CALENDAR -> CalendarScreen()
                                NavDestination.SETTINGS -> SettingsScreen()
                            }
                        }
                    }
                }
            }

            // Global Chapter Detail Sheet
            if (selectedChapter != null) {
                ChapterDetailSheet(
                    chapter = selectedChapter!!,
                    onDismiss = { syllabusViewModel.selectChapter(null) },
                    onSave = { updated -> syllabusViewModel.updateItem(updated) },
                    onDelete = { item -> syllabusViewModel.deleteItem(item) },
                    onMarkRevised = { item -> syllabusViewModel.completeRevision(item) },
                    onStartTimer = { item ->
                        val sub = subjects.find { it.id == item.subjectId }
                        timerViewModel.selectTimerSubject(sub)
                        timerViewModel.selectTimerChapter(item)
                        mainViewModel.navigateTo(NavDestination.TIMER)
                        syllabusViewModel.selectChapter(null)
                    }
                )
            }

            // Global Quick Add Bottom Sheet
            if (showQuickAddSheet) {
                QuickAddBottomSheet(
                    subjects = subjects,
                    items = items,
                    preselectedSubjectId = mainViewModel.selectedSubjectId.value,
                    onDismiss = { showQuickAddSheet = false },
                    onAddChapter = { subId, pId, type, title, prio, diff, tags ->
                        syllabusViewModel.addChapter(
                            subjectId = subId,
                            parentId = pId,
                            title = title,
                            priority = prio,
                            difficulty = diff
                        )
                    },
                    onAddSubject = { name, code, icon, colorHex, desc ->
                        subjectViewModel.addSubject(name, code, icon, colorHex, desc)
                    },
                    onAddStudyPlan = { dStr, tStr, sId, sName, chapter, mins, notes ->
                        plannerViewModel.addStudyPlan(dStr, tStr, sId, sName, chapter, mins, notes)
                    },
                    onAddGoal = { title, dateStr, subId, sName, chapters, hours ->
                        goalsViewModel.addGoal(com.example.data.model.Goal(title = title, targetDateStr = dateStr, subjectId = subId, subjectName = sName, targetChaptersCount = chapters, targetStudyHours = hours))
                    }
                )
            }
        }
    }
}
