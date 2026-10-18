package com.tarasovvp.cloudnativebackend.support

import com.tarasovvp.cloudnativebackend.domain.models.DemoObject
import com.tarasovvp.cloudnativebackend.storage.DemoObjectStore

class InMemoryDemoObjectStore(
    initialObjects: List<DemoObject> = emptyList(),
) : DemoObjectStore {
    private val objects = initialObjects.associateByTo(linkedMapOf()) {
        requireNotNull(it.demoObjectId)
    }

    override fun upsertAll(demoObjects: List<DemoObject>) {
        demoObjects.forEach { objects[requireNotNull(it.demoObjectId)] = it }
    }

    override fun findAll(): List<DemoObject> = objects.values.toList()

    override fun findById(demoObjectId: String): DemoObject? = objects[demoObjectId]

    override fun deleteById(demoObjectId: String) {
        objects.remove(demoObjectId)
    }
}
