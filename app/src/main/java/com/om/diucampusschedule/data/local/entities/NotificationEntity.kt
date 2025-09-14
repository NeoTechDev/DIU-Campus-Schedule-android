package com.om.diucampusschedule.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.om.diucampusschedule.domain.model.NotificationType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "notifications")
@TypeConverters(NotificationConverters::class)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false,
    val actionRoute: String? = null,
    val department: String? = null,
    val imageUrl: String? = null,
    val isFromAdmin: Boolean = false,
    val userId: String // To associate notifications with specific users
)

class NotificationConverters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime): String {
        return value.format(formatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime {
        return LocalDateTime.parse(value, formatter)
    }
    
    @TypeConverter
    fun fromNotificationType(type: NotificationType): String {
        return type.name
    }
    
    @TypeConverter
    fun toNotificationType(type: String): NotificationType {
        return NotificationType.valueOf(type)
    }
}