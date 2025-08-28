package com.om.diucampusschedule.data.local.dao

import androidx.room.*
import com.om.diucampusschedule.data.local.entities.RoutineEntity
import com.om.diucampusschedule.data.local.entities.RoutineScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    
    // Routine Schedule operations
    @Query("SELECT * FROM routine_schedules WHERE department = :department ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestScheduleForDepartment(department: String): RoutineScheduleEntity?
    
    @Query("SELECT * FROM routine_schedules WHERE department = :department ORDER BY createdAt DESC LIMIT 1")
    fun observeLatestScheduleForDepartment(department: String): Flow<RoutineScheduleEntity?>
    
    @Query("SELECT * FROM routine_schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: String): RoutineScheduleEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: RoutineScheduleEntity)
    
    @Update
    suspend fun updateSchedule(schedule: RoutineScheduleEntity)
    
    @Query("DELETE FROM routine_schedules WHERE department = :department")
    suspend fun deleteSchedulesForDepartment(department: String)
    
    @Query("UPDATE routine_schedules SET isSynced = :synced, lastSyncTime = :syncTime WHERE id = :scheduleId")
    suspend fun updateSyncStatus(scheduleId: String, synced: Boolean, syncTime: Long)
    
    // Routine Item operations
    @Query("SELECT * FROM routine_items WHERE scheduleId = :scheduleId")
    suspend fun getRoutineItemsForSchedule(scheduleId: String): List<RoutineEntity>
    
    @Query("SELECT * FROM routine_items WHERE scheduleId = :scheduleId")
    fun observeRoutineItemsForSchedule(scheduleId: String): Flow<List<RoutineEntity>>
    
    @Query("""
        SELECT * FROM routine_items 
        WHERE scheduleId = :scheduleId 
        AND department = :department 
        AND (
           (:isStudent = 1 AND batch = :batch AND (
                section = :section OR 
                labSection = :labSection OR 
                (LENGTH(:section) = 1 AND section LIKE (:section || '%') AND LENGTH(section) > 1 AND 
                 CAST(SUBSTR(section, 2) AS INTEGER) IS NOT NULL)
            ))
            OR 
            (:isStudent = 0 AND teacherInitial = :teacherInitial)
        )
        ORDER BY 
            CASE day 
                WHEN 'Saturday' THEN 1
                WHEN 'Sunday' THEN 2
                WHEN 'Monday' THEN 3
                WHEN 'Tuesday' THEN 4
                WHEN 'Wednesday' THEN 5
                WHEN 'Thursday' THEN 6
                WHEN 'Friday' THEN 7
                ELSE 8
            END,
            time
    """)
    suspend fun getRoutineForUser(
        scheduleId: String,
        department: String,
        isStudent: Boolean,
        batch: String = "",
        section: String = "",
        labSection: String = "",
        teacherInitial: String = ""
    ): List<RoutineEntity>
    
    @Query("""
        SELECT * FROM routine_items 
        WHERE scheduleId = :scheduleId 
        AND department = :department 
        AND day = :day
        AND (
           (:isStudent = 1 AND batch = :batch AND (
                section = :section OR 
                labSection = :labSection OR 
                (LENGTH(:section) = 1 AND section LIKE (:section || '%') AND LENGTH(section) > 1 AND 
                 CAST(SUBSTR(section, 2) AS INTEGER) IS NOT NULL)
            ))
            OR 
            (:isStudent = 0 AND teacherInitial = :teacherInitial)
        )
        ORDER BY time
    """)
    suspend fun getRoutineForUserAndDay(
        scheduleId: String,
        department: String,
        day: String,
        isStudent: Boolean,
        batch: String = "",
        section: String = "",
        labSection: String = "",
        teacherInitial: String = ""
    ): List<RoutineEntity>
    
    @Query("""
        SELECT * FROM routine_items 
        WHERE scheduleId = :scheduleId 
        AND department = :department 
        AND day = :day
        AND (
            (:isStudent = 1 AND batch = :batch AND (
                section = :section OR 
                labSection = :labSection OR 
                (LENGTH(:section) = 1 AND section LIKE (:section || '%') AND LENGTH(section) > 1 AND 
                 CAST(SUBSTR(section, 2) AS INTEGER) IS NOT NULL)
            ))
            OR 
            (:isStudent = 0 AND teacherInitial = :teacherInitial)
        )
        ORDER BY time
    """)
    fun observeRoutineForUserAndDay(
        scheduleId: String,
        department: String,
        day: String,
        isStudent: Boolean,
        batch: String = "",
        section: String = "",
        labSection: String = "",
        teacherInitial: String = ""
    ): Flow<List<RoutineEntity>>
    
    @Query("""
        SELECT DISTINCT day FROM routine_items 
        WHERE scheduleId = :scheduleId 
        AND department = :department 
        AND (
             (:isStudent = 1 AND batch = :batch AND (
                section = :section OR 
                labSection = :labSection OR 
                (LENGTH(:section) = 1 AND section LIKE (:section || '%') AND LENGTH(section) > 1 AND 
                 CAST(SUBSTR(section, 2) AS INTEGER) IS NOT NULL)
            ))
            OR 
            (:isStudent = 0 AND teacherInitial = :teacherInitial)
        )
        ORDER BY 
            CASE day 
                WHEN 'Saturday' THEN 1
                WHEN 'Sunday' THEN 2
                WHEN 'Monday' THEN 3
                WHEN 'Tuesday' THEN 4
                WHEN 'Wednesday' THEN 5
                WHEN 'Thursday' THEN 6
                WHEN 'Friday' THEN 7
                ELSE 8
            END
    """)
    suspend fun getActiveDaysForUser(
        scheduleId: String,
        department: String,
        isStudent: Boolean,
        batch: String = "",
        section: String = "",
        labSection: String = "",
        teacherInitial: String = ""
    ): List<String>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineItems(items: List<RoutineEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineItem(item: RoutineEntity)
    
    @Update
    suspend fun updateRoutineItem(item: RoutineEntity)
    
    @Query("DELETE FROM routine_items WHERE scheduleId = :scheduleId")
    suspend fun deleteRoutineItemsForSchedule(scheduleId: String)
    
    @Query("DELETE FROM routine_items WHERE department = :department")
    suspend fun deleteRoutineItemsForDepartment(department: String)
    
    @Query("DELETE FROM routine_items")
    suspend fun clearAllRoutineItems()
    
    @Query("DELETE FROM routine_schedules")
    suspend fun clearAllSchedules()
    
    // Transaction to replace entire routine
    @Transaction
    suspend fun replaceRoutineForDepartment(
        department: String,
        schedule: RoutineScheduleEntity,
        items: List<RoutineEntity>
    ) {
        deleteSchedulesForDepartment(department)
        deleteRoutineItemsForDepartment(department)
        insertSchedule(schedule)
        insertRoutineItems(items)
    }
}
