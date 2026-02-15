package com.example.helloandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import com.example.helloandroid.ui.theme.FinanceTheme
import com.example.helloandroid.ui.theme.HelloAndroidTheme

class QuickAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read persisted theme preference
        val prefs = getSharedPreferences("finance_prefs", MODE_PRIVATE)
        val themeModeStr = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (_: Exception) { ThemeMode.SYSTEM }
        
        setContent {
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
            }

            HelloAndroidTheme(darkTheme = isDark) {
                val colors = FinanceTheme.colors

                var title by remember { mutableStateOf("") }
                var amount by remember { mutableStateOf("") }
                var type by remember { mutableStateOf(TransactionType.EXPENSE) }

                Dialog(onDismissRequest = { finish() }) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.cardBg,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth()
                        ) {
                            // ── Header ──────────────────────────────
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row {
                                    Text(
                                        text = "Quick ",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.darkNavy
                                    )
                                    Text(
                                        text = "Add",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Light,
                                        color = colors.titleLight
                                    )
                                }
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = colors.subtitleGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // ── Income/Expense Toggle ────────────────
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
                                            colors.incomeGreen.copy(alpha = 0.1f)
                                        else
                                            Color.Transparent,
                                        contentColor = if (type == TransactionType.INCOME)
                                            colors.incomeGreen
                                        else
                                            colors.subtitleGray
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (type == TransactionType.INCOME) 1.5.dp else 1.dp,
                                        color = if (type == TransactionType.INCOME)
                                            colors.incomeGreen
                                        else
                                            colors.dividerColor
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
                                            colors.expenseRed.copy(alpha = 0.1f)
                                        else
                                            Color.Transparent,
                                        contentColor = if (type == TransactionType.EXPENSE)
                                            colors.expenseRed
                                        else
                                            colors.subtitleGray
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (type == TransactionType.EXPENSE) 1.5.dp else 1.dp,
                                        color = if (type == TransactionType.EXPENSE)
                                            colors.expenseRed
                                        else
                                            colors.dividerColor
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
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // ── Amount field ─────────────────────────
                            Text(
                                text = "AMOUNT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.summaryLabel,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                placeholder = { Text("0.00", color = colors.subtitleGray.copy(alpha = 0.4f)) },
                                leadingIcon = {
                                    Text(
                                        "₹",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.darkNavy
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = colors.dividerColor,
                                    focusedBorderColor = colors.darkNavy,
                                    cursorColor = colors.darkNavy
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // ── Title field ──────────────────────────
                            Text(
                                text = "TITLE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.summaryLabel,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = { Text("e.g. Groceries", color = colors.subtitleGray.copy(alpha = 0.4f)) },
                                leadingIcon = { 
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = colors.subtitleGray.copy(alpha = 0.5f)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = colors.dividerColor,
                                    focusedBorderColor = colors.darkNavy,
                                    cursorColor = colors.darkNavy
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // ── Save button ─────────────────────────
                            Button(
                                onClick = {
                                    val amt = amount.toDoubleOrNull() ?: 0.0
                                    if (title.isNotBlank() && amt > 0) {
                                        saveTransaction(title, amt, type)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.darkNavy
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    "Save Transaction",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onPrimaryButton
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveTransaction(title: String, amount: Double, type: TransactionType) {
        val database = AppDatabase.getDatabase(this)
        val dao = database.transactionDao()
        
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    amount = amount,
                    type = type
                )
            )
            // Update widget immediately
            FinanceWidget.updateAll(applicationContext)
            finish()
        }
    }
}
