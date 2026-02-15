package com.example.helloandroid

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO = Data Access Object
 *
 * This is like a menu of database operations.
 * You declare WHAT you want, and Room writes the actual SQL code for you.
 *
 * Notice these are interfaces (not classes) — Room auto-generates the
 * implementation at compile time using the KSP plugin.
 *
 * "Flow" means the data is LIVE — whenever the database changes,
 * the UI automatically gets the updated list. No manual refresh needed!
 */
@Dao
interface TransactionDao {

    /**
     * Get ALL transactions, sorted newest first.
     * Returns a Flow = live-updating stream of data.
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * Get transactions for a specific date.
     * :date is a parameter — Room fills it in from the function argument.
     */
    @Query("SELECT * FROM transactions WHERE date = :date ORDER BY date DESC")
    fun getTransactionsByDate(date: String): Flow<List<Transaction>>

    /**
     * Get transactions for a month (e.g., all of "2026-02" entries).
     * We use LIKE with a pattern: "2026-02%" matches all dates in Feb 2026.
     */
    @Query("SELECT * FROM transactions WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getTransactionsByMonth(monthPrefix: String): Flow<List<Transaction>>

    /**
     * Get total expenses for a specific date.
     * Returns null if no expenses found.
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE date = :dateString AND type = 'EXPENSE'")
    fun getDailyExpenseTotal(dateString: String): Flow<Double?>

    /**
     * Get total expenses for a specific date (Synchronous/One-shot).
     * Faster for widgets/notifications where we don't need a live stream.
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE date = :dateString AND type = 'EXPENSE'")
    suspend fun getDailyExpenseTotalRaw(dateString: String): Double?

    /**
     * Insert a new transaction.
     * OnConflictStrategy.REPLACE means: if a transaction with the same ID
     * already exists, replace it (useful for editing later).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    /**
     * Insert multiple transactions.
     * OnConflictStrategy.IGNORE means: if a transaction with the same ID
     * already exists, skip it (preserves existing data during import).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<Transaction>)

    /**
     * Delete a transaction by its ID.
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Delete all transactions (useful for "clear all data" feature).
     */
    /**
     * Delete all transactions (useful for "clear all data" feature).
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    /**
     * Unlink a category from transactions (set categoryId = null).
     * Used before deleting a category to preserve transaction history.
     */
    @Query("UPDATE transactions SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun unlinkCategory(categoryId: String)
}
