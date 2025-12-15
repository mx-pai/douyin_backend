package com.douyin.domain.repository

interface LikeRepository {
    suspend fun likeNote(userId: Long, noteId: Long): Boolean
    suspend fun unlikeNote(userId: Long, noteId: Long): Boolean
    suspend fun isNoteLiked(userId: Long, noteId: Long): Boolean
    suspend fun getNoteLikedSet(userId: Long, noteIds: List<Long>): Set<Long>

    suspend fun favoriteNote(userId: Long, noteId: Long): Boolean
    suspend fun unfavoriteNote(userId: Long, noteId: Long): Boolean
    suspend fun isNoteFavorited(userId: Long, noteId: Long): Boolean

    suspend fun likeComment(userId: Long, commentId: Long): Boolean
    suspend fun unlikeComment(userId: Long, commentId: Long): Boolean
    suspend fun isCommentLiked(userId: Long, commentId: Long): Boolean
    suspend fun getCommentLikedSet(userId: Long, commentIds: List<Long>): Set<Long>
}
