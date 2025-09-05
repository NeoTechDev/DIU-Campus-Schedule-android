package com.om.diucampusschedule.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.ReminderOption
import com.om.diucampusschedule.domain.model.Task
import com.om.diucampusschedule.domain.model.TaskGroup
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    onAddTask: (Task) -> Unit,
    onDismiss: () -> Unit,
    existingTask: Task? = null,
    taskGroups: List<TaskGroup> = emptyList(),
    selectedGroupId: Long = 0,
    onAddTaskGroup: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }
    var date by remember { mutableStateOf(existingTask?.date ?: "") }
    var time by remember { mutableStateOf(existingTask?.time ?: "") }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var reminderOption by remember { mutableStateOf(existingTask?.reminderOption ?: ReminderOption.NONE) }
    var groupId by remember { mutableStateOf(existingTask?.groupId ?: selectedGroupId) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US)
    val saveDateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.US)
    val displayDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US)

    var selectedLocalDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTimePair by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Initialize selectedLocalDate and selectedTimePair from existing task
    LaunchedEffect(existingTask) {
        if (existingTask != null) {
            // Parse the date
            if (existingTask.date.isNotEmpty()) {
                try {
                    val inputFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.US)
                    selectedLocalDate = LocalDate.parse(existingTask.date, inputFormat)
                } catch (e: Exception) {
                    // Handle parsing error
                }
            }

            // Parse the time
            if (existingTask.time.isNotEmpty()) {
                try {
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                    val parsedTime = timeFormat.parse(existingTask.time)
                    val calendar = Calendar.getInstance()
                    calendar.time = parsedTime!!
                    selectedTimePair = Pair(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
                } catch (e: Exception) {
                    // Handle parsing error
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    if (showDateTimePicker) {
        DateTimePicker(
            onDateTimeConfirmed = { pickedDate, pickedTime ->
                showDateTimePicker = false
                selectedLocalDate = pickedDate
                selectedTimePair = pickedTime

                date = pickedDate?.format(displayDateFormatter) ?: ""

                val formattedTime = pickedTime?.let { timePair ->
                    String.format("%02d:%02d", timePair.first, timePair.second)
                } ?: ""

                val calendarForTime = Calendar.getInstance()
                if (pickedTime != null) {
                    calendarForTime.set(Calendar.HOUR_OF_DAY, pickedTime.first)
                    calendarForTime.set(Calendar.MINUTE, pickedTime.second)
                    time = timeFormatter.format(calendarForTime.time)
                } else {
                    time = ""
                }
            },
            onDismissDialog = { showDateTimePicker = false }
        )
    }

    // Add this function to check if time difference is at least 30 minutes
    fun isTimeDifferenceAtLeast30Minutes(selectedDate: LocalDate?, selectedTime: Pair<Int, Int>?): Boolean {
        if (selectedDate == null || selectedTime == null) return false
        
        val selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(selectedTime.first, selectedTime.second))
        val currentDateTime = LocalDateTime.now()
        
        return Duration.between(currentDateTime, selectedDateTime).toMinutes() >= 30
    }

    // Reminder Options Sheet
    if (showReminderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReminderSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Set Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Modern reminder options with visual indicators
                ReminderOption.values().forEach { option ->
                    val isSelected = reminderOption == option
                    val isOptionEnabled = when (option) {
                        ReminderOption.THIRTY_MINUTES_BEFORE -> isTimeDifferenceAtLeast30Minutes(selectedLocalDate, selectedTimePair)
                        ReminderOption.BOTH -> isTimeDifferenceAtLeast30Minutes(selectedLocalDate, selectedTimePair)
                        else -> true
                    }
                    
                    // If 30m option is disabled, also disable BOTH option if it's currently selected
                    if (!isTimeDifferenceAtLeast30Minutes(selectedLocalDate, selectedTimePair) && 
                        (reminderOption == ReminderOption.THIRTY_MINUTES_BEFORE || reminderOption == ReminderOption.BOTH)) {
                        reminderOption = ReminderOption.ON_TIME
                    }
                    
                    val label = when (option) {
                        ReminderOption.NONE -> "None"
                        ReminderOption.ON_TIME -> "At Event Time"
                        ReminderOption.THIRTY_MINUTES_BEFORE -> "30 Min Before"
                        ReminderOption.BOTH -> "Both"
                    }

                    val description = when (option) {
                        ReminderOption.NONE -> "No reminder"
                        ReminderOption.ON_TIME -> "Notify when event starts"
                        ReminderOption.THIRTY_MINUTES_BEFORE -> if (isOptionEnabled) "Notify 30 minutes before" else "Event must be at least 30 minutes in the future"
                        ReminderOption.BOTH -> if (isOptionEnabled) "Notify at both times" else "Event must be at least 30 minutes in the future"
                    }

                    val icon = when (option) {
                        ReminderOption.NONE -> painterResource(id = R.drawable.reminder_off)
                        ReminderOption.ON_TIME -> painterResource(id = R.drawable.single_reminder)
                        ReminderOption.THIRTY_MINUTES_BEFORE -> painterResource(id = R.drawable.single_reminder)
                        ReminderOption.BOTH -> painterResource(id = R.drawable.two_reminders)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isOptionEnabled) {
                                    Modifier.clickable {
                                        reminderOption = option
                                        showReminderSheet = false
                                    }
                                } else Modifier
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        border = if (isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isSelected){
                                            if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        } else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = icon,
                                    contentDescription = label,
                                    tint = if (isSelected){
                                        if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White
                                    } else if (isOptionEnabled) {
                                        if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                                    } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected){
                                        if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White
                                    } else if (isOptionEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = description,
                                    fontSize = 14.sp,
                                    color = if (isOptionEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            if (isSelected) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))
            }
        }
    }

    // Add Group Dialog
    if (showAddGroupDialog && onAddTaskGroup != null) {
        AddTaskGroupDialog(
            onDismissRequest = { showAddGroupDialog = false },
            onConfirm = { groupName ->
                onAddTaskGroup(groupName)
                showAddGroupDialog = false
            }
        )
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 48.dp)
                .animateContentSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Close Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTask == null) "New Task" else "Edit Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { onDismiss() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title Field with modern styling
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    isError = it.isBlank()
                },
                label = { Text(text = "Task Title") },
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(text = "Title cannot be empty")
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors =  OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

//            Spacer(modifier = Modifier.height(16.dp))

            // Description Field with modern styling
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(text = "Notes (optional)") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add group selection using horizontal scrollable chips instead of dropdown
            Text(
                text = "Group",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Horizontal scrolling row of group chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                taskGroups.forEach { group ->
                    val isSelected = group.id == groupId
                    
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) 
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary) 
                        else 
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { groupId = group.id }
                    ) {
                        Text(
                            text = group.name,
                            color = if (isSelected){
                                if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White
                            }
                            else
                                if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // Add Group button
                if (onAddTaskGroup != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showAddGroupDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.add),
                                contentDescription = "Add Group",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New Group",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date/Time and Reminder side by side with adaptive layout
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date and Time Picker Trigger - Modern card style
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDateTimePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (date.isEmpty() && time.isEmpty()) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
                        ),
                        border = if (date.isNotEmpty() || time.isNotEmpty())
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        else
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (date.isEmpty() && time.isEmpty()) {
                                            if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                        } else {
                                            if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.add_date),
                                    contentDescription = "Date Time",
                                    tint = if (date.isEmpty() && time.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else {
                                        if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = if (date.isEmpty()) "Date and Time" else date,
                                    fontSize = 14.sp,
                                    fontWeight = if (date.isNotEmpty()) FontWeight.Medium else FontWeight.Normal,
                                    color = if (date.isEmpty()) {
                                        if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    } else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                if (time.isNotEmpty()) {
                                    Text(
                                        text = time,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Reminder Button - Modern card style with visual indicator
                    Card(
                        modifier = Modifier
                            .weight(0.8f)
                            .clip(RoundedCornerShape(12.dp))
                            .then(if (time.isNotEmpty()) Modifier.clickable { showReminderSheet = true } else Modifier), // Conditional clickable
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (time.isEmpty()) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            } else if (reminderOption == ReminderOption.NONE) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                        border = if (time.isEmpty()) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        } else if (reminderOption != ReminderOption.NONE) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (time.isEmpty()) {
                                            if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant
                                        } else if (reminderOption == ReminderOption.NONE) {
                                            if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                        } else {
                                            if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val reminderIcon = when (reminderOption) {
                                    ReminderOption.NONE -> painterResource(id = R.drawable.reminder_off)
                                    ReminderOption.ON_TIME -> painterResource(id = R.drawable.single_reminder)
                                    ReminderOption.THIRTY_MINUTES_BEFORE -> painterResource(id = R.drawable.single_reminder)
                                    ReminderOption.BOTH -> painterResource(id = R.drawable.two_reminders)
                                }

                                Icon(
                                    painter = reminderIcon,
                                    contentDescription = "Reminder",
                                    tint = if (time.isEmpty()) {
                                        if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    } else if (reminderOption == ReminderOption.NONE) {
                                        if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            val reminderText = when (reminderOption) {
                                ReminderOption.NONE -> "Remind"
                                ReminderOption.ON_TIME -> "At Event Time"
                                ReminderOption.THIRTY_MINUTES_BEFORE -> "30m Before"
                                ReminderOption.BOTH -> "Both"
                            }

                            Text(
                                text = reminderText,
                                fontSize = 14.sp,
                                fontWeight = if (reminderOption != ReminderOption.NONE) FontWeight.Medium else FontWeight.Normal,
                                color = if (time.isEmpty()) {
                                    if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                } else if (reminderOption == ReminderOption.NONE) {
                                    if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button with elevation and animation
            Button(
                onClick = {
                    /*if (title.isBlank()) {
                        Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }*/
                    if(title.isBlank()) {
                        isError = true
                        return@Button
                    }

                    val saveDate = selectedLocalDate?.format(saveDateFormatter) ?: ""
                    
                    val reminderTimeOnTime = if (reminderOption == ReminderOption.ON_TIME || reminderOption == ReminderOption.BOTH) {
                        selectedLocalDate?.let { pickedDate ->
                            selectedTimePair?.let { timePair ->
                                val localDateTime = LocalDateTime.of(
                                    pickedDate,
                                    LocalTime.of(timePair.first, timePair.second)
                                )
                                localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            }
                        }
                    } else null
                    
                    val reminderTime30MinBefore = if (reminderOption == ReminderOption.THIRTY_MINUTES_BEFORE || reminderOption == ReminderOption.BOTH) {
                        selectedLocalDate?.let { pickedDate ->
                            selectedTimePair?.let { timePair ->
                                val localDateTime = LocalDateTime.of(
                                    pickedDate,
                                    LocalTime.of(timePair.first, timePair.second)
                                ).minusMinutes(30)
                                localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            }
                        }
                    } else null
                    
                    val task = Task(
                        title = title,
                        description = description,
                        date = saveDate,
                        time = time,
                        isCompleted = existingTask?.isCompleted ?: false,
                        reminderOption = reminderOption,
                        reminderTimeOnTime = reminderTimeOnTime,
                        reminderTime30MinBefore = reminderTime30MinBefore,
                        groupId = groupId
                    )
                    
                    onAddTask(task)
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp) // Taller button for better touch target
            ) {
                Text("Save Task", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}