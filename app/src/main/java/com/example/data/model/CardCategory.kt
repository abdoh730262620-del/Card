package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "card_categories")
data class CardCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val faceValue: Double,
    val costPrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Int = 0,
    val validityHours: Int = 24,
    val quotaMb: Int = 1024,
    val colorHex: String = "#1E88E5",
    val notes: String = ""
)
