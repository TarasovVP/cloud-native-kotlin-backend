package com.tarasovvp.cloudnativebackend.storage

import com.tarasovvp.cloudnativebackend.ServerDatabaseQueries
import com.tarasovvp.cloudnativebackend.domain.models.DemoObject
import com.tarasovvp.cloudnativebackend.mapper.toDemoObject

class SqlDelightDemoObjectStore(
    private val queries: ServerDatabaseQueries,
) : DemoObjectStore {
    override fun upsertAll(demoObjects: List<DemoObject>) {
        queries.transaction {
            demoObjects.forEach { demoObject ->
                queries.insertDemoObjectWithOwner(
                    demoObjectId = requireNotNull(demoObject.demoObjectId),
                    name = demoObject.name,
                    htmlUrl = demoObject.htmlUrl,
                    description = demoObject.description,
                    login = demoObject.owner?.login,
                    ownerId = demoObject.owner?.ownerId,
                    avatarUrl = demoObject.owner?.avatarUrl,
                    url = demoObject.owner?.url,
                )
            }
        }
    }

    override fun findAll(): List<DemoObject> =
        queries.getDemoObjectWithOwners().executeAsList().map { it.toDemoObject() }

    override fun findById(demoObjectId: String): DemoObject? =
        queries.getDemoObjectWithOwnerById(demoObjectId).executeAsOneOrNull()?.toDemoObject()

    override fun deleteById(demoObjectId: String) {
        queries.deleteDemoObjectWithOwnerById(demoObjectId)
    }
}
