package com.douyin.application

import com.douyin.domain.exception.AppException
import com.douyin.interfaces.http.dto.ApiResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Application")

fun Application.configurePlugins(config: AppConfig) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/api") }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            "$httpMethod $path -> $status"
        }
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // TODO: 生产环境应限制为特定域名
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024)
        }
    }

    install(StatusPages) {
        exception<AppException> { call, cause ->
            logger.warn("AppException: ${cause.message}")
            call.respond(
                HttpStatusCode.fromValue(cause.code),
                ApiResponse<Unit>(code = cause.code, msg = cause.message)
            )
        }
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Unit>(code = 500, msg = "服务器内部错误")
            )
        }
    }

    install(Authentication) {
        jwt {
            realm = config.jwt.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(config.jwt.secret))
                    .withAudience(config.jwt.audience)
                    .withIssuer(config.jwt.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(config.jwt.audience)) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Unit>(code = 401, msg = "未授权或 Token 已过期")
                )
            }
        }
    }
}
