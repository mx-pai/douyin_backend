package com.douyin.application

import io.ktor.server.config.*

data class AppConfig(
    val db: DatabaseConfig,
    val jwt: JwtConfig
)

data class DatabaseConfig(
    val driverClassName: String,
    val jdbcUrl: String,
    val username: String,
    val password: String
)

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String
)

fun ApplicationConfig.toAppConfig(): AppConfig {
    val storage = config("storage")
    val jwt = config("jwt")
    return AppConfig(
        db = DatabaseConfig(
            driverClassName = storage.property("driverClassName").getString(),
            jdbcUrl = storage.property("jdbcUrl").getString(),
            username = storage.property("username").getString(),
            password = storage.property("password").getString()
        ),
        jwt = JwtConfig(
            secret = jwt.property("secret").getString(),
            issuer = jwt.property("issuer").getString(),
            audience = jwt.property("audience").getString(),
            realm = jwt.property("realm").getString()
        )
    )
}
