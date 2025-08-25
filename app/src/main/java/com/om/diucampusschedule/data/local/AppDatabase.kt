package com.om.diucampusschedule.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.om.diucampusschedule.data.local.dao.UserDao
import com.om.diucampusschedule.data.local.entities.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    
    companion object {
        const val DATABASE_NAME = "diu_campus_schedule_db"
    }
}