package com.example.data.model

import com.squareup.moshi.Json

/**
 * Server-side couple membership row (`couple_pairs` table / RPC payloads).
 */
data class CouplePairRecord(
    val id: String,
    @Json(name = "invite_code") val inviteCode: String,
    @Json(name = "user_a_id") val userAId: String,
    @Json(name = "user_b_id") val userBId: String? = null,
    val status: String,
    @Json(name = "creator_name") val creatorName: String,
    @Json(name = "creator_emoji") val creatorEmoji: String,
    @Json(name = "joiner_name") val joinerName: String? = null,
    @Json(name = "joiner_emoji") val joinerEmoji: String? = null,
    @Json(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    fun isCreator(currentUserId: String): Boolean = userAId == currentUserId
    fun isJoiner(currentUserId: String): Boolean = userBId == currentUserId
}
