package com.douyin.infrastructure.db.repository

import com.douyin.domain.model.Comment
import com.douyin.domain.model.CommentWithUser
import com.douyin.domain.model.Cursor
import com.douyin.domain.repository.CommentRepository
import com.douyin.infrastructure.db.table.CommentsTable
import com.douyin.infrastructure.db.table.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class CommentRepositoryImpl : CommentRepository {

    override suspend fun findById(id: Long): Comment? = dbQuery {
        CommentsTable.selectAll()
            .where { CommentsTable.id eq id }
            .map { it.toComment() }
            .singleOrNull()
    }

    override suspend fun findByNoteId(noteId: Long, cursor: Cursor?, limit: Int): List<CommentWithUser> = dbQuery {
        // 别名用于查询回复目标用户
        val parentUser = UsersTable.alias("parent_user")

        val query = CommentsTable
            .join(UsersTable, JoinType.INNER, CommentsTable.userId, UsersTable.id)
            .join(parentUser, JoinType.LEFT, CommentsTable.parentId, CommentsTable.id) {
                CommentsTable.parentId.isNotNull()
            }
            .selectAll()
            .where { CommentsTable.noteId eq noteId }

        val filteredQuery = if (cursor != null) {
            query.andWhere {
                (CommentsTable.createdAt less cursor.timestamp) or
                    ((CommentsTable.createdAt eq cursor.timestamp) and (CommentsTable.id less cursor.id))
            }
        } else {
            query
        }

        // 先获取评论的基础信息
        val comments = CommentsTable
            .join(UsersTable, JoinType.INNER, CommentsTable.userId, UsersTable.id)
            .selectAll()
            .where { CommentsTable.noteId eq noteId }
            .let { q ->
                if (cursor != null) {
                    q.andWhere {
                        (CommentsTable.createdAt less cursor.timestamp) or
                            ((CommentsTable.createdAt eq cursor.timestamp) and (CommentsTable.id less cursor.id))
                    }
                } else q
            }
            .orderBy(CommentsTable.createdAt to SortOrder.DESC, CommentsTable.id to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                val comment = row.toComment()
                CommentWithUser(
                    comment = comment,
                    userName = row[UsersTable.nickname],
                    userAvatar = row[UsersTable.avatarUrl],
                    replyToUsername = null // 后面单独查询
                )
            }

        // 查询回复目标用户名
        val parentIds = comments.mapNotNull { it.comment.parentId }.distinct()
        val parentUsernames = if (parentIds.isNotEmpty()) {
            CommentsTable
                .join(UsersTable, JoinType.INNER, CommentsTable.userId, UsersTable.id)
                .select(CommentsTable.id, UsersTable.nickname)
                .where { CommentsTable.id inList parentIds }
                .associate { it[CommentsTable.id] to it[UsersTable.nickname] }
        } else {
            emptyMap()
        }

        comments.map { cwu ->
            cwu.copy(replyToUsername = cwu.comment.parentId?.let { parentUsernames[it] })
        }
    }

    override suspend fun countByNoteId(noteId: Long): Long = dbQuery {
        CommentsTable.selectAll()
            .where { CommentsTable.noteId eq noteId }
            .count()
    }

    override suspend fun create(comment: Comment): Comment = dbQuery {
        val now = Instant.now()
        val id = CommentsTable.insert {
            it[noteId] = comment.noteId
            it[userId] = comment.userId
            it[parentId] = comment.parentId
            it[content] = comment.content
            it[location] = comment.location
            it[likeCount] = comment.likeCount
            it[createdAt] = now
            it[updatedAt] = now
        }[CommentsTable.id]
        comment.copy(id = id, createdAt = now, updatedAt = now)
    }

    override suspend fun incrementLikeCount(commentId: Long, delta: Int): Boolean = dbQuery {
        CommentsTable.update({ CommentsTable.id eq commentId }) {
            with(SqlExpressionBuilder) {
                it.update(likeCount, likeCount + delta.toLong())
            }
        } > 0
    }

    private fun ResultRow.toComment() = Comment(
        id = this[CommentsTable.id],
        noteId = this[CommentsTable.noteId],
        userId = this[CommentsTable.userId],
        parentId = this[CommentsTable.parentId],
        content = this[CommentsTable.content],
        location = this[CommentsTable.location],
        likeCount = this[CommentsTable.likeCount],
        createdAt = this[CommentsTable.createdAt],
        updatedAt = this[CommentsTable.updatedAt]
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
