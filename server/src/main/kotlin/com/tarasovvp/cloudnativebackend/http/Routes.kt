package com.tarasovvp.cloudnativebackend.http

import com.tarasovvp.cloudnativebackend.domain.mappers.DemoObjectResponseMapper
import com.tarasovvp.cloudnativebackend.domain.responses.DemoObjectResponse
import com.tarasovvp.cloudnativebackend.service.DemoObjectService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.apiRoutes(
    demoObjectService: DemoObjectService,
    demoObjectResponseMapper: DemoObjectResponseMapper,
) {
    routing {
        post("/demoObjects") {
            val request = runCatching { call.receive<List<DemoObjectResponse>>() }.getOrElse {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            try {
                demoObjectService.insertDemoObjects(
                    demoObjectResponseMapper.mapFromImplModelList(request),
                )
                call.respond(HttpStatusCode.Created)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (_: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        get("/demoObjects") {
            try {
                val response = demoObjectResponseMapper.mapToImplModelList(
                    demoObjectService.getDemoObjects(),
                )
                call.respond(response)
            } catch (_: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        get("/demoObjects/{id}") {
            val id = call.parameters["id"]
            if (id.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            try {
                val response = demoObjectService.getDemoObjectById(id)?.let {
                    demoObjectResponseMapper.mapToImplModel(it)
                }
                if (response == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(response)
                }
            } catch (_: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        delete("/demoObjects/{id}") {
            val id = call.parameters["id"]
            if (id.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            try {
                demoObjectService.deleteDemoObjectById(id)
                call.respond(HttpStatusCode.OK)
            } catch (_: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
