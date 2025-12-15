package com.douyin.domain.model

import java.time.Instant

data class User(
    val id: Long = 0,
    val nickname: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val gender: String? = null,
    val city: String? = null,
    val account: String,
    val passwordHash: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
