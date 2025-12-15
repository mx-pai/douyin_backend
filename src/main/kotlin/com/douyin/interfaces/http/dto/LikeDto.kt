package com.douyin.interfaces.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class LikeResponse(
    val isLiked: Boolean,
    val likes: Long
)
