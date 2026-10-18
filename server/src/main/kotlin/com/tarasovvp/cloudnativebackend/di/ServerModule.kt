package com.tarasovvp.cloudnativebackend.di

import com.tarasovvp.cloudnativebackend.ServerDatabase
import com.tarasovvp.cloudnativebackend.ServerDatabase.Companion.Schema
import com.tarasovvp.cloudnativebackend.db.DatabaseDriverFactory
import com.tarasovvp.cloudnativebackend.domain.mappers.DemoObjectResponseMapper
import com.tarasovvp.cloudnativebackend.domain.mappers.OwnerResponseMapper
import com.tarasovvp.cloudnativebackend.mapper.DemoObjectResponseMapperImpl
import com.tarasovvp.cloudnativebackend.mapper.OwnerResponseMapperImpl
import com.tarasovvp.cloudnativebackend.service.DemoObjectService
import com.tarasovvp.cloudnativebackend.service.DemoObjectServiceImpl
import com.tarasovvp.cloudnativebackend.storage.DemoObjectStore
import com.tarasovvp.cloudnativebackend.storage.SqlDelightDemoObjectStore
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val serverModule = module {
    single { DatabaseDriverFactory() }
    single { get<DatabaseDriverFactory>().createDriver() }
    single {
        ServerDatabase(get()).apply {
            Schema.create(get())
        }
    }
    single { get<ServerDatabase>().serverDatabaseQueries }
    single<DemoObjectStore> { SqlDelightDemoObjectStore(get()) }
    single {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
    single<OwnerResponseMapper> { OwnerResponseMapperImpl() }
    single<DemoObjectResponseMapper> { DemoObjectResponseMapperImpl(get()) }
    single<DemoObjectService> { DemoObjectServiceImpl(get()) }
}
