package com.om.diucampusschedule.ui.screens.welcome

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.om.diucampusschedule.R
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme

@SuppressLint("SuspiciousIndentation")
@Composable
fun WelcomeScreen(
    navController: NavController
) {
    DIUCampusScheduleTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image with Gradient Overlay
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                // Uncomment when background image is added
                Image(
                    painter = painterResource(id = R.drawable.welcome_screen_bg),
                    contentDescription = "DIU Campus Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.45f
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Top Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Lottie Animation
                    val composition by rememberLottieComposition(
                        spec = LottieCompositionSpec.RawRes(
                            resId = R.raw.wel_screen_animation
                        )
                    )
                    val progress by animateLottieCompositionAsState(
                        composition,
                        isPlaying = true,
                        iterations = LottieConstants.IterateForever
                    )

                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .size(280.dp)
                            .padding(bottom = 24.dp)
                    )

                    // Title with proper typography
                    Text(
                        text = "Welcome to",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 28.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    
                    Text(
                        text = "DIU Campus Schedule",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Subtitle with improved readability
                    Text(
                        text = "No more struggling with routine images! Take control of your university schedule with ease—organize your classes, tasks, reminders, and notes all in one app for a hassle-free experience!",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Bottom Section with enhanced button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Button(
                        onClick = { navController.navigate(Screen.SignIn.route) },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.right_arrow),
                                contentDescription = "Next Page",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
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
fun WelcomeScreenPreview() {
    val navController = rememberNavController()
    WelcomeScreen(navController = navController)
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WelcomeScreenDarkPreview() {
    val navController = rememberNavController()
    WelcomeScreen(navController = navController)
}
