package com.example.helloandroid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helloandroid.ui.theme.FinanceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategoryManager: () -> Unit,
    viewModel: FinanceViewModel? = null
) {
    val colors = FinanceTheme.colors

    Scaffold(
        containerColor = colors.screenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.SemiBold,
                        color = colors.darkNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.darkNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.screenBg,
                    titleContentColor = colors.darkNavy
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Dark Mode Toggle ─────────────────────────
            if (viewModel != null) {
                val themeMode by viewModel.themeMode.collectAsState()

                SettingsGroupLabel("APPEARANCE")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        // System default
                        ThemeModeRow(
                            label = "System default",
                            isSelected = themeMode == ThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                        )
                        HorizontalDivider(color = colors.dividerColor)
                        // Light
                        ThemeModeRow(
                            label = "Light",
                            isSelected = themeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                        )
                        HorizontalDivider(color = colors.dividerColor)
                        // Dark
                        ThemeModeRow(
                            label = "Dark",
                            isSelected = themeMode == ThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── General ──────────────────────────────────
            SettingsGroupLabel("GENERAL")

            SettingsItem(
                icon = Icons.Default.Settings,
                title = "Manage Categories",
                subtitle = "Add, edit, or delete categories",
                onClick = onNavigateToCategoryManager
            )

            // ── Backup ───────────────────────────────────
            if (viewModel != null) {
                val context = androidx.compose.ui.platform.LocalContext.current
                
                // Export Launcher
                val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")
                ) { uri ->
                    uri?.let {
                        val outputStream = context.contentResolver.openOutputStream(it)
                        viewModel.exportData(outputStream)
                        android.widget.Toast.makeText(context, "Data exported successfully", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                // Import Launcher
                val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
                ) { uri ->
                    uri?.let {
                        val inputStream = context.contentResolver.openInputStream(it)
                        viewModel.importData(inputStream)
                        android.widget.Toast.makeText(context, "Data imported successfully", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                SettingsGroupLabel("BACKUP")
                
                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = "Export Data",
                    subtitle = "Save all transactions to CSV",
                    onClick = { exportLauncher.launch("finance_backup_${System.currentTimeMillis()}.csv") }
                )
                
                SettingsItem(
                    icon = Icons.Default.Download,
                    title = "Import Data",
                    subtitle = "Restore transactions from CSV",
                    onClick = { importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) }
                )
            }
        }
    }
}

@Composable
private fun SettingsGroupLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = FinanceTheme.colors.summaryLabel,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ThemeModeRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DarkMode,
            contentDescription = null,
            tint = FinanceTheme.colors.darkNavy,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = FinanceTheme.colors.darkNavy,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = FinanceTheme.colors.darkNavy,
                unselectedColor = FinanceTheme.colors.subtitleGray
            )
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FinanceTheme.colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FinanceTheme.colors.darkNavy,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = FinanceTheme.colors.darkNavy,
                    fontSize = 16.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = FinanceTheme.colors.subtitleGray
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = FinanceTheme.colors.subtitleGray
            )
        }
    }
}
