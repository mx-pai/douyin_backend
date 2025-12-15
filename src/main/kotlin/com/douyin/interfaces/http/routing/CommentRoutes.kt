package com.douyin.interfaces.http.routing

import com.douyin.domain.exception.BadRequestException
import com.douyin.domain.exception.NotFoundException
import com.douyin.domain.exception.UnauthorizedException
import com.douyin.domain.model.Comment
import com.douyin.domain.model.CommentWithUser
import com.douyin.domain.model.Cursor
import com.douyin.domain.repository.CommentRepository
import com.douyin.domain.repository.LikeRepository
import com.douyin.domain.repository.NoteRepository
import com.douyin.domain.repository.UserRepository
import com.douyin.interfaces.http.dto.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Route.commentRoutes(
    noteRepository: NoteRepository,
    commentRepository: CommentRepository,
    likeRepository: LikeRepository,
    userRepository: UserRepository
) {
    route("/api/v1/notes/{noteId}/comments") {

        // 公开接口：获取评论列表
        get {
            val noteId = call.parameters["noteId"]?.toLongOrNull()
                ?: throw NotFoundException("笔记不存在")

            noteRepository.findById(noteId)
                ?: throw NotFoundException("笔记不存在")

            val cursorParam = call.request.queryParameters["cursor"]
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

            val cursor = Cursor.decode(cursorParam)
            val comments = commentRepository.findByNoteId(noteId, cursor, limit.coerceIn(1, 50))
            val total = commentRepository.countByNoteId(noteId)

            // 尝试获取当前用户的点赞状态
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
            val likedSet = if (userId != null && comments.isNotEmpty()) {
                likeRepository.getCommentLikedSet(userId, comments.map { it.comment.id })
            } else {
                emptySet()
            }

            val nextCursor = if (comments.size == limit && comments.isNotEmpty()) {
                val lastComment = comments.last().comment
                Cursor(lastComment.createdAt, lastComment.id).encode()
            } else null

            call.respond(
                ApiResponse(
                    data = CommentData(
                        total = total,
                        nextCursor = nextCursor,
                        list = comments.map { it.toCommentDto(it.comment.id in likedSet) }
                    )
                )
            )
        }

        // 需要登录：发表评论
        authenticate {
            post {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                    ?: throw UnauthorizedException()

                val noteId = call.parameters["noteId"]?.toLongOrNull()
                    ?: throw NotFoundException("笔记不存在")

                noteRepository.findById(noteId)
                    ?: throw NotFoundException("笔记不存在")

                val request = call.receive<CreateCommentRequest>()

                if (request.content.isBlank()) {
                    throw BadRequestException("评论内容不能为空")
                }

                // 验证父评论存在
                if (request.parentId != null) {
                    val parentComment = commentRepository.findById(request.parentId)
                        ?: throw BadRequestException("回复的评论不存在")
                    if (parentComment.noteId != noteId) {
                        throw BadRequestException("回复的评论不属于该笔记")
                    }
                }

                val comment = commentRepository.create(
                    Comment(
                        noteId = noteId,
                        userId = userId,
                        parentId = request.parentId,
                        content = request.content,
                        location = request.location
                    )
                )

                // 更新笔记评论数
                noteRepository.incrementCommentCount(noteId, 1)

                // 获取用户信息
                val user = userRepository.findById(userId)!!

                // 获取回复目标用户名
                val replyToUsername = if (request.parentId != null) {
                    val parentComment = commentRepository.findById(request.parentId)!!
                    val parentUser = userRepository.findById(parentComment.userId)
                    parentUser?.nickname
                } else null

                val commentWithUser = CommentWithUser(
                    comment = comment,
                    userName = user.nickname,
                    userAvatar = user.avatarUrl,
                    replyToUsername = replyToUsername
                )

                call.respond(
                    ApiResponse(
                        data = commentWithUser.toCommentDto(isLiked = false)
                    )
                )
            }
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    .withZone(ZoneId.of("Asia/Shanghai"))

private fun CommentWithUser.toCommentDto(isLiked: Boolean) = CommentDto(
    id = comment.id,
    userName = userName,
    avatar = userAvatar,
    content = comment.content,
    timestamp = dateFormatter.format(comment.createdAt),
    location = comment.location,
    likes = comment.likeCount,
    isLiked = isLiked,
    replyToUsername = replyToUsername,
    parentCommentId = comment.parentId,
    replies = null
)
