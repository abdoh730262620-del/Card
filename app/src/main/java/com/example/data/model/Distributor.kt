package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "distributors")
data class Distributor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0, // Positive: owes debt (مدين), Negative: credit in favor (دائن)
    val creditLimit: Double = 50000.0, // حد الديون المسموح به
    val paymentPeriodDays: Int = 14, // فترة التسديد المسموحة بالأيام
    val discountPercent: Double = 0.0, // نسبة الخصم المعتمدة للموزع (%)
    val lastPaymentDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    val isCreditLimitExceeded: Boolean
        get() = currentBalance > creditLimit

    val isCreditLimitNear: Boolean
        get() = currentBalance >= creditLimit * 0.85 && !isCreditLimitExceeded

    fun isPaymentOverdue(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (currentBalance <= 0) return false
        val graceMs = paymentPeriodDays.toLong() * 24 * 60 * 60 * 1000L
        return (currentTime - lastPaymentDate) > graceMs
    }

    fun daysSinceLastPayment(currentTime: Long = System.currentTimeMillis()): Int {
        val diff = currentTime - lastPaymentDate
        return (diff / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
    }
}
