package com.douyin.infrastructure.db.repository

import com.douyin.domain.model.User
import com.douyin.domain.repository.UserRepository
import com.douyin.infrastructure.db.table.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class UserRepositoryImpl : UserRepository {

    override suspend fun findById(id: Long): User? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun findByAccount(account: String): User? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.account eq account }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun create(user: User): User = dbQuery {
        val now = Instant.now()
        val id = UsersTable.insert {
            it[nickname] = user.nickname
            it[avatarUrl] = user.avatarUrl
            it[bio] = user.bio
            it[gender] = user.gender
            it[city] = user.city
            it[account] = user.account
            it[passwordHash] = user.passwordHash
            it[createdAt] = now
            it[updatedAt] = now
        }[UsersTable.id]
        user.copy(id = id, createdAt = now, updatedAt = now)
    }

    override suspend fun update(user: User): Boolean = dbQuery {
        val now = Instant.now()
        UsersTable.update({ UsersTable.id eq user.id }) {
            it[nickname] = user.nickname
            it[avatarUrl] = user.avatarUrl
            it[bio] = user.bio
            it[gender] = user.gender
            it[city] = user.city
            it[updatedAt] = now
        } > 0
    }

    override suspend fun existsByAccount(account: String): Boolean = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.account eq account }
            .count() > 0
    }

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id],
        nickname = this[UsersTable.nickname],
        avatarUrl = this[UsersTable.avatarUrl],
        bio = this[UsersTable.bio],
        gender = this[UsersTable.gender],
        city = this[UsersTable.city],
        account = this[UsersTable.account],
        passwordHash = this[UsersTable.passwordHash],
        createdAt = this[UsersTable.createdAt],
        updatedAt = this[UsersTable.updatedAt]
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
