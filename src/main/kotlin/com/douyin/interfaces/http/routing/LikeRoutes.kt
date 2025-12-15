package com.douyin.interfaces.http.routing

import com.douyin.domain.exception.NotFoundException
import com.douyin.domain.exception.UnauthorizedException
import com.douyin.domain.repository.CommentRepository
import com.douyin.domain.repository.LikeRepository
import com.douyin.domain.repository.NoteRepository
import com.douyin.interfaces.http.dto.ApiResponse
import com.douyin.interfaces.http.dto.LikeResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.likeRoutes(
    noteRepository: NoteRepository,
    commentRepository: CommentRepository,
    likeRepository: LikeRepository
) {
    authenticate {
        // Note likes
        route("/api/v1/notes/{id}") {
            post("/like") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                    ?: throw UnauthorizedException()

                val noteId = call.parameters["id"]?.toLongOrNull()
                    ?: throw NotFoundException("笔记不存在")

                val note = noteRepository.findById(noteId)
                    ?: throw NotFoundException("笔记不存在")

                val success = likeRepository.likeNote(userId, noteId)
                if (success) {
                    noteRepository.incrementLikeCount(noteId, 1)
                }

                val isLiked = likeRepository.isNoteLiked(userId, noteId)
                val updatedNote = noteRepository.findById(noteId)!!

                call.respond(
                    ApiResponse(
                        data = LikeResponse(
                            isLiked = isLiked,
                            likes = updatedNote.likeCount
                        )
                    )
                )
            }

            delete("/like") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                    ?: throw UnauthorizedException()

                val noteId = call.parameters["id"]?.toLongOrNull()
                    ?: throw NotFoundException("笔记不存在")

                val note = noteRepository.findById(noteId)
                    ?: throw NotFoundException("笔记不存在")

                val success = likeRepository.unlikeNote(userId, noteId)
                if (success) {
                    noteRepository.incrementLikeCount(noteId, -1)
                }

                val isLiked = likeRepository.isNoteLiked(userId, noteId)
                val updatedNote = noteRepository.findById(noteId)!!

                call.respond(
                    ApiResponse(
                        data = LikeResponse(
                            isLiked = isLiked,
                            likes = updatedNote.likeCount
                        )
                    )
                )
            }

            post("/favorite") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                    ?: throw UnauthorizedException()

                val noteId = call.parameters["id"]?.toLongOrNull()
                    ?: throw NotFoundException("笔记不存在")

                val note = noteRepository.findById(noteId)
                    ?: throw NotFoundException("笔记不存在")

                val success = likeRepository.favoriteNote(userId, noteId)
                if (success) {
                    noteRepository.incrementFavoriteCount(noteId, 1)
                }

                val isFavorited = likeRepository.isNoteFavorited(userId, noteId)
                val updatedNote = noteRepository.findById(noteId)!!

                call.respond(
                    ApiResponse(
                        data = LikeResponse(
                            isLiked = isFavorited,
                            likes = updatedNote.favoriteCount
                        )
                    )
                )
            }

            delete("/favorite") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                    ?: throw UnauthorizedException()

                val noteId = call.parameters["id"]?.toLongOrNull()
                    ?: throw NotFoundException("笔记不存在")

                val note = noteRepository.findById(noteId)
                    ?: throw NotFoundException("笔记不存在")

                val success = likeRepository.unfavoriteNote(userId, noteId)
                if (success) {
                    noteRepository.incrementFavoriteCount(noteId, -1)
                }

                val isFavorited = likeRepository.isNoteFavorited(userId, noteId)
                val updatedNote = noteRepository.findById(noteId)!!

                call.respond(
                    ApiResponse(
                        data = LikeResponse(
                            isLiked = isFavorited,
                            likes = updatedNote.favoriteCount
                        )
                    )
                )
            }
        }

        // Comment likes
        route("/api/v1/comments/{id}") {
            post("/like") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                    ?: throw UnauthorizedException()

                val commentId = call.parameters["id"]?.toLongOrNull()
                    ?: throw NotFoundException("评论不存在")

                val comment = commentRepository.findById(commentId)
                    ?: throw NotFoundException("评论不存在")

                val success = likeRepository.likeComment(userId, commentId)
                if (success) {
                    commentRepository.incrementLikeCount(commentId, 1)
                }

                val isLiked = likeRepository.isCommentLiked(userId, commentId)
                val updatedComment = commentRepository.findById(commentId)!!

                call.respond(
                    ApiResponse(
                        data = LikeResponse(
                            isLiked = isLiked,
                            likes = updatedComment.likeCount
                        )
                    )
                )
            }

            delete("/like") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                    ?: throw UnauthorizedException()

                val commentId = call.parameters["id"]?.toLongOrNull()
                    ?: throw NotFoundException("评论不存在")

                val comment = commentRepository.findById(commentId)
                    ?: throw NotFoundException("评论不存在")

                val success = likeRepository.unlikeComment(userId, commentId)
                if (success) {
                    commentRepository.incrementLikeCount(commentId, -1)
                }

                val isLiked = likeRepository.isCommentLiked(userId, commentId)
                val updatedComment = commentRepository.findById(commentId)!!

                call.respond(
                    ApiResponse(
                        data = LikeResponse(
                            isLiked = isLiked,
                            likes = updatedComment.likeCount
                        )
                    )
                )
            }
        }
    }
}
