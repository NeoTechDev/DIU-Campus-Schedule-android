package com.om.diucampusschedule.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.om.diucampusschedule.data.local.dao.UserDao
import com.om.diucampusschedule.data.local.dao.RoutineDao
import com.om.diucampusschedule.data.local.dao.NoteDao
import com.om.diucampusschedule.data.local.dao.TaskDao
import com.om.diucampusschedule.data.local.dao.TaskGroupDao
import com.om.diucampusschedule.data.local.entities.UserEntity
import com.om.diucampusschedule.data.local.entities.RoutineEntity
import com.om.diucampusschedule.data.local.entities.RoutineScheduleEntity
import com.om.diucampusschedule.data.local.entities.NoteEntity
import com.om.diucampusschedule.data.local.entities.TaskEntity
import com.om.diucampusschedule.data.local.entities.TaskGroupEntity

@Database(
    entities = [
        UserEntity::class,
        RoutineEntity::class,
        RoutineScheduleEntity::class,
        NoteEntity::class,
        TaskEntity::class,
        TaskGroupEntity::class
    ],
    version = 7, // Incremented version for entity structure changes
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun routineDao(): RoutineDao
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun taskGroupDao(): TaskGroupDao
    
    companion object {
        const val DATABASE_NAME = "diu_campus_schedule_db"
    }
}