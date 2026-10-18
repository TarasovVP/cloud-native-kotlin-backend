package com.tarasovvp.cloudnativebackend.service

import com.tarasovvp.cloudnativebackend.domain.models.DemoObject
import com.tarasovvp.cloudnativebackend.storage.DemoObjectStore

class DemoObjectServiceImpl(
    private val store: DemoObjectStore,
) : DemoObjectService {
    override suspend fun insertDemoObjects(demoObjects: List<DemoObject>) {
        require(demoObjects.all { !it.demoObjectId.isNullOrBlank() }) {
            "Every demo object must have a non-blank demoObjectId"
        }
        store.upsertAll(demoObjects)
    }

    override suspend fun getDemoObjects(): List<DemoObject> = store.findAll()

    override suspend fun getDemoObjectById(demoObjectId: String): DemoObject? = store.findById(demoObjectId)

    override suspend fun deleteDemoObjectById(demoObjectId: String) {
        store.deleteById(demoObjectId)
    }
}
