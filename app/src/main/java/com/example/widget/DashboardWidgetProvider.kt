package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.widget.RemoteViews
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, DashboardWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.widget.ACTION_REFRESH"

        fun triggerUpdate(context: Context) {
            val intent = Intent(context, DashboardWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_dashboard)

            // Setup Lovedays Count from shared preferences
            val sharedPrefs = context.getSharedPreferences("duo_space_auth_prefs", Context.MODE_PRIVATE)
            val loveStartDate = sharedPrefs.getString("love_start_date", "") ?: ""
            var daysCountStr = "--"
            if (loveStartDate.isNotBlank()) {
                try {
                    val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val startDate = inputSdf.parse(loveStartDate)
                    if (startDate != null) {
                        val diffMs = Date().time - startDate.time
                        val totalDays = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                        daysCountStr = totalDays.toString()
                    }
                } catch (e: java.lang.Exception) {
                    Log.e("DashboardWidget", "Error parsing love start date", e)
                }
            }
            views.setTextViewText(R.id.widget_lovedays_count, daysCountStr)

            // Playfulness: click on panel to launch MainActivity
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingMainIntent = PendingIntent.getActivity(
                context, 
                0, 
                mainIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_footer_tap, pendingMainIntent)
            views.setOnClickPendingIntent(R.id.widget_lovedays_panel, pendingMainIntent)
            views.setOnClickPendingIntent(R.id.widget_expenses_panel, pendingMainIntent)

            // Setup Refresh Broadcast Intent
            val refreshIntent = Intent(context, DashboardWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val pendingRefreshIntent = PendingIntent.getBroadcast(
                context, 
                1, 
                refreshIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, pendingRefreshIntent)

            // Load expenses total asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val expenses = db.appDao().getAllExpensesList()
                    val currentMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
                    val monthExpenses = expenses.filter { !it.isIncome && it.dateString.startsWith(currentMonthPrefix) }
                    
                    val totalSum = if (monthExpenses.isNotEmpty()) {
                        monthExpenses.sumOf { it.amount }
                    } else {
                        expenses.filter { !it.isIncome }.sumOf { it.amount }
                    }

                    views.setTextViewText(R.id.widget_expenses_sum, "$${"%.1f".format(totalSum)}")
                    
                    val titleText = if (monthExpenses.isNotEmpty()) "This Month Spent" else "Total Spent"
                    views.setTextViewText(R.id.widget_expenses_label, titleText)
                    
                } catch (e: Exception) {
                    Log.e("DashboardWidget", "Database fetch failure", e)
                    views.setTextViewText(R.id.widget_expenses_sum, "$0.0")
                } finally {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
