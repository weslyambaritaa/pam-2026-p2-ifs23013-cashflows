package org.delcom

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import org.koin.ktor.plugin.Koin
import org.delcom.data.cashFlowModule

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // 1. Dependency Injection (Koin)
    install(Koin) {
        modules(cashFlowModule)
    }

    // 2. Serialization (JSON)
    install(ContentNegotiation) {
        json()
    }

    // 3. CORS (Agar bisa diakses frontend/postman)
    install(CORS) {
        anyHost()
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowMethod(io.ktor.http.HttpMethod.Patch)
    }

    // 4. Routing
    configureRouting()
}