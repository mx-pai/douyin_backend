package com.douyin.domain.model

import java.time.Instant
import java.util.Base64

data class Cursor(
    val timestamp: Instant,
    val id: Long
) {
    fun encode(): String {
        val str = "${timestamp.toEpochMilli()}:$id"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(str.toByteArray())
    }

    companion object {
        fun decode(cursor: String?): Cursor? {
            if (cursor.isNullOrBlank()) return null
            return try {
                val decoded = String(Base64.getUrlDecoder().decode(cursor))
                val parts = decoded.split(":")
                if (parts.size == 2) {
                    Cursor(
                        timestamp = Instant.ofEpochMilli(parts[0].toLong()),
                        id = parts[1].toLong()
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}
