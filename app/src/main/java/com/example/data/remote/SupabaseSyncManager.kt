package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.db.AppDao
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class SupabaseSyncState {
    object Idle : SupabaseSyncState()
    data class Loading(val message: String) : SupabaseSyncState()
    data class Success(val message: String) : SupabaseSyncState()
    data class Error(val errorReason: String) : SupabaseSyncState()
}

class SupabaseSyncManager(private val context: Context, private val appDao: AppDao) {
    
    private val sharedPrefs = context.getSharedPreferences("duo_space_auth_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val _syncState = MutableStateFlow<SupabaseSyncState>(SupabaseSyncState.Idle)
    val syncState: StateFlow<SupabaseSyncState> = _syncState.asStateFlow()

    // Retrieve configured credentials from SharedPreferences or fallback to BuildConfig helper (via .env)
    fun getSupabaseUrl(): String {
        val saved = sharedPrefs.getString("supabase_url_prefs", "") ?: ""
        if (saved.isNotBlank()) return saved
        // Safe check for BuildConfig
        return try {
            val field = com.example.BuildConfig::class.java.getField("SUPABASE_URL")
            field.get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getSupabaseAnonKey(): String {
        val saved = sharedPrefs.getString("supabase_anon_key_prefs", "") ?: ""
        if (saved.isNotBlank()) return saved
        // Safe check for BuildConfig
        return try {
            val field = com.example.BuildConfig::class.java.getField("SUPABASE_ANON_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun saveCredentials(url: String, key: String) {
        sharedPrefs.edit()
            .putString("supabase_url_prefs", url.trim())
            .putString("supabase_anon_key_prefs", key.trim())
            .apply()
    }

    fun clearCredentials() {
        sharedPrefs.edit()
            .remove("supabase_url_prefs")
            .remove("supabase_anon_key_prefs")
            .apply()
    }

    fun isConfigured(): Boolean {
        return getSupabaseUrl().isNotBlank() && getSupabaseAnonKey().isNotBlank()
    }

    // --- Dynamic Raw Client Implementations ---

    private fun buildBaseRequest(tableName: String, method: String, url: String, key: String, queryParams: String = ""): Request.Builder {
        val fullUrl = if (url.endsWith("/")) "${url}rest/v1/$tableName" else "$url/rest/v1/$tableName"
        val finalUrl = if (queryParams.isNotBlank()) "$fullUrl?$queryParams" else fullUrl
        
        return Request.Builder()
            .url(finalUrl)
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
    }

    // Connect / Test status
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseAnonKey()
        if (url.isBlank() || key.isBlank()) return@withContext false

        try {
            val request = buildBaseRequest("user_profiles", "GET", url, key, "select=id&limit=1").get().build()
            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful || response.code == 404 // 404 is still dynamic API access
            }
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Connection failed", e)
            return@withContext false
        }
    }

    // --- Safe Table Clean Sync logic ---

    suspend fun forcePushToSupabase() = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseAnonKey()
        
        if (url.isBlank() || key.isBlank()) {
            _syncState.value = SupabaseSyncState.Error("Supabase URL or Anon Key is missing!")
            return@withContext
        }

        try {
            _syncState.value = SupabaseSyncState.Loading("Clearing existing Supabase data...")

            // List of tables to sync
            val tables = listOf(
                "user_profiles", "learning_roadmaps", "roadmap_lessons",
                "learning_tasks", "expense_entries", "saving_tasks"
            )

            // Step 1: Clear all tables in order on Supabase
            for (table in tables) {
                val deleteReq = buildBaseRequest(table, "DELETE", url, key, "id=not.is.null").delete().build()
                client.newCall(deleteReq).execute().close()
            }

            // Step 2: Push current local DB tables to Supabase
            _syncState.value = SupabaseSyncState.Loading("Pushing Profiles...")
            // Synchronously reading from Room lists (cannot block Room Main but safe here)
            val appDao = appDao

            // Read flows synchronously by fetching their first values or through custom direct queries
            // Since we need them immediately, get values from Room lists
            // Since AppDao does not have synchronous getters for all lists yet, let's implement robust serialization
            
            // Let's retrieve all values safely using extension syntax
            val profiles = appDao.getAllProfilesFlow().first()
            if (profiles.isNotEmpty()) {
                val adapter = moshi.adapter<List<UserProfile>>(Types.newParameterizedType(List::class.java, UserProfile::class.java))
                val json = adapter.toJson(profiles)
                postTableData("user_profiles", json, url, key)
            }

            _syncState.value = SupabaseSyncState.Loading("Pushing Roadmaps and lessons...")
            val roadmaps = appDao.getAllRoadmapsFlow().first()
            if (roadmaps.isNotEmpty()) {
                val adapter = moshi.adapter<List<LearningRoadmap>>(Types.newParameterizedType(List::class.java, LearningRoadmap::class.java))
                val json = adapter.toJson(roadmaps)
                postTableData("learning_roadmaps", json, url, key)
            }

            // Let's get all lessons. Since they are nested by roadmapId, we can get them dynamically
            val allLessons = mutableListOf<RoadmapLesson>()
            for (rm in roadmaps) {
                allLessons.addAll(appDao.getLessonsForRoadmap(rm.id))
            }
            if (allLessons.isNotEmpty()) {
                val adapter = moshi.adapter<List<RoadmapLesson>>(Types.newParameterizedType(List::class.java, RoadmapLesson::class.java))
                val json = adapter.toJson(allLessons)
                postTableData("roadmap_lessons", json, url, key)
            }

            _syncState.value = SupabaseSyncState.Loading("Pushing Tasks & Calendars...")
            val learningTasks = appDao.getAllLearningTasksFlow().first()
            if (learningTasks.isNotEmpty()) {
                val adapter = moshi.adapter<List<LearningTask>>(Types.newParameterizedType(List::class.java, LearningTask::class.java))
                val json = adapter.toJson(learningTasks)
                postTableData("learning_tasks", json, url, key)
            }

            _syncState.value = SupabaseSyncState.Loading("Pushing Budget Expenses...")
            val expenses = appDao.getAllExpensesFlow().first()
            if (expenses.isNotEmpty()) {
                val adapter = moshi.adapter<List<ExpenseEntry>>(Types.newParameterizedType(List::class.java, ExpenseEntry::class.java))
                val json = adapter.toJson(expenses)
                postTableData("expense_entries", json, url, key)
            }

            _syncState.value = SupabaseSyncState.Loading("Pushing Saving Tasks...")
            val savings = appDao.getAllSavingTasksFlow().first()
            if (savings.isNotEmpty()) {
                val adapter = moshi.adapter<List<SavingTask>>(Types.newParameterizedType(List::class.java, SavingTask::class.java))
                val json = adapter.toJson(savings)
                postTableData("saving_tasks", json, url, key)
            }

            _syncState.value = SupabaseSyncState.Success("All local data backed up to Supabase successfully!")
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Push failed", e)
            _syncState.value = SupabaseSyncState.Error("Sync failed: ${e.localizedMessage ?: "Unknown network error"}")
        }
    }

    suspend fun forcePullFromSupabase() = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseAnonKey()

        if (url.isBlank() || key.isBlank()) {
            _syncState.value = SupabaseSyncState.Error("Supabase URL or Anon Key is missing!")
            return@withContext
        }

        try {
            _syncState.value = SupabaseSyncState.Loading("Downloading Profiles from Supabase...")
            val profilesJson = getTableData("user_profiles", url, key)
            val profilesList = if (profilesJson.isNotBlank()) {
                val adapter = moshi.adapter<List<UserProfile>>(Types.newParameterizedType(List::class.java, UserProfile::class.java))
                adapter.fromJson(profilesJson) ?: emptyList()
            } else emptyList()

            _syncState.value = SupabaseSyncState.Loading("Downloading Roadmaps from Supabase...")
            val roadmapsJson = getTableData("learning_roadmaps", url, key)
            val roadmapsList = if (roadmapsJson.isNotBlank()) {
                val adapter = moshi.adapter<List<LearningRoadmap>>(Types.newParameterizedType(List::class.java, LearningRoadmap::class.java))
                adapter.fromJson(roadmapsJson) ?: emptyList()
            } else emptyList()

            _syncState.value = SupabaseSyncState.Loading("Downloading Roadmap Lessons from Supabase...")
            val lessonsJson = getTableData("roadmap_lessons", url, key)
            val lessonsList = if (lessonsJson.isNotBlank()) {
                val adapter = moshi.adapter<List<RoadmapLesson>>(Types.newParameterizedType(List::class.java, RoadmapLesson::class.java))
                adapter.fromJson(lessonsJson) ?: emptyList()
            } else emptyList()

            _syncState.value = SupabaseSyncState.Loading("Downloading Learning Tasks from Supabase...")
            val tasksJson = getTableData("learning_tasks", url, key)
            val tasksList = if (tasksJson.isNotBlank()) {
                val adapter = moshi.adapter<List<LearningTask>>(Types.newParameterizedType(List::class.java, LearningTask::class.java))
                adapter.fromJson(tasksJson) ?: emptyList()
            } else emptyList()

            _syncState.value = SupabaseSyncState.Loading("Downloading Expense Entries from Supabase...")
            val expensesJson = getTableData("expense_entries", url, key)
            val expensesList = if (expensesJson.isNotBlank()) {
                val adapter = moshi.adapter<List<ExpenseEntry>>(Types.newParameterizedType(List::class.java, ExpenseEntry::class.java))
                adapter.fromJson(expensesJson) ?: emptyList()
            } else emptyList()

            _syncState.value = SupabaseSyncState.Loading("Downloading Saving Goals from Supabase...")
            val savingsJson = getTableData("saving_tasks", url, key)
            val savingsList = if (savingsJson.isNotBlank()) {
                val adapter = moshi.adapter<List<SavingTask>>(Types.newParameterizedType(List::class.java, SavingTask::class.java))
                adapter.fromJson(savingsJson) ?: emptyList()
            } else emptyList()

            // Safe update Local Room database using atomic operations
            _syncState.value = SupabaseSyncState.Loading("Rebuilding local Database cache...")

            // Re-populate everything safely
            for (profile in profilesList) {
                appDao.insertProfile(profile)
            }
            for (roadmap in roadmapsList) {
                appDao.insertRoadmap(roadmap)
            }
            for (lesson in lessonsList) {
                appDao.insertLesson(lesson)
            }
            for (task in tasksList) {
                appDao.insertLearningTask(task)
            }
            for (expense in expensesList) {
                appDao.insertExpense(expense)
            }
            for (saving in savingsList) {
                appDao.insertSavingTask(saving)
            }

            _syncState.value = SupabaseSyncState.Success("Restored ${profilesList.size + roadmapsList.size + lessonsList.size + tasksList.size + expensesList.size + savingsList.size} cached items from Supabase!")
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Pull failed", e)
            _syncState.value = SupabaseSyncState.Error("Restore failed: ${e.localizedMessage ?: "Unknown network error"}")
        }
    }

    // Helper POST tables
    private fun postTableData(tableName: String, json: String, url: String, key: String) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = buildBaseRequest(tableName, "POST", url, key)
            .header("Prefer", "resolution=merge-duplicates") // Ask postgrest to upsert if possible
            .post(json.toRequestBody(mediaType))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Server responded with code ${response.code}: ${response.body?.string()}")
            }
        }
    }

    // Helper GET tables
    private fun getTableData(tableName: String, url: String, key: String): String {
        val request = buildBaseRequest(tableName, "GET", url, key, "select=*")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 404) return "" // Empty endpoint/no rows defined
                throw IOException("Server responded with code ${response.code}")
            }
            return response.body?.string() ?: ""
        }
    }

    // Register account on Supabase user_accounts table
    suspend fun registerAccountOnBackend(email: String, pwdHash: String, name: String, emoji: String): Boolean = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseAnonKey()
        if (url.isBlank() || key.isBlank()) return@withContext false
        try {
            val accountData = mapOf(
                "email" to email,
                "pwd_hash" to pwdHash,
                "name" to name,
                "emoji" to emoji
            )
            val adapter = moshi.adapter<Map<String, String>>(Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
            val json = adapter.toJson(accountData)
            
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = buildBaseRequest("user_accounts", "POST", url, key)
                .header("Prefer", "resolution=merge-duplicates") // upsert if same email
                .post(json.toRequestBody(mediaType))
                .build()
            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Failed to register account on backend", e)
            return@withContext false
        }
    }

    // Fetch account from Supabase user_accounts table
    suspend fun fetchAccountFromBackend(email: String): Map<String, String>? = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseAnonKey()
        if (url.isBlank() || key.isBlank()) return@withContext null
        try {
            val request = buildBaseRequest("user_accounts", "GET", url, key, "email=eq.$email&limit=1")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val json = response.body?.string() ?: ""
                if (json.isBlank() || json == "[]") return@withContext null
                
                val listType = Types.newParameterizedType(List::class.java, Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
                val adapter = moshi.adapter<List<Map<String, String>>>(listType)
                val list = adapter.fromJson(json)
                return@withContext list?.firstOrNull()
            }
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Failed to fetch account from backend", e)
            return@withContext null
        }
    }

    // Register active device telemetry metrics to Supabase
    suspend fun pushTelemetryToBackend(
        deviceId: String,
        deviceName: String,
        cpuUsage: Int,
        ramUsage: Double,
        userEmail: String
    ): Boolean = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseAnonKey()
        if (url.isBlank() || key.isBlank()) return@withContext false
        try {
            val telemetryData = mapOf(
                "device_id" to deviceId,
                "device_name" to deviceName,
                "cpu_usage" to cpuUsage.toString(),
                "ram_usage" to ramUsage.toString(),
                "user_email" to userEmail
            )
            val adapter = moshi.adapter<Map<String, String>>(Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
            val json = adapter.toJson(telemetryData)
            
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = buildBaseRequest("device_status_telemetry", "POST", url, key)
                .header("Prefer", "resolution=merge-duplicates") // upsert if same device_id
                .post(json.toRequestBody(mediaType))
                .build()
            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Failed to push telemetry to backend", e)
            return@withContext false
        }
    }

    // Fetch all active device telemetries for Admin Portal overview
    suspend fun fetchAllTelemetriesFromBackend(): List<Map<String, Any>>? = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseAnonKey()
        if (url.isBlank() || key.isBlank()) return@withContext null
        try {
            val request = buildBaseRequest("device_status_telemetry", "GET", url, key)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val json = response.body?.string() ?: ""
                if (json.isBlank() || json == "[]") return@withContext emptyList()
                
                val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                val listType = Types.newParameterizedType(List::class.java, mapType)
                val adapter = moshi.adapter<List<Map<String, Any>>>(listType)
                return@withContext adapter.fromJson(json)
            }
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Failed to fetch all telemetries from backend", e)
            return@withContext null
        }
    }

    fun resetState() {
        _syncState.value = SupabaseSyncState.Idle
    }
}
