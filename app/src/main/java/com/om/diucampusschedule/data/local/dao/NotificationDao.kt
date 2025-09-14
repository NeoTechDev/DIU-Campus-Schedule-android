package com.om.diucampusschedule.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.om.diucampusschedule.data.local.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllNotifications(userId: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isFromAdmin = 1 ORDER BY timestamp DESC")
    fun getAdminNotifications(userId: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :typeString ORDER BY timestamp DESC")
    fun getNotificationsByType(userId: String, typeString: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0 AND isFromAdmin = 1")
    fun getUnreadAdminCount(userId: String): Flow<Int>
    
    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: String): NotificationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)
    
    @Update
    suspend fun updateNotification(notification: NotificationEntity)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId AND isFromAdmin = 1")
    suspend fun markAllAdminAsRead(userId: String)
    
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
    
    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: String)
    
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllUserNotifications(userId: String)
    
    @Query("DELETE FROM notifications WHERE timestamp < :cutoffTimeString")
    suspend fun deleteOldNotifications(cutoffTimeString: String)
}