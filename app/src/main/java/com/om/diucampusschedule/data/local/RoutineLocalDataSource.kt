package com.om.diucampusschedule.data.local

import com.om.diucampusschedule.data.local.entities.RoutineEntity
import com.om.diucampusschedule.data.local.entities.RoutineScheduleEntity
import com.om.diucampusschedule.data.local.entities.toDomainModel
import com.om.diucampusschedule.data.local.entities.toEntity
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.RoutineSchedule
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineLocalDataSource @Inject constructor(
    private val database: AppDatabase
) {
    private val routineDao = database.routineDao()

    // Schedule operations
    suspend fun saveSchedule(schedule: RoutineSchedule) {
        val scheduleEntity = RoutineScheduleEntity(
            id = schedule.id,
            semester = schedule.semester,
            department = schedule.department,
            effectiveFrom = schedule.effectiveFrom,
            version = schedule.version,
            createdAt = schedule.createdAt,
            updatedAt = schedule.updatedAt,
            isSynced = true,
            lastSyncTime = System.currentTimeMillis()
        )
        
        val itemEntities = schedule.schedule.map { it.toEntity(schedule.id) }
        
        routineDao.replaceRoutineForDepartment(
            department = schedule.department,
            schedule = scheduleEntity,
            items = itemEntities
        )
    }

    suspend fun getLatestScheduleForDepartment(department: String): RoutineSchedule? {
        val scheduleEntity = routineDao.getLatestScheduleForDepartment(department) ?: return null
        val itemEntities = routineDao.getRoutineItemsForSchedule(scheduleEntity.id)
        
        return RoutineSchedule(
            id = scheduleEntity.id,
            semester = scheduleEntity.semester,
            department = scheduleEntity.department,
            effectiveFrom = scheduleEntity.effectiveFrom,
            schedule = itemEntities.map { it.toDomainModel() },
            version = scheduleEntity.version,
            createdAt = scheduleEntity.createdAt,
            updatedAt = scheduleEntity.updatedAt
        )
    }

    fun observeLatestScheduleForDepartment(department: String): Flow<RoutineSchedule?> {
        return routineDao.observeLatestScheduleForDepartment(department).map { scheduleEntity ->
            if (scheduleEntity == null) return@map null
            
            val itemEntities = routineDao.getRoutineItemsForSchedule(scheduleEntity.id)
            
            RoutineSchedule(
                id = scheduleEntity.id,
                semester = scheduleEntity.semester,
                department = scheduleEntity.department,
                effectiveFrom = scheduleEntity.effectiveFrom,
                schedule = itemEntities.map { it.toDomainModel() },
                version = scheduleEntity.version,
                createdAt = scheduleEntity.createdAt,
                updatedAt = scheduleEntity.updatedAt
            )
        }
    }

    // User-specific routine operations
    suspend fun getRoutineForUser(user: User): List<RoutineItem> {
        val schedule = getLatestScheduleForDepartment(user.department) ?: return emptyList()
        
        return routineDao.getRoutineForUser(
            scheduleId = schedule.id,
            department = user.department,
            isStudent = user.role == UserRole.STUDENT,
            batch = user.batch,
            section = user.section,
            labSection = user.labSection,
            teacherInitial = user.initial
        ).map { it.toDomainModel() }
    }

    suspend fun getRoutineForUserAndDay(user: User, day: String): List<RoutineItem> {
        val schedule = getLatestScheduleForDepartment(user.department) ?: return emptyList()
        
        return routineDao.getRoutineForUserAndDay(
            scheduleId = schedule.id,
            department = user.department,
            day = day,
            isStudent = user.role == UserRole.STUDENT,
            batch = user.batch,
            section = user.section,
            labSection = user.labSection,
            teacherInitial = user.initial
        ).map { it.toDomainModel() }
    }

    fun observeRoutineForUserAndDay(user: User, day: String): Flow<List<RoutineItem>> {
        return routineDao.observeLatestScheduleForDepartment(user.department).map { scheduleEntity ->
            if (scheduleEntity == null) return@map emptyList()
            
            routineDao.getRoutineForUserAndDay(
                scheduleId = scheduleEntity.id,
                department = user.department,
                day = day,
                isStudent = user.role == UserRole.STUDENT,
                batch = user.batch,
                section = user.section,
                labSection = user.labSection,
                teacherInitial = user.initial
            ).map { it.toDomainModel() }
        }
    }

    suspend fun getActiveDaysForUser(user: User): List<String> {
        val schedule = getLatestScheduleForDepartment(user.department) ?: return emptyList()
        
        return routineDao.getActiveDaysForUser(
            scheduleId = schedule.id,
            department = user.department,
            isStudent = user.role == UserRole.STUDENT,
            batch = user.batch,
            section = user.section,
            labSection = user.labSection,
            teacherInitial = user.initial
        )
    }

    suspend fun getAllTimeSlotsForDepartment(department: String): List<String> {
        val schedule = getLatestScheduleForDepartment(department) ?: return emptyList()
        
        return routineDao.getAllTimeSlotsForDepartment(
            scheduleId = schedule.id,
            department = department
        )
    }

    // Sync status operations
    suspend fun updateSyncStatus(scheduleId: String, synced: Boolean) {
        routineDao.updateSyncStatus(scheduleId, synced, System.currentTimeMillis())
    }

    suspend fun getScheduleById(scheduleId: String): RoutineScheduleEntity? {
        return routineDao.getScheduleById(scheduleId)
    }

    // Cleanup operations
    suspend fun clearAllRoutines() {
        routineDao.clearAllRoutineItems()
        routineDao.clearAllSchedules()
    }

    suspend fun clearRoutinesForDepartment(department: String) {
        routineDao.deleteSchedulesForDepartment(department)
        routineDao.deleteRoutineItemsForDepartment(department)
    }
}
