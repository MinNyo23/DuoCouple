package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.UserProfile
import com.example.data.model.ExpenseEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Call the Gemini API to get dual savings advice based on their current joint records.
     */
    suspend fun getDualSavingsAdvice(
        profiles: List<UserProfile>,
        recentExpenses: List<ExpenseEntry>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "AI Advisory is ready! Please enter your real Google Gemini API Key in the AI Studio Secrets panel to enable real-time predictions."
        }

        val myProfile = profiles.find { it.id == "user" }
        val gfProfile = profiles.find { it.id == "girlfriend" }

        val myName = myProfile?.name ?: "Minnyo"
        val gfName = gfProfile?.name ?: "Sweetheart"

        val myGoal = myProfile?.monthlySavingGoal ?: 500.0
        val gfGoal = gfProfile?.monthlySavingGoal ?: 500.0

        val totalMyExpense = recentExpenses.filter { it.ownerId == "user" && !it.isIncome }.sumOf { it.amount }
        val totalMyIncome = recentExpenses.filter { it.ownerId == "user" && it.isIncome }.sumOf { it.amount }

        val totalGfExpense = recentExpenses.filter { it.ownerId == "girlfriend" && !it.isIncome }.sumOf { it.amount }
        val totalGfIncome = recentExpenses.filter { it.ownerId == "girlfriend" && it.isIncome }.sumOf { it.amount }

        val prompt = """
            You are a helpful, warm, and cute Couple's Saving Money Advisor called "UsSpace AI".
            You are analyzing the financial data of a loving couple: $myName and $gfName.
            They have NO SECRETS from each other. They share all savings, tasks, and budgets.
            
            Current joint data:
            - $myName:
              * Monthly Saving Goal: ${'$'}$myGoal
              * Income recorded: ${'$'}$totalMyIncome
              * Expenses recorded: ${'$'}$totalMyExpense
            - $gfName:
              * Monthly Saving Goal: ${'$'}$gfGoal
              * Income recorded: ${'$'}$totalGfIncome
              * Expenses recorded: ${'$'}$totalGfExpense
              
            Joint Total Balance: Saved: ${'$'}${(totalMyIncome + totalGfIncome) - (totalMyExpense + totalGfExpense)}
            
            Please provide a cute, conversational couple's financial advice response in Markdown. Include:
            1. 📊 Co-status Checkup: How are they doing on their savings paths?
            2. 💡 Duo Action Actions: 3 cute, collaborative micro-tasks to save money together (e.g. "Create a study date at home instead of coffee shop to save ${'$'}15").
            3. 💕 Warm encouragement: A lovely sweet couple affirmation.
            
            Format nicely with emojis, brief bullet points, and neat typography appropriate for a Glass UI container. Maintain a friendly, supportive tone! Keep it under 200 words.
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            // Construct JSON request body using standard JSONObject
            val contentObj = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
                })
            }
            val requestBodyObj = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(contentObj)
                })
            }

            val requestBody = requestBodyObj.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val respBody = response.body?.string() ?: ""
                val jsonResp = JSONObject(respBody)
                val candidates = jsonResp.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No advice text received.")
                    }
                }
                return@withContext "Could not interpret Gemini response."
            } else {
                val errBody = response.body?.string() ?: ""
                Log.e(TAG, "Request failed: ${response.code}, body: $errBody")
                return@withContext "Advice Unavailable: API error ${response.code}. Ensure your Gemini API Key in the Secrets is valid."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching advice", e)
            return@withContext "Advice Unavailable. Please verify your connection or check that the Secrets API Key is configured correctly."
        }
    }
}
