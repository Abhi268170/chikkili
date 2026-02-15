package com.example.helloandroid

import kotlinx.coroutines.flow.Flow

/**
 * Repository = the middle-man between ViewModel and Database.
 *
 * Why not talk to the DAO directly from the ViewModel?
 * → Clean separation. If you ever swap Room for a different database
 *   or add a network layer, only the Repository changes.
 *   The ViewModel stays untouched.
 *
 * This is a very thin wrapper right now, but it's good practice.
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) {

    /**
     * Live-updating stream of ALL transactions.
     * Whenever the database changes, this automatically emits the new list.
     */
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    /**
     * Insert a new transaction into the database.
     * "suspend" means this runs in a background thread (won't freeze the UI).
     */
    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    /**
     * Insert multiple transactions into the database.
     */
    suspend fun insertTransactions(transactions: List<Transaction>) {
        transactionDao.insertAll(transactions)
    }

    /**
     * Delete a transaction by its ID.
     */
    suspend fun deleteById(id: String) {
        transactionDao.deleteById(id)
    }

    // ── Categories ───────────────────────────────────────────
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun insertCategories(categories: List<Category>) {
        categoryDao.insertAll(categories)
    }

    // ── Budgets ──────────────────────────────────────────────
    fun getBudgetsForMonth(yearMonth: String): Flow<List<Budget>> {
        return budgetDao.getBudgetsForMonth(yearMonth)
    }

    suspend fun setBudget(budget: Budget) {
        budgetDao.setBudget(budget)
    }

    suspend fun setBudgets(budgets: List<Budget>) {
        budgetDao.setBudgets(budgets)
    }

    // ── Widget Support ───────────────────────────────────────
    fun getDailyExpenseTotal(dateString: String): Flow<Double?> {
        return transactionDao.getDailyExpenseTotal(dateString)
    }

    suspend fun getDailyExpenseTotalRaw(dateString: String): Double? {
        return transactionDao.getDailyExpenseTotalRaw(dateString)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        // Safety: Unlink transactions first!
        // We need a query in TransactionDao for this. 
        // Since we didn't add it yet, we should add it to TransactionDao first or do it via raw query?
        // Let's add it to TransactionDao properly.
        transactionDao.unlinkCategory(category.id)
        categoryDao.deleteCategory(category)
    }
}
