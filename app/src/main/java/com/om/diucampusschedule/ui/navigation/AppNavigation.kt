package com.om.diucampusschedule.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.om.diucampusschedule.domain.model.AppState
import com.om.diucampusschedule.ui.components.MainScaffold
import com.om.diucampusschedule.ui.components.NavigationDrawer
import com.om.diucampusschedule.ui.components.WelcomeDialog
import com.om.diucampusschedule.ui.screens.auth.EmailVerificationScreen
import com.om.diucampusschedule.ui.screens.auth.ForgotPasswordScreen
import com.om.diucampusschedule.ui.screens.auth.RegistrationFormScreen
import com.om.diucampusschedule.ui.screens.auth.SignInScreen
import com.om.diucampusschedule.ui.screens.auth.SignUpScreen
import com.om.diucampusschedule.ui.screens.community.CommunityScreen
import com.om.diucampusschedule.ui.screens.debug.DebugScreen
import com.om.diucampusschedule.ui.screens.emptyrooms.EmptyRoomsScreen
import com.om.diucampusschedule.ui.screens.facultyinfo.FacultyInfoScreen
import com.om.diucampusschedule.ui.screens.notes.NoteEditorScreen
import com.om.diucampusschedule.ui.screens.notes.NotesScreen
import com.om.diucampusschedule.ui.screens.profile.ProfileScreen
import com.om.diucampusschedule.ui.screens.routine.RoutineScreen
import com.om.diucampusschedule.ui.screens.tasks.TaskScreen
import com.om.diucampusschedule.ui.screens.today.TodayScreen
import com.om.diucampusschedule.ui.screens.webview.WebViewScreen
import com.om.diucampusschedule.ui.screens.welcome.WelcomeScreen
import com.om.diucampusschedule.ui.viewmodel.AppInitializationViewModel
import com.om.diucampusschedule.ui.viewmodel.RoutineViewModel
import com.om.diucampusschedule.ui.viewmodel.WelcomeViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder

// Animation constants
private const val ANIMATION_DURATION = 400
private const val FADE_DURATION = 200

/**
 * Animation specifications for different navigation scenarios
 */
private object NavigationAnimations {
    
    // Forward navigation animations (going deeper into app)
    val slideInFromRight = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = FADE_DURATION,
            easing = LinearOutSlowInEasing
        )
    )
    
    val slideOutToLeft = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = FADE_DURATION,
            easing = LinearOutSlowInEasing
        )
    )
    
    // Backward navigation animations (going back/up in hierarchy)
    val slideInFromLeft = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = FADE_DURATION,
            easing = LinearOutSlowInEasing
        )
    )
    
    val slideOutToRight = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = FADE_DURATION,
            easing = LinearOutSlowInEasing
        )
    )
    
    // Fade animations for home screen and special cases
    val fadeIn = fadeIn(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
    
    val fadeOut = fadeOut(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = LinearOutSlowInEasing
        )
    )
    
    // Bottom navigation animations (horizontal movement between tabs)
    val slideInFromRightTab = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 150,
            easing = LinearOutSlowInEasing
        )
    )
    
    val slideOutToLeftTab = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 150,
            easing = LinearOutSlowInEasing
        )
    )
    
    val slideInFromLeftTab = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 150,
            easing = LinearOutSlowInEasing
        )
    )
    
    val slideOutToRightTab = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 150,
            easing = LinearOutSlowInEasing
        )
    )
}

/**
 * Main navigation component that handles app-wide navigation based on app state
 * 
 * @param navController Navigation controller for handling navigation
 * @param appState Current application state
 * @param startDestination Optional start destination for deep linking
 * @param appInitializationViewModel ViewModel for app initialization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    appState: AppState,
    startDestination: String? = null,
    appInitializationViewModel: AppInitializationViewModel = hiltViewModel()
) {
    // Add animated content for smooth state transitions
    androidx.compose.animation.AnimatedContent(
        targetState = appState,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = FastOutSlowInEasing
                )
            ) togetherWith fadeOut(
                animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = LinearOutSlowInEasing
                )
            )
        },
        label = "app_state_transition"
    ) { targetState ->
        when (targetState) {
            is AppState.Initializing -> {
                // Show loading screen while app initializes
                LoadingScreen()
            }
            
            is AppState.Unauthenticated -> {
                // Show authentication flow
                AuthNavigationHost(
                    navController = navController,
                    startDestination = startDestination ?: Screen.Welcome.route
                )
            }
            
            is AppState.AuthenticatedEmailUnverified -> {
                // Show email verification flow
                val encodedEmail = URLEncoder.encode(targetState.user.email, StandardCharsets.UTF_8.toString())
                AuthNavigationHost(
                    navController = navController,
                    startDestination = startDestination ?: "${Screen.EmailVerification.route}/${encodedEmail}"
                )
            }
            
            is AppState.AuthenticatedIncomplete -> {
                // Show profile completion flow
                AuthNavigationHost(
                    navController = navController,
                    startDestination = startDestination ?: Screen.RegsitrationForm.route
                )
            }
            
            is AppState.AuthenticatedComplete -> {
                // Show main app flow
                MainAppNavigationHost(
                    navController = navController,
                    startDestination = startDestination ?: Screen.Today.route
                )
            }
            
            is AppState.Error -> {
                // Show error screen with retry option
                ErrorScreen(
                    message = targetState.message,
                    onRetry = {
                        // Retry app initialization
                        appInitializationViewModel.refreshAppState()
                    }
                )
            }
        }
    }
}

/**
 * Loading screen shown during app initialization
 */
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Error screen with retry functionality
 */
@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

/**
 * Navigation host for authentication flow with smooth animations
 */
@Composable
private fun AuthNavigationHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication Screens with fade animations
        composable(
            route = Screen.Welcome.route,
            enterTransition = { NavigationAnimations.fadeIn },
            exitTransition = { NavigationAnimations.fadeOut }
        ) {
            WelcomeScreen(navController = navController)
        }

        composable(
            route = Screen.SignIn.route,
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft },
            popEnterTransition = { NavigationAnimations.slideInFromLeft },
            popExitTransition = { NavigationAnimations.slideOutToRight }
        ) {
            SignInScreen(navController = navController)
        }

        composable(
            route = Screen.SignUp.route,
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft },
            popEnterTransition = { NavigationAnimations.slideInFromLeft },
            popExitTransition = { NavigationAnimations.slideOutToRight }
        ) {
            SignUpScreen(navController = navController)
        }

        composable(
            route = Screen.ForgotPassword.route,
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft },
            popEnterTransition = { NavigationAnimations.slideInFromLeft },
            popExitTransition = { NavigationAnimations.slideOutToRight }
        ) {
            ForgotPasswordScreen(navController = navController)
        }

        composable(
            route = "${Screen.EmailVerification.route}/{email}",
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft },
            popEnterTransition = { NavigationAnimations.slideInFromLeft },
            popExitTransition = { NavigationAnimations.slideOutToRight }
        ) { backStackEntry ->
            val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())
            EmailVerificationScreen(
                navController = navController,
                userEmail = email
            )
        }

        composable(
            route = Screen.RegsitrationForm.route,
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft },
            popEnterTransition = { NavigationAnimations.slideInFromLeft },
            popExitTransition = { NavigationAnimations.slideOutToRight }
        ) {
            RegistrationFormScreen(navController = navController)
        }
    }
}

/**
 * Navigation host for main app flow with scaffold and smooth animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppNavigationHost(
    navController: NavHostController,
    startDestination: String
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Welcome dialog state management
    val welcomeViewModel: WelcomeViewModel = hiltViewModel()
    val welcomeUiState by welcomeViewModel.uiState.collectAsStateWithLifecycle()
    
    // Get routine data for effectiveFrom
    val routineViewModel: RoutineViewModel = hiltViewModel()
    val routineUiState by routineViewModel.uiState.collectAsStateWithLifecycle()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        drawerContent = {
            NavigationDrawer(
                onNavigate = { route ->
                    navController.navigate(route)
                    scope.launch { drawerState.close() }
                },
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        MainScaffold(navController = navController) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(
                    route = Screen.Notices.route,
                    enterTransition = { NavigationAnimations.slideInFromRight },
                    exitTransition = { NavigationAnimations.slideOutToLeft },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) {
                    com.om.diucampusschedule.ui.screens.notices.NoticesScreen(
                        onBack = { navController.navigateUp() }
                    )
                }
                // Main App Screens with smart directional transitions
                composable(
                    route = Screen.Today.route,
                    enterTransition = { NavigationAnimations.fadeIn },
                    exitTransition = { 
                        // Check where we're going and animate accordingly
                        when (targetState.destination.route) {
                            Screen.Routine.route, Screen.EmptyRooms.route, 
                            Screen.Tasks.route, Screen.Notes.route -> NavigationAnimations.slideOutToLeftTab
                            else -> NavigationAnimations.fadeOut
                        }
                    },
                    popEnterTransition = { NavigationAnimations.fadeIn },
                    popExitTransition = { NavigationAnimations.fadeOut }
                ) {
                    TodayScreen(
                        navController = navController,
                        onOpenDrawer = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
                
                composable(
                    route = Screen.Routine.route,
                    enterTransition = { 
                        when (initialState.destination.route) {
                            Screen.Today.route -> NavigationAnimations.slideInFromRightTab
                            Screen.EmptyRooms.route, Screen.Tasks.route, Screen.Notes.route -> NavigationAnimations.slideInFromLeftTab
                            else -> NavigationAnimations.slideInFromRight
                        }
                    },
                    exitTransition = { 
                        when (targetState.destination.route) {
                            Screen.Today.route -> NavigationAnimations.slideOutToLeftTab
                            Screen.EmptyRooms.route, Screen.Tasks.route, Screen.Notes.route -> NavigationAnimations.slideOutToLeftTab
                            else -> NavigationAnimations.slideOutToLeft
                        }
                    },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) {
                    RoutineScreen()
                }
                
                composable(
                    route = Screen.EmptyRooms.route,
                    enterTransition = { 
                        when (initialState.destination.route) {
                            Screen.Today.route, Screen.Routine.route -> NavigationAnimations.slideInFromRightTab
                            Screen.Tasks.route, Screen.Notes.route -> NavigationAnimations.slideInFromLeftTab
                            else -> NavigationAnimations.slideInFromRight
                        }
                    },
                    exitTransition = { 
                        when (targetState.destination.route) {
                            Screen.Today.route, Screen.Routine.route -> NavigationAnimations.slideOutToLeftTab
                            Screen.Tasks.route, Screen.Notes.route -> NavigationAnimations.slideOutToLeftTab
                            else -> NavigationAnimations.slideOutToLeft
                        }
                    },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) {
                    EmptyRoomsScreen()
                }
                
                composable(
                    route = Screen.Tasks.route,
                    enterTransition = { 
                        when (initialState.destination.route) {
                            Screen.Today.route, Screen.Routine.route, Screen.EmptyRooms.route -> NavigationAnimations.slideInFromRightTab
                            Screen.Notes.route -> NavigationAnimations.slideInFromLeftTab
                            else -> NavigationAnimations.slideInFromRight
                        }
                    },
                    exitTransition = { 
                        when (targetState.destination.route) {
                            Screen.Today.route, Screen.Routine.route, Screen.EmptyRooms.route -> NavigationAnimations.slideOutToLeftTab
                            Screen.Notes.route -> NavigationAnimations.slideOutToLeftTab
                            else -> NavigationAnimations.slideOutToLeft
                        }
                    },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) {
                    TaskScreen(navController = navController)
                }
                
                composable(
                    route = Screen.Notes.route,
                    enterTransition = { 
                        when (initialState.destination.route) {
                            Screen.Today.route, Screen.Routine.route, 
                            Screen.EmptyRooms.route, Screen.Tasks.route -> NavigationAnimations.slideInFromRightTab
                            else -> NavigationAnimations.slideInFromRight
                        }
                    },
                    exitTransition = { 
                        when (targetState.destination.route) {
                            Screen.Today.route, Screen.Routine.route, 
                            Screen.EmptyRooms.route, Screen.Tasks.route -> NavigationAnimations.slideOutToLeftTab
                            else -> NavigationAnimations.slideOutToLeft
                        }
                    },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) {
                    NotesScreen(navController = navController)
                }

                // Note Editor Screen - supports both create new note and edit existing note
                composable(
                    route = "note_editor?noteId={noteId}",
                    enterTransition = { NavigationAnimations.slideInFromRight },
                    exitTransition = { NavigationAnimations.slideOutToLeft },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
                    NoteEditorScreen(
                        navController = navController,
                        noteId = noteId
                    )
                }
                
                composable(
                    route = "note_editor",
                    enterTransition = { NavigationAnimations.slideInFromRight },
                    exitTransition = { NavigationAnimations.slideOutToLeft },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) {
                    NoteEditorScreen(
                        navController = navController,
                        noteId = null
                    )
                }

                // Profile Screen
                composable(
                    route = Screen.Profile.route,
                    enterTransition = { NavigationAnimations.slideInFromRight },
                    exitTransition = { NavigationAnimations.slideOutToLeft },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) {
                    ProfileScreen(navController = navController)
                }
                
                // Placeholder for unimplemented screens
                composable(
                    route = Screen.ExamRoutine.route,
                    enterTransition = { NavigationAnimations.slideInFromRight },
                    exitTransition = { NavigationAnimations.slideOutToLeft },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) {
                    PlaceholderScreen(title = "Exam Routine", description = "Exam schedules will be displayed here")
                }
                
                composable(
                    route = "${Screen.FacultyInfo.route}?searchQuery={searchQuery}",
                    enterTransition = { NavigationAnimations.slideInFromRight },
                    exitTransition = { NavigationAnimations.slideOutToLeft },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) { backStackEntry ->
                    val searchQuery = backStackEntry.arguments?.getString("searchQuery")
                    FacultyInfoScreen(
                        onBack = navController,
                        initialSearchQuery = searchQuery
                    )
                }

                composable(
                    route = Screen.Community.route,
                    enterTransition = { NavigationAnimations.slideInFromRight },
                    exitTransition = { NavigationAnimations.slideOutToLeft },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ){
                    CommunityScreen(navController = navController)
                }

                composable(
                    route = Screen.Debug.route,
                    enterTransition = { NavigationAnimations.slideInFromRight },
                    exitTransition = { NavigationAnimations.slideOutToLeft },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) {
                    DebugScreen(navController = navController)
                }

                composable(
                    route = "${Screen.WebView.route}?url={url}&title={title}",
                    enterTransition = { NavigationAnimations.slideInFromRight },
                    exitTransition = { NavigationAnimations.slideOutToLeft },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft },
                    popExitTransition = { NavigationAnimations.slideOutToRight }
                ) { backStackEntry ->
                    val url = backStackEntry.arguments?.getString("url")?.let { 
                        URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) 
                    } ?: ""
                    val title = backStackEntry.arguments?.getString("title")?.let { 
                        URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) 
                    } ?: ""
                    WebViewScreen(
                        url = url,
                        title = title,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }
        }
        
        // Show welcome dialog for first-time users
        if (welcomeUiState.showWelcomeDialog) {
            WelcomeDialog(
                effectiveFrom = routineUiState.effectiveFrom ?: "", // Use dynamic data with fallback
                onDismiss = {
                    welcomeViewModel.dismissWelcomeDialog()
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
