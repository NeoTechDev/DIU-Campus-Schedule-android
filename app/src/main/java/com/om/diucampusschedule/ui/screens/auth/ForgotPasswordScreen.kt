package com.om.diucampusschedule.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.R
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var isEmailFocused by remember { mutableStateOf(false) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }
    
    // Animation states
    val formScale by animateFloatAsState(
        targetValue = if (authState.isLoading) 0.98f else 1f,
        animationSpec = tween(300),
        label = "formScale"
    )
    
    val titleAlpha by animateFloatAsState(
        targetValue = if (authState.isLoading) 0.7f else 1f,
        animationSpec = tween(300),
        label = "titleAlpha"
    )

    // Clear flags when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearFlags()
        }
    }

    // Navigate back on success
    LaunchedEffect(authState.isPasswordResetSent) {
        if (authState.isPasswordResetSent) {
            // Optional: Navigate back after a delay to show success message
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
    }

    DIUCampusScheduleTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image with Gradient Overlay
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.welcome_screen_bg),
                    contentDescription = "DIU Campus Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Lock Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .graphicsLayer(alpha = titleAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = "Reset Password",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Title with animation
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = tween(600, delayMillis = 200)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .graphicsLayer(alpha = titleAlpha)
                        )

                        Text(
                            text = "Enter your email address and we'll send you a link to reset your password",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(bottom = 32.dp)
                                .graphicsLayer(alpha = titleAlpha)
                        )
                    }
                }

                // Reset Password Form with enhanced animation
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { 60 },
                        animationSpec = tween(600, delayMillis = 400)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(formScale),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Email Field with enhanced styling
                            OutlinedTextField(
                                value = email,
                                onValueChange = { 
                                    email = it
                                    viewModel.clearError()
                                    viewModel.clearSuccessMessage()
                                },
                                label = { 
                                    Text(
                                        "Email Address",
                                        color = if (isEmailFocused) MaterialTheme.colorScheme.primary 
                                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    ) 
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isEmailFocused) Icons.Filled.Email else Icons.Outlined.Email,
                                        contentDescription = "Email",
                                        tint = if (isEmailFocused) MaterialTheme.colorScheme.primary 
                                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { 
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        if (email.isNotBlank()) {
                                            viewModel.resetPassword(email)
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(emailFocusRequester)
                                    .onFocusChanged { focusState ->
                                        isEmailFocused = focusState.isFocused
                                    },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )

                            // Success Message with animation
                            AnimatedVisibility(
                                visible = authState.successMessage != null,
                                enter = slideInVertically(
                                    initialOffsetY = { -20 },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300)),
                                exit = slideOutVertically(
                                    targetOffsetY = { -20 },
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = authState.successMessage ?: "",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Error Message with animation
                            AnimatedVisibility(
                                visible = authState.error != null,
                                enter = slideInVertically(
                                    initialOffsetY = { -20 },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300)),
                                exit = slideOutVertically(
                                    targetOffsetY = { -20 },
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = "Error",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = authState.error ?: "",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Enhanced Reset Password Button
                            val buttonScale by animateFloatAsState(
                                targetValue = if (authState.isLoading) 0.98f else 1f,
                                animationSpec = tween(150),
                                label = "buttonScale"
                            )
                            
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    viewModel.resetPassword(email)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .scale(buttonScale),
                                enabled = !authState.isLoading && email.isNotBlank(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 2.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                if (authState.isLoading) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sending...",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Send Reset Email",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Send Reset Email",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Back to Sign In Link
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = tween(600, delayMillis = 800)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 800))
                ) {
                    Card(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                navController.popBackStack()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to Sign In",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Back to Sign In",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // Focus handling
            LaunchedEffect(Unit) {
                emailFocusRequester.requestFocus()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    val navController = rememberNavController()
    ForgotPasswordScreen(navController = navController)
}
