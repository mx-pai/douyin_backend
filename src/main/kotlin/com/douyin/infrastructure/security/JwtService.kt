package com.douyin.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.douyin.application.JwtConfig
import java.util.*

object JwtService {
    private lateinit var config: JwtConfig
    private lateinit var algorithm: Algorithm

    fun init(jwtConfig: JwtConfig) {
        config = jwtConfig
        algorithm = Algorithm.HMAC256(config.secret)
    }

    fun generateToken(userId: Long): String {
        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 days
            .sign(algorithm)
    }

    fun getUserId(token: String): Long? {
        return try {
            val verifier = JWT.require(algorithm)
                .withAudience(config.audience)
                .withIssuer(config.issuer)
                .build()
            val decoded = verifier.verify(token)
            decoded.getClaim("userId").asLong()
        } catch (e: Exception) {
            null
        }
    }
}
