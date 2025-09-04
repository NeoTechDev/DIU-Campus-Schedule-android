package com.om.diucampusschedule.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.om.diucampusschedule.ui.navigation.BottomNavItem
import com.om.diucampusschedule.ui.navigation.Screen

@Composable
fun DIUBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Enhanced color scheme with gradients
    val surfaceColor = MaterialTheme.colorScheme.surface
    val selectedPrimaryColor = MaterialTheme.colorScheme.primary
    val selectedSecondaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val unselectedColor = Color.Gray

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            ),
        color = surfaceColor
    ) {
        // Subtle gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem.items.forEach { item ->
                    val isSelected = currentRoute == item.route

                    EnhancedBottomNavItemContainer(
                        item = item,
                        isSelected = isSelected,
                        selectedPrimaryColor = selectedPrimaryColor,
                        selectedSecondaryColor = selectedSecondaryColor,
                        unselectedColor = unselectedColor,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedBottomNavItemContainer(
    item: BottomNavItem,
    isSelected: Boolean,
    selectedPrimaryColor: Color,
    selectedSecondaryColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    // Enhanced animations with different curves - dynamic width based on text length
    val baseWidth = 56.dp
    val textWidth = if (isSelected) {
        // Calculate approximate width based on text length - more generous sizing
        when (item.title.length) {
            in 0..4 -> 90.dp
            in 5..7 -> 110.dp
            in 8..10 -> 130.dp
            else -> 150.dp
        }
    } else baseWidth
    
    val animatedWidth by animateDpAsState(
        targetValue = textWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "container_width"
    )

    val animatedHeight by animateDpAsState(
        targetValue = if (isSelected) 44.dp else 56.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "container_height"
    )

    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                selectedPrimaryColor.copy(alpha = 0.12f),
                selectedSecondaryColor.copy(alpha = 0.12f)
            )
        )
    } else {
        Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
    }

    // Scale animation for press feedback
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "press_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(width = animatedWidth, height = animatedHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = if (isSelected) backgroundBrush else Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.Transparent)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            EnhancedBottomNavIcon(
                item = item,
                isSelected = isSelected,
                selectedPrimaryColor = selectedPrimaryColor,
                unselectedColor = unselectedColor
            )

            // Animated label appearance
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(
                    animationSpec = tween(300, delayMillis = 100)
                ) + slideInHorizontally(
                    animationSpec = tween(300, delayMillis = 100)
                ),
                exit = fadeOut(
                    animationSpec = tween(200)
                ) + slideOutHorizontally(
                    animationSpec = tween(200)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.title,
                        color = selectedPrimaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    }

    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun EnhancedBottomNavIcon(
    item: BottomNavItem,
    isSelected: Boolean,
    selectedPrimaryColor: Color,
    unselectedColor: Color,
    modifier: Modifier = Modifier
) {
    // Smooth color transition
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) selectedPrimaryColor else unselectedColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_color"
    )

    // Icon size with bounce effect
    val animatedIconSize by animateDpAsState(
        targetValue = if (item.route == Screen.Today.route) {
            if (isSelected) 34.dp else 30.dp
        } else {
            if (isSelected) 28.dp else 24.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_size"
    )


    Icon(
        imageVector = ImageVector.vectorResource(
            id = if (isSelected) item.selectedIcon else item.unselectedIcon
        ),
        contentDescription = item.title,
        tint = animatedColor,
        modifier = modifier
            .size(animatedIconSize)
    )
}