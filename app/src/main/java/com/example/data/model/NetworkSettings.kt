package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_settings")
data class NetworkSettings(
    @PrimaryKey
    val id: Int = 1,
    val networkName: String = "شبكة الواي فاي الذكية",
    val managerName: String = "المدير العام",
    val managerPhone: String = "777000000",
    val currency: String = "ريال",
    val requirePinOnLaunch: Boolean = false,
    val biometricEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val lowStockThreshold: Int = 20,
    val language: String = "ar",
    val isDarkMode: Boolean = false
)
