package com.example.helloandroid

import android.app.Application

class HelloApplication : Application() {
    // Database instance
    val database by lazy { AppDatabase.getDatabase(this) }

    // Repository instance (single source of truth for data)
    val repository by lazy { 
        TransactionRepository(
            database.transactionDao(),
            database.categoryDao(),
            database.budgetDao()
        ) 
    }
}
