package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FundType(val titleAr: String) {
    MAIN_TREASURY("صندوق البرنامج الرئيسي"),
    EMPLOYEE_FUND("صندوق الموظف المستقل")
}

@Entity(tableName = "cash_registers")
data class CashRegister(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: FundType,
    val employeeUsername: String? = null,
    val employeeDisplayName: String? = null,
    val currentBalance: Double = 0.0,
    val openingBalance: Double = 0.0,
    val totalInflows: Double = 0.0,
    val totalOutflows: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
