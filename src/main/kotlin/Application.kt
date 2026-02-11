// src/main/kotlin/Application.kt
package org.delcom

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.netty.EngineMain
import io.github.cdimascio.dotenv.dotenv
import kotlinx.serialization.json.Json
import org.delcom.data.cashFlowModule
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    val dotenv = dotenv {
        directory = "."
        ignoreIfMissing = false
    }
    dotenv.entries().forEach { System.setProperty(it.key, it.value) }
    EngineMain.main(args)
}

fun Application.module() {
    install(CORS) {
        anyHost()
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowMethod(io.ktor.http.HttpMethod.Patch)
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            allowStructuredMapKeys = true
        })
    }

    install(Koin) {
        modules(cashFlowModule)
    }

    configureRouting()
}