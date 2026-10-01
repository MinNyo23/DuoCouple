package com.example.data.remote

import com.example.BuildConfig

/**
 * Supabase URL and publishable (anon) key are supplied at **build time** via `.env`
 * (Secrets Gradle Plugin → BuildConfig). End users never enter these in the app.
 */
object SupabaseConfig {
    fun projectUrl(): String = normalizeUrl(BuildConfig.SUPABASE_URL)

    fun anonKey(): String = normalizeKey(BuildConfig.SUPABASE_ANON_KEY)

    fun isConfigured(): Boolean {
        val url = projectUrl()
        val key = anonKey()
        val looksLikePublicKey = key.startsWith("eyJ") || key.startsWith("sb_publishable_")
        return url.startsWith("https://") &&
            url.contains(".supabase.co") &&
            looksLikePublicKey &&
            !key.contains("your-publishable-key", ignoreCase = true) &&
            !key.contains("your-anon-public-key", ignoreCase = true) &&
            !key.contains("MY_", ignoreCase = true)
    }

    private fun normalizeUrl(raw: String?): String =
        raw?.trim()?.trim('"', '\'')?.removeSuffix("/").orEmpty()

    private fun normalizeKey(raw: String?): String =
        raw?.trim()?.trim('"', '\'')
            ?.removePrefix("Bearer ")
            ?.removePrefix("bearer ")
            ?.trim()
            .orEmpty()
}
