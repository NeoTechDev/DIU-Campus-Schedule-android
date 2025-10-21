package com.om.diucampusschedule.ui.screens.today.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRole

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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Animated menu items
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically { it / 2 } + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + slideOutVertically { it / 2 } + scaleOut(targetScale = 0.8f)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
            ) {

                // Find Course
                ActionMenuItem(
                    icon = R.drawable.search_course,
                    label = "Find Course",
                    onClick = onFindCourseClick
                )

                // Add Task
                ActionMenuItem(
                    icon = R.drawable.add_task,
                    label = "Add Task",
                    onClick = onAddTaskClick
                )

                // Faculty Info
                ActionMenuItem(
                    icon = R.drawable.faculty_info,
                    label = "Faculty Info",
                    onClick = onFacultyInfoClick
                )

                when(user?.role){
                    UserRole.STUDENT -> {
                        // Student Portal
                        ActionMenuItem(
                            icon = R.drawable.student_portal,
                            label = "Student Portal",
                            onClick = onStudentPortalClick
                        )
                    }
                    UserRole.TEACHER -> {
                        // Teacher Portal
                        ActionMenuItem(
                            icon = R.drawable.teacher_portal,
                            label = "Teacher Portal",
                            onClick = onTeacherPortalClick
                        )
                    }
                    else -> {
                        // Student Portal
                        ActionMenuItem(
                            icon = R.drawable.student_portal,
                            label = "Student Portal",
                            onClick = onStudentPortalClick
                        )
                        // Teacher Portal
                        ActionMenuItem(
                            icon = R.drawable.teacher_portal,
                            label = "Teacher Portal",
                            onClick = onTeacherPortalClick
                        )
                    }
                }

                // BLC
                ActionMenuItem(
                    icon = R.drawable.blc,
                    label = "BLC",
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

        val transition = updateTransition(targetState = isExpanded, label = "FAB Transition")

        val fabSize by transition.animateDp(label = "FAB Size") { expanded ->
            if (expanded) 48.dp else 56.dp // smaller when expanded
        }

        val fabShape by transition.animateDp(label = "FAB Shape") { expanded ->
            if (expanded) 28.dp else 16.dp // round when expanded
        }

        val containerColor by transition.animateColor(label = "FAB Color") { expanded ->
            if (expanded) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.primary
        }

        val iconRotation by transition.animateFloat(label = "Icon Rotation") { expanded ->
            if (expanded) 90f else 0f
        }

        FloatingActionButton(
            onClick = onToggleExpand,
            containerColor = containerColor,
            shape = RoundedCornerShape(fabShape),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 6.dp
            ),
            modifier = Modifier
                .size(fabSize)
                .shadow(fabElevation, RoundedCornerShape(fabShape))
        ) {
            Icon(
                painter = if (isExpanded)
                    painterResource(R.drawable.ic_close)
                else painterResource(R.drawable.rocket),
                contentDescription = if (isExpanded) "Close menu" else "Open menu",
                tint = if (isExpanded)
                    MaterialTheme.colorScheme.onSecondary
                else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(iconRotation)
            )
        }

    }
}

@Composable
private fun ActionMenuItem(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}