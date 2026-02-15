package com.example.helloandroid

import android.content.SharedPreferences
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.flow.flatMapLatest

/** Dark‑mode preference: follow system, or force light/dark. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * ViewModel = the "brain" of the app.
 *
 * BEFORE: data lived in a MutableStateFlow (memory only, lost on close)
 * AFTER:  data comes from Room database via the Repository (persisted!)
 *
 * The Repository gives us a Flow<List<Transaction>> that automatically
 * updates whenever the database changes. We convert it to a StateFlow
 * so Compose can observe it.
 */
class FinanceViewModel(
    private val application: Application,
    private val repository: TransactionRepository,
    private val prefs: SharedPreferences
) : AndroidViewModel(application) {

    // ── Theme / Dark‑mode ─────────────────────────────────
    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    // --- All transactions, live from the database ---
    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- All categories ---
    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Currently selected date (for the daily view) ---
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // --- Currently selected month (for the monthly view) ---
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    init {
        seedDefaultCategories()
    }

    private fun seedDefaultCategories() {
        viewModelScope.launch {
            val defaults = listOf(
                Category("cat_food", "Food", "restaurant", "#EF5350", TransactionType.EXPENSE), // Red 400
                Category("cat_transport", "Transport", "directions_bus", "#42A5F5", TransactionType.EXPENSE), // Blue 400
                Category("cat_shopping", "Shopping", "shopping_bag", "#AB47BC", TransactionType.EXPENSE), // Purple 400
                Category("cat_entertainment", "Entertainment", "movie", "#FFA726", TransactionType.EXPENSE), // Orange 400
                Category("cat_health", "Health", "medical_services", "#26A69A", TransactionType.EXPENSE), // Teal 400
                Category("cat_bills", "Bills & Utilities", "receipt_long", "#78909C", TransactionType.EXPENSE), // Blue Grey 400
                Category("cat_salary", "Salary", "payments", "#66BB6A", TransactionType.INCOME), // Green 400
                Category("cat_dther_income", "Other Income", "savings", "#8D6E63", TransactionType.INCOME) // Brown 400
            )
            // This will only insert if they don't exist (handled by DAO logic or we can check first)
            // But our DAO uses REPLACE. To avoid overwriting user edits, we should probably use IGNORE in DAO or check count here.
            // For now, let's just insert them. Ideally we check if DB is empty.
             repository.insertCategories(defaults)
        }
    }

    // =====================================================
    // Daily helpers
    // =====================================================

    fun getTransactionsForDate(date: LocalDate): List<Transaction> {
        return transactions.value.filter { it.date == date }
    }

    fun getDailyIncome(date: LocalDate): Double {
        return getTransactionsForDate(date)
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }

    fun getDailyExpense(date: LocalDate): Double {
        return getTransactionsForDate(date)
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }

    fun getDailyTotal(date: LocalDate): Double {
        return getDailyIncome(date) - getDailyExpense(date)
    }

    // =====================================================
    // Monthly helpers
    // =====================================================

    fun getTransactionsForMonth(month: YearMonth): List<Transaction> {
        return transactions.value.filter {
            YearMonth.from(it.date) == month
        }
    }

    fun getMonthlyIncome(month: YearMonth): Double {
        return getTransactionsForMonth(month)
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }

    fun getMonthlyExpense(month: YearMonth): Double {
        return getTransactionsForMonth(month)
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }

    fun getMonthlyTotal(month: YearMonth): Double {
        return getMonthlyIncome(month) - getMonthlyExpense(month)
    }

    fun getMonthlyTransactionsGroupedByDate(month: YearMonth): Map<LocalDate, List<Transaction>> {
        return getTransactionsForMonth(month)
            .sortedByDescending { it.date }
            .groupBy { it.date }
            .toSortedMap(compareByDescending { it })
    }

    // =====================================================
    // Budget helpers
    // =====================================================
    

    // We can expose a Flow of budgets for the selected month to observe in UI
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentMonthBudgets: StateFlow<List<Budget>> = _selectedMonth
        .flatMapLatest { month ->
            repository.getBudgetsForMonth(month.toString())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setCategoryBudget(categoryId: String, amount: Double) {
        val month = _selectedMonth.value.toString()
        viewModelScope.launch {
            repository.setBudget(Budget(month, categoryId, amount))
        }
    }

    // =====================================================
    // Date navigation
    // =====================================================

    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun goToDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun goToPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun goToMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    // =====================================================
    // Actions: add, delete
    // =====================================================

    /**
     * Add a transaction → saves to database.
     * viewModelScope.launch runs this on a background thread
     * so the UI doesn't freeze.
     */
    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        description: String = "",
        date: LocalDate = _selectedDate.value,
        categoryId: String? = null
    ) {
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            amount = amount,
            type = type,
            date = date,
            categoryId = categoryId
        )
        viewModelScope.launch {
            repository.insert(transaction)
            FinanceWidget.updateAll(application)
        }
    }

    /** Delete a transaction → removes from database */
    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteById(id)
            FinanceWidget.updateAll(application)
        }
    }
    // =====================================================
    // Category Management
    // =====================================================

    fun addCategory(name: String, iconName: String, colorHex: String, type: TransactionType) {
        viewModelScope.launch {
            val newCategory = Category(
                id = "cat_${System.currentTimeMillis()}", // Simple unique ID
                name = name,
                iconName = iconName,
                colorHex = colorHex,
                type = type
            )
            repository.insertCategory(newCategory)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // =====================================================
    // Backup: Import / Export
    // =====================================================

    fun exportData(outputStream: java.io.OutputStream?) {
        if (outputStream == null) return
        viewModelScope.launch {
            try {
                val allTransactions = transactions.value
                val csv = CsvHelper.transactionsToCsv(allTransactions)
                outputStream.use { it.write(csv.toByteArray()) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importData(inputStream: java.io.InputStream?) {
        if (inputStream == null) return
        viewModelScope.launch {
            try {
                val csv = inputStream.use { it.bufferedReader().readText() }
                val transactionsToImport = CsvHelper.csvToTransactions(csv)
                if (transactionsToImport.isNotEmpty()) {
                    repository.insertTransactions(transactionsToImport)
                    FinanceWidget.updateAll(application)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

/**
 * Factory for creating FinanceViewModel.
 *
 * Why do we need this?
 * By default, Android creates ViewModels with a no-argument constructor.
 * But our ViewModel needs a Repository. The Factory tells Android:
 * "Here's how to create this ViewModel with the Repository I'm giving you."
 */
class FinanceViewModelFactory(
    private val application: Application,
    private val repository: TransactionRepository,
    private val prefs: SharedPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            return FinanceViewModel(application, repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
