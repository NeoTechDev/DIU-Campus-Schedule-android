package com.om.diucampusschedule.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.om.diucampusschedule.data.repository.TaskRepository

@AndroidEntryPoint
class TaskCompleteReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var taskRepository: TaskRepository
    
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("task_id", -1L)
        
        if (taskId == -1L) return
        
        // Use coroutines to handle database operation
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Mark task as completed
                taskRepository.updateTaskCompletion(taskId, true)
                
                // Dismiss the notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationId = ReminderBroadcastReceiver.NOTIFICATION_ID_BASE + taskId.toInt()
                val notificationId30Min = notificationId + 10000
                
                notificationManager.cancel(notificationId)
                notificationManager.cancel(notificationId30Min)
                
                // Show confirmation toast on main thread
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Task marked as complete", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                // Handle error
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Failed to complete task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
