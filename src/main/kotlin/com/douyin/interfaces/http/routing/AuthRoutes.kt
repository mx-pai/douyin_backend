package com.douyin.interfaces.http.routing

import com.douyin.domain.exception.BadRequestException
import com.douyin.domain.exception.ConflictException
import com.douyin.domain.exception.UnauthorizedException
import com.douyin.domain.model.User
import com.douyin.domain.repository.UserRepository
import com.douyin.infrastructure.security.JwtService
import com.douyin.infrastructure.security.PasswordEncoder
import com.douyin.interfaces.http.dto.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userRepository: UserRepository) {
    route("/api/v1/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()

            if (request.account.isBlank() || request.password.isBlank()) {
                throw BadRequestException("账号和密码不能为空")
            }

            if (request.password.length < 6) {
                throw BadRequestException("密码长度至少6位")
            }

            if (userRepository.existsByAccount(request.account)) {
                throw ConflictException("账号已存在")
            }

            val user = userRepository.create(
                User(
                    account = request.account,
                    passwordHash = PasswordEncoder.encode(request.password),
                    nickname = request.nickname.ifBlank { "用户${System.currentTimeMillis() % 100000}" }
                )
            )

            val token = JwtService.generateToken(user.id)

            call.respond(
                ApiResponse(
                    data = LoginResponse(
                        token = token,
                        user = user.toDto()
                    )
                )
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            if (request.account.isBlank() || request.password.isBlank()) {
                throw BadRequestException("账号和密码不能为空")
            }

            val user = userRepository.findByAccount(request.account)
                ?: throw UnauthorizedException("账号或密码错误")

            if (!PasswordEncoder.verify(request.password, user.passwordHash)) {
                throw UnauthorizedException("账号或密码错误")
            }

            val token = JwtService.generateToken(user.id)

            call.respond(
                ApiResponse(
                    data = LoginResponse(
                        token = token,
                        user = user.toDto()
                    )
                )
            )
        }

        authenticate {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: throw UnauthorizedException()

                val user = userRepository.findById(userId)
                    ?: throw UnauthorizedException("用户不存在")

                call.respond(ApiResponse(data = user.toDto()))
            }
        }
    }
}

private fun User.toDto() = UserDto(
    id = id,
    nickname = nickname,
    avatar = avatarUrl,
    bio = bio,
    gender = gender,
    city = city
)
