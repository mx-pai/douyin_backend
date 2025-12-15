package com.douyin.interfaces.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: Long,
    val title: String?,
    val userName: String,
    val avatar: String?,
    val cover: String?,
    val coverWidth: Int?,
    val coverHeight: Int?,
    val likes: Long,
    val isVideo: Boolean,
    val isLiked: Boolean,
    val images: List<String>?
)

@Serializable
data class NoteDetailDto(
    val id: Long,
    val title: String?,
    val userName: String,
    val avatar: String?,
    val cover: String?,
    val coverWidth: Int?,
    val coverHeight: Int?,
    val likes: Long,
    val comments: Long,
    val favorites: Long,
    val isVideo: Boolean,
    val isLiked: Boolean,
    val isFavorited: Boolean,
    val images: List<String>?,
    val mediaUrl: String?,
    val createdAt: String
)

@Serializable
data class FeedData(
    val list: List<NoteDto>,
    val nextCursor: String?
)
