package com.tarasovvp.cloudnativebackend.storage

import com.tarasovvp.cloudnativebackend.domain.models.DemoObject

interface DemoObjectStore {
    fun upsertAll(demoObjects: List<DemoObject>)

    fun findAll(): List<DemoObject>

    fun findById(demoObjectId: String): DemoObject?

    fun deleteById(demoObjectId: String)
}
