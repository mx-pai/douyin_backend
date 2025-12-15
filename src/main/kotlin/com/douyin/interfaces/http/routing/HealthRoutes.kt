package com.douyin.interfaces.http.routing

import com.douyin.interfaces.http.dto.ApiResponse
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureHealthRoutes() {
    get("/health") {
        call.respondText("OK", status = HttpStatusCode.OK)
    }

    get("/api/v1/health") {
        call.respond(ApiResponse(data = mapOf("status" to "ok")))
    }
}
