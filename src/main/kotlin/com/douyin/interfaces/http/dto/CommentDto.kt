package com.douyin.interfaces.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: Long,
    val userName: String,
    val avatar: String?,
    val content: String,
    val timestamp: String,
    val location: String?,
    val likes: Long,
    val isLiked: Boolean,
    val replyToUsername: String?,
    val parentCommentId: Long?,
    val replies: List<CommentDto>? = null
)

@Serializable
data class CommentData(
    val total: Long,
    val nextCursor: String?,
    val list: List<CommentDto>
)

@Serializable
data class CreateCommentRequest(
    val content: String,
    val parentId: Long? = null,
    val location: String? = null
)
