package com.douyin.infrastructure.db.repository

import com.douyin.domain.repository.LikeRepository
import com.douyin.infrastructure.db.table.CommentLikesTable
import com.douyin.infrastructure.db.table.NoteFavoritesTable
import com.douyin.infrastructure.db.table.NoteLikesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class LikeRepositoryImpl : LikeRepository {

    // Note Likes
    override suspend fun likeNote(userId: Long, noteId: Long): Boolean = dbQuery {
        try {
            NoteLikesTable.insert {
                it[NoteLikesTable.userId] = userId
                it[targetId] = noteId
                it[createdAt] = Instant.now()
            }
            true
        } catch (e: Exception) {
            false // 已存在
        }
    }

    override suspend fun unlikeNote(userId: Long, noteId: Long): Boolean = dbQuery {
        NoteLikesTable.deleteWhere {
            (NoteLikesTable.userId eq userId) and (targetId eq noteId)
        } > 0
    }

    override suspend fun isNoteLiked(userId: Long, noteId: Long): Boolean = dbQuery {
        NoteLikesTable.selectAll()
            .where { (NoteLikesTable.userId eq userId) and (NoteLikesTable.targetId eq noteId) }
            .count() > 0
    }

    override suspend fun getNoteLikedSet(userId: Long, noteIds: List<Long>): Set<Long> = dbQuery {
        if (noteIds.isEmpty()) return@dbQuery emptySet()
        NoteLikesTable.selectAll()
            .where { (NoteLikesTable.userId eq userId) and (NoteLikesTable.targetId inList noteIds) }
            .map { it[NoteLikesTable.targetId] }
            .toSet()
    }

    // Note Favorites
    override suspend fun favoriteNote(userId: Long, noteId: Long): Boolean = dbQuery {
        try {
            NoteFavoritesTable.insert {
                it[NoteFavoritesTable.userId] = userId
                it[targetId] = noteId
                it[createdAt] = Instant.now()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun unfavoriteNote(userId: Long, noteId: Long): Boolean = dbQuery {
        NoteFavoritesTable.deleteWhere {
            (NoteFavoritesTable.userId eq userId) and (targetId eq noteId)
        } > 0
    }

    override suspend fun isNoteFavorited(userId: Long, noteId: Long): Boolean = dbQuery {
        NoteFavoritesTable.selectAll()
            .where { (NoteFavoritesTable.userId eq userId) and (NoteFavoritesTable.targetId eq noteId) }
            .count() > 0
    }

    // Comment Likes
    override suspend fun likeComment(userId: Long, commentId: Long): Boolean = dbQuery {
        try {
            CommentLikesTable.insert {
                it[CommentLikesTable.userId] = userId
                it[targetId] = commentId
                it[createdAt] = Instant.now()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun unlikeComment(userId: Long, commentId: Long): Boolean = dbQuery {
        CommentLikesTable.deleteWhere {
            (CommentLikesTable.userId eq userId) and (targetId eq commentId)
        } > 0
    }

    override suspend fun isCommentLiked(userId: Long, commentId: Long): Boolean = dbQuery {
        CommentLikesTable.selectAll()
            .where { (CommentLikesTable.userId eq userId) and (CommentLikesTable.targetId eq commentId) }
            .count() > 0
    }

    override suspend fun getCommentLikedSet(userId: Long, commentIds: List<Long>): Set<Long> = dbQuery {
        if (commentIds.isEmpty()) return@dbQuery emptySet()
        CommentLikesTable.selectAll()
            .where { (CommentLikesTable.userId eq userId) and (CommentLikesTable.targetId inList commentIds) }
            .map { it[CommentLikesTable.targetId] }
            .toSet()
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
