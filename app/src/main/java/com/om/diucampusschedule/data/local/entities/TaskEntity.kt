package com.om.diucampusschedule.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.om.diucampusschedule.domain.model.ReminderOption
import com.om.diucampusschedule.domain.model.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "date")
    val date: String,
    
    @ColumnInfo(name = "time")
    val time: String,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    
    @ColumnInfo(name = "reminder_option")
    val reminderOption: String = ReminderOption.NONE.name,
    
    @ColumnInfo(name = "reminder_time_on_time")
    val reminderTimeOnTime: Long? = null,
    
    @ColumnInfo(name = "reminder_time_30_min_before")
    val reminderTime30MinBefore: Long? = null,
    
    @ColumnInfo(name = "group_id")
    val groupId: Long = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

// Extension function to convert Entity to Domain Model
fun TaskEntity.toDomainModel(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        date = date,
        time = time,
        isCompleted = isCompleted,
        reminderOption = ReminderOption.valueOf(reminderOption),
        reminderTimeOnTime = reminderTimeOnTime,
        reminderTime30MinBefore = reminderTime30MinBefore,
        groupId = groupId
    )
}

// Extension function to convert Domain Model to Entity
fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        date = date,
        time = time,
        isCompleted = isCompleted,
        reminderOption = reminderOption.name,
        reminderTimeOnTime = reminderTimeOnTime,
        reminderTime30MinBefore = reminderTime30MinBefore,
        groupId = groupId,
        createdAt = if (id == 0L) System.currentTimeMillis() else System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
