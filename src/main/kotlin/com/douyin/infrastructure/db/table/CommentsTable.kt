package com.douyin.infrastructure.db.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object CommentsTable : Table("comments") {
    val id = long("id").autoIncrement()
    val noteId = long("note_id").references(NotesTable.id)
    val userId = long("user_id").references(UsersTable.id)
    val parentId = long("parent_id").nullable()
    val content = text("content")
    val location = varchar("location", 100).nullable()
    val likeCount = long("like_count").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
