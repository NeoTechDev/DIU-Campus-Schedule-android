package com.om.diucampusschedule.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.om.diucampusschedule.data.local.dao.UserDao
import com.om.diucampusschedule.data.local.dao.RoutineDao
import com.om.diucampusschedule.data.local.entities.UserEntity
import com.om.diucampusschedule.data.local.entities.RoutineEntity
import com.om.diucampusschedule.data.local.entities.RoutineScheduleEntity

@Database(
    entities = [
        UserEntity::class,
        RoutineEntity::class,
        RoutineScheduleEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun routineDao(): RoutineDao
    
    companion object {
        const val DATABASE_NAME = "diu_campus_schedule_db"
    }
}