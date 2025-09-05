package com.om.diucampusschedule.di

import android.content.Context
import androidx.room.Room
import com.om.diucampusschedule.data.local.AppDatabase
import com.om.diucampusschedule.data.local.dao.RoutineDao
import com.om.diucampusschedule.data.local.dao.UserDao
import com.om.diucampusschedule.data.local.dao.TaskDao
import com.om.diucampusschedule.data.local.dao.TaskGroupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // Only for development
        .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideRoutineDao(database: AppDatabase): RoutineDao {
        return database.routineDao()
    }

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideTaskGroupDao(database: AppDatabase): TaskGroupDao {
        return database.taskGroupDao()
    }
}