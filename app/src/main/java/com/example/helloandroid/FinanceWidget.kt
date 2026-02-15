package com.example.helloandroid

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class FinanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch data
        val repository = (context.applicationContext as HelloApplication).repository
        val today = LocalDate.now().toString()
        val totalExpense = repository.getDailyExpenseTotalRaw(today) ?: 0.0
        android.util.Log.d("FinanceWidget", "provideGlance: today=$today, totalExpense=$totalExpense")

        provideContent {
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
                .clickable(actionStartActivity<QuickAddActivity>()) // Tap to quick-add
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Expense",
                        style = TextStyle(
                            color = titleColor,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = "₹ ${String.format("%.2f", totalExpense)}",
                        style = TextStyle(
                            color = amountColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                // Push the button to the end
                Spacer(GlanceModifier.defaultWeight())

                // Quick Add Button
                Box(
                    modifier = GlanceModifier
                        .size(40.dp)
                        .background(ImageProvider(R.drawable.ic_dock_home)) // Placeholder
                        .clickable(actionStartActivity<QuickAddActivity>()),
                     contentAlignment = Alignment.Center
                ) {
                     Image(
                         provider = ImageProvider(R.drawable.ic_empty_state),
                         contentDescription = "Add",
                         modifier = GlanceModifier.size(24.dp)
                     )
                }
            }
        }
    }

    companion object {
        suspend fun updateAll(context: Context) {
            android.util.Log.d("FinanceWidget", "updateAll called")
            // Small delay to ensure DB write is committed and visible to raw query
            kotlinx.coroutines.delay(500) 
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(FinanceWidget::class.java)
            android.util.Log.d("FinanceWidget", "Found ${glanceIds.size} glanceIds to update")
            glanceIds.forEach { glanceId ->
                android.util.Log.d("FinanceWidget", "Updating glanceId: $glanceId")
                FinanceWidget().update(context, glanceId)
            }
        }
    }
}

class FinanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FinanceWidget()
}
