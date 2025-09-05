package com.om.diucampusschedule.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.R
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS,
    ERROR,
    INFO,
    RECEIVED_TASK
}

@Composable
fun CustomToast(
    message: String,
    subMessage: String? = null,
    type: ToastType = ToastType.SUCCESS,
    duration: Long = 3000,
    onDismiss: () -> Unit
) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }

    LaunchedEffect(visible) {
        delay(duration)
        visible.targetState = false
        delay(300) // Wait for exit animation
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visibleState = visible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            val (backgroundColor, iconColor, iconRes) = when(type) {
                ToastType.SUCCESS -> Triple(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary,
                    R.drawable.check_circle
                )
                ToastType.ERROR -> Triple(
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.onError,
                    R.drawable.cross_24
                )
                ToastType.INFO -> Triple(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.onSecondary,
                    R.drawable.share_square_24
                )
                ToastType.RECEIVED_TASK -> Triple(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.onSecondary,
                    R.drawable.android_device
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.2f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = iconColor
                    )

                    if (subMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subMessage,
                            fontSize = 14.sp,
                            color = iconColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}