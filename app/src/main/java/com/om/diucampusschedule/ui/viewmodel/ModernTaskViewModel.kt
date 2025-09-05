package com.om.diucampusschedule.ui.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.data.repository.TaskRepository
import com.om.diucampusschedule.domain.model.ReminderOption
import com.om.diucampusschedule.domain.model.Task
import com.om.diucampusschedule.domain.model.TaskGroup
import com.om.diucampusschedule.receiver.ReminderBroadcastReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ModernTaskViewModel @Inject constructor(
    application: Application,
    private val taskRepository: TaskRepository
) : AndroidViewModel(application) {

    private val _selectedGroupId = MutableStateFlow<Long>(0)
    val selectedGroupId: StateFlow<Long> = _selectedGroupId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Combine tasks and selected group to show filtered tasks
    val tasks = combine(
        taskRepository.getAllTasks(),
        _selectedGroupId
    ) { allTasks, groupId ->
        if (groupId == 0L) allTasks else allTasks.filter { it.groupId == groupId }
    }

    val taskGroups = taskRepository.getAllTaskGroups()
    val incompleteTasks = taskRepository.getIncompleteTasks()
    val completedTasks = taskRepository.getCompletedTasks()

    init {
        // Initialize default group if needed
        viewModelScope.launch {
            taskRepository.initializeDefaultGroup()
        }
    }

    fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                taskRepository.initializeDefaultGroup()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            try {
                val taskId = taskRepository.insertTask(task)
                val taskWithId = task.copy(id = taskId)
                scheduleReminders(taskWithId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateTask(updatedTask: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(updatedTask)
                // Cancel old reminders and schedule new ones
                cancelReminders(updatedTask)
                scheduleReminders(updatedTask)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(task)
                cancelReminders(task)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            try {
                val updatedTask = task.copy(isCompleted = !task.isCompleted)
                taskRepository.updateTask(updatedTask)
                
                if (updatedTask.isCompleted) {
                    // Cancel reminders when task is completed
                    cancelReminders(updatedTask)
                } else {
                    // Reschedule reminders when task is uncompleted
                    scheduleReminders(updatedTask)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteAllCompletedTasks() {
        viewModelScope.launch {
            try {
                taskRepository.deleteAllCompletedTasks()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Task Group Functions
    fun addTaskGroup(groupName: String) {
        viewModelScope.launch {
            try {
                val newGroup = TaskGroup(name = groupName)
                taskRepository.insertTaskGroup(newGroup)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateTaskGroup(groupId: Long, newName: String) {
        viewModelScope.launch {
            try {
                val existingGroup = taskRepository.getTaskGroupById(groupId)
                existingGroup?.let { group ->
                    val updatedGroup = group.copy(name = newName)
                    taskRepository.updateTaskGroup(updatedGroup)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteTaskGroup(groupId: Long) {
        viewModelScope.launch {
            try {
                if (groupId != 0L) { // Cannot delete default group
                    taskRepository.deleteTaskGroupById(groupId)
                    // Reset selected group if it was deleted
                    if (_selectedGroupId.value == groupId) {
                        _selectedGroupId.value = 0L
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun selectTaskGroup(groupId: Long) {
        _selectedGroupId.value = groupId
    }

    fun getTasksByDate(date: String) = taskRepository.getTasksByDate(date)

    // Reminder Management
    private fun scheduleReminders(task: Task) {
        val context = getApplication<Application>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            when (task.reminderOption) {
                ReminderOption.ON_TIME -> {
                    task.reminderTimeOnTime?.let { time ->
                        scheduleAlarm(alarmManager, context, task, time, ReminderBroadcastReceiver.REMINDER_ON_TIME)
                    }
                }
                ReminderOption.THIRTY_MINUTES_BEFORE -> {
                    task.reminderTime30MinBefore?.let { time ->
                        scheduleAlarm(alarmManager, context, task, time, ReminderBroadcastReceiver.REMINDER_30_MIN_BEFORE)
                    }
                }
                ReminderOption.BOTH -> {
                    task.reminderTimeOnTime?.let { time ->
                        scheduleAlarm(alarmManager, context, task, time, ReminderBroadcastReceiver.REMINDER_ON_TIME)
                    }
                    task.reminderTime30MinBefore?.let { time ->
                        scheduleAlarm(alarmManager, context, task, time, ReminderBroadcastReceiver.REMINDER_30_MIN_BEFORE)
                    }
                }
                ReminderOption.NONE -> {
                    // No reminders to schedule
                }
            }
        } catch (e: SecurityException) {
            // Handle permission error - user needs to grant exact alarm permission
        }
    }

    private fun scheduleAlarm(
        alarmManager: AlarmManager,
        context: Context,
        task: Task,
        triggerTime: Long,
        reminderType: String
    ) {
        if (triggerTime <= System.currentTimeMillis()) return // Don't schedule past alarms

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(ReminderBroadcastReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ReminderBroadcastReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(ReminderBroadcastReceiver.EXTRA_TASK_DESCRIPTION, task.description)
            putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_TYPE, reminderType)
        }

        val requestCode = generateRequestCode(task.id, reminderType)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    private fun cancelReminders(task: Task) {
        val context = getApplication<Application>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel "On Time" Reminder
        val onTimeRequestCode = generateRequestCode(task.id, ReminderBroadcastReceiver.REMINDER_ON_TIME)
        val onTimeIntent = Intent(context, ReminderBroadcastReceiver::class.java)
        val onTimePendingIntent = PendingIntent.getBroadcast(
            context,
            onTimeRequestCode,
            onTimeIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        onTimePendingIntent?.let { alarmManager.cancel(it) }

        // Cancel "30 Minutes Before" Reminder
        val beforeRequestCode = generateRequestCode(task.id, ReminderBroadcastReceiver.REMINDER_30_MIN_BEFORE)
        val beforeIntent = Intent(context, ReminderBroadcastReceiver::class.java)
        val beforePendingIntent = PendingIntent.getBroadcast(
            context,
            beforeRequestCode,
            beforeIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        beforePendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun generateRequestCode(taskId: Long, reminderType: String): Int {
        return when (reminderType) {
            ReminderBroadcastReceiver.REMINDER_30_MIN_BEFORE -> (taskId * 10).toInt() + 1
            else -> (taskId * 10).toInt() // ON_TIME
        }
    }

    // Helper function to create reminder times
    fun createReminderTimes(dateString: String, timeString: String): Pair<Long?, Long?> {
        try {
            val dateTimeFormat = SimpleDateFormat("MM-dd-yyyy hh:mm a", Locale.US)
            val dateTime = dateTimeFormat.parse("$dateString $timeString")
            
            return if (dateTime != null) {
                val calendar = Calendar.getInstance().apply { time = dateTime }
                val onTimeMillis = calendar.timeInMillis
                
                calendar.add(Calendar.MINUTE, -30)
                val thirtyMinBeforeMillis = calendar.timeInMillis
                
                Pair(onTimeMillis, thirtyMinBeforeMillis)
            } else {
                Pair(null, null)
            }
        } catch (e: Exception) {
            return Pair(null, null)
        }
    }

    // Sharing status sealed class
    sealed class SharingStatus {
        object Idle : SharingStatus()
        object Sharing : SharingStatus()
        data class Success(val deviceName: String) : SharingStatus()
        data class Error(val message: String) : SharingStatus()
    }

    // Received task info data class
    data class ReceivedTaskInfo(
        val task: Task,
        val senderName: String
    )

    // Sharing state
    private val _sharingStatus = MutableStateFlow<SharingStatus>(SharingStatus.Idle)
    val sharingStatus: StateFlow<SharingStatus> = _sharingStatus.asStateFlow()

    private val _taskReceived = MutableStateFlow<ReceivedTaskInfo?>(null)
    val taskReceived: StateFlow<ReceivedTaskInfo?> = _taskReceived.asStateFlow()

    // Sharing methods
    fun shareTask(service: NsdServiceInfo, task: Task) {
        viewModelScope.launch {
            try {
                _sharingStatus.value = SharingStatus.Sharing
                // Simulate sharing - replace with actual sharing implementation
                kotlinx.coroutines.delay(1000)
                _sharingStatus.value = SharingStatus.Success(service.serviceName ?: "Unknown Device")
            } catch (e: Exception) {
                _sharingStatus.value = SharingStatus.Error("Failed to share task: ${e.message}")
            }
        }
    }

    fun shareMultipleTasks(service: NsdServiceInfo, tasks: List<Task>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _sharingStatus.value = SharingStatus.Sharing
                // Simulate sharing - replace with actual sharing implementation
                kotlinx.coroutines.delay(1000)
                _sharingStatus.value = SharingStatus.Success(service.serviceName ?: "Unknown Device")
                onSuccess()
            } catch (e: Exception) {
                _sharingStatus.value = SharingStatus.Error("Failed to share tasks: ${e.message}")
            }
        }
    }

    fun onTaskReceivedEventHandled() {
        _taskReceived.value = null
    }
}
