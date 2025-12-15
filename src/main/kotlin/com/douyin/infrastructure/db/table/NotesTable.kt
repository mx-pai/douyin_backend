package com.douyin.infrastructure.db.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object NotesTable : Table("notes") {
    val id = long("id").autoIncrement()
    val authorId = long("author_id").references(UsersTable.id)
    val title = varchar("title", 255).nullable()
    val coverUrl = varchar("cover_url", 255).nullable()
    val coverWidth = integer("cover_width").nullable()
    val coverHeight = integer("cover_height").nullable()
    val isVideo = bool("is_video").default(false)
    val mediaUrl = varchar("media_url", 255).nullable()
    val images = text("images").nullable() // 存储 JSON 字符串，手动序列化
    val likeCount = long("like_count").default(0)
    val commentCount = long("comment_count").default(0)
    val favoriteCount = long("favorite_count").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val isPublic = bool("is_public").default(true)
    val status = short("status").default(0)

    override val primaryKey = PrimaryKey(id)
}
