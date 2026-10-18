package com.tarasovvp.cloudnativebackend.http

import com.tarasovvp.cloudnativebackend.configureHttp
import com.tarasovvp.cloudnativebackend.domain.models.DemoObject
import com.tarasovvp.cloudnativebackend.mapper.DemoObjectResponseMapperImpl
import com.tarasovvp.cloudnativebackend.mapper.OwnerResponseMapperImpl
import com.tarasovvp.cloudnativebackend.service.DemoObjectServiceImpl
import com.tarasovvp.cloudnativebackend.support.InMemoryDemoObjectStore
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutesTest {
    @Test
    fun `returns all demo objects as JSON`() = testApplication {
        val existing = DemoObject(demoObjectId = "sample-1", name = "Sample")
        applicationWith(InMemoryDemoObjectStore(listOf(existing)))

        val response = client.get("/demoObjects")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("sample-1"))
    }

    @Test
    fun `creates a demo object`() = testApplication {
        val store = InMemoryDemoObjectStore()
        applicationWith(store)

        val response = client.post("/demoObjects") {
            contentType(ContentType.Application.Json)
            setBody("""[{"demoObjectId":"sample-1","name":"Sample"}]""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Sample", store.findById("sample-1")?.name)
    }

    @Test
    fun `returns bad request for an object without id`() = testApplication {
        applicationWith(InMemoryDemoObjectStore())

        val response = client.post("/demoObjects") {
            contentType(ContentType.Application.Json)
            setBody("""[{"name":"Missing ID"}]""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `returns not found for an unknown id`() = testApplication {
        applicationWith(InMemoryDemoObjectStore())

        assertEquals(
            HttpStatusCode.NotFound,
            client.get("/demoObjects/unknown").status,
        )
    }

    @Test
    fun `deletes a demo object`() = testApplication {
        val store = InMemoryDemoObjectStore(listOf(DemoObject(demoObjectId = "sample-1")))
        applicationWith(store)

        val response = client.delete("/demoObjects/sample-1")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(null, store.findById("sample-1"))
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.applicationWith(
        store: InMemoryDemoObjectStore,
    ) {
        application {
            configureHttp(
                json = Json { ignoreUnknownKeys = true },
                demoObjectService = DemoObjectServiceImpl(store),
                demoObjectResponseMapper = DemoObjectResponseMapperImpl(OwnerResponseMapperImpl()),
            )
        }
    }
}
