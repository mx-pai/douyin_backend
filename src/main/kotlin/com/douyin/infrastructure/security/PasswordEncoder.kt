package com.douyin.infrastructure.security

import java.security.MessageDigest

object PasswordEncoder {
    fun encode(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, hash: String): Boolean {
        return encode(password) == hash
    }
}
