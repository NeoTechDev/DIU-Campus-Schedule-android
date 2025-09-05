package com.om.diucampusschedule.data.repository

import com.om.diucampusschedule.data.local.dao.TaskDao
import com.om.diucampusschedule.data.local.dao.TaskGroupDao
import com.om.diucampusschedule.data.local.entities.toDomainModel
import com.om.diucampusschedule.data.local.entities.toEntity
import com.om.diucampusschedule.domain.model.Task
import com.om.diucampusschedule.domain.model.TaskGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val taskGroupDao: TaskGroupDao
) {
    
    // Task operations
    fun getAllTasks(): Flow<List<Task>> = 
        taskDao.getAllTasks().map { entities -> entities.map { it.toDomainModel() } }
    
    fun getTasksByGroup(groupId: Long): Flow<List<Task>> = 
        taskDao.getTasksByGroup(groupId).map { entities -> entities.map { it.toDomainModel() } }
    
    suspend fun getTaskById(id: Long): Task? = 
        taskDao.getTaskById(id)?.toDomainModel()
    
    fun getIncompleteTasks(): Flow<List<Task>> = 
        taskDao.getIncompleteTasks().map { entities -> entities.map { it.toDomainModel() } }
    
    fun getCompletedTasks(): Flow<List<Task>> = 
        taskDao.getCompletedTasks().map { entities -> entities.map { it.toDomainModel() } }
    
    fun getTasksByDate(date: String): Flow<List<Task>> = 
        taskDao.getTasksByDate(date).map { entities -> entities.map { it.toDomainModel() } }
    
    suspend fun insertTask(task: Task): Long = 
        taskDao.insertTask(task.toEntity())
    
    suspend fun insertTasks(tasks: List<Task>) = 
        taskDao.insertTasks(tasks.map { it.toEntity() })
    
    suspend fun updateTask(task: Task) = 
        taskDao.updateTask(task.toEntity())
    
    suspend fun deleteTask(task: Task) = 
        taskDao.deleteTask(task.toEntity())
    
    suspend fun deleteTaskById(id: Long) = 
        taskDao.deleteTaskById(id)
    
    suspend fun deleteAllCompletedTasks() = 
        taskDao.deleteAllCompletedTasks()
    
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean) = 
        taskDao.updateTaskCompletion(id, isCompleted)
    
    suspend fun getTaskCount(): Int = 
        taskDao.getTaskCount()
    
    suspend fun getIncompleteTaskCount(): Int = 
        taskDao.getIncompleteTaskCount()
    
    suspend fun getCompletedTaskCount(): Int = 
        taskDao.getCompletedTaskCount()
    
    // Task Group operations
    fun getAllTaskGroups(): Flow<List<TaskGroup>> = 
        taskGroupDao.getAllTaskGroups().map { entities -> entities.map { it.toDomainModel() } }
    
    suspend fun getTaskGroupById(id: Long): TaskGroup? = 
        taskGroupDao.getTaskGroupById(id)?.toDomainModel()
    
    suspend fun insertTaskGroup(group: TaskGroup): Long = 
        taskGroupDao.insertTaskGroup(group.toEntity())
    
    suspend fun insertTaskGroups(groups: List<TaskGroup>) = 
        taskGroupDao.insertTaskGroups(groups.map { it.toEntity() })
    
    suspend fun updateTaskGroup(group: TaskGroup) = 
        taskGroupDao.updateTaskGroup(group.toEntity())
    
    suspend fun deleteTaskGroup(group: TaskGroup) {
        // Move all tasks in this group to default group (group ID 0)
        taskDao.moveTasksToDefaultGroup(group.id)
        taskGroupDao.deleteTaskGroup(group.toEntity())
    }
    
    suspend fun deleteTaskGroupById(id: Long) {
        // Move all tasks in this group to default group (group ID 0)
        taskDao.moveTasksToDefaultGroup(id)
        taskGroupDao.deleteTaskGroupById(id)
    }
    
    suspend fun getTaskGroupCount(): Int = 
        taskGroupDao.getTaskGroupCount()
    
    suspend fun taskGroupExists(id: Long): Boolean = 
        taskGroupDao.taskGroupExists(id)
    
    // Initialize default task group if needed
    suspend fun initializeDefaultGroup() {
        // First clean up any duplicate "All Tasks" groups
        cleanupDuplicateGroups()
        
        // Check if default group already exists
        val defaultGroupExists = taskGroupExists(0)
        if (!defaultGroupExists) {
            val defaultGroup = TaskGroup(id = 0, name = "All Tasks")
            insertTaskGroup(defaultGroup)
        }
    }
    
    // Clean up duplicate groups with same name
    private suspend fun cleanupDuplicateGroups() {
        val allGroups = taskGroupDao.getAllTaskGroupsList()
        val allTasksGroups = allGroups.filter { it.name == "All Tasks" }
        
        if (allTasksGroups.size > 1) {
            // Keep only the group with id = 0, or the first one if none has id = 0
            val groupToKeep = allTasksGroups.find { it.id == 0L } ?: allTasksGroups.first()
            val groupsToDelete = allTasksGroups.filter { it.id != groupToKeep.id }
            
            // Move tasks from duplicate groups to the keeper group
            for (group in groupsToDelete) {
                taskDao.moveTasksToGroup(group.id, groupToKeep.id)
                taskGroupDao.deleteTaskGroupById(group.id)
            }
        }
    }
    
    // Development helper: Reset all groups (use only for debugging)
    suspend fun resetTaskGroups() {
        // Move all tasks to default group first
        val allGroups = taskGroupDao.getAllTaskGroupsList()
        for (group in allGroups) {
            if (group.id != 0L) {
                taskDao.moveTasksToDefaultGroup(group.id)
                taskGroupDao.deleteTaskGroupById(group.id)
            }
        }
        
        // Delete all groups including default
        taskGroupDao.deleteAllTaskGroups()
        
        // Recreate default group
        val defaultGroup = TaskGroup(id = 0, name = "All Tasks")
        insertTaskGroup(defaultGroup)
    }
}
