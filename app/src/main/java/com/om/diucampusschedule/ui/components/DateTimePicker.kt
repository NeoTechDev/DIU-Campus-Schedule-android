package com.om.diucampusschedule.ui.components

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar

// Fixed sizes for the dialog
private val DIALOG_HEIGHT = 520.dp
private val DIALOG_MIN_HEIGHT = 500.dp
private val DIALOG_MAX_HEIGHT = 550.dp

@Composable
fun DateTimePicker(
    onDateTimeConfirmed: (LocalDate?, Pair<Int, Int>?) -> Unit, // Modified callback
    onDismissDialog: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Dialog(onDismissRequest = onDismissDialog) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .heightIn(min = DIALOG_MIN_HEIGHT, max = DIALOG_MAX_HEIGHT)
                .height(DIALOG_HEIGHT)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        when {
                            dragAmount < -15 -> selectedDate = selectedDate.plusMonths(1)
                            dragAmount > 15 -> selectedDate = selectedDate.minusMonths(1)
                        }
                    }
                }
        ) {
            MonthYearSelector(
                selectedDate = selectedDate,
                onDateChanged = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CalendarGrid(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    TimePickerButton(
                        selectedTime = selectedTime,
                        onTimeSelected = { hour, minute ->
                            selectedTime = Pair(hour, minute)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onDismissDialog)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Done",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            onDateTimeConfirmed(selectedDate, selectedTime) // Modified callback call
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun TimePickerButton(
    selectedTime: Pair<Int, Int>?,
    onTimeSelected: (Int, Int) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = "Set Time",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        val timeText = selectedTime?.let {
            String.format("%02d:%02d", it.first, it.second)
        } ?: "Set time"

        Text(
            text = timeText,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp
        )
    }

    if (showTimePicker) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                onTimeSelected(hourOfDay, minute) // Callback with hour and minute
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.setOnCancelListener {
            showTimePicker = false
        }
        timePickerDialog.show()
    }
}


@Composable
fun MonthYearSelector(selectedDate: LocalDate, onDateChanged: (LocalDate) -> Unit) {
    val monthYear = YearMonth.from(selectedDate)
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onDateChanged(selectedDate.minusMonths(1))}
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Month",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
            )
        }

        AnimatedContent(
            targetState = monthYear,
            transitionSpec = {
                // Slide down transition for both directions
                slideInVertically { height -> -height } + fadeIn() togetherWith
                        slideOutVertically { height -> height } + fadeOut()
            }
        ) { currentMonth ->
            Text(
                text = currentMonth.format(formatter),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }

        IconButton(
            onClick = { onDateChanged(selectedDate.plusMonths(1))}
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Month",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}

@Composable
fun CalendarGrid(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val month = YearMonth.from(selectedDate)

    Column(modifier = modifier) {
        // Weekday headers - static, not animated
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
            weekdays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Only the days grid is animated
        AnimatedContent(
            targetState = month,
            transitionSpec = {
                val direction = if (targetState.isAfter(initialState)) {
                    // Moving forward in time (to future month)
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    // Moving backward in time (to previous month)
                    AnimatedContentTransitionScope.SlideDirection.Right
                }

                ContentTransform(
                    targetContentEnter = slideIntoContainer(
                        towards = direction,
                        animationSpec = tween(350)
                    ) + fadeIn(animationSpec = tween(350)),
                    initialContentExit = slideOutOfContainer(
                        towards = direction,
                        animationSpec = tween(350)
                    ) + fadeOut(animationSpec = tween(350))
                ).using(
                    SizeTransform(clip = false)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { currentMonth ->
            CalendarDaysGrid(currentMonth, selectedDate, onDateSelected)
        }
    }
}

@Composable
fun CalendarDaysGrid(month: YearMonth, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val firstDayOfMonth = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val today = LocalDate.now()

    Column {
        // Calendar days
        for (week in 0 until 6) {
            Row {
                for (dayOfWeek in 0 until 7) {
                    val dayNumber = week * 7 + dayOfWeek - firstDayOfWeek + 1

                    if (dayNumber in 1..daysInMonth) {
                        val date = month.atDay(dayNumber)
                        val isSelected = date == selectedDate
                        val isToday = date == today

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                fontSize = 14.sp,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (isToday) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}