package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "card_swaps")
data class CardSwapRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val swapCode: String,
    val distributorId: Long,
    val distributorName: String,
    val fromCategoryId: Long,
    val fromCategoryName: String,
    val fromQuantity: Int,
    val fromUnitPrice: Double,
    val fromTotalValue: Double,
    val toCategoryId: Long,
    val toCategoryName: String,
    val toQuantity: Int,
    val toUnitPrice: Double,
    val toTotalValue: Double,
    val differenceAmount: Double, // toTotalValue - fromTotalValue (Positive: distributor pays extra; Negative: network owes distributor)
    val paidDifference: Double = 0.0,
    val remainingDifference: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val employeeName: String = "المدير",
    val notes: String = ""
)
