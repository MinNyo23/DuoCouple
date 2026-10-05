package com.example.data.model

data class CoupleInviteRecord(
    val code: String,
    val creatorEmail: String,
    val creatorName: String,
    val creatorEmoji: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
