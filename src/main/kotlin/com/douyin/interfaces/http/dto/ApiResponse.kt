package com.douyin.interfaces.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int = 0,
    val msg: String = "ok",
    val data: T? = null,
    val requestId: String? = null
)
