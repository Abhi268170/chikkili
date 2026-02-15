package com.example.helloandroid

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String,
    val name: String,
    val iconName: String, // e.g., "shopping_cart", "restaurant"
    val colorHex: String, // e.g., "#FF5722"
    val type: TransactionType // INCOME or EXPENSE
)
