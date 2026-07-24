package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.UserProfile
import com.example.data.model.LearningRoadmap
import com.example.data.model.RoadmapLesson
import com.example.data.model.LearningTask
import com.example.data.model.ExpenseEntry
import com.example.data.model.SavingTask
import com.example.data.model.CalendarTask
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- User Profiles ---
    @Query("SELECT * FROM user_profiles")
    fun getAllProfilesFlow(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getProfileById(id: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    // --- Learning Roadmaps ---
    @Query("SELECT * FROM learning_roadmaps ORDER BY createdTimestamp DESC")
    fun getAllRoadmapsFlow(): Flow<List<LearningRoadmap>>

    @Query("SELECT * FROM learning_roadmaps WHERE ownerId = :ownerId ORDER BY createdTimestamp DESC")
    fun getRoadmapsByOwnerFlow(ownerId: String): Flow<List<LearningRoadmap>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmap(roadmap: LearningRoadmap): Long

    @Query("DELETE FROM learning_roadmaps WHERE id = :id")
    suspend fun deleteRoadmapById(id: Int)

    // --- Roadmap Lessons ---
    @Query("SELECT * FROM roadmap_lessons WHERE roadmapId = :roadmapId ORDER BY orderIndex ASC")
    fun getLessonsForRoadmapFlow(roadmapId: Int): Flow<List<RoadmapLesson>>

    @Query("SELECT * FROM roadmap_lessons WHERE roadmapId = :roadmapId ORDER BY orderIndex ASC")
    suspend fun getLessonsForRoadmap(roadmapId: Int): List<RoadmapLesson>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: RoadmapLesson)

    @Update
    suspend fun updateLesson(lesson: RoadmapLesson)

    @Query("DELETE FROM roadmap_lessons WHERE id = :id")
    suspend fun deleteLessonById(id: Int)

    // --- Learning Tasks ---
    @Query("SELECT * FROM learning_tasks ORDER BY timestamp DESC")
    fun getAllLearningTasksFlow(): Flow<List<LearningTask>>

    @Query("SELECT * FROM learning_tasks WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getLearningTasksByOwnerFlow(ownerId: String): Flow<List<LearningTask>>

    @Query("SELECT * FROM learning_tasks WHERE dateString = :dateString")
    fun getLearningTasksByDateFlow(dateString: String): Flow<List<LearningTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningTask(task: LearningTask)

    @Update
    suspend fun updateLearningTask(task: LearningTask)

    @Query("DELETE FROM learning_tasks WHERE id = :id")
    suspend fun deleteLearningTaskById(id: Int)

    // --- Expense Entries ---
    @Query("SELECT * FROM expense_entries ORDER BY timestamp DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseEntry>>

    @Query("SELECT * FROM expense_entries WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getExpensesByOwnerFlow(ownerId: String): Flow<List<ExpenseEntry>>

    @Query("SELECT * FROM expense_entries ORDER BY timestamp DESC")
    suspend fun getAllExpensesList(): List<ExpenseEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntry)

    @Query("DELETE FROM expense_entries WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)

    // --- Saving Tasks ---
    @Query("SELECT * FROM saving_tasks ORDER BY timestamp DESC")
    fun getAllSavingTasksFlow(): Flow<List<SavingTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingTask(task: SavingTask)

    @Update
    suspend fun updateSavingTask(task: SavingTask)

    @Query("DELETE FROM saving_tasks WHERE id = :id")
    suspend fun deleteSavingTaskById(id: Int)

    // --- Calendar Tasks ---
    @Query("SELECT * FROM calendar_tasks ORDER BY timestamp DESC")
    fun getAllCalendarTasksFlow(): Flow<List<CalendarTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarTask(task: CalendarTask)

    @Update
    suspend fun updateCalendarTask(task: CalendarTask)

    @Query("DELETE FROM calendar_tasks WHERE id = :id")
    suspend fun deleteCalendarTaskById(id: String)

    @Query("DELETE FROM user_profiles")
    suspend fun clearUserProfiles()

    @Query("DELETE FROM learning_roadmaps")
    suspend fun clearLearningRoadmaps()

    @Query("DELETE FROM roadmap_lessons")
    suspend fun clearRoadmapLessons()

    @Query("DELETE FROM learning_tasks")
    suspend fun clearLearningTasks()

    @Query("DELETE FROM expense_entries")
    suspend fun clearExpenseEntries()

    @Query("DELETE FROM saving_tasks")
    suspend fun clearSavingTasks()

    @Query("DELETE FROM calendar_tasks")
    suspend fun clearCalendarTasks()
}

@Database(
    entities = [
        UserProfile::class,
        LearningRoadmap::class,
        RoadmapLesson::class,
        LearningTask::class,
        ExpenseEntry::class,
        SavingTask::class,
        CalendarTask::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.inMemoryDatabaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
