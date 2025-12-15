package com.douyin.infrastructure.db.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val nickname = varchar("nickname", 255)
    val avatarUrl = varchar("avatar_url", 255).nullable()
    val bio = text("bio").nullable()
    val gender = varchar("gender", 50).nullable()
    val city = varchar("city", 100).nullable()
    val account = varchar("account", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
