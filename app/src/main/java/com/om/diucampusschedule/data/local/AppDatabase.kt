package com.om.diucampusschedule.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.om.diucampusschedule.data.local.dao.UserDao
import com.om.diucampusschedule.data.local.dao.RoutineDao
import com.om.diucampusschedule.data.local.dao.NoteDao
import com.om.diucampusschedule.data.local.entities.UserEntity
import com.om.diucampusschedule.data.local.entities.RoutineEntity
import com.om.diucampusschedule.data.local.entities.RoutineScheduleEntity
import com.om.diucampusschedule.data.local.entities.NoteEntity

@Database(
    entities = [
        UserEntity::class,
        RoutineEntity::class,
        RoutineScheduleEntity::class,
        NoteEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun routineDao(): RoutineDao
    abstract fun noteDao(): NoteDao
    
    companion object {
        const val DATABASE_NAME = "diu_campus_schedule_db"
    }
}