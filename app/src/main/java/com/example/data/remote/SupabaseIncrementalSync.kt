package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.db.AppDao
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Incremental, per-row sync using [updatedAt] timestamps and [deletedAt] tombstones.
 */
class SupabaseIncrementalSync(
    private val context: Context,
    private val appDao: AppDao,
    private val http: OkHttpClient,
    private val accessTokenProvider: () -> String,
    private val urlProvider: () -> String,
    private val anonKeyProvider: () -> String
) {
    private val prefs = context.getSharedPreferences("duo_space_auth_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private fun lastPullKey(coupleId: String) = "sync_last_pull_$coupleId"
    private fun lastPushKey(coupleId: String) = "sync_last_push_$coupleId"

    suspend fun pull(coupleId: String) = withContext(Dispatchers.IO) {
        val since = prefs.getLong(lastPullKey(coupleId), 0L)
        val filter = if (since > 0L) {
            "coupleId=eq.$coupleId&or=(updatedAt.gte.$since,deletedAt.gte.$since)"
        } else {
            "coupleId=eq.$coupleId"
        }
        pullTable("user_profiles", filter) { json ->
            decodeList<UserProfile>(json).forEach { applyProfile(it) }
        }
        pullTable("learning_roadmaps", filter) { json ->
            decodeList<LearningRoadmap>(json).forEach { applyRoadmap(it) }
        }
        pullTable("roadmap_lessons", filter) { json ->
            decodeList<RoadmapLesson>(json).forEach { applyLesson(it) }
        }
        pullTable("learning_tasks", filter) { json ->
            decodeList<LearningTask>(json).forEach { applyLearningTask(it) }
        }
        pullTable("expense_entries", filter) { json ->
            decodeList<ExpenseEntry>(json).forEach { applyExpense(it) }
        }
        pullTable("saving_tasks", filter) { json ->
            decodeList<SavingTask>(json).forEach { applySavingTask(it) }
        }
        pullTable("calendar_tasks", filter) { json ->
            decodeList<CalendarTask>(json).forEach { applyCalendarTask(it) }
        }
        prefs.edit().putLong(lastPullKey(coupleId), System.currentTimeMillis()).apply()
    }

    suspend fun push(coupleId: String) = withContext(Dispatchers.IO) {
        val since = prefs.getLong(lastPushKey(coupleId), 0L)
        val now = System.currentTimeMillis()

        val profiles = appDao.getProfilesForSync(coupleId, since)
        if (profiles.isNotEmpty()) upsert("user_profiles", profiles)

        val roadmaps = appDao.getRoadmapsForSync(coupleId, since)
        if (roadmaps.isNotEmpty()) upsert("learning_roadmaps", roadmaps)

        val lessons = appDao.getLessonsForSync(coupleId, since)
        if (lessons.isNotEmpty()) upsert("roadmap_lessons", lessons)

        val learningTasks = appDao.getLearningTasksForSync(coupleId, since)
        if (learningTasks.isNotEmpty()) upsert("learning_tasks", learningTasks)

        val expenses = appDao.getExpensesForSync(coupleId, since)
        if (expenses.isNotEmpty()) upsert("expense_entries", expenses)

        val savings = appDao.getSavingTasksForSync(coupleId, since)
        if (savings.isNotEmpty()) upsert("saving_tasks", savings)

        val calendar = appDao.getCalendarTasksForSync(coupleId, since)
        if (calendar.isNotEmpty()) upsert("calendar_tasks", calendar)

        prefs.edit().putLong(lastPushKey(coupleId), now).apply()
    }

    suspend fun stampCoupleIdLocally(coupleId: String) = withContext(Dispatchers.IO) {
        val touch = System.currentTimeMillis()
        appDao.getAllProfilesFlow().first().forEach {
            appDao.insertProfile(it.copy(coupleId = coupleId, updatedAt = touch))
        }
        appDao.getAllRoadmapsFlow().first().forEach {
            appDao.insertRoadmap(it.copy(coupleId = coupleId, updatedAt = touch))
        }
        appDao.getAllRoadmapsFlow().first().forEach { rm ->
            appDao.getLessonsForRoadmap(rm.id).forEach { lesson ->
                appDao.insertLesson(lesson.copy(coupleId = coupleId, updatedAt = touch))
            }
        }
        appDao.getAllLearningTasksFlow().first().forEach {
            appDao.insertLearningTask(it.copy(coupleId = coupleId, updatedAt = touch))
        }
        appDao.getAllExpensesFlow().first().forEach {
            appDao.insertExpense(it.copy(coupleId = coupleId, updatedAt = touch))
        }
        appDao.getAllSavingTasksFlow().first().forEach {
            appDao.insertSavingTask(it.copy(coupleId = coupleId, updatedAt = touch))
        }
        appDao.getAllCalendarTasksFlow().first().forEach {
            appDao.insertCalendarTask(it.copy(coupleId = coupleId, updatedAt = touch))
        }
    }

    private inline fun <reified T> decodeList(json: String): List<T> {
        if (json.isBlank() || json == "[]") return emptyList()
        val type = Types.newParameterizedType(List::class.java, T::class.java)
        val adapter = moshi.adapter<List<T>>(type)
        return adapter.fromJson(json) ?: emptyList()
    }

    private suspend fun pullTable(table: String, query: String, consumer: (String) -> Unit) {
        val json = get(table, query)
        if (json.isNotBlank()) consumer(json)
    }

    private fun get(table: String, query: String): String {
        val url = urlProvider().removeSuffix("/")
        val key = anonKeyProvider()
        val token = accessTokenProvider()
        require(token.isNotBlank()) { "Missing auth session" }
        val fullUrl = "$url/rest/v1/$table?select=*&$query"
        val request = Request.Builder()
            .url(fullUrl)
            .header("apikey", key)
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 404) return ""
                throw IOException("Pull $table failed (${response.code})")
            }
            return response.body?.string().orEmpty()
        }
    }

    private inline fun <reified T> upsert(table: String, rows: List<T>) {
        val url = urlProvider().removeSuffix("/")
        val key = anonKeyProvider()
        val token = accessTokenProvider()
        require(token.isNotBlank()) { "Missing auth session" }
        val type = Types.newParameterizedType(List::class.java, T::class.java)
        val json = moshi.adapter<List<T>>(type).toJson(rows)
        val request = Request.Builder()
            .url("$url/rest/v1/$table")
            .header("apikey", key)
            .header("Authorization", "Bearer $token")
            .header("Prefer", "resolution=merge-duplicates")
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val body = response.body?.string()
                Log.e("SupabaseSync", "Upsert $table failed ${response.code}: $body")
                throw IOException("Upsert $table failed (${response.code})")
            }
        }
    }

    private suspend fun applyProfile(remote: UserProfile) {
        if (remote.deletedAt != null) {
            // Profiles are couple-scoped; skip hard delete of slot ids locally.
            return
        }
        val local = appDao.getProfileById(remote.id)
        if (local == null || remote.updatedAt >= local.updatedAt) {
            appDao.insertProfile(remote.copy(deletedAt = null))
        }
    }

    private suspend fun applyRoadmap(remote: LearningRoadmap) {
        if (remote.deletedAt != null) {
            appDao.deleteRoadmapById(remote.id)
            return
        }
        appDao.insertRoadmap(remote)
    }

    private suspend fun applyLesson(remote: RoadmapLesson) {
        if (remote.deletedAt != null) {
            appDao.deleteLessonById(remote.id)
            return
        }
        appDao.insertLesson(remote)
    }

    private suspend fun applyLearningTask(remote: LearningTask) {
        if (remote.deletedAt != null) {
            appDao.deleteLearningTaskById(remote.id)
            return
        }
        appDao.insertLearningTask(remote)
    }

    private suspend fun applyExpense(remote: ExpenseEntry) {
        if (remote.deletedAt != null) {
            appDao.deleteExpenseById(remote.id)
            return
        }
        appDao.insertExpense(remote)
    }

    private suspend fun applySavingTask(remote: SavingTask) {
        if (remote.deletedAt != null) {
            appDao.deleteSavingTaskById(remote.id)
            return
        }
        appDao.insertSavingTask(remote)
    }

    private suspend fun applyCalendarTask(remote: CalendarTask) {
        if (remote.deletedAt != null) {
            appDao.deleteCalendarTaskById(remote.id)
            return
        }
        appDao.insertCalendarTask(remote)
    }
}
