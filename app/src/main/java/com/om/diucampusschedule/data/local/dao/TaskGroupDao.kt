package com.om.diucampusschedule.data.local.dao

import androidx.room.*
import com.om.diucampusschedule.data.local.entities.TaskGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskGroupDao {
    
    @Query("SELECT * FROM task_groups ORDER BY created_at ASC")
    fun getAllTaskGroups(): Flow<List<TaskGroupEntity>>
    
    @Query("SELECT * FROM task_groups ORDER BY created_at ASC")
    suspend fun getAllTaskGroupsList(): List<TaskGroupEntity>
    
    @Query("SELECT * FROM task_groups WHERE id = :id")
    suspend fun getTaskGroupById(id: Long): TaskGroupEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskGroup(group: TaskGroupEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskGroups(groups: List<TaskGroupEntity>)
    
    @Update
    suspend fun updateTaskGroup(group: TaskGroupEntity)
    
    @Delete
    suspend fun deleteTaskGroup(group: TaskGroupEntity)
    
    @Query("DELETE FROM task_groups WHERE id = :id")
    suspend fun deleteTaskGroupById(id: Long)
    
    @Query("DELETE FROM task_groups")
    suspend fun deleteAllTaskGroups()
    
    @Query("SELECT COUNT(*) FROM task_groups")
    suspend fun getTaskGroupCount(): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM task_groups WHERE id = :id)")
    suspend fun taskGroupExists(id: Long): Boolean
}
