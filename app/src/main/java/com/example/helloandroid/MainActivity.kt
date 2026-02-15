package com.example.helloandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.helloandroid.ui.theme.HelloAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get the Repository from the Application class
        val repository = (application as HelloApplication).repository

        // SharedPreferences for persisting theme preference
        val prefs = getSharedPreferences("finance_prefs", MODE_PRIVATE)

        // Create the Factory that knows how to build our ViewModel
        val factory = FinanceViewModelFactory(application as HelloApplication, repository, prefs)

        setContent {
            // Create the ViewModel using the factory
            val viewModel: FinanceViewModel = viewModel(factory = factory)

            // Observe theme mode
            val themeMode by viewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
            }

            HelloAndroidTheme(darkTheme = isDark) {
                FinanceTrackerScreen(viewModel = viewModel)
            }
        }
    }
}