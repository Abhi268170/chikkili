package com.example.helloandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.helloandroid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Expense, 1 = Income
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val currentType = if (selectedTab == 0) TransactionType.EXPENSE else TransactionType.INCOME
    val filteredCategories = categories.filter { it.type == currentType }

    Scaffold(
        containerColor = FinanceTheme.colors.screenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text("Manage Categories", fontWeight = FontWeight.SemiBold, color = FinanceTheme.colors.darkNavy)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FinanceTheme.colors.darkNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinanceTheme.colors.screenBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = FinanceTheme.colors.darkNavy,
                contentColor = FinanceTheme.colors.onPrimaryButton
            ) {
                Icon(Icons.Default.Add, "Add Category")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = FinanceTheme.colors.tabActive,
                divider = { HorizontalDivider(color = FinanceTheme.colors.dividerColor) },
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
                    text = { Text("Expense", fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Income", fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal) }
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCategories) { category ->
                    CategoryItem(
                        category = category,
                        onEdit = { categoryToEdit = category },
                        onDelete = { categoryToDelete = category }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        CategoryDialog(
            type = currentType,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                viewModel.addCategory(name, "label", color, currentType) // Default icon "label" for now
                showAddDialog = false
            }
        )
    }

    if (categoryToEdit != null) {
        CategoryDialog(
            type = categoryToEdit!!.type,
            initialName = categoryToEdit!!.name,
            initialColor = categoryToEdit!!.colorHex,
            isEdit = true,
            onDismiss = { categoryToEdit = null },
            onConfirm = { name, color ->
                viewModel.updateCategory(categoryToEdit!!.copy(name = name, colorHex = color))
                categoryToEdit = null
            }
        )
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category?") },
            text = { Text("Transactions with this category will become 'Uncategorized'.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(categoryToDelete!!)
                    categoryToDelete = null
                }) {
                    Text("Delete", color = FinanceTheme.colors.expenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = FinanceTheme.colors.cardBg,
            titleContentColor = FinanceTheme.colors.darkNavy,
            textContentColor = FinanceTheme.colors.subtitleGray
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = try { Color(android.graphics.Color.parseColor(category.colorHex)) } catch (e: Exception) { FinanceTheme.colors.subtitleGray }
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FinanceTheme.colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = category.name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                color = FinanceTheme.colors.darkNavy
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", tint = FinanceTheme.colors.subtitleGray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = FinanceTheme.colors.expenseRed.copy(alpha = 0.7f))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDialog(
    type: TransactionType,
    initialName: String = "",
    initialColor: String = "#78909C",
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var colorHex by remember { mutableStateOf(initialColor) }
    
    // Simple color palette
    val colors = listOf(
        "#EF5350", "#EC407A", "#AB47BC", "#7E57C2", "#5C6BC0",
        "#42A5F5", "#29B6F6", "#26C6DA", "#26A69A", "#66BB6A",
        "#9CCC65", "#D4E157", "#FFEE58", "#FFCA28", "#FFA726",
        "#FF7043", "#8D6E63", "#BDBDBD", "#78909C"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FinanceTheme.colors.cardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    if (isEdit) "Edit Category" else "New ${type.name.lowercase().replaceFirstChar { it.uppercase() }} Category",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinanceTheme.colors.darkNavy
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FinanceTheme.colors.darkNavy,
                        cursorColor = FinanceTheme.colors.darkNavy,
                        focusedLabelColor = FinanceTheme.colors.darkNavy
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Color", fontSize = 14.sp, color = FinanceTheme.colors.subtitleGray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Color grid
                 FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { hex ->
                        val isSelected = colorHex == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { colorHex = hex }
                                .then(if (isSelected) Modifier.padding(4.dp) else Modifier) // visual check?? roughly
                        ) {
                             if (isSelected) {
                                 Box(
                                     modifier = Modifier
                                         .fillMaxSize()
                                         .background(Color.White.copy(alpha = 0.5f), CircleShape)
                                         .padding(8.dp)
                                         .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                 )
                             }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = FinanceTheme.colors.subtitleGray) }
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name, colorHex) },
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceTheme.colors.darkNavy)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
