package com.om.diucampusschedule.data.local.dao

import androidx.room.*
import com.om.diucampusschedule.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE group_id = :groupId ORDER BY created_at DESC")
    fun getTasksByGroup(groupId: Long): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY created_at DESC")
    fun getIncompleteTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE is_completed = 1 ORDER BY updated_at DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY time ASC")
    fun getTasksByDate(date: String): Flow<List<TaskEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
    
    @Query("DELETE FROM tasks WHERE is_completed = 1")
    suspend fun deleteAllCompletedTasks()
    
    @Query("UPDATE tasks SET is_completed = :isCompleted, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE tasks SET group_id = 0 WHERE group_id = :groupId")
    suspend fun moveTasksToDefaultGroup(groupId: Long)
    
    @Query("UPDATE tasks SET group_id = :newGroupId WHERE group_id = :oldGroupId")
    suspend fun moveTasksToGroup(oldGroupId: Long, newGroupId: Long)
    
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0")
    suspend fun getIncompleteTaskCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1")
    suspend fun getCompletedTaskCount(): Int
}
