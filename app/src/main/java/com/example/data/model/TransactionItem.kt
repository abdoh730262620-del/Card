package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionRecord::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId")]
)
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val categoryId: Long,
    val categoryName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalAmount: Double
)

data class InvoiceItemDraft(
    val categoryId: Long,
    val categoryName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalAmount: Double = quantity * unitPrice
)
