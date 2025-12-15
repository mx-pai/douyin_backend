package com.douyin.domain.exception

sealed class AppException(override val message: String, val code: Int) : RuntimeException(message)

class BadRequestException(message: String = "请求参数错误") : AppException(message, 400)
class UnauthorizedException(message: String = "未授权") : AppException(message, 401)
class ForbiddenException(message: String = "禁止访问") : AppException(message, 403)
class NotFoundException(message: String = "资源不存在") : AppException(message, 404)
class ConflictException(message: String = "资源冲突") : AppException(message, 409)
class InternalServerException(message: String = "服务器内部错误") : AppException(message, 500)
