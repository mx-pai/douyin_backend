package com.douyin.infrastructure.db.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object NoteLikesTable : Table("note_likes") {
    val userId = long("user_id").references(UsersTable.id)
    val targetId = long("target_id").references(NotesTable.id)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(userId, targetId)
}

object NoteFavoritesTable : Table("note_favorites") {
    val userId = long("user_id").references(UsersTable.id)
    val targetId = long("target_id").references(NotesTable.id)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(userId, targetId)
}

object CommentLikesTable : Table("comment_likes") {
    val userId = long("user_id").references(UsersTable.id)
    val targetId = long("target_id").references(CommentsTable.id)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(userId, targetId)
}

object UserRelationsTable : Table("user_relations") {
    val userId = long("user_id").references(UsersTable.id)
    val targetId = long("target_id").references(UsersTable.id)
    val relationType = short("relation_type")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(userId, targetId)
}
