package com.example.data.repository

import com.example.data.db.AppDao
import com.example.data.model.UserProfile
import com.example.data.model.LearningRoadmap
import com.example.data.model.RoadmapLesson
import com.example.data.model.LearningTask
import com.example.data.model.ExpenseEntry
import com.example.data.model.SavingTask
import com.example.data.model.CalendarTask
import com.example.data.model.PartnerLocationRecord
import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // --- User Profiles ---
    val allProfilesFlow: Flow<List<UserProfile>> = appDao.getAllProfilesFlow()

    suspend fun getProfileById(id: String): UserProfile? = appDao.getProfileById(id)

    suspend fun insertProfile(profile: UserProfile) = appDao.insertProfile(profile)

    // --- Learning Roadmaps ---
    val allRoadmapsFlow: Flow<List<LearningRoadmap>> = appDao.getAllRoadmapsFlow()

    fun getRoadmapsByOwnerFlow(ownerId: String): Flow<List<LearningRoadmap>> =
        appDao.getRoadmapsByOwnerFlow(ownerId)

    suspend fun insertRoadmap(roadmap: LearningRoadmap): Long = appDao.insertRoadmap(roadmap)

    suspend fun deleteRoadmapById(id: Int) = appDao.deleteRoadmapById(id)

    // --- Roadmap Lessons ---
    fun getLessonsForRoadmapFlow(roadmapId: Int): Flow<List<RoadmapLesson>> =
        appDao.getLessonsForRoadmapFlow(roadmapId)

    suspend fun getLessonsForRoadmap(roadmapId: Int): List<RoadmapLesson> =
        appDao.getLessonsForRoadmap(roadmapId)

    suspend fun insertLesson(lesson: RoadmapLesson) = appDao.insertLesson(lesson)

    suspend fun updateLesson(lesson: RoadmapLesson) = appDao.updateLesson(lesson)

    suspend fun deleteLessonById(id: Int) = appDao.deleteLessonById(id)

    // --- Learning Tasks ---
    val allLearningTasksFlow: Flow<List<LearningTask>> = appDao.getAllLearningTasksFlow()

    fun getLearningTasksByOwnerFlow(ownerId: String): Flow<List<LearningTask>> =
        appDao.getLearningTasksByOwnerFlow(ownerId)

    fun getLearningTasksByDateFlow(dateString: String): Flow<List<LearningTask>> =
        appDao.getLearningTasksByDateFlow(dateString)

    suspend fun insertLearningTask(task: LearningTask) = appDao.insertLearningTask(task)

    suspend fun updateLearningTask(task: LearningTask) = appDao.updateLearningTask(task)

    suspend fun deleteLearningTaskById(id: Int) = appDao.deleteLearningTaskById(id)

    // --- Expense Entries ---
    val allExpensesFlow: Flow<List<ExpenseEntry>> = appDao.getAllExpensesFlow()

    fun getExpensesByOwnerFlow(ownerId: String): Flow<List<ExpenseEntry>> =
        appDao.getExpensesByOwnerFlow(ownerId)

    suspend fun insertExpense(expense: ExpenseEntry) = appDao.insertExpense(expense)

    suspend fun deleteExpenseById(id: Int) = appDao.deleteExpenseById(id)

    suspend fun tombstoneExpense(id: Int) {
        val now = System.currentTimeMillis()
        appDao.tombstoneExpense(id, now, now)
    }

    suspend fun tombstoneLearningTask(id: Int) {
        val now = System.currentTimeMillis()
        appDao.tombstoneLearningTask(id, now, now)
    }

    suspend fun tombstoneSavingTask(id: Int) {
        val now = System.currentTimeMillis()
        appDao.tombstoneSavingTask(id, now, now)
    }

    suspend fun tombstoneCalendarTask(id: String) {
        val now = System.currentTimeMillis()
        appDao.tombstoneCalendarTask(id, now, now)
    }

    suspend fun tombstoneRoadmap(id: Int) {
        val now = System.currentTimeMillis()
        appDao.tombstoneRoadmap(id, now, now)
    }

    // --- Saving Tasks ---
    val allSavingTasksFlow: Flow<List<SavingTask>> = appDao.getAllSavingTasksFlow()

    suspend fun insertSavingTask(task: SavingTask) = appDao.insertSavingTask(task)

    suspend fun updateSavingTask(task: SavingTask) = appDao.updateSavingTask(task)

    suspend fun deleteSavingTaskById(id: Int) = appDao.deleteSavingTaskById(id)

    // --- Calendar Tasks ---
    val allCalendarTasksFlow: Flow<List<CalendarTask>> = appDao.getAllCalendarTasksFlow()

    suspend fun insertCalendarTask(task: CalendarTask) = appDao.insertCalendarTask(task)

    suspend fun updateCalendarTask(task: CalendarTask) = appDao.updateCalendarTask(task)

    suspend fun deleteCalendarTaskById(id: String) = appDao.deleteCalendarTaskById(id)

    val partnerLocationsFlow: Flow<List<PartnerLocationRecord>> = appDao.getAllPartnerLocationsFlow()

    suspend fun insertPartnerLocation(record: PartnerLocationRecord) = appDao.insertPartnerLocation(record)

    suspend fun clearPartnerLocations() = appDao.clearPartnerLocations()
}
