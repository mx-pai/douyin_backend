package com.douyin.domain.repository

import com.douyin.domain.model.User

interface UserRepository {
    suspend fun findById(id: Long): User?
    suspend fun findByAccount(account: String): User?
    suspend fun create(user: User): User
    suspend fun update(user: User): Boolean
    suspend fun existsByAccount(account: String): Boolean
}
