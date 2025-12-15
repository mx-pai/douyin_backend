package com.douyin.interfaces.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val nickname: String,
    val avatar: String?,
    val bio: String?,
    val gender: String?,
    val city: String?
)

@Serializable
data class RegisterRequest(
    val account: String,
    val password: String,
    val nickname: String
)

@Serializable
data class LoginRequest(
    val account: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserDto
)
