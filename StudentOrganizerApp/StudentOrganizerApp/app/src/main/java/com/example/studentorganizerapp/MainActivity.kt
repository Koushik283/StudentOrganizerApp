package com.example.studentorganizerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// ---------------------------------------------------------
// 1. NAVIGATION ROUTES
// ---------------------------------------------------------

sealed class Screen(val route: String, val title: String) {
    data object Dashboard : Screen("dashboard", "Dashboard")
    data object Assignments : Screen("assignments", "My Assignments")
    data object Profile : Screen("profile", "Student Profile")
    data object Settings : Screen("settings", "Settings")
    data object AddAssignment : Screen("add_assignment", "Add Assignment")
}

// ---------------------------------------------------------
// 2. DATA MODELS
// ---------------------------------------------------------

data class Course(
    val name: String,
    val code: String,
    val instructor: String,
    val progress: Float
)

data class Assignment(
    val title: String,
    val course: String,
    val dueDate: String,
    val priority: String,
    val description: String = "",
    val reminder: Boolean = false,
    val isCompleted: Boolean = false,
    val id: Long = 0L
)

// ---------------------------------------------------------
// 3. MAIN ACTIVITY
// ---------------------------------------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudentOrganizerApp()
        }
    }
}

// ---------------------------------------------------------
// 4. ROOT APP, SHARED STATE, THEME AND NAVIGATION
// ---------------------------------------------------------

@Composable
fun StudentOrganizerApp() {
    val navController = rememberNavController()

    var darkMode by remember { mutableStateOf(false) }

    var assignments by remember {
        mutableStateOf(
            listOf(
                Assignment(
                    title = "Complete SQL Lab",
                    course = "Database Systems",
                    dueDate = "12 Oct",
                    priority = "High",
                    id = 1L
                ),
                Assignment(
                    title = "Binary Tree Exercises",
                    course = "Data Structures",
                    dueDate = "14 Oct",
                    priority = "Medium",
                    id = 2L
                ),
                Assignment(
                    title = "CN Assignment",
                    course = "Computer Networks",
                    dueDate = "16 Oct",
                    priority = "Low",
                    id = 3L
                ),
                Assignment(
                    title = "OS Lab Report",
                    course = "Operating Systems",
                    dueDate = "18 Oct",
                    priority = "High",
                    id = 4L
                )
            )
        )
    }

    val courses = listOf(
        Course("Data Structures", "CS201", "Dr. Kumar", 0.75f),
        Course("Database Systems", "CS202", "Dr. Rao", 0.60f),
        Course("Computer Networks", "CS203", "Dr. Mehta", 0.85f),
        Course("Operating Systems", "CS204", "Dr. Sharma", 0.45f)
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val currentTitle = when (currentRoute) {
        Screen.Dashboard.route -> Screen.Dashboard.title
        Screen.Assignments.route -> Screen.Assignments.title
        Screen.Profile.route -> Screen.Profile.title
        Screen.Settings.route -> Screen.Settings.title
        Screen.AddAssignment.route -> Screen.AddAssignment.title
        else -> "Student Organizer"
    }

    MaterialTheme(
        colorScheme = if (darkMode) darkColorScheme() else lightColorScheme()
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    title = currentTitle,
                    canNavigateBack = currentRoute == Screen.AddAssignment.route,
                    onBackClick = { navController.popBackStack() }
                )
            },
            bottomBar = {
                if (currentRoute != Screen.AddAssignment.route) {
                    BottomBar(navController, currentRoute)
                }
            },
            floatingActionButton = {
                if (
                    currentRoute == Screen.Dashboard.route ||
                    currentRoute == Screen.Assignments.route
                ) {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screen.AddAssignment.route)
                        }
                    ) {
                        Text("+")
                    }
                }
            }
        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        courses = courses,
                        assignments = assignments,
                        onToggleComplete = { changed ->
                            assignments = assignments.map { item ->
                                if (item.id == changed.id) changed else item
                            }
                        }
                    )
                }

                composable(Screen.Assignments.route) {
                    AssignmentsScreen(
                        assignments = assignments,
                        onToggleComplete = { changed ->
                            assignments = assignments.map { item ->
                                if (item.id == changed.id) changed else item
                            }
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen()
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        darkMode = darkMode,
                        onDarkModeChange = { darkMode = it }
                    )
                }

                composable(Screen.AddAssignment.route) {
                    AddAssignmentScreen(
                        onCancel = { navController.popBackStack() },
                        onSave = { newAssignment ->
                            assignments = assignments + newAssignment.copy(
                                id = (assignments.maxOfOrNull { it.id } ?: 0L) + 1L
                            )
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 5. REUSABLE APP BAR
// ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    canNavigateBack: Boolean,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (canNavigateBack) {
                TextButton(onClick = onBackClick) {
                    Text("← Back")
                }
            }
        }
    )
}

// ---------------------------------------------------------
// 6. BOTTOM NAVIGATION
// ---------------------------------------------------------

@Composable
fun BottomBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val screens = listOf(
        Screen.Dashboard,
        Screen.Assignments,
        Screen.Profile,
        Screen.Settings
    )

    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Text(
                        when (screen) {
                            Screen.Dashboard -> "⌂"
                            Screen.Assignments -> "✓"
                            Screen.Profile -> "●"
                            Screen.Settings -> "⚙"
                            else -> ""
                        }
                    )
                },
                label = { Text(screen.title) }
            )
        }
    }
}

// ---------------------------------------------------------
// 7. DASHBOARD SCREEN
// ---------------------------------------------------------

@Composable
fun DashboardScreen(
    courses: List<Course>,
    assignments: List<Assignment>,
    onToggleComplete: (Assignment) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Welcome back, Student!",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Active Courses",
                value = courses.size.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pending Tasks",
                value = assignments.count { !it.isCompleted }.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Active Courses", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(courses) { course ->
                CourseInfo(
                    courseName = course.name,
                    courseCode = course.code,
                    instructor = course.instructor,
                    progress = course.progress
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Urgent Deadlines", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        val urgentAssignments = assignments
            .filter { !it.isCompleted }
            .take(2)

        if (urgentAssignments.isEmpty()) {
            Text("No pending urgent assignments.")
        } else {
            urgentAssignments.forEach { assignment ->
                AssignmentCard(
                    title = assignment.title,
                    course = assignment.course,
                    dueDate = assignment.dueDate,
                    priority = assignment.priority,
                    isCompleted = assignment.isCompleted,
                    onToggleComplete = { checked ->
                        onToggleComplete(
                            assignment.copy(isCompleted = checked)
                        )
                    }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

// ---------------------------------------------------------
// 8. REUSABLE COURSE CARD
// ---------------------------------------------------------

@Composable
fun CourseInfo(
    courseName: String,
    courseCode: String,
    instructor: String,
    progress: Float
) {
    Card(
        modifier = Modifier.width(220.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(courseName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(courseCode)
            Text("Instructor: $instructor")
            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
            Text("${(progress * 100).toInt()}% complete")
        }
    }
}

// ---------------------------------------------------------
// 9. REUSABLE ASSIGNMENT CARD
// ---------------------------------------------------------

@Composable
fun AssignmentCard(
    title: String,
    course: String,
    dueDate: String,
    priority: String,
    isCompleted: Boolean,
    onToggleComplete: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = onToggleComplete
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(course)
                Text("Due: $dueDate")
                Text("Priority: $priority")
            }
        }
    }
}

// ---------------------------------------------------------
// 10. ASSIGNMENTS LIST AND FILTERS
// ---------------------------------------------------------

@Composable
fun AssignmentsScreen(
    assignments: List<Assignment>,
    onToggleComplete: (Assignment) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredAssignments = assignments.filter { assignment ->
        when (selectedFilter) {
            "Pending" -> !assignment.isCompleted
            "Completed" -> assignment.isCompleted
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Filter Assignments", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("All", "Pending", "Completed").forEach { filter ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                    Text(filter)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Assignments (${filteredAssignments.size})")
        Spacer(Modifier.height(8.dp))

        if (filteredAssignments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No assignments in this category.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredAssignments,
                    key = { it.id }
                ) { assignment ->
                    AssignmentCard(
                        title = assignment.title,
                        course = assignment.course,
                        dueDate = assignment.dueDate,
                        priority = assignment.priority,
                        isCompleted = assignment.isCompleted,
                        onToggleComplete = { checked ->
                            onToggleComplete(
                                assignment.copy(isCompleted = checked)
                            )
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 11. REUSABLE STUDENT PROFILE COMPONENT
// ---------------------------------------------------------

@Composable
fun StudentProfile(
    studentName: String,
    studentId: String,
    major: String,
    avatarRes: Int = 0
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji avatar keeps the project independent of image resources.
            Text("👤", style = MaterialTheme.typography.displayLarge)

            Spacer(Modifier.height(12.dp))
            Text(studentName, style = MaterialTheme.typography.headlineSmall)
            Text("Student ID: $studentId")
            Text("Major: $major")
        }
    }
}

// ---------------------------------------------------------
// 12. PROFILE SCREEN
// ---------------------------------------------------------

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StudentProfile(
            studentName = "Student Name",
            studentId = "PES123",
            major = "Computer Science"
        )

        Spacer(Modifier.height(24.dp))

        Text("Academic Statistics", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileStat("GPA", "3.82")
                HorizontalDivider()
                ProfileStat("Completed Credits", "84")
                HorizontalDivider()
                ProfileStat("Current Semester", "Fall 2026")
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { /* Profile editing is outside the required scope. */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Profile")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { /* Export action placeholder. */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export Academic Summary")
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value)
    }
}

// ---------------------------------------------------------
// 13. SETTINGS SCREEN
// ---------------------------------------------------------

@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    var notifications by remember { mutableStateOf(true) }
    var startScreen by remember { mutableStateOf("Dashboard") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Preferences", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark Mode")
                    Text(
                        "Change the app colour scheme",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked = darkMode,
                    onCheckedChange = onDarkModeChange
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Push Notifications")
                Checkbox(
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Default Start Screen", style = MaterialTheme.typography.titleLarge)

        listOf("Dashboard", "Assignments").forEach { screen ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = startScreen == screen,
                    onClick = { startScreen = screen }
                )
                Text(screen)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Selected start screen: $startScreen")
    }
}

// ---------------------------------------------------------
// 14. STATE-HOISTED ASSIGNMENT FORM
// ---------------------------------------------------------

@Composable
fun AssignmentForm(
    title: String,
    onTitleChange: (String) -> Unit,
    course: String,
    onCourseChange: (String) -> Unit,
    dueDate: String,
    onDueDateChange: (String) -> Unit,
    priority: String,
    onPriorityChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    reminder: Boolean,
    onReminderChange: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val isFormValid = title.isNotBlank() &&
            course.isNotBlank() &&
            dueDate.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Assignment Title *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = course,
            onValueChange = onCourseChange,
            label = { Text("Course Name / Code *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = dueDate,
            onValueChange = onDueDateChange,
            label = { Text("Due Date * (e.g. 14 Oct)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Priority Level", style = MaterialTheme.typography.titleMedium)

        listOf("Low", "Medium", "High").forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = priority == option,
                    onClick = { onPriorityChange(option) }
                )
                Text(option)
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description / Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = reminder,
                onCheckedChange = onReminderChange
            )
            Text("Enable Reminder Notification")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onSave,
                enabled = isFormValid,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Assignment")
            }
        }

        if (!isFormValid) {
            Text(
                "Enter title, course and due date to enable Save.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ---------------------------------------------------------
// 15. ADD ASSIGNMENT SCREEN
// ---------------------------------------------------------

@Composable
fun AddAssignmentScreen(
    onCancel: () -> Unit,
    onSave: (Assignment) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var description by remember { mutableStateOf("") }
    var reminder by remember { mutableStateOf(false) }

    AssignmentForm(
        title = title,
        onTitleChange = { title = it },
        course = course,
        onCourseChange = { course = it },
        dueDate = dueDate,
        onDueDateChange = { dueDate = it },
        priority = priority,
        onPriorityChange = { priority = it },
        description = description,
        onDescriptionChange = { description = it },
        reminder = reminder,
        onReminderChange = { reminder = it },
        onCancel = onCancel,
        onSave = {
            if (
                title.isNotBlank() &&
                course.isNotBlank() &&
                dueDate.isNotBlank()
            ) {
                onSave(
                    Assignment(
                        title = title.trim(),
                        course = course.trim(),
                        dueDate = dueDate.trim(),
                        priority = priority,
                        description = description.trim(),
                        reminder = reminder
                    )
                )
            }
        }
    )
}