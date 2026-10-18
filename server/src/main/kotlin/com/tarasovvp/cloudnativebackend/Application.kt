package com.tarasovvp.cloudnativebackend

import com.tarasovvp.cloudnativebackend.di.serverModule
import com.tarasovvp.cloudnativebackend.domain.mappers.DemoObjectResponseMapper
import com.tarasovvp.cloudnativebackend.http.apiRoutes
import com.tarasovvp.cloudnativebackend.service.DemoObjectService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(
        Netty,
        host = HTTP_HOST,
        port = System.getenv(PORT_ENV)?.toIntOrNull() ?: HTTP_PORT,
        module = Application::appModule,
    ).start(wait = true)
}

fun Application.appModule() {
    install(Koin) {
        modules(serverModule)
    }
    configureHttp(get(), get(), get())
}

fun Application.configureHttp(
    json: Json,
    demoObjectService: DemoObjectService,
    demoObjectResponseMapper: DemoObjectResponseMapper,
) {
    install(ContentNegotiation) {
        json(json)
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowMethod(HttpMethod.Delete)
    }
    apiRoutes(demoObjectService, demoObjectResponseMapper)
}
