package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MovementDirection(val titleAr: String) {
    INFLOW("إيداع / قبض"),
    OUTFLOW("صرف / تسليم")
}

@Entity(tableName = "fund_movements")
data class FundMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val registerId: Long,
    val registerName: String,
    val isEmployeeFund: Boolean, // True: independent employee fund, False: main app treasury
    val employeeUsername: String? = null,
    val employeeDisplayName: String? = null,
    val direction: MovementDirection,
    val amount: Double,
    val balanceAfter: Double,
    val title: String,
    val referenceType: String = "MANUAL", // "SALE", "PAYMENT_IN", "PAYMENT_OUT", "TRANSFER_TO_MAIN", "MANUAL"
    val referenceNumber: String = "",
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
