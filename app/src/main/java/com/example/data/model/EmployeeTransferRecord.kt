package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employee_transfers")
data class EmployeeTransferRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transferCode: String,
    val fromEmployee: String,
    val toEmployee: String,
    val transferType: String, // "CARDS" (عهدة كروت), "CASH" (عهدة نقدية)
    val categoryId: Long? = null,
    val categoryName: String = "",
    val quantity: Int = 0,
    val totalFaceValue: Double = 0.0,
    val cashAmount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED", // COMPLETED, PENDING
    val notes: String = ""
)
