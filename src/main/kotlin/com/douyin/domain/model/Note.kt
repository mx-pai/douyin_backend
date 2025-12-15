package com.douyin.domain.model

import java.time.Instant

data class Note(
    val id: Long = 0,
    val authorId: Long,
    val title: String? = null,
    val coverUrl: String? = null,
    val coverWidth: Int? = null,
    val coverHeight: Int? = null,
    val isVideo: Boolean = false,
    val mediaUrl: String? = null,
    val images: List<String>? = null,
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val favoriteCount: Long = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val isPublic: Boolean = true,
    val status: Int = 0
)

data class NoteWithAuthor(
    val note: Note,
    val authorName: String,
    val authorAvatar: String?
)
