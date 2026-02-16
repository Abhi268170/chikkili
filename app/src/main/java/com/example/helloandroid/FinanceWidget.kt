package com.example.helloandroid

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class FinanceWidget : GlanceAppWidget() {
    
    // 1. Tell Glance to use Preferences for state
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // 2. Read directly from the state, NOT the database
            val prefs = currentState<Preferences>()
            val totalExpense = prefs[expensePreferenceKey] ?: 0.0
            
            WidgetContent(totalExpense)
        }
    }

    @Composable
    private fun WidgetContent(totalExpense: Double) {
        // Use Android color resources for automatic day/night support
        val bgColor = ColorProvider(R.color.widget_bg)
        val titleColor = ColorProvider(R.color.widget_title)
        val amountColor = ColorProvider(R.color.widget_expense)

        // Outer container
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor)
                .clickable(actionStartActivity<QuickAddActivity>()) // Tap anywhere to quick-add
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Expense",
                    style = TextStyle(
                        color = titleColor,
                        fontSize = 14.sp // Slightly larger title
                    )
                )
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    text = "₹ ${String.format("%.2f", totalExpense)}",
                    style = TextStyle(
                        color = amountColor,
                        fontSize = 26.sp, // Larger expense text
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }

    companion object {
        val expensePreferenceKey = doublePreferencesKey("today_expense")

        // 3. Create a unified Push updater
        suspend fun pushUpdate(context: Context, newTotal: Double) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(FinanceWidget::class.java)
            
            glanceIds.forEach { glanceId ->
                // Write the new exact total into the widget's internal state
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[expensePreferenceKey] = newTotal
                    }
                }
                // Tell the widget to redraw using the new state
                FinanceWidget().update(context, glanceId)
            }
        }
    }
}

class FinanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FinanceWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        
        // Fetch initial data when widget is added or updated by system
        // This ensures the widget doesn't stay at 0 until the next transaction
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
             try {
                 val repository = (context.applicationContext as HelloApplication).repository
                 val today = java.time.LocalDate.now().toString()
                 val total = repository.getDailyExpenseTotalRaw(today) ?: 0.0
                 FinanceWidget.pushUpdate(context, total)
             } catch (e: Exception) {
                 android.util.Log.e("FinanceWidget", "Error fetching initial data", e)
             }
        }
    }
}
