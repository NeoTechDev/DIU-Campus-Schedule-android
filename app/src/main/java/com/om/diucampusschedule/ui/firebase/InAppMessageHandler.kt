package com.om.diucampusschedule.ui.firebase

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.om.diucampusschedule.domain.model.InAppMessage
import com.om.diucampusschedule.domain.model.MessageButton
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.viewmodel.InAppMessageViewModel
import kotlinx.coroutines.delay

@Composable
fun InAppMessageHandler(
    navController: NavController,
    targetScreen: String,
    modifier: Modifier = Modifier,
    messageViewModel: InAppMessageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val messages by messageViewModel.messages.collectAsState()
    val dismissedMessages by messageViewModel.dismissedMessages.collectAsState()

    // Filter messages for this screen
    val screenMessages = remember(messages, dismissedMessages, targetScreen) {
        val filtered = messageViewModel.getMessagesForScreen(targetScreen)
//        Log.d("InAppMessageHandler", "Screen: $targetScreen, Messages: ${messages.size}, Filtered: ${filtered.size}")
        filtered.forEach { msg ->
//            Log.d("InAppMessageHandler", "Message: ${msg.id} - ${msg.title} (type: ${msg.type})")
        }
        filtered
    }

    // Separate dialog and banner messages
    val dialogMessages = remember(screenMessages) {
        val dialogs = screenMessages.filter { it.type == "dialog" }
//        Log.d("InAppMessageHandler", "Dialog messages: ${dialogs.size}")
        dialogs.forEach {
//            Log.d("InAppMessageHandler", "Dialog: ${it.id} - ${it.title}")
        }
        dialogs
    }

    val bannerMessages = remember(screenMessages) {
        val banners = screenMessages.filter { it.type == "banner" }
//        Log.d("InAppMessageHandler", "Banner messages: ${banners.size}")
        banners.forEach {
//            Log.d("InAppMessageHandler", "Banner: ${it.id} - ${it.title}")
        }
        banners
    }

    // State for controlling which dialog to show (only show one at a time)
    var currentDialogIndex by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }

    // State for banner visibility
    var visibleBanners by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Load messages once when the component first mounts
    LaunchedEffect(Unit) {
//        Log.d("InAppMessageHandler", "Loading messages once at startup")
        messageViewModel.loadMessages()
    }

    // OPTION 1: No background refresh (inactive)
    // Messages load once at startup only

    // OPTION 3: Manual refresh when needed (ACTIVE)
    // Smart refresh triggers for optimal performance

    // Lifecycle observer for app resume detection
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        // Initial refresh when app starts
        messageViewModel.refreshMessagesIfNeeded()
    }

    // Refresh when app resumes from background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
//                    Log.d("InAppMessageHandler", "App resumed - checking for new messages")
                    messageViewModel.refreshMessagesIfNeeded()
                }
                else -> { /* Handle other events if needed */ }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // Cleanup observer when composable is disposed
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Periodic refresh (every 30 minutes) but only when app is active
    LaunchedEffect(Unit) {
        while (true) {
            delay(30 * 60 * 1000L) // 30 minutes
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
//                Log.d("InAppMessageHandler", "Periodic refresh - app is active")
                messageViewModel.refreshMessagesIfNeeded()
            } else {
//                Log.d("InAppMessageHandler", "Periodic refresh skipped - app in background")
            }
        }
    }

    // Manual force refresh function available as:
    // messageViewModel.forceRefreshMessages()
    // Use this for pull-to-refresh or user-triggered updates

    // Log screen changes for debugging (but don't reload messages)
    LaunchedEffect(targetScreen) {
//        Log.d("InAppMessageHandler", "Screen changed to: $targetScreen (filtering existing messages)")
    }

    // Show dialog messages (one at a time) - update immediately when screen changes
    LaunchedEffect(dialogMessages, targetScreen) {
        if (dialogMessages.isNotEmpty() && !showDialog) {
            currentDialogIndex = 0
            showDialog = true
//            Log.d("InAppMessageHandler", "Showing dialog for $targetScreen: ${dialogMessages[0].title}")
        }
    }

    // Show banner messages - update immediately when screen changes
    LaunchedEffect(bannerMessages, targetScreen) {
        val bannerIds = bannerMessages.map { it.id }.toSet()
//        Log.d("InAppMessageHandler", "Setting visible banners for $targetScreen: $bannerIds")
        visibleBanners = bannerIds
    }

    // Function to handle button actions
    fun handleButtonAction(button: MessageButton, message: InAppMessage) {
        when {
            button.action == "dismiss" -> {
                messageViewModel.dismissMessage(message.id)
                if (message.type == "dialog") {
                    showDialog = false
                    // Show next dialog if available
                    if (currentDialogIndex + 1 < dialogMessages.size) {
                        currentDialogIndex++
                        showDialog = true
                    }
                } else {
                    visibleBanners = visibleBanners - message.id
                }
            }

            button.action.startsWith("navigate:") -> {
                val destination = button.action.removePrefix("navigate:")
                messageViewModel.dismissMessage(message.id)

                if (message.type == "dialog") {
                    showDialog = false
                    if (currentDialogIndex + 1 < dialogMessages.size) {
                        currentDialogIndex++
                        showDialog = true
                    }
                } else {
                    visibleBanners = visibleBanners - message.id
                }

                // Navigate to the specified screen
                when (destination) {
                    "today" -> navController.navigate(Screen.Today.route)
                    "profile" -> navController.navigate(Screen.Profile.route)
                    "routine" -> navController.navigate(Screen.Routine.route)
                    "routines" -> navController.navigate(Screen.Routine.route) // Support both
                    "tasks" -> navController.navigate(Screen.Tasks.route)
                    "notes" -> navController.navigate(Screen.Notes.route)
                    "community" -> navController.navigate(Screen.Community.route)
                    "empty_rooms" -> navController.navigate(Screen.EmptyRooms.route)
                    "faculty_info" -> navController.navigate(Screen.FacultyInfo.route)
                    "debug" -> navController.navigate(Screen.Debug.route)
                    "welcome" -> navController.navigate(Screen.Welcome.route)
                    "signin" -> navController.navigate(Screen.SignIn.route)
                    "signup" -> navController.navigate(Screen.SignUp.route)
                    else -> {
                        // Try to navigate to custom destination
                        try {
                            navController.navigate(destination)
                        } catch (e: Exception) {
                            // Ignore navigation errors for invalid destinations
                        }
                    }
                }
            }

            button.action.startsWith("url:") -> {
                val url = button.action.removePrefix("url:")
                messageViewModel.dismissMessage(message.id)

                if (message.type == "dialog") {
                    showDialog = false
                    if (currentDialogIndex + 1 < dialogMessages.size) {
                        currentDialogIndex++
                        showDialog = true
                    }
                } else {
                    visibleBanners = visibleBanners - message.id
                }

                // Open URL in external browser
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Ignore errors for invalid URLs
                }
            }

            else -> {
                // Unknown action, just dismiss
                messageViewModel.dismissMessage(message.id)
                if (message.type == "dialog") {
                    showDialog = false
                    if (currentDialogIndex + 1 < dialogMessages.size) {
                        currentDialogIndex++
                        showDialog = true
                    }
                } else {
                    visibleBanners = visibleBanners - message.id
                }
            }
        }
    }

    // Render UI - only banners that float over content
    // Banner messages - absolutely positioned floating overlay
    Column(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(1000f) // Ensure banners appear above all content
            .padding(top = 56.dp) // Padding to appear below top bar
    ) {
        bannerMessages.forEach { message ->
            val isVisible = visibleBanners.contains(message.id)
            InAppMessageBanner(
                message = message,
                isVisible = isVisible,
                onButtonClick = { button -> handleButtonAction(button, message) },
                onDismiss = {
//                    Log.d("InAppMessageHandler", "Banner ${message.id} dismissed")
                    messageViewModel.dismissMessage(message.id)
                    visibleBanners = visibleBanners - message.id
                }
            )
        }
    }

    // Dialog messages (show one at a time) - separate Box for proper centering
    if (showDialog && dialogMessages.isNotEmpty() && currentDialogIndex < dialogMessages.size) {
        val currentDialog = dialogMessages[currentDialogIndex]
        InAppMessageDialog(
            message = currentDialog,
            onButtonClick = { button -> handleButtonAction(button, currentDialog) },
            onDismiss = {
                messageViewModel.dismissMessage(currentDialog.id)
                showDialog = false
                // Show next dialog if available
                if (currentDialogIndex + 1 < dialogMessages.size) {
                    currentDialogIndex++
                    showDialog = true
                }
            }
        )
    }
}