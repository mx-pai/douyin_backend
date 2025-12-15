package com.douyin.application

import com.douyin.domain.repository.CommentRepository
import com.douyin.domain.repository.LikeRepository
import com.douyin.domain.repository.NoteRepository
import com.douyin.domain.repository.UserRepository
import com.douyin.infrastructure.db.DatabaseFactory
import com.douyin.infrastructure.db.repository.CommentRepositoryImpl
import com.douyin.infrastructure.db.repository.LikeRepositoryImpl
import com.douyin.infrastructure.db.repository.NoteRepositoryImpl
import com.douyin.infrastructure.db.repository.UserRepositoryImpl
import com.douyin.infrastructure.security.JwtService
import com.douyin.interfaces.http.routing.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val config = environment.config.toAppConfig()

    // 初始化数据库
    DatabaseFactory.init(config.db)

    // 初始化 JWT 服务
    JwtService.init(config.jwt)

    // 配置插件
    configurePlugins(config)

    // 初始化 Repository
    val userRepository: UserRepository = UserRepositoryImpl()
    val noteRepository: NoteRepository = NoteRepositoryImpl()
    val commentRepository: CommentRepository = CommentRepositoryImpl()
    val likeRepository: LikeRepository = LikeRepositoryImpl()

    // 配置路由
    routing {
        configureHealthRoutes()
        authRoutes(userRepository)
        noteRoutes(noteRepository, likeRepository)
        likeRoutes(noteRepository, commentRepository, likeRepository)
        commentRoutes(noteRepository, commentRepository, likeRepository, userRepository)
    }
}
