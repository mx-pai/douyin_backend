package com.douyin.infrastructure.db.repository

import com.douyin.domain.model.Cursor
import com.douyin.domain.model.Note
import com.douyin.domain.model.NoteWithAuthor
import com.douyin.domain.repository.NoteRepository
import com.douyin.infrastructure.db.table.NotesTable
import com.douyin.infrastructure.db.table.UsersTable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class NoteRepositoryImpl : NoteRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun findById(id: Long): Note? = dbQuery {
        NotesTable.selectAll()
            .where { NotesTable.id eq id }
            .map { it.toNote() }
            .singleOrNull()
    }

    override suspend fun findByIdWithAuthor(id: Long): NoteWithAuthor? = dbQuery {
        NotesTable.join(UsersTable, JoinType.INNER, NotesTable.authorId, UsersTable.id)
            .selectAll()
            .where { NotesTable.id eq id }
            .map { row ->
                NoteWithAuthor(
                    note = row.toNote(),
                    authorName = row[UsersTable.nickname],
                    authorAvatar = row[UsersTable.avatarUrl]
                )
            }
            .singleOrNull()
    }

    override suspend fun getFeed(cursor: Cursor?, limit: Int): List<NoteWithAuthor> = dbQuery {
        val query = NotesTable.join(UsersTable, JoinType.INNER, NotesTable.authorId, UsersTable.id)
            .selectAll()
            .where { NotesTable.isPublic eq true }

        val filteredQuery = if (cursor != null) {
            query.andWhere {
                (NotesTable.createdAt less cursor.timestamp) or
                    ((NotesTable.createdAt eq cursor.timestamp) and (NotesTable.id less cursor.id))
            }
        } else {
            query
        }

        filteredQuery
            .orderBy(NotesTable.createdAt to SortOrder.DESC, NotesTable.id to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                NoteWithAuthor(
                    note = row.toNote(),
                    authorName = row[UsersTable.nickname],
                    authorAvatar = row[UsersTable.avatarUrl]
                )
            }
    }

    override suspend fun findByAuthorId(authorId: Long, cursor: Cursor?, limit: Int): List<Note> = dbQuery {
        val query = NotesTable.selectAll()
            .where { NotesTable.authorId eq authorId }

        val filteredQuery = if (cursor != null) {
            query.andWhere {
                (NotesTable.createdAt less cursor.timestamp) or
                    ((NotesTable.createdAt eq cursor.timestamp) and (NotesTable.id less cursor.id))
            }
        } else {
            query
        }

        filteredQuery
            .orderBy(NotesTable.createdAt to SortOrder.DESC, NotesTable.id to SortOrder.DESC)
            .limit(limit)
            .map { it.toNote() }
    }

    override suspend fun create(note: Note): Note = dbQuery {
        val now = Instant.now()
        val id = NotesTable.insert {
            it[authorId] = note.authorId
            it[title] = note.title
            it[coverUrl] = note.coverUrl
            it[coverWidth] = note.coverWidth
            it[coverHeight] = note.coverHeight
            it[isVideo] = note.isVideo
            it[mediaUrl] = note.mediaUrl
            it[images] = note.images?.let { imgs -> json.encodeToString(imgs) }
            it[likeCount] = note.likeCount
            it[commentCount] = note.commentCount
            it[favoriteCount] = note.favoriteCount
            it[createdAt] = now
            it[updatedAt] = now
            it[isPublic] = note.isPublic
            it[status] = note.status.toShort()
        }[NotesTable.id]
        note.copy(id = id, createdAt = now, updatedAt = now)
    }

    override suspend fun update(note: Note): Boolean = dbQuery {
        val now = Instant.now()
        NotesTable.update({ NotesTable.id eq note.id }) {
            it[title] = note.title
            it[coverUrl] = note.coverUrl
            it[coverWidth] = note.coverWidth
            it[coverHeight] = note.coverHeight
            it[isVideo] = note.isVideo
            it[mediaUrl] = note.mediaUrl
            it[images] = note.images?.let { imgs -> json.encodeToString(imgs) }
            it[isPublic] = note.isPublic
            it[status] = note.status.toShort()
            it[updatedAt] = now
        } > 0
    }

    override suspend fun incrementLikeCount(noteId: Long, delta: Int): Boolean = dbQuery {
        NotesTable.update({ NotesTable.id eq noteId }) {
            with(SqlExpressionBuilder) {
                it.update(likeCount, likeCount + delta.toLong())
            }
        } > 0
    }

    override suspend fun incrementCommentCount(noteId: Long, delta: Int): Boolean = dbQuery {
        NotesTable.update({ NotesTable.id eq noteId }) {
            with(SqlExpressionBuilder) {
                it.update(commentCount, commentCount + delta.toLong())
            }
        } > 0
    }

    override suspend fun incrementFavoriteCount(noteId: Long, delta: Int): Boolean = dbQuery {
        NotesTable.update({ NotesTable.id eq noteId }) {
            with(SqlExpressionBuilder) {
                it.update(favoriteCount, favoriteCount + delta.toLong())
            }
        } > 0
    }

    private fun ResultRow.toNote() = Note(
        id = this[NotesTable.id],
        authorId = this[NotesTable.authorId],
        title = this[NotesTable.title],
        coverUrl = this[NotesTable.coverUrl],
        coverWidth = this[NotesTable.coverWidth],
        coverHeight = this[NotesTable.coverHeight],
        isVideo = this[NotesTable.isVideo],
        mediaUrl = this[NotesTable.mediaUrl],
        images = this[NotesTable.images]?.let { json.decodeFromString<List<String>>(it) },
        likeCount = this[NotesTable.likeCount],
        commentCount = this[NotesTable.commentCount],
        favoriteCount = this[NotesTable.favoriteCount],
        createdAt = this[NotesTable.createdAt],
        updatedAt = this[NotesTable.updatedAt],
        isPublic = this[NotesTable.isPublic],
        status = this[NotesTable.status].toInt()
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
