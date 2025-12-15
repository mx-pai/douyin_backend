package com.douyin.domain.repository

import com.douyin.domain.model.Cursor
import com.douyin.domain.model.Note
import com.douyin.domain.model.NoteWithAuthor

interface NoteRepository {
    suspend fun findById(id: Long): Note?
    suspend fun findByIdWithAuthor(id: Long): NoteWithAuthor?
    suspend fun getFeed(cursor: Cursor?, limit: Int): List<NoteWithAuthor>
    suspend fun findByAuthorId(authorId: Long, cursor: Cursor?, limit: Int): List<Note>
    suspend fun create(note: Note): Note
    suspend fun update(note: Note): Boolean
    suspend fun incrementLikeCount(noteId: Long, delta: Int): Boolean
    suspend fun incrementCommentCount(noteId: Long, delta: Int): Boolean
    suspend fun incrementFavoriteCount(noteId: Long, delta: Int): Boolean
}
