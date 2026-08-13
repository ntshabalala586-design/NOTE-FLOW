package com.notflow.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.notflow.app.data.model.CalendarTaskEntity
import com.notflow.app.data.model.GroceryCategoryEntity
import com.notflow.app.data.model.GroceryItemEntity
import com.notflow.app.data.model.NoteEntity
import com.notflow.app.data.model.ShoppingTripEntity
import com.notflow.app.ui.components.CalendarTaskEditDialog
import com.notflow.app.ui.components.GroceryCategoryDialog
import com.notflow.app.ui.components.GroceryEditDialog
import com.notflow.app.ui.components.NoteEditDialog
import com.notflow.app.ui.components.QuickAddOption
import com.notflow.app.ui.components.QuickAddSheet
import com.notflow.app.ui.components.ShoppingTripEditDialog
import com.notflow.app.ui.screens.CalendarScreen
import com.notflow.app.ui.screens.GroceryScreen
import com.notflow.app.ui.screens.HomeScreen
import com.notflow.app.ui.screens.NotesScreen
import com.notflow.app.ui.screens.SettingsScreen
import com.notflow.app.ui.theme.NoteFlowTheme
import com.notflow.app.viewmodel.CalendarViewModel
import com.notflow.app.viewmodel.GroceryViewModel
import com.notflow.app.viewmodel.MainViewModel
import com.notflow.app.viewmodel.NoteViewModel
import com.notflow.app.viewmodel.SettingsViewModel
import com.notflow.app.viewmodel.ShoppingTripViewModel

sealed class NavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : NavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Notes : NavItem("notes", "Notes", Icons.Filled.Description, Icons.Outlined.Description)
    object Grocery : NavItem("grocery", "Grocery", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    object Calendar : NavItem("calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Settings : NavItem("settings", "Settings", Icons.Filled.Settings, Icons.Filled.Settings)
}

class MainActivity : ComponentActivity() {

    private val applicationContextApp by lazy { application as NotFlowApplication }
    private val repository by lazy { applicationContextApp.repository }

    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory(repository) }
    private val noteViewModel: NoteViewModel by viewModels { NoteViewModel.Factory(repository) }
    private val groceryViewModel: GroceryViewModel by viewModels { GroceryViewModel.Factory(repository) }
    private val shoppingTripViewModel: ShoppingTripViewModel by viewModels { ShoppingTripViewModel.Factory(repository) }
    private val calendarViewModel: CalendarViewModel by viewModels { CalendarViewModel.Factory(repository) }
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkThemeConfig by settingsViewModel.darkThemeConfig.collectAsStateWithLifecycle()

            // Request Notification permission for Android 13+
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    settingsViewModel.setNotificationsEnabled(isGranted)
                }
            )

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            NoteFlowTheme(darkThemeConfig = darkThemeConfig) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotFlowApp(
                        mainViewModel = mainViewModel,
                        noteViewModel = noteViewModel,
                        groceryViewModel = groceryViewModel,
                        shoppingTripViewModel = shoppingTripViewModel,
                        calendarViewModel = calendarViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotFlowApp(
    mainViewModel: MainViewModel,
    noteViewModel: NoteViewModel,
    groceryViewModel: GroceryViewModel,
    shoppingTripViewModel: ShoppingTripViewModel,
    calendarViewModel: CalendarViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavItem.Home.route

    val isQuickAddOpen by mainViewModel.isQuickAddOpen.collectAsStateWithLifecycle()

    // Dialog state controllers
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var showNoteDialog by remember { mutableStateOf(false) }

    var groceryToEdit by remember { mutableStateOf<GroceryItemEntity?>(null) }
    var showGroceryDialog by remember { mutableStateOf(false) }

    var showGroceryCategoryDialog by remember { mutableStateOf(false) }
    var showNoteCategoryDialog by remember { mutableStateOf(false) }

    var tripToEdit by remember { mutableStateOf<ShoppingTripEntity?>(null) }
    var showTripDialog by remember { mutableStateOf(false) }

    var calendarTaskToEdit by remember { mutableStateOf<CalendarTaskEntity?>(null) }
    var showCalendarTaskDialog by remember { mutableStateOf(false) }

    // State collections
    val recentNotes by mainViewModel.recentNotes.collectAsStateWithLifecycle()
    val remainingGroceryCount by mainViewModel.remainingGroceryCount.collectAsStateWithLifecycle()
    val upcomingTrips by mainViewModel.upcomingTrips.collectAsStateWithLifecycle()
    val upcomingReminders by mainViewModel.upcomingReminders.collectAsStateWithLifecycle()

    val notesList by noteViewModel.notes.collectAsStateWithLifecycle()
    val noteCategoriesList by noteViewModel.categories.collectAsStateWithLifecycle()
    val selectedNoteCategory by noteViewModel.selectedCategory.collectAsStateWithLifecycle()
    val noteSearchQuery by noteViewModel.searchQuery.collectAsStateWithLifecycle()

    val groceryItemsList by groceryViewModel.groceryItems.collectAsStateWithLifecycle()
    val groceryCategoriesList by groceryViewModel.categories.collectAsStateWithLifecycle()
    val selectedGroceryCategory by groceryViewModel.selectedCategory.collectAsStateWithLifecycle()
    val grocerySearchQuery by groceryViewModel.searchQuery.collectAsStateWithLifecycle()

    val currentCal by calendarViewModel.currentCalendar.collectAsStateWithLifecycle()
    val selectedDateMillis by calendarViewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedDateTasks by calendarViewModel.selectedDateTasks.collectAsStateWithLifecycle()
    val selectedDateShoppingTrips by calendarViewModel.selectedDateShoppingTrips.collectAsStateWithLifecycle()
    val allCalendarTasks by calendarViewModel.allCalendarTasks.collectAsStateWithLifecycle()
    val allShoppingTrips by shoppingTripViewModel.shoppingTrips.collectAsStateWithLifecycle()

    val darkThemeConfig by settingsViewModel.darkThemeConfig.collectAsStateWithLifecycle()
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val defaultGroceryCategory by settingsViewModel.defaultGroceryCategory.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavItem.Home,
        NavItem.Notes,
        NavItem.Grocery,
        NavItem.Calendar
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            NavItem.Home.route -> "Note Flow"
                            NavItem.Notes.route -> "Notes"
                            NavItem.Grocery.route -> "Grocery List"
                            NavItem.Calendar.route -> "Calendar"
                            NavItem.Settings.route -> "Settings"
                            else -> "Note Flow"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (currentRoute != NavItem.Settings.route) {
                        IconButton(
                            onClick = { navController.navigate(NavItem.Settings.route) },
                            modifier = Modifier.testTag("top_bar_settings_button")
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        modifier = Modifier.testTag("nav_${item.route}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == NavItem.Home.route) {
                FloatingActionButton(
                    onClick = { mainViewModel.openQuickAdd() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("home_fab_quick_add")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Quick Add")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavItem.Home.route) {
                HomeScreen(
                    recentNotes = recentNotes,
                    remainingGroceryCount = remainingGroceryCount,
                    upcomingTrips = upcomingTrips,
                    upcomingReminders = upcomingReminders,
                    onQuickAddClick = { mainViewModel.openQuickAdd() },
                    onNavigateToNotes = { navController.navigate(NavItem.Notes.route) },
                    onNavigateToGrocery = { navController.navigate(NavItem.Grocery.route) },
                    onNavigateToCalendar = { navController.navigate(NavItem.Calendar.route) },
                    onNoteClick = { note ->
                        noteToEdit = note
                        showNoteDialog = true
                    },
                    onTripClick = { trip ->
                        tripToEdit = trip
                        showTripDialog = true
                    }
                )
            }

            composable(NavItem.Notes.route) {
                NotesScreen(
                    notes = notesList,
                    categories = noteCategoriesList,
                    selectedCategory = selectedNoteCategory,
                    searchQuery = noteSearchQuery,
                    onCategorySelect = { noteViewModel.setCategory(it) },
                    onSearchQueryChange = { noteViewModel.setSearchQuery(it) },
                    onAddNoteClick = {
                        noteToEdit = null
                        showNoteDialog = true
                    },
                    onNoteClick = { note ->
                        noteToEdit = note
                        showNoteDialog = true
                    },
                    onNoteDelete = { noteViewModel.deleteNote(it) },
                    onAddCategoryClick = { showNoteCategoryDialog = true }
                )
            }

            composable(NavItem.Grocery.route) {
                GroceryScreen(
                    groceryItems = groceryItemsList,
                    categories = groceryCategoriesList,
                    selectedCategory = selectedGroceryCategory,
                    searchQuery = grocerySearchQuery,
                    remainingCount = remainingGroceryCount,
                    onCategorySelect = { groceryViewModel.setCategory(it) },
                    onSearchQueryChange = { groceryViewModel.setSearchQuery(it) },
                    onToggleCheck = { groceryViewModel.toggleItemCheck(it) },
                    onAddItemClick = {
                        groceryToEdit = null
                        showGroceryDialog = true
                    },
                    onEditItemClick = { item ->
                        groceryToEdit = item
                        showGroceryDialog = true
                    },
                    onDeleteItem = { groceryViewModel.deleteGroceryItem(it) },
                    onClearCompleted = { groceryViewModel.clearCompletedItems() },
                    onAddCategoryClick = { showGroceryCategoryDialog = true },
                    onRenameCategoryClick = { cat ->
                        // rename handled
                    },
                    onDeleteCategoryClick = { cat -> groceryViewModel.deleteCategory(cat) }
                )
            }

            composable(NavItem.Calendar.route) {
                CalendarScreen(
                    currentCalendar = currentCal,
                    selectedDateMillis = selectedDateMillis,
                    tasksForSelectedDate = selectedDateTasks,
                    shoppingTripsForSelectedDate = selectedDateShoppingTrips,
                    allTasks = allCalendarTasks,
                    allShoppingTrips = allShoppingTrips,
                    onSelectDate = { calendarViewModel.setSelectedDate(it) },
                    onPreviousMonth = { calendarViewModel.previousMonth() },
                    onNextMonth = { calendarViewModel.nextMonth() },
                    onAddTaskClick = {
                        calendarTaskToEdit = null
                        showCalendarTaskDialog = true
                    },
                    onTaskClick = { task ->
                        calendarTaskToEdit = task
                        showCalendarTaskDialog = true
                    },
                    onTaskDelete = { calendarViewModel.deleteCalendarTask(it) },
                    onTripClick = { trip ->
                        tripToEdit = trip
                        showTripDialog = true
                    }
                )
            }

            composable(NavItem.Settings.route) {
                SettingsScreen(
                    darkThemeConfig = darkThemeConfig,
                    notificationsEnabled = notificationsEnabled,
                    defaultGroceryCategory = defaultGroceryCategory,
                    availableGroceryCategories = groceryCategoriesList.map { it.name },
                    onDarkThemeConfigChange = { settingsViewModel.setDarkThemeConfig(it) },
                    onNotificationsEnabledChange = { settingsViewModel.setNotificationsEnabled(it) },
                    onDefaultGroceryCategoryChange = { settingsViewModel.setDefaultGroceryCategory(it) }
                )
            }
        }
    }

    // Quick Add Modal Bottom Sheet
    if (isQuickAddOpen) {
        QuickAddSheet(
            onDismiss = { mainViewModel.closeQuickAdd() },
            onSelectOption = { option ->
                when (option) {
                    QuickAddOption.NOTE -> {
                        noteToEdit = null
                        showNoteDialog = true
                    }
                    QuickAddOption.GROCERY -> {
                        groceryToEdit = null
                        showGroceryDialog = true
                    }
                    QuickAddOption.SHOPPING_TRIP -> {
                        tripToEdit = null
                        showTripDialog = true
                    }
                    QuickAddOption.CALENDAR_TASK -> {
                        calendarTaskToEdit = null
                        showCalendarTaskDialog = true
                    }
                }
            }
        )
    }

    // Dialogs
    if (showNoteDialog) {
        NoteEditDialog(
            note = noteToEdit,
            availableCategories = noteCategoriesList.map { it.name },
            onDismiss = { showNoteDialog = false },
            onSave = { id, title, content, category, isPinned ->
                noteViewModel.saveNote(id, title, content, category, isPinned)
            },
            onAddCategory = { noteViewModel.addCategory(it) }
        )
    }

    if (showGroceryDialog) {
        GroceryEditDialog(
            item = groceryToEdit,
            availableCategories = groceryCategoriesList.map { it.name },
            onDismiss = { showGroceryDialog = false },
            onSave = { id, name, category, isChecked ->
                groceryViewModel.saveGroceryItem(id, name, category, isChecked)
            },
            onAddCategory = { groceryViewModel.addCategory(it) }
        )
    }

    if (showGroceryCategoryDialog) {
        GroceryCategoryDialog(
            titleText = "Add Grocery Category",
            onDismiss = { showGroceryCategoryDialog = false },
            onConfirm = { groceryViewModel.addCategory(it) }
        )
    }

    if (showNoteCategoryDialog) {
        GroceryCategoryDialog(
            titleText = "Add Note Category",
            onDismiss = { showNoteCategoryDialog = false },
            onConfirm = { noteViewModel.addCategory(it) }
        )
    }

    if (showTripDialog) {
        ShoppingTripEditDialog(
            trip = tripToEdit,
            onDismiss = { showTripDialog = false },
            onSave = { id, title, date, timeHour, timeMinute, description, reminderMinutesBefore, isReminderEnabled ->
                shoppingTripViewModel.saveShoppingTrip(
                    id = id,
                    title = title,
                    date = date,
                    timeHour = timeHour,
                    timeMinute = timeMinute,
                    description = description,
                    reminderMinutesBefore = reminderMinutesBefore,
                    isReminderEnabled = isReminderEnabled
                )
            }
        )
    }

    if (showCalendarTaskDialog) {
        CalendarTaskEditDialog(
            task = calendarTaskToEdit,
            initialDateMillis = selectedDateMillis,
            onDismiss = { showCalendarTaskDialog = false },
            onSave = { id, title, description, date, timeHour, timeMinute, reminderMinutesBefore, isReminderEnabled ->
                calendarViewModel.saveCalendarTask(
                    id = id,
                    title = title,
                    description = description,
                    date = date,
                    timeHour = timeHour,
                    timeMinute = timeMinute,
                    reminderMinutesBefore = reminderMinutesBefore,
                    isReminderEnabled = isReminderEnabled
                )
            }
        )
    }
}
