package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.UserProfile
import com.example.data.model.LearningRoadmap
import com.example.data.model.RoadmapLesson
import com.example.data.model.LearningTask
import com.example.data.model.ExpenseEntry
import com.example.data.model.SavingTask
import com.example.data.repository.AppRepository
import com.example.data.remote.SupabaseSyncManager
import com.example.data.remote.SupabaseSyncState
import com.example.network.GeminiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.security.MessageDigest

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    val supabaseSyncManager: SupabaseSyncManager
    
    // --- Secure Duo-Coupling SharedPreferences ---
    private val sharedPrefs = application.getSharedPreferences("duo_space_auth_prefs", android.content.Context.MODE_PRIVATE)
    private val accountsPrefs = application.getSharedPreferences("local_accounts_prefs", android.content.Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isAppLoading = MutableStateFlow(true)
    val isAppLoading: StateFlow<Boolean> = _isAppLoading.asStateFlow()

    private val _isCoupled = MutableStateFlow(sharedPrefs.getBoolean("is_coupled", false))
    val isCoupled: StateFlow<Boolean> = _isCoupled.asStateFlow()

    private val _coupleCode = MutableStateFlow(sharedPrefs.getString("couple_code", "") ?: "")
    val coupleCode: StateFlow<String> = _coupleCode.asStateFlow()

    private val _myProfileName = MutableStateFlow(sharedPrefs.getString("my_name", "Minnyo") ?: "Minnyo")
    val myProfileName: StateFlow<String> = _myProfileName.asStateFlow()

    private val _myProfileEmoji = MutableStateFlow(sharedPrefs.getString("my_emoji", "🦁") ?: "🦁")
    val myProfileEmoji: StateFlow<String> = _myProfileEmoji.asStateFlow()

    private val _partnerProfileName = MutableStateFlow(sharedPrefs.getString("partner_name", "Honey 🌸") ?: "Honey 🌸")
    val partnerProfileName: StateFlow<String> = _partnerProfileName.asStateFlow()

    private val _partnerProfileEmoji = MutableStateFlow(sharedPrefs.getString("partner_emoji", "🦄") ?: "🦄")
    val partnerProfileEmoji: StateFlow<String> = _partnerProfileEmoji.asStateFlow()

    private val _loveStartDate = MutableStateFlow(sharedPrefs.getString("love_start_date", "") ?: "")
    val loveStartDate: StateFlow<String> = _loveStartDate.asStateFlow()

    // --- State Streams ---
    val profilesFlow: StateFlow<List<UserProfile>>
    val allExpensesFlow: StateFlow<List<ExpenseEntry>>
    val roadmapsFlow: StateFlow<List<LearningRoadmap>>
    val allSavingTasksFlow: StateFlow<List<SavingTask>>
    val allLearningTasksFlow: StateFlow<List<LearningTask>>

    // --- UI Controls ---
    private val _selectedTab = MutableStateFlow(0) // 0: Dashboard, 1: Learning & Calendar, 2: Monthly Expenses, 3: Saving Advisor, 4: Profiles
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // --- Gemini Advisor State ---
    private val _aiAdvice = MutableStateFlow<String>("")
    val aiAdvice: StateFlow<String> = _aiAdvice.asStateFlow()

    private val _isLoadingAdvice = MutableStateFlow(false)
    val isLoadingAdvice: StateFlow<Boolean> = _isLoadingAdvice.asStateFlow()

    // --- Active User Selected (For adding individual items on the UI) ---
    private val _activeUserContext = MutableStateFlow("user") // "user" or "girlfriend"
    val activeUserContext: StateFlow<String> = _activeUserContext.asStateFlow()

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
        supabaseSyncManager = SupabaseSyncManager(application, appDao)

        // Initialize today's date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        _selectedDate.value = sdf.format(Date())

        // Set up reactive streams
        profilesFlow = repository.allProfilesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allExpensesFlow = repository.allExpensesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        roadmapsFlow = repository.allRoadmapsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSavingTasksFlow = repository.allSavingTasksFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allLearningTasksFlow = repository.allLearningTasksFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed data on cold start if database is empty
        viewModelScope.launch {
            seedDefaultAccounts()
            seedInitialDatabaseIfEmpty()
            startTelemetryLoop()
            // Keep the loading screen active for 2 seconds to showcase the modern logo and background transition
            kotlinx.coroutines.delay(2000)
            _isAppLoading.value = false
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectDate(dateString: String) {
        _selectedDate.value = dateString
    }

    fun setActiveUserContext(id: String) {
        _activeUserContext.value = id
    }

    // --- Supabase Actions ---
    val supabaseSyncState: StateFlow<SupabaseSyncState> = supabaseSyncManager.syncState

    fun saveSupabaseCredentials(url: String, key: String) {
        supabaseSyncManager.saveCredentials(url, key)
    }

    fun clearSupabaseCredentials() {
        supabaseSyncManager.clearCredentials()
    }

    fun isSupabaseConfigured(): Boolean {
        return supabaseSyncManager.isConfigured()
    }

    fun triggerSupabasePush() {
        viewModelScope.launch {
            supabaseSyncManager.forcePushToSupabase()
        }
    }

    fun triggerSupabasePull() {
        viewModelScope.launch {
            supabaseSyncManager.forcePullFromSupabase()
        }
    }

    fun resetSupabaseState() {
        supabaseSyncManager.resetState()
    }

    suspend fun testSupabaseConnection(): Boolean {
        return supabaseSyncManager.testConnection()
    }

    // --- Database Mutations ---

    // Profiles
    fun updateProfile(id: String, name: String, avatar: String, dailyBudget: Double, monthlySavingGoal: Double) {
        viewModelScope.launch {
            repository.insertProfile(
                UserProfile(
                    id = id,
                    name = name,
                    avatarEmoji = avatar,
                    dailyBudget = dailyBudget,
                    monthlySavingGoal = monthlySavingGoal
                )
            )
            if (id == "user") {
                _myProfileName.value = name
                _myProfileEmoji.value = avatar
                sharedPrefs.edit()
                    .putString("my_name", name)
                    .putString("my_emoji", avatar)
                    .apply()
            } else if (id == "girlfriend") {
                _partnerProfileName.value = name
                _partnerProfileEmoji.value = avatar
                sharedPrefs.edit()
                    .putString("partner_name", name)
                    .putString("partner_emoji", avatar)
                    .apply()
            }
            triggerBackupOfActiveUser()
        }
    }

    suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String, emoji: String): String? {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty()) return "Email cannot be empty"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return "Please enter a valid email address"
        }
        if (password.length < 6) return "Password must be at least 6 characters"
        if (name.trim().isEmpty()) return "Name cannot be empty"

        val hashedPassword = password.sha256()

        // If Supabase is configured, see if account already exists on the backend
        if (supabaseSyncManager.isConfigured()) {
            val remoteAccount = supabaseSyncManager.fetchAccountFromBackend(cleanEmail)
            if (remoteAccount != null) {
                return "An account with this email already exists on the backend"
            }
        }

        val existingPwd = accountsPrefs.getString("acc_pwd_$cleanEmail", null)
        if (existingPwd != null) {
            return "An account with this email already exists"
        }

        // Save locally
        accountsPrefs.edit()
            .putString("acc_pwd_$cleanEmail", hashedPassword)
            .putString("acc_name_$cleanEmail", name.trim())
            .putString("acc_emoji_$cleanEmail", emoji)
            .apply()

        // Clear old local tables before creating a brand-new user to prevent leftover session leak
        withContext(Dispatchers.IO) {
            val appDao = AppDatabase.getDatabase(getApplication()).appDao()
            appDao.clearUserProfiles()
            appDao.clearLearningRoadmaps()
            appDao.clearRoadmapLessons()
            appDao.clearLearningTasks()
            appDao.clearExpenseEntries()
            appDao.clearSavingTasks()
        }

        // Track active user email
        sharedPrefs.edit().putString("active_user_email", cleanEmail).apply()

        // Save to backend database
        if (supabaseSyncManager.isConfigured()) {
            supabaseSyncManager.registerAccountOnBackend(cleanEmail, hashedPassword, name.trim(), emoji)
        }

        // Generate couple code automatically right after creating account!
        generateAndSetCoupleCode()

        logIn(name.trim(), emoji)
        triggerBackupOfActiveUser()
        return null
    }

    suspend fun logInWithEmailAndPassword(email: String, password: String): String? {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty()) return "Email cannot be empty"
        if (password.isEmpty()) return "Password cannot be empty"

        var registeredPwd = accountsPrefs.getString("acc_pwd_$cleanEmail", null)
        var name = accountsPrefs.getString("acc_name_$cleanEmail", null)
        var emoji = accountsPrefs.getString("acc_emoji_$cleanEmail", null)

        val hashedPassword = password.sha256()

        // If not found locally, query backend
        if (registeredPwd == null && supabaseSyncManager.isConfigured()) {
            val remote = supabaseSyncManager.fetchAccountFromBackend(cleanEmail)
            if (remote != null) {
                val remotePwd = remote["pwd_hash"]
                val remoteName = remote["name"] ?: "User"
                val remoteEmoji = remote["emoji"] ?: "🦁"
                if (remotePwd != null) {
                    registeredPwd = remotePwd
                    name = remoteName
                    emoji = remoteEmoji
                    // Backport to local storage for offline use
                    accountsPrefs.edit()
                        .putString("acc_pwd_$cleanEmail", remotePwd)
                        .putString("acc_name_$cleanEmail", remoteName)
                        .putString("acc_emoji_$cleanEmail", remoteEmoji)
                        .apply()
                }
            }
        }

        if (registeredPwd == null) {
            return "Account not found. Select 'Create Account' to register."
        }

        if (registeredPwd != hashedPassword && registeredPwd != password) {
            return "Incorrect password. Please try again."
        }

        // Automatic security upgrade for legacy plain-text accounts
        if (registeredPwd == password) {
            accountsPrefs.edit().putString("acc_pwd_$cleanEmail", hashedPassword).apply()
            if (supabaseSyncManager.isConfigured()) {
                supabaseSyncManager.registerAccountOnBackend(cleanEmail, hashedPassword, name ?: "User", emoji ?: "🦁")
            }
        }

        // Track active user email
        sharedPrefs.edit().putString("active_user_email", cleanEmail).apply()

        // Restore user data and config BEFORE triggering active login state
        restoreUserData(cleanEmail)

        val finalName = sharedPrefs.getString("my_name", "")?.takeIf { it.isNotBlank() } ?: name ?: "Minnyo"
        val finalEmoji = sharedPrefs.getString("my_emoji", "")?.takeIf { it.isNotBlank() } ?: emoji ?: "🦁"

        // Ensure we pre-populate couple code if empty
        val currentCode = sharedPrefs.getString("couple_code", "") ?: ""
        if (currentCode.isBlank()) {
            generateAndSetCoupleCode()
        }

        logIn(finalName, finalEmoji)
        triggerBackupOfActiveUser()
        return null
    }

    suspend fun verifyEmailExists(email: String): Boolean = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty()) return@withContext false
        
        // Check local
        val localPwd = accountsPrefs.getString("acc_pwd_$cleanEmail", null)
        if (localPwd != null) return@withContext true
        
        // Check backend
        if (supabaseSyncManager.isConfigured()) {
            val remote = supabaseSyncManager.fetchAccountFromBackend(cleanEmail)
            if (remote != null) {
                // Pre-populate to local storage for future reference
                val remotePwd = remote["pwd_hash"] ?: ""
                val remoteName = remote["name"] ?: "User"
                val remoteEmoji = remote["emoji"] ?: "🦁"
                accountsPrefs.edit()
                    .putString("acc_pwd_$cleanEmail", remotePwd)
                    .putString("acc_name_$cleanEmail", remoteName)
                    .putString("acc_emoji_$cleanEmail", remoteEmoji)
                    .apply()
                return@withContext true
            }
        }
        false
    }

    suspend fun updateAccountPassword(email: String, newPasswordPlain: String): Boolean = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val hashedPassword = newPasswordPlain.sha256()
        
        // Fetch existing metadata to avoid losing name/emoji
        val name = accountsPrefs.getString("acc_name_$cleanEmail", "User") ?: "User"
        val emoji = accountsPrefs.getString("acc_emoji_$cleanEmail", "🦁") ?: "🦁"
        
        accountsPrefs.edit()
            .putString("acc_pwd_$cleanEmail", hashedPassword)
            .apply()
            
        if (supabaseSyncManager.isConfigured()) {
            return@withContext supabaseSyncManager.registerAccountOnBackend(cleanEmail, hashedPassword, name, emoji)
        }
        true
    }

    fun logIn(name: String, emoji: String) {
        viewModelScope.launch {
            _myProfileName.value = name
            _myProfileEmoji.value = emoji
            _isLoggedIn.value = true
            sharedPrefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("my_name", name)
                .putString("my_emoji", emoji)
                .apply()
            
            // Sync with database
            repository.insertProfile(
                UserProfile(
                    id = "user",
                    name = name,
                    avatarEmoji = emoji,
                    dailyBudget = 80.0,
                    monthlySavingGoal = 600.0
                )
            )
        }
    }

    fun generateAndSetCoupleCode() {
        val randomNum = (1000..9999).random()
        val prefixes = listOf("LOVE", "DUO", "BOND", "SOUL", "COSM")
        val code = "${prefixes.random()}-$randomNum"
        _coupleCode.value = code
        sharedPrefs.edit().putString("couple_code", code).apply()
    }

    fun completeCoupling(partnerName: String, partnerEmoji: String) {
        viewModelScope.launch {
            _partnerProfileName.value = partnerName
            _partnerProfileEmoji.value = partnerEmoji
            _isCoupled.value = true
            sharedPrefs.edit()
                .putBoolean("is_coupled", true)
                .putString("partner_name", partnerName)
                .putString("partner_emoji", partnerEmoji)
                .apply()
            
            // Sync with database
            repository.insertProfile(
                UserProfile(
                    id = "girlfriend",
                    name = partnerName,
                    avatarEmoji = partnerEmoji,
                    dailyBudget = 70.0,
                    monthlySavingGoal = 550.0
                )
            )
            triggerBackupOfActiveUser()
        }
    }

    fun updateLoveStartDate(dateStr: String) {
        _loveStartDate.value = dateStr
        sharedPrefs.edit().putString("love_start_date", dateStr).apply()
        com.example.widget.DashboardWidgetProvider.triggerUpdate(getApplication())
        triggerBackupOfActiveUser()
    }

    fun logOut() {
        viewModelScope.launch {
            val email = getActiveUserEmail()
            if (email.isNotBlank()) {
                backupUserData(email)
            }

            _isLoggedIn.value = false
            _isCoupled.value = false
            _coupleCode.value = ""
            _myProfileName.value = "Minnyo"
            _myProfileEmoji.value = "🦁"
            _partnerProfileName.value = "Honey 🌸"
            _partnerProfileEmoji.value = "🦄"
            _loveStartDate.value = ""
            
            sharedPrefs.edit().clear().apply()
            
            // Clear current database values cleanly on Thread Pool
            withContext(Dispatchers.IO) {
                val appDao = AppDatabase.getDatabase(getApplication()).appDao()
                appDao.clearUserProfiles()
                appDao.clearLearningRoadmaps()
                appDao.clearRoadmapLessons()
                appDao.clearLearningTasks()
                appDao.clearExpenseEntries()
                appDao.clearSavingTasks()
                
                // Seed defaults again
                appDao.insertProfile(UserProfile("user", "Minnyo", "🦁", dailyBudget = 80.0, monthlySavingGoal = 600.0))
                appDao.insertProfile(UserProfile("girlfriend", "Honey 🌸", "🦄", dailyBudget = 70.0, monthlySavingGoal = 550.0))
            }
        }
    }

    // Roadmaps
    fun addRoadmap(ownerId: String, title: String, description: String, lessons: List<Pair<String, String>>) {
        viewModelScope.launch {
            val roadmapId = repository.insertRoadmap(
                LearningRoadmap(ownerId = ownerId, title = title, description = description)
            ).toInt()
            
            // Generate standard lessons
            lessons.forEachIndexed { index, lessonPair ->
                repository.insertLesson(
                    RoadmapLesson(
                        roadmapId = roadmapId,
                        title = lessonPair.first,
                        description = lessonPair.second,
                        difficulty = when {
                            index == 0 -> "Beginner"
                            index < lessons.size - 1 -> "Intermediate"
                            else -> "Advanced"
                        },
                        isCompleted = false,
                        orderIndex = index
                    )
                )
            }
            triggerBackupOfActiveUser()
        }
    }

    fun deleteRoadmap(roadmapId: Int) {
        viewModelScope.launch {
            repository.deleteRoadmapById(roadmapId)
            triggerBackupOfActiveUser()
        }
    }

    // Lessons
    fun toggleLessonCompletion(lesson: RoadmapLesson) {
        viewModelScope.launch {
            repository.updateLesson(lesson.copy(isCompleted = !lesson.isCompleted))
            triggerBackupOfActiveUser()
        }
    }

    fun deleteLesson(lessonId: Int) {
        viewModelScope.launch {
            repository.deleteLessonById(lessonId)
            triggerBackupOfActiveUser()
        }
    }

    fun addLessonToRoadmap(roadmapId: Int, title: String, description: String, difficulty: String, orderIndex: Int) {
        viewModelScope.launch {
            repository.insertLesson(
                RoadmapLesson(
                    roadmapId = roadmapId,
                    title = title,
                    description = description,
                    difficulty = difficulty,
                    isCompleted = false,
                    orderIndex = orderIndex
                )
            )
            triggerBackupOfActiveUser()
        }
    }

    // Learning Tasks
    fun addLearningTask(ownerId: String, title: String, dateString: String, minutes: Int) {
        viewModelScope.launch {
            repository.insertLearningTask(
                LearningTask(
                    ownerId = ownerId,
                    title = title,
                    dateString = dateString,
                    isCompleted = false,
                    minutesSpent = minutes
                )
            )
            triggerBackupOfActiveUser()
        }
    }

    fun toggleLearningTaskCompletion(task: LearningTask) {
        viewModelScope.launch {
            repository.updateLearningTask(task.copy(isCompleted = !task.isCompleted))
            triggerBackupOfActiveUser()
        }
    }

    fun deleteLearningTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteLearningTaskById(taskId)
            triggerBackupOfActiveUser()
        }
    }

    // Expense Entries
    fun addExpense(ownerId: String, amount: Double, isIncome: Boolean, category: String, dateString: String, note: String) {
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntry(
                    ownerId = ownerId,
                    amount = amount,
                    isIncome = isIncome,
                    category = category,
                    dateString = dateString,
                    note = note
                )
            )
            com.example.widget.DashboardWidgetProvider.triggerUpdate(getApplication())
            triggerBackupOfActiveUser()
        }
    }

    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            repository.deleteExpenseById(expenseId)
            com.example.widget.DashboardWidgetProvider.triggerUpdate(getApplication())
            triggerBackupOfActiveUser()
        }
    }

    // Saving Tasks
    fun addSavingTask(ownerId: String, title: String, reward: Double, dateString: String) {
        viewModelScope.launch {
            repository.insertSavingTask(
                SavingTask(
                    ownerId = ownerId,
                    title = title,
                    rewardAmount = reward,
                    dateString = dateString,
                    isCompleted = false
                )
            )
            triggerBackupOfActiveUser()
        }
    }

    fun toggleSavingTaskCompletion(task: SavingTask) {
        viewModelScope.launch {
            repository.updateSavingTask(task.copy(isCompleted = !task.isCompleted))
            triggerBackupOfActiveUser()
        }
    }

    fun deleteSavingTask(id: Int) {
        viewModelScope.launch {
            repository.deleteSavingTaskById(id)
            triggerBackupOfActiveUser()
        }
    }

    // --- AI Savings Advisor Trigger ---
    fun fetchAISavingsAdvice() {
        viewModelScope.launch {
            _isLoadingAdvice.value = true
            try {
                val profiles = profilesFlow.value
                val expenses = allExpensesFlow.value
                val advice = GeminiService.getDualSavingsAdvice(profiles, expenses)
                _aiAdvice.value = advice
            } catch (e: Exception) {
                _aiAdvice.value = "Failed to connect to savings advisor: ${e.localizedMessage}. Check your internet connection."
            } finally {
                _isLoadingAdvice.value = false
            }
        }
    }

    fun getLessonsForRoadmap(roadmapId: Int): Flow<List<RoadmapLesson>> {
        return repository.getLessonsForRoadmapFlow(roadmapId)
    }

    // --- Account-scoped Local Data Backup & Recovery ---
    private fun triggerBackupOfActiveUser() {
        val email = getActiveUserEmail()
        if (email.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                kotlinx.coroutines.delay(200) // Ensure DB operations have committed
                backupUserData(email)
            }
        }
    }

    private suspend fun backupUserData(email: String) = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty()) return@withContext

        try {
            val appDao = AppDatabase.getDatabase(getApplication()).appDao()
            val moshi = com.squareup.moshi.Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            // 1. Gather all local database states
            val profiles = appDao.getAllProfilesFlow().first()
            val expenses = appDao.getAllExpensesList()
            val learningTasks = appDao.getAllLearningTasksFlow().first()
            val savingTasks = appDao.getAllSavingTasksFlow().first()
            val roadmaps = appDao.getAllRoadmapsFlow().first()
            val lessons = mutableListOf<com.example.data.model.RoadmapLesson>()
            for (roadmap in roadmaps) {
                lessons.addAll(appDao.getLessonsForRoadmap(roadmap.id))
            }

            // 2. Serialize database structures to JSON
            val profilesJson = moshi.adapter<List<com.example.data.model.UserProfile>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.UserProfile::class.java)
            ).toJson(profiles)

            val expensesJson = moshi.adapter<List<com.example.data.model.ExpenseEntry>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.ExpenseEntry::class.java)
            ).toJson(expenses)

            val learningTasksJson = moshi.adapter<List<com.example.data.model.LearningTask>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.LearningTask::class.java)
            ).toJson(learningTasks)

            val savingTasksJson = moshi.adapter<List<com.example.data.model.SavingTask>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.SavingTask::class.java)
            ).toJson(savingTasks)

            val roadmapsJson = moshi.adapter<List<com.example.data.model.LearningRoadmap>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.LearningRoadmap::class.java)
            ).toJson(roadmaps)

            val lessonsJson = moshi.adapter<List<com.example.data.model.RoadmapLesson>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.RoadmapLesson::class.java)
            ).toJson(lessons)

            // 3. Extract the active coupling and partner configurations
            val isCoupled = sharedPrefs.getBoolean("is_coupled", false)
            val partnerName = sharedPrefs.getString("partner_name", "") ?: ""
            val partnerEmoji = sharedPrefs.getString("partner_emoji", "") ?: ""
            val coupleCode = sharedPrefs.getString("couple_code", "") ?: ""
            val loveStartDate = sharedPrefs.getString("love_start_date", "") ?: ""
            val myName = sharedPrefs.getString("my_name", "") ?: ""
            val myEmoji = sharedPrefs.getString("my_emoji", "") ?: ""

            // 4. Persistence into separate local accounts preference container
            accountsPrefs.edit()
                .putString("data_profiles_$cleanEmail", profilesJson)
                .putString("data_expenses_$cleanEmail", expensesJson)
                .putString("data_learning_tasks_$cleanEmail", learningTasksJson)
                .putString("data_saving_tasks_$cleanEmail", savingTasksJson)
                .putString("data_roadmaps_$cleanEmail", roadmapsJson)
                .putString("data_lessons_$cleanEmail", lessonsJson)
                .putBoolean("cfg_is_coupled_$cleanEmail", isCoupled)
                .putString("cfg_partner_name_$cleanEmail", partnerName)
                .putString("cfg_partner_emoji_$cleanEmail", partnerEmoji)
                .putString("cfg_couple_code_$cleanEmail", coupleCode)
                .putString("cfg_love_start_date_$cleanEmail", loveStartDate)
                .putString("cfg_my_name_$cleanEmail", myName)
                .putString("cfg_my_emoji_$cleanEmail", myEmoji)
                .apply()

            android.util.Log.d("UserDataBackup", "Successfully backed up data locally for $cleanEmail")
        } catch (e: Exception) {
            android.util.Log.e("UserDataBackup", "Error executing local data backup", e)
        }
    }

    private suspend fun restoreUserData(email: String) = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty()) return@withContext

        try {
            val appDao = AppDatabase.getDatabase(getApplication()).appDao()
            val moshi = com.squareup.moshi.Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            // 1. Flush/clear any active/cached database tables before restoration
            appDao.clearUserProfiles()
            appDao.clearLearningRoadmaps()
            appDao.clearRoadmapLessons()
            appDao.clearLearningTasks()
            appDao.clearExpenseEntries()
            appDao.clearSavingTasks()

            // 2. Fetch back strings from accountsPrefs
            val profilesJson = accountsPrefs.getString("data_profiles_$cleanEmail", null)
            val expensesJson = accountsPrefs.getString("data_expenses_$cleanEmail", null)
            val learningTasksJson = accountsPrefs.getString("data_learning_tasks_$cleanEmail", null)
            val savingTasksJson = accountsPrefs.getString("data_saving_tasks_$cleanEmail", null)
            val roadmapsJson = accountsPrefs.getString("data_roadmaps_$cleanEmail", null)
            val lessonsJson = accountsPrefs.getString("data_lessons_$cleanEmail", null)

            // 3. If there is no backed-up history for this account, seed starter database configuration
            if (profilesJson.isNullOrEmpty()) {
                seedInitialDatabaseIfEmpty()
            } else {
                // Safely map and feed them back to the database
                val profiles = moshi.adapter<List<com.example.data.model.UserProfile>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.UserProfile::class.java)
                ).fromJson(profilesJson)
                profiles?.forEach { appDao.insertProfile(it) }

                if (!expensesJson.isNullOrEmpty()) {
                    val expenses = moshi.adapter<List<com.example.data.model.ExpenseEntry>>(
                        com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.ExpenseEntry::class.java)
                    ).fromJson(expensesJson)
                    expenses?.forEach { appDao.insertExpense(it) }
                }

                if (!learningTasksJson.isNullOrEmpty()) {
                    val learningTasks = moshi.adapter<List<com.example.data.model.LearningTask>>(
                        com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.LearningTask::class.java)
                    ).fromJson(learningTasksJson)
                    learningTasks?.forEach { appDao.insertLearningTask(it) }
                }

                if (!savingTasksJson.isNullOrEmpty()) {
                    val savingTasks = moshi.adapter<List<com.example.data.model.SavingTask>>(
                        com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.SavingTask::class.java)
                    ).fromJson(savingTasksJson)
                    savingTasks?.forEach { appDao.insertSavingTask(it) }
                }

                if (!roadmapsJson.isNullOrEmpty()) {
                    val roadmaps = moshi.adapter<List<com.example.data.model.LearningRoadmap>>(
                        com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.LearningRoadmap::class.java)
                    ).fromJson(roadmapsJson)
                    roadmaps?.forEach { appDao.insertRoadmap(it) }
                }

                if (!lessonsJson.isNullOrEmpty()) {
                    val lessons = moshi.adapter<List<com.example.data.model.RoadmapLesson>>(
                        com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.RoadmapLesson::class.java)
                    ).fromJson(lessonsJson)
                    lessons?.forEach { appDao.insertLesson(it) }
                }
            }

            // 4. Restore the companion configuration parameters into memory & SharedPrefs
            val isCoupled = accountsPrefs.getBoolean("cfg_is_coupled_$cleanEmail", false)
            val partnerName = accountsPrefs.getString("cfg_partner_name_$cleanEmail", "") ?: ""
            val partnerEmoji = accountsPrefs.getString("cfg_partner_emoji_$cleanEmail", "") ?: ""
            val coupleCode = accountsPrefs.getString("cfg_couple_code_$cleanEmail", "") ?: ""
            val loveStartDate = accountsPrefs.getString("cfg_love_start_date_$cleanEmail", "") ?: ""
            val myName = accountsPrefs.getString("cfg_my_name_$cleanEmail", "") ?: ""
            val myEmoji = accountsPrefs.getString("cfg_my_emoji_$cleanEmail", "") ?: ""

            // Correctly execute state Flow mutations on the main thread
            withContext(Dispatchers.Main) {
                _isCoupled.value = isCoupled
                _partnerProfileName.value = if (partnerName.isNotBlank()) partnerName else "Honey 🌸"
                _partnerProfileEmoji.value = if (partnerEmoji.isNotBlank()) partnerEmoji else "🦄"
                _coupleCode.value = coupleCode
                _loveStartDate.value = loveStartDate
                if (myName.isNotBlank()) _myProfileName.value = myName
                if (myEmoji.isNotBlank()) _myProfileEmoji.value = myEmoji
            }

            sharedPrefs.edit()
                .putBoolean("is_coupled", isCoupled)
                .putString("partner_name", partnerName)
                .putString("partner_emoji", partnerEmoji)
                .putString("couple_code", coupleCode)
                .putString("love_start_date", loveStartDate)
                .putString("my_name", myName)
                .putString("my_emoji", myEmoji)
                .apply()

            android.util.Log.d("UserDataBackup", "Successfully restored data and coupling config for $cleanEmail")
        } catch (e: Exception) {
            android.util.Log.e("UserDataBackup", "Error executing local data recovery", e)
        }
    }

    // Seeding logic
    private fun seedDefaultAccounts() {
        val minnyoEmail = "minnyo.work@gmail.com"
        val demoEmail = "demo@example.com"
        
        // Seed minnyo.work@gmail.com if it doesn't already exist
        if (accountsPrefs.getString("acc_pwd_$minnyoEmail", null) == null) {
            accountsPrefs.edit()
                .putString("acc_pwd_$minnyoEmail", "123456".sha256())
                .putString("acc_name_$minnyoEmail", "Minnyo")
                .putString("acc_emoji_$minnyoEmail", "🦁")
                .apply()
        }
        
        // Seed demo@example.com if it doesn't already exist
        if (accountsPrefs.getString("acc_pwd_$demoEmail", null) == null) {
            accountsPrefs.edit()
                .putString("acc_pwd_$demoEmail", "123456".sha256())
                .putString("acc_name_$demoEmail", "DuoUser")
                .putString("acc_emoji_$demoEmail", "🐻")
                .apply()
        }
    }

    private suspend fun seedInitialDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingProfiles = repository.allProfilesFlow.first()
        if (existingProfiles.isNotEmpty()) return@withContext

        // 1. Setup Profiles
        val p1 = UserProfile("user", "Minnyo", "🦁", dailyBudget = 80.0, monthlySavingGoal = 600.0)
        val p2 = UserProfile("girlfriend", "Honey 🌸", "🦄", dailyBudget = 70.0, monthlySavingGoal = 550.0)
        repository.insertProfile(p1)
        repository.insertProfile(p2)

        val todayStr = _selectedDate.value

        // 2. Setup Roadmaps & Starter Lessons
        val r1Id = repository.insertRoadmap(
            LearningRoadmap(ownerId = "user", title = "Android Compose Mastery 🚀", description = "Learning responsive Glass UI development with Jetpack Compose.")
        ).toInt()
        repository.insertLesson(RoadmapLesson(roadmapId = r1Id, title = "Compose Layout Fundamentals", description = "Understand Rows, Columns, Boxes and modifiers.", difficulty = "Beginner", isCompleted = true, orderIndex = 0))
        repository.insertLesson(RoadmapLesson(roadmapId = r1Id, title = "Central State Management", description = "Learn ViewModel, MutableStateFlow, and Flow collection.", difficulty = "Beginner", isCompleted = true, orderIndex = 1))
        repository.insertLesson(RoadmapLesson(roadmapId = r1Id, title = "Styling custom gradients and shape borders", description = "Combine glass translucent layers and linear brushes.", difficulty = "Intermediate", isCompleted = false, orderIndex = 2))
        repository.insertLesson(RoadmapLesson(roadmapId = r1Id, title = "Integrating Room & async tasks", description = "Establish database layer with KSP compilation.", difficulty = "Advanced", isCompleted = false, orderIndex = 3))

        val r2Id = repository.insertRoadmap(
            LearningRoadmap(ownerId = "girlfriend", title = "Flutter Mobile UI App Design 🎨", description = "Mastering Flutter widgets, state parameters, and transitions.")
        ).toInt()
        repository.insertLesson(RoadmapLesson(roadmapId = r2Id, title = "Stateless vs Stateful Widgets", description = "Learn widget hierarchies and build parameters.", difficulty = "Beginner", isCompleted = true, orderIndex = 0))
        repository.insertLesson(RoadmapLesson(roadmapId = r2Id, title = "Hero animations & glass transitions", description = "Design elegant fluid layout changes.", difficulty = "Intermediate", isCompleted = false, orderIndex = 1))
        repository.insertLesson(RoadmapLesson(roadmapId = r2Id, title = "State control with Riverpod", description = "Understand reactive updates and state engines.", difficulty = "Advanced", isCompleted = false, orderIndex = 2))

        // 3. Setup Learning Tasks
        repository.insertLearningTask(LearningTask(ownerId = "user", title = "Study Jetpack Compose Modifiers", dateString = todayStr, isCompleted = true, minutesSpent = 45))
        repository.insertLearningTask(LearningTask(ownerId = "user", title = "Refactor Database Repositories", dateString = todayStr, isCompleted = false, minutesSpent = 0))
        repository.insertLearningTask(LearningTask(ownerId = "girlfriend", title = "Practice Flutter Flex layouts", dateString = todayStr, isCompleted = true, minutesSpent = 60))

        // 4. Setup Initial Expense / Cash Logs
        // Real entries to demonstrate
        repository.insertExpense(ExpenseEntry(ownerId = "user", amount = 15.0, isIncome = false, category = "Food", dateString = todayStr, note = "Duo dinner meal box"))
        repository.insertExpense(ExpenseEntry(ownerId = "user", amount = 22.0, isIncome = false, category = "Transport", dateString = todayStr, note = "Gasoline fill-up"))
        repository.insertExpense(ExpenseEntry(ownerId = "girlfriend", amount = 8.5, isIncome = false, category = "Shopping", dateString = todayStr, note = "Cute sticky notes bundle"))
        repository.insertExpense(ExpenseEntry(ownerId = "girlfriend", amount = 1200.0, isIncome = true, category = "Salary", dateString = todayStr, note = "Biweekly design payroll"))
        repository.insertExpense(ExpenseEntry(ownerId = "user", amount = 1400.0, isIncome = true, category = "Salary", dateString = todayStr, note = "Biweekly tech salary"))

        // 5. Setup Micro Saving Tasks
        repository.insertSavingTask(SavingTask(ownerId = "shared", title = "Skip afternoon café run (Drink home tea)", rewardAmount = 12.0, dateString = todayStr, isCompleted = true))
        repository.insertSavingTask(SavingTask(ownerId = "shared", title = "Cook dinner together at home", rewardAmount = 25.0, dateString = todayStr, isCompleted = false))
        repository.insertSavingTask(SavingTask(ownerId = "user", title = "Carpooled to work", rewardAmount = 10.0, dateString = todayStr, isCompleted = false))
        repository.insertSavingTask(SavingTask(ownerId = "girlfriend", title = "Unsubscribe unused streaming app", rewardAmount = 15.0, dateString = todayStr, isCompleted = false))
    }

    // --- Admin Portal Telemetry Engine & Helpers ---
    private val _adminDeviceTelemetries = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val adminDeviceTelemetries: StateFlow<List<Map<String, Any>>> = _adminDeviceTelemetries.asStateFlow()

    fun getActiveUserEmail(): String {
        return sharedPrefs.getString("active_user_email", "guest@example.com") ?: "guest@example.com"
    }

    fun triggerTelemetryPush() {
        viewModelScope.launch {
            if (!supabaseSyncManager.isConfigured()) return@launch
            val devId = android.os.Build.ID ?: "simulated_id_${(1000..9999).random()}"
            val devName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            val cpuSimulated = (10..65).random() // Realistic representation of active CPU workload
            
            // Extract memory heap stats dynamically
            val totalMemory = Runtime.getRuntime().totalMemory()
            val freeMemory = Runtime.getRuntime().freeMemory()
            val usedMemory = (totalMemory - freeMemory).toDouble() / (1024.0 * 1024.0) // RAM usage in Megabytes
            
            val activeMail = getActiveUserEmail()
            
            supabaseSyncManager.pushTelemetryToBackend(
                deviceId = devId,
                deviceName = devName,
                cpuUsage = cpuSimulated,
                ramUsage = usedMemory,
                userEmail = activeMail
            )
        }
    }

    fun startTelemetryLoop() {
        viewModelScope.launch {
            while (true) {
                if (isLoggedIn.value) {
                    triggerTelemetryPush()
                }
                kotlinx.coroutines.delay(20000) // sync telemetry and heartbeat every 20 seconds
            }
        }
    }

    fun fetchAdminTelemetries() {
        viewModelScope.launch {
            if (!supabaseSyncManager.isConfigured()) return@launch
            val list = supabaseSyncManager.fetchAllTelemetriesFromBackend()
            if (list != null) {
                _adminDeviceTelemetries.value = list
            }
        }
    }
}

private fun String.sha256(): String {
    return try {
        val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        bytes.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        // Fallback robust checksum matching if SHA-256 instance fails
        this.hashCode().toString()
    }
}
