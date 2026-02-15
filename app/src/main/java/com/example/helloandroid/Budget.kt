package com.example.helloandroid

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "budgets",
    primaryKeys = ["yearMonth", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class Budget(
    val yearMonth: String, // Format: "YYYY-MM"
    val categoryId: String,
    val amount: Double
)
