package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String, // "user" (Minnyo) or "girlfriend" (Sweetheart)
    val name: String,
    val avatarEmoji: String,
    val imageUri: String? = null,
    val remoteImagePath: String? = null,
    val dailyBudget: Double = 50.0,
    val monthlySavingGoal: Double = 500.0
)

@Entity(tableName = "learning_roadmaps")
data class LearningRoadmap(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: String, // "user" or "girlfriend"
    val title: String,
    val description: String,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "roadmap_lessons")
data class RoadmapLesson(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roadmapId: Int,
    val title: String,
    val description: String,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val isCompleted: Boolean = false,
    val orderIndex: Int
)

@Entity(tableName = "learning_tasks")
data class LearningTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: String, // "user" or "girlfriend"
    val title: String,
    val dateString: String, // "YYYY-MM-DD" e.g., "2026-05-21"
    val isCompleted: Boolean = false,
    val minutesSpent: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expense_entries")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: String, // "user" or "girlfriend"
    val amount: Double,
    val isIncome: Boolean, // true = Income, false = Expense
    val category: String, // "Food", "Transport", "Shopping", "Entertainment", "Utilities", "Income", "Salary", "Other"
    val dateString: String, // "YYYY-MM-DD"
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saving_tasks")
data class SavingTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: String, // "user" or "girlfriend" or "shared"
    val title: String,
    val rewardAmount: Double, // Est. money saved
    val dateString: String, // "YYYY-MM-DD"
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_tasks")
data class CalendarTask(
    @PrimaryKey val id: String,
    val title: String,
    val time: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
