package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employee_warehouse_stocks")
data class EmployeeStock(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val employeeName: String,
    val categoryId: Long,
    val categoryName: String,
    val quantity: Int = 0
)
