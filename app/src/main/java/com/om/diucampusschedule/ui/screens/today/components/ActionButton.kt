package com.om.diucampusschedule.ui.screens.today.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.ui.theme.AccentGreen
import com.om.diucampusschedule.ui.theme.AccentOrange
import com.om.diucampusschedule.ui.theme.AccentPurple
import com.om.diucampusschedule.ui.theme.AccentRed
import com.om.diucampusschedule.ui.theme.AccentTeal

@Composable
fun TodayActionButton(
    user: User?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onFindCourseClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onFacultyInfoClick: () -> Unit,
    onStudentPortalClick: () -> Unit,
    onTeacherPortalClick: () -> Unit,
    onBlcClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Animated menu items
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically { it / 2 } + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + slideOutVertically { it / 2 } + scaleOut(targetScale = 0.8f)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
            ) {

                // Find Course
                ActionMenuItem(
                    icon = R.drawable.search_course,
                    label = "Find Course",
                    tint = AccentPurple,
                    onClick = onFindCourseClick
                )

                // Add Task
                ActionMenuItem(
                    icon = R.drawable.add_task,
                    label = "Add Task",
                    tint = AccentOrange,
                    onClick = onAddTaskClick
                )

                // Faculty Info
                ActionMenuItem(
                    icon = R.drawable.faculty_info,
                    label = "Faculty Info",
                    tint = AccentGreen,
                    onClick = onFacultyInfoClick
                )

                when(user?.role){
                    UserRole.STUDENT -> {
                        // Student Portal
                        ActionMenuItem(
                            icon = R.drawable.student_portal,
                            label = "Student Portal",
                            tint = AccentTeal,
                            onClick = onStudentPortalClick
                        )
                    }
                    UserRole.TEACHER -> {
                        // Teacher Portal
                        ActionMenuItem(
                            icon = R.drawable.teacher_portal,
                            label = "Teacher Portal",
                            tint = AccentTeal,
                            onClick = onTeacherPortalClick
                        )
                    }
                    else -> {
                        // Student Portal
                        ActionMenuItem(
                            icon = R.drawable.student_portal,
                            label = "Student Portal",
                            tint = AccentTeal,
                            onClick = onStudentPortalClick
                        )
                        // Teacher Portal
                        ActionMenuItem(
                            icon = R.drawable.teacher_portal,
                            label = "Teacher Portal",
                            tint = AccentTeal,
                            onClick = onTeacherPortalClick
                        )
                    }
                }

                // BLC
                ActionMenuItem(
                    icon = R.drawable.blc,
                    label = "BLC",
                    tint = AccentRed,
                    onClick = onBlcClick
                )
            }
        }

        // Main FAB
        val fabElevation by animateDpAsState(
            targetValue = if (isExpanded) 8.dp else 6.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "fabElevation"
        )

        FloatingActionButton(
            onClick = onToggleExpand,
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.shadow(fabElevation, RoundedCornerShape(16.dp))
        ) {
            Icon(
                painter = if (isExpanded) painterResource(R.drawable.ic_close) else painterResource(R.drawable.rocket),
                contentDescription = if (isExpanded) "Close menu" else "Open menu",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(if (isExpanded) 90f else 0f)
            )
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: Int,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                tint,
                                tint.copy(alpha = 0.9f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}