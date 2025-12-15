package com.douyin.interfaces.http.routing

import com.douyin.domain.exception.NotFoundException
import com.douyin.domain.model.Cursor
import com.douyin.domain.model.NoteWithAuthor
import com.douyin.domain.repository.LikeRepository
import com.douyin.domain.repository.NoteRepository
import com.douyin.interfaces.http.dto.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Route.noteRoutes(
    noteRepository: NoteRepository,
    likeRepository: LikeRepository
) {
    route("/api/v1/notes") {

        // 公开接口：获取 Feed
        get("/feed") {
            val cursorParam = call.request.queryParameters["cursor"]
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

            val cursor = Cursor.decode(cursorParam)
            val notes = noteRepository.getFeed(cursor, limit.coerceIn(1, 50))

            // 尝试获取当前用户的点赞状态
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
            val likedSet = if (userId != null && notes.isNotEmpty()) {
                likeRepository.getNoteLikedSet(userId, notes.map { it.note.id })
            } else {
                emptySet()
            }

            val nextCursor = if (notes.size == limit && notes.isNotEmpty()) {
                val lastNote = notes.last().note
                Cursor(lastNote.createdAt, lastNote.id).encode()
            } else null

            call.respond(
                ApiResponse(
                    data = FeedData(
                        list = notes.map { it.toNoteDto(it.note.id in likedSet) },
                        nextCursor = nextCursor
                    )
                )
            )
        }

        // 公开接口：获取笔记详情
        get("/{id}") {
            val noteId = call.parameters["id"]?.toLongOrNull()
                ?: throw NotFoundException("笔记不存在")

            val noteWithAuthor = noteRepository.findByIdWithAuthor(noteId)
                ?: throw NotFoundException("笔记不存在")

            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
            val isLiked = userId?.let { likeRepository.isNoteLiked(it, noteId) } ?: false
            val isFavorited = userId?.let { likeRepository.isNoteFavorited(it, noteId) } ?: false

            call.respond(
                ApiResponse(
                    data = noteWithAuthor.toNoteDetailDto(isLiked, isFavorited)
                )
            )
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    .withZone(ZoneId.of("Asia/Shanghai"))

private fun NoteWithAuthor.toNoteDto(isLiked: Boolean) = NoteDto(
    id = note.id,
    title = note.title,
    userName = authorName,
    avatar = authorAvatar,
    cover = note.coverUrl,
    coverWidth = note.coverWidth,
    coverHeight = note.coverHeight,
    likes = note.likeCount,
    isVideo = note.isVideo,
    isLiked = isLiked,
    images = note.images
)

private fun NoteWithAuthor.toNoteDetailDto(isLiked: Boolean, isFavorited: Boolean) = NoteDetailDto(
    id = note.id,
    title = note.title,
    userName = authorName,
    avatar = authorAvatar,
    cover = note.coverUrl,
    coverWidth = note.coverWidth,
    coverHeight = note.coverHeight,
    likes = note.likeCount,
    comments = note.commentCount,
    favorites = note.favoriteCount,
    isVideo = note.isVideo,
    isLiked = isLiked,
    isFavorited = isFavorited,
    images = note.images,
    mediaUrl = note.mediaUrl,
    createdAt = dateFormatter.format(note.createdAt)
)
