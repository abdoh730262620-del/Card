package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    SALE,            // بيع كروت
    PURCHASE,        // توريد/شراء كروت للمخزن
    RETURN,          // مرتجع كروت عام
    PURCHASE_RETURN, // مرتجع مشتريات (إرجاع كروت للمورد واسترداد المبلغ للصندوق)
    SALE_RETURN,     // مرتجع مبيعات (إرجاع كروت من العميل/الموزع وتنزيل الحساب/الصندوق)
    PAYMENT_IN,      // سند قبض (تسديد من الموزع)
    PAYMENT_OUT,     // سند صرف / مصروفات
    DISCOUNT,        // خصم لحساب الموزع
    SWAP             // استبدال فئات كروت
}

@Entity(tableName = "transactions")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val type: TransactionType,
    val distributorId: Long? = null,
    val distributorName: String = "",
    val categoryId: Long? = null,
    val categoryName: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentMethod: String = "CASH", // CASH, CREDIT (آجل), TRANSFER (حوالة)
    val date: Long = System.currentTimeMillis(),
    val employeeName: String = "المدير",
    val notes: String = ""
)
