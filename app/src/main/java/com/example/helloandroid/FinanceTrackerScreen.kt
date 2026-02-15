package com.example.helloandroid

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helloandroid.ui.theme.*
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

// ── Currency formatter ───────────────────────────────────
private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
private fun Double.formatCurrency(): String {
    return currencyFormat.format(this)
}

// ══════════════════════════════════════════════════════════
// MAIN SCREEN
// ══════════════════════════════════════════════════════════

// ── View Modes ───────────────────────────
enum class TrackerViewMode {
    EXPENSES,
    BUDGETS
}

// ── Screen Navigation ────────────────────
enum class AppScreen {
    HOME,
    SETTINGS,
    CATEGORY_MANAGER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceTrackerScreen(viewModel: FinanceViewModel) {

    // Navigation State
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreenContent(
                viewModel = viewModel,
                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
            )
        }
        AppScreen.SETTINGS -> {
            SettingsScreen(
                onNavigateBack = { currentScreen = AppScreen.HOME },
                onNavigateToCategoryManager = { currentScreen = AppScreen.CATEGORY_MANAGER },
                viewModel = viewModel
            )
        }
        AppScreen.CATEGORY_MANAGER -> {
            CategoryManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = AppScreen.SETTINGS }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    viewModel: FinanceViewModel,
    onNavigateToSettings: () -> Unit
) {
    // Main view state
    var currentViewMode by remember { mutableStateOf(TrackerViewMode.EXPENSES) }
    
    // Expenses view state
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val transactions by viewModel.transactions.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    Scaffold(
        containerColor = FinanceTheme.colors.screenBg,

        // ── Full-width Bottom Dock ───────────────────────
        bottomBar = {
            BottomDock(
                currentMode = currentViewMode,
                onModeSelected = { currentViewMode = it }
            )
        },

        // ── Circular FAB (only show in Expenses mode) ─────
        floatingActionButton = {
            if (currentViewMode == TrackerViewMode.EXPENSES) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    shape = CircleShape,
                    containerColor = FinanceTheme.colors.darkNavy,
                    contentColor = FinanceTheme.colors.onPrimaryButton,
                    modifier = Modifier
                        .size(56.dp)
                        .offset(y = (-8).dp)
                ) {
                    Icon(Icons.Default.Add, "Add", modifier = Modifier.size(24.dp))
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top Bar with Title and Settings ──────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = FinanceTheme.colors.darkNavy)) {
                            append("Finance ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Light, color = FinanceTheme.colors.titleLight)) {
                            append("Tracker")
                        }
                    },
                    fontSize = 28.sp
                )
                
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = FinanceTheme.colors.subtitleGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ── Switch content based on View Mode ─────────
            when (currentViewMode) {
                TrackerViewMode.EXPENSES -> {
                    // ── Tab Row: [Daily] [Monthly] ────────────────
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = FinanceTheme.colors.tabActive,
                        divider = {
                            HorizontalDivider(color = FinanceTheme.colors.dividerColor)
                        },
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    height = 2.5.dp,
                                    color = FinanceTheme.colors.tabActive
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "Daily",
                                    fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedTab == 0) FinanceTheme.colors.tabActive else FinanceTheme.colors.tabInactive
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "Monthly",
                                    fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedTab == 1) FinanceTheme.colors.tabActive else FinanceTheme.colors.tabInactive
                                )
                            }
                        )
                    }

                    // ── Content below tabs ────────────────────────
                    val categories by viewModel.categories.collectAsState()
                    
                    when (selectedTab) {
                        0 -> DailyView(
                            viewModel = viewModel,
                            date = selectedDate,
                            transactions = transactions,
                            categories = categories
                        )
                        1 -> MonthlyView(
                            viewModel = viewModel,
                            month = selectedMonth,
                            transactions = transactions,
                            categories = categories
                        )
                    }
                }
                TrackerViewMode.BUDGETS -> {
                    BudgetScreen(viewModel = viewModel)
                }
            }
        }
    }

    // ── Add Transaction Dialog ─────────────────────────────
    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, amount, type, categoryId ->
                viewModel.addTransaction(title, amount, type, description, categoryId = categoryId)
                showAddDialog = false
            },
            viewModel = viewModel
        )
    }
}

// ══════════════════════════════════════════════════════════
// BOTTOM DOCK – Full-width flat bar with icons + labels
// ══════════════════════════════════════════════════════════

@Composable
fun BottomDock(
    currentMode: TrackerViewMode,
    onModeSelected: (TrackerViewMode) -> Unit
) {
    Surface(
        color = FinanceTheme.colors.cardBg,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                label = "HOME",
                iconResId = R.drawable.ic_dock_home,
                isSelected = currentMode == TrackerViewMode.EXPENSES,
                onClick = { onModeSelected(TrackerViewMode.EXPENSES) }
            )
            DockItem(
                label = "BUDGET",
                iconResId = R.drawable.ic_dock_budget,
                isSelected = currentMode == TrackerViewMode.BUDGETS,
                onClick = { onModeSelected(TrackerViewMode.BUDGETS) }
            )
        }
    }
}

@Composable
fun DockItem(
    label: String,
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) FinanceTheme.colors.darkNavy else FinanceTheme.colors.dockTextInactive

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = tint,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun BudgetScreen(viewModel: FinanceViewModel) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val budgets by viewModel.currentMonthBudgets.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()

    var showSetBudgetDialog by remember { mutableStateOf<Category?>(null) }
    
    val spentByCategory = remember(allTransactions, selectedMonth) {
        allTransactions
            .filter { YearMonth.from(it.date) == selectedMonth && it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId ?: "uncategorized" }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
    }

    val budgetByCategory = remember(budgets) {
        budgets.associate { it.categoryId to it.amount }
    }
    
    val currentMonthTotalBudget = budgets.sumOf { it.amount }
    val currentMonthTotalSpent = spentByCategory.values.sum()
    val currentMonthRemaining = currentMonthTotalBudget - currentMonthTotalSpent

    val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        item {
            DateNavigationBar(
                label = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                sublabel = "BUDGET PLANNING",
                onPrevious = { viewModel.goToPreviousMonth() },
                onNext = { viewModel.goToNextMonth() }
            )
        }
        
        // ── Budget Summary Card ───────────────────────────
        item {
             BudgetSummaryCard(
                 totalBudget = currentMonthTotalBudget,
                 totalSpent = currentMonthTotalSpent,
                 remaining = currentMonthRemaining
             )
        }

        item {
             SectionHeader("CATEGORIES")
        }

        items(expenseCategories) { category ->
            val budgetAmount = budgetByCategory[category.id] ?: 0.0
            val spentAmount = spentByCategory[category.id] ?: 0.0
            
            BudgetCategoryRow(
                category = category,
                budget = budgetAmount,
                spent = spentAmount,
                onClick = { showSetBudgetDialog = category }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        item {
             val uncategorizedSpent = spentByCategory["uncategorized"] ?: 0.0
             if (uncategorizedSpent > 0) {
                 BudgetCategoryRow(
                    category = Category("uncategorized", "Uncategorized", "help_center", "#9E9E9E", TransactionType.EXPENSE),
                    budget = 0.0,
                    spent = uncategorizedSpent,
                    onClick = { }
                )
             }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    
    if (showSetBudgetDialog != null) {
        SetBudgetDialog(
            category = showSetBudgetDialog!!,
            currentBudget = budgetByCategory[showSetBudgetDialog!!.id] ?: 0.0,
            onDismiss = { showSetBudgetDialog = null },
            onConfirm = { amount ->
                viewModel.setCategoryBudget(showSetBudgetDialog!!.id, amount)
                showSetBudgetDialog = null
            }
        )
    }
}

@Composable
fun BudgetSummaryCard(
    totalBudget: Double,
    totalSpent: Double,
    remaining: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FinanceTheme.colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TOTAL BUDGET
            SummaryItem(
                label = "TOTAL\nBUDGET",
                amount = totalBudget,
                amountColor = FinanceTheme.colors.summaryValue,
                amountSize = 20.sp,
                prefix = "₹"
            )
            VerticalDivider(modifier = Modifier.height(50.dp))
            // SPENT
            SummaryItem(
                label = "SPENT",
                amount = totalSpent,
                amountColor = FinanceTheme.colors.expenseRed,
                amountSize = 20.sp,
                prefix = "₹"
            )
            VerticalDivider(modifier = Modifier.height(50.dp))
            // REMAINING
            SummaryItem(
                label = "REMAINING",
                amount = remaining,
                amountColor = if(remaining >= 0) FinanceTheme.colors.incomeGreen else FinanceTheme.colors.expenseRed,
                amountSize = 20.sp,
                prefix = "₹"
            )
        }
    }
}


@Composable
fun BudgetCategoryRow(
    category: Category,
    budget: Double,
    spent: Double,
    onClick: () -> Unit
) {
    val progress = if (budget > 0) (spent / budget).toFloat() else 0f
    val progressColor = when {
        spent > budget && budget > 0 -> FinanceTheme.colors.expenseRed
        progress > 0.8f -> Color(0xFFFFB300)
        else -> FinanceTheme.colors.incomeGreen
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FinanceTheme.colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(category.colorHex)).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (category.id == "uncategorized") Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(android.graphics.Color.parseColor(category.colorHex)),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.SemiBold,
                        color = FinanceTheme.colors.darkNavy, 
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (budget > 0) 
                                "${((spent/budget)*100).toInt()}% of ${currencyFormat.format(budget)}" 
                               else "No Budget Set",
                        fontSize = 13.sp,
                        color = FinanceTheme.colors.subtitleGray,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = currencyFormat.format(spent),
                    fontWeight = FontWeight.Bold,
                    color = if(spent > budget && budget > 0) FinanceTheme.colors.expenseRed else FinanceTheme.colors.summaryValue,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp) 
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = FinanceTheme.colors.dividerColor,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun SetBudgetDialog(
    category: Category,
    currentBudget: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "") }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FinanceTheme.colors.cardBg,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // ── Header ──────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Split title: "Set" bold + "Budget" light
                    Row {
                        Text(
                            text = "Set ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinanceTheme.colors.darkNavy
                        )
                        Text(
                            text = "Budget",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            color = FinanceTheme.colors.titleLight
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = FinanceTheme.colors.subtitleGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.name.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinanceTheme.colors.summaryLabel,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // ── Monthly Limit Input ─────────────────────
                Text(
                    text = "MONTHLY LIMIT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinanceTheme.colors.summaryLabel,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("0.00", color = FinanceTheme.colors.subtitleGray.copy(alpha = 0.4f)) },
                    leadingIcon = {
                        Text(
                            "₹",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinanceTheme.colors.darkNavy
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = FinanceTheme.colors.dividerColor,
                        focusedBorderColor = FinanceTheme.colors.darkNavy,
                        cursorColor = FinanceTheme.colors.darkNavy
                    )
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // ── Save Button ─────────────────────────────
                Button(
                    onClick = {
                        val value = amount.toDoubleOrNull()
                        if (value != null) onConfirm(value)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FinanceTheme.colors.darkNavy
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Save Budget",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FinanceTheme.colors.onPrimaryButton
                    )
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════
// DAILY VIEW
// ══════════════════════════════════════════════════════════

@Composable
fun DailyView(
    viewModel: FinanceViewModel,
    date: LocalDate,
    transactions: List<Transaction>,
    categories: List<Category>
) {
    val today = LocalDate.now()
    val dateLabel = when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        today.plusDays(1) -> "Tomorrow"
        else -> date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }

    val dailyTransactions = transactions.filter { it.date == date }
    val dailyIncome = dailyTransactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }
    val dailyExpense = dailyTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }
    val dailyTotal = dailyIncome - dailyExpense

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // ── Date navigation ───────────────────────────────
        item {
            DateNavigationBar(
                label = dateLabel,
                sublabel = date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy")).uppercase(),
                onPrevious = { viewModel.goToPreviousDay() },
                onNext = { viewModel.goToNextDay() }
            )
        }

        // ── Summary card ──────────────────────────────────
        item {
            SummaryCard(
                total = dailyTotal,
                income = dailyIncome,
                expense = dailyExpense
            )
        }

        // ── Transaction list ──────────────────────────────
        if (dailyTransactions.isEmpty()) {
            item {
                EmptyState(message = "No transactions for $dateLabel")
            }
        } else {
            item {
                SectionHeader("TRANSACTIONS")
            }

            item {
                TransactionListCard(
                    transactions = dailyTransactions,
                    categories = categories,
                    onDelete = { id -> viewModel.deleteTransaction(id) }
                )
            }
        }
    }
}


// ══════════════════════════════════════════════════════════
// MONTHLY VIEW
// ══════════════════════════════════════════════════════════

@Composable
fun MonthlyView(
    viewModel: FinanceViewModel,
    month: YearMonth,
    transactions: List<Transaction>,
    categories: List<Category>
) {
    val monthlyTransactions = transactions.filter { YearMonth.from(it.date) == month }
    val monthlyIncome = monthlyTransactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }
    val monthlyExpense = monthlyTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }
    val monthlyTotal = monthlyIncome - monthlyExpense
    val grouped = monthlyTransactions
        .sortedByDescending { it.date }
        .groupBy { it.date }
        .toSortedMap(compareByDescending { it })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        item {
            DateNavigationBar(
                label = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                sublabel = null,
                onPrevious = { viewModel.goToPreviousMonth() },
                onNext = { viewModel.goToNextMonth() }
            )
        }

        item {
            SummaryCard(
                total = monthlyTotal,
                income = monthlyIncome,
                expense = monthlyExpense
            )
        }

        if (grouped.isEmpty()) {
            item {
                EmptyState(message = "No transactions in ${month.format(DateTimeFormatter.ofPattern("MMMM"))}")
            }
        } else {
            grouped.forEach { (groupDate, txList) ->
                item(key = "header-$groupDate") {
                    SectionHeader(
                        text = formatDateGroupHeader(groupDate)
                    )
                }

                item(key = "card-$groupDate") {
                    TransactionListCard(
                        transactions = txList,
                        categories = categories,
                        onDelete = { id -> viewModel.deleteTransaction(id) }
                    )
                }

                item(key = "spacer-$groupDate") {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/** Format date for monthly group headers */
private fun formatDateGroupHeader(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "TODAY"
        today.minusDays(1) -> "YESTERDAY"
        else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM dd")).uppercase()
    }
}


// ══════════════════════════════════════════════════════════
// SHARED COMPONENTS
// ══════════════════════════════════════════════════════════

/**
 * ← [label] → navigation bar wrapped in a pill-shaped card
 */
@Composable
fun DateNavigationBar(
    label: String,
    sublabel: String?,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FinanceTheme.colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = FinanceTheme.colors.subtitleGray
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize()
            ) {
                Text(
                    text = label,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinanceTheme.colors.dateLabelText
                )
                if (sublabel != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sublabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = FinanceTheme.colors.dateSublabel,
                        letterSpacing = 1.sp
                    )
                }
            }
            IconButton(onClick = onNext) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = FinanceTheme.colors.subtitleGray
                )
            }
        }
    }
}

/**
 * 3-column summary of total balance, income, and expenses.
 */
@Composable
fun SummaryCard(
    total: Double,
    income: Double,
    expense: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FinanceTheme.colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TOTAL BALANCE
            SummaryItem(
                label = "TOTAL\nBALANCE",
                amount = total,
                amountColor = FinanceTheme.colors.summaryValue,
                amountSize = 24.sp,
                prefix = "₹",
                prefixColor = FinanceTheme.colors.summaryValue
            )
            VerticalDivider(modifier = Modifier.height(50.dp))
            // INCOME
            SummaryItem(
                label = "INCOME",
                amount = income,
                amountColor = FinanceTheme.colors.incomeGreen,
                amountSize = 20.sp,
                prefix = "₹",
                prefixColor = FinanceTheme.colors.incomeGreen
            )
            VerticalDivider(modifier = Modifier.height(50.dp))
            // EXPENSE
            SummaryItem(
                label = "EXPENSE",
                amount = expense,
                amountColor = FinanceTheme.colors.expenseRed,
                amountSize = 20.sp,
                prefix = "₹",
                prefixColor = FinanceTheme.colors.expenseRed
            )
        }
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(color = FinanceTheme.colors.dividerColor.copy(alpha = 0.6f))
    )
}

@Composable
fun SummaryItem(
    label: String,
    amount: Double,
    amountColor: Color,
    amountSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    prefix: String = "",
    prefixColor: Color = amountColor
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = FinanceTheme.colors.summaryLabel,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
            lineHeight = 16.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = prefixColor, fontWeight = FontWeight.Bold, fontSize = amountSize)) {
                    append(prefix)
                }
                withStyle(SpanStyle(color = amountColor, fontWeight = FontWeight.Bold, fontSize = amountSize)) {
                    val formatted = String.format(Locale("en", "IN"), "%,.2f", amount)
                    append(formatted)
                }
            },
            textAlign = TextAlign.Center
        )
    }
}


/**
 * Header for a section, e.g. "TRANSACTIONS"
 */
@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = FinanceTheme.colors.sectionLabelColor,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 4.dp),
        letterSpacing = 0.5.sp
    )
}

/**
 * A card containing a list of transactions.
 */
@Composable
fun TransactionListCard(
    transactions: List<Transaction>,
    categories: List<Category>,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FinanceTheme.colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            transactions.forEachIndexed { index, transaction ->
                TransactionRow(
                    transaction = transaction,
                    categories = categories,
                    onDelete = { onDelete(transaction.id) }
                )
                if (index < transactions.size - 1) {
                    HorizontalDivider(color = FinanceTheme.colors.dividerColor, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
                }
            }
        }
    }
}


/**
 * A single transaction row showing icon, title, amount, etc.
 */
@Composable
fun TransactionRow(
    transaction: Transaction,
    categories: List<Category>,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val category = categories.find { it.id == transaction.categoryId }
    
    val amountColor = if (isIncome) FinanceTheme.colors.incomeGreen else FinanceTheme.colors.expenseRed
    val iconColor = if (category != null) Color(android.graphics.Color.parseColor(category.colorHex)) else amountColor
    val iconBgColor = iconColor.copy(alpha = 0.15f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (category != null) Icons.Default.Add else Icons.Default.Edit,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        // Title and optional description
        Column(Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontWeight = FontWeight.Medium,
                color = FinanceTheme.colors.darkNavy
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (category != null) {
                    Text(
                        text = category.name,
                        fontSize = 12.sp,
                        color = iconColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                
                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        fontSize = 13.sp,
                        color = FinanceTheme.colors.subtitleGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // Amount and delete button
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (isIncome) "+ " else "- ") + currencyFormat.format(transaction.amount),
                color = amountColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(
                    Icons.Default.Delete,
                    "Delete",
                    tint = FinanceTheme.colors.subtitleGray.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


/**
 * A dialog for adding a new income or expense transaction.
 */
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, TransactionType, String?) -> Unit,
    viewModel: FinanceViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    
    val categories by viewModel.categories.collectAsState()
    
    val currentCategories = remember(categories, type) {
        categories.filter { it.type == type }
    }
    
    LaunchedEffect(type, currentCategories) {
        if (selectedCategoryId != null && categories.find { it.id == selectedCategoryId }?.type != type) {
            selectedCategoryId = null
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FinanceTheme.colors.cardBg,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Header ──────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        Text(
                            text = "Add ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinanceTheme.colors.darkNavy
                        )
                        Text(
                            text = "Transaction",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            color = FinanceTheme.colors.titleLight
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = FinanceTheme.colors.subtitleGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // ── Income/Expense Toggle ────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Income Button
                    OutlinedButton(
                        onClick = { type = TransactionType.INCOME },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (type == TransactionType.INCOME)
                                FinanceTheme.colors.incomeIconBg
                            else
                                Color.Transparent,
                            contentColor = if (type == TransactionType.INCOME)
                                FinanceTheme.colors.incomeGreen
                            else
                                FinanceTheme.colors.subtitleGray
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (type == TransactionType.INCOME) 1.5.dp else 1.dp,
                            color = if (type == TransactionType.INCOME)
                                FinanceTheme.colors.incomeGreen
                            else
                                FinanceTheme.colors.dividerColor
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "Income",
                            fontWeight = if (type == TransactionType.INCOME) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                    // Expense Button
                    OutlinedButton(
                        onClick = { type = TransactionType.EXPENSE },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (type == TransactionType.EXPENSE)
                                FinanceTheme.colors.expenseIconBg
                            else
                                Color.Transparent,
                            contentColor = if (type == TransactionType.EXPENSE)
                                FinanceTheme.colors.expenseRed
                            else
                                FinanceTheme.colors.subtitleGray
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (type == TransactionType.EXPENSE) 1.5.dp else 1.dp,
                            color = if (type == TransactionType.EXPENSE)
                                FinanceTheme.colors.expenseRed
                            else
                                FinanceTheme.colors.dividerColor
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "Expense",
                            fontWeight = if (type == TransactionType.EXPENSE) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ── Category Selector ────────────────────────
                Text(
                    text = "CATEGORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinanceTheme.colors.summaryLabel,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentCategories) { category ->
                         val isSelected = selectedCategoryId == category.id
                         val chipColor = try {
                             Color(android.graphics.Color.parseColor(category.colorHex))
                         } catch (e: Exception) {
                             FinanceTheme.colors.subtitleGray
                         }
                         FilterChip(
                             selected = isSelected,
                             onClick = { selectedCategoryId = category.id },
                             label = {
                                 Text(
                                     category.name,
                                     fontSize = 13.sp,
                                     fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                     color = if (isSelected) FinanceTheme.colors.darkNavy else FinanceTheme.colors.dateLabelText
                                 )
                             },
                             leadingIcon = {
                                 Box(
                                     modifier = Modifier
                                         .size(8.dp)
                                         .background(
                                             color = chipColor,
                                             shape = RoundedCornerShape(4.dp)
                                         )
                                 )
                             },
                             colors = FilterChipDefaults.filterChipColors(
                                 selectedContainerColor = FinanceTheme.colors.dockBgSelected,
                                 containerColor = FinanceTheme.colors.screenBg
                             ),
                             border = FilterChipDefaults.filterChipBorder(
                                 enabled = true,
                                 selected = isSelected,
                                 borderColor = FinanceTheme.colors.dividerColor,
                                 selectedBorderColor = FinanceTheme.colors.dockTextSelected.copy(alpha = 0.3f)
                             ),
                             shape = RoundedCornerShape(12.dp)
                         )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // ── Amount field ─────────────────────────────
                Text(
                    text = "AMOUNT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinanceTheme.colors.summaryLabel,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = { Text("0.00", color = FinanceTheme.colors.subtitleGray.copy(alpha = 0.4f)) },
                    leadingIcon = {
                        Text(
                            "₹",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinanceTheme.colors.darkNavy
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = FinanceTheme.colors.dividerColor,
                        focusedBorderColor = FinanceTheme.colors.darkNavy,
                        cursorColor = FinanceTheme.colors.darkNavy
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ── Title field ──────────────────────────────
                Text(
                    text = "TITLE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinanceTheme.colors.summaryLabel,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Groceries", color = FinanceTheme.colors.subtitleGray.copy(alpha = 0.4f)) },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = FinanceTheme.colors.subtitleGray.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = FinanceTheme.colors.dividerColor,
                        focusedBorderColor = FinanceTheme.colors.darkNavy,
                        cursorColor = FinanceTheme.colors.darkNavy
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ── Description field (optional) ────────────
                Text(
                    text = "DESCRIPTION (OPTIONAL)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinanceTheme.colors.summaryLabel,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Add details...", color = FinanceTheme.colors.subtitleGray.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = FinanceTheme.colors.dividerColor,
                        focusedBorderColor = FinanceTheme.colors.darkNavy,
                        cursorColor = FinanceTheme.colors.darkNavy
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // ── Save button ─────────────────────────────
                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull()
                        if (title.isNotBlank() && amountValue != null) {
                            onConfirm(title, description, amountValue, type, selectedCategoryId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FinanceTheme.colors.darkNavy
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Save Transaction",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FinanceTheme.colors.onPrimaryButton
                    )
                }
            }
        }
    }
}

/**
 * Empty state with icon + message
 */
@Composable
fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_empty_state),
            contentDescription = "No transactions",
            tint = FinanceTheme.colors.emptyStateIcon,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = FinanceTheme.colors.emptyStateText,
            textAlign = TextAlign.Center
        )
    }
}
