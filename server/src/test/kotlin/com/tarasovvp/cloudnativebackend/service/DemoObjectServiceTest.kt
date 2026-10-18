package com.tarasovvp.cloudnativebackend.service

import com.tarasovvp.cloudnativebackend.domain.models.DemoObject
import com.tarasovvp.cloudnativebackend.support.InMemoryDemoObjectStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DemoObjectServiceTest {
    @Test
    fun `inserts and reads demo objects`() = runTest {
        val service = DemoObjectServiceImpl(InMemoryDemoObjectStore())
        val objectToInsert = DemoObject(demoObjectId = "sample-1", name = "Sample")

        service.insertDemoObjects(listOf(objectToInsert))

        assertEquals(listOf(objectToInsert), service.getDemoObjects())
        assertEquals(objectToInsert, service.getDemoObjectById("sample-1"))
    }

    @Test
    fun `rejects objects without an id`() = runTest {
        val service = DemoObjectServiceImpl(InMemoryDemoObjectStore())

        assertFailsWith<IllegalArgumentException> {
            service.insertDemoObjects(listOf(DemoObject(name = "Missing ID")))
        }
    }

    @Test
    fun `deletes a demo object`() = runTest {
        val objectToDelete = DemoObject(demoObjectId = "sample-1")
        val service = DemoObjectServiceImpl(InMemoryDemoObjectStore(listOf(objectToDelete)))

        service.deleteDemoObjectById("sample-1")

        assertNull(service.getDemoObjectById("sample-1"))
    }
}
