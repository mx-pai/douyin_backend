package com.douyin.domain.model

import java.time.Instant

data class Comment(
    val id: Long = 0,
    val noteId: Long,
    val userId: Long,
    val parentId: Long? = null,
    val content: String,
    val location: String? = null,
    val likeCount: Long = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

data class CommentWithUser(
    val comment: Comment,
    val userName: String,
    val userAvatar: String?,
    val replyToUsername: String? = null
)
