package com.om.diucampusschedule.ui.screens.today.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.Task
import com.om.diucampusschedule.ui.theme.AccentGreen
import com.om.diucampusschedule.ui.theme.AccentRed
import com.om.diucampusschedule.ui.theme.customFontFamily
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarViewComponent(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    dailyEventCounts: Map<LocalDate, Pair<Int, Int>>
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val today = LocalDate.now()
    var internalCurrentMonth by remember { mutableStateOf(currentMonth) }
    var isAnimating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Month & Year + Navigation Arrows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    isAnimating = true
                    internalCurrentMonth = internalCurrentMonth.minusMonths(1)
                    onMonthChanged(internalCurrentMonth)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Month and Year text inside AnimatedContent
            AnimatedContent(
                targetState = internalCurrentMonth,
                transitionSpec = {
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                },
                label = "monthYearTransition"
            ) { targetMonth ->
                Text(
                    text = targetMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = customFontFamily(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = {
                    isAnimating = true
                    internalCurrentMonth = internalCurrentMonth.plusMonths(1)
                    onMonthChanged(internalCurrentMonth)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Weekday Labels
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Create an array of day names starting with Sunday
            val daysOfWeek = arrayOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

            for (i in 0..6) {
                Text(
                    text = daysOfWeek[i],
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = if (i == 5) // Only Friday (index 5) is red
                        MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontFamily = customFontFamily(),
                    modifier = Modifier.width(36.dp)
                )
            }
        }

        // Calendar Grid with Animation
        AnimatedContent(
            targetState = internalCurrentMonth,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }.using(SizeTransform(clip = false))
            },
            label = "monthTransition",
            modifier = Modifier
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            isAnimating = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            if (!isAnimating) {
                                if (dragAmount > 15) { // Right swipe
                                    isAnimating = true
                                    internalCurrentMonth = internalCurrentMonth.minusMonths(1)
                                    onMonthChanged(internalCurrentMonth)
                                } else if (dragAmount < -15) { // Left swipe
                                    isAnimating = true
                                    internalCurrentMonth = internalCurrentMonth.plusMonths(1)
                                    onMonthChanged(internalCurrentMonth)
                                }
                            }
                        }
                    )
                }
        ) { targetMonth ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Calendar Grid
                val daysInMonth = targetMonth.lengthOfMonth()
                // Adjust firstDayOfMonth calculation to match Sunday as first day (0-based)
                val firstDayOfMonth = targetMonth.atDay(1).dayOfWeek.value % 7
                val dates = (1..daysInMonth).map { it }

                Column {
                    var index = 0
                    // Calculate the number of weeks needed for this month
                    val totalDaysDisplayed = firstDayOfMonth + daysInMonth
                    val weeksNeeded = (totalDaysDisplayed + 6) / 7 // Ceiling division to get number of weeks

                    for (week in 0 until weeksNeeded) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (day in 0..6) {
                                if (week == 0 && day < firstDayOfMonth || index >= daysInMonth) {
                                    Spacer(modifier = Modifier.size(36.dp))
                                } else {
                                    val date = dates[index++]
                                    val currentDate = targetMonth.atDay(date)
                                    val isSelected = selectedDate == currentDate
                                    val isTodayDate = today == currentDate
                                    // Only highlight Friday as special day
                                    val isFriday = currentDate.dayOfWeek.value == 5
                                    val eventCounts = dailyEventCounts[currentDate] ?: Pair(0, 0)
                                    val hasEvents = eventCounts.first > 0 || eventCounts.second > 0

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isTodayDate -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isTodayDate && !isSelected) 1.dp else 0.dp,
                                                color = if (isTodayDate && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedDate = currentDate
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = date.toString(),
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected || isTodayDate) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    isTodayDate -> if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                                    isFriday -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                fontFamily = customFontFamily()
                                            )

                                            if (hasEvents) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    if (eventCounts.first > 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .background(
                                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                    else AccentRed,
                                                                    CircleShape
                                                                )
                                                        )
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                    }
                                                    if (eventCounts.second > 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .background(
                                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                    else AccentGreen,
                                                                    CircleShape
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Legend for dot indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Class routine indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(AccentRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Class Routine",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontFamily = customFontFamily()
                )
            }

            // Task indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(AccentGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Task",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontFamily = customFontFamily()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = { onDismiss() },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = customFontFamily()
                    )
                }
            }

            Surface(
                onClick = {
                    onDateSelected(selectedDate)
                    onDismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Select",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = customFontFamily()
                    )
                }
            }
        }
    }
}

fun calculateDailyEventCounts(
    routineItems: List<RoutineItem>,
    tasks: List<Task>,
    yearMonth: YearMonth
): Map<LocalDate, Pair<Int, Int>> {
    val dailyCounts = mutableMapOf<LocalDate, Pair<Int, Int>>()
    val daysInMonth = yearMonth.lengthOfMonth()
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.US)

    for (dayOfMonth in 1..daysInMonth) {
        val date = yearMonth.atDay(dayOfMonth)
        val formattedDate = date.format(dateFormatter)
        
        // Count routine items for this day of week
        val routineCount = routineItems.count { routine ->
            routine.day.uppercase() == date.dayOfWeek.toString().uppercase()
        }
        
        // Count tasks for this specific date
        val taskCount = tasks.count { task ->
            !task.isCompleted && task.date == formattedDate
        }
        
        dailyCounts[date] = Pair(routineCount, taskCount)
    }
    return dailyCounts
}