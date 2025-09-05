package com.om.diucampusschedule.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.om.diucampusschedule.domain.model.TaskGroup

@Entity(tableName = "task_groups")
data class TaskGroupEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "color")
    val color: Long = 0xFF6200EE, // Default color in ARGB format
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

// Extension function to convert Entity to Domain Model
fun TaskGroupEntity.toDomainModel(): TaskGroup {
    return TaskGroup(
        id = id,
        name = name,
        color = color
    )
}

// Extension function to convert Domain Model to Entity
fun TaskGroup.toEntity(): TaskGroupEntity {
    return TaskGroupEntity(
        id = id,
        name = name,
        color = color,
        createdAt = if (id == 0L) System.currentTimeMillis() else System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
