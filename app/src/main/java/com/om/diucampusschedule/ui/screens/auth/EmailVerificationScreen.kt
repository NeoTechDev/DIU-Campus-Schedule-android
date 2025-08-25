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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    navController: NavController,
    userEmail: String = "",
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    var timeLeft by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    
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

    // Countdown timer for resend button
    LaunchedEffect(authState.isEmailVerificationSent) {
        if (authState.isEmailVerificationSent) {
            timeLeft = 60
            canResend = false
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            canResend = true
        }
    }

    // Check verification status periodically
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Check every 3 seconds
            viewModel.checkEmailVerification()
        }
    }

    // Navigate to profile completion if verified
    LaunchedEffect(authState.user) {
        if (authState.user != null && authState.successMessage?.contains("verified") == true) {
            delay(1500) // Show success message briefly
            navController.navigate(Screen.RegsitrationForm.route) {
                popUpTo(Screen.EmailVerification.route) { inclusive = true }
            }
        }
    }

    // Clear flags when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearFlags()
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
                // Email Icon with animation
                val infiniteTransition = rememberInfiniteTransition(label = "emailPulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .graphicsLayer(alpha = titleAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "Email Verification",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
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
                            text = "Verify Your Email",
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
                            text = "We've sent a verification link to",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.graphicsLayer(alpha = titleAlpha)
                        )
                        
                        Text(
                            text = userEmail.ifEmpty { "your email address" },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .graphicsLayer(alpha = titleAlpha)
                        )
                        
                        Text(
                            text = "Click the link in your email to verify your account",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(bottom = 32.dp)
                                .graphicsLayer(alpha = titleAlpha)
                        )
                    }
                }

                // Verification Status Card
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
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = authState.successMessage ?: "",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
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
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = "Error",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = authState.error ?: "",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Check Status Button
                            val checkButtonScale by animateFloatAsState(
                                targetValue = if (authState.isLoading) 0.98f else 1f,
                                animationSpec = tween(150),
                                label = "checkButtonScale"
                            )
                            
                            Button(
                                onClick = {
                                    viewModel.checkEmailVerification()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .scale(checkButtonScale),
                                enabled = !authState.isLoading,
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
                                            text = "Checking...",
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
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Check Verification",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "I've Verified My Email",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }

                            // Resend Email Button
                            OutlinedButton(
                                onClick = {
                                    viewModel.sendEmailVerification()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = canResend && !authState.isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (canResend) MaterialTheme.colorScheme.primary 
                                                  else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            ) {
                                if (canResend) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Resend Email",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Resend Verification Email",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Resend in ${timeLeft}s",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Sign Out Link
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
                                viewModel.signOut()
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(0) { inclusive = true }
                                }
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
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmailVerificationScreenPreview() {
    val navController = rememberNavController()
    EmailVerificationScreen(
        navController = navController,
        userEmail = "student@diu.edu.bd"
    )
}
