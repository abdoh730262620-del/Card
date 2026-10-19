package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NotificationType {
    CREDIT_LIMIT,    // تنبيه تجاوز الحد الائتماني
    DUE_PAYMENT,     // تنبيه استحقاق دفعة للموزع
    LOW_STOCK,       // تنبيه نقص كمية كروت
    MANAGER_MESSAGE, // إشعار وتوجيه من المدير للموظفين
    TRANSFER,        // تحويل عهدة مخازن
    SWAP,            // استبدال فئات كروت
    GENERAL          // عام
}

@Entity(tableName = "system_notifications")
data class SystemNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    val targetEmployee: String = "ALL", // "ALL" or specific username
    val senderName: String = "المدير العام",
    val date: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionUrlOrId: String? = null
)
