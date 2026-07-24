package com.example.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.CalendarTask
import com.example.data.model.ExpenseEntry
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportUtil {

    fun exportExpensesToCsv(context: Context, expenses: List<ExpenseEntry>) {
        val fileName = "expenses_export_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        try {
            val writer = FileWriter(file)
            writer.append("ID,Owner,Amount,Type,Category,Note,Date\n")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            
            for (expense in expenses) {
                val dateStr = sdf.format(Date(expense.timestamp))
                val typeStr = if (expense.isIncome) "Income" else "Expense"
                writer.append("${expense.id},${expense.ownerId},${expense.amount},$typeStr,${expense.category.replace(",", "")},${expense.note.replace(",", " ")},$dateStr\n")
            }
            writer.flush()
            writer.close()
            
            shareFile(context, file, "Export Expenses")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportCalendarTasksToCsv(context: Context, tasks: List<CalendarTask>) {
        val fileName = "calendar_tasks_export_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        try {
            val writer = FileWriter(file)
            writer.append("ID,Title,Time,Status,Date Added\n")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            
            for (task in tasks) {
                val dateStr = sdf.format(Date(task.timestamp))
                val status = if (task.isCompleted) "Completed" else "Pending"
                writer.append("${task.id},${task.title.replace(",", " ")},${task.time.replace(",", " ")},$status,$dateStr\n")
            }
            writer.flush()
            writer.close()
            
            shareFile(context, file, "Export Calendar Tasks")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareFile(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
