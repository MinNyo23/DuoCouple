package com.example.location

import android.content.Context
import android.location.Location
import com.example.data.db.AppDatabase
import com.example.data.model.PartnerLocationRecord
import com.example.data.remote.SupabaseSyncManager
import com.example.data.repository.AppRepository

/**
 * Publishes the logged-in partner's GPS fix to Room and Supabase (when configured).
 */
class PartnerLocationPublisher(context: Context) {
    private val appContext = context.applicationContext
    private val repository = AppRepository(AppDatabase.getDatabase(appContext).appDao())
    private val supabaseSyncManager = SupabaseSyncManager(
        appContext,
        AppDatabase.getDatabase(appContext).appDao()
    )

    suspend fun publishFix(location: Location): PublishResult {
        val coupleId = supabaseSyncManager.getActiveCoupleId().takeIf { it.isNotBlank() }
        val record = PartnerLocationRecord(
            ownerId = LOCAL_OWNER_ID,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            isSharingEnabled = true,
            updatedAt = System.currentTimeMillis(),
            coupleId = coupleId
        )
        cacheLastFix(location)
        repository.insertPartnerLocation(record)
        val cloudOk = if (supabaseSyncManager.isConfigured()) {
            supabaseSyncManager.pushPartnerLocation(record)
        } else {
            true
        }
        return PublishResult(
            latitude = location.latitude,
            longitude = location.longitude,
            cloudOk = cloudOk
        )
    }

    suspend fun publishSharingPaused() {
        val prefs = appContext.getSharedPreferences("duo_space_auth_prefs", Context.MODE_PRIVATE)
        val lastLat = prefs.getFloat("last_loc_lat", 0f).toDouble()
        val lastLon = prefs.getFloat("last_loc_lon", 0f).toDouble()
        val record = PartnerLocationRecord(
            ownerId = LOCAL_OWNER_ID,
            latitude = lastLat,
            longitude = lastLon,
            accuracyMeters = 0f,
            isSharingEnabled = false,
            updatedAt = System.currentTimeMillis(),
            coupleId = supabaseSyncManager.getActiveCoupleId().takeIf { it.isNotBlank() }
        )
        repository.insertPartnerLocation(record)
        if (supabaseSyncManager.isConfigured()) {
            supabaseSyncManager.pushPartnerLocation(record)
        }
    }

    fun cacheLastFix(location: Location) {
        appContext.getSharedPreferences("duo_space_auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .putFloat("last_loc_lat", location.latitude.toFloat())
            .putFloat("last_loc_lon", location.longitude.toFloat())
            .apply()
    }

    data class PublishResult(val latitude: Double, val longitude: Double, val cloudOk: Boolean)

    companion object {
        /** Each device stores its own GPS under the local "user" profile slot. */
        const val LOCAL_OWNER_ID = "user"
    }
}
