package com.douyin.domain.repository

import com.douyin.domain.model.Comment
import com.douyin.domain.model.CommentWithUser
import com.douyin.domain.model.Cursor

interface CommentRepository {
    suspend fun findById(id: Long): Comment?
    suspend fun findByNoteId(noteId: Long, cursor: Cursor?, limit: Int): List<CommentWithUser>
    suspend fun countByNoteId(noteId: Long): Long
    suspend fun create(comment: Comment): Comment
    suspend fun incrementLikeCount(commentId: Long, delta: Int): Boolean
}
